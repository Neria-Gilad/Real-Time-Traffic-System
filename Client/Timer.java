public class Timer extends Thread
{
    private final long time;
    private final Event64 evTime;

    public Timer(long time,Event64 evTime)
    {
        this.time=time;
        this.evTime=evTime;
        setDaemon(true);
        start();
    }

    public void run()
    {
        try
        {
            sleep(time);
        } catch (InterruptedException ex) {}
        evTime.sendEvent();
    }

}
