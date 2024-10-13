package task3.tests;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeoutException;

import task2.implementation.broker.BrokerManager;
import task2.implementation.broker.DisconnectedException;
import task3.implementation.API.MessageQueue;
import task3.implementation.API.MessageQueue.Listener;
import task3.implementation.API.QueueBroker;
import task3.implementation.API.QueueBroker.AcceptListener;
import task3.implementation.API.QueueBroker.ConnectListener;
import task3.implementation.event.EventPump;
import task3.implementation.queue.QueueBrokerImpl;

public class TestEvent {
	private static void clean_previous_test() {
		BrokerManager.self.removeAllBrokers();
		EventPump.self.restartPump();
	}

	public static void main(String[] args) {
		try {
//			clean_previous_test();
//			test1();
//			clean_previous_test();
//			test2(1, 1);
//			clean_previous_test();
//			test2(10, 2);
			clean_previous_test();
			test3(20);
			clean_previous_test();
			test4();
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("	The test has failed : " + e.getMessage());
			System.exit(-1);
		}
	}

	// Echo server (from the specification doc)
	public static void test1() throws Exception {
		System.out.println("Test 1 in progress...");
		Semaphore sm = new Semaphore(0); // Allows to block the execution until the echo message

		QueueBroker client = new QueueBrokerImpl("client");
		QueueBroker server = new QueueBrokerImpl("server");
		int connection_port = 6969;

		client.connect("server", connection_port, new ConnectListener() {
			@Override
			public void connected(MessageQueue queue) {
				queue.setListener(new Listener() {
					@Override
					public void received(byte[] msg) {
						System.out.println("	-> Message echoed : " + new String(msg));
						queue.close();
					}

					@Override
					public void closed() {
						System.out.println("	-> Connection closed (client)");
						sm.release(); // Allows to end the test
					}
				});

				queue.send("Hello world!".getBytes());
			}

			@Override
			public void refused() {
				System.out.println("	-> Connection refused (client)");
				throw new IllegalStateException("	-> Connection refused (client)");
			}
		});

		server.bind(connection_port, new AcceptListener() {
			@Override
			public void accepted(MessageQueue queue) {
				queue.setListener(new Listener() {
					@Override
					public void received(byte[] msg) {
						queue.send(msg);
						queue.close();
					}

					@Override
					public void closed() {
						server.unbind(connection_port);
						System.out.println("	-> Connection closed (server)");
						sm.release(); // Allows to end the test
					}
				});
			}
		});

		sm.acquire(2); // Waits the end of the test
		System.out.println("Test 1 done !\n");
	}

	private static void echo_client(QueueBroker client, int connection_port, Semaphore sm) throws TimeoutException {
		echo_client(client, connection_port, sm, true);
	}

	private static void echo_client(QueueBroker client, int connection_port, Semaphore sm, boolean try_to_reconnect)
			throws TimeoutException {
		client.connect("server", connection_port, new ConnectListener() {
			@Override
			public void connected(MessageQueue queue) {
				queue.setListener(new Listener() {
					@Override
					public void received(byte[] msg) {
						System.out.println("	-> Echo message (" + client.name + ") : " + new String(msg));
						queue.close();
					}

					@Override
					public void closed() {
						System.out.println("	-> Connection closed (" + client.name + ")");
						sm.release();
					}
				});

				queue.send(("Hello world! - By " + client.name).getBytes());
			}

			@Override
			public void refused() {
				System.out.println("	-> Connection refused (" + client.name + ")...");
				if (try_to_reconnect) {
					try {
						System.out.println("	-> New connecting try (" + client.name + ")...");
						System.out.println("	-> Try to reconnect " + client.name + "...");
						echo_client(client, connection_port, sm, false);
					} catch (TimeoutException e) {
						// Nothing there
					}
				}
			}
		});

	}

	private static void echo_server(QueueBroker server, int connection_port) throws DisconnectedException {
		server.bind(connection_port, new AcceptListener() {
			@Override
			public void accepted(MessageQueue queue) {
				queue.setListener(new Listener() {
					@Override
					public void received(byte[] msg) {
						queue.send(msg);
					}

					@Override
					public void closed() {
						// Nothing there
					}
				});
			}
		});
	}

	// Echo server with several clients on same port
	public static void test2(int nbre_clients, int test_number) throws Exception {
		System.out.println("Test 2." + test_number + " in progress...");
		Semaphore sm = new Semaphore(1 - nbre_clients); // Allows to block the execution until the echo message

		int connection_port = 6969;
		QueueBroker server = new QueueBrokerImpl("server");

		for (int i = 0; i < nbre_clients; i++) {
			QueueBroker client = new QueueBrokerImpl("client" + i);
			echo_client(client, connection_port, sm);
		}
		echo_server(server, connection_port);

		sm.acquire(); // Waits the end of the test
		System.out.println("Test 2." + test_number + " done !\n");
	}

	// Echo server with several clients on different ports
	public static void test3(int nbre_clients) throws Exception {
		System.out.println("Test 3 in progress...");
		Semaphore sm = new Semaphore(1 - nbre_clients); // Allows to block the execution until the echo message

		int connection_port = 6969;
		QueueBroker server = new QueueBrokerImpl("server");

		for (int i = 0; i < nbre_clients; i++) {
			QueueBroker client = new QueueBrokerImpl("client" + i);
			echo_client(client, connection_port + i, sm);
			echo_server(server, connection_port + i);
		}

		sm.acquire(); // Waits the end of the test
		System.out.println("Test 3 done !\n");
	}

	// Test the return statement of method connection
	public static void test4() throws Exception {
		System.out.println("Test 4 in progress...");

		QueueBroker client = new QueueBrokerImpl("client");
		int connection_port = 6969;

		// Initialization of server's method tests
		boolean client_connect_test = false;

		client_connect_test = client.connect("server", connection_port, null); // False
		if (client_connect_test)
			throw new Exception("The client tries to connect a not existing broker !");

		QueueBroker server = new QueueBrokerImpl("server");

		client_connect_test = client.connect("server", connection_port, null); // True
		if (!client_connect_test)
			throw new Exception("The client doesn't find the broker !");

		// Initialization of client's method tests
		boolean server_bind_test = false;
		boolean server_unbind_test = false;

		server_unbind_test = server.unbind(connection_port); // False
		if (server_unbind_test)
			throw new Exception("The server tries to unbind a not connected port !");

		server_bind_test = server.bind(connection_port, null); // True
		if (!server_bind_test)
			throw new Exception("The server can't bind a connection port !");

		server_bind_test = server.bind(connection_port, null); // False
		if (server_bind_test)
			throw new Exception("The server tries to bind an existing port !");

		server_unbind_test = server.unbind(connection_port); // True
		if (!server_unbind_test)
			throw new Exception("The server can't unbind a connected port !");

		System.out.println("Test 4 done !\n");
	}
}
