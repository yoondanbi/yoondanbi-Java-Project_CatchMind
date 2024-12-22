package client;

import client.components.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class CatchMindClient2 extends JFrame implements EndGameHandler{
    private static final String LOG_TAG = "GameStart :";
    private String userId;
    private ImageIcon icGameStart, penIconBlack, penIconRed, penIconOrange, penIconYellow,
            penIconGreen, penIconBlue, penIconIndigo, penIconPurple;
    private Socket clientSocket;
    private PrintWriter outputWriter;
    private MyPanel panelMain;
    private JButton btnStart;
    private JPanel panelId, panelSub, panelDrawRoom, panelBasePanel,panelMplId, panelBottom, btnPanel, panelEndGame;
    private JSplitPane splitPane;
    private MyPanel2 panelPalette;
    private MyButton btnEraser;
    private MyButton1 btnDelete;

    private PaintButton btnBlackDrawPen, btnRedDrawPen, btnOrangeDrawPen, btnYellowDrawPen, btnGreenDrawPen,
            btnBlueDrawPen, btnIndigoDrawPen, btnPurpleDrawPen;

    private JTextArea textAreaUserList, textAreaChat;
    private JPanel panelChat;
    private TextField textFieldChatInput, textFieldUserIdInput;
    private JScrollPane scrollPaneChat;

    private JLabel labelQuizTitle, labelQuizContent, labelUserId, labelScoreBoard;
    private JButton btnId, btnReady,btnExit, btnEndGame, btnRestart,btnSend;

    private Font ftSmall, ftMedium, ftLarge;
    private BufferedImage imgBuff;
    private JLabel canvasLabel;
    private Brush drawingBrush;
    String drawCommand, colorCommand;
    public static boolean isDrawingEnabled = true;
    ReaderThread readerThread;
    private boolean isReady = false; // 버튼 상태를 저장하는 변수 (false: 준비 안 됨, true: 준비됨)
    private ImageIcon ready1Icon, ready2Icon;

    public CatchMindClient2() {
        init();
        setting();
        batch();
        listener();
        setVisible(true);
    }

    private void init() {
        panelMain = new MyPanel();
        panelPalette = new MyPanel2();
        btnEraser = new MyButton();
        btnDelete = new MyButton1();

        panelId = new JPanel();
        panelSub = new JPanel();
        panelDrawRoom = new JPanel();
        panelBasePanel=new JPanel();
        panelMplId = new JPanel();
        panelBottom = new JPanel();
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        btnPanel = new JPanel();
        panelChat = new JPanel();  // panelChat 객체 초기화 추가

        panelEndGame = new JPanel();
        panelEndGame.setLayout(null);
        panelEndGame.setBounds(0, 0, 800, 640);
        panelEndGame.setVisible(false);
        labelScoreBoard = new JLabel();
        btnEndGame = new JButton("게임 종료");
        btnRestart = new JButton("재시작");

        penIconGreen = new ImageIcon("img/greenPaint.png");
        penIconBlue = new ImageIcon("img/bluePaint.png");
        penIconIndigo = new ImageIcon("img/navyPaint.png");
        penIconPurple = new ImageIcon("img/purplePaint.png");
        penIconBlack = new ImageIcon("img/blackPaint.png");
        penIconRed = new ImageIcon("img/redPaint.png");
        penIconOrange = new ImageIcon("img/orangePaint.png");
        penIconYellow = new ImageIcon("img/yellowPaint.png");
        icGameStart = new ImageIcon("img/gameStart.png");

        btnStart = new JButton(icGameStart);
        btnId = new JButton(icGameStart);
        btnSend =new JButton("send");

        // Ready 버튼 아이콘 로드 및 고품질 리사이즈
        ready1Icon = resizeIcon(new ImageIcon("img/ready.png"), 40, 40); // 원하는 크기로 설정
        ready2Icon = resizeIcon(new ImageIcon("img/ready2.png"), 60, 60);
        btnReady = new JButton(ready1Icon);

        btnExit = new JButton(new ImageIcon("img/exit.png"));

        btnBlackDrawPen = new PaintButton(penIconBlack);
        btnRedDrawPen = new PaintButton(penIconRed);
        btnOrangeDrawPen = new PaintButton(penIconOrange);
        btnYellowDrawPen = new PaintButton(penIconYellow);
        btnGreenDrawPen = new PaintButton(penIconGreen);
        btnBlueDrawPen = new PaintButton(penIconBlue);
        btnIndigoDrawPen = new PaintButton(penIconIndigo);
        btnPurpleDrawPen = new PaintButton(penIconPurple);

        labelUserId = new JLabel("아이디");
        labelQuizTitle = new JLabel("제시어: ");
        labelQuizContent = new JLabel("변수");

        textFieldUserIdInput = new TextField();
        textFieldChatInput = new TextField();

        textAreaChat = new JTextArea();
        textAreaUserList = new JTextArea();

        scrollPaneChat = new JScrollPane(textAreaChat, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // 세로 스크롤바 색상 변경
        JScrollBar verticalScrollBar = scrollPaneChat.getVerticalScrollBar();
        verticalScrollBar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            private Color decrementButtonColor;

            @Override
            protected void configureScrollBarColors() {
                // 세로 스크롤바의 thumb(스크롤바 조작 부분) 색상
                this.thumbColor = Color.PINK;
                // 스크롤바 트랙 색상
                this.trackColor = new Color(255, 204, 229);
            }
            @Override
            protected JButton createDecreaseButton(int orientation) {
                JButton button = super.createDecreaseButton(orientation);
                button.setBackground(Color.PINK); // 위 버튼 색
                button.setBorderPainted(false); // 버튼 테두리 제거
                button.setIcon(null); // 화살표 아이콘 제거
                return button;
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                JButton button = super.createIncreaseButton(orientation);
                button.setBackground(Color.PINK); // 아래 버튼 색
                button.setBorderPainted(false); // 버튼 테두리 제거
                button.setIcon(null); // 화살표 아이콘 제거
                return button;
            }
        });

        imgBuff = new BufferedImage(750, 450, BufferedImage.TYPE_INT_ARGB);
        canvasLabel = new JLabel(new ImageIcon(imgBuff));
        drawingBrush = new Brush(imgBuff);
    }

    private void setting() {
        setTitle("캐치마인드");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // panelMain
        setContentPane(panelMain);
        panelMain.setLayout(null);
        btnStart.setBounds(300, 400, 200, 80); // 시작 버튼 위치 조정

        icGameStart = new ImageIcon("img/gameStart.png"); // 게임시작 버튼 이미지
        // panelId
        panelId.setLayout(null);
        panelId.setVisible(false); // 비활성화
        panelId.setBackground(new Color(242, 242, 242));
        panelId.setBounds(44, 100, 650, 400); // panelId 위치, 크기 조정 (x, y, width, height) 좌표는 panelMain 기준

        panelSub.setLayout(null);
        panelSub.setVisible(false); // 비활성화
        panelSub.setBorder(new LineBorder(new Color(87, 87, 87), 3, true));
        panelSub.setBounds(275, 200, 250, 40); // 아이디 입력 필드 중앙 배치

        labelUserId.setBounds(0, 2, 62, 32); // labelUserId 위치, 크기 조정 (x, y, width, height) 좌표는 panelId 기준
        labelUserId.setBorder(new LineBorder(new Color(87, 87, 87), 2, true));
        labelUserId.setFont(ftSmall);
        labelUserId.setHorizontalAlignment(JLabel.CENTER); // 글자 가운데 정렬


        textFieldUserIdInput.setBounds(65, 3, 185, 34);
        textFieldUserIdInput.setBackground(new Color(242, 242, 242, 255));
        textFieldUserIdInput.setFont(ftMedium);
        btnId.setBounds(300, 300, 200, 80); // 아이디 확인 버튼 위치 조정

        panelDrawRoom.setLayout(new BorderLayout()); //BorderLayout으로 배치관리자 설정
        panelDrawRoom.setVisible(false); // 비활성화
        panelDrawRoom.setBounds(0,0, 600, 500);// panelDrawRoom 위치, 크기 조정 좌표는 panelMain 기준

        //스케치북 디자인을 위해 배경이 되는 패널
        panelBasePanel.setLayout(null);
        panelBasePanel.setBackground(Color.WHITE);

        //스케치북 디자인을 위해 스프링 이미지를 넣는 이미지 레이블
        ImageIcon springImage = new ImageIcon("img/spring.png");
        JLabel imageLabel = new JLabel(springImage);
        JLabel imageLabel1 = new JLabel(springImage);
        JLabel imageLabel2 = new JLabel(springImage);

        // 스프링 이미지 레이블 크기 및 위치 설정 (panelBasePanel 상단에 배치)
        imageLabel.setBounds(5, -45, springImage.getIconWidth(), springImage.getIconHeight());
        imageLabel1.setBounds(137, -45, springImage.getIconWidth(), springImage.getIconHeight());
        imageLabel2.setBounds(274, -45, springImage.getIconWidth(), springImage.getIconHeight());

        panelBasePanel.add(imageLabel);
        panelBasePanel.add(imageLabel1);
        panelBasePanel.add(imageLabel2);

        //스케치북이 되는 영역
        panelMplId.setLayout(null);
        panelMplId.setBackground(Color.WHITE);
        panelMplId.setBounds(10, 15, 400, 315); // panelMplId 위치, 크기 조정 좌표는 panelBasePanel 기준
        // 테두리 설정
        Border border = BorderFactory.createLineBorder(Color.BLACK, 5);  // 검은색 선 테두리, 두께 5
        panelMplId.setBorder(border);

        //팔레트&지우개&휴지통 관련...
        penIconBlack = new ImageIcon("img/drawBlackPen.png");
        penIconRed = new ImageIcon("img/drawRedPen.png");
        penIconOrange = new ImageIcon("img/drawOrangePen.png");
        penIconYellow = new ImageIcon("img/drawYellowPen.png");
        penIconGreen = new ImageIcon("img/drawGreenPen.png");
        penIconBlue = new ImageIcon("img/drawBluePen.png");
        penIconIndigo = new ImageIcon("img/drawIndigoPen.png");
        penIconPurple = new ImageIcon("img/drawPurplePen.png");

        btnEraser.setBackground(Color.WHITE); //지우개 버튼
        btnDelete.setBackground(Color.WHITE); //휴지통 버튼
        //end of 팔레트&지우개&휴지통 관련...

        panelChat.setLayout(null); /////////////////////

        //끝내기, 준비 버튼과 제시어가 나오는 패널
        btnPanel.setLayout(null);
        btnPanel.setPreferredSize(new Dimension(600, 70));
        btnPanel.setBackground(Color.WHITE);

        //크레용과 지우개, 휴지통을 담는 팔레트
        panelPalette.setLayout(new GridLayout(1,10));

        //SplitPane - 아이디와 점수판을 나타내는 패널
        textAreaUserList.setBounds(0, 0, 255, 150); // textAreaUserList 위치, 크기 조정 좌표는 plEast 기준
        textAreaUserList.setFont(loadCustomFont("fonts/cuteFont.ttf", Font.PLAIN, 23));
        textAreaUserList.setBackground(Color.WHITE);
        textAreaUserList.setLineWrap(true);

        //SplitPane - 채팅창(textAreaChat), 채팅 입력칸(textFieldChatInput), 스크롤팬(scrollPaneChat)을 가지는 패널
        panelChat.setBackground(Color.WHITE);
        panelChat.setSize(170,300);// panelChat 위치, 크기 조정 좌표는 plEast 기준

        //SplitPane - 채팅창의 입력 칸
        textFieldChatInput.setBounds(0,200,100, 43); // textFieldChatInput 크기
        textFieldChatInput.setFont(loadCustomFont("fonts/cuteFont.ttf", Font.BOLD, 20));
        textFieldChatInput.setBackground(Color.WHITE);
        textFieldChatInput.setColumns(30);

        //SplitPane - 채팅창의 입력 칸 값을 보내는 버튼
        btnSend.setBounds(105,200,58,43);
        btnSend.setFont(loadCustomFont("fonts/cuteFont.ttf", Font.BOLD, 11));
        btnSend.setBackground(Color.pink);
        btnSend.setForeground(Color.WHITE);

        //채팅 창의 스크롤 팬
        scrollPaneChat.setSize(165, 190); // textAreaChat 크기
        scrollPaneChat.setFocusable(false);
        scrollPaneChat.setBackground(Color.WHITE);
        //채팅 기록이 보여지는 채팅창
        textAreaChat.setLineWrap(true);
        textAreaChat.setFont(loadCustomFont("fonts/cuteFont.ttf", Font.PLAIN, 13));
        textAreaChat.setBackground(Color.WHITE);

        //제시어, 준비, 나가기 등이 부착되는 btnPanel
        //제시어 안내 레이블
        labelQuizTitle.setVisible(true);
        labelQuizTitle.setBounds(10,18,100,50);
        labelQuizTitle.setFont(loadCustomFont("fonts/cuteFont.ttf", Font.BOLD, 30));
        labelQuizTitle.setHorizontalAlignment(JLabel.CENTER); // 글자 가운데 정렬

        //실제 제시어가 나오는 레이블
        labelQuizContent.setVisible(false);
        labelQuizContent.setBounds(100,18,100,50);
        labelQuizContent.setFont(loadCustomFont("fonts/cuteFont.ttf", Font.BOLD, 30));
        labelQuizContent.setHorizontalAlignment(JLabel.CENTER); // 글자 가운데 정렬

        //게임 패널 내의 준비 버튼
        btnReady.setBackground(Color.WHITE);
        btnReady.setBounds(485,15,40,40);
        btnReady.setBorderPainted(false);
        btnReady.setFocusPainted(false); // 포커스 제거

        //게임 패널 내의 나가기 버튼
        btnExit.setBackground(Color.WHITE);
        btnExit.setBounds(535,16,40,40);
        btnExit.setBorderPainted(false);
        //end of 제시어, 준비, 나가기 등이 부착되는 btnPanel...

        //게임 패널 내의 드로우 캔버스
        canvasLabel.setBounds(0,0,750,450);
        canvasLabel.setBackground(Color.WHITE);
        drawingBrush.setBounds(0, 0, 750, 450); //이 부분 추후 다시 수정


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
        panelEndGame.add(btnRestart);
        panelEndGame.add(btnEndGame);
        setSize(850, 600);
    }

    private void batch() {
        panelMain.add(btnStart);
        panelMain.add(panelId);
        panelMain.add(panelDrawRoom);
        btnStart.setIcon(icGameStart);

        panelId.add(panelSub);
        panelSub.add(labelUserId);
        panelSub.add(textFieldUserIdInput);
        panelId.add(btnId);
        btnId.setIcon(icGameStart);


        //메인 게임 패널에 붙이는 부분
        panelDrawRoom.add(panelBasePanel,BorderLayout.CENTER); //center, 앞을 drawroom으로 변경  //??????????
        panelDrawRoom.add(panelPalette,BorderLayout.SOUTH); //panelPalette로 변경, south
        panelDrawRoom.add(splitPane,BorderLayout.EAST); //east
        panelDrawRoom.add(btnPanel,BorderLayout.NORTH); //north

        //팔레트에 크레용 및 지우개, 휴지통 버튼 추가
        panelPalette.add(btnBlackDrawPen);
        panelPalette.add(btnRedDrawPen);
        panelPalette.add(btnOrangeDrawPen);
        panelPalette.add(btnYellowDrawPen);
        panelPalette.add(btnGreenDrawPen);
        panelPalette.add(btnBlueDrawPen);
        panelPalette.add(btnIndigoDrawPen);
        panelPalette.add(btnPurpleDrawPen);
        panelPalette.add(btnEraser);
        panelPalette.add(btnDelete);
        panelPalette.setPreferredSize(new Dimension(0, 100)); // 높이 200px로 제한
        panelPalette.setMinimumSize(new Dimension(0, 100));  // 최소 높이 200px

        //BorderLayout의 EAST에 위치하는 JSplitPane
        splitPane.setDividerLocation(70); //h좌표 100에서 나누도록 설정
        splitPane.setDividerSize(0); // Divider 크기 0으로 설정
        splitPane.setBorder(null); // 테두리 제거
        splitPane.setEnabled(false); //SplitPane영역을 사용자가 임의로 움직일 수 없도록 설정
        splitPane.setTopComponent(textAreaUserList);
        splitPane.setBottomComponent(panelChat);
        splitPane.setPreferredSize(new Dimension(180, 0)); // 폭 180px로 제한
        splitPane.setMinimumSize(new Dimension(180, 0));  // 최소 폭 180px

        Component add = panelChat.add(scrollPaneChat);
        panelChat.add(textFieldChatInput);
        panelChat.add(btnSend);

        btnPanel.add(labelQuizTitle);
        btnPanel.add(labelQuizContent);
        btnPanel.add(btnReady);
        btnPanel.add(btnExit);

        panelBasePanel.add(panelMplId);

        // 드로우
        panelMplId.add(canvasLabel);
        panelMplId.add(drawingBrush);

        // 게임 종료 화면을 반드시 마지막에 추가
        panelMain.add(panelEndGame);
        panelMain.setComponentZOrder(panelEndGame, 0); // 최상위로 설정
    }

    private void listener() {
        textFieldChatInput.addActionListener(e -> sendChatMessage());

        btnStart.addActionListener(e -> {
            panelId.setVisible(true);
            panelSub.setVisible(true);
            btnStart.setVisible(false);
            btnId.setVisible(true);
        });

        btnId.addActionListener(e -> {
            initializeConnection();
            sendUserId();
        });

        btnExit.addActionListener(e -> {
            sendExitCommand();
            System.exit(0);
        });

        btnReady.addActionListener(e -> {
            if (!btnReady.isEnabled()) {
                return; // 버튼이 비활성화된 경우 동작하지 않음
            }
            isReady = !isReady; // 준비 상태 토글
//            btnReady.setFocusPainted(false); // 포커스 효과 제거
//            if (isReady) {
//                btnReady.setText("준비 취소"); // 텍스트 변경
//                btnReady.setBackground(Color.ORANGE); // 초록색으로 변경
//                btnReady.setForeground(Color.WHITE); // 텍스트 흰색
//                sendReadyCommand(); // 서버에 준비 상태 전송
//            } else {
//                //준비 취소 이미지로 변경 코드 추후 추가
//                btnReady.setBackground(Color.WHITE); // 기본 배경색으로 변경
//                sendReadyCommand(); // 서버에 준비 취소 상태 전송
//            }
            if (isReady) {
                btnReady.setIcon(ready2Icon); // 준비 완료 이미지로 변경
                btnReady.setBounds(471,5,60,60);
                sendReadyCommand(); // 서버에 준비 상태 전송
            } else {
                btnReady.setIcon(ready1Icon); // 준비 취소 이미지로 변경
                btnReady.setBounds(482,15,40,40);
                sendReadyCommand(); // 서버에 준비 취소 상태 전송
            }
        });

        canvasLabel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (ReaderThread.isDrawingEnabled) {
                    drawCommand = "DRAW&" + e.getX() + "," + e.getY();
                    drawingBrush.setX(e.getX());
                    drawingBrush.setY(e.getY());
                    drawingBrush.repaint();
                    drawingBrush.printAll(imgBuff.getGraphics());
                    outputWriter.println(drawCommand);
                }
            }
        });

        btnBlackDrawPen.addActionListener(e -> updateBrushColor("Black", Color.BLACK));
        btnRedDrawPen.addActionListener(e -> updateBrushColor("Red", Color.RED));
        btnOrangeDrawPen.addActionListener(e -> updateBrushColor("Orange", Color.ORANGE));
        btnYellowDrawPen.addActionListener(e -> updateBrushColor("Yellow", Color.YELLOW));
        btnGreenDrawPen.addActionListener(e -> updateBrushColor("Green", Color.GREEN));
        btnBlueDrawPen.addActionListener(e -> updateBrushColor("Blue", Color.CYAN));
        btnIndigoDrawPen.addActionListener(e -> updateBrushColor("Indigo", Color.BLUE));
        btnPurpleDrawPen.addActionListener(e -> updateBrushColor("Purple", Color.PINK));
        btnEraser.addActionListener(e -> updateBrushColor("White", Color.WHITE));

        btnDelete.addActionListener(e -> {
            colorCommand = "COLOR&Delete";
            outputWriter.println(colorCommand);
            drawingBrush.setShouldClearCanvas(false);
            clearCanvas();
        });

        btnEndGame.addActionListener(e -> System.exit(0)); // 게임 종료 버튼
        btnRestart.addActionListener(e -> resetGameSession());  // 재시작 버튼
    }

    // 게임 종료 화면 표시
    public void showEndGameScreen(String scores) {
        System.out.println("Showing End Game Screen...");

        // 종료 화면 활성화
        panelDrawRoom.setVisible(false); // 게임 화면 비활성화
        panelId.setVisible(false);       // ID 입력 화면 비활성화

        // 종료 화면 패널 크기 설정
        panelEndGame.setBounds(0, 0, 600, 500); // 종료 화면 크기 600x500으로 설정
        panelEndGame.setVisible(true);
        panelEndGame.setBackground(panelMain.getBackground()); // 기존 배경 유지

        // "Game Over!" 텍스트 추가
        JLabel gameOverLabel = new JLabel("Game Over!");
        gameOverLabel.setFont(new Font("맑은 고딕", Font.BOLD, 36));
        gameOverLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gameOverLabel.setBounds(0, 30, 600, 50); // 화면 상단에 중앙 정렬
        panelEndGame.add(gameOverLabel);

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
        JPanel scorePanel = new JPanel();
        scorePanel.setLayout(new GridLayout(scoreList.size() + 1, 3, 10, 10)); // 점수판 레이아웃 설정
        scorePanel.setBounds(100, 100, 400, 200); // 화면 가운데 위치
        scorePanel.setOpaque(false); // 배경 투명

        JLabel rankHeader = new JLabel("랭킹", SwingConstants.CENTER);
        JLabel idHeader = new JLabel("아이디", SwingConstants.CENTER);
        JLabel scoreHeader = new JLabel("점수", SwingConstants.CENTER);
        rankHeader.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        idHeader.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        scoreHeader.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        scorePanel.add(rankHeader);
        scorePanel.add(idHeader);
        scorePanel.add(scoreHeader);

        int rank = 1;
        for (String[] entry : scoreList) {
            String playerId = entry[0].trim();
            String playerScore = entry[1].trim();

            JLabel rankLabel = new JLabel(String.valueOf(rank), SwingConstants.CENTER);
            JLabel idLabel = new JLabel(playerId, SwingConstants.CENTER);
            JLabel scoreLabel = new JLabel(playerScore, SwingConstants.CENTER);
            rankLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
            idLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
            scoreLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
            scorePanel.add(rankLabel);
            scorePanel.add(idLabel);
            scorePanel.add(scoreLabel);

            rank++;
        }

        panelEndGame.add(scorePanel);

        // 버튼 공통 크기 및 스타일 설정
        int buttonWidth = 200;
        int buttonHeight = 50;
        int buttonY = 350;
        int buttonGap = 20;
        Font buttonFont = new Font("맑은 고딕", Font.BOLD, 14);

        // 재시작 버튼
        btnRestart.setText("재시작");
        btnRestart.setBounds(50, buttonY, buttonWidth, buttonHeight);
        btnRestart.setFont(buttonFont);
        btnRestart.setBackground(Color.WHITE);
        btnRestart.setForeground(Color.BLACK);
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

        panelEndGame.add(btnRestart);

        // 종료 버튼
        btnEndGame.setText("게임 종료");
        btnEndGame.setBounds(150 + buttonWidth + buttonGap, buttonY, buttonWidth, buttonHeight);
        btnEndGame.setFont(buttonFont);
        btnEndGame.setBackground(Color.WHITE);
        btnEndGame.setForeground(Color.BLACK);
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

        panelEndGame.add(btnEndGame);

        // 버튼 리스너
        btnEndGame.addActionListener(e -> System.exit(0));
        btnRestart.addActionListener(e -> resetGameSession());

        // 갱신
        panelMain.revalidate();
        panelMain.repaint();

        // 준비 버튼 초기화
        btnReady.setEnabled(true);
        btnReady.setBorderPainted(false);
        btnReady.setBackground(Color.WHITE); // 기본 배경색
        isReady = false;
    }


    // 이미지 크기를 조정하고 고품질로 변환하는 메서드
    private ImageIcon resizeIcon(ImageIcon icon, int width, int height) {
        Image img = icon.getImage();
        Image resizedImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImg);
    }


    // 재시작 메서드
    private void resetGameSession() {
        panelEndGame.setVisible(false); // 종료 화면 숨김
        panelId.setVisible(false);       // 초기화면 활성화
        btnId.setVisible(false);
        btnStart.setVisible(true);   // 시작 버튼 활성화
        panelDrawRoom.setVisible(false); // 그리기 방 비활성화
        resetGameState();            // 게임 상태 초기화

        // **panelEndGame 초기화**
        resetGameOverScreen();
    }
    // panelEndGame 초기화 메서드
    private void resetGameOverScreen() {
        // panelEndGame에 추가된 모든 컴포넌트 제거
        panelEndGame.removeAll();

        // 기본 종료 버튼과 재시작 버튼 다시 추가
        panelEndGame.add(btnRestart);
        panelEndGame.add(btnEndGame);

        // 갱신
        panelEndGame.revalidate();
        panelEndGame.repaint();
    }

    // 게임 상태 초기화
    private void resetGameState() {
        setSize(600, 500);
        drawingBrush.setShouldClearCanvas(true);
        clearCanvas(); // 캔버스 초기화
        textFieldUserIdInput.setText(""); // 아이디 입력 필드 초기화
        textAreaChat.setText(""); // 채팅 창 초기화
        textAreaUserList.setText(""); // 유저 리스트 초기화
        isDrawingEnabled = false; // 그림 그리기 비활성화
        isReady= false;
        readerThread.setUserId("");
    }

    private void updateBrushColor(String colorName, Color color) {
        colorCommand = "COLOR&" + colorName;
        drawingBrush.setColor(color);
        outputWriter.println(colorCommand);
    }

    // 접속 시 서버 연결 메서드.
    private void initializeConnection() {
        try {
            clientSocket = new Socket("localhost", 3000);
            readerThread = new ReaderThread(clientSocket, drawingBrush, textAreaChat, textAreaUserList, scrollPaneChat, labelQuizContent, btnReady, panelBottom, textFieldChatInput, imgBuff, this, userId);
            readerThread.start();

        } catch (Exception e) {
            System.out.println(LOG_TAG + "서버 연결 실패");
        }
    }

    // EXIT 프로토콜 메서드.
    private void sendExitCommand() {
        try {
            outputWriter = new PrintWriter(clientSocket.getOutputStream(), true);
            outputWriter.println("EXIT&" + userId);
        } catch (Exception e) {
            System.out.println(LOG_TAG + "Exit Msg writer fail...");
        }
    }

    // READY 프로토콜 메서드.
    private void sendReadyCommand() {
        try {
            outputWriter = new PrintWriter(clientSocket.getOutputStream(), true);
            outputWriter.println("READY&");
        } catch (Exception e) {
            System.out.println(LOG_TAG + "Ready Msg send fail...");
        }

    }

    // CHAT 프로토콜 메서드.
    private void sendChatMessage() {
        try {
            outputWriter = new PrintWriter(clientSocket.getOutputStream(), true);
            String chatString = textFieldChatInput.getText();
            outputWriter.println("CHAT&" + chatString);
            textFieldChatInput.setText("");
        } catch (Exception e) {
            System.out.println(LOG_TAG + "채팅 메세지 요청 실패");
        }
    }

    // ID 프로토콜 메서드
    private void sendUserId() {
        try {
            outputWriter = new PrintWriter(clientSocket.getOutputStream(), true);
            userId = textFieldUserIdInput.getText();
            System.out.println("userId = " + userId);
            if ((userId.equals(""))) {
                userId = "user" + (int)(Math.random() * 10000); // 랜덤한 더미값 생성
                panelId.setVisible(false); // panelId 비활성화
                panelSub.setVisible(false); // panelId 활성화
                panelDrawRoom.setVisible(true); // panelDrawRoom 활성화
                setSize(600, 500);
            } else { // 아이디 존재
                //outputWriter.println("ID&" + userId);
                textFieldUserIdInput.setText("");
                panelId.setVisible(false); // panelId 비활성화
                panelSub.setVisible(false); // panelId 활성화
                panelDrawRoom.setVisible(true); // panelDrawRoom 활성화
                setSize(600, 500);
            }
            readerThread.setUserId(userId);
            outputWriter.println("ID&" + userId);


        } catch (IOException e) {
            System.out.println(LOG_TAG + "준비 메세지 요청 실패");
        }
    }

    // 드로우 캔버스 초기화 메서드
    private void clearCanvas() {
        drawingBrush.setShouldClearCanvas(false);
        drawingBrush.repaint();
        drawingBrush.printAll(imgBuff.getGraphics());
    }

    public static void main(String[] args) {
        new CatchMindClient2();
    }

    // TTF 폰트를 불러오는 메서드
    private static Font loadCustomFont(String path, int style, int size) {
        try {
            File fontFile = new File(path);
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            return customFont.deriveFont(style, size);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            return new Font("Arial", style, size); // 실패 시 기본 폰트 사용
        }
    }
}
