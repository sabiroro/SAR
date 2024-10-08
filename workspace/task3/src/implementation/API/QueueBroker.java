package implementation.API;

import java.util.concurrent.TimeoutException;

import implementation.BrokerImpl;
import implementation.DisconnectedException;

public abstract class QueueBroker {
	public Broker broker;
	public String name;
	
	public QueueBroker(String name) throws Exception {
		this.broker = new BrokerImpl(name);
		this.name = name;
	}

	public interface AcceptListener {
		public void accepted(MessageQueue queue);
	}

	public abstract boolean bind(int port, AcceptListener listener) throws DisconnectedException;

	public abstract boolean unbind(int port);

	public interface ConnectListener {
		public void connected(MessageQueue queue);

		public void refused();
	}

	public abstract boolean connect(String name, int port, ConnectListener listener) throws TimeoutException;
}
