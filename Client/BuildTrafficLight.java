import javax.swing.JRadioButton;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BuildTrafficLight {
    static String HOST = "LOCALHOST";


    public static void main(String[] args) {
        final int numOfLights = 4 + 12 + 1;
        Ramzor ramzorim[] = new Ramzor[numOfLights];
        ramzorim[0] = new Ramzor(3, 40, 430, 110, 472, 110, 514, 110);
        ramzorim[1] = new Ramzor(3, 40, 450, 310, 450, 352, 450, 394);
        ramzorim[2] = new Ramzor(3, 40, 310, 630, 280, 605, 250, 580);
        ramzorim[3] = new Ramzor(3, 40, 350, 350, 308, 350, 266, 350);

        ramzorim[4] = new Ramzor(2, 20, 600, 18, 600, 40);
        ramzorim[5] = new Ramzor(2, 20, 600, 227, 600, 205);
        ramzorim[6] = new Ramzor(2, 20, 600, 255, 600, 277);
        ramzorim[7] = new Ramzor(2, 20, 600, 455, 600, 433);
        ramzorim[8] = new Ramzor(2, 20, 575, 475, 553, 475);
        ramzorim[9] = new Ramzor(2, 20, 140, 608, 150, 590);
        ramzorim[10] = new Ramzor(2, 20, 205, 475, 193, 490);
        ramzorim[11] = new Ramzor(2, 20, 230, 475, 250, 475);
        ramzorim[12] = new Ramzor(2, 20, 200, 453, 200, 433);
        ramzorim[13] = new Ramzor(2, 20, 200, 255, 200, 277);
        ramzorim[14] = new Ramzor(2, 20, 200, 227, 200, 205);
        ramzorim[15] = new Ramzor(2, 20, 200, 18, 200, 40);

        ramzorim[16] = new Ramzor(1, 30, 555, 645);

        TrafficLightFrame tlf = new TrafficLightFrame(" תשע''ט installation of traffic lights", ramzorim);

        // values in gX are red when X is green
        int lightMax = numOfLights - 1;


        Event64[] toGreen = new Event64[lightMax];
        Event64[] toRed = new Event64[lightMax];
        Event64[] isRed = new Event64[lightMax];

        Event64[] toShabat = new Event64[lightMax];
        Event64[] toChol = new Event64[lightMax];

        for (int i = 0; i < lightMax; i++) {
            toGreen[i] = new Event64();
            toRed[i] = new Event64();
            isRed[i] = new Event64();
            toShabat[i] = new Event64();
            toChol[i] = new Event64();
        }

        Event64 freeze = new Event64();
        Event64 unfreeze = new Event64();
        Event64 evGroupSetter = new Event64();

        Event64[] evDieded = new Event64[4];
        for (int i = 0; i < evDieded.length; i++) {
            evDieded[i] = new Event64();
        }

        ConcurrentLinkedQueue q[] = new ConcurrentLinkedQueue[4];
        for (int i = 0; i < 4; i++) {
            q[i] = new ConcurrentLinkedQueue();
        }

        int idx;
        for (idx = 0; idx < 4; idx++)
            new VehicleLight(ramzorim[idx], tlf.myPanel, idx, toGreen[idx], toRed[idx], toShabat[idx], toChol[idx], q[idx], evDieded[idx]);
        for (; idx < lightMax; idx++)
            new PedLight(ramzorim[idx], tlf.myPanel, toGreen[idx], toRed[idx], toShabat[idx], toChol[idx]);

        new BlinkingLight(ramzorim[lightMax], tlf.myPanel);

        Event64 SH = new Event64();
        Event64 CH = new Event64();
        Event64 evButt = new Event64();
        new Controller(numOfLights, evButt, evGroupSetter, CH, SH, freeze, unfreeze, toGreen, toRed, isRed, toShabat, toChol);


        ButtActionListener shabatListener = new ButtActionListener(CH, SH);
        ButtActionListener pedListener = new ButtActionListener(evButt);

        JRadioButton butt[] = new JRadioButton[13];

        for (int i = 0; i < butt.length - 1; i++) {
            butt[i] = new JRadioButton();
            butt[i].setName(Integer.toString(i + 4));
            butt[i].setOpaque(false);
            butt[i].addActionListener(pedListener);
            tlf.myPanel.add(butt[i]);
        }
        butt[0].setBounds(620, 30, 18, 18);
        butt[1].setBounds(620, 218, 18, 18);
        butt[2].setBounds(620, 267, 18, 18);
        butt[3].setBounds(620, 447, 18, 18);
        butt[4].setBounds(566, 495, 18, 18);
        butt[5].setBounds(162, 608, 18, 18);
        butt[6].setBounds(213, 495, 18, 18);
        butt[7].setBounds(240, 457, 18, 18);
        butt[8].setBounds(220, 443, 18, 18);
        butt[9].setBounds(220, 267, 18, 18);
        butt[10].setBounds(220, 218, 18, 18);
        butt[11].setBounds(220, 30, 18, 18);

        butt[12] = new JRadioButton();
        butt[12].setName(Integer.toString(16));
        butt[12].setBounds(50, 30, 55, 20);
        butt[12].setText("שבת");
        butt[12].setOpaque(false);
        butt[12].addActionListener(shabatListener);
        butt[12].setSelected(false);
        tlf.myPanel.add(butt[12]);


        /////SERVER-CLIENT ADDITIONS/////
        java.io.Console cnsl = System.console();
        String ip;
        if (cnsl != null) {
            System.out.println("Enter server IP address, or LOCALHOST");
            // Read a line from the user input. The cursor blinks after the specified input.
            ip = cnsl.readLine();
            if (!(ip.equals("LOCALHOST")||(ip.equals("X")))){
                HOST = ip;
            }
        }

        new ClientEx(HOST, CH, SH, freeze, unfreeze, evGroupSetter, q, evDieded, butt[12]);


    }
}
