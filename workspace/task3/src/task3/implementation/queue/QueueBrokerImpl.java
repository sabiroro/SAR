package task3.implementation.queue;

import java.util.HashMap;
import java.util.concurrent.TimeoutException;

import task2.implementation.API.Channel;
import task2.implementation.broker.BrokerManager;
import task2.implementation.broker.DisconnectedException;
import task3.implementation.API.MessageQueue;
import task3.implementation.API.QueueBroker;

public class QueueBrokerImpl extends QueueBroker {
	//private final int CONNECTION_QUEUE_SIZE = 50; // Represents the maximum port number opens at the same time 
	//private task3.implementation.API.Task task;
	private HashMap<Integer, Boolean> connection_port_list; // To know binding and using port (true for a connection
															// port, false for an accept port)

	public QueueBrokerImpl(String name) throws Exception {
		super(name);
		//task = new task3.implementation.event.TaskImpl();
		connection_port_list = new HashMap<>();
	}

	@Override
	public boolean bind(int port, AcceptListener listener) throws DisconnectedException {
		if (connection_port_list.containsKey((Integer) port))
			return false;
		connection_port_list.put(port, false);

		// Separate the thread-world with the event world
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					while (true) { // TODO rajouter un backlog
						Channel channel = broker.accept(port);
						task3.implementation.API.Task task = new task3.implementation.event.TaskImpl();
						MessageQueue msg_queue = new MessageQueueImpl(channel, task);
						task.post(new Runnable() {
							@Override
							public void run() {
								listener.accepted(msg_queue);
							}
						});
					}
				} catch (DisconnectedException e) {
					// Nothing there
				}
			}
		};

		new task2.implementation.broker.TaskImpl(this.broker, r, "Bind thread"); // Create and launch task

		return true;
	}

	@Override
	public boolean unbind(int port) {
		if (!connection_port_list.containsKey((Integer) port))
			return false;

		connection_port_list.remove((Integer) port);
		return true;
	}

	@Override
	public boolean connect(String name, int port, ConnectListener listener) throws TimeoutException {
		if (BrokerManager.self.get(name) == null || connection_port_list.containsKey((Integer) port)) // Target broker
																										// doesn't exist
			return false;
		connection_port_list.put(port, true);

		// Separate the thread-world with the event world
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					Channel channel = broker.connect(name, port);
					task3.implementation.API.Task task = new task3.implementation.event.TaskImpl();
					MessageQueue msg_queue = new MessageQueueImpl(channel, task);
					task.post(new Runnable() {
						@Override
						public void run() {
							listener.connected(msg_queue);
						}
					});
				} catch (TimeoutException | DisconnectedException e) {
					task3.implementation.API.Task task = new task3.implementation.event.TaskImpl();
					connection_port_list.remove((Integer) port);
					task.post(new Runnable() {
						@Override
						public void run() {
							listener.refused();
						}
					});
				}
			}
		};

		new task2.implementation.broker.TaskImpl(this.broker, r, "Connect thread"); // Create and launch task

		return true;
	}

}
