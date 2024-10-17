package task3.implementation.queue;

public class Port {
	public int port_number;
	public boolean is_connecting_port; // To know binding and accepting port (true for a connecting port, false for an accepting port)
	
	public int backlog; // To limit connection queue on an accepting port
	public boolean is_active; // To know if the task must stop or not (for accept)
	public task2.implementation.API.Task bind_task;  // Task running "accept" on a port
	
	public Port(int port_number, boolean is_connecting_port) {
		this.port_number = port_number;
		this.is_connecting_port = is_connecting_port;
		
		this.backlog = 0;
		this.is_active = true;
		this.bind_task = null;
	}
}
