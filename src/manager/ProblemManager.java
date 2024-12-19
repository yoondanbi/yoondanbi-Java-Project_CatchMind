package manager;

public class ProblemManager {
    // 문제를 저장하는 배열
    private static final String[] PROBLEMS = {
            "햄버거",
    };

    // 문제 배열 반환 메서드
    public static String[] getProblems() {
        return PROBLEMS;
    }

    // 특정 문제 반환 메서드
    public static String getProblem(int index) {
        if (index >= 0 && index < PROBLEMS.length) {
            return PROBLEMS[index];
        }
        return "문제 없음"; // 범위 밖일 경우 기본 메시지
    }

    // 문제 개수 반환
    public static int getProblemCount() {
        return PROBLEMS.length;
    }
}
