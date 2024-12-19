package client;

import manager.ProblemManager;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

// ReaderThread 클래스: 서버로부터 받은 메시지를 처리하는 스레드 클래스
class ReaderThread extends Thread {
    private BufferedReader reader; // 서버로부터 메시지를 읽기 위한 BufferedReader
    private Socket socket; // 서버와의 소켓 연결
    private Brush brush; // 그림 그리기 도구
    private JTextArea taChat; // 채팅 내용을 표시하는 JTextArea
    private JTextArea taUserList; // 유저 목록을 표시하는 JTextArea
    private JLabel laQuiz; // 퀴즈 내용을 표시하는 JLabel
    private JButton btnReady; // 준비 버튼
    private JPanel plBottom; // 하단 패널
    private TextField tfChat; // 채팅 입력창
    private JScrollPane scrChat; // 채팅 스크롤 패널
    private BufferedImage imgBuff; // 그림을 저장할 BufferedImage
    private EndGameHandler endGameHandler; // 게임 종료 처리 핸들러
    public static boolean drawPPAP; // 현재 사용자가 그림을 그릴 수 있는지 여부
    private int selectProblem = 0; // 선택된 문제 번호
    private String IDString; // 현재 사용자의 아이디

    // 생성자: ReaderThread 객체 초기화
    public ReaderThread(Socket socket, Brush brush, JTextArea taChat, JTextArea taUserList,
                        JScrollPane scrChat, JLabel laQuiz, JButton btnReady,
                        JPanel plBottom, TextField tfChat, BufferedImage imgBuff, EndGameHandler endGameHandler, String IDString) {
        this.socket = socket;
        this.brush = brush;
        this.taChat = taChat;
        this.taUserList = taUserList;
        this.scrChat = scrChat;
        this.laQuiz = laQuiz;
        this.btnReady = btnReady;
        this.plBottom = plBottom;
        this.tfChat = tfChat;
        this.imgBuff = imgBuff;
        this.endGameHandler = endGameHandler;
        this.IDString = IDString; // 아이디 저장
    }

    // 사용자 아이디를 설정하는 메서드
    public void setIDString(String IDString) {
        System.out.println("IDStringSeet = " + IDString);
        this.IDString = IDString;
    }

    @Override
    public void run() {
        try {
            // 서버로부터 메시지를 읽기 위한 스트림 초기화
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String readerMsg;
            // 서버로부터 메시지를 지속적으로 읽어와 처리
            while ((readerMsg = reader.readLine()) != null) {
                processMessage(readerMsg);
            }
        } catch (IOException e) {
            // 통신 실패 시 출력
            System.out.println("통신 실패");
        }
    }

    // 서버로부터 받은 메시지를 처리하는 메서드
    private void processMessage(String msg) {
        String[] parsReaderMsg = msg.split("&"); // 메시지를 & 기준으로 분리

        switch (parsReaderMsg[0]) {
            case "DRAW":
                handleDraw(parsReaderMsg[1]); // DRAW 메시지 처리
                break;
            case "COLOR":
                handleColor(parsReaderMsg[1]); // COLOR 메시지 처리
                break;
            case "SERVER":
                System.out.println("start2 = " + parsReaderMsg);
                taChat.append("[SERVER]: " + parsReaderMsg[1] + "\n"); // 서버 메시지 표시
                break;
            case "CHAT":
                if (parsReaderMsg.length > 1) taChat.append(parsReaderMsg[1] + "\n"); // 채팅 메시지 표시
                break;
            case "START":
                System.out.println("start1 = " + parsReaderMsg);
                btnReady.setEnabled(false); // 준비 버튼 비활성화 추가
                break;
            case "ID":
                taUserList.setText(""); // 유저 목록 초기화
                break;
            case "IDLIST":
                if (parsReaderMsg.length > 1) {
                    String[] userParts = parsReaderMsg[1].split(":");
                    if (userParts.length == 2) {
                        String userID = userParts[0];
                        String userScore = userParts[1];
                        // 자신의 ID와 점수만 업데이트
                        taUserList.setText(userID + "\n정답 : " + userScore);
                    }
                }
                break;

            case "TURN":
                handleTurn(); // 사용자 턴 처리
                break;
            case "NOTTURN":
                handleNotTurn(); // 사용자 턴 종료 처리
                break;
            case "ANSWER":
                selectNextProblem(); // 다음 문제 선택
                break;
            case "END":
                handleEnd(parsReaderMsg); // 게임 종료 처리
                btnReady.setVisible(true);  // 기존 코드
                btnReady.setEnabled(true);  // 준비 버튼 다시 활성화
                break;
            default:
                taChat.append("\n"); // 기타 메시지 처리
                break;
        }
        // 채팅 스크롤을 가장 아래로 설정
        scrChat.getVerticalScrollBar().setValue(scrChat.getVerticalScrollBar().getMaximum());
    }

    // DRAW 메시지를 처리하는 메서드
    private void handleDraw(String drawData) {
        String[] drawM = drawData.split(",");
        brush.setX(Integer.parseInt(drawM[0])); // X 좌표 설정
        brush.setY(Integer.parseInt(drawM[1])); // Y 좌표 설정
        brush.repaint(); // 브러시 다시 그리기
        brush.printAll(imgBuff.getGraphics()); // 그림 버퍼에 저장
    }

    // COLOR 메시지를 처리하는 메서드
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

    // 사용자 턴을 처리하는 메서드
    private void handleTurn() {
        laQuiz.setText(ProblemManager.getProblem(selectProblem)); // 현재 문제 설정
        laQuiz.setVisible(true); // 퀴즈 표시
        drawPPAP = true; // 그림 그리기 활성화
        tfChat.setEnabled(false); // 채팅 입력 비활성화
        plBottom.setVisible(true); // 하단 패널 표시
    }

    // 사용자 턴 종료를 처리하는 메서드
    private void handleNotTurn() {
        laQuiz.setVisible(false); // 퀴즈 숨기기
        drawPPAP = false; // 그림 그리기 비활성화
        brush.setDrawPen(false); // 펜 비활성화
        tfChat.setEnabled(true); // 채팅 입력 활성화
        plBottom.setVisible(false); // 하단 패널 숨기기
    }

    // 다음 문제를 선택하는 메서드
    private void selectNextProblem() {
        selectProblem++;
        if (selectProblem >= ProblemManager.getProblemCount()) {
            selectProblem = 0; // 문제 번호 초기화
        }
    }

    // 게임 종료 메시지를 처리하는 메서드
    private void handleEnd(String[] parsReaderMsg) {
        btnReady.setVisible(true); // 준비 버튼 표시
        tfChat.setEnabled(true); // 채팅 입력 활성화
        plBottom.setVisible(true); // 하단 패널 표시
        btnReady.setVisible(true);
        laQuiz.setVisible(false); // 퀴즈 숨기기
        drawPPAP = true; // 그림 그리기 활성화

        // 점수 데이터 생성
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
