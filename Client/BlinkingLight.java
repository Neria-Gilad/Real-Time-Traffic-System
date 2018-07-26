import java.awt.Color;

import javax.swing.JPanel;

class BlinkingLight extends Thread {
    enum State {OFF, ON}

    Ramzor ramzor;
    JPanel panel;
    private int timeout = 1 * 1000;
    private Event64 evTimeout = new Event64();

    public BlinkingLight(Ramzor ramzor, JPanel panel) {
        this.ramzor = ramzor;
        this.panel = panel;
        start();
    }

    //better to do with sleep to use less cpu but it has to be a state-chart...
    public void run() {
        setLight(1, Color.GRAY);
        State state = State.OFF;
        new Timer(timeout, evTimeout);
        while (true) {
            switch (state) {
                case OFF:
                    while (true) {
                        if (evTimeout.arrivedEvent()) {
                            evTimeout.waitEvent();
                            setLight(1, Color.YELLOW);
                            state = State.ON;
                            new Timer(timeout, evTimeout);
                            break;
                        } else yield();
                    }
                    break;
                case ON:
                    while (true) {
                        if (evTimeout.arrivedEvent()) {
                            evTimeout.waitEvent();
                            setLight(1, Color.GRAY);
                            state = State.OFF;
                            new Timer(timeout, evTimeout);
                            break;
                        } else yield();
                    }
                    break;
            }
        }
    }

    public void setLight(int place, Color color) {
        ramzor.colorLight[place - 1] = color;
        panel.repaint();
    }

}
