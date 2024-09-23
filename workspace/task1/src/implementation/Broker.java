package implementation;

import java.util.concurrent.ConcurrentHashMap;

public class Broker {
	String name;
	ConcurrentHashMap<Integer, RdV> rendez_vous; // To store port and rendez-vous effective
	
	public Broker(String name) {
		this.name = name;
		BrokerManager.getBuff().put(name, this);
	}

	public Channel accept(int port) throws InterruptedException {
		RdV rdv = new RdV();
		rendez_vous.put(port, rdv);
		Channel channel = rdv.accept(this, port);
		return channel;
	}

	public Channel connect(String name, int port) throws InterruptedException {
		Channel channel = BrokerManager.get(name).connect(name, port);
		return channel;
	}
}
