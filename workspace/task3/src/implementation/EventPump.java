package implementation;

import java.util.LinkedList;

public class EventPump extends Thread {
	public static EventPump self;
	public LinkedList<Runnable> queue;
	
	// To ensure a singleton
	static {
		self = new EventPump();
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
		while (true) {
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
}
