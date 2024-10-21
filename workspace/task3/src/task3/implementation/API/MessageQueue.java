package task3.implementation.API;

public abstract class MessageQueue {
	public interface Listener {
		public void received(byte[] msg);

		public void sent(byte[] msg);
		
		public void closed();
	}

	public abstract void setListener(Listener l);

	public abstract boolean send(byte[] bytes);
	public abstract boolean send(byte[] bytes, int offset, int length);

	public abstract void close();

	public abstract boolean closed();
}
