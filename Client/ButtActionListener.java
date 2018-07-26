import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JRadioButton;

public class ButtActionListener implements ActionListener {
    private Event64 buttonPress;
    private Event64 buttonPress2;//shabat

    public ButtActionListener(Event64 buttonPress) {
        this.buttonPress = buttonPress;
        this.buttonPress2 = buttonPress; //just in case because why not
    }

    public ButtActionListener(Event64 buttonPress, Event64 buttonPress2) {
        this.buttonPress = buttonPress;
        this.buttonPress2 = buttonPress2;
    }

    public void actionPerformed(ActionEvent e) {
        JRadioButton butt = (JRadioButton) e.getSource();
        System.out.println(butt.getName());
        if ((butt.getText().equals("שבת"))) {
            boolean flg = butt.isSelected();
            butt.setSelected(flg);
            if (flg)
                buttonPress2.sendEvent();
            else
                buttonPress.sendEvent();
        } else {
            butt.setSelected(false);
            buttonPress.sendEvent(Integer.parseInt(butt.getName()));
        }
    }

}
