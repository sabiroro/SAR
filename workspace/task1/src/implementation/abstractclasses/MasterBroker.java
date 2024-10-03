package implementation.abstractclasses;

import java.util.concurrent.TimeoutException;

import implementation.DisconnectedException;
import implementation.API.Broker;

public abstract class MasterBroker {
	public String name;
	public Broker broker;

	public abstract MasterChannel accept(int port) throws DisconnectedException;

	public abstract MasterChannel connect(String name, int port) throws TimeoutException;
}
