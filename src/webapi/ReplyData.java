package webapi;

public class ReplyData {
	byte[] data;
	String contentType;
	public ReplyData (byte[] data, String type) {
		this.data = data;
		this.contentType=type;
	}
	public String getContentType() {
		return contentType;
	}
	public byte[] getData() {
		return data;
	}
}
