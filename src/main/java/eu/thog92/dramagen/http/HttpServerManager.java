package eu.thog92.dramagen.http;

import com.sun.net.httpserver.HttpServer;
import eu.thog92.dramagen.Config;
import eu.thog92.dramagen.DramaGenerator;
import eu.thog92.dramagen.http.handler.DramaHandler;
import eu.thog92.dramagen.http.handler.RefreshHandler;
import eu.thog92.dramagen.http.handler.ResourceHandler;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpServerManager {

	public HttpServerManager(DramaGenerator instance, Config cfg) {
		HttpServer server;
		try {
			server = HttpServer.create(new InetSocketAddress(cfg.port), 0);
			server.createContext("/", new ResourceHandler());
			server.createContext("/refresh", new RefreshHandler(instance));
			server.createContext("/drama", new DramaHandler(instance.getDramaTask(), false));
			server.createContext("/api/drama", new DramaHandler(instance.getDramaTask(), true));
			server.setExecutor(null); // creates a default executor
			server.start();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
