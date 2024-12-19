package client.components;

import javax.swing.*;
import java.awt.*;

public class MyPanel2 extends JPanel {
    private ImageIcon icon = new ImageIcon("img/drawColor.png");
    private Image imgMain = icon.getImage();

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(imgMain, 0, 0, getWidth(), getHeight(), null);
    }
}