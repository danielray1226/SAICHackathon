package testapi;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import app.App;
import utils.JsonUtils;

/**
 * Servlet implementation class TestAPI
 */
@WebServlet("/TestAPI/*")
public class TestAPI extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TestAPI() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String method = request.getMethod();
		String path = request.getPathInfo();
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		System.err.println("Path: " + request.getPathInfo());
		System.err.print(method);
		
		// Lets try to generate a file path, we can use for testing
		String testFileName=method.toLowerCase()+request.getPathInfo();
		
		
		
		Map<String, String[]> params = request.getParameterMap();
		for (Entry<String, String[]> entry : params.entrySet()) {
			String param=entry.getKey();
			String[] values=entry.getValue();
			for (String v : values) {
				testFileName+= ("_"+param+"_"+v);
				System.err.println(param+" = "+v);
				
			}
		}
		
		String tokenString="";
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String name=headerNames.nextElement();
			Enumeration<String> headers = request.getHeaders(name);
			while (headers.hasMoreElements()) {
				String value=headers.nextElement();
				System.err.println("HEADER "+name+"="+value);
				if ("authorization".equalsIgnoreCase(name)) tokenString=value;
			}
		}
		String[] tokenParts = tokenString.split("\\s+");
		String token=tokenParts.length>1?tokenParts[1]:tokenString;
		testFileName+= "_"+token;
		
		
		testFileName=testFileName.replaceAll("/", "_");
		testFileName=testFileName.replaceAll(" ", "");
		
		File testFile=new File(App.getApp().getRoot()+"/WEB-INF/test_api_data/"+testFileName+".json");
		System.err.println("Looking up data in: "+testFile);
		
		if (testFile.exists()) {
			//JsonElement testStuff = JsonParser.parseString(new String(,
			JsonElement testReply = JsonParser.parseString(new String(Files.readAllBytes(testFile.toPath()), StandardCharsets.UTF_8));
			int status=JsonUtils.getInteger(testReply, "status");
			JsonElement data = JsonUtils.getJsonElement(testReply, "data");
			if (data!=null) response.getWriter().write(data.toString());
			response.setStatus(status);
		} else {
			response.setStatus(404);
		}
	}

}
