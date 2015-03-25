package eu.thog92.dramagen;


import eu.thog92.generator.api.BotGenerator;
import eu.thog92.generator.api.annotations.Module;
import eu.thog92.generator.api.annotations.SubscribeEvent;
import eu.thog92.generator.api.events.InitEvent;
import eu.thog92.generator.api.tasks.ScheduledTask;
import eu.thog92.generator.core.tasks.GeneratorTask;
import eu.thog92.generator.core.tasks.TwitterTask;

@Module(name = "Clickbait", version = "1.1")
public class ClickbaiGenerator
{
    private BotGenerator generator;
    private GeneratorTask generatorTask;
    private ScheduledTask twitterTask;

    @SubscribeEvent
    public void init(InitEvent event)
    {
        generator = event.getBotGenerator();
        this.generatorTask = new GeneratorTask();
        this.twitterTask = new TwitterTask(this.generatorTask);
        this.twitterTask.setDelay(generator.getConfig().delay);
        generator.getTasksManager().scheduleTask(this.twitterTask);
    }
}
