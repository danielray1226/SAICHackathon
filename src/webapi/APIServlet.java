package webapi;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class APIServlet
 */
@WebServlet(asyncSupported = true, urlPatterns = { "/api" })
public class APIServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	ExecutorService processingService = Executors.newFixedThreadPool(10);
    Translator translator= new Translator();
    /**
     * @see HttpServlet#HttpServlet()
     */
    public APIServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response)
	 */
    
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = request.getPathInfo();
		if (path != null) {
			path = path.substring(1);
		}
		
		AsyncContext asyncContext = request.startAsync(request, response);
		asyncContext.setTimeout(3000000);
		new APIRequestHandler(asyncContext, processingService, translator);
		
	}

}
