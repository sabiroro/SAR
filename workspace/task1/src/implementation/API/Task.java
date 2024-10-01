package implementation.API;

public abstract class Task extends Thread {
	protected Broker broker;
	protected Runnable runnable;
	
	public Task(Broker b, Runnable r) {
		this.broker = b;
		this.runnable = r;
	};

	public static Broker getBroker() {
		Task t = (Task) Thread.currentThread();
		return t.broker;
	}
}