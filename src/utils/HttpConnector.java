package utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.apache.hc.client5.http.ContextBuilder;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.ConnectionReuseStrategy;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.entity.BasicHttpEntity;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

/*
 * adapted from https://github.com/apache/httpcomponents-client/blob/master/httpclient5/src/test/java/org/apache/hc/client5/http/examples
 * for ignoring invalid certificates, adapted:
 * https://stackoverflow.com/questions/19517538/ignoring-ssl-certificate-in-apache-httpclient-4-3/19950935#19950935
 */

@SuppressWarnings("deprecation")
public class HttpConnector {
	final SSLContextBuilder sslBuilder=SSLContexts.custom(); //ignores invalid ssl certificates
	final SSLContext sslContext;
	final SSLConnectionSocketFactory sslsf;
	final Registry<ConnectionSocketFactory> socketFactoryRegistry;
	{
		try {
			sslBuilder.loadTrustMaterial(new TrustStrategy() {
				public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					return true;
				}
			});
		sslContext = sslBuilder.build();
		sslsf=new SSLConnectionSocketFactory(sslContext, new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}});
		socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create().register("https", sslsf).register("http", new PlainConnectionSocketFactory()).build();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	@SuppressWarnings("deprecation")
	final PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry); //passes always-trust configuration

	{
		connManager.setMaxTotal(200); //200 connections available at all times
		connManager.setDefaultMaxPerRoute(100); //up to 100 per-host-ports 
		connManager.setDefaultConnectionConfig(ConnectionConfig.custom().setConnectTimeout(Timeout.ofSeconds(30)) //will disconnect if not connected to after 30 seconds
				.setSocketTimeout(Timeout.ofSeconds(30)).setValidateAfterInactivity(TimeValue.ofSeconds(10)) //if sockets dont respond in 30 seconds will disconnect,
				.setTimeToLive(TimeValue.ofHours(1)).build());
	}
	final ConnectionReuseStrategy connectionReuseStrategy = new ConnectionReuseStrategy() {
		@Override
		public boolean keepAlive(HttpRequest request, HttpResponse response, HttpContext context) {
			return true;
		}
	};
	final CloseableHttpClient httpClient = HttpClients.custom().setConnectionReuseStrategy(connectionReuseStrategy) //copied from example
			.setConnectionManager(connManager).build();
	
	public FullHttpResponse callGet(String url, BasicHeader... headers) { //takes url and array of headers to be sent
		FullHttpResponse fullResponse = new FullHttpResponse();
		try {
			final HttpGet httpget = new HttpGet(url);
			// Request configuration can be overridden at the request level.
			// They will take precedence over the one set at the client level.
			// We can tune individual request parameters, if we need to:
			final RequestConfig requestConfig = RequestConfig.custom()// RequestConfig.copy(defaultRequestConfig)
					.setResponseTimeout(Timeout.ofSeconds(30)).setConnectionRequestTimeout(Timeout.ofSeconds(5))
					.setRedirectsEnabled(true).setCircularRedirectsAllowed(false).setConnectionKeepAlive(TimeValue.ofMinutes(5))
//                    .setProxy(new HttpHost("myproxyhost", 8080)) <- example of setting up proxy
					.build();
			httpget.setConfig(requestConfig);

			boolean hasAccept = false;
			for (BasicHeader bh : headers) {
				httpget.addHeader(bh);
				if ("accept".equalsIgnoreCase(bh.getName()))
					hasAccept = true;
			}
			if (!hasAccept)
				httpget.addHeader(new BasicHeader("Accept", "*/*")); //accepts all content types back

			// Execution context can be customized locally.
			// Contextual attributes set the local context level will take
			// precedence over those set at the client level.
			final HttpClientContext context = ContextBuilder.create().build();

			httpClient.execute(httpget, context, (r) -> {
				fullResponse.add(new StatusLine(r));
				fullResponse.add(r.getHeaders());
				fullResponse.setContentType(r.getEntity().getContentType());
				InputStream is = r.getEntity().getContent();
				fullResponse.add(is);
				return r;
			});
		} catch (Exception ex) {
			fullResponse.setException(ex);
		}

		return fullResponse;

	}

	public FullHttpResponse callPost(String url, String contentType, byte[] payload, BasicHeader... headers) {
		return callMethod("post", url, contentType, payload, headers); 
	}
	public FullHttpResponse callMethod(String method, String url, String contentType, byte[] payload, BasicHeader... headers) { //takes what kind of method (post/get/etc), the url, and finally contenttype/payload.
		FullHttpResponse fullResponse = new FullHttpResponse();
		try {
			BasicHttpEntity entity = new BasicHttpEntity(new ByteArrayInputStream(payload),
					ContentType.create(contentType));
			HttpUriRequestBase post = null;
			if ("post".equalsIgnoreCase(method) || method==null) post=new HttpPost(url);
			else if ("delete".equalsIgnoreCase(method)) post=new HttpDelete(url);
			else if ("patch".equalsIgnoreCase(method)) post=new HttpPatch(url);
			else if ("put".equalsIgnoreCase(method)) post=new HttpPut(url);
			else throw new RuntimeException("Method "+method+" is not implemented");
			
			post.setEntity(entity);
			boolean hasAccept = false;
			for (BasicHeader bh : headers) {
				post.addHeader(bh);
				if ("accept".equalsIgnoreCase(bh.getName()))
					hasAccept = true;
			}
			if (!hasAccept)
				post.addHeader(new BasicHeader("Accept", "*/*"));

			final RequestConfig requestConfig = RequestConfig.custom()// RequestConfig.copy(defaultRequestConfig)
					.setResponseTimeout(Timeout.ofSeconds(30)).setConnectionRequestTimeout(Timeout.ofSeconds(5))
					.setRedirectsEnabled(true).setCircularRedirectsAllowed(false).setConnectionKeepAlive(TimeValue.ofMinutes(5))
					// .setProxy(new HttpHost("myproxyhost", 8080)) <- example of setting up proxy
					.build();
			post.setConfig(requestConfig);

			final HttpClientContext context = ContextBuilder.create()
					// .useCookieStore(cookieStore)
					// .useCredentialsProvider(credentialsProvider)
					.build();

			httpClient.execute(post, context, (r) -> {
				fullResponse.add(new StatusLine(r));
				fullResponse.add(r.getHeaders());
				fullResponse.setContentType(r.getEntity().getContentType());
				InputStream is = r.getEntity().getContent();
				fullResponse.add(is);
				return r;
			});
		} catch (Exception ex) {
			fullResponse.setException(ex);
		}
		return fullResponse;
	}

	public static void main(String[] args) throws Exception {
		HttpConnector connector = new HttpConnector();

		ExecutorService tp = Executors.newCachedThreadPool();

		for (int i = 0; i < 5; ++i) {
			final int num = i;
			tp.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					FullHttpResponse response1 = connector.callGet("https://httpbin.org/stream/" + num,
							new BasicHeader("Autorization", "Bearer MyToken"),
							new BasicHeader("User-Agent", "My browser"));
					System.out.println("Response1: " + response1);
					return null;
				}

			});
		}

		for (int i = 0; i < 5; ++i) {
			final int num = i;
			tp.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					String json = "{\"login\":\"mylogin" + num + "\",\"password\":\"p@ssword" + num + "\"}";
					FullHttpResponse response2 = connector.callPost("http://localhost:8000/login", "application/json",
							json.getBytes(StandardCharsets.UTF_8), new BasicHeader("Autorization", "Bearer MyToken"),
							new BasicHeader("User-Agent", "My browser"));
					System.out.println("Response2: " + response2);
					return null;
				}
			});
		}

		tp.shutdown();

	}

}
