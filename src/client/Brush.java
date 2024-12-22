package client;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Brush extends JLabel {
    private static final int DEFAULT_BRUSH_SIZE = 15; // 브러시 기본 크기
    private static final int EXTRA_ERASER_SIZE = 25; // 지우개 추가 크기
    private static final int CANVAS_X_MIN = -10; // 캔버스 X 최소값
    private static final int CANVAS_X_MAX = 395; // 캔버스 X 최대값
    private static final int CANVAS_Y_MIN = -10; // 캔버스 Y 최소값
    private static final int CANVAS_Y_MAX = 310; // 캔버스 Y 최대값
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
        double check = (color == Color.WHITE) ? 1.5 : 0;
        int brushSize = (color == Color.WHITE) ? size + EXTRA_ERASER_SIZE : size;
        int xStart = xCoordinate - brushSize;
        int yStart = yCoordinate - brushSize;
        int xEnd = xCoordinate + brushSize;
        int yEnd = yCoordinate + brushSize;

        // 브러시 또는 지우기가 캔버스 범위 내에 있는 경우만 실행
        if (xStart > CANVAS_X_MIN-(check*18) && xEnd < CANVAS_X_MAX &&
                yStart > CANVAS_Y_MIN-(check*18) && yEnd < CANVAS_Y_MAX) {
            g.fillOval(xCoordinate, yCoordinate, brushSize , brushSize );
        }
    }


    private void clearCanvas(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(4, 4, 392, 307);
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
