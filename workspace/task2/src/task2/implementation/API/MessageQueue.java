package task2.implementation.API;

import task2.implementation.broker.DisconnectedException;

public abstract class MessageQueue {
	public abstract void send(byte[] bytes, int offset, int length) throws DisconnectedException;

	public abstract byte[] receive() throws DisconnectedException;

	public abstract void close();

	public abstract boolean closed();
}
