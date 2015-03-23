package eu.thog92.dramagen.task;

import eu.thog92.dramagen.Dictionary;
import eu.thog92.dramagen.util.WritableArrayList;

import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DramaTask implements ITask<String>
{

    private static final Pattern INSERTION_REGEX = Pattern.compile(Pattern.quote("[")
            + "(.*?)" + Pattern.quote("]"));
    
    private static final Pattern RANGE_REGEX = Pattern.compile("[0-9]+\\.\\.\\.[0-9]+");
    private Dictionary dictionary;
    private Random rand;

    public DramaTask()
    {
        this.dictionary = Dictionary.getInstance();
        this.rand = new Random();
    }

    @Override
    public String execute()
    {
        return this.generateSentence(true);
    }

    public String generateSentence(boolean needToBeBlacklisted)
    {
        List<String> sentences = dictionary.get("sentences");
        int sentenceID = this.rand.nextInt(sentences.size());
        String sentence = sentences.get(sentenceID);
        boolean startWithUser = sentence.startsWith("[people]");
        sentence = replaceWords(sentence);

        if (sentence == null || sentence.isEmpty())
            return null;

        // TODO: Use a Pattern
        sentence = sentence.replace("[", "").replace("]", "");
        if (dictionary.get("blacklist").contains(sentence))
        {
            return execute();
        }

        if (needToBeBlacklisted)
        {
            ((WritableArrayList) dictionary.get("blacklist")).addAndWrite(sentence);
        }

        // Don't capitalize first character if user has special name (like iLexiconn)
        if (!startWithUser && !Character.isUpperCase(sentence.charAt(1)))
        {
            return sentence.substring(0, 1).toUpperCase() + sentence.substring(1);
        } else
        {
            return sentence;
        }
    }

    private String replaceWords(String sentence)
    {
        Matcher toReplaces = INSERTION_REGEX.matcher(sentence);
        while (toReplaces.find())
        {
            String toReplace = toReplaces.group(1);


            String modifier = "";

            // TODO: Find a better way to handle $
            if (toReplace.equals("price"))
            {
                modifier = "\\$";
            } else if (toReplace.startsWith("%rand"))
            {
                int pos = toReplace.indexOf("=");
                String input = toReplace.substring(pos + 1, toReplace.length());
                Matcher matcher = RANGE_REGEX.matcher(input);
                int value = 0;
                if (matcher.find())
                {
                    String[] bounds = input.split("\\.\\.\\.");
                    int lowerBound = Integer.parseInt(bounds[0]);
                    int upperBound = Integer.parseInt(bounds[1]);
                    while (value == 0)
                    {
                        value = rand.nextInt(upperBound - lowerBound) + lowerBound;
                    }
                } else
                {
                    while (value == 0)
                        value = rand.nextInt(Integer.valueOf(input));
                }

                sentence = sentence.replaceFirst(toReplace, String.valueOf(value));
                continue;
            } else if (toReplace.startsWith("%prime"))
            {
                int pos = toReplace.indexOf("=");
                String maxStr = toReplace.substring(pos + 1, toReplace.length());
                int maxRand = Integer.valueOf(maxStr);
                int num = 0;
                while (!checkForAllOneDigit(num))
                    num = rand.nextInt(maxRand);

                sentence = sentence.replaceFirst(toReplace, String.valueOf(num));
                continue;
            }


            List<String> targetReplacementList = dictionary.get(toReplace);

            if (targetReplacementList == null)
            {
                System.err.println(toReplace + "doesn't exist!");
                return null;
            }


            int replacementID = rand.nextInt(targetReplacementList.size());


            String replacement = targetReplacementList.get(replacementID);
            Matcher subReplaces = INSERTION_REGEX.matcher(replacement);

            if (subReplaces.find())
            {
                replacement = this.replaceWords(replacement);
            }

            sentence = sentence.replaceFirst(toReplace,
                    replacement + modifier);
        }
        return sentence;
    }

    public boolean checkForAllOneDigit(int value)
    {
        int manipulatedValue = value;
        int digit = manipulatedValue % 10;
        manipulatedValue = manipulatedValue / 10;
        while (manipulatedValue > 0)
        {
            if (manipulatedValue % 10 != digit)
            {
                return false;
            }
            manipulatedValue = manipulatedValue / 10;
        }
        return value != 0;
    }
}
