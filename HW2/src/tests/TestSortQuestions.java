package tests;

import application.Question;
import java.sql.SQLException;
import java.util.List;

public class TestSortQuestions extends TestBase {
    public static void main(String[] args) throws SQLException {
        setup();

        System.out.println("\nTest: Sort Questions by Tag");
        List<Question> sortedQuestions = dbHelper.getQuestionsByTag("Assignments");
        System.out.println(sortedQuestions.isEmpty() ? "❌ No questions found for this tag." : "✅ Questions sorted by tag successfully.");

        cleanup();
    }
}
