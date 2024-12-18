package server;

import manager.ProblemManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;


public class CatchMindServer {
    private static final String LOG_TAG = "CatchMindServer: ";
   //private Vector<ClientHandler> connectedClients;

    public static List<ClientHandler> connectedClients = Collections.synchronizedList(new ArrayList<>());
    private ServerSocket server;

    private int readyCount = 0;
    private boolean gameStarted = false;
    private int currentWordIndex = 0;

    public CatchMindServer() {
        try {
            server = new ServerSocket(3000);
            connectedClients = new Vector<>();
            while (true) {
                System.out.println("Waiting for client connections...");
                Socket clientSocket = server.accept();
                System.out.println("Client connected successfully.");
                ClientHandler client = new ClientHandler(clientSocket);
                client.start();
                connectedClients.add(client);
            }
        } catch (Exception e) {
            System.out.println(LOG_TAG + "Server connection failed.");
        }
    }
    class ClientHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter output;
        private BufferedReader input;

        private boolean ready = false;
        private boolean gameEnded = false;
        private boolean answeredCorrectly = false;
        private int turnIndex = 0;
        private int currentTurn = 1;

        private String clientID;
        private int score = 0;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                output = new PrintWriter(clientSocket.getOutputStream(), true);
                String receivedMessage;

