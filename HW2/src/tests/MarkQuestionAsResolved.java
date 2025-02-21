package tests;

import application.Question;
import java.sql.SQLException;
import java.util.List;

public class MarkQuestionAsResolved extends TestBase {
    public static void main(String[] args) throws SQLException {
        setup();

        System.out.println("\nTest: Mark Question as Resolved");

        // Step 1: Create a test question
        Question testQuestion = new Question(0, "How does garbage collection work in Java?", testUser, "General");
        dbHelper.addQuestion(testQuestion);

        // Step 2: Fetch the newly created question
        List<Question> questions = dbHelper.getQuestionsByUser(testUser.getId());
        if (questions.isEmpty()) {
            System.out.println("❌ Question creation failed.");
            cleanup();
            return;
        }
        testQuestion = questions.get(0);

        // Step 3: Mark the question as resolved
        dbHelper.markQuestionResolved(testQuestion.getId());

        // Step 4: Retrieve the question again and check if it's marked as resolved
        Question updatedQuestion = dbHelper.getQuestionById(testQuestion.getId());
        if (updatedQuestion.isResolved()) {
            System.out.println("✅ Question successfully marked as resolved.");
        } else {
            System.out.println("❌ Failed to mark question as resolved.");
        }

        cleanup();
    }
}
