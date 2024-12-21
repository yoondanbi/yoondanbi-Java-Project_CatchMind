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
    private JPanel plId, plSub, plDrawRoom, plMplId, plBottom, btnPanel, plEndGame;
    private JSplitPane splitPane;
    private MyPanel2 plPalette;
    private MyButton btnEraser;
    private MyButton1 btnDelete;

    private PaintButton btnBlackDrawPen, btnRedDrawPen, btnOrangeDrawPen, btnYellowDrawPen, btnGreenDrawPen,
            btnBlueDrawPen, btnIndigoDrawPen, btnPurpleDrawPen;

    private JTextArea taUserList, taChat;
    private JPanel plChat;
    private TextField tfChat, tfIdInput;
    private JScrollPane scrChat;

    private JLabel laQuizTitle, laQuiz, laId, lbScores;
    private JButton btnId, btnReady, btnEndGame, btnRestart;
    private PaintButton btnExit;

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
        plPalette = new MyPanel2();
        btnEraser = new MyButton();
        btnDelete = new MyButton1();

        plId = new JPanel();
        plSub = new JPanel();
        plDrawRoom = new JPanel();
        plMplId = new JPanel();
        plBottom = new JPanel();
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
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
        iconBlackPen = new ImageIcon("img/blackPaint.png");
        iconRedPen = new ImageIcon("img/redPaint.png");
        iconOrangePen = new ImageIcon("img/orangePaint.png");
        iconYellowPen = new ImageIcon("img/yellowPaint.png");
        iconGreenPen = new ImageIcon("img/greenPaint.png");
        iconBluePen = new ImageIcon("img/bluePaint.png");
        iconIndigoPen = new ImageIcon("img/navyPaint.png");
        iconPurplePen = new ImageIcon("img/purplePaint.png");

        btnStart = new JButton(icGameStart);
        btnId = new JButton(icGameStart);
        btnReady = new JButton("준비");
        btnExit = new PaintButton(new ImageIcon("img/exit.png"));

        btnBlackDrawPen = new PaintButton(iconBlackPen);
        btnRedDrawPen = new PaintButton(iconRedPen);
        btnOrangeDrawPen = new PaintButton(iconOrangePen);
        btnYellowDrawPen = new PaintButton(iconYellowPen);
        btnGreenDrawPen = new PaintButton(iconGreenPen);
        btnBlueDrawPen = new PaintButton(iconBluePen);
        btnIndigoDrawPen = new PaintButton(iconIndigoPen);
        btnPurpleDrawPen = new PaintButton(iconPurplePen);

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

        plDrawRoom.setLayout(new BorderLayout()); //BorderLayout으로 배치관리자 설정
        plDrawRoom.setVisible(false); // 비활성화
        plDrawRoom.setBounds(0,0, 600, 500);// plDrawRoom 위치, 크기 조정 좌표는 plMain 기준

        //스케치북이 되는 영역
        plMplId.setLayout(null);
        plMplId.setBackground(Color.WHITE);
        plMplId.setBounds(0, 110, 750, 450); // plMplId 위치, 크기 조정 좌표는 plDrawRoom 기준

        //팔레트&지우개&휴지통 관련...
        iconBlackPen = new ImageIcon("img/drawBlackPen.png");
        iconRedPen = new ImageIcon("img/drawRedPen.png");
        iconOrangePen = new ImageIcon("img/drawOrangePen.png");
        iconYellowPen = new ImageIcon("img/drawYellowPen.png");
        iconGreenPen = new ImageIcon("img/drawGreenPen.png");
        iconBluePen = new ImageIcon("img/drawBluePen.png");
        iconIndigoPen = new ImageIcon("img/drawIndigoPen.png");
        iconPurplePen = new ImageIcon("img/drawPurplePen.png");

        btnEraser.setBackground(Color.WHITE); //지우개 버튼
        btnDelete.setBackground(Color.WHITE); //휴지통 버튼
        //end of 팔레트&지우개&휴지통 관련...

        plChat.setLayout(null); /////////////////////

        //끝내기, 준비 버튼과 제시어가 나오는 패널
        btnPanel.setLayout(new FlowLayout());
        btnPanel.setBackground(Color.YELLOW);
        btnPanel.setBounds(700, 530, 405, 130);

        //크레용과 지우개, 휴지통을 담는 팔레트
        plPalette.setLayout(new GridLayout(1,10));

        //SplitPane - 아이디와 점수판을 나타내는 패널
        taUserList.setBounds(0, 0, 255, 150); // taUserList 위치, 크기 조정 좌표는 plEast 기준
        taUserList.setFont(ftMedium);
        taUserList.setBackground(Color.MAGENTA);
        taUserList.setLineWrap(true);

        //SplitPane - 채팅창(tachat), 채팅 입력칸(tfchat), 스크롤팬(scrchat)을 가지는 패널
        plChat.setBackground(Color.GREEN);
        plChat.setSize(180,300);// plChat 위치, 크기 조정 좌표는 plEast 기준

        //SplitPane - 채팅창의 입력 칸
        tfChat.setBounds(0,190,180, 50); // tfChat 크기
        tfChat.setFont(ftMedium);
        tfChat.setBackground(Color.CYAN);
        tfChat.setColumns(30);

        //채팅 창의 스크롤 팬
        scrChat.setSize(180, 190); // taChat 크기
        scrChat.setFocusable(false);
        //채팅 기록이 보여지는 채팅창
        taChat.setLineWrap(true);
        taChat.setBackground(Color.PINK);

        //제시어, 준비, 나가기 등이 부착되는 btnPanel
        //제시어 안내 레이블
        laQuizTitle.setVisible(true);
        laQuizTitle.setFont(ftMedium);
        laQuizTitle.setBackground(Color.CYAN);
        laQuizTitle.setHorizontalAlignment(JLabel.CENTER); // 글자 가운데 정렬

        //실제 제시어가 나오는 레이블
        laQuiz.setVisible(false);
        laQuiz.setFont(ftMedium);
        laQuiz.setBackground(Color.RED);
        laQuiz.setHorizontalAlignment(JLabel.CENTER); // 글자 가운데 정렬

        //게임 패널 내의 준비 버튼
        btnReady.setFont(ftMedium);
        btnReady.setBackground(Color.BLUE);
        btnReady.setBorder(new LineBorder(new Color(87, 87, 87), 5, true));

        //게임 패널 내의 나가기 버튼
        //btnExit.setFont(ftMedium);
        btnExit.setBackground(Color.WHITE);
        btnExit.setBounds(btnPanel.getX()-10,btnPanel.getY()-20,40,40);
        //btnExit.setBorder(new LineBorder(new Color(87, 87, 87), 5, true));
        //end of 제시어, 준비, 나가기 등이 부착되는 btnPane...

        //게임 패널 내의 드로우 캔버스
        drawLabel.setBounds(0,0,750,450);
        drawLabel.setBackground(Color.CYAN);
        brush.setBounds(0, 0, 750, 450); //이 부분 추후 다시 수정


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


        //메인 게임 패널에 붙이는 부분
        //plDrawRoom.add(plTopMpId); //추후 삭제
        plDrawRoom.add(plMplId,BorderLayout.CENTER); //center, 앞을 drawroom으로 변경
        plDrawRoom.add(plPalette,BorderLayout.SOUTH); //plPalette로 변경, south
        plDrawRoom.add(splitPane,BorderLayout.EAST); //east
        plDrawRoom.add(btnPanel,BorderLayout.NORTH); //north

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
        plPalette.setPreferredSize(new Dimension(0, 100)); // 높이 200px로 제한
        plPalette.setMinimumSize(new Dimension(0, 100));  // 최소 높이 200px

        //BorderLayout의 EAST에 위치하는 JSplitPane
        splitPane.setDividerLocation(70); //h좌표 100에서 나누도록 설정
        splitPane.setEnabled(false); //SplitPane영역을 사용자가 임의로 움직일 수 없도록 설정
        splitPane.setTopComponent(taUserList);
        splitPane.setBottomComponent(plChat);
        splitPane.setPreferredSize(new Dimension(180, 0)); // 폭 180px로 제한
        splitPane.setMinimumSize(new Dimension(180, 0));  // 최소 폭 180px

        Component add = plChat.add(scrChat);
        plChat.add(tfChat);

        btnPanel.add(laQuizTitle);
        btnPanel.add(laQuiz);
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
                setSize(600, 500);
            } else { // 아이디 존재
                //writer.println("ID&" + IDString);
                tfIdInput.setText("");
                plId.setVisible(false); // plId 비활성화
                plSub.setVisible(false); // plId 활성화
                plDrawRoom.setVisible(true); // plDrawRoom 활성화
                setSize(600, 500);
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
