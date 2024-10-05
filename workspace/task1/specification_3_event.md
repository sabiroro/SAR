# Programmation événementielle

## Mise en place

Le but de ce nouveau style de programmation est d'être sur un seul thread et de fonctionner avec un système d'events via une pompe à événements.

## QueueBroker

### Accept style
Le queuebroker peut désormais se *bind*, ouvrir un port, pour attendre une connexion extérieure signalée par un **AcceptListener**. La méthode renvoie un booléen pour indiquer si le bind a pu avoir lieu (port libre) ou pas (port déjà bind). Le bind est alors non bloquant et l'événement accepted est poussé dès que nécessaire.  
Le queuebroker peut aussi se *unbind* pour libérer un port, la méthode renvoie un booléen pour indiquer si le port a bien été libéré.

### Connect style
Le queuebroker peut se connecter à un autre queuebroker via son nom en connaissant le port de conexion. Le **ConnectListener** permet d'indiquer l'événement ayant eu lieu. Le connect est alors non bloquant et renvoie true s'il accepte de se connecter.


## MessageQueue

