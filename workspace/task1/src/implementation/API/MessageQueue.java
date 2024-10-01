package implementation.API;

public abstract class MessageQueue {
	public abstract void send(byte[] bytes, int offset, int length) throws Exception;

	public abstract byte[] receive() throws Exception;

	public abstract void close() throws Exception;

	public abstract boolean closed() throws Exception;
}
