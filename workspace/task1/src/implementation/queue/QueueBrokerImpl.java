package implementation.queue;

import implementation.API.Broker;
import implementation.API.MessageQueue;
import implementation.API.QueueBroker;

public class QueueBrokerImpl extends QueueBroker {

	public QueueBrokerImpl(Broker broker) {
		super(broker);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String name() throws Exception {
		throw new Exception("NYI");
	}

	@Override
	public MessageQueue accept(int port) throws Exception {
		throw new Exception("NYI");
	}

	@Override
	public MessageQueue connect(String name, int port) throws Exception {
		throw new Exception("NYI");
	}

}
