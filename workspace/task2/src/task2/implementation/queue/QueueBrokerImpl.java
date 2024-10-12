package task2.implementation.queue;

import java.util.concurrent.TimeoutException;

import task2.implementation.API.Broker;
import task2.implementation.API.Channel;
import task2.implementation.API.MessageQueue;
import task2.implementation.API.QueueBroker;
import task2.implementation.broker.DisconnectedException;

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
