package eu.thog92.dramagen.task;

import eu.thog92.dramagen.Dictionary;
import eu.thog92.dramagen.TasksManager;
import eu.thog92.dramagen.util.WritableArrayList;
import twitter4j.TwitterException;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TwitterTask extends ScheduledTask {


	private Dictionary dictionary;
	private Random rand;
	private int numTweets;
	private static final Pattern REGEX = Pattern.compile(Pattern.quote("[")
			+ "(.*?)" + Pattern.quote("]"));

	public TwitterTask(TasksManager manager) {
		super(manager);
		this.dictionary = Dictionary.getInstance();
		this.rand = new Random();
	}

	@Override
	public Boolean execute() {
		if(numTweets == 0 && !manager.getConfig().sendTweetOnStartup){
			System.out.println("Waiting " + delay + "s for the next tweet");
			numTweets++;
			return true;
		}

		try {
			String result = this.randomDrama();
			System.out.println(result);

			try {
				System.out.println("Sending to Twitter...");
				manager.getTwitter().updateStatus(result);
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
		if (dictionary.get("blacklist").contains(sentence))
			this.randomDrama();

		((WritableArrayList)dictionary.get("blacklist")).addAndWrite(sentence);

		// Don't capitalize first character if user has special name (like iLexiconn)
		if (!startWithUser && !Character.isUpperCase(sentence.charAt(1)))
			return sentence.substring(0, 1).toUpperCase() + sentence.substring(1);
		else
			return sentence;
	}

}
