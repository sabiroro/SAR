package implementation;

import java.util.HashMap;

import task1.implementation.broker.BrokerImpl;
import task1.implementation.broker.BrokerManager;

public class BrokerManager {
	public static BrokerManager self; // To create a singleton
	private HashMap<String, BrokerImpl> brokers = new HashMap<>(); // Static manager to get all the local brokers

	// Get or create the singleton
	static BrokerManager getSelf() {
		return self;
	}

	static {
		self = new BrokerManager();
	}

	// To initialize the brokers' buffer
	private BrokerManager() {
		brokers = new HashMap<String, BrokerImpl>();
	}

	public synchronized HashMap<String, BrokerImpl> getBrockers() { // Static method to get the buffer
		return brokers;
	}

	public synchronized BrokerImpl get(String name) {
		return brokers.get(name);
	}
	
	public synchronized BrokerImpl remove(String name) {
		BrokerImpl b_removed = brokers.remove(name);
		return b_removed;
	}
	
	public synchronized void removeAllBrokers() {
		brokers.clear();
	}

	/**
	 * 
	 * @param name : Broker's name
	 * @param b    : Broker to save
	 * @return true if the broker's name doesn't exist in the manager, false
	 *         otherwise
	 */
	public synchronized void put(String name, BrokerImpl b) {
		if (get(name) != null)
			throw new IllegalStateException("Broker's name : " + name + " already exists");

		brokers.put(name, b);
	}
}
