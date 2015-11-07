package eu.thog92.dramagen;


import eu.thog92.generator.api.BotGenerator;
import eu.thog92.generator.api.Dictionary;
import eu.thog92.generator.api.annotations.Module;
import eu.thog92.generator.api.annotations.SubscribeEvent;
import eu.thog92.generator.api.config.Configuration;
import eu.thog92.generator.api.events.InitEvent;
import eu.thog92.generator.api.events.irc.IRCChannelMessage;
import eu.thog92.generator.api.events.irc.IRCPrivateMessage;
import eu.thog92.generator.api.events.irc.IRCReady;
import eu.thog92.generator.api.irc.IRCClient;
import eu.thog92.generator.api.irc.IRCConfiguration;
import eu.thog92.generator.api.tasks.GeneratorTask;
import eu.thog92.generator.api.tasks.ScheduledTask;
import eu.thog92.generator.api.twitter.TwitterConfiguration;
import eu.thog92.generator.api.twitter.TwitterModule;
import eu.thog92.generator.api.twitter.TwitterTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;

@Module(name = "Clickbait", version = "1.3", dependencies = "after:twitter;")
public class ClickbaiGenerator
{
    private BotGenerator generator;
    private GeneratorTask generatorTask;
    private ScheduledTask twitterTask;
    private Dictionary dictionary;
    private IRCConfiguration ircConfiguration;
    private ClickbaitConfiguration clickbaitConfiguration;

    @SubscribeEvent
    public void init(InitEvent event)
    {
        File moduleDir = new File(event.getConfigDir(), "ClickBait");
        moduleDir.mkdirs();
        generator = event.getBotGenerator();
        Configuration globalSettings = new Configuration(event.getConfigDir(), "ClickBait", "global");
        clickbaitConfiguration = globalSettings.readFromFile(ClickbaitConfiguration.class);

        Configuration twitterSettings = new Configuration(event.getConfigDir(), "ClickBait", "twitter");

        if (clickbaitConfiguration == null)
        {
            clickbaitConfiguration = new ClickbaitConfiguration(3600);
            globalSettings.saveToDisk(clickbaitConfiguration);
        }

        TwitterConfiguration twitterConfiguration = twitterSettings.readFromFile(TwitterConfiguration.class);
        if (twitterConfiguration == null)
        {
            System.err.println("[ClickBait Generator] A new Twitter config have been created! Complete it before restart.");
            twitterSettings.saveToDisk(new TwitterConfiguration("", "", "", "", "#MinecraftModdingClickbait"));
            System.exit(666);
        }

        Configuration ircSettings = new Configuration(event.getConfigDir(), "ClickBait", "irc");
        ircConfiguration = ircSettings.readFromFile(IRCConfiguration.class);

        if (ircConfiguration == null)
        {
            System.err.println("[ClickBait Generator] A new IRC config have been created! Complete it before restart.");
            ircSettings.saveToDisk(new IRCConfiguration("", 6666, "ClickbaitGenerator", new String[]{"#WAMM"}));
            System.exit(333);
        }

        this.dictionary = new Dictionary();
        this.dictionary.setDir(new File(moduleDir, "dictionary"));
        try
        {
            this.dictionary.load();
        } catch (IOException e)
        {
            e.printStackTrace();
            System.exit(49);
        }

        this.generatorTask = new GeneratorTask(this.dictionary);
        this.twitterTask = new TwitterTask(TwitterModule.getInstance().createTwitterInstance(twitterConfiguration.debug, twitterConfiguration.consumerKey, twitterConfiguration.consumerSecret, twitterConfiguration.accessToken, twitterConfiguration.accessTokenSecret), this.generatorTask, twitterConfiguration.sendTweetOnStartup, twitterConfiguration.endOfSentence);
        this.twitterTask.setDelay(clickbaitConfiguration.tweetDelay);

        generator.getTasksManager().scheduleTask(this.twitterTask);
        try
        {
            if (ircConfiguration.enabled)
            {
                File logFile = new File("log/clickbait/irc.log");
                logFile.getParentFile().mkdirs();
                logFile.delete();
                logFile.createNewFile();

                FileOutputStream out = new FileOutputStream(logFile);
                IRCClient ircClient = IRCClient.createIRCClient(ircConfiguration.hostname, ircConfiguration.port, ircConfiguration.username).addChannels(ircConfiguration.channels).setPrintStream(new PrintStream(out, false, "UTF-8"));
                ircClient.connect();
            }
            generator.checkAndCreateIRCClient(ircConfiguration, "clickbait");
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        System.out.println("ClickBait is ready.");

    }

    @SubscribeEvent
    public void onReady(IRCReady event)
    {

    }

    @SubscribeEvent
    public void onMesssage(IRCChannelMessage event)
    {
        if (event.getMessage().contains(clickbaitConfiguration.commandPrefix + clickbaitConfiguration.clickbaitCommand))
        {
            event.getIRCClient().sendToChat(event.getChannel(), this.generatorTask.generateSentence(false));
        } else if (event.getMessage().toLowerCase().startsWith(clickbaitConfiguration.commandPrefix + "join"))
        {
            String[] split = event.getMessage().split(" ");
            if (split.length == 0)
                event.getIRCClient().sendToChat(event.getChannel(), "Illegal Arguments");
            else
                event.getIRCClient().joinChannel(split[1]);
        }
    }

    @SubscribeEvent
    public void onPrivateMessage(IRCPrivateMessage event)
    {
        if (event.getMessage().contains(clickbaitConfiguration.clickbaitCommand))
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
