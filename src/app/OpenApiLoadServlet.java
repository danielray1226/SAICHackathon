package app;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;

import utils.JsonUtils;

/**
 * Servlet implementation class OpenApiLoadServlet
 */
@WebServlet("/OpenApiData")
public class OpenApiLoadServlet extends HttpServlet {
      
    /**
     * @see HttpServlet#HttpServlet()
     */
    public OpenApiLoadServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		/*
		 Do we need authorization here?
		 If so, we need to check the cookie, and see if its registerd for the user
		 
		 	Cookie appCookie = App.getApp().getRequestCookie(request);
			JsonObject userInfo = null;
			if (appCookie != null) {
				userInfo = App.getApp().checkUserByCookie(appCookie.getValue());
			} 
			if (userInfo==null) spit out authorization error with 403
		 */
		
		response.getWriter().write(JsonUtils.prettyPrint( App.getApp().getOpenApi() ));
		response.setStatus(200);
	}

}
