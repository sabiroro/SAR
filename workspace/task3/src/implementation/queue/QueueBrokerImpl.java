package implementation.queue;

import java.util.concurrent.TimeoutException;

import task1.implementation.broker.BrokerImpl;
import task1.implementation.broker.BrokerManager;
import task1.implementation.broker.DisconnectedException;
import task1.implementation.broker.RdV;
import task1.implementation.broker.TaskImpl;
import task2.implementation.API.Channel;
import task2.implementation.API.MessageQueue;
import task2.implementation.API.QueueBroker;
import task2.implementation.API.Task;
import task2.implementation.queue.MessageQueueImpl;

public class QueueBrokerImpl extends QueueBroker {
	private Task task;

	public QueueBrokerImpl(String name) throws Exception {
		super(name);
		task = new TaskImpl();
	}

	@Override
	public boolean bind(int port, AcceptListener listener) throws DisconnectedException {
		BrokerImpl b = (BrokerImpl) super.broker;
		if (b.getRendezVous(port) != null)
			return false;

		// Separate the thread-world with the event world
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Channel channel = broker.accept(port);
					MessageQueue msg_queue = new MessageQueueImpl(channel, task);
					task.post(new Runnable() {
						@Override
						public void run() {
							listener.accepted(msg_queue);
						}
					});
				} catch (DisconnectedException e) {
					// Nothing there
				}
			}
		});
		t.start();

		return true;
	}

	@Override
	public boolean unbind(int port) {
		BrokerImpl b = (BrokerImpl) super.broker;
		RdV rdv = b.getRendezVous(port);
		if (rdv != null) {
			b.rendez_vous.remove(port);
			return true;
		}

		return false;
	}

	@Override
	public boolean connect(String name, int port, ConnectListener listener) throws TimeoutException {
		if (BrokerManager.self.get(name) == null) // Target broker doesn't exist
			return false;

		// Separate the thread-world with the event world
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Channel channel = broker.connect(name, port);
					MessageQueue msg_queue = new MessageQueueImpl(channel, task);
					task.post(new Runnable() {
						@Override
						public void run() {
							listener.connected(msg_queue);
						}
					});
				} catch (TimeoutException | DisconnectedException e) {
					task.post(new Runnable() {
						@Override
						public void run() {
							listener.refused();
						}
					});
				}
			}
		});
		t.start();

		return true;
	}

}
