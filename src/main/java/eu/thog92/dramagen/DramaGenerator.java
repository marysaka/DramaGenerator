package eu.thog92.dramagen;

import eu.thog92.dramagen.http.HttpServerHandlers;
import eu.thog92.dramagen.task.DramaTask;

import java.io.IOException;

public class DramaGenerator {

	private HttpServerHandlers httpServerManager;
	private TasksManager tasksManager;

	public DramaGenerator() {

	}

	private void init(int i) {
		try {
			this.tasksManager = new TasksManager();
			DramaTask drama = new DramaTask(tasksManager);
			drama.setDelay(i);
			this.tasksManager.scheduleTask(drama);
			this.httpServerManager = new HttpServerHandlers(this);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("INIT Failed! EXITING...");
			System.exit(-1);
		}
	}

	public static void main(String[] args) {
		int i = 900;
		if (args.length == 1)
			i = Integer.parseInt(args[0]);

		DramaGenerator instance = new DramaGenerator();
		instance.init(i);
	}

	public void reload() {
		try {
			this.tasksManager.reload();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
