# Programmation mixte : événementielle et threadé

## Mise en place

Le but de ce nouveau projet est de fonctionner avec un seul thread par un système d'events via une pompe à événements tout en conversant la partie multi-threadé des channels. L'usage d'événements va permettre de changer les méthodes des QueueBrokers et des MessageQueues vers des méthodes **non bloquantes**.

## QueueBroker

### Accept style
Le queuebroker peut désormais se *bind*, ouvrir un port, pour attendre une connexion extérieure signalée par un **AcceptListener**. La méthode renvoie un booléen pour indiquer si le bind est possible (port libre) ou pas (port déjà bind). Le bind est alors non bloquant et l'événement *accepted* est poussé dès qu'une connexion a eu lieue.  
Le queuebroker peut aussi se *unbind* pour libérer un port, la méthode renvoie un booléen pour indiquer si le port est libérable (port en cours d'utilisation) ou pas (port non attribué).

### Connect style
Le queuebroker peut se connecter à un autre queuebroker via son nom en connaissant le port de connexion. Le **ConnectListener** permet d'indiquer l'événement ayant eu lieu. Le connect est alors non bloquant et renvoie true s'il accepte de se connecter au queuebroker spécifié ou false si cela n'est pas le cas (nom inexistant).  
L'événement *connected* est alors poussé dès que la connexion a eu lieue. Sinon, c'est l'événement *refused* qui est poussé.


## MessageQueue

### Send style
Le MessageQueue peut envoyer un message via la méthode *send* qui renvoie un booléen pour indiquer si l'envoi est possible ou pas (queuebroker distant déconnecté). Le message correspond à un tableau de bytes où l'on peut préciser les bornes du message [offset, offset+length[.

### Close style
Le MessageQueue peut fermer (localement) sa connexion avec la méthode *close*. L'événement *closed* est alors poussé quand la fermeture a bien eu lieue.  
La méthode *closed* permet notamment de savoir si le messagequeue est fermé ou pas.

### Receive style
Le MessageQueue peut lire un message avec l'événement *received* qui est poussé dès qu'un message est disponible à la lecture locale.

### Mise en place des listeners
Le MessageQueue n'étant instancié par l'utilisateur, la méthode *setListener* permet de définir les comportements des événements cités ci-dessus (received, closed) via l'interface **Listener**.


## Exemple d'utilisation

On illustre ci-dessous un exemple d'utilisation de l'API avec un echo serveur.
```java
QueueBroker client = new QueueBroker(new Broker("client"));
QueueBroker server = new QueueBroker(new Broker("server"));

client.connect("server", 6969, new ConnectListener() {
    @Override
    public void connected(MessageQueue queue) {
        queue.setListener(new Listener() {
            @Override
            public void received(byte[] msg) {
                System.out.println("Echo message : " + new String(msg));
                queue.close();
            }
            
            @Override
            public void closed() {
                client.unbind(6969);
                System.out.println("Connection closed");
            }
        });

        queue.send("Hello world!".getBytes());
    }

    @Override
    public void refused() {
        System.out.println("Connection refused");
    }
});

server.bind(6969, new AcceptListener() {
    @Override
    public void accepted(MessageQueue queue) {
        queue.setListener(new Listener() {
            @Override
            public void received(byte[] msg) {
                queue.send(msg);
                queue.close();
            }
            
            @Override
            public void closed() {
                client.unbind(6969);
                System.out.println("Connection closed");
            }
        });
    }
});
````