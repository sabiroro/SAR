package implementation.API;

import java.util.concurrent.TimeoutException;

import implementation.DisconnectedException;
import implementation.abstractclasses.MasterBroker;

public abstract class Broker extends MasterBroker {
	public Broker(String name) {
		super.name = name;
		super.broker = this;
	};

	public abstract Channel accept(int port) throws DisconnectedException;

	public abstract Channel connect(String name, int port) throws TimeoutException;
}
