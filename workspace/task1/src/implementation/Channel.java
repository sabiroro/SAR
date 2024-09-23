package implementation;

public class Channel {
	public Boolean is_disconnected;
	CircularBuffer in; // Buffer to write
	CircularBuffer out; // Buffer to read
	
	/**
	 * @param in : Buffer to write
	 * @param out : buffer to read
	 */
	public Channel(CircularBuffer in, CircularBuffer out, Boolean is_disconnected) {
		this.is_disconnected = is_disconnected;
		this.in = in;
		this.out = out;
	}

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

	public synchronized void disconnect() {
		is_disconnected = true;
	}

	public boolean disconnected() {
		return is_disconnected;
	}
}