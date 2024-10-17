package task3.implementation.queue;

import java.nio.ByteBuffer;

import task2.implementation.API.Channel;
import task2.implementation.broker.BrokerManager;
import task2.implementation.broker.DisconnectedException;
import task3.implementation.API.MessageQueue;
import task3.implementation.event.Message;

public class MessageQueueImpl extends MessageQueue {
	private Channel channel;
	private Listener listener;

	public MessageQueueImpl(Channel channel, task3.implementation.API.Task task) throws DisconnectedException {
		this.channel = channel;
	}

	@Override
	public void setListener(Listener l) {
		this.listener = l;
		receive();
	}

	@Override
	public boolean send(byte[] bytes) {
		Message msg = new Message(bytes);
		return send(msg);
	}

	@Override
	public boolean send(byte[] bytes, int offset, int length) {
		Message msg = new Message(bytes, 0, bytes.length);
		return send(msg);
	}

	@Override
	public void close() {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				channel.disconnect();
				task3.implementation.API.Task task_pump = new task3.implementation.event.TaskImpl();
				task_pump.post(new Runnable() {
					@Override
					public void run() {
						listener.closed();
					}
				});
			}
		};

		new task2.implementation.broker.TaskImpl(channel.broker, r); // Create and launch task
	}

	@Override
	public boolean closed() {
		return channel.disconnected();
	}

	private void receive() {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				while (!channel.disconnected()) {
					try {
						// Read size
						byte[] size_in_bytes = new byte[Integer.BYTES];
						_receive(size_in_bytes, size_in_bytes.length);
						int size = ByteBuffer.wrap(size_in_bytes).getInt();

						// Read message
						byte[] msg_in_bytes = new byte[size];
						_receive(msg_in_bytes, size);

						task3.implementation.API.Task task_pump = new task3.implementation.event.TaskImpl();
						task_pump.post(new Runnable() {
							@Override
							public void run() {
								listener.received(msg_in_bytes);
							}
						});
					} catch (DisconnectedException e) {
						// Nothing there
					}
				}
			}
		};

		new task2.implementation.broker.TaskImpl(channel.broker, r, "Receive thread"); // Create and launch task
	}

	private void _send(byte[] bytes, int offset, int length) throws DisconnectedException {
		int bytes_sent = offset;
		while (bytes_sent < length)
			bytes_sent += channel.write(bytes, bytes_sent, length - bytes_sent);
	}

	private void _receive(byte[] bytes, int length) throws DisconnectedException {
		int bytes_received = 0;
		while (bytes_received < length && !channel.disconnected())
			bytes_received += channel.read(bytes, bytes_received, length - bytes_received);
	}

	private boolean send(Message msg) {
		if (this.channel.disconnected())
			return false;

		// Separate the thread-world with the event world
		Runnable r = new Runnable() {
			@Override
			public void run() {
				byte[] size_in_bytes = ByteBuffer.allocate(Integer.BYTES).putInt(msg.length).array();

				try {
					_send(size_in_bytes, 0, Integer.BYTES); // Send size
					_send(msg.bytes, msg.offset, msg.length); // Send message
				} catch (DisconnectedException e) {
					// Nothing there
				}
			}
		};

		// Create and launch task (by using the pool)
		BrokerManager.self.getExecutor().execute(r);

		return true;
	}
}
