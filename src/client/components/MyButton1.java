package client.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MyButton1 extends JButton {
    private ImageIcon icon = new ImageIcon("img/reset.png");
    private Image imgMain = icon.getImage();

    // 색상 정의
    private Color hoverColor = new Color(255, 182, 193); // 핑크색
    private Color clickColor = new Color(255, 20, 147); // 더 짙은 핑크색
    private Color defaultColor = Color.WHITE; // 기본 배경색

    public MyButton1() {
        setOpaque(true); // 불투명 활성화
        setBackground(defaultColor); // 기본 배경색 설정
        setBorderPainted(false); // 테두리 제거

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
                setBackground(hoverColor); // 클릭 해제 시 Hover 색상으로 복원
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(imgMain, 7, 10, 40, 40, null); // 버튼 이미지 그리기
    }
}
