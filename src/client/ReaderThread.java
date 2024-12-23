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
    private BufferedReader inputReader; // 서버로부터 메시지를 읽기 위한 BufferedReader
    private Socket clientSocket; // 서버와의 소켓 연결
    private Brush drawingBrush; // 그림 그리기 도구
    private JTextArea textAreaChat, textAreaUserList; // 유저 목록을 표시하는 JTextArea
    private JLabel labelQuizContent; // 퀴즈 내용을 표시하는 JLabel
    private JButton btnReady; // 준비 버튼
    private JPanel panelBottom; // 하단 패널
    private TextField textFieldChatInput; // 채팅 입력창
    private JScrollPane scrollPaneChat; // 채팅 스크롤 패널
    private BufferedImage imageBuffer; // 그림을 저장할 BufferedImage
    private EndGameHandler endGameHandler; // 게임 종료 처리 핸들러
    public static boolean isDrawingEnabled; // 현재 사용자가 그림을 그릴 수 있는지 여부
    private int selectProblem = 0; // 선택된 문제 번호
    private String userId; // 현재 사용자의 아이디

    // 생성자: ReaderThread 객체 초기화
    public ReaderThread(Socket clientSocket, Brush drawingBrush, JTextArea textAreaChat, JTextArea textAreaUserList,
                        JScrollPane scrollPaneChat, JLabel labelQuizContent, JButton btnReady,
                        JPanel panelBottom, TextField textFieldChatInput, BufferedImage imageBuffer, EndGameHandler endGameHandler, String userId) {
        this.clientSocket = clientSocket;
        this.drawingBrush = drawingBrush;
        this.textAreaChat = textAreaChat;
        this.textAreaUserList = textAreaUserList;
        this.scrollPaneChat = scrollPaneChat;
        this.labelQuizContent = labelQuizContent;
        this.btnReady = btnReady;
        this.panelBottom = panelBottom;
        this.textFieldChatInput = textFieldChatInput;
        this.imageBuffer = imageBuffer;
        this.endGameHandler = endGameHandler;
        this.userId = userId; // 아이디 저장
    }

    // 사용자 아이디를 설정하는 메서드
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public void run() {
        try {
            // 서버로부터 메시지를 읽기 위한 스트림 초기화
            inputReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String readerMsg;
            // 서버로부터 메시지를 지속적으로 읽어와 처리
            while ((readerMsg = inputReader.readLine()) != null) {
                processMessage(readerMsg);
            }
        } catch (IOException e) {
            // 통신 실패 시 출력
            System.out.println("통신 실패");
        }
    }

    // 서버로부터 받은 메시지를 처리하는 메서드
    private void processMessage(String msg) {
        String[] parseMessageReader = msg.split("&"); // 메시지를 & 기준으로 분리

        switch (parseMessageReader[0]) {
            case "DRAW":
                handleDraw(parseMessageReader[1]); // DRAW 메시지 처리
                break;
            case "COLOR":
                handleColor(parseMessageReader[1]); // COLOR 메시지 처리
                break;
            case "SERVER":
                System.out.println("start2 = " + parseMessageReader);
                textAreaChat.append("[SERVER]: " + parseMessageReader[1] + "\n"); // 서버 메시지 표시
                break;
            case "CHAT":
                if (parseMessageReader.length > 1) textAreaChat.append(parseMessageReader[1] + "\n"); // 채팅 메시지 표시
                break;
            case "START":
                System.out.println("start1 = " + parseMessageReader);
                btnReady.setEnabled(false); // 준비 버튼 비활성화 추가
                break;
            case "ID":
                textAreaUserList.setText(""); // 유저 목록 초기화
                break;
            case "IDLIST":
                if (parseMessageReader.length > 1) {
                    String[] userParts = parseMessageReader[1].split(":");
                    if (userParts.length == 2) {
                        String userID = userParts[0];
                        String userScore = userParts[1];
                        // 자신의 ID와 점수만 업데이트
                        textAreaUserList.setText(userID + "\n정답 : " + userScore);
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
                handleEnd(parseMessageReader); // 게임 종료 처리
                btnReady.setVisible(true);  // 기존 코드
                btnReady.setEnabled(true);  // 준비 버튼 다시 활성화
                break;
            default:
                textAreaChat.append("\n"); // 기타 메시지 처리
                break;
        }
        // 채팅 스크롤을 가장 아래로 설정
        scrollPaneChat.getVerticalScrollBar().setValue(scrollPaneChat.getVerticalScrollBar().getMaximum());
    }

    // DRAW 메시지를 처리하는 메서드
    private void handleDraw(String drawData) {
        String[] drawM = drawData.split(",");
        drawingBrush.setX(Integer.parseInt(drawM[0])); // X 좌표 설정
        drawingBrush.setY(Integer.parseInt(drawM[1])); // Y 좌표 설정
        drawingBrush.repaint(); // 브러시 다시 그리기
        drawingBrush.printAll(imageBuffer.getGraphics()); // 그림 버퍼에 저장
    }

    // COLOR 메시지를 처리하는 메서드
    private void handleColor(String color) {
        switch (color) {
            case "Black": drawingBrush.setColor(Color.BLACK); break;
            case "Red": drawingBrush.setColor(Color.RED); break;
            case "Orange": drawingBrush.setColor(Color.ORANGE); break;
            case "Yellow": drawingBrush.setColor(Color.YELLOW); break;
            case "Green": drawingBrush.setColor(Color.GREEN); break;
            case "Blue": drawingBrush.setColor(Color.CYAN); break;
            case "Indigo": drawingBrush.setColor(Color.BLUE); break;
            case "Purple": drawingBrush.setColor(Color.PINK); break;
            case "White": drawingBrush.setColor(Color.WHITE); break;
            case "Delete":
                drawingBrush.setShouldClearCanvas(false);
                drawingBrush.repaint();
                drawingBrush.printAll(imageBuffer.getGraphics());
                break;
        }
    }

    // 사용자 턴을 처리하는 메서드
    private void handleTurn() {
        labelQuizContent.setText(ProblemManager.getProblem(selectProblem)); // 현재 문제 설정
        labelQuizContent.setVisible(true); // 퀴즈 표시
        isDrawingEnabled = true; // 그림 그리기 활성화
        textFieldChatInput.setEnabled(false); // 채팅 입력 비활성화
        panelBottom.setVisible(true); // 하단 패널 표시
        handleColor("Delete"); // 그림판 초기화
    }

    // 사용자 턴 종료를 처리하는 메서드
    private void handleNotTurn() {
        labelQuizContent.setVisible(false); // 퀴즈 숨기기
        isDrawingEnabled = false; // 그림 그리기 비활성화
        drawingBrush.setDrawingEnabled(false); // 펜 비활성화
        textFieldChatInput.setEnabled(true); // 채팅 입력 활성화
        panelBottom.setVisible(false); // 하단 패널 숨기기
        handleColor("Delete"); // 그림판 초기화
    }

    // 다음 문제를 선택하는 메서드
    private void selectNextProblem() {
        selectProblem++;
        if (selectProblem >= ProblemManager.getProblemCount()) {
            selectProblem = 0; // 문제 번호 초기화
        }
    }

    // 게임 종료 메시지를 처리하는 메서드
    private void handleEnd(String[] parseMessageReader) {
        btnReady.setVisible(true); // 준비 버튼 표시
        textFieldChatInput.setEnabled(true); // 채팅 입력 활성화
        panelBottom.setVisible(true); // 하단 패널 표시
        btnReady.setVisible(true);
        labelQuizContent.setVisible(false); // 퀴즈 숨기기
        isDrawingEnabled = true; // 그림 그리기 활성화

        // 점수 데이터 생성
        StringBuilder scores = new StringBuilder();
        for (int i = 1; i < parseMessageReader.length; i++) {
            scores.append(parseMessageReader[i]).append("&");
        }

        // 마지막 & 제거
        if (scores.length() > 0) {
            scores.setLength(scores.length() - 1);
        }

        // 게임 종료 화면 호출
        endGameHandler.showEndGameScreen(scores.toString());
    }
}
