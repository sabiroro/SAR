package implementation;

import implementation.API.Broker;
import implementation.API.Task;

public class TaskImpl extends Task {
	public TaskImpl(Broker b, Runnable r) {
		super(b, r);
		this.start();
	}

	public void run() {
		runnable.run();
	}
}
