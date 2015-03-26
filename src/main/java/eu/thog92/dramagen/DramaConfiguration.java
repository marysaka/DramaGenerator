package eu.thog92.dramagen;


public class DramaConfiguration
{

    public int tweetDelay;
    public int httpPort;

    public DramaConfiguration()
    {

    }

    public DramaConfiguration(int delay, int httpPort)
    {
        this.tweetDelay = delay;
        this.httpPort = httpPort;
    }
}
