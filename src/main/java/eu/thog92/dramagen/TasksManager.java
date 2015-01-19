package eu.thog92.dramagen;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import eu.thog92.dramagen.task.ScheduledTask;
import eu.thog92.dramagen.util.ArrayListHelper;
import eu.thog92.dramagen.util.WritableArrayList;

public class TasksManager {

	private WritableArrayList<String> blackList;
	private Dictionary dictionary;
	private ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(100);

	public TasksManager() throws IOException {
		this.dictionary = Dictionary.getInstance();
		this.dictionary.setDir(new File("data"));
		this.dictionary.loadCombinaisons();
		this.loadBlackList();
	}

	private void loadBlackList() throws IOException {

		File blackListFile = new File("blacklist.txt");
		if (!blackListFile.exists())
			blackListFile.createNewFile();
		this.blackList = new WritableArrayList<String>(
				ArrayListHelper.loadStringArrayFromFile(blackListFile
						.getAbsolutePath()), blackListFile);
	}

	public WritableArrayList<String> getBlackList() {
		return blackList;
	}

	public void reload() throws IOException {
		System.out.println("Reloading Dictionary...");
		this.dictionary.reload();
		System.out.println("Reloading BlackList...");
		this.loadBlackList();
		System.out.println("Config Reloaded");
	}

	public void scheduleTask(ScheduledTask task) {
		task.run();
	}

	public void onFinishTask(ScheduledTask task) {
		if (!task.isCancelled()) {
			scheduler.schedule(task, task.getDelay(), TimeUnit.SECONDS);
		}
	}
}
