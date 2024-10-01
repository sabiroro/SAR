package implementation.API;

public abstract class Task extends Thread {
	public Broker broker;
	public QueueBroker queuebroker;
	public Runnable runnable;

	public Task(Broker b, Runnable r) {
		this.broker = b;
		this.queuebroker = null;
		this.runnable = r;
	};
	
	public Task(QueueBroker b, Runnable r) throws Exception {
		throw new Exception("NYI");
	};

	public abstract Broker getBroker();

	public abstract QueueBroker getQueueBroker() throws Exception;
	
	public static Task getTask() {
		Task t = (Task) Thread.currentThread();
		return t;
	}
}