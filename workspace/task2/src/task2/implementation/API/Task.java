package task2.implementation.API;

public abstract class Task extends Thread {
	public Broker broker;
	public QueueBroker queuebroker;
	public Runnable runnable;

	public Task(Broker b, Runnable r) {
		this.broker = b;
		this.queuebroker = null;
		this.runnable = r;
	};
	
	public Task(QueueBroker b, Runnable r) {
		this.broker = b.broker;
		this.queuebroker = b;
		this.runnable = r;
	};

	public abstract Broker getBroker();

	public abstract QueueBroker getQueueBroker();
	
	public static Task getTask() {
		Task t = (Task) Thread.currentThread();
		return t;
	}
}