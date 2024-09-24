package implementation;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Broker {
	public static final int WAITING_TIME = 15; // Waiting time before a timeout in seconds
	String name;
	ConcurrentHashMap<Integer, RdV> rendez_vous; // To store port and rendez-vous effective

	public Broker(String name) {
		this.name = name;
		BrokerManager.put(name, this);
	}

	public Channel accept(int port) throws InterruptedException {
		RdV rdv = new RdV();
		rendez_vous.put(port, rdv);
		Channel channel = rdv.accept(this, port);
		return channel;
	}

	public Channel connect(String name, int port) throws InterruptedException {
		Broker target_broker = BrokerManager.get(name);

		// The target broker doesn't exist yet
		if (target_broker == null)
			return null;

		// Create an executor to manage the timeout
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Callable<RdV> callable = new Callable<RdV>() {
			@Override
			public RdV call() throws Exception {
				// Wait the rendez-vous creation
				RdV rdv_target_callable = target_broker.getRendezVous(port);
				return rdv_target_callable;
			}
		};
		// Create a future to execute the callable AND manage it
		Future<RdV> future = executor.submit(callable);
		RdV rdv_target = null;
		try {
			executor.shutdown(); // Deny new connections
			rdv_target = future.get(WAITING_TIME, TimeUnit.SECONDS); // Get result after a maximum waiting time
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			return null; // Timed out
		}

		Channel channel = rdv_target.connect(this);
		return channel;
	}

	/**
	 * @param port : Connection's port of this broker to a rendez-vous
	 * @return the rdv if exists, null otherwise
	 */
	public RdV getRendezVous(int port) {
		RdV rdv = this.rendez_vous.get(port);
		return rdv;
	}
}
