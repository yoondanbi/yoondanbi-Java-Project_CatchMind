package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;

public class Brush extends JLabel {
    private static final int DEFAULT_BRUSH_SIZE = 15; // 브러시 기본 크기
    private static final int EXTRA_ERASER_SIZE = 25; // 지우개 추가 크기
    private static final int CANVAS_X_MIN = -10; // 캔버스 X 최소값
    private static final int CANVAS_X_MAX = 395; // 캔버스 X 최대값
    private static final int CANVAS_Y_MIN = -10; // 캔버스 Y 최소값
    private static final int CANVAS_Y_MAX = 310; // 캔버스 Y 최대값
    private int xCoordinate = -1; // 현재 X 좌표
    private int yCoordinate = -1; // 현재 Y 좌표
    private int prevXCoordinate = -1; // 이전 X 좌표
    private int prevYCoordinate = -1; // 이전 Y 좌표
    private Color currentColor = Color.BLACK;
    private boolean isDrawingEnabled = true;
    private boolean shouldClearCanvas = true;
    private final BufferedImage imageBuffer;
    private static final int TIMEOUT_MS = 50; // 0.05초 (50ms)
    private Timer timeoutTimer; // 타이머 추가

    public Brush(BufferedImage imageBuffer) {
        this.imageBuffer = imageBuffer; // 이미지 버퍼 초기화
        initializeTimeoutTimer(); // 타이머 초기화
    }

    @Override
    public void paint(Graphics g) {
        if (isDrawingEnabled) {
            drawBrush(g, currentColor, DEFAULT_BRUSH_SIZE);
        }

        if (shouldClearCanvas) {
            drawBrush(g, currentColor, DEFAULT_BRUSH_SIZE);
        } else {
            clearCanvas(g);
            shouldClearCanvas = true; // 캔버스 초기화 플래그 재설정
        }
    }

    private void drawBrush(Graphics g, Color color, int size) {
        // 좌표가 초기값(-1, -1)일 경우 그리지 않음
        if (xCoordinate == -1 || yCoordinate == -1) {
            return;
        }

        g.setColor(color);
        int brushSize = (color == Color.WHITE) ? size + EXTRA_ERASER_SIZE : size;
        double check = (color == Color.WHITE) ? 1.5 : 0;

        int xStart = xCoordinate - brushSize;
        int yStart = yCoordinate - brushSize;
        int xEnd = xCoordinate + brushSize;
        int yEnd = yCoordinate + brushSize;

        // 현재 좌표가 캔버스 범위 내에 있는 경우만 실행
        if (xStart > CANVAS_X_MIN - (check * 18) && xEnd < CANVAS_X_MAX &&
                yStart > CANVAS_Y_MIN - (check * 18) && yEnd < CANVAS_Y_MAX) {

            // 이전 좌표가 존재하는 경우, 선형 보간법으로 중간 점을 채운다
            if (prevXCoordinate != -1 && prevYCoordinate != -1) {
                double distance = Math.hypot(xCoordinate - prevXCoordinate, yCoordinate - prevYCoordinate);
                int steps = (int) distance; // 점의 간격을 촘촘히 만듦

                for (int i = 0; i <= steps; i++) {
                    double t = i / (double) steps;
                    int interpolatedX = (int) (prevXCoordinate * (1 - t) + xCoordinate * t);
                    int interpolatedY = (int) (prevYCoordinate * (1 - t) + yCoordinate * t);
                    if(interpolatedX==0 && interpolatedY==0) continue;
                    g.fillOval(interpolatedX, interpolatedY, brushSize, brushSize);
                }
            }
        }

        // 현재 좌표를 이전 좌표로 저장
        prevXCoordinate = xCoordinate;
        prevYCoordinate = yCoordinate;

        // 타이머 재시작
        timeoutTimer.restart();
    }

    private void clearCanvas(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(4, 4, 392, 307);

        resetCoordinates(); // 좌표 초기화
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
        if (shouldClear) {
            prevXCoordinate = -1; // 초기화
            prevYCoordinate = -1; // 초기화
        }
    }

    private void resetCoordinates() {
        prevXCoordinate = -1;
        prevYCoordinate = -1;
    }

    private void initializeTimeoutTimer() {
        timeoutTimer = new Timer(TIMEOUT_MS, e -> resetCoordinates());
        timeoutTimer.setRepeats(false); // 타이머는 한 번만 실행되도록 설정
    }
}
