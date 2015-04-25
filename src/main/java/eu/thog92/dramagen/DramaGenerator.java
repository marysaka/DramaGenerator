package eu.thog92.dramagen;


import com.sun.net.httpserver.HttpServer;
import eu.thog92.generator.api.BotGenerator;
import eu.thog92.generator.api.Dictionary;
import eu.thog92.generator.api.annotations.Module;
import eu.thog92.generator.api.annotations.SubscribeEvent;
import eu.thog92.generator.api.config.Configuration;
import eu.thog92.generator.api.events.HttpStartEvent;
import eu.thog92.generator.api.events.InitEvent;
import eu.thog92.generator.api.events.irc.IRCChannelMessage;
import eu.thog92.generator.api.events.irc.IRCPrivateMessage;
import eu.thog92.generator.api.events.irc.IRCReady;
import eu.thog92.generator.api.irc.IRCClient;
import eu.thog92.generator.api.irc.IRCConfiguration;
import eu.thog92.generator.api.tasks.GeneratorTask;
import eu.thog92.generator.api.tasks.ScheduledTask;
import eu.thog92.generator.twitter.TwitterConfiguration;
import eu.thog92.generator.twitter.TwitterModule;
import eu.thog92.generator.twitter.TwitterTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

@Module(name = "Drama", version = "1.1", dependencies = "after:twitter;")
public class DramaGenerator
{
    private BotGenerator generator;
    private GeneratorTask generatorTask;
    private ScheduledTask twitterTask;
    private HttpServer server;
    private Dictionary dictionary;
    private IRCConfiguration ircConfiguration;
    private DramaConfiguration dramaConfiguration;

    @SubscribeEvent
    public void init(InitEvent event)
    {
        File moduleDir = new File(event.getConfigDir(), "Drama");
        moduleDir.mkdirs();
        generator = event.getBotGenerator();
        Configuration globalSettings = new Configuration(event.getConfigDir(), "Drama", "drama");
        dramaConfiguration = globalSettings.readFromFile(DramaConfiguration.class);

        Configuration twitterSettings = new Configuration(event.getConfigDir(), "Drama", "twitter");

        if (dramaConfiguration == null)
        {
            dramaConfiguration = new DramaConfiguration(3600, 8080);
            globalSettings.saveToDisk(dramaConfiguration);
        }

        TwitterConfiguration twitterConfiguration = twitterSettings.readFromFile(TwitterConfiguration.class);
        if (twitterConfiguration == null)
        {
            System.err.println("[Drama Generator] A new Twitter config have been created! Complete it before restart.");
            twitterSettings.saveToDisk(new TwitterConfiguration("", "", "", "", ""));
            System.exit(666);
        }

        Configuration ircSettings = new Configuration(event.getConfigDir(), "Drama", "irc");
        ircConfiguration = ircSettings.readFromFile(IRCConfiguration.class);

        if (ircConfiguration == null)
        {
            System.err.println("[Drama Generator] A new IRC config have been created! Complete it before restart.");
            ircSettings.saveToDisk(new IRCConfiguration("", 6666, "DramaGenerator", new String[] {"#WAMM"}));
            System.exit(333);
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
        this.twitterTask = new TwitterTask(TwitterModule.getInstance().createTwitterInstance(twitterConfiguration.debug, twitterConfiguration.consumerKey, twitterConfiguration.consumerSecret, twitterConfiguration.accessToken, twitterConfiguration.accessTokenSecret), this.generatorTask, twitterConfiguration.sendTweetOnStartup, null);
        this.twitterTask.setDelay(dramaConfiguration.tweetDelay);

        generator.getTasksManager().scheduleTask(this.twitterTask);
        try
        {
            this.server = generator.getHttpManager().createHTTPServer(dramaConfiguration.httpPort);

            if(ircConfiguration.enabled)
            {
                File logFile = new File("log/drama/irc.log");
                logFile.getParentFile().mkdirs();
                logFile.delete();
                logFile.createNewFile();

                FileOutputStream out = new FileOutputStream(logFile);
                IRCClient ircClient = IRCClient.createIRCClient(ircConfiguration.hostname, ircConfiguration.port, ircConfiguration.username).addChannels(ircConfiguration.channels).setPrintStream(new PrintStream(out));
                ircClient.connect();
            }
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

    @SubscribeEvent
    public void onReady(IRCReady event)
    {

    }

    @SubscribeEvent
    public void onMesssage(IRCChannelMessage event)
    {
        if(event.getMessage().contains(dramaConfiguration.commandPrefix + dramaConfiguration.dramaCommand))
        {
            event.getIRCClient().sendToChat(event.getChannel(), this.generatorTask.generateSentence(false));
        }
        else if(event.getMessage().toLowerCase().startsWith(dramaConfiguration.commandPrefix + "join"))
        {
            String[] split = event.getMessage().split(" ");
            if(split.length == 0)
                event.getIRCClient().sendToChat(event.getChannel(), "Illegal Arguments");
            else
                event.getIRCClient().joinChannel(split[1]);
        }
    }

    @SubscribeEvent
    public void onPrivateMessage(IRCPrivateMessage event)
    {
        if(event.getMessage().contains(dramaConfiguration.dramaCommand))
        {
            event.getIRCClient().sendToChat(event.getSender(), this.generatorTask.generateSentence(false));
        }
        /*else if(event.getMessage().toLowerCase().startsWith("join"))
        {
            String[] split = event.getMessage().split(" ");
            if(split.length == 0)
                event.getIRCClient().sendToChat(event.getSender(), "Illegal Arguments");
            else
                event.getIRCClient().joinChannel(split[1]);
        }*/
    }
}
