package tests;

import application.Question;
import java.sql.SQLException;
import java.util.List;

public class TestDeleteQuestions extends TestBase {
    public static void main(String[] args) throws SQLException {
        setup();

        System.out.println("\nTest: Delete a Question");

        Question testQuestion = new Question(0, "What is polymorphism in Java?", testUser, "Assignments");
        dbHelper.addQuestion(testQuestion);
        testQuestion = dbHelper.getQuestionsByUser(testUser.getId()).get(0);

        dbHelper.deleteQuestion(testQuestion.getId());
        List<Question> afterDeletion = dbHelper.getQuestionsByUser(testUser.getId());
        System.out.println(afterDeletion.isEmpty() ? "✅ Question deleted successfully." : "❌ Failed to delete question.");

        cleanup();
    }
}
