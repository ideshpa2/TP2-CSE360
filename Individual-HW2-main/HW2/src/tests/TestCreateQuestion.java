package tests;

import application.Question;
import java.sql.SQLException;

public class TestCreateQuestion extends TestBase {
    public static void main(String[] args) throws SQLException {
        setup();

        System.out.println("\nTest: Create a New Question");
        Question testQuestion = new Question(0, "What is polymorphism in Java?", testUser, "Assignments");
        dbHelper.addQuestion(testQuestion);

        if (dbHelper.getQuestionsByUser(testUser.getId()).isEmpty()) {
            System.out.println("❌ Question creation failed.");
        } else {
            System.out.println("✅ Question created successfully.");
        }

        cleanup();
    }
}
