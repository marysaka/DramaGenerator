package eu.thog92.dramagen;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import eu.thog92.dramagen.task.ScheduledTask;
import eu.thog92.dramagen.util.ArrayListHelper;
import eu.thog92.dramagen.util.WritableArrayList;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TasksManager {

	private Twitter twitter;
	private WritableArrayList<String> blackList;
	private Dictionary dictionary;
	private ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(100);

	private HashMap<ScheduledTask, ScheduledFuture<?>> activeTasks = new HashMap<ScheduledTask, ScheduledFuture<?>>();
	private Config config;

	public TasksManager() throws IOException {
		this.loadConfig();
		this.dictionary = Dictionary.getInstance();
		this.dictionary.setDir(new File("data"));
		this.dictionary.loadCombinaisons();
		this.loadBlackList();
	}

	private void loadConfig() throws IOException {
		File configFile = new File("config.yml");
		if(!configFile.exists())
			throw new FileNotFoundException("Config not found");
		YamlReader reader = new YamlReader(new FileReader(configFile));
		config = reader.read(Config.class);
		reader.close();
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(config.debugTwitter)
				.setOAuthConsumerKey(config.consumerKey)
				.setOAuthConsumerSecret(config.consumerSecret)
				.setOAuthAccessToken(config.accessToken)
				.setOAuthAccessTokenSecret(config.accessTokenSecret);
		TwitterFactory tf = new TwitterFactory(cb.build());
		this.twitter = tf.getInstance();
		System.out.println("config.token" + config.accessToken);
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
		System.out.println("Reloading Config...");
		this.loadConfig();
		System.out.println("Reloading Dictionary...");
		this.dictionary.reload();
		System.out.println("Reloading BlackList...");
		this.loadBlackList();
		System.out.println("Config Reloaded");
	}

	public void scheduleTask(ScheduledTask task) {
		System.out.println("Scheduling " + task.getName() + "...");
		this.activeTasks.put(task, scheduler.scheduleAtFixedRate(task, 0,
				task.getDelay(), TimeUnit.SECONDS));
	}

	public void resetExecutorService(){
		scheduler.shutdownNow();
		scheduler = Executors.newScheduledThreadPool(100);
	}

	public void onFinishTask(ScheduledTask task) {
		if (task.isCancelled()) {
			if (this.activeTasks.get(task) != null)
				this.activeTasks.remove(task).cancel(true);
		}
	}

	public Twitter getTwitter(){
		return twitter;
	}

	public Config getConfig(){
		return config;
	}
}
