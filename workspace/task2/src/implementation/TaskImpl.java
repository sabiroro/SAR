package implementation;

import implementation.API.Broker;
import implementation.API.QueueBroker;
import implementation.API.Task;

public class TaskImpl extends Task {
	public TaskImpl(Broker b, Runnable r) {
		super(b, r);
		this.start();
	}
	
	public TaskImpl(QueueBroker b, Runnable r) {
		super(b, r);
		this.start();
	}

	public void run() {
		runnable.run();
	}
	
	@Override
	public Broker getBroker() {
		Task t = Task.getTask();
		return t.broker;
	}

	@Override
	public QueueBroker getQueueBroker() {
		Task t = Task.getTask();
		return t.queuebroker;
	}
}
