package eu.thog92.dramagen;

import eu.thog92.generator.api.http.IRequestHandler;
import eu.thog92.generator.api.tasks.GeneratorTask;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.AsciiString;

import java.io.*;
import java.nio.charset.Charset;

import static eu.thog92.generator.api.http.HttpServer.sendError;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class DramaHandler implements IRequestHandler
{

    private static final AsciiString CONTENT_TYPE = new AsciiString("Content-Type");
    private static final AsciiString CONTENT_LENGTH = new AsciiString("Content-Length");
    private static final AsciiString CONNECTION = new AsciiString("Connection");
    private static final AsciiString KEEP_ALIVE = new AsciiString("keep-alive");
    private final boolean plainTxt;
    private final GeneratorTask generatorTask;

    public DramaHandler(GeneratorTask generatorTask, boolean plain)
    {
        this.generatorTask = generatorTask;
        this.plainTxt = plain;
    }

    private byte[] handlePlain() throws IOException
    {
        String randomDrama = generatorTask.generateSentence(false);
        if (randomDrama == null)
            randomDrama = "The Minecraft Drama Generator has been bought by Microsoft.";
        return randomDrama.getBytes(Charset.forName("UTF-8"));
    }

    private byte[] handleWithHTML(ChannelHandlerContext ctx, FullHttpRequest request)
    {
        try
        {
            InputStream in = DramaHandler.class.getResourceAsStream("/public/drama.html");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
            {
                out.append(line);
            }
            reader.close();
            String randomDrama = generatorTask.generateSentence(false);
            if (randomDrama == null)
            {
                randomDrama = "The Minecraft Drama Generator has been bought by Microsoft.";
            }

            byte[] response = out.toString().replace("%DRAMA%", randomDrama).getBytes(Charset.forName("UTF-8"));

            return response;
        } catch (Exception e)
        {
            e.printStackTrace();
            sendError(ctx, INTERNAL_SERVER_ERROR);
            return null;
        }
    }

    @Override
    public void handle(ChannelHandlerContext ctx, FullHttpRequest request, String path) throws Exception
    {
        if (HttpUtil.is100ContinueExpected(request))
            ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
        boolean keepAlive = HttpUtil.isKeepAlive(request);
        byte[] data = plainTxt ? this.handlePlain() : this.handleWithHTML(ctx, request);
        FullHttpResponse response;
        if (data == null)
            response = new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR);
        else
            response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(data));
        response.headers().set(CONTENT_TYPE, plainTxt ? "text/plain" : "text/html");
        response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());

        if (!keepAlive)
        {
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        } else
        {
            response.headers().set(CONNECTION, KEEP_ALIVE);
            ctx.write(response);
        }
        ctx.flush();
    }
}
