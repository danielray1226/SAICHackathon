package app;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class LoginServlet
 */
@WebServlet("/logoff")
public class LogoffServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public LogoffServlet() {
	}

	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Cookie appCookie = App.getApp().getRequestCookie(request);
		if (appCookie!=null) {
			if (App.getApp().removeCookie(appCookie.getValue())) {
				response.setStatus(200);
			} else {
				System.err.println("Unknown cookie: "+appCookie.getValue());
				response.setStatus(500);
			}
		}
	}

}
