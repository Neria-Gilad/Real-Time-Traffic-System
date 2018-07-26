import java.awt.Color;

import javax.swing.JPanel;

class PedLight extends Thread {
    Event64 evToGreen, evToRed, evAtRed, evToShabat, evToChol;
    OutState outState;
    InState inState;
    Ramzor ramzor;
    JPanel panel;

    public PedLight(Ramzor ramzor, JPanel panel, Event64 evToGreen, Event64 evToRed, Event64 evToShabat, Event64 evToChol) {
        this.evToGreen = evToGreen;
        this.evToRed = evToRed;
        this.evToShabat = evToShabat;
        this.evToChol = evToChol;
        this.ramzor = ramzor;
        this.panel = panel;
        start();
    }

    public void run() {
        outState = OutState.ON_CHOL;
        inState = InState.ON_RED;

        while (true) {
            switch (outState) {
                case ON_CHOL:
                    inState = InState.ON_RED;
                    setToRed();
                    while (outState == OutState.ON_CHOL) {
                        switch (inState) {
                            case ON_RED:
                                while (true) {
                                    if (evToRed.arrivedEvent()) {
                                        evAtRed = (Event64) evToRed.waitEvent();
                                        evAtRed.sendEvent();
                                        break;
                                    } else if (evToGreen.arrivedEvent()) {
                                        evToGreen.waitEvent();
                                        setToGreen();
                                        inState = InState.ON_GREEN;
                                        break;
                                    } else if (evToShabat.arrivedEvent()) {
                                        evToShabat.waitEvent();
                                        setToOff();
                                        outState = OutState.ON_SHABAT;
                                        break;
                                    } else yield();
                                    break;
                                }
                                break;
                            case ON_GREEN:
                                while (true) {
                                    if (evToRed.arrivedEvent()) {
                                        evAtRed = (Event64) evToRed.waitEvent();
                                        setToRed();
                                        evAtRed.sendEvent();
                                        inState = InState.ON_RED;
                                        break;
                                    } else if (evToShabat.arrivedEvent()) {
                                        evToShabat.waitEvent();
                                        setToOff();
                                        outState = OutState.ON_SHABAT;
                                        break;
                                    } else yield();
                                }
                                break;
                        }
                    }
                    break;
                case ON_SHABAT:
                    evToChol.waitEvent();
                    outState = OutState.ON_CHOL;
                    setToRed();
                    inState = InState.ON_RED;
                    break;
            }
        }

    }

    private void setToOff() {
        setLight(1, Color.GRAY);
        setLight(2, Color.GRAY);
    }

    private void setToGreen() {
        setLight(1, Color.GRAY);
        setLight(2, Color.GREEN);
    }

    private void setToRed() {
        setLight(1, Color.RED);
        setLight(2, Color.GRAY);
    }

    public void setLight(int place, Color color) {
        ramzor.colorLight[place - 1] = color;
        panel.repaint();
    }

    enum InState {ON_RED, ON_GREEN}

    enum OutState {ON_CHOL, ON_SHABAT}
}
