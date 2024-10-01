package implementation.abstractclasses;

public abstract class MasterBroker {
	public String name;

	public abstract MasterChannel accept(int port);

	public abstract MasterChannel connect(String name, int port);
}
