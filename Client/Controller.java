public class Controller extends Thread {
    final private int[] g0 = {4, 5, 8, 11, 14, 15};
    final private int[] g1 = {8, 11, 14, 15};
    final private int[] g2 = {9, 10};
    final private int[] g3 = {6, 7, 12, 13};
    final private int[][] groups = {g0, g1, g2, g3};
    final private int timeout = 3 * 1000;

    private int group = 0;
    private int lightMax;
    private Event64 evButt;
    private Event64 evToChol;
    private Event64 evToShabat;
    private Event64 evFreeze;
    private Event64 evUnFreeze;
    private Event64 evGoupSetter;
    private ReallyOutState reallyOutState;
    private OutState outState;
    private InState inState;
    private Event64[] evRamzorToGreen;
    private Event64[] evRamzorToRed;
    private Event64[] evRamzorIsRed;
    private Event64[] evRamzorToShabat;
    private Event64[] evRamzorToChol;
    private Event64 evTimer;

    public Controller(int lightMax, Event64 evButt,Event64 evGoupSetter, Event64 evToChol, Event64 evToShabat, Event64 evFreeze, Event64 evUnFreeze, Event64[] evRamzorToGreen,
                      Event64[] evRamzorToRed, Event64[] evRamzorIsRed, Event64[] evRamzorToShabat, Event64[] evRamzorToChol) {
        this.lightMax = lightMax - 1;
        this.evButt = evButt;
        this.evGoupSetter = evGoupSetter;
        this.evToChol = evToChol;
        this.evToShabat = evToShabat;
        this.evFreeze = evFreeze;
        this.evUnFreeze = evUnFreeze;
        this.evRamzorToGreen = evRamzorToGreen;
        this.evRamzorToRed = evRamzorToRed;
        this.evRamzorIsRed = evRamzorIsRed;
        this.evRamzorToShabat = evRamzorToShabat;
        this.evRamzorToChol = evRamzorToChol;
        start();
    }

    public void run() {
        Event64 allIsRed = new Event64();
        reallyOutState = ReallyOutState.DEFROSTED;
        outState = OutState.ON_CHOL;
        inState = InState.Init;
        evTimer = new Event64();
        setAllRed(allIsRed);

        while (true) {
            switch (reallyOutState) {
                case DEFROSTED:
                    switch (outState) {
                        case ON_CHOL:
                            switch (inState) {
                                case Init: //just make sure everything is red, then start the cycle

                                    while (true) {
                                        if (evToShabat.arrivedEvent()) {
                                            evToShabat.waitEvent();
                                            outState = OutState.ON_SHABAT;
                                            throwStonesAtKoifrim();
                                            break;
                                        } else if (allIsRed.arrivedEvent()) {
                                            allIsRed.waitEvent();
                                            group = 0;
                                            setGroupTurn(group);
                                            inState = InState.Green;
                                            evTimer = new Event64();
                                            new Timer(timeout, evTimer);
                                            break;
                                        } else yield();
                                    }
                                    break;
                                case Green:
                                    while (true) {
                                        if (evFreeze.arrivedEvent()) {
                                            evFreeze.waitEvent();
                                            reallyOutState = ReallyOutState.FROZEN;
                                            break;
                                        } else if (evToShabat.arrivedEvent()) {
                                            evToShabat.waitEvent();
                                            outState = OutState.ON_SHABAT;
                                            throwStonesAtKoifrim();
                                            break;
                                        } else if (evTimer.arrivedEvent()) {
                                            evTimer.waitEvent();
                                            allIsRed = new Event64();
                                            setAllRed(allIsRed);
                                            inState = InState.ToRed;
                                            break;
                                        } else yield();
                                    }
                                    break;
                                case ToRed:
                                    while (true) {
                                        if (evFreeze.arrivedEvent()) {
                                            evFreeze.waitEvent();
                                            reallyOutState = ReallyOutState.FROZEN;
                                            break;
                                        } else if (evToShabat.arrivedEvent()) {
                                            evToShabat.waitEvent();
                                            outState = OutState.ON_SHABAT;
                                            throwStonesAtKoifrim();
                                            break;
                                        } else if (allIsRed.arrivedEvent()) {
                                            allIsRed.waitEvent();
                                            inState = InState.IsRed;
                                            break;
                                        } else yield();
                                    }
                                    break;
                                case IsRed:
                                    if (evFreeze.arrivedEvent()) {
                                        evFreeze.waitEvent();
                                        reallyOutState = ReallyOutState.FROZEN;
                                        break;
                                    }else if (evGoupSetter.arrivedEvent()){
                                        group = (int)evGoupSetter.waitEvent();
                                        setGroupTurn(group);
                                        inState = InState.Green;
                                        evTimer = new Event64();
                                        new Timer(timeout, evTimer);
                                    } else if (evToShabat.arrivedEvent()) {
                                        evToShabat.waitEvent();
                                        outState = OutState.ON_SHABAT;
                                        throwStonesAtKoifrim();//SHABBES!
                                        break;
                                    } else if (evButt.arrivedEvent()) {
                                        int pressedButt = (int) evButt.waitEvent();
                                        group = nearestGroup(group, pressedButt);
                                        setGroupTurn(group);
                                        inState = InState.Green;
                                        evTimer = new Event64();
                                        new Timer(timeout, evTimer);
                                    } else {
                                        group = (group + 1) % 4;//next group
                                        setGroupTurn(group);
                                        inState = InState.Green;
                                        evTimer = new Event64();
                                        new Timer(timeout, evTimer);
                                    }
                                    break;
                            }
                            break;
                        case ON_SHABAT:
                            while (true) {
                                if (evFreeze.arrivedEvent()) {
                                    evFreeze.waitEvent();
                                    reallyOutState = ReallyOutState.FROZEN;
                                    break;
                                } else if (evToChol.arrivedEvent()) {
                                    evToChol.waitEvent();
                                    inState = InState.Init;
                                    outState = OutState.ON_CHOL;
                                    stopThrowStonesAtKoifrim();
                                    allIsRed = new Event64();
                                    setAllRed(allIsRed);
                                    break;
                                } else yield();
                            }
                            break;
                    }
                    break;
                case FROZEN:
                    while (true) {
                        if (evUnFreeze.arrivedEvent()) {
                            evUnFreeze.waitEvent();
                            reallyOutState = ReallyOutState.DEFROSTED;
                            break;
                        } else yield();
                    }
                    break;
            }
        }
    }

    private boolean isButtonNotInGroup(int button, int group) {
        for (int g : groups[group])
            if (g == button) return false; //if button in group, it cant be green with the vehicle light
        return true;
    }

    private void throwStonesAtKoifrim() {//shabbes
        for (int i = 0; i < lightMax; i++) {
            evRamzorToShabat[i].sendEvent();
        }
    }

    private void stopThrowStonesAtKoifrim() {//motzash
        for (int i = 0; i < lightMax; i++) {
            evRamzorToChol[i].sendEvent();
        }
    }

    private void setAllRed(Event64 evAllRed) {
        Thread t = new Thread(() -> {
            for (int i = 0; i < 4; i++) {
                evRamzorToRed[i].sendEvent(evRamzorIsRed[i]);
            }
            try {
                sleep(2000); ///it takes some time for vehicle light so give peds some time
            } catch (InterruptedException e) {
            }
            for (int i = 4; i < lightMax; i++) {
                evRamzorToRed[i].sendEvent(evRamzorIsRed[i]);
            }
            for (int i = 0; i < lightMax; i++) {
                evRamzorIsRed[i].waitEvent();
            }
            try {
                sleep(1000);
            } catch (InterruptedException e) {
            }
            evAllRed.sendEvent();
        });
        t.setDaemon(true);
        t.start();
    }

    private int nearestGroup(int group, int pressedButt) {
        int nextGroup = 0;
        for (int i = 1; i < 5; i++) { //start with one so we wont repeat the current group
            nextGroup = (group + i) % 4;
            if (isButtonNotInGroup(pressedButt, nextGroup))//if can be green with group
                break;
        }
        return nextGroup;
    }

    private void setGroupTurn(int gNumber) {//everyone who can be green should be green. the rest should be red
        int gindex = 0;
        evRamzorToGreen[gNumber].sendEvent();
        for (int i = 4; i < lightMax; i++) {
            if (gindex >= groups[gNumber].length || i != groups[gNumber][gindex]) {
                evRamzorToGreen[i].sendEvent();
            } else {
                gindex++;
            }
            //red, but all lights are red when func is called, so...
        }
    }


    enum InState {
        Init,
        Green,
        ToRed,
        IsRed,
    }

    enum OutState {ON_CHOL, ON_SHABAT}

    enum ReallyOutState {FROZEN, DEFROSTED}


}
