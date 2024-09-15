# Communication channels

Ce projet permet de créer un système de communication entre plusieurs tâches.


## Eléments de base

Un canal (classe **Channel**) permet la communication entre 2 tâches (classe **Task**). Une tâche peut alors être soit client soit serveur soit pair (peer-2-peer).  
Une tâche s'exécute sur un seul et même canal, mais un canal peut exécuter plusieurs tâches (mais **une seule à la fois**).  
Les tâches utilisent le canal pour envoyer des messages sous la forme de **flux d'octets**.  
Un **Broker** permet la création de ces canaux. Une tâche peut avoir plusieurs brokers (mais les noms des brokers doivent être différents). Une tâche a besoin d'un broker pour se conecter à un canal. Les brokers possèdent plusieurs ports de connexion (un port par connexion) qui lui permettent d'avoir plusieurs tâches, mais ces ports sont uniques et distincts à chaque broker.  
Un broker peut communiquer avec plusieurs tâches **en même temps**.  
Le canal envoie et reçoit des octets en supposant le cas **FIFO lossness**. La communication est **bidirectionnel** (full duplex).  


## Concurrence

Le **broker est synchronisé** (en concurrence) car plusieurs tâches peuvent arriver en même temps.  
Au contraire, le **canal n'est pas en concurrence** afin d'éviter un mélange d'octets par les tâches dans le canal en fonction des lectures et écritures.  


## Lecture/écriture

On donne un buffer au canal lors de l'écriture (**write**) afin qu'il puisse écrire le message dedans au fur et à mesure, la méthode retourne un int pour indiquer le **nombre d'octets écrit** et **-1** si on a une erreur (déconnection par exemple).  
La lecture (**read**) fonctionne sur le même principe.  
Dans les 2 cas, la valeur 0 ne peut être retournée car les 2 **méthodes sont bloquantes** et s'attendent à écrire ou lire au moins une fois ; c'est pourquoi on doit préciser un octet de départ (*offset*) ainsi que le nombre d'octets à lire/écrire.


## Déconnexion

Une déconnexion permet de couper le canal **à tout moment**. Le canal passe alors en mode *déconnecté* et les transferts/lectures d'octets sont impossibles. La ou les tâches sont alors terminées (interrompues).


