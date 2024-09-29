package implementation;

import implementation.API.Broker;
import implementation.API.Channel;

public class ChannelImpl extends Channel {
	final int CIRCULAR_BUFFER_SIZE = 10;
	int port;
	boolean is_disconnected;
	boolean is_dangling; // True if the opposite channel is disconnected, false otherwise
	ChannelImpl remote_channel;
	CircularBuffer in; // Buffer to write (for remote broker)
	CircularBuffer out; // Buffer to read (from remote broker)

	protected ChannelImpl(Broker broker, int port) {
		super(broker);
		this.port = port;
		this.is_disconnected = false;
		this.is_dangling = false;
		this.remote_channel = null;
		this.out = null;
		this.in = new CircularBuffer(CIRCULAR_BUFFER_SIZE);
	}

	protected void connect(ChannelImpl remote_channel) {
		// Local values
		this.remote_channel = remote_channel;
		this.out = remote_channel.in;

		// Remote values
		remote_channel.remote_channel = this;
		remote_channel.out = this.in;
	}

	@Override
	public int read(byte[] bytes, int offset, int length) throws DisconnectedException {
		if (is_disconnected)
			throw new DisconnectedException("The channel is disconnected !");

		int bytes_read = 0;
		try {
			while (bytes_read == 0) {
				synchronized (out) {
					while (out.empty()) {
						if (is_dangling || is_disconnected) {
							throw new DisconnectedException();
						}
						try {
							out.wait();
						} catch (InterruptedException e) {
							// Nothing there
						}
					}
				}

				while (!out.empty() && bytes_read < length) {
					byte byte_value = out.pull();
					bytes[offset + bytes_read] = byte_value;
					bytes_read++;
				}

				if (bytes_read != 0) {
					synchronized (out) {
						out.notify();
					}
				}
			}
		}

		catch (DisconnectedException e) {
			if (!is_disconnected) {
				is_disconnected = true;
				synchronized (out) {
					out.notify();
				}
			}
			throw e;
		}

		return bytes_read;
	}

	@Override
	public int write(byte[] bytes, int offset, int length) throws DisconnectedException {
		if (is_disconnected)
			throw new DisconnectedException("The channel is disconnected !");

		int bytes_wrote = 0;
		try {
			while (bytes_wrote == 0) {
				synchronized (in) {
					while (in.full()) {
						if (is_dangling || is_disconnected) {
							throw new DisconnectedException();
						}
						try {
							in.wait();
						} catch (InterruptedException e) {
							// Nothing there
						}
					}
				}

				while (!in.full() && bytes_wrote < length) {
					in.push(bytes[offset + bytes_wrote]);
					bytes_wrote++;
				}

				if (bytes_wrote != 0) {
					synchronized (in) {
						in.notify();
					}
				}
			}
		}

		catch (DisconnectedException e) {
			if (!is_disconnected) {
				is_disconnected = true;
				synchronized (in) {
					in.notify();
				}
			}
			throw e;
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
