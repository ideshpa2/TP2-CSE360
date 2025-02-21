package tests;

import databasePart1.DatabaseHelper;
import application.User;
import java.sql.SQLException;

public abstract class TestBase {
    protected static DatabaseHelper dbHelper;
    protected static User testUser;

    public static void setup() throws SQLException {
        dbHelper = new DatabaseHelper();
        dbHelper.connectToDatabase();

        // Create a test user
        testUser = new User("testUser", "password123", "student");
        dbHelper.register(testUser);
        testUser = dbHelper.getUserByUserName("testUser"); // Get user with ID
    }

    public static void cleanup() throws SQLException {
        if (testUser != null) {
            dbHelper.deleteUser(testUser.getId()); // Cleanup test data
        }
        dbHelper.closeConnection();
    }
}
