package task1.implementation.API;

import task1.implementation.broker.DisconnectedException;

public abstract class Channel {
	Broker broker;
	
	public Channel(Broker b) {
		this.broker = b;
	}
	
	public abstract int read(byte[] bytes, int offset, int length) throws DisconnectedException;

	public abstract int write(byte[] bytes, int offset, int length) throws DisconnectedException;

	public abstract void disconnect();

	public abstract boolean disconnected();
}
