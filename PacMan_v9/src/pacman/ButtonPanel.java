package pacman;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ButtonPanel extends JPanel implements ActionListener {
    private JFrame frame;
    private JPanel panel;
    private JLabel xlabel;
    private JLabel ylabel;
    private JLabel glabel;
    private JLabel errlabel;
    private JTextField xField; //pole na wymiar x
    private JTextField yField; //pole na wymiar y
    private JTextField gField; //pole na ilość duszków
    private JButton button;
    private boolean IfClicked = false;
    private boolean IfParsed = false;
    private String xDimTxt;
    private String yDimTxt;
    private String gNumTxt;
    private int xDim;
    private int yDim;
    private int gNum;
    public final int HEIGHT = 250;
    public final int WIDTH = 450;
    static public final int MINx = 4;
    static public final int MAXx = 64;
    static public final int MINy = 4;
    static public final int MAXy = 32;
    private final int MAX_GHOST = 100;
    private final int MIN_GHOST = 1;
    public ButtonPanel() {
        initialize();
    }
    public boolean getIfClickedValue() {
        return IfClicked;
    }
    public int getxDim() {
        return xDim;
    }
    public int getyDim() {
        return yDim;
    }
    public int getgNum() {
        return gNum;
    }
    private void makeAction() {
        String tmp;
        xDimTxt = xField.getText();
        yDimTxt = yField.getText();
        gNumTxt = gField.getText();

        try {
            xDim = Integer.parseInt(xDimTxt);
        } catch (NumberFormatException e) {
            errlabel.setText("X dimension field is empty or contains non-valid characters!");
            throw new RuntimeException(e);
        }

        try {
            yDim = Integer.parseInt(yDimTxt);
        } catch (NumberFormatException e) {
            errlabel.setText("Y dimension field is empty or contains non-valid characters!");
            throw new RuntimeException(e);
        }

        try {
            gNum = Integer.parseInt(gNumTxt);
        } catch (NumberFormatException e) {
            errlabel.setText("Ghosts number field is empty or contains non-valid characters!");
            throw new RuntimeException(e);
        }

        if (xDim <= MAXx && xDim >= MINx && yDim <= MAXy && yDim >= MINy && gNum <= MAX_GHOST && gNum >= MIN_GHOST) {
            IfClicked = true;
            frame.dispose();
        } else {
            tmp = "";
            if (xDim > MAXx) tmp += " X dimension is too high,";
            if (xDim < MINx) tmp += " X dimension is too low,";
            if (yDim > MAXy) tmp += " Y dimension is too high,";
            if (yDim < MINy) tmp += " Y dimension is too low,";
            if (gNum > MAX_GHOST) tmp += " too much ghosts,";
            if (gNum < MIN_GHOST) tmp += " not enough ghosts,";
            errlabel.setText(tmp);
        }

    }
    private void initialize() {
        frame = new JFrame();
        frame.setTitle("Dimensions setting panel");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(WIDTH, HEIGHT);
        frame.setLayout(new BorderLayout(10, 10));
        frame.setLocationRelativeTo(null);

        panel = new JPanel();
        xlabel = new JLabel("Enter X dimension (from " + MINx + " to " + MAXx + ")");
        xField = new JTextField(20);
        ylabel = new JLabel("Enter Y dimension (from " + MINy + " to " + MAXy + ")");
        yField = new JTextField(20);
        glabel = new JLabel("Enter number of ghosts (from " + MIN_GHOST + " to " + MAX_GHOST + ")");
        gField = new JTextField(20);
        button = new JButton("Set variables");
        errlabel = new JLabel("");

        button.addActionListener(this);

        panel.add(xlabel);
        panel.add(xField);
        panel.add(ylabel);
        panel.add(yField);
        panel.add(glabel);
        panel.add(gField);
        panel.add(button);
        panel.add(errlabel);
        frame.add(panel, BorderLayout.CENTER);

        xField.addKeyListener (new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                    if (key == KeyEvent.VK_ENTER) {
                    makeAction();
                    }
            }
        }
        );

        yField.addKeyListener (new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_ENTER) {
                    makeAction();
                }
            }
        }
        );

        gField.addKeyListener (new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_ENTER) {
                    makeAction();
                }
            }
        }
        );
    }

    public void show() {
        this.frame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        makeAction();
    }
}
