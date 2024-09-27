# Message queues

Cet API permet de créer un système de communication basé sur l'envoi et la réception de messages.

## Task

Une **task** permet d'exécuter sur un thread les actions à appliquer à un broker ou un queuebroker.
> On pourra se référer à un [exemple d'utilisation](#exemple-dutilisation) dans une communication "classique" entre 2 services.

Une task permet aussi de récupérer son broker ou queuebroker associé via les méthodes *getBroker* et *getQueueBroker*.  

La tâche actuelle peut également être récupérer via *getTask*.


## QueueBroker

L'API utilise des **brokers**, nommés de façon unique par l'utilisateur, qui permettent de créer des **queuebrokers**. Ces queuebrokers permettent d'établir la communication entre 2 queuebrokers (le même ou des différents).
> NB : Les méthodes implémentées pour les brokers classiques sont utilisées de façon interne et ne doivent être manipulées ; hormis s'il s'agit bien de ce que l'on souhaite faire, se référer alors à la [documentation des brokers et channels](/specification_1_channel.md).

### Connexion (accept)
Un queuebroker peut ouvrir un port de connexion via la méthode *accept* pour permettre à un autre queuebroker de se connecter entre eux.  
Un queuebroker possède plusieurs ports de connexion mais chaque port est unique et distinct à chaque queuebroker.  
Un port ne peut posséder qu'une seule connexion à la fois ; mais un queuebroker peut avoir plusieurs connexions sur différents ports en même temps.

### Connexion (connect)
A l'inverse, un queuebroker peut se connecter à un ou plusieurs autres queuebrokers via la méthode *connect* à l'aide de la paire : (nom du queuebroker cible, port de connexion).
Le nom d'un queuebroker correspond à celui du broker donné au constructeur, celui-ci peut être récupéré avec la méthode *name*.
Cependant :
- si le queuebroker cible n'existe pas, alors **null** est retourné ;
- sinon, si au bout de 15 secondes, aucune réponse n'a encore eu lieue par le queuebroker accept, une **TimeoutException** est levée.

L'ordre d'exécution des méthodes n'ont pas d'importance, mais elles sont bloquantes tant que l'autre partie n'est pas connectée/acceptée aussi.  
Lorsque 2 queuebrokers sont connectés, les méthodes accept et connect retournent chacun un **messagequeue** distinct permettant la communication.


## MessageQueue

Un messagequeue permet d'envoyer ou de lire des messages de façon **birectionnel** supposé **FIFO lossness** entre les 2 queuebrokers ayant initier les messagequeues.  
Un messagequeue peut alors envoyer/lire des bytes à/de l'autre messagequeue.

### Envoi de messages
Un messagequeue peut envoyer un message avec la méthode *send*. Ce message est un tableau de bytes où l'on doit préciser les bornes d'envoi [offset, offset+length[.  
Le message doit contenir au moins un byte pour être envoyer, toutefois, si un tableau vide est envoyé, rien ne se passe.  
A l'inverse un message ne peut dépasser la taille de de 2^31-1 octets (environ égal à 2.1 Go) ; si c'est le cas, il faut alors segmenter le message.
> Ainsi, si l'on envoie un tableau de 2.1Go correspondant à un texte (string formée de chars), alors le texte initial peut contenir un peu plus d'un milliard de caractères (car un char vaut 2 octets, d'où : `(2^31-1) / 2 ~= 2^30` caractères, soit un peu plus de 200 millions de mots français (un mot vaut en moyenne 5 lettres)).  

En cas de fermeture du messagequeue :
- par le "local", l'envoi de nouveaux messages lève une DisconnectedException.
- par le "distant", l'envoi de nouveaux messages ne lève pas d'erreurs mais les bytes envoyés ne sont pas considérés (on écrit "dans le vide").

### Réception de message
Un messagequeue peut lire un message avec la méthode *receive*. Le message reçu est un tableau de bytes dont le récepteur est libre de convertir (notamment en string via `new String(bytes_table)`).
Les messages reçus contiennent au minimum 1 octet et au maximum 2^31-1 octets.  
En cas de fermeture du messagequeue :
- par le "local", la lecture de nouveaux messages lève une DisconnectedException.
- par le "distant", la lecture des messages en transit reste toujours possible ; cependant, lorsque plus aucun message ne circule, le messagequeue est fermé et une DisconnectedException est levée.

### Fermeture de messagequeue
Un messagequeue peut fermer la connexion **de son côté** quand il veut.  
Une fermeture locale empêche la lecture ou l'écriture de nouveaux messages, une DisconnectedException est alors levée le cas échéant.  
Une fermeture distante passe le local en mode "semi-déconnecté", le messagequeue local peut alors envoyer des messages (mais qui sont perdus) et lire les derniers messages envoyés par le distant tant qu'il en reste. S'il n'y a plus de message à lire, une nouvelle lecture lève alors une DisconnectedException.

On peut accéder à l'état d'ouverture/fermeture locale du messagequeue via la méthode *closed*.  
La méthode *close*, elle, déconnecte localement le messagequeue et fait passer le messagequeue distant en état de semi-déconnexion.


## Concurrence

Un queuebroker est **synchronisé** car plusieurs tâches peuvent être exécutées en même temps. Les états décrits précédemment restent vérifiés (un port par connexion, ...)


## Exemple d'utilisation

On présente ici un exemple d'usage de l'API (sans tenir compte des exceptions) avec l'envoi d'un message par les messagequeues :
```java
QueueBroker qb1 = new QueueBroker(new Broker("broker1"));
QueueBroker qb2 = new QueueBroker(new Broker("broker2"));

Runnable send_message = new Runnable(() -> 
    MessageQueue msg_queue = qb1.accept(8080);
    byte[] bytes_messages = "Message".getBytes();
    msg_queue.send(bytes_messages, 0, bytes_messages.length);
    msg_queue.close();
);

Runnable receive_message = new Runnable(() -> 
    MessageQueue msg_queue = qb2.connect("broker1", 8080);
    byte[] bytes_messages = msg_queue.receive();
    msg_queue.close();
);

new Task(qb1, send_message);
new Task(qb2, receive_message);

````