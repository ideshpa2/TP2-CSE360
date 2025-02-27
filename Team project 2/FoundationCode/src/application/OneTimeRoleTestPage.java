package application;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import javafx.application.Application;
import databasePart1.DatabaseHelper;
import javafx.stage.Stage;

/*******
 * <p> Title: PasswordEvaluationTestingAutomation Class. </p>
 * 
 * <p> Description: A Java demonstration for semi-automated tests </p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2022 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 1.00	2022-02-25 A set of semi-automated test cases
 * @version 2.00	2024-09-22 Updated for use at ASU
 * 
 */
public class OneTimeRoleTestPage {
    private static final DatabaseHelper databaseHelper = new DatabaseHelper();

    /*
     * This mainline displays a header to the console, performs a sequence of
     * test cases, and then displays a footer with a summary of the results
     */
    public static void main(String[] args) throws SQLException {
        System.out.println("______________________________________");
        System.out.println("\nTesting Automation: One-Time Password Management");
        databaseHelper.increment_num_users();
        User user1 = new User(databaseHelper.get_num_users(),"John", "Password!123", "john@example.com", Arrays.asList("user"));
        databaseHelper.connectToDatabase();
        databaseHelper.register(user1, user1.getRoles());
        System.out.println("Users after registration: " + databaseHelper.getAllUsers());

        String otp = "OTP12345";
        databaseHelper.setOneTimePassword("John", otp);
        
        // Validate OTP was set correctly
        String retrievedOtp = databaseHelper.getOneTimePassword("John");
        if (otp.equals(retrievedOtp)) {
            System.out.println("***Success*** OTP was successfully set.");
        } else {
            System.out.println("***Failure*** OTP was not set correctly.");
        }
        
        // Test clearing OTP
        databaseHelper.clearOneTimePassword("John");
        retrievedOtp = databaseHelper.getOneTimePassword("John");
        if (retrievedOtp == null || retrievedOtp.isEmpty()) {
            System.out.println("***Success*** OTP was successfully cleared.");
        } else {
            System.out.println("***Failure*** OTP was not cleared correctly.");
        }
    }
}