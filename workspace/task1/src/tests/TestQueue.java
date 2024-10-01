package tests;

import implementation.BrokerImpl;
import implementation.BrokerManager;
import implementation.TaskImpl;
import implementation.API.Broker;
import implementation.API.MessageQueue;
import implementation.API.QueueBroker;
import implementation.API.Task;
import implementation.queue.QueueBrokerImpl;

public class TestQueue {
	public static void main(String[] args) {
		try {
			BrokerManager.self.removeAllBrokers();
			test1();
			test2(8080, 1, 1);
			test2(67294, 0, 0);
			test3();
			test4(true);
			test4(false);
			System.out.println("All tests have been done successfully !");
		}

		catch (Exception e) {
			e.printStackTrace();
			System.out.println("	The test has failed : " + e.getMessage());
		}
	}

	// Connection test
	public static void test1() throws Exception {
		System.out.println("Test 1 in progress...");

		Broker b1 = new BrokerImpl("Client");
		QueueBroker qb1 = new QueueBrokerImpl(b1);
		Task t1 = new TaskImpl(qb1, new Runnable() {

			@Override
			public void run() {
				try {
					MessageQueue mq = qb1.connect("Server", 6969);
					Thread.sleep(500);
					mq.close();

					if (!mq.closed() || qb1.name() != "Client")
						throw new Exception("	-> Channel closed or wrong client's name");
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(-1);
				}

			}
		});

		Broker b2 = new BrokerImpl("Server");
		QueueBroker qb2 = new QueueBrokerImpl(b2);
		Task t2 = new TaskImpl(qb2, new Runnable() {

			@Override
			public void run() {
				try {
					MessageQueue mq = qb2.connect("Server", 6969);
					Thread.sleep(500);
					mq.close();

					if (!mq.closed() || qb2.name() != "Server")
						throw new Exception("	-> Channel closed or wrong client's name");
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(-1);
				}

			}
		});

		t1.join();
		t2.join();

		System.out.println("Test 1 done !\n");
	}

	// Simple echo message
	private static void test2(int port, int times, int n_of_test_instance) throws Exception {
		System.out.println("Test 2." + n_of_test_instance + " in progress...");

		Broker b1 = new BrokerImpl("Client");
		QueueBroker qb1 = new QueueBrokerImpl(b1);
		Task t1 = new TaskImpl(qb1, client_runnable(qb1, port, "Server", "Je suis le message client ", times, true));

		Broker b2 = new BrokerImpl("Server");
		QueueBroker qb2 = new QueueBrokerImpl(b2);
		Task t2 = new TaskImpl(qb2, server_runnable(qb2, port, times));

		t1.join();
		t2.join();

		System.out.println("Test 2." + n_of_test_instance + " done !\n");
	}

	// Create different clients on different servers.
	private static void test3() throws Exception {
		System.out.println("Test 3 in progress...");
		int number_of_client = 5;
		Task[] task_created = new Task[number_of_client * 2];
		int port = 10000;
		int times = 10;

		for (int i = 0; i < number_of_client; i++) {
			Broker b1 = new BrokerImpl("Client");
			QueueBroker qb1 = new QueueBrokerImpl(b1);
			task_created[2 * i] = new TaskImpl(qb1,
					client_runnable(qb1, port + i, "Server", "Je suis le message client ", times, true));

			Broker b2 = new BrokerImpl("Server");
			QueueBroker qb2 = new QueueBrokerImpl(b2);
			task_created[2 * i + 1] = new TaskImpl(qb2, server_runnable(qb2, port + i, times));
		}

		for (int i = 0; i < task_created.length; i++) {
			task_created[i].join();
		}

		System.out.println("Test 3 done !\n");
	}

	// Surcharge a server with several clients
	private static void test4(boolean client_is_waiting_echo_message) throws Exception {
		System.out.println("Test 4 in progress...");
		int number_of_client = 10;
		Task[] task_created = new Task[number_of_client];
		int port = 1234;

		Broker b_serv = new BrokerImpl("Server");
		QueueBroker qb_serv = new QueueBrokerImpl(b_serv);
		Task server_task = new TaskImpl(qb_serv, real_server_runnable(qb_serv, port));

		for (int i = 0; i < number_of_client; i++) {
			Broker b_client = new BrokerImpl("Client " + i);
			QueueBroker qb_client = new QueueBrokerImpl(b_client);
			task_created[i] = new TaskImpl(qb_client,
					client_runnable(qb_client, port, "Server", "Je suis le message client ", 1, client_is_waiting_echo_message));
		}

		for (int i = 0; i < task_created.length; i++) {
			task_created[i].join();
		}

		server_task.interrupt();

		System.out.println("Test 4 done !\n");
	}

	// Internal test function to send *times* messages *msg_to_send* from
	// queuebroker *qb* on port *port* to a receiver *receiver_name*
	private static Runnable client_runnable(QueueBroker qb, int port, String receiver_name, String msg_to_send,
			int times, boolean wait_echo) {
		Runnable r = new Runnable() {

			@Override
			public void run() {
				MessageQueue mq = null;
				try {
					mq = qb.connect(receiver_name, port);
					for (int i = 0; i < times; i++) {
						String message = msg_to_send + (i + 1);
						byte[] byte_message = message.getBytes();

						if (wait_echo) {
							mq.send(byte_message, 0, byte_message.length);
							byte[] byte_receive = mq.receive();
							String echo_message = new String(byte_receive);

							if (!message.equals(echo_message))
								throw new Exception(
										"	-> Messages exchanged aren't the same :\n		-> Client's message : "
												+ message + "\n			-> Echo message : " + echo_message);
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
					System.exit(-1);
				} finally {
					if (mq != null)
						try {
							mq.close();
						} catch (Exception e) {
							// TODO A retirer après implem
						}
				}
			}
		};

		return r;
	}

	// Internal test function to echo *times* times a message receive to queuebroker
	// *qb* on port *port*
	private static Runnable server_runnable(QueueBroker qb, int port, int times) {
		Runnable r = new Runnable() {

			@Override
			public void run() {
				MessageQueue mq = null;
				try {
					mq = qb.accept(port);
					for (int i = 0; i < times; i++) {
						byte[] bytes_received = mq.receive();
						mq.send(bytes_received, 0, bytes_received.length);
					}

				} catch (Exception e) {
					e.printStackTrace();
					System.exit(-1);
				} finally {
					if (mq != null)
						try {
							mq.close();
						} catch (Exception e) {
							// TODO A retirer après implem
						}
				}
			}
		};

		return r;
	}

	private static Runnable real_server_runnable(QueueBroker qb, int port) {
		Runnable r = new Runnable() {

			@Override
			public void run() {
				MessageQueue mq = null;
				try {
					while (true) {
						mq = qb.accept(port);
						byte[] bytes_received = mq.receive();
						mq.send(bytes_received, 0, bytes_received.length);
					}

				} catch (Exception e) {
					e.printStackTrace();
					System.exit(-1);
				} finally {
					if (mq != null)
						try {
							mq.close();
						} catch (Exception e) {
							// TODO A retirer après implem
						}
				}
			}
		};

		return r;
	}
}
