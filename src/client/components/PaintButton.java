package client.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PaintButton extends JButton {
    private Image imgMain;

    // 색상 정의
    private Color hoverColor = new Color(255, 182, 193); // 핑크색 (Hover 시)
    private Color clickColor = new Color(255, 20, 147); // 더 짙은 핑크색
    private Color defaultColor = Color.WHITE; // 기본 배경색

    // 생성자: 이미지 경로를 받아 바로 설정
    public PaintButton(ImageIcon icon) {
        setImageIcon(icon); // 초기 이미지 설정
        setBackground(defaultColor);
        setBorderPainted(false); // 버튼 테두리 제거
        setOpaque(true); // 불투명 활성화

        // 마우스 이벤트 리스너 추가
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // 커서를 손 모양으로 변경
                setBackground(hoverColor); // Hover 시 배경색 변경
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setCursor(Cursor.getDefaultCursor()); // 기본 커서로 복원
                setBackground(defaultColor); // 기본 배경색으로 복원
            }

            @Override
            public void mousePressed(MouseEvent e) {
                setBackground(clickColor); // 클릭 시 배경색 변경
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                setBackground(defaultColor); // 클릭 해제 시 Hover 색상으로 복원
            }
        });
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
            g.drawImage(imgMain, 7, 10, 40, 40, this);
        }
    }
}
