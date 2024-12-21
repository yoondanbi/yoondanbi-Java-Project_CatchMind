package client;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Brush extends JLabel {
    private static final int DEFAULT_BRUSH_SIZE = 15; // 브러시 기본 크기
    private static final int CLEAR_RECT_SIZE = 1000; // 캔버스 초기화 크기
    private int xCoordinate;
    private int yCoordinate;
    private Color currentColor = Color.BLACK;
    private boolean isDrawingEnabled = true;
    private boolean shouldClearCanvas = true;
    private final BufferedImage imageBuffer;

    public Brush(BufferedImage imageBuffer) {
        this.imageBuffer = imageBuffer; // 이미지 버퍼 초기화
    }
    @Override
    public void paint(Graphics g) {
        if (isDrawingEnabled) {
            drawBrush(g, currentColor, DEFAULT_BRUSH_SIZE);
        } else {
            drawBrush(g, Color.BLACK, 0); // 그리지 않을 때
        }

        if (shouldClearCanvas) {
            drawBrush(g, currentColor, DEFAULT_BRUSH_SIZE);
        } else {
            clearCanvas(g);
            shouldClearCanvas = true; // 캔버스 초기화 플래그 재설정
        }
    }

    private void drawBrush(Graphics g, Color color, int size) {
        g.setColor(color);
        if(color==Color.white){
            g.fillOval(xCoordinate - size, yCoordinate - size, size+28, size+28);
        }else {
            g.fillOval(xCoordinate - size, yCoordinate - size, size, size);
        }
    }

    private void clearCanvas(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, CLEAR_RECT_SIZE, CLEAR_RECT_SIZE);
    }

    public void setX(int x) {
        this.xCoordinate = x;
    }

    public void setY(int y) {
        this.yCoordinate = y;
    }
    public void setColor(Color color) {
        this.currentColor = color;
    }

    public void setDrawingEnabled(boolean isEnabled) {
        this.isDrawingEnabled = isEnabled;
    }

    public void setShouldClearCanvas(boolean shouldClear) {
        this.shouldClearCanvas = shouldClear;
    }
}
