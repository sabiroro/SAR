package task2.implementation.broker;

public class DisconnectedException extends Exception {
	private static final long serialVersionUID = 1L;

	public DisconnectedException() {
		super();
	}

	public DisconnectedException(String reason) {
		super(reason);
	}
}
