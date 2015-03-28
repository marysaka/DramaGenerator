package eu.thog92.dramagen;


import com.sun.net.httpserver.HttpServer;
import eu.thog92.generator.api.BotGenerator;
import eu.thog92.generator.api.Dictionary;
import eu.thog92.generator.api.annotations.Module;
import eu.thog92.generator.api.annotations.SubscribeEvent;
import eu.thog92.generator.api.config.Configuration;
import eu.thog92.generator.api.events.HttpStartEvent;
import eu.thog92.generator.api.events.InitEvent;
import eu.thog92.generator.api.tasks.ScheduledTask;
import eu.thog92.generator.api.tasks.GeneratorTask;
import eu.thog92.generator.twitter.TwitterTask;
import eu.thog92.generator.twitter.TwitterModule;

import java.io.File;
import java.io.IOException;

@Module(name = "Drama", version = "1.1", dependencies = "after:twitter;")
public class DramaGenerator
{
    private BotGenerator generator;
    private GeneratorTask generatorTask;
    private ScheduledTask twitterTask;
    private HttpServer server;
    private Dictionary dictionary;

    @SubscribeEvent
    public void init(InitEvent event)
    {
        File moduleDir = new File(event.getConfigDir(), "Drama");
        moduleDir.mkdirs();
        generator = event.getBotGenerator();
        Configuration configuration = new Configuration(event.getConfigDir(), "Drama");
        DramaConfiguration dramaConfiguration = configuration.readFromFile(DramaConfiguration.class);
        if (dramaConfiguration == null)
        {
            dramaConfiguration = new DramaConfiguration(3600, 8080);
            configuration.saveToDisk(dramaConfiguration);
        }

        this.dictionary = new Dictionary();
        this.dictionary.setDir(new File(moduleDir, "dictionary"));
        try
        {
            this.dictionary.loadCombinations();
            this.dictionary.loadBlackList();
        } catch (IOException e)
        {
            e.printStackTrace();
            System.exit(49);
        }

        this.generatorTask = new GeneratorTask(this.dictionary);
        this.twitterTask = new TwitterTask(TwitterModule.getInstance().getTwitter(), this.generatorTask, true, null);
        this.twitterTask.setDelay(dramaConfiguration.tweetDelay);

        generator.getTasksManager().scheduleTask(this.twitterTask);
        try
        {
            this.server = generator.getHttpManager().createHTTPServer(dramaConfiguration.httpPort);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        System.out.println("Drama is ready.");

    }

    @SubscribeEvent
    public void onHTTPServerInit(HttpStartEvent event)
    {
        System.out.println("Loading HTTP Server...");
        HttpServer server = event.getServer();
        server.createContext("/drama", new DramaHandler(generatorTask, false));
        server.createContext("/api/drama", new DramaHandler(generatorTask, true));
    }
}
