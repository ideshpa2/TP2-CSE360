package tests;

import application.Question;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class TestViewUnresolvedQuestions extends TestBase {
    public static void main(String[] args) throws SQLException {
        setup();

        System.out.println("\nTest: View Unresolved Questions");

        // ✅ Fetch all questions (since there's no direct unresolved filter)
        List<Question> allQuestions = dbHelper.getQuestionsByTag(""); // Fetches all tags

        // ✅ Filter only unresolved questions
        List<Question> unresolvedQuestions = allQuestions.stream()
                .filter(q -> !q.isResolved())
                .collect(Collectors.toList());

        if (unresolvedQuestions.isEmpty()) {
            System.out.println("❌ No unresolved questions found.");
        } else {
            System.out.println("✅ Unresolved questions retrieved:");
            for (Question q : unresolvedQuestions) {
                System.out.println("- " + q.getContent() + " (Tags: " + q.getTags() + ")");
            }
        }

        cleanup();
    }
}
