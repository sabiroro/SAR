package tests;

import implementation.Broker;
import implementation.Channel;
import implementation.Task;

public class Test {
	public static void main(String[] args) {
		try {
			test1();
			test2();
			test3();
			System.out.println("All tests have been done successfully !");
		}

		catch (Exception e) {
			e.printStackTrace();
			System.out.println("A test has failed : " + e.getMessage());
		}
	}

	// Connection test
	public static void test1() throws Exception {
		System.out.println("Test 1 in progress...");

		Broker b1 = new Broker("Device1");
		Channel c1 = b1.accept(6969);

		Broker b2 = new Broker("Device2");
		Channel c2 = b2.connect("Device1", 6969);

		// Check the connection
		if (c1.disconnected() || c2.disconnected())
			throw new Exception("The channels seem to not be connected...");

		c1.disconnect();
		c2.disconnect();

		// Check the disconnection
		if (!c1.disconnected() || !c2.disconnected())
			throw new Exception("The channels seem to remain connected...");

		System.out.println("Test 1 done !");
	}

	// Try a simple echo
	public static void test2() throws Exception {
		System.out.println("Test 2 in progress...");

		Broker server = new Broker("Server");
		Channel serv_channel = server.accept(6969);

		Broker client = new Broker("Client");
		Channel client_channel = client.connect("Server", 6969);
		String msg_to_send = "Client's message";
		byte[] byte_message_sent = msg_to_send.getBytes();
		byte[] byte_message_echo_read = new byte[byte_message_sent.length];
		Runnable client_runnable = new Runnable() {
			@Override
			public void run() {
				// Write message
				int number_of_bytes_wrote = 0;
				while (number_of_bytes_wrote != -1) {
					number_of_bytes_wrote = serv_channel.write(byte_message_sent, number_of_bytes_wrote,
							byte_message_sent.length - number_of_bytes_wrote);
				}
				System.out.println("Message wrote by client !");

				// Read echo message
				int number_of_bytes_read = 0;
				while (number_of_bytes_read != -1) {
					number_of_bytes_read = client_channel.read(byte_message_echo_read, number_of_bytes_read,
							byte_message_sent.length - number_of_bytes_read);
				}
				System.out.println("Echo message wrote by client !");
			}
		};

		byte[] byte_message_received = new byte[byte_message_sent.length];
		Runnable server_runnable = new Runnable() {
			@Override
			public void run() {
				// Read message
				int number_of_bytes_read = 0;
				while (number_of_bytes_read != -1) {
					number_of_bytes_read = serv_channel.read(byte_message_received, number_of_bytes_read,
							byte_message_received.length - number_of_bytes_read);
				}
				System.out.println("Message read by serveur !");

				// Send echo message
				int number_of_bytes_wrote = 0;
				while (number_of_bytes_wrote != -1) {
					number_of_bytes_wrote = client_channel.write(byte_message_received, number_of_bytes_wrote,
							byte_message_sent.length - number_of_bytes_wrote);
				}
				System.out.println("Echo message wrote by server !");
			}
		};

		// Write message
		Task t1 = new Task(client, client_runnable);
		// Read message
		Task t2 = new Task(server, server_runnable);

		// We expected the end of tasks to test if it is the same message
		t1.join();
		t2.join();
		if (byte_message_sent.toString() == byte_message_echo_read.toString())
			throw new Exception("Those messages are not the same...");

		serv_channel.disconnect();
		client_channel.disconnect();

		System.out.println("Test 2 done !");
	}

