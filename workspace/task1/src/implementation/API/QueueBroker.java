package implementation.API;

public abstract class QueueBroker {
	public QueueBroker(Broker broker) {
	};

	public abstract String name() throws Exception;

	public abstract MessageQueue accept(int port) throws Exception;

	public abstract MessageQueue connect(String name, int port) throws Exception;
}
