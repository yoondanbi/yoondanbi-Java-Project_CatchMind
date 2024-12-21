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
    private static final String LOG_TAG = "twoEZ - catchMind: ";
    private static final int PORT = 3000;
    public static List<ClientHandler> connectedClients = Collections.synchronizedList(new ArrayList<>());
    private ServerSocket server;
    private int readyCount = 0;
    private boolean gameStarted = false;
    private int currentWordIndex = 0;

    public CatchMindServer() {
        try {
            initializeServer();
            handleClientConnections();
        } catch (IOException e) {
            System.err.println(LOG_TAG + "Server initialization or connection failed: " + e.getMessage());
        }
    }

    private void initializeServer() throws IOException {
        server = new ServerSocket(PORT);
        connectedClients = new Vector<>();
        System.out.println(LOG_TAG + "Server started on port " + PORT);
    }

    private void handleClientConnections() throws IOException {
        while (true) {
            Socket clientSocket = server.accept();
            addClient(clientSocket);
        }
    }

    private void addClient(Socket clientSocket) {
        ClientHandler client = new ClientHandler(clientSocket);
        client.start();
        connectedClients.add(client);
        System.out.println(LOG_TAG + "New client connected: " + clientSocket.getInetAddress());
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
            if (parsedMessage == null || parsedMessage.length == 0) {
                System.err.println("Invalid protocol message received.");
                return;
            }

            String command = parsedMessage[0];

            switch (command) {
                case "ID":
                    processClientID(parsedMessage);
                    break;
                case "CHAT":
                    processChat(parsedMessage);
                    break;
                case "START":
                    startGame();
                    break;
                case "READY":
                    toggleReadyStatus();
                    break;
                case "TURN":
                    processTurn();
                    break;
                case "COLOR":
                    forwardColorChange(parsedMessage);
                    break;
                case "DRAW":
                    forwardDrawing(parsedMessage);
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
                initializeClient(connectedClients.get(i), i + 1);
            }
            sendClientList();
        }

        private void initializeClient(ClientHandler client, int turnIndex) {
            client.output.println("ID&");
            client.turnIndex = turnIndex;
            client.score = 0;
        }

        private void sendClientList() {
            for (ClientHandler client : connectedClients) {
                String clientInfo = "IDLIST&" + client.clientID + ":" + client.score;
                client.output.println(clientInfo);
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
               // score++; // 정답 맞춘 클라이언트의 점수 증가
                broadcastMessage("SERVER", "[" + clientID + "] 점수: " + score);

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
                    sendClientList(); // 점수 변경 후 업데이트
                    client.output.println("SERVER&Your current score: " + client.score);
                    break;
                }
            }
        }

        private void moveToNextWord() {
            currentWordIndex++;
            if (currentWordIndex >= ProblemManager.getProblemCount()) {
                currentWordIndex = 0;
            }

            // 게임 종료 조건
            if (connectedClients.size() < 2 || currentWordIndex==0) {
                gameEnded = true;
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
                // 모든 클라이언트에게 "START" 메시지 전송
                for (ClientHandler client : connectedClients) {
                    client.output.println("START&");
                }

                processTurn(); // 첫 턴 시작
            }
        }

        private void processTurn() {
            if (gameStarted) {
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
            String scoreBoard = generateScoreBoard();

            // 모든 클라이언트에 게임 종료 메시지 전송
            for (ClientHandler client : connectedClients) {
                client.output.println("END&" + scoreBoard.replace("\n", "&"));
                try {
                    client.clientSocket.close(); // 클라이언트 소켓 닫기
                } catch (IOException e) {
                    System.out.println(LOG_TAG + "Error closing client socket: " + e.getMessage());
                }
            }
            for (ClientHandler client : connectedClients) {
                client.resetGameState();
            }
            // 클라이언트 리스트 비우기
            connectedClients.clear();
            // 서버 상태 초기화
            resetGameState();
        }

        private void resetGameState() {
            gameStarted = false;
            gameEnded = false;
            answeredCorrectly = false;
            readyCount = 0;
            currentTurn = 1;
            score = 0;
            ready = false;
        }
    }

    public static void main(String[] args) {
        new CatchMindServer();
    }
}
