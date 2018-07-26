import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;


public class CmdEx extends JFrame implements ActionListener, WindowListener, KeyListener {
    private static int num = 0;
    public JButton send;
    private Event64 evShabat, evChol, evFreeze, evUnFreeze;
    private JTextPane paneTextUp;
    private StyledDocument doc;
    private JTextArea textAreaDown;
    private Style base = StyleContext.getDefaultStyleContext().
            getStyle(StyleContext.DEFAULT_STYLE);
    private Style myStyle, redStyle, errorStyle;
    private ConcurrentLinkedQueue<String> channel;

    public CmdEx(Event64 evShabat, Event64 evChol, Event64 evFreeze, Event64 evUnFreeze, ConcurrentLinkedQueue<String> channel) {
        super("Server Command Line");
        this.evShabat = evShabat;
        this.evChol = evChol;
        this.evFreeze = evFreeze;
        this.evUnFreeze = evUnFreeze;
        this.channel = channel;

        addWindowListener(this);
        //there is only one instance, but i didn't write this code
        setLocation((num % 3) * 335 + 5, (num / 3) * 230 + 50);
        num++;

        paneTextUp = new JTextPane();
        paneTextUp.setEditable(false);

        doc = paneTextUp.getStyledDocument();

        myStyle = doc.addStyle("myStyle", base);
        StyleConstants.setFontSize(myStyle, 16);
        StyleConstants.setForeground(myStyle, Color.BLACK);

        redStyle = doc.addStyle("redStyle", base);
        StyleConstants.setFontSize(redStyle, 20);
        StyleConstants.setForeground(redStyle, Color.RED);

        errorStyle = doc.addStyle("errorStyle", base);
        StyleConstants.setFontSize(errorStyle, 14);
        StyleConstants.setForeground(errorStyle, Color.PINK);


        JScrollPane scrollPaneUp = new JScrollPane(paneTextUp);
        scrollPaneUp.setPreferredSize(new Dimension(400, 400));

        textAreaDown = new JTextArea(5, 25);
        textAreaDown.setEditable(true);
        textAreaDown.addKeyListener(this);
        JScrollPane scrollPaneDown = new JScrollPane(textAreaDown);

        send = new JButton("Send");
        send.addActionListener(this);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(send, BorderLayout.CENTER);


        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        mainPanel.add(scrollPaneUp);
        mainPanel.add(scrollPaneDown);
        mainPanel.add(buttonPanel);
        add(mainPanel);
        pack();
        setVisible(true);
    }

    public void println(String str, int altStyle) {
        try {
            if (altStyle == 81147)
                doc.insertString(doc.getLength(), str + '\n', errorStyle);
            else
                doc.insertString(doc.getLength(), str + '\n', redStyle);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        paneTextUp.setCaretPosition(paneTextUp.getDocument().getLength());
    }

    public void println(String str) {
        try {
            doc.insertString(doc.getLength(), str + '\n', myStyle);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        paneTextUp.setCaretPosition(paneTextUp.getDocument().getLength());
    }

    public void doOperation(String str) {
        String splitted[] = str.split("\\s+");
        if (str.equals("shabat"))
            evShabat.sendEvent();
        else if (str.equals("chol"))
            evChol.sendEvent();
        else if (str.equals("frz") || str.equals("freeze"))
            evFreeze.sendEvent();
        else if (str.equals("unfrz") || str.equals("unfreeze"))
            evUnFreeze.sendEvent();
        else if (str.equals("help") || str.equals("h"))
            println("acceptable commands:\n\tshabat\n\tchol\n\tfrz\n\tfreeze\n\tunfrz , unfreeze\n\thelp , h\n\tadd %n to %n\n\tcreate %n at %n\n\tset route %n\n\troute %n", 1);
        else if (splitted[0].equals("add") || splitted[0].equals("create"))
            channel.add("0 " + splitted[1] + " " + splitted[3]);
        else if ((splitted[0].equals("set") && splitted[1].equals("route")))
            channel.add("route " + splitted[2]);
        else if (splitted[0].equals("route"))
            channel.add("route " + splitted[1]);
        else
            channel.add("asd asd asd asd asd asd");
    }

    private void dataEntered() {
        String input = textAreaDown.getText().trim();
        println(input);
        doOperation(input);

        textAreaDown.setText("");
    }

    public void actionPerformed(ActionEvent arg0) {
        if (((JButton) arg0.getSource()).getText().equals("Close")) {
            this.dispose();
        }
        dataEntered();
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void keyPressed(KeyEvent key) {
        if (key.getKeyCode() == KeyEvent.VK_ENTER)
            dataEntered();
    }

    public void keyReleased(KeyEvent arg0) {
        // TODO Auto-generated method stub

    }

    public void keyTyped(KeyEvent arg0) {
        // TODO Auto-generated method stub

    }

}
