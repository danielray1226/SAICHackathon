package app;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import utils.HttpConnector;
import utils.JsonUtils;

public class App {
	final String root;
	final JsonObject openApi;
	final HttpConnector httpConnector=new HttpConnector();
	
	private App(String r) {
		this.root=r;
		try {
			openApi= JsonParser.parseString(new String(Files.readAllBytes(new File(root+"/WEB-INF/data/openapi.json").toPath()), StandardCharsets.UTF_8)).getAsJsonObject();
		} catch (Exception ex) {
			System.err.println("Failed to load/parse openapi.json");
			throw new RuntimeException("Failed to load/parse openapi.json : "+ex.getMessage());
		}	

	}
	private void init() {
		//placeholder for any threads you wanna start so they start after app is fully constructed
		
	}
	private static class Helper {
		final static App app=initApp(); //load directory of the app
		private static App initApp() {
			App tmp=new App(AppContextListener.getRoot());
			tmp.init();
			return tmp;
		}
	}
	public static App getApp() {return Helper.app;}
	public String getAppCookieName() {return "api-tester";}
	public JsonObject getOpenApi() {return getApp().openApi;}
	public Cookie getRequestCookie(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie c : cookies) {
				if (getAppCookieName().equals(c.getName())) {
					return c;
				}
			}
		}
		return null;
	}
	public String getRoot() {return root;}
	public HttpConnector getHttpConnector() {return httpConnector;}
	
	synchronized public JsonObject checkUserByCookie(String cookie) throws IOException {
		// poor's man database
		JsonElement cookies = JsonParser.parseString(new String(Files.readAllBytes(new File(root+"/WEB-INF/data/cookiesdb.json").toPath()), StandardCharsets.UTF_8));
		String login=JsonUtils.getString(cookies, cookie); //gets value of attributes as a string
		if (login!=null) {
			JsonElement users = JsonParser.parseString(new String(Files.readAllBytes(new File(root+"/WEB-INF/data/usersdb.json").toPath()), StandardCharsets.UTF_8));
			JsonObject userInfo = JsonUtils.getJsonObject(users, login);
			return userInfo;
		}
		return null;
	}
	synchronized public JsonObject checkAddUserCookie(String login, String password, String newCookie) throws JsonSyntaxException, IOException {
		JsonElement users = JsonParser.parseString(new String(Files.readAllBytes(new File(root+"/WEB-INF/data/usersdb.json").toPath()), StandardCharsets.UTF_8));
		String storedPassword=JsonUtils.getString(users, login, "password");
		if (storedPassword!=null && storedPassword.equals(password)) {
			saveUserCookie(login, newCookie);
			return JsonUtils.getJsonObject(users, login);
		}
		return null;
	}
	private void saveUserCookie(String login, String newCookie) throws JsonSyntaxException, IOException {
		JsonObject cookies = JsonParser.parseString(new String(Files.readAllBytes(new File(root+"/WEB-INF/data/cookiesdb.json").toPath()), StandardCharsets.UTF_8)).getAsJsonObject();
		cookies.addProperty(newCookie, login);
		Files.write(new File(root+"/WEB-INF/data/cookiesdb.json").toPath(),cookies.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
		
	}
	synchronized public boolean removeCookie(String cookie) throws IOException {
		JsonObject cookies = JsonParser.parseString(new String(Files.readAllBytes(new File(root+"/WEB-INF/data/cookiesdb.json").toPath()), StandardCharsets.UTF_8)).getAsJsonObject();
		JsonElement removed = cookies.remove(cookie);
		String removedLogin=JsonUtils.getString(removed);
		if (removedLogin!=null) for (Iterator<String> cookiesIterator = cookies.keySet().iterator(); cookiesIterator.hasNext();) {
			String storedCookie=cookiesIterator.next();
			String dupLogin=JsonUtils.getString(cookies, storedCookie);
			if (removedLogin.equals(dupLogin)) {
				cookiesIterator.remove();
			}
		}
		Files.write(new File(root+"/WEB-INF/data/cookiesdb.json").toPath(),cookies.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
		return removed!=null;
	}

	
	
}
