import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.lang.Thread.yield;

class ClientEx {

    private String SERVERHOST;

    private int DEFAULT_PORT = 50000;
    private Socket clientSocket = null;
    private BufferedReader bufferSocketIn;
    private PrintWriter bufferSocketOut;
    private String line;

    private Event64 CH, SH, freeze, unfreeze, evGroupSetter;
    private ConcurrentLinkedQueue<Integer>[] q;
    private Event64[] evDieded;
    private JRadioButton shabatButt;

    public ClientEx(String SERVERHOST, Event64 CH, Event64 SH, Event64 freeze, Event64 unfreeze, Event64 evGroupSetter, ConcurrentLinkedQueue<Integer>[] q, Event64[] evDieded, JRadioButton shabatButt) {
        this.SERVERHOST = SERVERHOST;
        this.CH = CH;
        this.SH = SH;
        this.freeze = freeze;
        this.unfreeze = unfreeze;
        this.evGroupSetter = evGroupSetter;
        this.q = q;
        this.evDieded = evDieded;
        this.shabatButt = shabatButt;
        doit();
    }

    private void somethingHappened(String data) {
        String[] splited = data.split("\\s+");
        boolean flg;
        switch (splited[0]) {
            case "create":
                q[Integer.parseInt(splited[3])].offer(Integer.parseInt(splited[1])); //add specified car to relevant queue
                break;
            case "shabat":
                SH.sendEvent();
                shabatButt.setSelected(true);
                break;
            case "chol":
                shabatButt.setSelected(false);
                CH.sendEvent();
                break;
            case "freeze":
                freeze.sendEvent();
                break;
            case "unfreeze":
                unfreeze.sendEvent();
                break;
            case "route":
                evGroupSetter.sendEvent(Integer.parseInt(splited[1]));
                break;
        }
    }

    public void setServerHost(String str) {
        SERVERHOST = str;
    }

    public void doit() {
        try {
            // request to server
            clientSocket = new Socket(SERVERHOST, DEFAULT_PORT);

            // Init streams to read/write text in this socket
            bufferSocketIn = new BufferedReader(
                    new InputStreamReader(
                            clientSocket.getInputStream()));
            bufferSocketOut = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    clientSocket.getOutputStream())), true);

            //instantiate event listener
            Thread t = new Thread(() -> {

                while (true) {
                    for (int i = 0; i < evDieded.length; i++) {
                        if (evDieded[i].arrivedEvent()) {
                            int who = (int) evDieded[i].waitEvent();
                            bufferSocketOut.println(Integer.toString(who) + " dieded at " + Integer.toString(i));
                        }
                    }
                    yield();
                }
            });
            t.setDaemon(true);
            t.start();

            // notice about the connection
            while (true) {
                line = bufferSocketIn.readLine(); // reads a line from the server

                // connection is closed ?  exit
                if (line == null)
                    break;
                else if (line.equals("end"))
                    break;

                somethingHappened(line);
                yield();
            }
        } catch (IOException e) {
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e2) {
            }
        }
    }


}
