package SwingContent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class InitialPrompt extends JPanel implements ActionListener {
    private JButton button;
    private JComboBox<String> options;
    private SpinnerModel minLights;
    private SpinnerModel maxLights;
    private JSpinner minSpinner;
    private JSpinner maxSpinner;
    private JPanel minBox;
    private JPanel maxBox;

    private JFrame frame;
    private JPanel panel;
    private String[] layouts = {"Load HH118 House from the CASAS datasets",
                                "Play CASAS HH118",
                                "Simulate dataset HH107",
                                };

    public static void main(String[] args) {
        new InitialPrompt();
    }

    public InitialPrompt(){

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                initialize();

                panel = new JPanel();
                BoxLayout box = new BoxLayout(panel, BoxLayout.Y_AXIS);
                panel.setLayout(box);
                panel.add(options);
                panel.add(minBox);
                panel.add(maxBox);
                panel.add(button);
                panel.setPreferredSize(new Dimension(400, 300));

                frame = new JFrame("IoT Simulator");
                frame.setPreferredSize(new Dimension(400, 300));
                frame.setContentPane(panel);
                frame.pack();
                frame.setVisible(true);
            }
        });
    }

    public void initialize(){
        options = new JComboBox(layouts);
        options.setSelectedIndex(0);
        options.setAlignmentX(Component.CENTER_ALIGNMENT);

        button = new JButton();
        button.setText("submit");
        button.addActionListener(this);
        button.setPreferredSize(new Dimension(80, 30));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        minLights = new SpinnerNumberModel(0, 0, 98, 1);
        minSpinner = new JSpinner(minLights);
        minBox = new JPanel();
        minBox.add(new JLabel("Minimum Number of Lights"));
        minBox.add(minSpinner);
        minBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        maxLights = new SpinnerNumberModel(1, 1, 99, 1);
        maxSpinner = new JSpinner(maxLights);
        maxBox = new JPanel();
        maxBox.add(new JLabel("Maximum Number of Lights"));
        maxBox.add(maxSpinner);
        maxBox.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        frame.setVisible(false);
        new Simulator(options.getSelectedIndex(), (int)  minLights.getValue(), (int) maxLights.getValue());
    }
}
