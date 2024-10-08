package tests;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeoutException;

import implementation.BrokerManager;
import implementation.DisconnectedException;
import implementation.EventPump;
import implementation.API.MessageQueue;
import implementation.API.MessageQueue.Listener;
import implementation.API.QueueBroker;
import implementation.API.QueueBroker.AcceptListener;
import implementation.API.QueueBroker.ConnectListener;
import implementation.queue.QueueBrokerImpl;

public class TestEvent {
	public static void main(String[] args) {
		try {
			BrokerManager.self.removeAllBrokers();
			test1();
			BrokerManager.self.removeAllBrokers();
			test2(1, 1);
			BrokerManager.self.removeAllBrokers();
			test2(10, 2);
			BrokerManager.self.removeAllBrokers();
			test3(20);
			BrokerManager.self.removeAllBrokers();
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
		Semaphore sm = new Semaphore(-1); // Allows to block the execution until the echo message

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
						queue.close(); // TODO prbl de déconnexion
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
		EventPump.self.stopPump();
		System.out.println("Test 1 done !\n");
	}

	private static void echo_client(QueueBroker client, int connection_port, Semaphore sm) throws TimeoutException {
		client.connect("server", connection_port, new ConnectListener() {
			@Override
			public void connected(MessageQueue queue) {
				queue.setListener(new Listener() {
					@Override
					public void received(byte[] msg) {
						System.out.println("Echo message : " + new String(msg));
						queue.close();
					}

					@Override
					public void closed() {
						System.out.println("Connection closed");
						sm.release();
					}
				});

				queue.send("Hello world!".getBytes());
			}

			@Override
			public void refused() {
				System.out.println("Connection refused");
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
						queue.close();
					}

					@Override
					public void closed() {
						server.unbind(connection_port);
						System.out.println("Connection closed");
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
			echo_server(server, connection_port);
		}

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
