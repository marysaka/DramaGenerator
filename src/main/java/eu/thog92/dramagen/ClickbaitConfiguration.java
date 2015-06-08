package eu.thog92.dramagen;

public class ClickbaitConfiguration
{
    public int tweetDelay;
    public String clickbaitCommand;
    public String commandPrefix;

    public ClickbaitConfiguration()
    {

    }

    public ClickbaitConfiguration(int delay)
    {
        this.tweetDelay = delay;
        this.commandPrefix = "!";
        this.clickbaitCommand = "clickbait!";
    }
}
