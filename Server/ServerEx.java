import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentLinkedQueue;

class ServerEx extends Thread {
    ConcurrentLinkedQueue<DialogEx> clientList = new ConcurrentLinkedQueue<>();
    ConcurrentLinkedQueue<String> channel = new ConcurrentLinkedQueue<>();

    int ctr = 0;

    int DEFAULT_PORT = 50000;
    ServerSocket listenSocket;
    Socket clientSockets;
    CmdEx cmd;
    private Event64
            evShabat = new Event64(),
            evChol = new Event64(),
            evFreeze = new Event64(),
            evUnFreeze = new Event64();

    public ServerEx() {
        cmd = new CmdEx(evShabat, evChol, evFreeze, evUnFreeze, channel);
        try {
            listenSocket = new ServerSocket(DEFAULT_PORT);
        } catch (IOException e) {
            System.out.println("Problem creating the server-socket");
            System.out.println(e.getMessage());
            System.exit(1);
        }

        System.out.println("Server starts on port " + DEFAULT_PORT);
        start();
    }

    private void listenEvents() {
        if (evShabat.arrivedEvent()) {
            evShabat.waitEvent();
            for (DialogEx d : clientList) {
                d.bufferSocketOut.println("shabat");
            }
        }
        if (evChol.arrivedEvent()) {
            evChol.waitEvent();
            for (DialogEx d : clientList) {
                d.bufferSocketOut.println("chol");
            }
        }
        if (evFreeze.arrivedEvent()) {
            evFreeze.waitEvent();
            for (DialogEx d : clientList) {
                d.bufferSocketOut.println("freeze");
            }
        }
        if (evUnFreeze.arrivedEvent()) {
            evUnFreeze.waitEvent();
            for (DialogEx d : clientList) {
                d.bufferSocketOut.println("unfreeze");
            }
        }
        while (!channel.isEmpty()) {
            String out = channel.poll();
            String splitted[] = out.split("\\s+");
            int id, car, junction;

            try {
                if (splitted[0].equals("route")) {
                    String junctionStr = Integer.toString(Integer.parseInt((splitted[1])));
                    for (DialogEx d : clientList) {
                        d.bufferSocketOut.println("route " + junctionStr);
                    }
                    continue;
                }


                id = Integer.parseInt(splitted[0]);
                car = Integer.parseInt(splitted[1]);
                junction = Integer.parseInt((splitted[2]));

                if (clientList.size() == 0) {
                    cmd.println("ERROR - NO CLIENTS AT ALL !", 81147);
                    continue;
                }
                if (id > clientList.size() || junction >= 4) {
                    throw new Exception();
                }
            } catch (Exception e) {
                cmd.println("ERROR - ITS TIME TO STOP !", 81147);
                continue;
            }
            boolean prev = false;
            boolean sent = false;
            DialogEx first = clientList.peek();
            System.out.println("create " + Integer.toString(car) + " at " + Integer.toString(junction));

            for (DialogEx d : clientList) {
                if (d.getDialogId() == id)
                    prev = true;
                else if (prev) {
                    sent = true;
                    d.bufferSocketOut.println("create " + Integer.toString(car) + " at " + Integer.toString(junction));
                    break;
                }
            }
            if (!sent) {
                first.bufferSocketOut.println("create " + Integer.toString(car) + " at " + Integer.toString(junction));
            }
        }
    }

    public void run() {
        System.out.println("Server started running");
        Thread t = new Thread(() -> {
            while (true) {
                listenEvents();
                yield();
            }
        });
        t.setDaemon(true);
        t.start();

        try {
            System.out.println("Server started waiting for clients");
            while (true) {
                clientSockets = listenSocket.accept();
                clientList.add(new DialogEx(clientSockets, this, ctr, channel));
                cmd.println("Client no. " + Integer.toString(ctr) + " is connected!", 1);
                System.out.println("Client no. " + Integer.toString(ctr) + " is connected!");

                ctr++;
            }
        } catch (IOException e) {
            System.exit(1);
        } finally {

        }
    }
}

