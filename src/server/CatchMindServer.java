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
    private static final String LOG_PREFIX = "twoEZ - catchMind: ";
    private static final int SERVER_PORT = 3000;
    public static List<ClientHandler> connectedClients = Collections.synchronizedList(new ArrayList<>());
    private ServerSocket serverSocket;
    private int readyPlayerCount = 0;
    private boolean isGameActive = false;
    private int currentQuizIndex = 0;

    public CatchMindServer() {
        try {
            initializeServer();
            handleClientConnections();
        } catch (IOException e) {
            System.err.println(LOG_PREFIX + "Server initialization or connection failed: " + e.getMessage());
        }
    }

    private void initializeServer() throws IOException {
        serverSocket = new ServerSocket(SERVER_PORT);
        connectedClients = new Vector<>();
        System.out.println(LOG_PREFIX + "Server started on SERVER_PORT " + SERVER_PORT);
    }

    private void handleClientConnections() throws IOException {
        while (true) {
            Socket clientSocket = serverSocket.accept();
            addClient(clientSocket);
        }
    }

    private void addClient(Socket clientSocket) {
        ClientHandler client = new ClientHandler(clientSocket);
        client.start();
        connectedClients.add(client);
        System.out.println(LOG_PREFIX + "New client connected: " + clientSocket.getInetAddress());
    }

    class ClientHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter outputWriter;
        private BufferedReader inputReader;
        private boolean isReady = false;
        private boolean isGameOver = false;
        private boolean hasAnsweredCorrectly = false;
        private int playerTurnOrder = 0;
        private int currentPlayerTurn = 1;
        private String clientID;
        private int playerScore = 0;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                inputReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                outputWriter = new PrintWriter(clientSocket.getOutputStream(), true);
                String receivedMessage;

                while ((receivedMessage = inputReader.readLine()) != null) {
                    String[] parsedMessage = receivedMessage.split("&");
                    handleProtocol(parsedMessage);
                }
            } catch (Exception e) {
                System.out.println(LOG_PREFIX + "Communication error.");
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
            broadcastMessage("SERVER", clientID + " has entered the room");
            assignTurnsAndScores();
        }

        private void broadcastMessage(String prefix, String message) {
            for (ClientHandler client : connectedClients) {
                client.outputWriter.println(prefix + "&" + message);
            }
        }

        private void assignTurnsAndScores() {
            for (int i = 0; i < connectedClients.size(); i++) {
                initializeClient(connectedClients.get(i), i + 1);
            }
            sendClientList();
        }

        private void initializeClient(ClientHandler client, int playerTurnOrder) {
            client.outputWriter.println("ID&");
            client.playerTurnOrder = playerTurnOrder;
            client.playerScore = 0;
        }

        private void sendClientList() {
            for (ClientHandler client : connectedClients) {
                String clientInfo = "IDLIST&" + client.clientID + ":" + client.playerScore;
                client.outputWriter.println(clientInfo);
            }
        }

        private void processChat(String[] parsedMessage) {
            if (parsedMessage.length > 1) {
                String chatMessage = parsedMessage[1];
                broadcastMessage("CHAT", clientID + ": " + chatMessage);
                checkAnswer(chatMessage); // 채팅 메시지를 정답 체크 메서드로 전달
            }
        }

        private void checkAnswer(String answer) {
            String currentWord = ProblemManager.getProblem(currentQuizIndex);
            if (isGameActive && answer.equals(currentWord)) {
                //broadcastMessage("SERVER", "[" + clientID + "] guessed the word correctly: [" + currentWord + "]");
                increaseScore();
                // 모든 클라이언트에게 정답 처리 완료 알림
                for (ClientHandler client : connectedClients) {
                    client.outputWriter.println("ANSWER&" + currentWord);
                }
               // playerScore++; // 정답 맞춘 클라이언트의 점수 증가
                broadcastMessage("SERVER",  clientID + " [+" + playerScore+"점]");

                assignNextTurnToCorrectAnswerer(this); // 정답자에게 다음 턴 권한
                moveToNextWord();
                // 다음 턴으로 이동
                hasAnsweredCorrectly = true;  // 상태 업데이트
                processTurn();
            }
        }
        private void assignNextTurnToCorrectAnswerer(ClientHandler correctClient) {
            // 정답을 맞춘 클라이언트를 다음 턴으로 설정
            currentPlayerTurn = correctClient.playerTurnOrder;
        }

        private void increaseScore() {
            for (ClientHandler client : connectedClients) {
                if (client.clientID.equals(clientID)) {
                    client.playerScore++;
                    sendClientList(); // 점수 변경 후 업데이트
                    //client.outputWriter.println("SERVER&Your current score: " + client.playerScore);
                    break;
                }
            }
        }

        private void moveToNextWord() {
            currentQuizIndex++;
            if (currentQuizIndex >= ProblemManager.getProblemCount()) {
                currentQuizIndex = 0;
            }

            // 게임 종료 조건
            if (connectedClients.size() < 2 || currentQuizIndex==0) {
                isGameOver = true;
                endGame();
            }
        }

        private void toggleReadyStatus() {
            isReady = !isReady; // 준비 상태 토글
            if (isReady) {
                readyPlayerCount++;
                broadcastMessage("SERVER", clientID + " is ready.");
            } else {
                readyPlayerCount--;
                broadcastMessage("SERVER", clientID + " is not ready.");
            }
            System.out.println("readyPlayerCount = " + readyPlayerCount);
            // Ready 상태 확인 후 바로 게임 시작 체크
            if (readyPlayerCount == connectedClients.size() && !isGameActive) {
                startGame();
            }
        }

        private void startGame() {
            if (readyPlayerCount == connectedClients.size() && !isGameActive) {
                System.out.println("StartGame!! = " + clientSocket);
                isGameActive = true; // 게임 시작 상태 업데이트
                broadcastMessage("SERVER", "Game is starting!");
                // 모든 클라이언트에게 "START" 메시지 전송
                for (ClientHandler client : connectedClients) {
                    client.outputWriter.println("START&");
                }

                processTurn(); // 첫 턴 시작
            }
        }

        private void processTurn() {
            if (isGameActive) {
                for (ClientHandler client : connectedClients) {
                    if (client.playerTurnOrder == currentPlayerTurn) { // 현재 턴의 클라이언트 찾기
                        String currentWord = ProblemManager.getProblem(currentQuizIndex);
                        client.outputWriter.println("TURN&" + currentWord); // TURN과 제시어 전송
                        broadcastMessage("SERVER", client.clientID + " is turn");
                        //break;
                    }else{
                        client.outputWriter.println("NOTTURN&"); // NOTTURN 전송
                    }
                }
                // 상태 초기화
                hasAnsweredCorrectly = false;
            }
        }

        private void forwardDrawing(String[] parsedMessage) {
            for (ClientHandler client : connectedClients) {
                if (client != this) {
                    client.outputWriter.println("DRAW&" + parsedMessage[1]);
                }
            }
        }

        private void forwardColorChange(String[] parsedMessage) {
            for (ClientHandler client : connectedClients) {
                if (client != this) {
                    client.outputWriter.println("COLOR&" + parsedMessage[1]);
                }
            }
        }

        private void skipTurn(String[] parsedMessage) {
            hasAnsweredCorrectly = true;
            moveToNextWord();
            broadcastMessage("SERVER", "Turn skipped.");
        }

        private void handleClientExit(String[] parsedMessage) {
            broadcastMessage("SERVER", clientID + " has left the game.");
            if (isReady) {
                readyPlayerCount--; // 나가기 전에 준비 상태였다면 readyPlayerCount 감소
            }
            connectedClients.remove(this);
            if (isGameActive && connectedClients.size() < 2) {
                isGameActive = false;
                broadcastMessage("SERVER", "Not enough players. Game stopped.");
            }
        }


        private String generateScoreBoard() {
            StringBuilder scoreBoard = new StringBuilder();
            for (ClientHandler client : connectedClients) {
                scoreBoard.append(client.clientID)
                        .append(": ")
                        .append(client.playerScore)
                        .append("\n");
            }
            return scoreBoard.toString();
        }

        private void endGame() {
            String scoreBoard = generateScoreBoard();

            // 모든 클라이언트에 게임 종료 메시지 전송
            for (ClientHandler client : connectedClients) {
                client.outputWriter.println("END&" + scoreBoard.replace("\n", "&"));
                try {
                    client.clientSocket.close(); // 클라이언트 소켓 닫기
                } catch (IOException e) {
                    System.out.println(LOG_PREFIX + "Error closing client socket: " + e.getMessage());
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
            isGameActive = false;
            isGameOver = false;
            hasAnsweredCorrectly = false;
            readyPlayerCount = 0;
            currentPlayerTurn = 1;
            playerScore = 0;
            isReady = false;
        }
    }

    public static void main(String[] args) {
        new CatchMindServer();
    }
}
