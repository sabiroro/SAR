package task2.implementation.API;

import java.util.concurrent.TimeoutException;

import task2.implementation.broker.DisconnectedException;

public abstract class QueueBroker {
	protected Broker broker;
	protected String name;

	public QueueBroker(Broker broker) {
		this.broker = broker;
		this.name = broker.name;
	};

	public abstract String name();

	public abstract MessageQueue accept(int port) throws DisconnectedException, InterruptedException;

	public abstract MessageQueue connect(String name, int port) throws TimeoutException, InterruptedException;
}
