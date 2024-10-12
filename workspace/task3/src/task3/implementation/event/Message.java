package task3.implementation.event;

public class Message {
	public byte[] bytes;
	public int offset;
	public int length;

	public Message(byte[] bytes) {
		this.bytes = bytes;
		this.offset = 0;
		this.length = bytes.length;
	}
	
	public Message(byte[] bytes, int offset, int length) {
		this.bytes = bytes;
		this.offset = offset;
		this.length = length;
	}
}
