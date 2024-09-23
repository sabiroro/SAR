package implementation;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class BrokerManager {
	public static ConcurrentHashMap<String, Broker> buff; // Static manager to get all the local brokers

	public static ConcurrentMap<String, Broker> getBuff() { // Static method to get the buffer
		return buff;
	}

	public static Broker get(String name) {
		return buff.get(name);
	}
	
	/**
	 * 
	 * @param name : Broker's name
	 * @param b : Broker to save
	 * @return true if the broker's name doesn't exist in the manager, false otherwise
	 */
	public static boolean put(String name, Broker b) {
		if (get(name) == null) {
			buff.put(name, b);
			return true;
		}
		
		return false;
	}
}
