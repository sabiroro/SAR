package implementation.API;

public abstract class Task extends Thread {
	public abstract void post(Runnable r);

	public static Task task() {
		return null; // TODO during implementation
	}

	public abstract void kill();

	public abstract boolean killed();
}