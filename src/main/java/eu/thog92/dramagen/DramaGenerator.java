package eu.thog92.dramagen;

import eu.thog92.dramagen.http.HttpServerManager;
import eu.thog92.dramagen.task.TwitterTask;

import java.io.IOException;

public class DramaGenerator {

	private HttpServerManager httpServerManager;
	private TasksManager tasksManager;
	private TwitterTask drama;

	private static DramaGenerator INSTANCE = new DramaGenerator();

	private DramaGenerator() {}

	private void init() {
		try {
			this.tasksManager = new TasksManager();
			drama = new TwitterTask(tasksManager);
			drama.setDelay(tasksManager.getConfig().delay);
			this.tasksManager.scheduleTask(drama);
			this.httpServerManager = new HttpServerManager(this);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("INIT Failed! EXITING...");
			System.exit(-1);
		}
	}

	public static void main(String[] args) {
		DramaGenerator instance = new DramaGenerator();
		instance.init();
	}

	public void reload() {
		try {
			this.tasksManager.reload();
			drama.setDelay(tasksManager.getConfig().delay);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static DramaGenerator getInstance()
	{
		return INSTANCE;
	}

}
