package implementation.queue;

import java.nio.ByteBuffer;

import implementation.Message;
import task1.implementation.broker.DisconnectedException;
import task2.implementation.API.Channel;
import task2.implementation.API.MessageQueue;
import task2.implementation.API.Task;

public class MessageQueueImpl extends MessageQueue {
	private Channel channel;
	private Task task;
	private Listener listener;

	public MessageQueueImpl(Channel channel, Task task) throws DisconnectedException {
		this.channel = channel;
		this.task = task;
		receive();
	}

	@Override
	public void setListener(Listener l) {
		this.listener = l;
	}

	@Override
	public boolean send(byte[] bytes) {
		Message msg = new Message(bytes);
		return send(msg);
	}

	@Override
	public boolean send(byte[] bytes, int offset, int length) {
		Message msg = new Message(bytes, 0, bytes.length); // TODO A nous de segmenter les msgs ??
		return send(msg);
	}

	@Override
	public void close() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				channel.disconnect();
				task.post(new Runnable() {
					@Override
					public void run() {
						listener.closed();
					}
				});
			}
		});
		t.start();
	}

	@Override
	public boolean closed() {
		return channel.disconnected();
	}

	private void receive() throws DisconnectedException {
		Thread t = new Thread(new Runnable() {
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

						task.post(new Runnable() {
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
		});
		t.setDaemon(true);
		t.start();
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
		Thread t = new Thread(new Runnable() {
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
		});
		t.start();

		return true;
	}
}
