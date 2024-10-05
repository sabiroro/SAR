package implementation.queue;

import java.util.concurrent.TimeoutException;

import implementation.DisconnectedException;
import implementation.API.Broker;
import implementation.API.Channel;
import implementation.API.MessageQueue;
import implementation.API.QueueBroker;

public class QueueBrokerImpl extends QueueBroker {
	public QueueBrokerImpl(Broker broker) {
		super(broker);
	}

	@Override
	public String name() {
		return super.name;
	}

	@Override
	public MessageQueue accept(int port) throws DisconnectedException {
		Channel mc = super.broker.accept(port);
		MessageQueue mq = new MessageQueueImpl((Channel) mc);
		return mq;
	}

	@Override
	public MessageQueue connect(String name, int port) throws TimeoutException {
		Channel mc = super.broker.connect(name, port);
		MessageQueue mq = new MessageQueueImpl((Channel) mc);
		return mq;
	}

}
