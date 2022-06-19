package pacman;

import javax.swing.*;
import java.util.concurrent.TimeUnit;

public class PacMan extends JFrame {
    public PacMan(int xDim, int yDim, int gNum)
    {
        add(new Model(xDim, yDim, gNum));
    }

    public static void main(String[] args)
    {
        int xDim, yDim, gNum;
        boolean IfClicked = false;
        ButtonPanel panel = new ButtonPanel();
        panel.show();
        while (!IfClicked)
        {
            IfClicked = panel.getIfClickedValue();
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        xDim = panel.getxDim();
        yDim = panel.getyDim();
        gNum = panel.getgNum();
        System.out.println(xDim);
        System.out.println(yDim);
        System.out.println(gNum);
        PacMan pacman = new PacMan(xDim, yDim, gNum);
        pacman.setVisible(true);
        pacman.setTitle("Pacman Game");
        if(xDim < 12) pacman.setSize(24*12+15,24*yDim+65);
        else pacman.setSize(24*xDim+15,24*yDim+65);
        pacman.setDefaultCloseOperation(EXIT_ON_CLOSE);
        pacman.setLocationRelativeTo(null);
    }
}
