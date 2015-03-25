package eu.thog92.dramagen;


import com.sun.net.httpserver.HttpServer;
import eu.thog92.generator.api.BotGenerator;
import eu.thog92.generator.api.annotations.Module;
import eu.thog92.generator.api.annotations.SubscribeEvent;
import eu.thog92.generator.api.events.HttpInitEvent;
import eu.thog92.generator.api.events.InitEvent;
import eu.thog92.generator.api.tasks.ScheduledTask;
import eu.thog92.generator.core.tasks.GeneratorTask;
import eu.thog92.generator.core.tasks.TwitterTask;

@Module(name = "Drama", version = "1.1")
public class DramaGenerator
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

    @SubscribeEvent
    public void onHTTPServerInit(HttpInitEvent event)
    {
        HttpServer server = event.getServer();
        server.createContext("/drama", new DramaHandler(generatorTask, false));
        server.createContext("/api/drama", new DramaHandler(generatorTask, true));
    }
}
