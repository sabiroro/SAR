package task3.implementation.event;

import task3.implementation.API.Task;

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
		//if (killed())
		//	return;

		this.runnable = r;
		pump.post(r);
	}

	@Override
	public void kill() {
		throw new IllegalStateException("NYI -> ask teacher");
//		if (!killed()) {
//			pump.queue.remove(runnable);
//			is_killed = true;
//		}
	}

	@Override
	public boolean killed() {
		throw new IllegalStateException("NYI -> ask teacher");
//		return is_killed;
	}
}
