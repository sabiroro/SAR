package implementation.API;

import implementation.DisconnectedException;
import implementation.abstractclasses.MasterBroker;

public abstract class QueueBroker extends MasterBroker {
	public QueueBroker(Broker broker) {
		super.broker = broker;
	};

	public abstract String name() throws Exception;

	public abstract MessageQueue accept(int port) throws DisconnectedException;

	public abstract MessageQueue connect(String name, int port);
}
