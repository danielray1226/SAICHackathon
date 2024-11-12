package webapi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;import java.net.http.HttpRequest;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.DocumentException;

public class APIRequestHandler {
	AsyncContext asyncContext;
	ExecutorService processingService;
	Translator translator;
	int statusCode = 200;
	String error;
	Future<Void> processingFuture;
	public APIRequestHandler (AsyncContext async, ExecutorService procService, Translator translator) throws IOException{
		this.asyncContext =async;
		this.processingService=procService;
		this.translator= translator;
		asyncContext.getRequest().getInputStream().setReadListener(readListener);
		asyncContext.addListener(asyncListener);
	}
	ByteArrayOutputStream clientData = new ByteArrayOutputStream();  
	ReplyData reply;
	ReadListener readListener = new ReadListener() {
		@Override
		public void onAllDataRead() throws IOException {
			 processingFuture = processingService.submit(new Callable<Void> () {
				
				@Override
				public Void call() throws Exception {
						translate();
					return null;
				}

			});
		}
		
		@Override
		public void onDataAvailable() throws IOException {
			ServletInputStream sis = asyncContext.getRequest().getInputStream();
			byte[] buffer = new byte[4096];
			while (sis.isReady()) {
				int numOfRead = sis.read(buffer);
				if (numOfRead>0) {
					clientData.write(buffer, 0, numOfRead);
				}
			}
		}

		@Override
		public void onError(Throwable t) {
			// TODO Auto-generated method stub
			System.err.println("Client aborted connection before submitting full request!");
			t.printStackTrace();
			cleanup();
		}


	};
	AsyncListener asyncListener = new AsyncListener() {

		@Override
		public void onComplete(AsyncEvent arg0) throws IOException {
			System.out.println("COMPLETE!");
			//cleanup();
		}

		@Override
		public void onError(AsyncEvent arg0) throws IOException {
			System.err.println("Client disconnected while waiting for reply.");
			cleanup();

		}

		@Override
		public void onStartAsync(AsyncEvent arg0) throws IOException {
			// TODO Auto-generated method stub

		}

		@Override
		public void onTimeout(AsyncEvent arg0) throws IOException {
			System.out.println("TIMEDOUT!");
			cleanup();
		}

	};
	WriteListener writeListener = new WriteListener() {

		@Override
		public void onError(Throwable t) {
			t.printStackTrace();
		}

		@Override
		public void onWritePossible() throws IOException {
			ServletOutputStream outputStream = asyncContext.getResponse().getOutputStream();
			if (outputStream.isReady()) {
				outputStream.write(reply.getData());
				asyncContext.complete();
			}
		}

	};
	private void cleanup() {
		try {
			asyncContext.complete();
		} catch (Exception e) {
		}
		if (processingFuture!=null) {
			processingFuture.cancel(true);
		}
	}
	private void translate() throws IOException {
		//This will be called from the executor threads to translate. All the client data would be available by the time this is called.
		//This will produce data that can be fed back to the client (e.i the translated file);
		String contentType = asyncContext.getRequest().getContentType();
		String path = ((HttpServletRequest) asyncContext.getRequest()).getPathInfo();
		if (path != null) {
			path = path.substring(1);
		}
		Map<String, String[]> params = asyncContext.getRequest().getParameterMap();
		try {
			reply = translator.translate(clientData.toByteArray(), contentType, path, params);
			((HttpServletResponse)asyncContext.getResponse()).setContentType(reply.getContentType());
			((HttpServletResponse)asyncContext.getResponse()).setStatus(statusCode);
		} catch (Exception ex) {
			error = ex.toString();
			((HttpServletResponse)asyncContext.getResponse()).setContentType("text/plain");
			statusCode=500;
			((HttpServletResponse)asyncContext.getResponse()).setStatus(statusCode);
			reply= new ReplyData(ex.getMessage().getBytes(), "text/plain");
			ex.printStackTrace();
		}
		asyncContext.getResponse().getOutputStream().setWriteListener(writeListener);
		
	}
}
