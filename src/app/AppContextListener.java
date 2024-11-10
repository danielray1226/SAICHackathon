package app;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {
	volatile static String root;
	public void contextInitialized(javax.servlet.ServletContextEvent sce) {		
		root = sce.getServletContext().getRealPath("/");
		System.err.println("Context Initialized with " +root);
		App.getApp(); // Force app instance to get loaded
	}
	public void contextDestroyed(javax.servlet.ServletContextEvent sce) {
		System.err.println("Context Destroyed");
	}
	public static String getRoot() {
		if (root == null) {
			String envRoot = System.getenv("TEST_WEB_FILE_ROOT");
			if (envRoot == null) {
				throw new RuntimeException("Please define the environment variable TEST_WEB_FILE_ROOT.");
			}
			return envRoot;
		} else
			return root;
	}
}
