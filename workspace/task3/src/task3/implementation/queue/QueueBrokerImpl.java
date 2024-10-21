package task3.implementation.queue;

import java.util.HashMap;
import java.util.concurrent.TimeoutException;

import task2.implementation.API.Channel;
import task2.implementation.broker.DisconnectedException;
import task3.implementation.API.MessageQueue;
import task3.implementation.API.QueueBroker;
import task3.implementation.event.QueueBrokerManager;

public class QueueBrokerImpl extends QueueBroker {
//	private final int PORT_BACKLOG = 5; // Represents the maximum waiting queue to an accepting port
	private HashMap<Integer, Port> ports_list;

	public QueueBrokerImpl(String name) throws Exception {
		super(name);
		QueueBrokerManager.self.put(name, this);
		ports_list = new HashMap<>();
	}

	@Override
	public boolean bind(int port, AcceptListener listener) throws DisconnectedException {
		if (ports_list.containsKey((Integer) port))
			return false;
		Port port_object = new Port(port, false);
		ports_list.put(port, port_object);

		// Separate the thread-world with the event world
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					while (port_object.is_active) {
						Channel channel = broker.accept(port);
						task3.implementation.API.Task task = new task3.implementation.event.TaskImpl();
						MessageQueue msg_queue = new MessageQueueImpl(channel);

						task.post(new Runnable() {
							@Override
							public void run() {
								listener.accepted(msg_queue);
							}
						});
					}
				} catch (DisconnectedException e) {
					// Nothing there

				} catch (InterruptedException e) {
					// System.out.println(" --> Bind task interrupted successfully ! Port : " +
					// port);
				}
			}
		};

		// Create and launch task
		task2.implementation.API.Task t = new task2.implementation.broker.TaskImpl(this.broker, r, "Bind thread");
		port_object.bind_task = t;

		return true;
	}

	@Override
	public boolean unbind(int port) {
		if (!ports_list.containsKey((Integer) port))
			return false;

		Port port_object = ports_list.remove((Integer) port);
		if (!port_object.is_connecting_port) {
			port_object.bind_task.interrupt();
			port_object.is_active = false;
		}
		return true;
	}

	@Override
	public boolean connect(String name, int port, ConnectListener listener) throws TimeoutException {
		QueueBrokerImpl remote_qb = QueueBrokerManager.self.get(name);
		if (remote_qb == null) // Target broker
			// doesn't exist
			return false;

//		Port remote_port = remote_qb.ports_list.get((Integer) port);
//		if (remote_port.getBacklog() > PORT_BACKLOG)
//			return false;
		
		
		Port port_object = new Port(port, true);
		ports_list.put(port, port_object);

		// Separate the thread-world with the event world
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
//					remote_port.addBacklog();
					Channel channel = broker.connect(name, port);
//					remote_port.subBacklog();
					task3.implementation.API.Task task = new task3.implementation.event.TaskImpl();
					MessageQueue msg_queue = new MessageQueueImpl(channel);

					task.post(new Runnable() {
						@Override
						public void run() {
							listener.connected(msg_queue);
						}
					});
				} catch (TimeoutException | DisconnectedException | InterruptedException e) {
					task3.implementation.API.Task task = new task3.implementation.event.TaskImpl();
					ports_list.remove((Integer) port);
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
