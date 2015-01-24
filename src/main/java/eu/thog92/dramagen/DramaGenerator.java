package eu.thog92.dramagen;

import eu.thog92.dramagen.http.HttpServerHandlers;
import eu.thog92.dramagen.task.DramaTask;

import java.io.IOException;

public class DramaGenerator {

	private HttpServerHandlers httpServerManager;
	private TasksManager tasksManager;
	private DramaTask drama;

	public DramaGenerator() {

	}

	private void init() {
		try {
			this.tasksManager = new TasksManager();
			drama = new DramaTask(tasksManager);
			drama.setDelay(tasksManager.getConfig().delay);
			this.tasksManager.scheduleTask(drama);
			this.httpServerManager = new HttpServerHandlers(this);
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

}
