package task3.implementation.API;

public abstract class Task {
	public abstract void post(Runnable r);

	public static Task task() {
		throw new IllegalStateException("NYI -> ask teacher");
	}

	public abstract void kill();

	public abstract boolean killed();
}