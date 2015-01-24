package eu.thog92.dramagen.task;

import eu.thog92.dramagen.Dictionary;
import eu.thog92.dramagen.TasksManager;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DramaTask extends ScheduledTask {

	private Twitter twitter;
	private Dictionary dictionary;
	private Random rand;
	private static final Pattern REGEX = Pattern.compile(Pattern.quote("[")
			+ "(.*?)" + Pattern.quote("]"));

	public DramaTask(TasksManager manager) {
		super(manager);
		this.dictionary = Dictionary.getInstance();
		this.rand = new Random();
		this.twitter = new TwitterFactory().getInstance();
	}

	@Override
	public boolean execute() {
		try {
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
		return true;
	}

	public String randomDrama() throws IOException {
		List<String> sentences = dictionary.get("sentences");
		int sentenceID = this.rand.nextInt(sentences.size());
		String sentence = sentences.get(sentenceID);
		boolean startWithUser = sentence.startsWith("[people]");
		Matcher toReplaces = REGEX.matcher(sentence);
		while (toReplaces.find()) {
			String toReplace = toReplaces.group(1);
			List<String> targetReplacementList = dictionary.get(toReplace);
			int replacementID = rand.nextInt(targetReplacementList.size());
			String modifier = "";

			// TODO: Find a better way to handle $
			if (toReplace.equals("price"))
				modifier = "\\$";

			sentence = sentence.replaceFirst(toReplace,
					targetReplacementList.get(replacementID) + modifier);
		}

		// TODO: Use a Pattern
		sentence = sentence.replace("[", "").replace("]", "");
		if (manager.getBlackList().contains(sentence))
			this.randomDrama();

		manager.getBlackList().addAndWrite(sentence);

		// Don't capitalize first character if user has special name (like iLexiconn)
		if (!startWithUser && !Character.isUpperCase(sentence.charAt(1)))
			return sentence.substring(0, 1).toUpperCase() + sentence.substring(1);
		else
			return sentence;
	}

}
