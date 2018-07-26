import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

class DialogEx extends Thread {
    Socket client;
    ServerEx myServer;
    BufferedReader bufferSocketIn;
    PrintWriter bufferSocketOut;
    ConcurrentLinkedQueue<String> channel;
    private int did; //dialog id

    public DialogEx(Socket clientSocket, ServerEx myServer, int did, ConcurrentLinkedQueue<String> channel) {
        client = clientSocket;
        this.myServer = myServer;
        this.did = did;
        this.channel = channel;

        try {
            // Init streams to read/write text in this socket
            bufferSocketIn = new BufferedReader(
                    new InputStreamReader(
                            clientSocket.getInputStream()));
            bufferSocketOut = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    clientSocket.getOutputStream())), true);
        } catch (IOException e) {
            try {
                client.close();
            } catch (IOException e2) {
            }
            return;
        }
        start();
    }

    public int getDialogId() {
        return did;
    }

    public void run() {
        String line;
        try {
            while (true) {
                line = bufferSocketIn.readLine();
                if (line == null)
                    break;
                if (line.equals("end"))
                    break;
                line = line.trim();
                String splitted[] = line.split("\\s+");
                if (splitted.length >= 3 && splitted[1].equals("dieded")) {
                    channel.add(Integer.toString(getDialogId()) + " " + splitted[0] + " " + splitted[3]);
                }
            }
        } catch (IOException e) {
        } finally {
            try {
                client.close();
            } catch (IOException e2) {
            }
        }
    }

}
