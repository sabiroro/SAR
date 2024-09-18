# Communication channels

Ce projet permet de créer un système de communication entre plusieurs tâches.


## Eléments de base

Un canal (classe **Channel**) est un canal de de communination qui permet la communication entre 2 tâches (classe **Task**). Une tâche peut alors être soit client soit serveur soit pair (peer-2-peer).  
Une tâche s'exécute sur un seul et même canal, mais un canal peut exécuter plusieurs tâches (mais **une seule à la fois**).  
Les tâches utilisent le canal pour envoyer des messages sous la forme de **flux d'octets**.  
Un **Broker** permet la création de ces canaux. Une tâche peut avoir plusieurs brokers (mais les noms des brokers doivent être différents). Une tâche a besoin d'un broker pour se conecter à un canal. Les brokers possèdent plusieurs ports de connexion (un port par connexion) qui lui permettent d'avoir plusieurs tâches, mais ces ports sont uniques et distincts à chaque broker.  
Un broker peut communiquer avec plusieurs tâches **en même temps**.  
Le canal envoie et reçoit des octets en supposant le cas **FIFO lossness**. La communication est **bidirectionnel** (full-duplex). Elle permet notamment d'établir une communication classique à double sens entre 2 tâches qui sont capables de lire comme d'écrire.  


## Connexion

La connexion sur un channel s'ouvre quand un accept (sur un broker A) et un connect (via le nom du broker A depuis un même broker ou un broker différent) sur le port de l'accept.  

Le connect est bloquant, ainsi, si le broker espéré n'existe pas ou n'accepte pas la connexion, le connect reste bloqué. Pour parer à cela, 2 cas sont à prévoir :
- si le broker distant acceptant n'existe pas, le connect retourne *null*.
- si le broker distant existe mais n'accepte pas la connexion, un timer de 15 secondes est mis en place en espérant la connexion, sinon une exception est levée pour interrompre le connect.


## Lecture/écriture

Le système de communication est thread-safe entre 2 tâches pour la lecture/écriture.  
La concurrence implémentée permet d'avoir une tâche qui lit et l'autre qui écrit.  
**Cependant**, les fonctions de lecture et d'écriture depuis une même tâche ne sont pas prévues pour être thread-safe.  

### Ecriture
La méthode : **int write(byte[] bytes,int offset,int length)** permet d'écrire un message sur le canal.  
La méthode prend un tableau de bytes représentant le message à envoyer entre [offset,offset+length[. Utilisant des flux d'octets, cette méthode peut être coupée. C'est pourquoi un *int* est renvoyé indiquant le numéro de l'octet écrit. 

La valeur 0 peut être retournée, bien que la **méthode est bloquante** puisqu'elle s'attend à écrire au moins une fois, cela signifie alors qu'on est bloqué.
En cas de déconnexion du canal une channel exception (DisconnectedException) est levée.

Un exemple de fonction *send* illustre un de ses usages :
```
  void send(byte[] bytes) {
    int remaining = bytes.length;
    int offset = 0;
    while (remaining!=0) {
      int n = channel.write(bytes,offset,remaining);
      offset += n;
      remaining -= n;
    }
  }
```


### Lecture
La méthode : **int read(byte[] bytes,int offset,int length)** permet de lire un message sur le canal.  
La méthode prend un tableau de bytes représentant le message à envoyer entre [offset,offset+length[. Utilisant des flux d'octets, cette méthode peut être coupée. C'est pourquoi un *int* est renvoyé indiquant le numéro de l'octet lu. 

La valeur 0 peut être retournée, bien que la **méthode est bloquante** puisqu'elle s'attend à écrire au moins une fois, cela signifie alors qu'on est bloqué.
En cas de déconnexion du canal une channel exception est levée.

La fin du flux d'octets est signalée par une DisconnectedException.

Un exemple de fonction *receive* illustre un de ses usages :
```
  void receive(byte[] bytes) throws DisconnectedException {
    int remaining = bytes.length;
    int offset = 0;
    while (remaining!=0) {
      int n = channel.read(bytes,offset,remaining);
      offset += n;
      remaining -= n;
    }
  }
```


## Déconnexion

Un canal peut être déconnecté à tout moment. La déconnexion est exécutée sur un thread de façon asynchrone.  
En cas de déconnexion, les opérations en cours doivent se terminer, 2 cas ont alors lieu :
- le write est interrompu.
- le read finit de lire dans le canal.
Lorsqu'une déconnexion a eu lieue, on ne peut plus invoqué de *read* ou de *write* (une exception serait alors levée).

Lors d'une déconnexion, un flag **disconnected** permet de savoir si le channel est toujours connecté ou non.  


## Concurrence

Le **broker est synchronisé** (en concurrence) car plusieurs tâches peuvent arriver en même temps. Mais une seule connexion est accepté par port.  
Au contraire, le **canal n'est pas en concurrence** afin d'éviter un mélange d'octets par les tâches dans le canal en fonction des lectures et écritures, c'est à la couche supérieure de gérer le problème.  

