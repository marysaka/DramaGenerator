package eu.thog92.dramagen.http;

import com.sun.net.httpserver.HttpServer;
import eu.thog92.dramagen.DramaGenerator;
import eu.thog92.dramagen.http.handler.RefreshHandler;
import eu.thog92.dramagen.http.handler.ResourceHandler;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpServerManager {

	public HttpServerManager(DramaGenerator instance) {
		HttpServer server;
		try {
			server = HttpServer.create(new InetSocketAddress(1010), 0);
			server.createContext("/refresh", new RefreshHandler(instance));
			//server.createContext("/public/", new ResourceHandler());
			server.setExecutor(null); // creates a default executor
			server.start();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
