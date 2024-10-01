package implementation.queue;

import implementation.API.MessageQueue;

public class MessageQueueImpl extends MessageQueue {

	@Override
	public void send(byte[] bytes, int offset, int length) throws Exception {
		throw new Exception("NYI");
	}

	@Override
	public byte[] receive() throws Exception {
		throw new Exception("NYI");
	}

	@Override
	public void close() throws Exception {
		throw new Exception("NYI");
	}

	@Override
	public boolean closed() throws Exception {
		throw new Exception("NYI");
	}

}
