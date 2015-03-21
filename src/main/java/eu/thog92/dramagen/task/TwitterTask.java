package eu.thog92.dramagen.task;

import eu.thog92.dramagen.TasksManager;
import twitter4j.TwitterException;

public class TwitterTask extends ScheduledTask {

    private DramaTask dramaTask;
    private int numTweets;

    public TwitterTask(TasksManager manager, DramaTask dramaTask) {
        super(manager);
        this.dramaTask = dramaTask;
    }

    @Override
    public Boolean execute() {
        if (numTweets == 0 && !manager.getConfig().sendTweetOnStartup) {
            System.out.println("Waiting " + delay + "s for the next tweet");
            numTweets++;
            return true;
        }
        String result = dramaTask.execute();
		if(manager.getConfig().endOfSentense != null)
			result = result +  " " + manager.getConfig().endOfSentense;
        System.out.println(result);
        try {
            System.out.println("Sending to Twitter...");
            manager.getTwitter().updateStatus(result);
            System.out.println("Done. Waiting " + delay
                    + "s for the next tweet");
        } catch (StackOverflowError e) {
            System.err.println("No more drama available! Exiting!");
            System.exit(42);
        } catch (TwitterException e) {
            e.printStackTrace();
        }
        return true;
    }

}
