package implementation;

import implementation.API.Task;

public class TaskImpl extends Task {
	EventPump pump;
	boolean is_killed;
	Runnable runnable;

	public TaskImpl() {
		this.pump = EventPump.self;
		this.is_killed = false;
		this.runnable = null;
	}

	@Override
	public void post(Runnable r) {
		if (killed())
			return;

		this.runnable = r;
		pump.post(r);
	}

	@Override
	public void kill() {
		if (!killed()) {
			pump.queue.remove(runnable);
			is_killed = true;
		}
	}

	@Override
	public boolean killed() {
		return is_killed;
	}
}
