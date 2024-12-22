package client.components;

import javax.swing.*;
import java.awt.*;

public class MyPanel extends JPanel {
    private JLabel gifLabel;
    private JLabel designLabel;

    public MyPanel() {
        ImageIcon designIcon=new ImageIcon("img/catchMindImg.png"); //캐치마인드 로고가 되는 이미지 아이콘
        designLabel=new JLabel(designIcon);

        // GIF 이미지 로드 및 설정
        ImageIcon gifIcon = new ImageIcon("img/startBg2.gif"); //배경의 반짝이는 gif
        Image scaledGif = gifIcon.getImage().getScaledInstance(600, 500, Image.SCALE_DEFAULT);
        // 스케일된 GIF를 JLabel로 추가
        ImageIcon scaledIcon = new ImageIcon(scaledGif);
        gifLabel = new JLabel(scaledIcon);

        this.setLayout(null); // 자유 레이아웃
        gifLabel.setBounds(0, 0, 600, 500);
        designLabel.setBounds(50, -70, 500,400);
        this.add(gifLabel);
        this.add(designLabel);
        this.setComponentZOrder(gifLabel, this.getComponentCount() - 1); // GIF를 뒤로 보냄
    }

    //배경화면을 구성하는 이미지들을 제거하는 메서드
    public void removeStartPanelDesign(){
        this.remove(gifLabel);
        this.remove(designLabel);
        this.repaint();
    }
}