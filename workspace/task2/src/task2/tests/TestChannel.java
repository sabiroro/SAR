package task2.tests;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeoutException;

import task2.implementation.API.Broker;
import task2.implementation.API.Channel;
import task2.implementation.API.Task;
import task2.implementation.broker.BrokerImpl;
import task2.implementation.broker.BrokerManager;
import task2.implementation.broker.DisconnectedException;
import task2.implementation.broker.TaskImpl;

public class TestChannel {
	public static void main(String[] args) {
		try {
			test1();
			test1bis();
			test2();
			test3();
			test4();
			test5();
			System.out.println("All tests have been done successfully !");
			System.exit(0);
		}

		catch (Exception e) {
			e.printStackTrace();
			System.out.println("	The test has failed : " + e.getMessage());
			System.exit(-1);
		}
	}

	// Connection test
	public static void test1() throws Exception {
		System.out.println("Test 1 in progress...");

		Broker b1 = new BrokerImpl("Device1");
		Broker b2 = new BrokerImpl("Device2");

		Task t1 = new TaskImpl(b1, new Runnable() {

			@Override
			public void run() {
				try {
					try {
						// Put some delay to try doing connect before accept
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// Nothing there
					}
					Channel c1 = b1.accept(6969);

					// Check the connection
					if (c1.disconnected())
						throw new IllegalStateException("The channels seem to not be connected...");

					c1.disconnect();

					// Check the disconnection
					if (!c1.disconnected())
						throw new IllegalStateException("The channels seem to remain connected...");

				} catch (NullPointerException | IllegalStateException | DisconnectedException e) {
					e.printStackTrace();
					System.exit(-1);
				}

			}
		});

		Task t2 = new TaskImpl(b2, new Runnable() {

			@Override
			public void run() {
				try {
					Channel c2 = b2.connect("Device1", 6969);

					// Check the connection
					if (c2.disconnected())
						throw new IllegalStateException("The channels seem to not be connected...");

					c2.disconnect();

					// Check the disconnection
					if (!c2.disconnected())
						throw new IllegalStateException("The channels seem to remain connected...");

				} catch (NullPointerException | IllegalStateException | TimeoutException e) {
					e.printStackTrace();
					System.exit(-1);
				}

			}
		});

		t1.join();
		t2.join();

		System.out.println("Test 1 done !\n");
	}

	// Test with read/write exception because of disconnection
	public static void test1bis() throws Exception {
		System.out.println("Test 1 bis in progress...");

		Broker b1 = new BrokerImpl("pc1");
		Broker b2 = new BrokerImpl("pc2");

		Task t1 = new TaskImpl(b1, new Runnable() {

			@Override
			public void run() {
				try {
					Channel c1 = b1.accept(6969);
					c1.write("hello world".getBytes(), 0, 5);
				} catch (DisconnectedException e) {
					// Nothing there
				}
			}
		});

		Task t2 = new TaskImpl(b2, new Runnable() {

			@Override
			public void run() {
				Channel c2 = null;
				try {
					c2 = b2.connect("pc1", 6969);
				} catch (TimeoutException e1) {
					e1.printStackTrace();
				}
				try {
					c2.read(new byte[20], 0, 3);
					c2.disconnect();

					c2.read(new byte[5], 0, 3);
					throw new IllegalStateException("The channel should not be readable");

				} catch (DisconnectedException e) {
					System.out.println("	-> Disconnected exception caugth sucessfully : " + e.getMessage());
				}

			}
		});

		t1.join();
		t2.join();

		System.out.println("Test 1 bis done !\n");
	}

