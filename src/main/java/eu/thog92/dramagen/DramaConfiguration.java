package eu.thog92.dramagen;


public class DramaConfiguration
{

    public int tweetDelay;
    public int httpPort;
    public String dramaCommand;
    public String commandPrefix;

    public DramaConfiguration()
    {

    }

    public DramaConfiguration(int delay, int httpPort)
    {
        this.tweetDelay = delay;
        this.httpPort = httpPort;
        this.commandPrefix = "!";
        this.dramaCommand = "drama!";
    }
}
