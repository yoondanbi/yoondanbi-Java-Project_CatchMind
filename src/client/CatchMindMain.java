package client;

import server.CatchMindServer;

public class CatchMindMain {
    public static void main(String[] args) {
        try {
            // 서버 실행
            System.out.println("Starting CatchMindServer...");
            Thread serverThread = new Thread(() -> {
                try {
                    CatchMindServer.main(null); // 서버 실행
                } catch (Exception e) {
                    System.err.println("Error starting server: " + e.getMessage());
                }
            });
            serverThread.start();

            // 서버가 안정적으로 시작될 시간을 확보 (3초 지연)
            Thread.sleep(1000);

            // 첫 번째 클라이언트 실행
            System.out.println("Starting CatchMindClient1...");
            CatchMindClient1.main(null); // 첫 번째 클라이언트 실행
            // 두 번째 클라이언트 실행
            System.out.println("Starting CatchMindClient2...");
            CatchMindClient2.main(null); // 두 번째 클라이언트 실행
        } catch (Exception e) {
            System.err.println("Error in main sequence: " + e.getMessage());
        }
    }
}