	// Try a server echo by knowing the message size
	public static void test2() throws Exception {
		System.out.println("Test 2 in progress...");

		Broker server = new BrokerImpl("Server");
		Broker client = new BrokerImpl("Client");

		String msg_to_send = "Client's message";
		byte[] byte_message_sent = msg_to_send.getBytes();
		byte[] byte_message_echo_read = new byte[byte_message_sent.length];
		Runnable client_runnable = new Runnable() {
			@Override
			public void run() {
				try {
					Channel client_channel = client.connect("Server", 6969);

					// Write message
					int number_of_bytes_wrote = 0;
					while (number_of_bytes_wrote != byte_message_sent.length) {
						number_of_bytes_wrote += client_channel.write(byte_message_sent, number_of_bytes_wrote,
								byte_message_sent.length - number_of_bytes_wrote);
					}
					System.out.println("	-> Message wrote by client !");

					// Read echo message
					int number_of_bytes_read = 0;
					while (number_of_bytes_read != byte_message_sent.length) {
						number_of_bytes_read += client_channel.read(byte_message_echo_read, number_of_bytes_read,
								byte_message_sent.length - number_of_bytes_read);
					}

					client_channel.disconnect();
				}

				catch (DisconnectedException | TimeoutException e) {
					// Nothing there
				}

				System.out.println("	-> Echo message wrote by client !");
			}
		};

		byte[] byte_message_received = new byte[byte_message_sent.length];
		Runnable server_runnable = new Runnable() {
			@Override
			public void run() {
				try {
					Channel serv_channel = server.accept(6969);

					// Read message
					int number_of_bytes_read = 0;
					while (number_of_bytes_read != byte_message_received.length) {
						number_of_bytes_read += serv_channel.read(byte_message_received, number_of_bytes_read,
								byte_message_received.length - number_of_bytes_read);
					}
					System.out.println("	-> Message read by serveur !");

					// Send echo message
					int number_of_bytes_wrote = 0;
					while (number_of_bytes_wrote != byte_message_sent.length) {
						number_of_bytes_wrote += serv_channel.write(byte_message_received, number_of_bytes_wrote,
								byte_message_sent.length - number_of_bytes_wrote);
					}

					serv_channel.disconnect();
				}

				catch (DisconnectedException e) {
					// Nothing there
				}
				System.out.println("	-> Echo message wrote by server !");
			}
		};

		// Write message
		Task t1 = new TaskImpl(client, client_runnable);
		// Read message
		Task t2 = new TaskImpl(server, server_runnable);

		// We expected the end of tasks to test if it is the same message
		t1.join();
		t2.join();

		String message = new String(byte_message_sent);
		String echo_message = new String(byte_message_echo_read);

		if (!message.equals(echo_message))
			throw new Exception("	-> Those messages are not the same... '" + message + "' != '" + echo_message + "'");
		else
			System.out.println("	-> Messages send : '" + message + "' == '" + echo_message + "'");

		System.out.println("Test 2 done !\n");
	}

