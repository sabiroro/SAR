package implementation;

import implementation.API.Broker;
import implementation.API.Channel;

public class RdV {
	static final int CIRCULAR_BUFFER_SIZE = 10;

	Broker ba; // Accepting broker
	Broker bc; // Connecting broker
	int port; // Communication port
	Channel channel_accept; // Channel to the accepting broker
	Channel channel_connect; // Channel to the connecting broker
	CircularBuffer in; // The buffer to write for the connecting task
	CircularBuffer out; // The buffer to write for the accepting task

	public RdV() {
	}

	public synchronized Channel connect(Broker b, int port) { // Broker wanted a connection
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
			try {
				wait();
			} catch (InterruptedException e) {
				// Nothing there
			}
		}

		return channel_connect;
	}

	public synchronized Channel accept(Broker b, int port) throws InterruptedException { // Broker expected a connection
		this.ba = b;
		this.port = port;

		// We expect a connection
		while (bc == null) {
			wait();
		}

		// Create channels
		this.in = new CircularBuffer(CIRCULAR_BUFFER_SIZE);
		this.out = new CircularBuffer(CIRCULAR_BUFFER_SIZE);
		this.channel_accept = new ChannelImpl(in, out);
		this.channel_connect = new ChannelImpl(out, in);
		notifyAll(); // If connect is blocked
		return this.channel_accept;
	}
}
