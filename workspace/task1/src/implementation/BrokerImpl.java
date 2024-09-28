package implementation;

import java.util.concurrent.ConcurrentHashMap;

import implementation.API.Broker;
import implementation.API.Channel;

public class BrokerImpl extends Broker {
	public static final int WAITING_TIME = 15; // Waiting time before a timeout in seconds
	ConcurrentHashMap<Integer, RdV> rendez_vous; // To store port and rendez-vous effective

	public BrokerImpl(String name) throws Exception {
		super(name);
		this.rendez_vous = new ConcurrentHashMap<Integer, RdV>();
		BrokerManager.self.put(name, this);
	}

	@Override
	public synchronized Channel accept(int port) {
		RdV rdv = new RdV();
		rendez_vous.put(port, rdv);
		Channel channel = null;
		try {
			channel = rdv.accept(this, port);
		} catch (InterruptedException e) {
			// Nothing there
		}
		return channel;
	}

	@Override
	public synchronized Channel connect(String name, int port) {
		BrokerImpl target_broker = BrokerManager.self.get(name);

		// The target broker doesn't exist yet
		if (target_broker == null)
			return null;

		// Create an executor to manage the timeout
//		ExecutorService executor = Executors.newSingleThreadExecutor();
//		Callable<RdV> callable = new Callable<RdV>() {
//			@Override
//			public RdV call() throws Exception {
//				// Wait the rendez-vous creation
//				RdV rdv_target_callable = target_broker.getRendezVous(port);
//				return rdv_target_callable;
//			}
//		};
//		// Create a future to execute the callable AND manage it
//		Future<RdV> future = executor.submit(callable);
//		RdV rdv_target = null;
//		try {
//			executor.shutdown(); // Deny new connections
//			rdv_target = future.get(WAITING_TIME, TimeUnit.SECONDS); // Get result after a maximum waiting time
//		} catch (InterruptedException | ExecutionException e) {
//			e.printStackTrace();
//		} catch (TimeoutException e) {
//			return null; // Timed out
//		}

		Channel channel = _connect(target_broker, port);
		
		return channel;
	}

	private Channel _connect(BrokerImpl broker, int port) {
		RdV rdv = null;
		synchronized (rendez_vous) {
			while (rdv == null)
				rdv = broker.getRendezVous(port);
				
			rendez_vous.remove(port);
		}
		Channel channel = rdv.connect(broker, port);
		
		return channel;
	}

	/**
	 * @param port : Connection's port of this broker to a rendez-vous
	 * @return the rdv if exists, null otherwise
	 */
	private RdV getRendezVous(int port) {
		RdV rdv = this.rendez_vous.get(port);
		return rdv;
	}
}
