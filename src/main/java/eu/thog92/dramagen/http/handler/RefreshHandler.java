package eu.thog92.dramagen.http.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import eu.thog92.dramagen.DramaGenerator;

import java.io.IOException;
import java.io.OutputStream;

public class RefreshHandler implements HttpHandler {

    private DramaGenerator main;

    public RefreshHandler(DramaGenerator instance) {
        this.main = instance;
    }

    @Override
    public void handle(HttpExchange ext) throws IOException {
        main.reload();
        String response = "Config Reloaded";
        ext.sendResponseHeaders(200, response.length());
        OutputStream os = ext.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

}