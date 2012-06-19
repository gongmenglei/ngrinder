package com.nhncorp.ngrinder.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.URIUtil;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.servlet.DispatcherServlet;

import com.nhncorp.ngrinder.listener.AgentInitDataListener;
import com.nhncorp.ngrinder.listener.NGrinderHanitorAgent;

/**
 * Agent main class with Jetty embedded web server.
 * 
 * @author JunHo Yoon
 */
public class AgentMain {

	public AgentMain() {
	}

	public void configure(Server server) {
		setConnector(server);
		setHandler(server);
	}

	public void setConnector(Server server) {
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setPort(8080);
		server.addConnector(connector);
	}

	public void setHandler(Server server) {
		ServletContextHandler handler = new ServletContextHandler(
				ServletContextHandler.SESSIONS) {
			@Override
			protected boolean isProtectedTarget(String target) {
				while (target.startsWith("//"))
					target = URIUtil.compactPath(target);
				return StringUtil.startsWithIgnoreCase(target, "/web-inf")
						|| StringUtil.startsWithIgnoreCase(target, "/meta-inf");
			}
		};
		handler.setContextPath("/");
		handler.setMaxFormContentSize(512 * 1024 * 1024);
		addContextLoaderListener(handler);
		addDispatcherServlet(handler);

		addDefaultServlet(handler);
		server.setHandler(handler);
	}

	public void addContextLoaderListener(ServletContextHandler handler) {
		handler.addEventListener(new ContextLoaderListener());
		handler.setInitParameter("contextConfigLocation",
				"classpath:applicationContext.xml");
		handler.addEventListener(new AgentInitDataListener());

		handler.addEventListener(new NGrinderHanitorAgent());
	}

	public void addDispatcherServlet(ServletContextHandler handler) {
		ServletHolder holder = new ServletHolder(new DispatcherServlet());

		holder.setInitParameter("contextConfigLocation",
				"classpath:servlet-context.xml");
		holder.setInitOrder(3);
		handler.addServlet(holder, "/");
	}

	public void addDefaultServlet(ServletContextHandler handler) {
		ServletHolder holder = new ServletHolder(new DefaultServlet());
		// holder.setInitParameter("resourceBase", "");
		holder.setInitParameter("dirAllowed", "true");
		holder.setInitParameter("welcomeServlets", "false");
		holder.setInitParameter("gzip", "false");
		handler.addServlet(holder, "*.css");
		handler.addServlet(holder, "*.js");
		handler.addServlet(holder, "*.ico");
	}

	/**
	 * Main class for Agent
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Server server = new Server();
			new AgentMain().configure(server);
			server.start();
			server.join();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
