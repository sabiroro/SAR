package task3.implementation.event;

import java.util.LinkedList;

public class EventPump extends Thread {
	public static EventPump self;
	public LinkedList<Runnable> queue;
	private boolean is_running;
	
	// To ensure a singleton
	static {
		self = new EventPump();
		self.is_running = true;
		self.start();
	}
	
	static void getSelf() {
		self = new EventPump();
	}
	
	private EventPump() {
		queue = new LinkedList<Runnable>();
	}

	public synchronized void run() {
		Runnable r;
		while (true && self.is_running) {
			r = queue.poll();
			while (r != null) {
				r.run();
				r = queue.poll();
			}
			sleep();
		}
	}

	public synchronized void post(Runnable r) {
		queue.add(r); // at the end...
		notify();
	}

	private void sleep() {
		try {
			wait();
		} catch (InterruptedException ex) {
			// nothing to do here.
		}
	}
	
	public synchronized void stopPump() {
		self.is_running = false;
		notify();
	}
}