	// Echo with clients
	public static void test3() throws Exception {
		System.out.println("Test 3 in progress...");

		Broker server = new BrokerImpl("Server1");
		Broker client1 = new BrokerImpl("Client1");
		Broker client2 = new BrokerImpl("Client2");

		String msg_to_send1 = "Client1's message";
		byte[] byte_message_sent1 = msg_to_send1.getBytes();
		byte[] byte_message_echo_read1 = new byte[byte_message_sent1.length];
		Runnable client_runnable1 = new Runnable() {
			@Override
			public void run() {
				try {
					Channel client1_channel = client1.connect("Server1", 6969);

					// Write message
					int number_of_bytes_wrote = 0;
					while (number_of_bytes_wrote != byte_message_sent1.length) {
						number_of_bytes_wrote += client1_channel.write(byte_message_sent1, number_of_bytes_wrote,
								byte_message_sent1.length - number_of_bytes_wrote);
					}
					System.out.println("	-> Message wrote by client 1 !");

					// Read echo message
					int number_of_bytes_read = 0;
					while (number_of_bytes_read != byte_message_sent1.length) {
						number_of_bytes_read += client1_channel.read(byte_message_echo_read1, number_of_bytes_read,
								byte_message_sent1.length - number_of_bytes_read);
					}
					client1_channel.disconnect();
				} catch (DisconnectedException | TimeoutException e) {
					// Nothing there
				}
				System.out.println("	-> Echo message wrote by client 1 !");
			}
		};

		String msg_to_send2 = "Client2's message";
		byte[] byte_message_sent2 = msg_to_send2.getBytes();
		byte[] byte_message_echo_read2 = new byte[byte_message_sent2.length];
		Runnable client_runnable2 = new Runnable() {
			@Override
			public void run() {
				try {
					Channel client2_channel = client2.connect("Server1", 6969);

					// Write message
					int number_of_bytes_wrote = 0;
					while (number_of_bytes_wrote != byte_message_sent2.length) {
						number_of_bytes_wrote += client2_channel.write(byte_message_sent2, number_of_bytes_wrote,
								byte_message_sent2.length - number_of_bytes_wrote);
					}
					System.out.println("	-> Message wrote by client 2 !");

					// Read echo message
					int number_of_bytes_read = 0;
					while (number_of_bytes_read != byte_message_sent2.length) {
						number_of_bytes_read += client2_channel.read(byte_message_echo_read2, number_of_bytes_read,
								byte_message_sent2.length - number_of_bytes_read);
					}
					client2_channel.disconnect();
				} catch (DisconnectedException | TimeoutException e) {
					// Nothing there
				}
				System.out.println("	-> Echo message wrote by client 2 !");
			}
		};

		byte[] byte_message_received1 = new byte[byte_message_sent1.length];
		byte[] byte_message_received2 = new byte[byte_message_sent2.length];
		Runnable server_runnable = new Runnable() {
			@Override
			public void run() {
				Channel serv_channel = null;
				try {
					serv_channel = server.accept(6969);
					// Read message
					int number_of_bytes_read = 0;
					while (number_of_bytes_read != byte_message_received1.length) {
						number_of_bytes_read += serv_channel.read(byte_message_received1, number_of_bytes_read,
								byte_message_received1.length - number_of_bytes_read);
					}
					System.out.println("	-> Message (from client 1) read by serveur !");

					// Send echo message
					int number_of_bytes_wrote = 0;
					while (number_of_bytes_wrote != byte_message_sent1.length) {
						number_of_bytes_wrote += serv_channel.write(byte_message_received1, number_of_bytes_wrote,
								byte_message_sent1.length - number_of_bytes_wrote);
					}
				} catch (DisconnectedException e) {
					// Nothing there
				}
				System.out.println("	-> Echo message (from client 1) wrote by server !");

				serv_channel.disconnect();

				try {
					serv_channel = server.accept(6969);

					// Read message
					int number_of_bytes_read = 0;
					while (number_of_bytes_read != byte_message_received2.length) {
						number_of_bytes_read += serv_channel.read(byte_message_received2, number_of_bytes_read,
								byte_message_received2.length - number_of_bytes_read);
					}
					System.out.println("	-> Message (from client 2) read by serveur !");

					// Send echo message
					int number_of_bytes_wrote = 0;
					while (number_of_bytes_wrote != byte_message_sent2.length) {
						number_of_bytes_wrote += serv_channel.write(byte_message_received2, number_of_bytes_wrote,
								byte_message_sent2.length - number_of_bytes_wrote);
					}
				} catch (DisconnectedException e) {
					// Nothing there
				}
				System.out.println("	-> Echo message (from client 2) wrote by server !");

				serv_channel.disconnect();
			}
		};

		// Write message (client 1)
		Task t1 = new TaskImpl(client1, client_runnable1);
		// Read message
		Task t2 = new TaskImpl(server, server_runnable);
		// Write message (client 2)
		Task t3 = new TaskImpl(client2, client_runnable2);

		// We expected the end of tasks to test if it is the same message
		t1.join();
		t2.join();
		t3.join();

		String msg1 = new String(byte_message_sent1);
		String echo1 = new String(byte_message_echo_read1);

		String msg2 = new String(byte_message_sent2);
		String echo2 = new String(byte_message_echo_read2);

		if (!msg1.equals(echo1))
			throw new Exception("	-> Client1's messages are not the same... '" + msg1 + "' != '" + echo1 + "'");
		if (!msg2.equals(echo2))
			throw new Exception("	-> Client2's messages are not the same... '" + msg2 + "' != '" + echo2 + "'");

		System.out.println("		=> Messages of client1 and 2 are echoed successfully !");
		System.out.println("Test 3 done !\n");
	}

