package client;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Brush extends JLabel {
    private int xx, yy;
    private Color color = Color.BLACK;
    private boolean drawPen = true;
    private boolean clearC = true;
    private BufferedImage imgBuff;

    public Brush(BufferedImage imgBuff) {
        this.imgBuff = imgBuff; // 이미지 버퍼를 전달
    }

    @Override
    public void paint(Graphics g) {
        if (drawPen) {
            g.setColor(color);
            g.fillOval(xx - 10, yy - 10, 10, 10);
        } else {
            g.setColor(Color.WHITE);
            g.fillOval(0, 0, 0, 0);
        }
        if (clearC) {
            g.setColor(color);
            g.fillOval(xx - 10, yy - 10, 10, 10);
        } else {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, 1000, 1000);
            clearC = true;
        }
    }

    public void setX(int x) { this.xx = x; }
    public void setY(int y) { this.yy = y; }
    public void setColor(Color color) { this.color = color; }
    public void setDrawPen(boolean drawPen) { this.drawPen = drawPen; }
    public void setClearC(boolean clearC) { this.clearC = clearC; }
}
