package client;

import manager.ProblemManager;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

class ReaderThread extends Thread {
    private BufferedReader reader;
    private Socket socket;
    private Brush brush;
    private JTextArea taChat;
    private JTextArea taUserList;
    private JLabel laQuiz;
    private JButton btnReady;
    private JPanel plBottom;
    private TextField tfChat;
    private JScrollPane scrChat;
    private BufferedImage imgBuff;
    private EndGameHandler endGameHandler; // 인터페이스 참조
    public static boolean drawPPAP;
    private int selectProblem = 0;
    private String IDString; // 현재 사용자의 아이디 추가

    public ReaderThread(Socket socket, Brush brush, JTextArea taChat, JTextArea taUserList,
                        JScrollPane scrChat, JLabel laQuiz, JButton btnReady,
                        JPanel plBottom, TextField tfChat, BufferedImage imgBuff, EndGameHandler endGameHandler, String IDString) {
        this.socket = socket;
        this.brush = brush;
        this.taChat = taChat;
        this.taUserList = taUserList; // 추가
        this.scrChat = scrChat; // 추가
        this.laQuiz = laQuiz;
        this.btnReady = btnReady;
        this.plBottom = plBottom;
        this.tfChat = tfChat;
        this.imgBuff = imgBuff;
        this.endGameHandler = endGameHandler; // 인터페이스 설정
        this.IDString = IDString; // 아이디 저장
    }

    public void setIDString(String IDString) {
        System.out.println("IDStringSeet = " + IDString);
        this.IDString = IDString;
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String readerMsg;
            while ((readerMsg = reader.readLine()) != null) {
                processMessage(readerMsg);
            }
        } catch (IOException e) {
            System.out.println("통신 실패");
        }
    }

    private void processMessage(String msg) {
        String[] parsReaderMsg = msg.split("&");

        switch (parsReaderMsg[0]) {
            case "DRAW":
                handleDraw(parsReaderMsg[1]);
                break;
            case "COLOR":
                handleColor(parsReaderMsg[1]);
                break;
            case "SERVER":
                taChat.append("[SERVER]: " + parsReaderMsg[1] + "\n");
                break;
            case "CHAT":
                if (parsReaderMsg.length > 1) taChat.append(parsReaderMsg[1] + "\n");
                break;
            case "START":
                btnReady.setVisible(false);
                break;
            case "ID":
                taUserList.setText("");
                break;
            case "IDLIST":
                // 아이디 목록에서 본인 아이디만 표시
                System.out.println("IDString11 = " + IDString);
                System.out.println("parsReaderMsg.length = " + parsReaderMsg.length);
                System.out.println("parsReaderMsg[1] = " + parsReaderMsg[parsReaderMsg.length-1]);
                taUserList.setText(IDString + "\n");
//                if (parsReaderMsg.length > 1 && parsReaderMsg[1].equals(IDString)) {
//                    System.out.println("sasad");
//                    taUserList.setText(parsReaderMsg[1] + "\n");
//                }
                break;
            case "TURN":
                handleTurn();
                break;
            case "NOTTURN":
                handleNotTurn();
                break;
            case "ANSWER":
                selectNextProblem();
                break;
            case "END":
                handleEnd(parsReaderMsg);
                break;
            default:
                taChat.append("\n");
                break;
        }
        scrChat.getVerticalScrollBar().setValue(scrChat.getVerticalScrollBar().getMaximum());
    }

    private void handleDraw(String drawData) {
        String[] drawM = drawData.split(",");
        brush.setX(Integer.parseInt(drawM[0]));
        brush.setY(Integer.parseInt(drawM[1]));
        brush.repaint();
        brush.printAll(imgBuff.getGraphics());
    }

    private void handleColor(String color) {
        switch (color) {
            case "Black": brush.setColor(Color.BLACK); break;
            case "Red": brush.setColor(Color.RED); break;
            case "Orange": brush.setColor(Color.ORANGE); break;
            case "Yellow": brush.setColor(Color.YELLOW); break;
            case "Green": brush.setColor(Color.GREEN); break;
            case "Blue": brush.setColor(Color.CYAN); break;
            case "Indigo": brush.setColor(Color.BLUE); break;
            case "Purple": brush.setColor(Color.PINK); break;
            case "White": brush.setColor(Color.WHITE); break;
            case "Delete":
                brush.setClearC(false);
                brush.repaint();
                brush.printAll(imgBuff.getGraphics());
                break;
        }
    }

    private void handleTurn() {
        laQuiz.setText(ProblemManager.getProblem(selectProblem));
        laQuiz.setVisible(true);
        drawPPAP = true;
        tfChat.setEnabled(false);
        plBottom.setVisible(true);
    }

    private void handleNotTurn() {
        laQuiz.setVisible(false);
        drawPPAP = false;
        brush.setDrawPen(false);
        tfChat.setEnabled(true);
        plBottom.setVisible(false);
    }

    private void selectNextProblem() {
        selectProblem++;
        if (selectProblem >= ProblemManager.getProblemCount()) {
            selectProblem = 0;
        }
    }

    private void handleEnd(String[] parsReaderMsg) {
        btnReady.setVisible(true);
        tfChat.setEnabled(true);
        plBottom.setVisible(true);
        btnReady.setVisible(true);
        laQuiz.setVisible(false);
        drawPPAP = true;
        // 게임 종료 화면 호출
        // parsReaderMsg[1]부터 시작하여 ID와 점수 데이터를 합쳐서 전달
        StringBuilder scores = new StringBuilder();
        for (int i = 1; i < parsReaderMsg.length; i++) {
            scores.append(parsReaderMsg[i]).append("&");
        }

        // 마지막 & 제거
        if (scores.length() > 0) {
            scores.setLength(scores.length() - 1);
        }

        // 게임 종료 화면 호출
        endGameHandler.showEndGameScreen(scores.toString());
    }
}
