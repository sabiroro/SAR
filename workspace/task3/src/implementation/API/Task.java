package implementation.API;

import task2.implementation.API.Task;

public abstract class Task extends Thread {
	public abstract void post(Runnable r);

	public static Task task() {
		Task t = (Task) Thread.currentThread();
		return t;
	}

	public abstract void kill();

	public abstract boolean killed();
}