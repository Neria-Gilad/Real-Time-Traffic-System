import javax.swing.JPanel;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CarsMaker extends Thread {
    JPanel myPanel;
    int key;
    ConcurrentLinkedQueue<Integer> q;
    ConcurrentLinkedQueue<Event64> running = new ConcurrentLinkedQueue<>();
    Event64 evSomoneElsesProblem;
    private VehicleLight myRamzor;

    public CarsMaker(JPanel myPanel, VehicleLight myRamzor, int key, ConcurrentLinkedQueue<Integer> q, Event64 evSomoneElsesProblem) {
        this.myPanel = myPanel;
        this.myRamzor = myRamzor;
        this.key = key;
        this.q = q;
        this.evSomoneElsesProblem = evSomoneElsesProblem;
        setDaemon(true);
        start();
    }


    public void run() {
        try {
            while (true) {
                sleep(300);
                if (!myRamzor.isStop() && !q.isEmpty()) {
                    Event64 evCarDieded = new Event64();
                    running.offer(evCarDieded);
                    new CarMoovingEx(myPanel, myRamzor, key, evCarDieded, q.poll());
                }
                if (!(running.isEmpty()) && running.peek().arrivedEvent())
                    evSomoneElsesProblem.sendEvent(running.poll().waitEvent());//take out of queue and waitEvent
                yield();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
