package task3.implementation.queue;

import java.util.ArrayList;

import task2.implementation.API.Channel;

public class Port {
	public int port_number;
	public boolean is_connecting_port; // To know binding and accepting port (true for a connecting port, false for an
										// accepting port)

	public ArrayList<Channel> client_channel_connected;
	public int backlog; // To limit connection queue on an accepting port
	public boolean is_active; // To know if the task must stop or not (for accept)
	public task2.implementation.API.Task bind_task; // Task running "accept" on a port

	public Port(int port_number, boolean is_connecting_port) {
		this.port_number = port_number;
		this.is_connecting_port = is_connecting_port;

		this.client_channel_connected = new ArrayList<>();
		this.backlog = 0;
		this.is_active = true;
		this.bind_task = null;
	}

	public synchronized void addBacklog(Channel channel) {
		if (!client_channel_connected.contains(channel)) {
			client_channel_connected.add(channel);
			backlog++;
			notifyAll();
		}
	}

	public synchronized void subBacklog(Channel channel) {
		if (client_channel_connected.contains(channel)) {
			client_channel_connected.remove(channel);
			backlog--;
			notifyAll();
		}
	}

	public synchronized int getBacklog() {
		return this.backlog;
	}
}
