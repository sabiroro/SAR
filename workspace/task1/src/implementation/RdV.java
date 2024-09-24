package implementation;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RdV {
	static final int CIRCULAR_BUFFER_SIZE = 10;

	Broker ba; // Accepting broker
	Broker bc; // Connecting broker
	int port; // Communication port
	Channel channel_accept; // Channel to the accepting broker
	Channel channel_connect; // Channel to the connecting broker
	CircularBuffer in; // The buffer to write for the connecting task
	CircularBuffer out; // The buffer to write for the accepting task
	Semaphore waiting_queue; // The waiting connecting queue (semaphore to ensure FIFO)

	public RdV() {
		this.waiting_queue = new Semaphore(0, true);
	}

	public synchronized Channel connect(Broker b) throws InterruptedException { // Broker wanted a connection
		this.waiting_queue.acquire(); // Wait until there is a place

		this.bc = b;
		notifyAll(); // If accept is blocked

		// Create an executor to manage the timeout
//		ExecutorService executor = Executors.newSingleThreadExecutor();
//		Callable<Boolean> callable = new Callable<Boolean>() {
//			@Override
//			public Boolean call() throws Exception {
//				// Wait the rendez-vous creation
//				while (channel_connect == null) {
//					wait();
//				}
//				return true;
//			}
//		};
//		// Create a future to execute the callable AND manage it
//		Future<Boolean> future = executor.submit(callable);
//		try {
//			executor.shutdown(); // Deny new connections
//			future.get(Broker.WAITING_TIME, TimeUnit.SECONDS); // Get result after a maximum waiting time
//		} catch (InterruptedException | ExecutionException e) {
//			e.printStackTrace();
//		} catch (TimeoutException e) {
//			return null; // Timed out
//		}
		
		while (channel_connect == null) {
			wait();
		}

		return channel_connect;
	}

	public synchronized Channel accept(Broker b, int port) throws InterruptedException { // Broker expected a connection
		this.waiting_queue.release(); // At each accept, a place is release in the waiting queue (semaphore)
		
		this.ba = b;
		this.port = port;

		// We expect a connection
		while (bc == null) {
			wait();
		}

		// Create channels
		this.in = new CircularBuffer(CIRCULAR_BUFFER_SIZE);
		this.out = new CircularBuffer(CIRCULAR_BUFFER_SIZE);
		Boolean is_disconnected = false; // We use the Boolean's class to pass by reference
		this.channel_accept = new Channel(in, out, is_disconnected);
		this.channel_connect = new Channel(out, in, is_disconnected);
		notifyAll(); // If connect is blocked
		return this.channel_accept;
	}
}
