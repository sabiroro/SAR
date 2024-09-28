package implementation;

import implementation.API.Channel;

public class ChannelImpl extends Channel {
	public boolean is_disconnected;
	CircularBuffer in; // Buffer to write
	CircularBuffer out; // Buffer to read

	/**
	 * @param in  : Buffer to write
	 * @param out : buffer to read
	 */
	public ChannelImpl(CircularBuffer in, CircularBuffer out) {
		this.is_disconnected = false;
		this.in = in;
		this.out = out;
	}

	@Override
	public int read(byte[] bytes, int offset, int length) throws DisconnectedException {
		if (is_disconnected)
			throw new DisconnectedException("The channel is disconnected !");

		int bytes_read = 0;
		while (!out.empty() && bytes_read < length) {
			out.pull();
			bytes_read++;
		}
		return bytes_read;
	}

	@Override
	public int write(byte[] bytes, int offset, int length) throws DisconnectedException {
		if (is_disconnected)
			throw new DisconnectedException("The channel is disconnected !");

		int bytes_wrote = 0;
		while (!in.full() && bytes_wrote < length) {
			if (is_disconnected)
				throw new DisconnectedException("The channel is disconnected !");

			in.push(bytes[offset]);
			bytes_wrote++;
		}
		return bytes_wrote;
	}

	@Override
	public synchronized void disconnect() {
		is_disconnected = true;
	}

	@Override
	public boolean disconnected() {
		return is_disconnected;
	}
}
