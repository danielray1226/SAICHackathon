package app;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import utils.JsonUtils;

/**
 * Servlet implementation class LoginServlet
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public LoginServlet() {
	}


	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Cookie appCookie = App.getApp().getRequestCookie(request);
		JsonObject userInfo = null;
		if (appCookie != null) {
			userInfo = App.getApp().checkUserByCookie(appCookie.getValue());
		}
		if (userInfo == null || appCookie == null) {
			// First time user, or unknown cookie
			appCookie = new Cookie(App.getApp().getAppCookieName(), UUID.randomUUID().toString());
			appCookie.setHttpOnly(true); // should we allow js to access it?
			appCookie.setMaxAge(365 * 24 * 3600);
			response.addCookie(appCookie); // Add it back to the response
		}
		if (userInfo == null) {
			JsonElement req = JsonParser.parseReader(request.getReader());
			String login = JsonUtils.getString(req, "login");
			String password = JsonUtils.getString(req, "password");
			userInfo = App.getApp().checkAddUserCookie(login, password, appCookie.getValue());
		}
		
		JsonObject resp=null;
		if (userInfo!=null) {
			resp=userInfo.deepCopy();
			resp.remove("password"); // lets not send it back
			resp.addProperty("success", true);
			response.setStatus(200);
		} else {
			resp = new JsonObject();
			resp.addProperty("success", false);
			resp.addProperty("message", "Please provide valid login/password");
			response.setStatus(403);
		}		
		response.getWriter().write(resp.toString());
	}

}