	// Communication with length specified
	private static void send(byte[] bytes, Channel channel) throws DisconnectedException {
		int remaining = bytes.length;
		int offset = 0;
		while (remaining != 0) {
			int n = channel.write(bytes, offset, remaining);
			offset += n;
			remaining -= n;
		}
	}

	private static void receive(byte[] bytes, Channel channel) throws DisconnectedException {
		int remaining = bytes.length;
		int offset = 0;
		while (remaining != 0) {
			int n = channel.read(bytes, offset, remaining);
			offset += n;
			remaining -= n;
		}
	}

	public static void test4() throws Exception {
		System.out.println("Test 4 in progress...");
		BrokerManager.self.removeAllBrokers(); // To reset buffer

		Broker b1 = new BrokerImpl("Device1");
		Broker b2 = new BrokerImpl("Device2");

		byte[] msg_sent = "Salut c'est le Ro !".getBytes();
		byte[] msg_received = new byte[msg_sent.length];
		Runnable send_runnable = new Runnable() {
			@Override
			public void run() {
				try {
					Channel c = b1.accept(6969);

					// Send size
					int msg_size = msg_sent.length;
					byte[] size_in_bytes = ByteBuffer.allocate(4).putInt(msg_size).array(); // To convert int to bytes
																							// array
					send(size_in_bytes, c);

					// Send message
					send(msg_sent, c);

					// Read echo message
					receive(msg_received, c);

					c.disconnect();
				} catch (DisconnectedException e) {
					e.printStackTrace();
				}
			}
		};

		Runnable read_runnable = new Runnable() {
			@Override
			public void run() {
				try {
					Channel c = b2.connect("Device1", 6969);
					// Receive size
					byte[] size_in_bytes = new byte[4]; // An int is 4 bytes
					receive(size_in_bytes, c);

					int size = ByteBuffer.wrap(size_in_bytes).getInt();

					// Receive message
					byte[] msg_get = new byte[size];
					receive(msg_get, c);

					// Send echo message
					send(msg_get, c);

					c.disconnect();
				} catch (DisconnectedException | TimeoutException e) {
					// Nothing there
				}
			}
		};

		Task t1 = new TaskImpl(b1, send_runnable);
		Task t2 = new TaskImpl(b2, read_runnable);

		t1.join();
		t2.join();

		String smsg = new String(msg_sent);
		String rmsg = new String(msg_received);

		if (!smsg.equals(rmsg))
			throw new Exception("	-> Messages are not the same... '" + smsg + "' is different than '" + rmsg + "'");

		System.out.println("	-> Messages send and echo : '" + smsg + "' == '" + rmsg + "' !");

		System.out.println("Test 4 done !\n");
	}

	public static void test5() throws Exception {
		System.out.println("Test 5 in progress...");
		System.out.println("	-> The test will wait a TimeoutException...");
		BrokerManager.self.removeAllBrokers(); // To reset buffer

		Broker b1 = new BrokerImpl("Device1");
		new BrokerImpl("Device2");

		Task t = new TaskImpl(b1, new Runnable() {

			@Override
			public void run() {
				try {
					b1.connect("Device2", 6969);
					throw new IllegalStateException("	-> Aucun timeout n'a été levé...");

				} catch (NullPointerException | IllegalStateException e) {
					e.printStackTrace();
					System.exit(-1);
				} catch (TimeoutException e) {
					System.out.println("	-> The timeout was caugth successfully !");
				}

			}
		});

		t.join();

		System.out.println("Test 5 done !\n");
	}
}
