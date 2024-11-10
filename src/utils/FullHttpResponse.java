package utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.http.message.StatusLine.StatusClass;

public class FullHttpResponse {
	byte[] data;
	Header[] headers;
	StatusLine statusLine;
	Exception exception;
	String contentType;

	public String getContentType() throws Exception {
		if (exception!=null) throw exception;
		return contentType;
	}
	public byte[] getData() throws Exception {
		if (exception!=null) throw exception;
		return data;
	}

	public int getStatusCode() throws Exception {
		if (statusLine!=null) return statusLine.getStatusCode();
		if (exception!=null) throw exception;
		throw new RuntimeException("status is not available");
	}
	public String getStatusPhrase() throws Exception {
		if (statusLine!=null) return statusLine.getReasonPhrase();
		if (exception!=null) throw exception;
		throw new RuntimeException("status is not available");
	}
	public StatusClass getStatusClass() throws Exception {
		if (statusLine!=null) return statusLine.getStatusClass();
		if (exception!=null) throw exception;
		throw new RuntimeException("status is not available");
	}
	
	protected FullHttpResponse() {}
	
	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder();

		if (exception!=null) sb.append("Exception: ").append(exception.getMessage()).append("\n");	
		if (statusLine!=null) sb.append("StatusLine: "+statusLine).append("\n");
		if (headers!=null) {
			sb.append("Headers:\n");
    		for (Header h: headers) {
    			sb.append("\t"+h).append("\n");	
    		}
		}
		if (contentType!=null) sb.append("ContentType: "+contentType).append("\n");
		;
		if (data!=null) {
			try {
				String str=new String(data, StandardCharsets.UTF_8);
				sb.append("Data: "+str).append("\n");		
			} catch (Exception ex) {
				sb.append("Data is binary\n");
			}
		}
		return sb.toString();
	}
	
	protected void add(Header[] headers) {
		this.headers=headers;
		
	}
	protected void add(StatusLine statusLine) {
		this.statusLine=statusLine;	
	}
	protected void add(InputStream is) {
		try {
			data=is.readAllBytes();
		} catch (IOException ex) {
			exception=ex;
		}
	}
	protected void setContentType(String contentType) {
		if (contentType!=null)
			this.contentType=contentType;
		else {
			for (Header h : headers) {
				if ("content-type".equalsIgnoreCase(h.getName())) {
					this.contentType=h.getValue();
					return;
				}
			}
			this.contentType="application/octet-stream";
		}
		
	}
	
	protected void setException(Exception ex) {
		if (exception==null) exception=ex;
	}
	public Exception getException() {
		return exception;
	}

	



}
