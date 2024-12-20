package client;

import client.components.*;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class CatchMindClient1 extends JFrame implements EndGameHandler{
    private static final String TAG = "GameStart :";
    private String IDString;

    private ImageIcon icGameStart, iconBlackPen, iconRedPen, iconOrangePen, iconYellowPen,
            iconGreenPen, iconBluePen, iconIndigoPen, iconPurplePen;

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    private MyPanel plMain;
    private JButton btnStart;
    private JPanel plId, plSub, plDrawRoom, plTopMpId, plTop, plMplId, plBottom, plEast, btnPanel, plEndGame;
    private MyPanel1 plDraw;
    private MyPanel2 plPalette;
    private MyButton btnEraser;
    private MyButton1 btnDelete;

    private JButton btnBlackDrawPen, btnRedDrawPen, btnOrangeDrawPen, btnYellowDrawPen, btnGreenDrawPen,
            btnBlueDrawPen, btnIndigoDrawPen, btnPurpleDrawPen;

    private JTextArea taUserList, taChat;
    private JPanel plChat;
    private TextField tfChat, tfIdInput;
    private JScrollPane scrChat;

    private JLabel laQuizTitle, laQuiz, laId, lbScores;
    private JButton btnId, btnReady, btnExit, btnEndGame, btnRestart;

    private Font ftSmall, ftMedium, ftLarge;
    private BufferedImage imgBuff;
    private JLabel drawLabel;
    private Brush brush;
    String sendDraw, sendColor;
    public static boolean drawPPAP = true;
    ReaderThread readerThread;
    private boolean isReady = false; // 버튼 상태를 저장하는 변수 (false: 준비 안 됨, true: 준비됨)

    public CatchMindClient1() {
        init();
        setting();
        batch();
        listener();
        setVisible(true);
    }

    private void init() {
        plMain = new MyPanel();
        plTopMpId = new MyPanel1();
        plPalette = new MyPanel2();
        btnEraser = new MyButton();
        btnDelete = new MyButton1();

        plId = new JPanel();
        plSub = new JPanel();
        plDrawRoom = new JPanel();
        plTop = new JPanel();
        plMplId = new JPanel();
        plBottom = new JPanel();
        plEast = new JPanel();
        btnPanel = new JPanel();
        plChat = new JPanel();  // plChat 객체 초기화 추가

        plEndGame = new JPanel();
        plEndGame.setLayout(null);
        plEndGame.setBounds(0, 0, 800, 640);
        plEndGame.setVisible(false);
        lbScores = new JLabel();
        btnEndGame = new JButton("게임 종료");
        btnRestart = new JButton("재시작");

        icGameStart = new ImageIcon("img/gameStart.png");
        iconBlackPen = new ImageIcon("img/drawBlackPen.png");
        iconRedPen = new ImageIcon("img/drawRedPen.png");
        iconOrangePen = new ImageIcon("img/drawOrangePen.png");
        iconYellowPen = new ImageIcon("img/drawYellowPen.png");
        iconGreenPen = new ImageIcon("img/drawGreenPen.png");
        iconBluePen = new ImageIcon("img/drawBluePen.png");
        iconIndigoPen = new ImageIcon("img/drawIndigoPen.png");
        iconPurplePen = new ImageIcon("img/drawPurplePen.png");

        btnStart = new JButton(icGameStart);
        btnId = new JButton(icGameStart);
        btnReady = new JButton("준비");
        btnExit = new JButton("나가기");

        btnBlackDrawPen = new JButton(iconBlackPen);
        btnRedDrawPen = new JButton(iconRedPen);
        btnOrangeDrawPen = new JButton(iconOrangePen);
        btnYellowDrawPen = new JButton(iconYellowPen);
        btnGreenDrawPen = new JButton(iconGreenPen);
        btnBlueDrawPen = new JButton(iconBluePen);
        btnIndigoDrawPen = new JButton(iconIndigoPen);
        btnPurpleDrawPen = new JButton(iconPurplePen);

        laId = new JLabel("아이디");
        laQuizTitle = new JLabel("제시어");
        laQuiz = new JLabel("변수");

        tfIdInput = new TextField();
        tfChat = new TextField();

        taChat = new JTextArea();
        taUserList = new JTextArea();

        ftSmall = new Font("맑은고딕", Font.PLAIN, 16);
        ftMedium = new Font("맑은고딕", Font.PLAIN, 24);
        ftLarge = new Font("맑은고딕", Font.PLAIN, 36);

        scrChat = new JScrollPane(taChat, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        imgBuff = new BufferedImage(750, 450, BufferedImage.TYPE_INT_ARGB);
        drawLabel = new JLabel(new ImageIcon(imgBuff));
        brush = new Brush(imgBuff);
    }

    private void setting() {
        setTitle("캐치마인드");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // plMain
        setContentPane(plMain);
        plMain.setLayout(null);
        btnStart.setBounds(300, 400, 200, 80); // 시작 버튼 위치 조정

        icGameStart = new ImageIcon("img/gameStart.png"); // 게임시작 버튼 이미지
        // plId
        plId.setLayout(null);
        plId.setVisible(false); // 비활성화
        plId.setBackground(new Color(242, 242, 242));
        plId.setBounds(44, 100, 650, 400); // plId 위치, 크기 조정 (x, y, width, height) 좌표는 plMain 기준

        plSub.setLayout(null);
        plSub.setVisible(false); // 비활성화
        plSub.setBorder(new LineBorder(new Color(87, 87, 87), 3, true));
        plSub.setBounds(275, 200, 250, 40); // 아이디 입력 필드 중앙 배치

        laId.setBounds(0, 2, 62, 32); // laId 위치, 크기 조정 (x, y, width, height) 좌표는 plId 기준
        laId.setBorder(new LineBorder(new Color(87, 87, 87), 2, true));
        laId.setFont(ftSmall);
        laId.setHorizontalAlignment(JLabel.CENTER); // 글자 가운데 정렬


        tfIdInput.setBounds(65, 3, 185, 34);
        tfIdInput.setBackground(new Color(242, 242, 242, 255));
        tfIdInput.setFont(ftMedium);
        btnId.setBounds(300, 300, 200, 80); // 아이디 확인 버튼 위치 조정

        plDrawRoom.setLayout(null); //BorderLayout으로 배치관리자 설정
        plDrawRoom.setVisible(false); // 비활성화
        plDrawRoom.setBounds(59, 105, 1005, 656);// plDrawRoom 위치, 크기 조정 좌표는 plMain 기준

        plTopMpId.setLayout(null);
        plTopMpId.setBackground(Color.GREEN);
        plTopMpId.setBounds(0, 0, 750, 530);

        plTop.setLayout(null);
        plTop.setBackground(Color.CYAN);
        plTop.setBounds(0, 0, 750, 80); // plTop 위치, 크기 조정 좌표는 plDrawRoom 기준

        //스케치북이 되는 영역
        plMplId.setLayout(null);
        plMplId.setBackground(Color.RED);
        plMplId.setBounds(0, 110, 750, 450); // plMplId 위치, 크기 조정 좌표는 plDrawRoom 기준

        plBottom.setLayout(null);
        plBottom.setBackground(Color.GRAY);
        plBottom.setBounds(0, 530, 700, 130); // plBottom 위치, 크기 조정 좌표는 plDrawRoom 기준

        //팔레트&지우개&휴지통 관련...
        iconBlackPen = new ImageIcon("img/drawBlackPen.png");
        iconRedPen = new ImageIcon("img/drawRedPen.png");
        iconOrangePen = new ImageIcon("img/drawOrangePen.png");
        iconYellowPen = new ImageIcon("img/drawYellowPen.png");
        iconGreenPen = new ImageIcon("img/drawGreenPen.png");
        iconBluePen = new ImageIcon("img/drawBluePen.png");
        iconIndigoPen = new ImageIcon("img/drawIndigoPen.png");
        iconPurplePen = new ImageIcon("img/drawPurplePen.png");

        btnBlackDrawPen.setBackground(new Color(242, 242, 242, 255));
        btnBlackDrawPen.setBorderPainted(false); // 버튼 테두리 제거

        btnRedDrawPen.setBackground(new Color(242, 242, 242, 255));
        btnRedDrawPen.setBorderPainted(false); // 버튼 테두리 제거

        btnOrangeDrawPen.setBackground(new Color(242, 242, 242, 255));
        btnOrangeDrawPen.setBorderPainted(false); // 버튼 테두리 제거

        btnYellowDrawPen.setBackground(new Color(242, 242, 242, 255));
        btnYellowDrawPen.setBorderPainted(false); // 버튼 테두리 제거

        btnGreenDrawPen.setBackground(new Color(242, 242, 242, 255));
        btnGreenDrawPen.setBorderPainted(false); // 버튼 테두리 제거

        btnBlueDrawPen.setBackground(new Color(242, 242, 242, 255));
        btnBlueDrawPen.setBorderPainted(false); // 버튼 테두리 제거

        btnIndigoDrawPen.setBackground(new Color(242, 242, 242, 255));
        btnIndigoDrawPen.setBorderPainted(false); // 버튼 테두리 제거

        btnPurpleDrawPen.setBackground(new Color(242, 242, 242, 255));
        btnPurpleDrawPen.setBorderPainted(false); // 버튼 테두리 제거

        btnEraser.setBackground(new Color(242, 242, 242, 255)); //지우개 버튼

        btnDelete.setBackground(new Color(242, 242, 242, 255)); //휴지통 버튼
        //end of 팔레트&지우개&휴지통 관련...

        plEast.setLayout(null);
        plEast.setBounds(750, 0, 255, 530); // plEast 위치, 크기 조정 좌표는 plDrawRoom 기준

        plChat.setLayout(null);

        btnPanel.setLayout(null);
        btnPanel.setBackground(Color.YELLOW);
        btnPanel.setBounds(700, 530, 405, 130);

        //크레용과 지우개, 휴지통을 담는 팔레트
        plPalette.setLayout(new GridLayout(1,10));
        plPalette.setBackground(Color.RED);
        plPalette.setBounds(0, 0, 520, 130); // plPalette 위치, 크기 조정 좌표는 plBottom 기준

        // plEast
        taUserList.setBounds(0, 0, 255, 150); // taUserList 위치, 크기 조정 좌표는 plEast 기준
        taUserList.setFont(ftMedium);
        taUserList.setBackground(Color.MAGENTA);
        taUserList.setLineWrap(true);

        plChat.setBackground(Color.GREEN);
        plChat.setBounds(0, 150, 255, 385); // plChat 위치, 크기 조정 좌표는 plEast 기준

        // plEast - plChat
        //채팅창의 입력 칸
        tfChat.setBounds(0, 350, 255, 30); // tfChat 위치, 크기 조정 좌표는 plEast 기준
        tfChat.setFont(ftMedium);
        tfChat.setBackground(Color.CYAN);
        tfChat.setColumns(30);

        //채팅 창의 스크롤 팬
        scrChat.setBounds(0, 0, 255, 350); // taChat 위치, 크기 조정 좌표는 plEast 기준
        scrChat.setFocusable(false);

        //채팅 기록이 보여지는 채팅창
        taChat.setLineWrap(true);
        taChat.setBackground(Color.PINK);

        //제시어, 준비, 나가기 등이 부착되는 btnPanel
        //제시어 안내 레이블
        laQuizTitle.setVisible(true);
        laQuizTitle.setBounds(0, 2, 155, 65); // laQuiz 위치, 크기 조정 좌표는 plTop 기준
        laQuizTitle.setFont(ftMedium);
        laQuizTitle.setBackground(Color.CYAN);
        laQuizTitle.setHorizontalAlignment(JLabel.CENTER); // 글자 가운데 정렬

        //실제 제시어가 나오는 레이블
        laQuiz.setVisible(false);
        laQuiz.setBounds(0, 67, 155, 65); // laQuiz 위치, 크기 조정 좌표는 plTop 기준
        laQuiz.setFont(ftMedium);
        laQuiz.setBackground(Color.RED);
        laQuiz.setHorizontalAlignment(JLabel.CENTER); // 글자 가운데 정렬

        //게임 패널 내의 준비 버튼
        btnReady.setBounds(150, 2, 155, 65); // btnReady 위치, 크기 조정 좌표는 plEast 기준
        btnReady.setFont(ftMedium);
        btnReady.setBackground(Color.BLUE);
        btnReady.setBorder(new LineBorder(new Color(87, 87, 87), 5, true));

        //게임 패널 내의 나가기 버튼
        btnExit.setBounds(150, 62, 155, 65); // btnExit 위치, 크기 조정 좌표는 plEast 기준
        btnExit.setFont(ftMedium);
        btnExit.setBackground(Color.DARK_GRAY);
        btnExit.setBorder(new LineBorder(new Color(87, 87, 87), 5, true));
        //end of 제시어, 준비, 나가기 등이 부착되는 btnPane...

        //게임 패널 내의 드로우 캔버스
        drawLabel.setBounds(0, 0, 750, 450);
        drawLabel.setBackground(new Color(255, 255, 255, 0));
        brush.setBounds(0, 0, 750, 450);


        // 종료 버튼
        btnEndGame = new JButton();
        btnEndGame.setBounds(300, 400, 200, 60);
        btnEndGame.setBorderPainted(false);
        btnEndGame.setContentAreaFilled(false);
        btnEndGame.setFocusPainted(false);

        // 재시작 버튼
        btnRestart = new JButton();
        btnRestart.setBounds(300, 300, 200, 60);
        btnRestart.setBorderPainted(false);
        btnRestart.setContentAreaFilled(false);
        btnRestart.setFocusPainted(false);

        // 버튼 추가
        plEndGame.add(btnRestart);
        plEndGame.add(btnEndGame);
        setSize(850, 600);
    }

    private void batch() {
        plMain.add(btnStart);
        plMain.add(plId);
        plMain.add(plDrawRoom);
        btnStart.setIcon(icGameStart);

        plId.add(plSub);
        plSub.add(laId);
        plSub.add(tfIdInput);
        plId.add(btnId);
        btnId.setIcon(icGameStart);

        plDrawRoom.add(plTopMpId);

        plTopMpId.add(plTop);
        plTopMpId.add(plMplId);

        plDrawRoom.add(plBottom);
        plDrawRoom.add(plEast);
        plDrawRoom.add(btnPanel);

        plBottom.add(plPalette);
        //팔레트에 크레용 및 지우개, 휴지통 버튼 추가
        plPalette.add(btnBlackDrawPen);
        plPalette.add(btnRedDrawPen);
        plPalette.add(btnOrangeDrawPen);
        plPalette.add(btnYellowDrawPen);
        plPalette.add(btnGreenDrawPen);
        plPalette.add(btnBlueDrawPen);
        plPalette.add(btnIndigoDrawPen);
        plPalette.add(btnPurpleDrawPen);
        plPalette.add(btnEraser);
        plPalette.add(btnDelete);


        plEast.add(plChat);
        plEast.add(taUserList);

        Component add = plChat.add(scrChat);
        plChat.add(tfChat);

        btnPanel.add(laQuiz);
        btnPanel.add(laQuizTitle);
        btnPanel.add(btnReady);
        btnPanel.add(btnExit);

        // 드로우
        plMplId.add(drawLabel);
        plMplId.add(brush);

        // 게임 종료 화면을 반드시 마지막에 추가
        plMain.add(plEndGame);
        plMain.setComponentZOrder(plEndGame, 0); // 최상위로 설정
    }

    private void listener() {
        tfChat.addActionListener(e -> sendChat());

        btnStart.addActionListener(e -> {
            plId.setVisible(true);
            plSub.setVisible(true);
            btnStart.setVisible(false);
            btnId.setVisible(true);
        });

        btnId.addActionListener(e -> {
            connectServer();
            sendInsertId();
        });

        btnExit.addActionListener(e -> {
            sendExit();
            System.exit(0);
        });

        btnReady.addActionListener(e -> {
            if (!btnReady.isEnabled()) {
                return; // 버튼이 비활성화된 경우 동작하지 않음
            }
            isReady = !isReady; // 준비 상태 토글
            btnReady.setFocusPainted(false); // 포커스 효과 제거
            if (isReady) {
                btnReady.setText("준비 취소"); // 텍스트 변경
                btnReady.setBackground(Color.ORANGE); // 초록색으로 변경
                btnReady.setForeground(Color.WHITE); // 텍스트 흰색
                sendReady(); // 서버에 준비 상태 전송
            } else {
                btnReady.setText("준비"); // 텍스트 변경
                btnReady.setBackground(Color.PINK); // 기본 배경색으로 변경
                btnReady.setForeground(Color.BLACK); // 텍스트 검정색
                sendReady(); // 서버에 준비 취소 상태 전송
            }
        });

        drawLabel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (ReaderThread.drawPPAP) {
                    sendDraw = "DRAW&" + e.getX() + "," + e.getY();
                    brush.setX(e.getX());
                    brush.setY(e.getY());
                    brush.repaint();
                    brush.printAll(imgBuff.getGraphics());
                    writer.println(sendDraw);
                }
            }
        });

        btnBlackDrawPen.addActionListener(e -> changePenColor("Black", Color.BLACK));
        btnRedDrawPen.addActionListener(e -> changePenColor("Red", Color.RED));
        btnOrangeDrawPen.addActionListener(e -> changePenColor("Orange", Color.ORANGE));
        btnYellowDrawPen.addActionListener(e -> changePenColor("Yellow", Color.YELLOW));
        btnGreenDrawPen.addActionListener(e -> changePenColor("Green", Color.GREEN));
        btnBlueDrawPen.addActionListener(e -> changePenColor("Blue", Color.CYAN));
        btnIndigoDrawPen.addActionListener(e -> changePenColor("Indigo", Color.BLUE));
        btnPurpleDrawPen.addActionListener(e -> changePenColor("Purple", Color.PINK));
        btnEraser.addActionListener(e -> changePenColor("White", Color.WHITE));

        btnDelete.addActionListener(e -> {
            sendColor = "COLOR&Delete";
            writer.println(sendColor);
            brush.setClearC(false);
            cleanDraw();
        });

        btnEndGame.addActionListener(e -> System.exit(0)); // 게임 종료 버튼
        btnRestart.addActionListener(e -> restartGame());  // 재시작 버튼
    }

    // 게임 종료 화면 표시
    public void showEndGameScreen(String scores) {
        System.out.println("Showing End Game Screen...");

        // 종료 화면 활성화
        plDrawRoom.setVisible(false); // 게임 화면 비활성화
        plId.setVisible(false);       // ID 입력 화면 비활성화

        // 종료 화면 패널 크기 설정
        plEndGame.setBounds(60, 100, 970, 700);
        plEndGame.setVisible(true);

        // 점수 데이터 처리
        String[] scoreLines = scores.split("&");
        java.util.List<String[]> scoreList = new java.util.ArrayList<>();
        for (String line : scoreLines) {
            String[] parts = line.split(":"); // "아이디:점수" 형식
            if (parts.length == 2) {
                scoreList.add(parts);
            }
        }

        // 점수 내림차순 정렬
        scoreList.sort((a, b) -> Integer.parseInt(b[1].trim()) - Integer.parseInt(a[1].trim()));

        // 점수 표시 영역 초기화
        int startX = 300; // 시작 X 좌표
        int startY = 100;  // 시작 Y 좌표
        int rowHeight = 40; // 각 행의 높이

        JLabel titleLabel = new JLabel("랭킹");
        titleLabel.setBounds(startX, startY, 100, rowHeight);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        plEndGame.add(titleLabel);

        JLabel idLabel = new JLabel("아이디");
        idLabel.setBounds(startX + 120, startY, 200, rowHeight);
        idLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        plEndGame.add(idLabel);

        JLabel scoreLabel = new JLabel("점수");
        scoreLabel.setBounds(startX + 340, startY, 100, rowHeight);
        scoreLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        plEndGame.add(scoreLabel);

        // 점수 표시
        int rank = 1;
        for (String[] entry : scoreList) {
            String playerId = entry[0].trim();
            String playerScore = entry[1].trim();

            JLabel rankLabel = new JLabel(rank + "");
            rankLabel.setBounds(startX, startY + rank * rowHeight, 100, rowHeight);
            rankLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 24));
            plEndGame.add(rankLabel);

            JLabel playerIdLabel = new JLabel(playerId);
            playerIdLabel.setBounds(startX + 120, startY + rank * rowHeight, 200, rowHeight);
            playerIdLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 24));
            plEndGame.add(playerIdLabel);

            JLabel playerScoreLabel = new JLabel(playerScore);
            playerScoreLabel.setBounds(startX + 340, startY + rank * rowHeight, 100, rowHeight);
            playerScoreLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 24));
            plEndGame.add(playerScoreLabel);

            rank++;
        }

        // 버튼 공통 크기 및 스타일 설정
        int buttonWidth = 300;
        int buttonHeight = 80;
        int buttonY = 400;
        Font buttonFont = new Font("맑은 고딕", Font.BOLD, 22);

        // 재시작 버튼
        btnRestart.setText("재시작");
        btnRestart.setBounds(200, buttonY, buttonWidth, buttonHeight); // 왼쪽 버튼 위치
        btnRestart.setFont(buttonFont);
        btnRestart.setBackground(Color.WHITE); // 기본 흰색 배경
        btnRestart.setForeground(Color.BLACK); // 기본 검정 텍스트
        btnRestart.setFocusPainted(false);
        btnRestart.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        // hover 효과 (재시작 버튼)
        btnRestart.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                btnRestart.setBackground(new Color(87, 255, 87)); // 초록색 배경
                btnRestart.setForeground(Color.WHITE); // 흰색 글자
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                btnRestart.setBackground(Color.WHITE); // 기본 흰색 배경
                btnRestart.setForeground(Color.BLACK); // 기본 검정 텍스트
            }
        });

        plEndGame.add(btnRestart);

        // 종료 버튼
        btnEndGame.setText("게임 종료");
        btnEndGame.setBounds(500, buttonY, buttonWidth, buttonHeight); // 오른쪽 버튼 위치
        btnEndGame.setFont(buttonFont);
        btnEndGame.setBackground(Color.WHITE); // 기본 흰색 배경
        btnEndGame.setForeground(Color.BLACK); // 기본 검정 텍스트
        btnEndGame.setFocusPainted(false);
        btnEndGame.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        // hover 효과 (종료 버튼)
        btnEndGame.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                btnEndGame.setBackground(new Color(255, 87, 87)); // 빨간색 배경
                btnEndGame.setForeground(Color.WHITE); // 흰색 글자
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                btnEndGame.setBackground(Color.WHITE); // 기본 흰색 배경
                btnEndGame.setForeground(Color.BLACK); // 기본 검정 텍스트
            }
        });

        plEndGame.add(btnEndGame);

        // 버튼 리스너
        btnEndGame.addActionListener(e -> System.exit(0));
        btnRestart.addActionListener(e -> restartGame());

        // 갱신
        plMain.revalidate();
        plMain.repaint();

        // 준비 버튼 초기화
        btnReady.setEnabled(true);
        btnReady.setText("준비");
        btnReady.setBackground(Color.RED); // 기본 배경색
        btnReady.setForeground(Color.BLACK); // 기본 텍스트 색상
        isReady = false;
    }

    // 재시작 메서드
    private void restartGame() {
        plEndGame.setVisible(false); // 종료 화면 숨김
        plId.setVisible(false);       // 초기화면 활성화
        btnId.setVisible(false);
        btnStart.setVisible(true);   // 시작 버튼 활성화
        plDrawRoom.setVisible(false); // 그리기 방 비활성화
        resetGameState();            // 게임 상태 초기화

        // **plEndGame 초기화**
        clearEndGameScreen();
    }
    // plEndGame 초기화 메서드
    private void clearEndGameScreen() {
        // plEndGame에 추가된 모든 컴포넌트 제거
        plEndGame.removeAll();

        // 기본 종료 버튼과 재시작 버튼 다시 추가
        plEndGame.add(btnRestart);
        plEndGame.add(btnEndGame);

        // 갱신
        plEndGame.revalidate();
        plEndGame.repaint();
    }

    // 게임 상태 초기화
    private void resetGameState() {
        setSize(850, 600);
        brush.setClearC(true);
        cleanDraw(); // 캔버스 초기화
        tfIdInput.setText(""); // 아이디 입력 필드 초기화
        taChat.setText(""); // 채팅 창 초기화
        taUserList.setText(""); // 유저 리스트 초기화
        drawPPAP = false; // 그림 그리기 비활성화
        isReady= false;
        readerThread.setIDString("");
    }

    private void changePenColor(String colorName, Color color) {
        sendColor = "COLOR&" + colorName;
        brush.setColor(color);
        writer.println(sendColor);
    }

    // 접속 시 서버 연결 메서드.
    private void connectServer() {
        try {
            socket = new Socket("localhost", 3000);
            readerThread = new ReaderThread(socket, brush, taChat, taUserList, scrChat, laQuiz, btnReady, plBottom, tfChat, imgBuff, this, IDString);
            readerThread.start();

        } catch (Exception e) {
            System.out.println(TAG + "서버 연결 실패");
        }
    }

    // EXIT 프로토콜 메서드.
    private void sendExit() {
        try {
            writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println("EXIT&" + IDString);
        } catch (Exception e) {
            System.out.println(TAG + "Exit Msg writer fail...");
        }
    }

    // READY 프로토콜 메서드.
    private void sendReady() {
        try {
            writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println("READY&");
        } catch (Exception e) {
            System.out.println(TAG + "Ready Msg send fail...");
        }

    }

    // CHAT 프로토콜 메서드.
    private void sendChat() {
        try {
            writer = new PrintWriter(socket.getOutputStream(), true);
            String chatString = tfChat.getText();
            writer.println("CHAT&" + chatString);
            tfChat.setText("");
        } catch (Exception e) {
            System.out.println(TAG + "채팅 메세지 요청 실패");
        }
    }

    // ID 프로토콜 메서드
    private void sendInsertId() {
        try {
            writer = new PrintWriter(socket.getOutputStream(), true);
            IDString = tfIdInput.getText();
            System.out.println("IDString = " + IDString);
            if ((IDString.equals(""))) {
                IDString = "user" + (int)(Math.random() * 10000); // 랜덤한 더미값 생성
                plId.setVisible(false); // plId 비활성화
                plSub.setVisible(false); // plId 활성화
                plDrawRoom.setVisible(true); // plDrawRoom 활성화
                setSize(1100, 800);
            } else { // 아이디 존재
                //writer.println("ID&" + IDString);
                tfIdInput.setText("");
                plId.setVisible(false); // plId 비활성화
                plSub.setVisible(false); // plId 활성화
                plDrawRoom.setVisible(true); // plDrawRoom 활성화
                setSize(1100, 800);
            }
            readerThread.setIDString(IDString);
            writer.println("ID&" + IDString);


        } catch (IOException e) {
            System.out.println(TAG + "준비 메세지 요청 실패");
        }
    }

    // 드로우 캔버스 초기화 메서드
    private void cleanDraw() {
        brush.setClearC(false);
        brush.repaint();
        brush.printAll(imgBuff.getGraphics());
    }

    public static void main(String[] args) {
        new CatchMindClient1();
    }
}
