# Programmation mixte : événementielle et threadé

## Mise en place

Le but de ce nouveau projet est de fonctionner avec un seul thread par un système d'events via une pompe à événements tout en conversant la partie multi-threadé des channels. L'usage d'événements va permettre de changer les méthodes des QueueBrokers et des MessageQueues vers des méthodes **non bloquantes**.

## QueueBroker

### Accept style
Le queuebroker peut désormais se *bind*, ouvrir un port, pour attendre une connexion extérieure signalée par un **AcceptListener**. La méthode renvoie un booléen pour indiquer si le bind est possible (port libre) ou pas (port déjà bind). Le bind est alors non bloquant et l'événement *accepted* est poussé dès qu'une connexion a eu lieue.  
Pour éviter des attaques comme DDOS, on limite le nombre de connexions sur le port acceptant avec un backlog de 5. Ainsi, au-delà de cette limite, la méthode *connect* (voir ci-après) retourne **false**.  
Le queuebroker peut aussi se **unbind** pour libérer un port, la méthode renvoie un booléen pour indiquer si le port est libérable (port en cours d'utilisation) ou pas (port non attribué).  
Lorsque l'unbind est effectué, la connexion est interrompue du côté serveur.

### Connect style
Le queuebroker peut se connecter à un autre queuebroker via son nom en connaissant le port de connexion. Le **ConnectListener** permet d'indiquer l'événement ayant eu lieu. Le connect est alors non bloquant et renvoie true s'il accepte de se connecter au queuebroker spécifié ou false si cela n'est pas le cas (nom inexistant).  
L'événement *connected* est alors poussé dès que la connexion a eu lieue. Sinon, c'est l'événement *refused* qui est poussé.  
Un broker est autorisé à se connecter à lui-même.


## MessageQueue

### Send style
Le MessageQueue peut envoyer un message via la méthode *send* qui renvoie un booléen pour indiquer si l'envoi est possible ou pas (queuebroker distant déconnecté). Le message correspond à un tableau de bytes où l'on peut préciser les bornes du message [offset, offset+length[.  
Le tableau envoyé peut être modifié après l'envoi (celui-ci est copié à l'envoi).  
Lorsque le message a été envoyé, l'événement *sent* est poussé à l'émetteur.

### Close style
Le MessageQueue peut fermer (localement) sa connexion avec la méthode *close*. L'événement *closed* est alors poussé quand la fermeture a bien eu lieue.  
La méthode *closed* permet notamment de savoir si le messagequeue est fermé ou pas.

### Receive style
Le MessageQueue peut lire un message avec l'événement *received* qui est poussé dès qu'un message est disponible à la lecture locale.

### Mise en place des listeners
Le MessageQueue n'étant instancié par l'utilisateur, la méthode *setListener* permet de définir les comportements des événements cités ci-dessus (received, sent, closed) via l'interface **Listener**.


## Exemple d'utilisation

On illustre ci-dessous un exemple d'utilisation de l'API avec un echo serveur.
```java
QueueBroker client = new QueueBrokerImpl("client");
QueueBroker server = new QueueBrokerImpl("server");
int connection_port = 6969;

client.connect("server", connection_port, new ConnectListener() {
    @Override
    public void connected(MessageQueue queue) {
        queue.setListener(new Listener() {
            @Override
            public void received(byte[] msg) {
                System.out.println("	-> Message echoed : " + new String(msg));
                queue.close();
            }

            @Override
            public void sent(byte[] msg) {
                // Nothing there
            }

            @Override
            public void closed() {
                System.out.println("	-> Connection closed (client)");
            }
        });

        queue.send("Hello world!".getBytes());
    }

    @Override
    public void refused() {
        System.out.println("	-> Connection refused (client)");
        throw new IllegalStateException("	-> Connection refused (client)");
    }
});

server.bind(connection_port, new AcceptListener() {
    @Override
    public void accepted(MessageQueue queue) {
        queue.setListener(new Listener() {
            @Override
            public void received(byte[] msg) {
                queue.send(msg);
                
                // Code to hard unbind accepting port after 1 second (even if the message is not totally sent)
                //Thread t = new Thread(new Runnable() {
                //   @Override
                //   public void run() {
                //      try {
                //          Thread.sleep(1000); // Wait 1 second to send message after kill server
                //          queue.close();
                //      } catch (InterruptedException e) {
                //          // Nothing there
                //      }
                //  }
                //});
                //t.start();
            }

            @Override
            public void sent(byte[] msg) {
                queue.close();
            }

            @Override
            public void closed() {
                server.unbind(connection_port);
                System.out.println("	-> Connection closed (server)");
                sm.release(); // Allows to end the test
            }
        });
    }
});
````
