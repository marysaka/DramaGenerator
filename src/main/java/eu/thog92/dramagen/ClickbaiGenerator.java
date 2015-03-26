package eu.thog92.dramagen;


import eu.thog92.generator.api.BotGenerator;
import eu.thog92.generator.api.Dictionary;
import eu.thog92.generator.api.annotations.Module;
import eu.thog92.generator.api.annotations.SubscribeEvent;
import eu.thog92.generator.api.config.Configuration;
import eu.thog92.generator.api.events.InitEvent;
import eu.thog92.generator.api.tasks.ScheduledTask;
import eu.thog92.generator.core.tasks.GeneratorTask;
import eu.thog92.generator.core.tasks.TwitterTask;
import eu.thog92.generator.twitter.TwitterModule;

import java.io.File;
import java.io.IOException;

@Module(name = "Clickbait", version = "1.1",  dependencies = "after:twitter;")
public class ClickbaiGenerator
{
    private BotGenerator generator;
    private GeneratorTask generatorTask;
    private ScheduledTask twitterTask;
    private Dictionary dictionary;

    @SubscribeEvent
    public void init(InitEvent event)
    {
        File moduleDir = new File(event.getConfigDir(), "ClickBait");
        moduleDir.mkdirs();
        generator = event.getBotGenerator();

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
        this.generatorTask = new GeneratorTask(dictionary);

        Configuration configuration = new Configuration(event.getConfigDir(), "ClickBait");
        ClickbaitConfiguration clickbaitConfiguration = configuration.readFromFile(ClickbaitConfiguration.class);
        if (clickbaitConfiguration == null)
        {
            clickbaitConfiguration = new ClickbaitConfiguration(1800);
            configuration.saveToDisk(clickbaitConfiguration);
        }

        this.twitterTask = new TwitterTask(TwitterModule.getInstance().getTwitter(), this.generatorTask, true, null);
        this.twitterTask.setDelay(clickbaitConfiguration.tweetDelay);

        generator.getTasksManager().scheduleTask(this.twitterTask);
    }
}
