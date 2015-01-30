package eu.thog92.dramagen.http.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class ResourceHandler implements HttpHandler {

	@Override
	public void handle(HttpExchange ext) throws IOException {
		OutputStream os = ext.getResponseBody();
		try {
			String path = ext.getRequestURI().toString().replaceFirst("/", "/public/");
			if (path.equalsIgnoreCase("/public/")) {
				path = "/public/index.html";
			}
			URL resource = ResourceHandler.class.getResource(path);
			InputStream in = ResourceHandler.class.getResourceAsStream(path);
			if (resource == null) {
				String response = "File not found";
				ext.sendResponseHeaders(404, response.length());
				os.write(response.getBytes());
				os.close();
				return;
			}
			int len;
			byte[] buf = new byte[1024];
			ext.sendResponseHeaders(200, new File(resource.toURI()).length());
			while ((len = in.read(buf, 0, 1024)) != -1)
				os.write(buf, 0, len);
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
			os.write("File not found".getBytes());
			os.close();
		}

	}
}
