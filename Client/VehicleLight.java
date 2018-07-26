import java.awt.Color;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.swing.JPanel;


public class VehicleLight extends Thread {
    private final int timeout = 1 * 1000;
    private Event64 evToGreen, evToRed, evAtRed, evToShabat, evToChol;
    private OutState outState;
    private InState inState;
    private InInnerState inInnerState;
    private Ramzor ramzor;
    private JPanel panel;
    private boolean turningRed = false;
    private boolean stop = true;
    private Event64 evTimeout = new Event64();

    public VehicleLight(Ramzor ramzor, JPanel panel, int key, Event64 evToGreen, Event64 evToRed, Event64 evToShabat, Event64 evToChol, ConcurrentLinkedQueue q, Event64 EvDieded) {
        this.evToGreen = evToGreen;
        this.evToRed = evToRed;
        this.evToShabat = evToShabat;
        this.evToChol = evToChol;
        this.ramzor = ramzor;
        this.panel = panel;


        new CarsMaker(panel, this, key, q, EvDieded);
        start();
    }

    private Event64 resetTimer(int time) {
        Event64 toReset = new Event64();
        new Timer(time, toReset);
        return toReset;
    }

    public void run() {
        outState = OutState.ON_CHOL;
        inState = InState.ON_RED;
        inInnerState = InInnerState.IS_ON;
        int count = 0;
        while (true) {
            switch (outState) {
                case ON_CHOL:
                    while (outState == OutState.ON_CHOL) {
                        switch (inState) {
                            case ON_GREEN:
                                while (true) {
                                    if (evToRed.arrivedEvent()) {
                                        evAtRed = (Event64) evToRed.waitEvent();
                                        turningRed = true;
                                        count = 0;
                                        evTimeout = resetTimer(500);
                                        inInnerState = InInnerState.IS_ON;
                                        inState = InState.BLINKING_GREEN;
                                        break;
                                    } else if (evToShabat.arrivedEvent()) {
                                        evToShabat.waitEvent();
                                        outState = OutState.ON_SHABAT;
                                        evTimeout = resetTimer(500);
                                        setToOff();
                                        inInnerState = InInnerState.IS_OFF;
                                        break;
                                    } else yield();
                                }
                                break;
                            case BLINKING_GREEN:
                                while (true) {
                                    if (evToShabat.arrivedEvent()) {
                                        evToShabat.waitEvent();
                                        outState = OutState.ON_SHABAT;
                                        evTimeout = resetTimer(500);
                                        setToOff();
                                        inInnerState = InInnerState.IS_OFF;
                                        break;
                                    } else if (count >= 3) {
                                        evTimeout.waitEvent();
                                        setToYellow();
                                        inState = InState.ON_YELLOW;
                                        evTimeout = resetTimer(1000);
                                        break;
                                    } else {
                                        switch (inInnerState) {
                                            case IS_ON:
                                                while (true) {
                                                    if (evToShabat.arrivedEvent()) {
                                                        evToShabat.waitEvent();
                                                        outState = OutState.ON_SHABAT;
                                                        evTimeout = resetTimer(500);
                                                        setToOff();
                                                        inInnerState = InInnerState.IS_OFF;
                                                        break;
                                                    } else if (evTimeout.arrivedEvent()) {
                                                        evTimeout.waitEvent();
                                                        setToOff();
                                                        inInnerState = InInnerState.IS_OFF;
                                                        evTimeout = resetTimer(500);
                                                        break;
                                                    } else yield();
                                                }
                                                break;
                                            case IS_OFF:
                                                while (true) {
                                                    if (evToShabat.arrivedEvent()) {
                                                        evToShabat.waitEvent();
                                                        outState = OutState.ON_SHABAT;
                                                        evTimeout = resetTimer(500);
                                                        setToOff();
                                                        inInnerState = InInnerState.IS_OFF;
                                                        break;
                                                    } else if (evTimeout.arrivedEvent()) {
                                                        evTimeout.waitEvent();
                                                        setToGreen();
                                                        inInnerState = InInnerState.IS_ON;
                                                        count++;
                                                        evTimeout = resetTimer(500);
                                                        break;//while
                                                    } else yield();
                                                }
                                                break;//switch
                                        }
                                        break;//while
                                    }
                                }
                                break;//switch
                            case ON_YELLOW:
                                while (true) {
                                    if (evToShabat.arrivedEvent()) {
                                        evToShabat.waitEvent();
                                        outState = OutState.ON_SHABAT;
                                        evTimeout = resetTimer(500);
                                        setToOff();
                                        inInnerState = InInnerState.IS_OFF;
                                        break;
                                    } else if (evTimeout.arrivedEvent()) {
                                        evTimeout.waitEvent();
                                        setToRed();
                                        stop = true;
                                        evAtRed.sendEvent();
                                        turningRed = false;
                                        inState = InState.ON_RED;
                                        break;
                                    } else yield();
                                }
                                break;
                            case ON_RED:
                                while (true) {
                                    if (evToRed.arrivedEvent()) {
                                        // in case the light is already red, but got an event to switch to red
                                        evAtRed = (Event64) evToRed.waitEvent();
                                        evAtRed.sendEvent();
                                    } else if (evToGreen.arrivedEvent()) {
                                        evToGreen.waitEvent();
                                        setToOrange();
                                        inState = InState.ON_ORANGE;
                                        evTimeout = resetTimer(timeout);
                                        break;
                                    } else if (evToShabat.arrivedEvent()) {
                                        evToShabat.waitEvent();
                                        outState = OutState.ON_SHABAT;
                                        evTimeout = resetTimer(500);
                                        setToOff();
                                        inInnerState = InInnerState.IS_OFF;
                                        break;
                                    } else yield();
                                }
                                break;
                            case ON_ORANGE:
                                if (evTimeout.arrivedEvent()) {
                                    evTimeout.waitEvent();
                                    setToGreen();
                                    inState = InState.ON_GREEN;
                                    stop = false;
                                    break;
                                } else if (evToShabat.arrivedEvent()) {
                                    evToShabat.waitEvent();
                                    outState = OutState.ON_SHABAT;
                                    evTimeout = resetTimer(500);
                                    setToOff();
                                    inInnerState = InInnerState.IS_OFF;
                                    break;
                                } else yield();
                                break;
                        }
                        break;
                    }
                    break;
                case ON_SHABAT:
                    //evTimeout = resetTimer(500);
                    //setToOff();
                    //inInnerState = InInnerState.IS_OFF;
                    while (outState == OutState.ON_SHABAT) {
                        if (evToChol.arrivedEvent()) {
                            evToChol.waitEvent();
                            outState = OutState.ON_CHOL;
                            setToRed();
                            turningRed = false;
                            inState = InState.ON_RED;
                            break;
                        } else {
                            switch (inInnerState) {
                                case IS_ON:
                                    while (true) {
                                        if (evToChol.arrivedEvent()) {
                                            evToChol.waitEvent();
                                            outState = OutState.ON_CHOL;
                                            setToRed();
                                            if (turningRed)
                                                evAtRed.sendEvent();
                                            turningRed = false;
                                            inState = InState.ON_RED;
                                            break;
                                        } else if (evTimeout.arrivedEvent()) {
                                            evTimeout.waitEvent();
                                            setToOff();
                                            inInnerState = InInnerState.IS_OFF;
                                            evTimeout = resetTimer(500);
                                            break;
                                        } else yield();
                                    }
                                    break;
                                case IS_OFF:
                                    while (true) {
                                        if (evToChol.arrivedEvent()) {
                                            evToChol.waitEvent();
                                            outState = OutState.ON_CHOL;
                                            setToRed();
                                            if (turningRed)
                                                evAtRed.sendEvent();
                                            turningRed = false;
                                            inState = InState.ON_RED;
                                            break;
                                        } else if (evTimeout.arrivedEvent()) {
                                            evTimeout.waitEvent();
                                            setToYellow();
                                            inInnerState = InInnerState.IS_ON;
                                            evTimeout = resetTimer(500);
                                            break;//while
                                        } else yield();
                                    }
                                    break;//switch
                            }
                        }
                        break;
                    }
            }
        }
    }

