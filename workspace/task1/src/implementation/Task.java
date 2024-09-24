package implementation;

public class Task extends Thread {
	private Broker broker;
	private Runnable runnable;

	public Task(Broker b, Runnable r) {
		this.broker = b;
		this.runnable = r;
		this.start();
	}

	public void run() {
		runnable.run();
	}

	public static Broker getBroker() {
		Task t = (Task) Thread.currentThread();
		return t.broker;
	}
}
