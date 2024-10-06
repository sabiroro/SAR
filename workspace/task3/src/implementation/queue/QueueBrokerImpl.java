package implementation.queue;

import implementation.API.QueueBroker;

public class QueueBrokerImpl extends QueueBroker {

	public QueueBrokerImpl(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean bind(int port, AcceptListener listener) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean unbind(int port) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean connect(String name, int port, ConnectListener listener) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
