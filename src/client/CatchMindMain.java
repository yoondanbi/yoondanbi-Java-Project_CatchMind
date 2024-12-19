package client;

import server.CatchMindServer;

public class CatchMindMain {
    public static void main(String[] args) {
        // 서버 실행 스레드
        Thread serverThread = new Thread(() -> {
            try {
                System.out.println("Starting CatchMindServer...");
                CatchMindServer.main(null); // 서버 실행
            } catch (Exception e) {
                System.err.println("Error starting server: " + e.getMessage());
            }
        });

        // 서버 실행
        serverThread.start();

        try {
            // 서버가 시작될 시간을 확보 (1초 지연)
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 클라이언트 실행 스레드
        Thread clientThread = new Thread(() -> {
            try {
                System.out.println("Starting CatchMindClient...");
                CatchMindClient1.main(null); // 클라이언트 실행
                CatchMindClient2.main(null); // 클라이언트 실행
            } catch (Exception e) {
                System.err.println("Error starting client: " + e.getMessage());
            }
        });

        // 클라이언트 실행
        clientThread.start();
    }
}
