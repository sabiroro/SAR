package implementation.API;

import implementation.DisconnectedException;
import implementation.abstractclasses.MasterChannel;

public abstract class MessageQueue extends MasterChannel {
	public abstract void send(byte[] bytes, int offset, int length) throws DisconnectedException;

	public abstract byte[] receive() throws DisconnectedException;

	public abstract void close();

	public abstract boolean closed();
}
