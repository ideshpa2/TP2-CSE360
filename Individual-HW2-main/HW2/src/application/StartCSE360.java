package application;

import javafx.application.Application;
import javafx.stage.Stage;
import java.sql.SQLException;
import databasePart1.DatabaseHelper;

public class StartCSE360 extends Application {

    private static final DatabaseHelper databaseHelper = new DatabaseHelper();

    public static void main(String[] args) {
        System.out.println("🔹 StartCSE360.main() is executing...");
        launch(args);
        System.out.println("🔹 This line should not print unless JavaFX fails.");
    }

    @Override
    public void start(Stage primaryStage) {
        System.out.println("🔹 JavaFX start() method is running...");
        try {
            databaseHelper.connectToDatabase(); // Connect to the database
            System.out.println("🔹 Database connection attempted.");

            if (databaseHelper.isDatabaseEmpty()) {
                System.out.println("🔹 Database is empty. Showing FirstPage.");
                new FirstPage(databaseHelper).show(primaryStage);
            } else {
                System.out.println("🔹 Database has data. Showing SetupLoginSelectionPage.");
                new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
            }
        } catch (SQLException e) {
            System.out.println("🔴 SQL Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
