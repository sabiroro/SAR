package implementation.API;

import java.util.concurrent.TimeoutException;

import implementation.DisconnectedException;

public abstract class QueueBroker {
	protected Broker broker;
	protected String name;

	public QueueBroker(Broker broker) {
		this.broker = broker;
		this.name = broker.name;
	};

	public abstract String name();

	public abstract MessageQueue accept(int port) throws DisconnectedException;

	public abstract MessageQueue connect(String name, int port) throws TimeoutException;
}
