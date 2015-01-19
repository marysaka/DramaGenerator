package eu.thog92.dramagen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.thog92.dramagen.http.HttpServerHandlers;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class DramaGenerator implements Runnable {

	private Random rand;
	private HashMap<String, ArrayList<String>> dictionary;
	private ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(1);
	private WritableArrayList<String> blackList;
	private long delay;
	private Twitter twitter;
	private static final Pattern REGEX = Pattern.compile(Pattern.quote("[")
			+ "(.*?)" + Pattern.quote("]"));

	public DramaGenerator() {
		this.rand = new Random();
		twitter = new TwitterFactory().getInstance();

		try {
			new HttpServerHandlers(this);
			this.loadCombinaisons();
			this.loadBlackList();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private void loadBlackList() throws IOException {

		File blackListFile = new File("blacklist.txt");
		if (!blackListFile.exists())
			blackListFile.createNewFile();
		this.blackList = new WritableArrayList<String>(
				loadStringFromFile(blackListFile.getAbsolutePath()),
				blackListFile);

	}

	public void loadCombinaisons() throws IOException {
		this.dictionary = new HashMap<String, ArrayList<String>>();
		File dir = new File("data");
		for (String file : dir.list()) {
			dictionary.put(file.replaceAll(".txt", ""),
					loadStringFromFile(dir.getAbsolutePath() + File.separator
							+ file));
		}
	}

	private ArrayList<String> loadStringFromFile(String file)
			throws IOException {

		System.out.println("Loading " + file.replaceAll(".txt", "") + "...");
		ArrayList<String> tmp = new ArrayList<String>();
		BufferedReader fileIn = new BufferedReader(new FileReader(file));

		String entry = null;

		while ((entry = fileIn.readLine()) != null) {
			tmp.add(entry);
		}

		fileIn.close();

		return tmp;
	}

	public String randomDrama() throws IOException {
		List<String> sentences = dictionary.get("sentences");
		int sentenceID = this.rand.nextInt(sentences.size());
		String sentence = sentences.get(sentenceID);
		Matcher toReplaces = REGEX.matcher(sentence);

		while (toReplaces.find()) {
			String toReplace = toReplaces.group(1);
			List<String> targetReplacementList = dictionary.get(toReplace);
			int replacementID = rand.nextInt(targetReplacementList.size());
			sentence = sentence.replaceFirst(toReplace,
					targetReplacementList.get(replacementID));
		}

		// TODO: Use a Pattern
		sentence = sentence.replace("[", "").replace("]", "");
		if (this.blackList.contains(sentence))
			this.randomDrama();

		this.blackList.addAndWrite(sentence);
		return sentence;
	}

	public static void main(String[] args) {
		int i = 900;
		if (args.length == 1)
			i = Integer.parseInt(args[0]);

		DramaGenerator instance = new DramaGenerator();
		instance.schedule(i);
		//instance.run();
	}

	private void schedule(int i) {
		this.delay = i;
	}

	@Override
	public void run() {
		try {
			ScheduledFuture<?> future = scheduler.schedule(this, delay,
					TimeUnit.SECONDS);
			String result = this.randomDrama();
			System.out.println(result);
			try {
				System.out.println("Sending to Twitter...");
				twitter.updateStatus(result);
				System.out.println("Done. Waiting " + delay
						+ "s for the next tweet");
			} catch (TwitterException e) {
				e.printStackTrace();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (StackOverflowError e) {
			System.err.println("No more drama available! Exiting!");
			System.exit(42);
		}
	}

	public void reload() throws IOException {
		System.out.println("Reloading Dictionary...");
		this.loadCombinaisons();
		this.loadBlackList();
		System.out.println("Config Reloaded");
	}

}
