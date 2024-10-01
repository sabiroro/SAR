package implementation.abstractclasses;

public abstract class MasterChannel {
	// For Channel
	public abstract int read(byte[] bytes, int offset, int length);

	public abstract int write(byte[] bytes, int offset, int length);

	public abstract void disconnect();

	public abstract boolean disconnected();

	// For MessageQueue
	public abstract void send(byte[] bytes, int offset, int length);

	public abstract byte[] receive();

	public abstract void close();

	public abstract boolean closed();
}