package implementation.API;

import implementation.DisconnectedException;

public abstract class Broker {
	public String name;

	public Broker(String name) {
		this.name = name;
	};

	public abstract Channel accept(int port) throws DisconnectedException;

	public abstract Channel connect(String name, int port);
}
