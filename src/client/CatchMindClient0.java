package client;

import client.components.*;
import manager.ProblemManager;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class CatchMindClient0 extends JFrame implements EndGameHandler{
    private static final String TAG = "GameStart :";
    private String IDString;
    public String[] problem = ProblemManager.getProblems();
    public int selectProblem = 0;

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
    private JButton btnId, btnSkip, btnReady, btnExit, btnEndGame, btnRestart;

    private Font ftSmall, ftMedium, ftLarge;
    //int x, y;

    private BufferedImage imgBuff;
    private JLabel drawLabel;
    private Brush brush;
    private ImageIcon endGameBg, endGameBtnIcon, restartBtnIcon;
    String sendDraw, sendColor;
    public static boolean drawPPAP = true;

    public CatchMindClient0() {
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


        // 종료 화면 배경 및 버튼 이미지 설정
        endGameBg = new ImageIcon("img/endGame.png");
        endGameBtnIcon = new ImageIcon("img/endGameBtn.png");
        restartBtnIcon = new ImageIcon("img/restartBtn.png");
        plEndGame = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(endGameBg.getImage(), 0, 0, getWidth(), getHeight(), null);
            }
        };
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
        btnSkip = new JButton("넘기기");
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
        btnStart.setBounds(300, 360, 180, 110); // btnStart 위치, 크기 조정 (x, y, width, height)
        btnStart.setBorderPainted(false); // 버튼 테두리 제거

        icGameStart = new ImageIcon("img/gameStart.png"); // 게임시작 버튼 이미지
        // plId
        plId.setLayout(null);
        plId.setVisible(false); // 비활성화
        plId.setBackground(new Color(242, 242, 242));
        plId.setBounds(180, 200, 420, 300); // plId 위치, 크기 조정 (x, y, width, height) 좌표는 plMain 기준

        plSub.setLayout(null);
        plSub.setVisible(false); // 비활성화
        plSub.setBorder(new LineBorder(new Color(87, 87, 87), 3, true));
        plSub.setBounds(90, 50, 246, 36); // plId 위치, 크기 조정 (x, y, width, height) 좌표는 plMain 기준

        laId.setBounds(0, 2, 62, 32); // laId 위치, 크기 조정 (x, y, width, height) 좌표는 plId 기준
        laId.setBorder(new LineBorder(new Color(87, 87, 87), 2, true));
        laId.setFont(ftSmall);
        laId.setHorizontalAlignment(JLabel.CENTER); // 글자 가운데 정렬

        tfIdInput.setBounds(63, 3, 180, 30); // tfIdInput 위치, 크기 조정 (x, y, width, height) 좌표는 plId 기준
        tfIdInput.setBackground(new Color(242, 242, 242, 255));
        tfIdInput.setFont(ftMedium);

        btnId.setBounds(120, 150, 180, 110); // btnId 위치, 크기 조정 (x, y, width, height) 좌표는 plId 기준
        btnId.setBorderPainted(false); // 버튼 테두리 제거

        // plDrawRoom
        plDrawRoom.setLayout(null);
        plDrawRoom.setVisible(false); // 비활성화
        plDrawRoom.setBounds(70, 120, 1005, 660);// plDrawRoom 위치, 크기 조정 좌표는 plMain 기준

        // plDrawRoom - plTopMpId
        plTopMpId.setLayout(null);
        plTopMpId.setBackground(new Color(255, 255, 255, 255));
        plTopMpId.setBounds(0, 0, 750, 530);

        // plDrawRoom - plTop
        plTop.setLayout(null);
        plTop.setBackground(new Color(255, 255, 255, 0));
        plTop.setBounds(0, 0, 750, 80); // plTop 위치, 크기 조정 좌표는 plDrawRoom 기준

        // plDrawRoom - plMplId
        plMplId.setLayout(null);
        plMplId.setBackground(new Color(255, 255, 255, 255));
        plMplId.setBounds(0, 110, 750, 450); // plMplId 위치, 크기 조정 좌표는 plDrawRoom 기준

        // plDrawRoom - plBottom
        plBottom.setLayout(null);
        plBottom.setBackground(new Color(242, 242, 242, 255));
        plBottom.setBounds(0, 530, 700, 130); // plBottom 위치, 크기 조정 좌표는 plDrawRoom 기준

        iconBlackPen = new ImageIcon("img/drawBlackPen.png");
        iconRedPen = new ImageIcon("img/drawRedPen.png");
        iconOrangePen = new ImageIcon("img/drawOrangePen.png");
        iconYellowPen = new ImageIcon("img/drawYellowPen.png");
        iconGreenPen = new ImageIcon("img/drawGreenPen.png");
        iconBluePen = new ImageIcon("img/drawBluePen.png");
        iconIndigoPen = new ImageIcon("img/drawIndigoPen.png");
        iconPurplePen = new ImageIcon("img/drawPurplePen.png");

        btnBlackDrawPen.setBackground(new Color(242, 242, 242, 255));
        btnBlackDrawPen.setBounds(0, 0, 65, 130);
        btnBlackDrawPen.setBorderPainted(false); // 버튼 테두리 제거

        btnRedDrawPen.setBackground(new Color(242, 242, 242, 255));
        btnRedDrawPen.setBounds(65, 0, 65, 130);
        btnRedDrawPen.setBorderPainted(false); // 버튼 테두리 제거

        btnOrangeDrawPen.setBackground(new Color(242, 242, 242, 255));
        btnOrangeDrawPen.setBounds(130, 0, 65, 130);
        btnOrangeDrawPen.setBorderPainted(false); // 버튼 테두리 제거

        btnYellowDrawPen.setBackground(new Color(242, 242, 242, 255));
        btnYellowDrawPen.setBounds(195, 0, 65, 130);
        btnYellowDrawPen.setBorderPainted(false); // 버튼 테두리 제거

        btnGreenDrawPen.setBackground(new Color(242, 242, 242, 255));
        btnGreenDrawPen.setBounds(260, 0, 65, 130);
        btnGreenDrawPen.setBorderPainted(false); // 버튼 테두리 제거

        btnBlueDrawPen.setBackground(new Color(242, 242, 242, 255));
        btnBlueDrawPen.setBounds(325, 0, 65, 130);
        btnBlueDrawPen.setBorderPainted(false); // 버튼 테두리 제거

        btnIndigoDrawPen.setBackground(new Color(242, 242, 242, 255));
        btnIndigoDrawPen.setBounds(390, 0, 65, 130);
        btnIndigoDrawPen.setBorderPainted(false); // 버튼 테두리 제거

        btnPurpleDrawPen.setBackground(new Color(242, 242, 242, 255));
        btnPurpleDrawPen.setBounds(455, 0, 65, 130);
        btnPurpleDrawPen.setBorderPainted(false); // 버튼 테두리 제거

        // plDrawRoom - plEast
        plEast.setLayout(null);
        plEast.setBounds(750, 0, 255, 530); // plEast 위치, 크기 조정 좌표는 plDrawRoom 기준

        // plDrawRoom - plChat
        plChat.setLayout(null);

        // plDrawRoom - btnPanel
        btnPanel.setLayout(null);
        btnPanel.setBackground(new Color(242, 242, 242, 255));
        btnPanel.setBounds(700, 530, 405, 130);

        // plBottom
        plPalette.setLayout(null);
        plPalette.setBackground(new Color(242, 242, 242, 255));
        plPalette.setBounds(0, 0, 520, 130); // plPalette 위치, 크기 조정 좌표는 plBottom 기준

        btnEraser.setBackground(new Color(242, 242, 242, 255));
        btnEraser.setBounds(520, 0, 80, 130); // btnEraser 위치, 크기 조정 좌표는 plBottom 기준

        btnDelete.setBackground(new Color(242, 242, 242, 255));
        btnDelete.setBounds(600, 0, 100, 130); // btnEraser 위치, 크기 조정 좌표는 plBottom 기준

        // plEast
        taUserList.setBounds(0, 0, 255, 150); // taUserList 위치, 크기 조정 좌표는 plEast 기준
        taUserList.setFont(ftMedium);
        taUserList.setBackground(new Color(242, 242, 242, 255));
        taUserList.setLineWrap(true);

        plChat.setBackground(Color.WHITE);
        plChat.setBounds(0, 150, 255, 385); // plChat 위치, 크기 조정 좌표는 plEast 기준

        // plEast - plChat
        tfChat.setBackground(Color.WHITE);
        tfChat.setBounds(0, 350, 255, 30); // tfChat 위치, 크기 조정 좌표는 plEast 기준
        tfChat.setFont(ftMedium);
        tfChat.setBackground(new Color(242, 242, 242, 255));
        tfChat.setColumns(30);

        scrChat.setBounds(0, 0, 255, 350); // taChat 위치, 크기 조정 좌표는 plEast 기준
        scrChat.setFocusable(false);

        taChat.setLineWrap(true);
        taChat.setBackground(new Color(242, 242, 242, 255));

        // btnPanel
        laQuizTitle.setVisible(true);
        laQuizTitle.setBounds(0, 2, 155, 65); // laQuiz 위치, 크기 조정 좌표는 plTop 기준
        laQuizTitle.setFont(ftMedium);
        laQuizTitle.setBackground(new Color(242, 242, 242, 255));
        laQuizTitle.setHorizontalAlignment(JLabel.CENTER); // 글자 가운데 정렬

        laQuiz.setVisible(false);
        laQuiz.setBounds(0, 67, 155, 65); // laQuiz 위치, 크기 조정 좌표는 plTop 기준
        laQuiz.setFont(ftMedium);
        laQuiz.setBackground(new Color(242, 242, 242, 255));
        laQuiz.setHorizontalAlignment(JLabel.CENTER); // 글자 가운데 정렬

        btnReady.setBounds(150, 2, 155, 65); // btnReady 위치, 크기 조정 좌표는 plEast 기준
        btnReady.setFont(ftMedium);
        btnReady.setBackground(new Color(242, 242, 242, 255));
        btnReady.setBorder(new LineBorder(new Color(87, 87, 87), 5, true));

        btnSkip.setVisible(false);
        btnSkip.setBounds(150, 2, 155, 65); // btnSkip 위치, 크기 조정 좌표는 plTop 기준
        btnSkip.setFont(ftMedium);
        btnSkip.setBackground(new Color(242, 242, 242, 255));
        btnSkip.setBorder(new LineBorder(new Color(87, 87, 87), 5, true));

        btnExit.setBounds(150, 62, 155, 65); // btnExit 위치, 크기 조정 좌표는 plEast 기준
        btnExit.setFont(ftMedium);
        btnExit.setBackground(new Color(242, 242, 242, 255));
        btnExit.setBorder(new LineBorder(new Color(87, 87, 87), 5, true));

        // 드로우 캔버스
        drawLabel.setBounds(0, 0, 750, 450);
        drawLabel.setBackground(new Color(255, 255, 255, 0));
        brush.setBounds(0, 0, 750, 450);


        // 종료 버튼
        btnEndGame = new JButton(endGameBtnIcon);
        btnEndGame.setBounds(300, 400, 200, 60);
        btnEndGame.setBorderPainted(false);
        btnEndGame.setContentAreaFilled(false);
        btnEndGame.setFocusPainted(false);

        // 재시작 버튼
        btnRestart = new JButton(restartBtnIcon);
        btnRestart.setBounds(300, 300, 200, 60);
        btnRestart.setBorderPainted(false);
        btnRestart.setContentAreaFilled(false);
        btnRestart.setFocusPainted(false);

        // 버튼 추가
        plEndGame.add(btnRestart);
        plEndGame.add(btnEndGame);
        setSize(800, 640);
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
        plBottom.add(btnEraser);
        plBottom.add(btnDelete);

        plPalette.add(btnBlackDrawPen);
        plPalette.add(btnRedDrawPen);
        plPalette.add(btnOrangeDrawPen);
        plPalette.add(btnYellowDrawPen);
        plPalette.add(btnGreenDrawPen);
        plPalette.add(btnBlueDrawPen);
        plPalette.add(btnIndigoDrawPen);
        plPalette.add(btnPurpleDrawPen);

        plEast.add(plChat);
        plEast.add(taUserList);

        plChat.add(scrChat);
        plChat.add(tfChat);

        btnPanel.add(laQuiz);
        btnPanel.add(laQuizTitle);
        btnPanel.add(btnReady);
        btnPanel.add(btnSkip);
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
        });

        btnId.addActionListener(e -> {
            connectServer();
            sendInsertId();
        });

        btnExit.addActionListener(e -> {
            sendExit();
            System.exit(0);
        });

        btnReady.addActionListener(e -> sendReady());

        btnSkip.addActionListener(e -> sendSkip());

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
        plEndGame.setVisible(true);   // 종료 화면 활성화

        // 점수 데이터 표시
        lbScores.setText("<html><div style='text-align:center;'>" + scores.replace("\n", "<br>") + "</div></html>");
        plEndGame.add(lbScores);

        // 종료 버튼 리스너
        btnEndGame.addActionListener(e -> System.exit(0));

        // 재시작 버튼 리스너
        btnRestart.addActionListener(e -> restartGame());

        // 갱신
        plMain.revalidate();
        plMain.repaint();
    }

    // 재시작 메서드
    private void restartGame() {
        plEndGame.setVisible(false); // 종료 화면 숨김
        plId.setVisible(true);       // 초기화면 활성화
        btnStart.setVisible(true);   // 시작 버튼 활성화
        plDrawRoom.setVisible(false); // 그리기 방 비활성화
        resetGameState();            // 게임 상태 초기화
    }
    // 게임 상태 초기화
    private void resetGameState() {
        brush.setClearC(true);
        cleanDraw(); // 캔버스 초기화
        tfIdInput.setText(""); // 아이디 입력 필드 초기화
        taChat.setText(""); // 채팅 창 초기화
        taUserList.setText(""); // 유저 리스트 초기화
        drawPPAP = false; // 그림 그리기 비활성화
    }

    // 서버로부터 종료 메시지를 받아 화면 표시
    private void handleServerMessage(String[] message) {
        if (message[0].equals("END")) {
            System.out.println("message11 = " + message);
            // 서버로부터 점수 데이터를 받아와 화면에 표시
            StringBuilder scores = new StringBuilder("<html><div style='text-align:center;'>게임 종료!<br/>");
            for (int i = 1; i < message.length; i++) {
                scores.append(message[i]).append("<br/>");
            }
            scores.append("</div></html>");

            // 종료 화면 활성화
            showEndGameScreen(scores.toString());
        }
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
            new ReaderThread(socket, brush, taChat, taUserList, scrChat, laQuiz, btnReady, btnSkip, plBottom, tfChat, imgBuff,this).start();

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

    // SKIP 프로토콜 메서드.
    private void sendSkip() {
        try {
            writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println("SKIP&");
        } catch (Exception e) {
            System.out.println(TAG + "Skip Msg writer fail...");
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
            if ((IDString.equals(""))) { // NULL값 입력시
                IDString = "emptyID";
                writer.println("ID&" + IDString);
                plId.setVisible(false); // plId 비활성화
                plSub.setVisible(false); // plId 활성화
                plDrawRoom.setVisible(true); // plDrawRoom 활성화
                setSize(1152, 864);
            } else { // 아이디 값 입력시.
                writer.println("ID&" + IDString);
                tfIdInput.setText("");
                plId.setVisible(false); // plId 비활성화
                plSub.setVisible(false); // plId 활성화
                plDrawRoom.setVisible(true); // plDrawRoom 활성화
                setSize(1152, 864);
            }

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
        new CatchMindClient0();
    }
}
