package implementation;

public abstract class Channel {
	public abstract int read(byte[] bytes, int offset, int length);

	public abstract int write(byte[] bytes, int offset, int length);

	public abstract void disconnect();

	public abstract boolean disconnected();
}