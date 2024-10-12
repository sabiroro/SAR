package task2.implementation.broker;

import task2.implementation.API.Broker;
import task2.implementation.API.Channel;

public class RdV {
	Broker ba; // Accepting broker
	Broker bc; // Connecting broker
	ChannelImpl channel_accept; // Channel to the accepting broker
	ChannelImpl channel_connect; // Channel to the connecting broker
	CircularBuffer in; // The buffer to write for the connecting task
	CircularBuffer out; // The buffer to write for the accepting task

	/**
	 * Allows to wait the creation of the other channel
	 */
	private void _wait() {
		while (channel_accept == null || channel_connect == null) {
			try {
				wait();
			} catch (InterruptedException e) {
				// Nothing there
			}
		}
	}

	synchronized Channel connect(Broker connecting_broker, int port) { // Broker wanted a connection
		this.bc = connecting_broker;
		this.channel_connect = new ChannelImpl(bc, port);
		if (channel_accept == null) {
			_wait();
		}

		else {
			// If the accept's channel is already created, also the connect's channel
			// initiates pipelines
			channel_accept.connect(channel_connect);
			notify();
		}

		return channel_connect;
	}

	protected synchronized Channel accept(Broker accepting_broker, int port) { // Broker expected a connection
		this.ba = accepting_broker;
		this.channel_accept = new ChannelImpl(ba, port);
		if (channel_connect == null) {
			_wait();
		}

		else {
			// If the accept's channel is already created, also the connect's channel
			// initiates pipelines
			channel_accept.connect(channel_connect);
			notify();
		}

		return channel_accept;
	}
}
