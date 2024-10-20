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
import task3.implementation.event.QueueBrokerManager;
import task3.implementation.queue.QueueBrokerImpl;

public class TestEvent {
	private static void clean_previous_test() {
		BrokerManager.self.removeAllBrokers();
		QueueBrokerManager.self.removeAllQueueBrokers();
		EventPump.self.restartPump();
	}

	private static void stop_test() {
		BrokerManager.self.removeAllBrokers();
		EventPump.self.restartPump();
	}

	public static void main(String[] args) {
		try {
			clean_previous_test();
			test1();
			clean_previous_test();
			test2(1, 1);
			clean_previous_test();
			test2(10, 2);
			clean_previous_test();
			test3(100);
			clean_previous_test();
			test4();
//			clean_previous_test();
			//test5();

			stop_test();
			System.out.println("That's all folks");
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
						
						// Unbind accepting port "for fun" and to test after
						Thread t = new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									Thread.sleep(1000); // Wait 1 second to wait the message is sent
									queue.close();
								} catch (InterruptedException e) {
									// Nothing there
								}
							}
						});
						t.start();
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
						if (sm.availablePermits() == 0)
							System.out.println(
									"WARNING : If test3 is running, a timeout is currently in progress, please wait roughtly 15 seconds to get TimeoutException of client and reconnection...");
					}
				});

				queue.send(("Hello world! - By " + client.name + ", port : " + connection_port).getBytes());
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

	/**
	 * Create an echo serv with an accepted port opened until the unbind
	 * 
	 * @param server
	 * @param connection_port
	 * @throws DisconnectedException
	 */
	private static void echo_server(QueueBroker server, int connection_port) throws DisconnectedException {
		echo_server(server, connection_port, false);
	}

	private static void echo_server(QueueBroker server, int connection_port, boolean need_to_unbind)
			throws DisconnectedException {
		server.bind(connection_port, new AcceptListener() {
			@Override
			public void accepted(MessageQueue queue) {
				queue.setListener(new Listener() {
					@Override
					public void received(byte[] msg) {
						queue.send(msg);

						if (need_to_unbind) {
							// Unbind accepting port "for fun" and to test after
							Thread t = new Thread(new Runnable() {
								@Override
								public void run() {
									try {
										Thread.sleep(1000); // Wait 1 second to wait the message is sent
										queue.close();
									} catch (InterruptedException e) {
										// Nothing there
									}
								}
							});
							t.start();
						}
					}

					@Override
					public void closed() {
						server.unbind(connection_port);
						// System.out.println(" ---> " + connection_port + " closed from server");
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
			int port = connection_port + i;
			QueueBroker client = new QueueBrokerImpl("client" + i);
			echo_client(client, port, sm);
			if (i == nbre_clients - 1) {
				System.out.println("We will wait 16 seconds to simulate a client reconnection");
				Thread.currentThread();
				Thread.sleep(16000); // Create a client's reconnection because of Timeout
			}

			echo_server(server, port, true);
		}

		sm.acquire(); // Waits the end of the test
		System.out.println("Test 3 done !\n");
	}

	// Test the return statement of method connection
	public static void test4() throws Exception {
		System.out.println("Test 4 in progress...");

		QueueBroker client = new QueueBrokerImpl("client");
		int connection_port = 6969;

		AcceptListener default_accept_listener = new AcceptListener() {

			@Override
			public void accepted(MessageQueue queue) {
				// Just for test statement, nothing there
			}
		};

		ConnectListener default_connect_listener = new ConnectListener() {

			@Override
			public void refused() {
				// Just for test statement, nothing there
			}

			@Override
			public void connected(MessageQueue queue) {
				// Just for test statement, nothing there
			}
		};

		// Initialization of server's method tests
		boolean client_connect_test = false;

		client_connect_test = client.connect("server", connection_port, default_connect_listener); // False
		if (client_connect_test)
			throw new Exception("The client tries to connect a not existing broker !");

		QueueBroker server = new QueueBrokerImpl("server");

		client_connect_test = client.connect("server", connection_port, default_connect_listener); // True
		if (!client_connect_test)
			throw new Exception("The client doesn't find the broker !");

		// Initialization of client's method tests
		boolean server_bind_test = false;
		boolean server_unbind_test = false;

		server_unbind_test = server.unbind(connection_port); // False
		if (server_unbind_test)
			throw new Exception("The server tries to unbind a not connected port !");

		server_bind_test = server.bind(connection_port, default_accept_listener); // True
		if (!server_bind_test)
			throw new Exception("The server can't bind a connection port !");

		server_bind_test = server.bind(connection_port, default_accept_listener); // False
		if (server_bind_test)
			throw new Exception("The server tries to bind an existing port !");

		server_unbind_test = server.unbind(connection_port); // True
		if (!server_unbind_test)
			throw new Exception("The server can't unbind a connected port !");

		server_bind_test = server.bind(connection_port, default_accept_listener); // True
		if (!server_bind_test)
			throw new Exception("The server can't bind an old connection port !");

		System.out.println("Test 4 done !\n");
	}
	
	// Test the return statement of method connection
	public static void test5() throws Exception {
		throw new IllegalStateException("NYI -> ask teacher");
//		System.out.println("Test 5 in progress...");
//		Semaphore sm = new Semaphore(0);
//
//		QueueBroker server= new QueueBrokerImpl("server");
//		QueueBroker client = new QueueBrokerImpl("client");
//		int connection_port = 6969;
//		
//		server.bind(connection_port, new AcceptListener() {
//			@Override
//			public void accepted(MessageQueue queue) {
//				queue.setListener(new Listener() {
//					@Override
//					public void received(byte[] msg) {
//						System.out.println("	-> received (server)");
//					}
//					
//					@Override
//					public void closed() {
//						System.out.println("	-> closed (server)");
//					}
//				});
//			}
//		});
//		
//		client.connect("server", connection_port, new ConnectListener() {
//			@Override
//			public void refused() {
//				System.out.println("	-> refused");
//			}
//			
//			@Override
//			public void connected(MessageQueue queue) {
//				System.out.println("	-> connected");
//				queue.setListener(new Listener() {
//					@Override
//					public void received(byte[] msg) {
//						System.out.println("	-> received (server)");
//					}
//					
//					@Override
//					public void closed() {
//						System.out.println("	-> closed (server)");
//					}
//				});
//				
//				queue.send("msg example".getBytes());
//				task3.implementation.API.Task.task().kill();
//			}
//		});
//
//		sm.acquire();
//		System.out.println("Test 5 done !\n");
	}
}
