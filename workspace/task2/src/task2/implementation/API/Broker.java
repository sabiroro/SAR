package task2.implementation.API;

import java.util.concurrent.TimeoutException;

import task2.implementation.broker.DisconnectedException;

public abstract class Broker {
	String name;
	Broker broker;
	
	public Broker(String name) {
		this.name = name;
		this.broker = this;
	};

	public abstract Channel accept(int port) throws DisconnectedException;

	public abstract Channel connect(String name, int port) throws TimeoutException;
}
