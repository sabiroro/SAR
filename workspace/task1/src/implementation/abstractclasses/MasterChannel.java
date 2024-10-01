package implementation.abstractclasses;

import implementation.DisconnectedException;

public abstract class MasterChannel {
	// For Channel
	public abstract int read(byte[] bytes, int offset, int length) throws DisconnectedException;

	public abstract int write(byte[] bytes, int offset, int length) throws DisconnectedException;

	public abstract void disconnect();

	public abstract boolean disconnected();

	// For MessageQueue
	public abstract void send(byte[] bytes, int offset, int length) throws DisconnectedException;

	public abstract byte[] receive() throws DisconnectedException;

	public abstract void close();

	public abstract boolean closed();
}