	// Echo with clients
	public static void test3() throws Exception {
		System.out.println("Test 3 in progress...");

		Broker server = new Broker("Server");
		Channel serv_channel = server.accept(6969);

		Broker client1 = new Broker("Client1");
		Channel client1_channel = client1.connect("Server", 6969);
		String msg_to_send1 = "Client1's message";
		byte[] byte_message_sent1 = msg_to_send1.getBytes();
		byte[] byte_message_echo_read1 = new byte[byte_message_sent1.length];
		Runnable client_runnable1 = new Runnable() {
			@Override
			public void run() {
				// Write message
				int number_of_bytes_wrote = 0;
				while (number_of_bytes_wrote != -1) {
					number_of_bytes_wrote = serv_channel.write(byte_message_sent1, number_of_bytes_wrote,
							byte_message_sent1.length - number_of_bytes_wrote);
				}
				System.out.println("Message wrote by client 1 !");

				// Read echo message
				int number_of_bytes_read = 0;
				while (number_of_bytes_read != -1) {
					number_of_bytes_read = serv_channel.read(byte_message_echo_read1, number_of_bytes_read,
							byte_message_sent1.length - number_of_bytes_read);
				}
				System.out.println("Echo message wrote by client 1 !");
			}
		};

		Broker client2 = new Broker("Client2");
		Channel client2_channel = client2.connect("Server", 6969);
		String msg_to_send2 = "Client2's message";
		byte[] byte_message_sent2 = msg_to_send2.getBytes();
		byte[] byte_message_echo_read2 = new byte[byte_message_sent2.length];
		Runnable client_runnable2 = new Runnable() {
			@Override
			public void run() {
				// Write message
				int number_of_bytes_wrote = 0;
				while (number_of_bytes_wrote != -1) {
					number_of_bytes_wrote = serv_channel.write(byte_message_sent2, number_of_bytes_wrote,
							byte_message_sent2.length - number_of_bytes_wrote);
				}
				System.out.println("Message wrote by client 2 !");

				// Read echo message
				int number_of_bytes_read = 0;
				while (number_of_bytes_read != -1) {
					number_of_bytes_read = serv_channel.read(byte_message_echo_read2, number_of_bytes_read,
							byte_message_sent2.length - number_of_bytes_read);
				}
				System.out.println("Echo message wrote by client 2 !");
			}
		};

		byte[] byte_message_received1 = new byte[byte_message_sent1.length];
		Runnable server_runnable1 = new Runnable() {
			@Override
			public void run() {
				// Read message
				int number_of_bytes_read = 0;
				while (number_of_bytes_read != -1) {
					number_of_bytes_read = serv_channel.read(byte_message_received1, number_of_bytes_read,
							byte_message_received1.length - number_of_bytes_read);
				}
				System.out.println("Message (from client 1) read by serveur !");

				// Send echo message
				int number_of_bytes_wrote = 0;
				while (number_of_bytes_wrote != -1) {
					number_of_bytes_wrote = serv_channel.write(byte_message_received1, number_of_bytes_wrote,
							byte_message_sent1.length - number_of_bytes_wrote);
				}
				System.out.println("Echo message (from client 1) wrote by server !");
			}
		};
		
		
		byte[] byte_message_received2 = new byte[byte_message_sent2.length];
		Runnable server_runnable2 = new Runnable() {
			@Override
			public void run() {
				// Read message
				int number_of_bytes_read = 0;
				while (number_of_bytes_read != -1) {
					number_of_bytes_read = serv_channel.read(byte_message_received2, number_of_bytes_read,
							byte_message_received2.length - number_of_bytes_read);
				}
				System.out.println("Message (from client 2) read by serveur !");

				// Send echo message
				int number_of_bytes_wrote = 0;
				while (number_of_bytes_wrote != -1) {
					number_of_bytes_wrote = serv_channel.write(byte_message_received2, number_of_bytes_wrote,
							byte_message_sent2.length - number_of_bytes_wrote);
				}
				System.out.println("Echo message (from client 2) wrote by server !");
			}
		};
		

		// Write message (client 1)
		Task t1 = new Task(client1, client_runnable1);
		// Read message
		Task t2 = new Task(server, server_runnable1);
		// Write message (client 2)
		Task t3 = new Task(client2, client_runnable2);
		// Read message
		Task t4 = new Task(server, server_runnable2);

		// We expected the end of tasks to test if it is the same message
		t1.join();
		t2.join();
		t3.join();
		t4.join();
		
		if (byte_message_sent1.toString() == byte_message_echo_read1.toString())
			throw new Exception("Client1's messages are not the same...");
		if (byte_message_sent2.toString() == byte_message_echo_read2.toString())
			throw new Exception("Client2's messages are not the same...");

		serv_channel.disconnect();
		client1_channel.disconnect();
		client2_channel.disconnect();

		System.out.println("Test 3 done !");
	}
}
