package implementation.queue;

import java.nio.ByteBuffer;

import implementation.DisconnectedException;
import implementation.API.Channel;
import implementation.API.MessageQueue;

public class MessageQueueImpl extends MessageQueue {
	private Channel channel;

	public MessageQueueImpl(Channel channel) {
		this.channel = channel;
	}

	/**
	 * This function send a message over Channel by writing first 4 bytes of the
	 * msg's size and then the msg.
	 * @throws DisconnectedException 
	 */
	@Override
	public void send(byte[] bytes, int offset, int length) throws DisconnectedException {
		int msg_size = bytes.length;
		byte[] size_in_bytes = ByteBuffer.allocate(Integer.BYTES).putInt(msg_size).array();

		_send(size_in_bytes, 0, Integer.BYTES); // Send size
		_send(bytes, offset, length); // Send message
	}

	/**
	 * This function reads first the 4 bytes of the message size and then the
	 * message.
	 * @throws DisconnectedException 
	 */
	@Override
	public byte[] receive() throws DisconnectedException {
		// Read size
		byte[] size_in_bytes = new byte[Integer.BYTES];
		_receive(size_in_bytes, size_in_bytes.length);
		int size = ByteBuffer.wrap(size_in_bytes).getInt();
		
		// Read message
		byte[] msg_in_bytes = new byte[size];
		_receive(msg_in_bytes, size);
		
		return msg_in_bytes;
	}

	@Override
	public void close() {
		channel.disconnect();
	}

	@Override
	public boolean closed() {
		return channel.disconnected();
	}

	private void _send(byte[] bytes, int offset, int length) throws DisconnectedException {
		int bytes_sent = offset;
		while (bytes_sent < length)
			bytes_sent += channel.write(bytes, bytes_sent, length - bytes_sent);
	}

	private void _receive(byte[] bytes, int length) throws DisconnectedException {
		int bytes_received = 0;
		while (bytes_received < length)
			bytes_received += channel.read(bytes, bytes_received, length - bytes_received);
	}
}
