package task1.implementation.broker;

import task1.implementation.API.Broker;
import task1.implementation.API.Task;

public class TaskImpl extends Task {
	public TaskImpl(Broker b, Runnable r) {
		super(b, r);
		this.start();
	}

	public void run() {
		runnable.run();
	}
}
