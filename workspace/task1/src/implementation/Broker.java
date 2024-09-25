package implementation;

import java.util.concurrent.ConcurrentHashMap;

public class Broker {
	public static final int WAITING_TIME = 15; // Waiting time before a timeout in seconds
	String name;
	ConcurrentHashMap<Integer, RdV> rendez_vous; // To store port and rendez-vous effective

	public Broker(String name) throws Exception {
		this.name = name;
		this.rendez_vous = new ConcurrentHashMap<Integer, RdV>();
		BrokerManager.self.put(name, this);
	}

	public synchronized Channel accept(int port) throws InterruptedException {
		RdV rdv = new RdV();
		rendez_vous.put(port, rdv);
		Channel channel = rdv.accept(this, port);
		return channel;
	}

	public synchronized Channel connect(String name, int port) throws InterruptedException {
		Broker target_broker = BrokerManager.self.get(name);

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

	private Channel _connect(Broker broker, int port) {
		RdV rdv = null;
		synchronized (rendez_vous) {
			while (rdv == null)
				rdv = broker.getRendezVous(port);
				
			rendez_vous.remove(port);
		}
		Channel channel = rdv.connect();
		
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