    private void setLight(int place, Color color) {
        ramzor.colorLight[place - 1] = color;
        panel.repaint();
    }

    public boolean isStop() {
        return stop;
    }

    private void setToOff() {
        setLight(1, Color.LIGHT_GRAY);
        setLight(2, Color.LIGHT_GRAY);
        setLight(3, Color.LIGHT_GRAY);
    }

    private void setToOrange() {
        setLight(1, Color.RED);
        setLight(2, Color.YELLOW);
        setLight(3, Color.LIGHT_GRAY);
    }

    private void setToRed() {
        setLight(1, Color.RED);
        setLight(2, Color.LIGHT_GRAY);
        setLight(3, Color.LIGHT_GRAY);
    }

    private void setToGreen() {
        setLight(1, Color.LIGHT_GRAY);
        setLight(2, Color.LIGHT_GRAY);
        setLight(3, Color.GREEN);
    }

    private void setToYellow() {
        setLight(1, Color.LIGHT_GRAY);
        setLight(2, Color.YELLOW);
        setLight(3, Color.LIGHT_GRAY);
    }

    enum InState {ON_RED, ON_ORANGE, ON_GREEN, BLINKING_GREEN, ON_YELLOW}

    enum OutState {ON_CHOL, ON_SHABAT}

    enum InInnerState {IS_ON, IS_OFF}


}

