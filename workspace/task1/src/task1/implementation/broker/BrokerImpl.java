package task1.implementation.broker;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import task1.implementation.API.Broker;
import task1.implementation.API.Channel;

public class BrokerImpl extends Broker {
	public static final int WAITING_TIME = 15; // Waiting time before a timeout in seconds
	ConcurrentHashMap<Integer, RdV> rendez_vous; // To store port and rendez-vous effective

	public BrokerImpl(String name) throws Exception {
		super(name);
		this.rendez_vous = new ConcurrentHashMap<Integer, RdV>();
		BrokerManager.self.put(name, this);
	}

	@Override
	public Channel accept(int port) throws DisconnectedException {
		RdV rdv = null;
		synchronized (rendez_vous) {
			rdv = getRendezVous(port);
			if (rdv != null)
				throw new DisconnectedException("Broker's port : " + port + " is already in the accepting queue");
			rdv = new RdV();
			rendez_vous.put(port, rdv);
			rendez_vous.notifyAll();
		}
		
		Channel channel = rdv.accept(this, port);
		return channel;
	}

	@Override
	public Channel connect(String name, int port) throws TimeoutException {
		BrokerImpl target_broker = BrokerManager.self.get(name);

		// The target broker doesn't exist yet
		if (target_broker == null)
			return null;
		
		// Create an executor to manage the timeout
		BrokerImpl broker_connect = this;
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Callable<Channel> callable = new Callable<Channel>() {
			@Override
			public Channel call() throws Exception {
				// Wait the rendez-vous creation
				Channel channel = target_broker._connect(broker_connect, port);
				return channel;
			}
		};
		// Create a future to execute the callable AND manage it
		Future<Channel> future = executor.submit(callable);
		Channel channel = null;
		try {
			executor.shutdown(); // Deny new connections
			channel = future.get(WAITING_TIME, TimeUnit.SECONDS); // Get result after a maximum waiting time
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		
		return channel;
	}

	private Channel _connect(BrokerImpl broker, int port) {
		RdV rdv = null;
		synchronized (rendez_vous) {
			rdv = getRendezVous(port);
			while (rdv == null) {
				try {
					rendez_vous.wait();
				} catch (InterruptedException e) {
					// Nothing there
				}
				
				rdv = getRendezVous(port);
			}
			rendez_vous.remove(port);
		}
		
		Channel channel = rdv.connect(this, port);
		return channel;
	}

	/**
	 * @param port : Connection's port of this broker to a rendez-vous
	 * @return the rdv if exists, null otherwise
	 */
	private RdV getRendezVous(int port) {
		RdV rdv = rendez_vous.get(port);
		return rdv;
	}
}
