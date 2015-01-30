package eu.thog92.dramagen.task;

import eu.thog92.dramagen.Dictionary;
import eu.thog92.dramagen.util.WritableArrayList;

import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DramaTask implements ITask<String> {

	private static final Pattern REGEX = Pattern.compile(Pattern.quote("[")
			+ "(.*?)" + Pattern.quote("]"));
	private Dictionary dictionary;
	private Random rand;

	public DramaTask() {
		this.dictionary = Dictionary.getInstance();
		this.rand = new Random();
	}

	@Override
	public String execute() {
		return this.generateSentence(true);
	}

	public String generateSentence(boolean needToBeBlacklisted) {
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
			if (toReplace.equals("price")) {
				modifier = "\\$";
			}

			sentence = sentence.replaceFirst(toReplace,
					targetReplacementList.get(replacementID) + modifier);
		}

		// TODO: Use a Pattern
		sentence = sentence.replace("[", "").replace("]", "");
		if (dictionary.get("blacklist").contains(sentence)) {
			return execute();
		}

		if (needToBeBlacklisted) {
			((WritableArrayList) dictionary.get("blacklist")).addAndWrite(sentence);
		}

		// Don't capitalize first character if user has special name (like iLexiconn)
		if (!startWithUser && !Character.isUpperCase(sentence.charAt(1))) {
			return sentence.substring(0, 1).toUpperCase() + sentence.substring(1);
		} else {
			return sentence;
		}
	}
}