                while ((receivedMessage = input.readLine()) != null) {
                    String[] parsedMessage = receivedMessage.split("&");
                    handleProtocol(parsedMessage);
                }
            } catch (Exception e) {
                System.out.println(LOG_TAG + "Communication error.");
            }
        }

        private void handleProtocol(String[] parsedMessage) {
            switch (parsedMessage[0]) {
                case "ID":
                    processClientID(parsedMessage);
                    break;
                case "CHAT":
                    processChat(parsedMessage);
                    break;
                case "READY":
                    toggleReadyStatus();
                    break;
                case "START":
                    startGame();
                    break;
                case "TURN":
                    processTurn();
                    break;
                case "DRAW":
                    forwardDrawing(parsedMessage);
                    break;
                case "COLOR":
                    forwardColorChange(parsedMessage);
                    break;
                case "SKIP":
                    skipTurn(parsedMessage);
                    break;
                case "EXIT":
                    handleClientExit(parsedMessage);
                    break;
            }
        }

        private void processClientID(String[] parsedMessage) {
            clientID = parsedMessage[1];
            broadcastMessage("SERVER", "[" + clientID + "] has entered the room.");
            assignTurnsAndScores();
        }

        private void broadcastMessage(String prefix, String message) {
            for (ClientHandler client : connectedClients) {
                client.output.println(prefix + "&" + message);
            }
        }

        private void assignTurnsAndScores() {
            for (int i = 0; i < connectedClients.size(); i++) {
                ClientHandler client = connectedClients.get(i);
                client.output.println("ID&");
                client.turnIndex = i + 1;
                client.score = 0;
            }
            sendClientList();
        }

        private void sendClientList() {
            for (ClientHandler client : connectedClients) {
                for (ClientHandler otherClient : connectedClients) {
                    client.output.println("IDLIST&[" + otherClient.clientID + "]");
                }
            }
        }

        private void processChat(String[] parsedMessage) {
            if (parsedMessage.length > 1) {
                String chatMessage = parsedMessage[1];
                broadcastMessage("CHAT", "[" + clientID + "]: " + chatMessage);
                checkAnswer(chatMessage); // 채팅 메시지를 정답 체크 메서드로 전달
            }
        }


        private void checkAnswer(String answer) {
            String currentWord = ProblemManager.getProblem(currentWordIndex);
            if (gameStarted && answer.equals(currentWord)) {
                broadcastMessage("SERVER", "[" + clientID + "] guessed the word correctly: [" + currentWord + "]");
                increaseScore();
                // 모든 클라이언트에게 정답 처리 완료 알림
                for (ClientHandler client : connectedClients) {
                    client.output.println("ANSWER&" + currentWord);
                }

                assignNextTurnToCorrectAnswerer(this); // 정답자에게 다음 턴 권한
                moveToNextWord();
                // 다음 턴으로 이동
                answeredCorrectly = true;  // 상태 업데이트
                processTurn();
            }
        }
        private void assignNextTurnToCorrectAnswerer(ClientHandler correctClient) {
            // 정답을 맞춘 클라이언트를 다음 턴으로 설정
            currentTurn = correctClient.turnIndex;
        }

        private void increaseScore() {
            for (ClientHandler client : connectedClients) {
                if (client.clientID.equals(clientID)) {
                    client.score++;
                    client.output.println("SERVER&Your current score: " + client.score);
                    break;
                }
            }
        }

        private void moveToNextWord() {
            currentWordIndex++;
            if (currentWordIndex >= ProblemManager.getProblemCount()) {
                currentWordIndex = 0; // 제시어 순환
            }

//            if (currentTurn > connectedClients.size()) {
//                currentTurn = 1; // 턴 초기화
//            }
//
            // 게임 종료 조건
            if (connectedClients.size() < 2 || currentWordIndex==0) {
                gameEnded = true;
                System.out.println("endGame1");
                endGame();
            }
        }


        private void toggleReadyStatus() {
            ready = !ready; // 준비 상태 토글
            if (ready) {
                readyCount++;
                broadcastMessage("SERVER", "[" + clientID + "] is ready.");
            } else {
                readyCount--;
                broadcastMessage("SERVER", "[" + clientID + "] is not ready.");
            }
            System.out.println("readyCount = " + readyCount);
            // Ready 상태 확인 후 바로 게임 시작 체크
            if (readyCount == connectedClients.size() && !gameStarted) {
                startGame();
            }
        }


        private void startGame() {
            if (readyCount == connectedClients.size() && !gameStarted) {
                System.out.println("StartGame!! = " + clientSocket);
                gameStarted = true; // 게임 시작 상태 업데이트
                broadcastMessage("SERVER", "Game is starting!");
                processTurn(); // 첫 턴 시작
            }
        }


        private void processTurn() {
            if (gameStarted) { // answeredCorrectly 제거: 첫 턴도 처리해야 하기 때문
//                for (ClientHandler client : connectedClients) {
//                    client.output.println("NOTTURN&"); // 모두에게 NOTTURN 전송
//                }
                for (ClientHandler client : connectedClients) {
                    if (client.turnIndex == currentTurn) { // 현재 턴의 클라이언트 찾기
                        String currentWord = ProblemManager.getProblem(currentWordIndex);
                        client.output.println("TURN&" + currentWord); // TURN과 제시어 전송
                        broadcastMessage("SERVER", "[" + client.clientID + "] is taking their turn.");
                        //break;
                    }else{
                        client.output.println("NOTTURN&"); // NOTTURN 전송
                    }
                }
//                currentTurn++; // 턴을 다음 클라이언트로 이동
//                if (currentTurn > connectedClients.size()) {
//                    currentTurn = 1; // 턴이 마지막 클라이언트를 넘어가면 초기화
//                }
//
                // 상태 초기화
                answeredCorrectly = false;
            }
        }


        private void forwardDrawing(String[] parsedMessage) {
            for (ClientHandler client : connectedClients) {
                if (client != this) {
                    client.output.println("DRAW&" + parsedMessage[1]);
                }
            }
        }

        private void forwardColorChange(String[] parsedMessage) {
            for (ClientHandler client : connectedClients) {
                if (client != this) {
                    client.output.println("COLOR&" + parsedMessage[1]);
                }
            }
        }

        private void skipTurn(String[] parsedMessage) {
            answeredCorrectly = true;
            moveToNextWord();
            broadcastMessage("SERVER", "Turn skipped.");
        }

        private void handleClientExit(String[] parsedMessage) {
            broadcastMessage("SERVER", "[" + clientID + "] has left the game.");
            if (ready) {
                readyCount--; // 나가기 전에 준비 상태였다면 readyCount 감소
            }
            connectedClients.remove(this);
            if (gameStarted && connectedClients.size() < 2) {
                gameStarted = false;
                broadcastMessage("SERVER", "Not enough players. Game stopped.");
            }
        }


        private String generateScoreBoard() {
            StringBuilder scoreBoard = new StringBuilder();
            for (ClientHandler client : connectedClients) {
                scoreBoard.append(client.clientID)
                        .append(": ")
                        .append(client.score)
                        .append("\n");
            }
            return scoreBoard.toString();
        }

        private void endGame() {
            System.out.println("endGame2");

//            String scoreBoard = generateScoreBoard();
//            broadcastMessage("END", scoreBoard);
//            broadcastMessage("SERVER", "Game over! Displaying results...");
//            for (ClientHandler client : connectedClients) {
//                client.resetGameState();
//            }
            String scoreBoard = generateScoreBoard();

            // 게임 종료 메시지 전송
            for (ClientHandler client : connectedClients) {
                client.output.println("END&" + scoreBoard.replace("\n", "&"));
            }

            // 서버 상태 초기화
            resetGameState();
        }

        private void resetGameState() {
            gameStarted = false;
            gameEnded = false;
            ready = false;
            answeredCorrectly = false;
            readyCount = 0;
            currentTurn = 1;
            score = 0;
        }
    }

    public static void main(String[] args) {
        new CatchMindServer();
    }
}
