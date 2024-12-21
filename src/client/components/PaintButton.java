package client.components;

import javax.swing.*;
import java.awt.*;

public class PaintButton extends JButton {
    private Image imgMain;

    // 생성자: 이미지 경로를 받아 바로 설정
    public PaintButton(ImageIcon icon) {
        setImageIcon(icon); // 초기 이미지 설정
        setBackground(Color.WHITE);
        setBorderPainted(false); // 버튼 테두리 제거
    }

    // 이미지를 설정하는 메서드
    public void setImageIcon(ImageIcon icon) {
        if (icon != null) {
            this.imgMain = icon.getImage();
            repaint(); // 이미지 변경 반영을 위해 다시 그리기
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (imgMain != null) {
            // 버튼 크기에 맞게 이미지를 그림
            g.drawImage(imgMain, 0, 10,40,40, this);
        }
    }
}
