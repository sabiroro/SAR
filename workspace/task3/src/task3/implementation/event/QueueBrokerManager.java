package task3.implementation.event;

import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import task3.implementation.queue.QueueBrokerImpl;

public class QueueBrokerManager {
	public static QueueBrokerManager self; // To create a singleton
	private HashMap<String, QueueBrokerImpl> queuebrokers = new HashMap<>(); // Static manager to get all the local
																				// queuebrokers

	private final int POOL_THREAD_NUMBER = 5;
	private Executor exec;

	static {
		self = new QueueBrokerManager();
	}

	// Get or create the singleton
	static QueueBrokerManager getSelf() {
		return self;
	}

	// To initialize the brokers' buffer
	private QueueBrokerManager() {
		queuebrokers = new HashMap<String, QueueBrokerImpl>();

		exec = Executors.newFixedThreadPool(POOL_THREAD_NUMBER, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(r);
				thread.setName("SendPool thread");
				return thread;
			}
		});
	}

	public Executor getExecutor() {
		return exec;
	}

	public synchronized HashMap<String, QueueBrokerImpl> getQueueBrockers() { // Static method to get the buffer
		return queuebrokers;
	}

	public synchronized QueueBrokerImpl get(String name) {
		return queuebrokers.get(name);
	}

	public synchronized QueueBrokerImpl remove(String name) {
		QueueBrokerImpl qb_removed = queuebrokers.remove(name);
		return qb_removed;
	}

	public synchronized void removeAllQueueBrokers() {
		queuebrokers.clear();
	}

	/**
	 * 
	 * @param name : Queuebroker's name
	 * @param b    : Queuebroker to save
	 * @return true if the queuebroker's name doesn't exist in the manager, false
	 *         otherwise
	 */
	public synchronized void put(String name, QueueBrokerImpl qb) {
		if (get(name) != null)
			throw new IllegalStateException("Queuebroker's name : " + name + " already exists");

		queuebrokers.put(name, qb);
	}
}
