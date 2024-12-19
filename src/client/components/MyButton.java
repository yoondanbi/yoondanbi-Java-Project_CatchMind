package client.components;

import javax.swing.*;
import java.awt.*;

public class MyButton extends JButton {
    private ImageIcon icon = new ImageIcon("img/drawEraser.png");
    private Image imgMain = icon.getImage();

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(imgMain, 0, 0, getWidth(), getHeight(), null);
        setBorderPainted(false); // 버튼 테두리 제거
    }
}