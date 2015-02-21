package eu.thog92.dramagen;

import eu.thog92.dramagen.http.HttpServerManager;
import eu.thog92.dramagen.task.DramaTask;
import eu.thog92.dramagen.task.TwitterTask;

import java.io.IOException;

public class DramaGenerator {

    private static DramaGenerator INSTANCE = new DramaGenerator();
    private HttpServerManager httpServerManager;
    private TasksManager tasksManager;
    private TwitterTask drama;
    private DramaTask dramaTask;

    private DramaGenerator() {
    }

    public static void main(String[] args) {
        DramaGenerator instance = new DramaGenerator();
        instance.init();
    }

    public static DramaGenerator getInstance() {
        return INSTANCE;
    }

    private void init() {
        try {
            this.tasksManager = new TasksManager();
            this.dramaTask = new DramaTask();
            drama = new TwitterTask(tasksManager, dramaTask);
            drama.setDelay(tasksManager.getConfig().delay);
            this.tasksManager.scheduleTask(drama);
            this.httpServerManager = new HttpServerManager(this, this.tasksManager.getConfig());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("INIT Failed! EXITING...");
            System.exit(-1);
        }
    }

    public void reload() {
        try {
            this.tasksManager.reload();
            drama.setDelay(tasksManager.getConfig().delay);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public DramaTask getDramaTask() {
        return dramaTask;
    }

}
