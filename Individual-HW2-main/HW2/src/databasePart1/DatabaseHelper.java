package databasePart1;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import application.Answer;
import application.Question;
import application.User;


/**
 * The DatabaseHelper class is responsible for managing the connection to the database,
 * performing operations such as user registration, login validation, and handling invitation codes.
 */
public class DatabaseHelper {

	// JDBC driver name and database URL 
	static final String JDBC_DRIVER = "org.h2.Driver";   
	static final String DB_URL = "jdbc:h2:~/FoundationDatabase";  

	//  Database credentials 
	static final String USER = "sa"; 
	static final String PASS = ""; 

	public Connection connection = null;
	private Statement statement = null; 
	
	//	PreparedStatement pstmt

	public void connectToDatabase() throws SQLException {
		try {
			Class.forName(JDBC_DRIVER); // Load the JDBC driver
			System.out.println("Connecting to database...");
			connection = DriverManager.getConnection(DB_URL, USER, PASS);
			statement = connection.createStatement(); 
			// You can use this command to clear the database and restart from fresh.
			// statement.execute("DROP ALL OBJECTS");

			createTables();  // Create the necessary tables if they don't exist
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
		}
	}
	
	public Connection getConnection() throws SQLException {
	    if (connection == null || connection.isClosed()) {
	        connectToDatabase(); // Reconnect if connection is lost
	    }
	    return connection;
	}

	private void createTables() throws SQLException {
	    // 1️⃣ Create Users table FIRST
	    String userTable = "CREATE TABLE IF NOT EXISTS Users ("
	            + "id INT AUTO_INCREMENT PRIMARY KEY, "
	            + "userName VARCHAR(255) UNIQUE NOT NULL, "
	            + "password VARCHAR(255) NOT NULL, "
	            + "role VARCHAR(20) NOT NULL)";
	    statement.execute(userTable);

	    // 2️⃣ Create Questions table (AFTER Users)
	    String questionsTable = "CREATE TABLE IF NOT EXISTS Questions ("
	            + "id INT AUTO_INCREMENT PRIMARY KEY, "
	            + "content TEXT NOT NULL, "
	            + "user_id INT NOT NULL, "
	            + "tags VARCHAR(255), " // Store as a single string
	            + "date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
	            + "is_resolved BOOLEAN DEFAULT FALSE, "
	            + "FOREIGN KEY (user_id) REFERENCES cse360users(id))";
	    statement.execute(questionsTable);
	    
	    // 3️⃣ Create Answers table (AFTER Questions)
	    String answersTable = "CREATE TABLE IF NOT EXISTS Answers ("
	            + "id INT AUTO_INCREMENT PRIMARY KEY, "
	            + "content TEXT NOT NULL, "
	            + "question_id INT NOT NULL, "
	            + "user_id INT NOT NULL, "
	            + "date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
	            + "is_solution BOOLEAN DEFAULT FALSE, "
	            + "FOREIGN KEY (question_id) REFERENCES Questions(id) ON DELETE CASCADE, "
	            + "FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE)";
	    statement.execute(answersTable);

	    // 4️⃣ Create ReadAnswers table (AFTER Answers)
	    String readAnswersTable = "CREATE TABLE IF NOT EXISTS ReadAnswers ("
	            + "user_id INT NOT NULL, "
	            + "answer_id INT NOT NULL, "
	            + "FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE, "
	            + "FOREIGN KEY (answer_id) REFERENCES Answers(id) ON DELETE CASCADE, "
	            + "PRIMARY KEY (user_id, answer_id))";
	    statement.execute(readAnswersTable);
	    
		// Create the invitation codes table
	    String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
	            + "code VARCHAR(10) PRIMARY KEY, "
	            + "isUsed BOOLEAN DEFAULT FALSE)";
	    statement.execute(invitationCodesTable);
	}

	public User getUserByUserName(String userName) throws SQLException {
	    String query = "SELECT * FROM Users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();

	        if (rs.next()) {
	            return new User(
	                rs.getInt("id"),  //  Correct user ID
	                rs.getString("userName"),
	                rs.getString("password"),
	                rs.getString("role")
	            );
	        }
	    }
	    return null; // User not found
	}

	// Check if the database is empty
	// Check if the Users table is empty
	public boolean isDatabaseEmpty() throws SQLException {
	    String query = "SELECT COUNT(*) AS count FROM Users"; //  FIXED table name
	    ResultSet resultSet = statement.executeQuery(query);
	    if (resultSet.next()) {
	        return resultSet.getInt("count") == 0;
	    }
	    return true;
	}

	// Registers a new user in the database.
	public void register(User user) throws SQLException {
	    String insertUser = "INSERT INTO Users (userName, password, role) VALUES (?, ?, ?)"; //  
	    try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
	        pstmt.setString(1, user.getUserName());
	        pstmt.setString(2, user.getPassword());
	        pstmt.setString(3, user.getRole());
	        pstmt.executeUpdate();
	    }
	}

	// Validates a user's login credentials.
	public boolean login(User user) throws SQLException {
		String query = "SELECT * FROM Users WHERE userName = ? AND password = ? AND role = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			pstmt.setString(3, user.getRole());
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}
	
	// Checks if a user already exists in the database based on their userName.
	public boolean doesUserExist(String userName) {
	    String query = "SELECT COUNT(*) FROM Users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            // If the count is greater than 0, the user exists
	            return rs.getInt(1) > 0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false; // If an error occurs, assume user doesn't exist
	}
	
	
	// Retrieves the role of a user from the database using their UserName.
	public String getUserRole(String userName) {
	    String query = "SELECT role FROM Users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("role"); // Return the role if user exists
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null; // If no user exists or an error occurs
	}
	
	// Retrieve user by ID
	
	public User getUserById(int userId) throws SQLException {
        String query = "SELECT * FROM Users WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("userName"),
                        rs.getString("password"),
                        rs.getString("role")
                );
            }
        }
        return null;
    }
	
	// Generates a new invitation code and inserts it into the database.
	public String generateInvitationCode() {
	    String code = UUID.randomUUID().toString().substring(0, 4); // Generate a random 4-character code
	    String query = "INSERT INTO InvitationCodes (code) VALUES (?)";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return code;
	}
	
	// Validates an invitation code to check if it is unused.
	public boolean validateInvitationCode(String code) {
	    String query = "SELECT * FROM InvitationCodes WHERE code = ? AND isUsed = FALSE";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            // Mark the code as used
	            markInvitationCodeAsUsed(code);
	            return true;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}
	
	// Marks the invitation code as used in the database.
	private void markInvitationCodeAsUsed(String code) {
	    String query = "UPDATE InvitationCodes SET isUsed = TRUE WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	// Closes the database connection and statement.
	public void closeConnection() {
		try{ 
			if(statement!=null) statement.close(); 
		} catch(SQLException se2) { 
			se2.printStackTrace();
		} 
		try { 
			if(connection!=null) connection.close(); 
		} catch(SQLException se){ 
			se.printStackTrace(); 
		} 
	}
	
	// ************ QUESTIONS ************ // 
	
	// Create question
	
	public void addQuestion(Question question) throws SQLException {
	    if (question.getUser().getId() == 0) {
	        throw new SQLException("Invalid user ID. Ensure user exists in database before adding a question.");
	    }
	    
	    String query = "INSERT INTO Questions (content, user_id, tags) VALUES (?, ?, ?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, question.getContent());
	        pstmt.setInt(2, question.getUser().getId());
	        pstmt.setString(3, String.join(",", question.getTags()));
	        pstmt.executeUpdate();
	    }
	}
	
	// Retrieve question by ID
	
	public Question getQuestionById(int questionId) throws SQLException {
	    String query = "SELECT * FROM Questions WHERE id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, questionId);
	        ResultSet rs = pstmt.executeQuery();

	        if (rs.next()) {
	            User user = getUserById(rs.getInt("user_id"));
	            String tags = rs.getString("tags"); // Store as a single string
	            return new Question(rs.getInt("id"), rs.getString("content"), user, tags);
	        }
	    }
	    return null;
	}

	public List<Question> getQuestionsByUser(int userId) throws SQLException {
	    List<Question> questions = new ArrayList<>();
	    String query = "SELECT id, content, user_id, tags, is_resolved FROM Questions WHERE user_id = ?";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, userId);
	        ResultSet rs = pstmt.executeQuery();

	        while (rs.next()) {
	            User user = getUserById(rs.getInt("user_id"));
	            String tags = rs.getString("tags"); // Store as a single string
	            boolean isResolved = rs.getBoolean("is_resolved"); // Fetch resolved status

	            Question question = new Question(rs.getInt("id"), rs.getString("content"), user, tags);
	            question.setResolved(isResolved); // Save resolved status
	            questions.add(question);
	        }
	    }
	    return questions;
	}
	
	public List<Question> getQuestionsByTag(String tag) throws SQLException {
	    List<Question> questions = new ArrayList<>();
	    String query = "SELECT * FROM Questions WHERE tags LIKE ? ORDER BY date_created DESC";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, "%" + tag + "%"); // Match partial tags
	        ResultSet rs = pstmt.executeQuery();

	        while (rs.next()) {
	            User user = getUserById(rs.getInt("user_id"));
	            String tags = rs.getString("tags"); // Store tags as a single string
	            boolean isResolved = rs.getBoolean("is_resolved");

	            Question question = new Question(rs.getInt("id"), rs.getString("content"), user, tags);
	            question.setResolved(isResolved);
	            questions.add(question);
	        }
	    }
	    return questions;
	}

	
	// Mark question resolved
	
	public void markQuestionResolved(int questionId) throws SQLException {
	    String query = "UPDATE Questions SET is_resolved = TRUE WHERE id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, questionId);
	        int updatedRows = pstmt.executeUpdate();
	        if (updatedRows > 0) {
	            System.out.println("Question " + questionId + " marked as resolved.");
	        } else {
	            System.out.println("⚠️ ERROR: Question ID " + questionId + " not found or update failed.");
	        }
	    }
	}
	
	public void deleteQuestion(int questionId) throws SQLException {
	    System.out.println("Attempting to delete question ID: " + questionId);

	    String query = "DELETE FROM Questions WHERE id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, questionId);
	        int affectedRows = pstmt.executeUpdate();
	        if (affectedRows == 0) {
	            System.out.println("Error: No question deleted. (Already deleted?)");
	        } else {
	            System.out.println(" Question deleted successfully!");
	        }
	    }
	}
	
	// Retrieve questions sorted by date (newest first)
	public List<Question> getQuestionsSortedByDate() throws SQLException {
	    List<Question> questions = new ArrayList<>();
	    String query = "SELECT * FROM Questions ORDER BY date_created DESC";

	    try (PreparedStatement pstmt = connection.prepareStatement(query);
	         ResultSet rs = pstmt.executeQuery()) {
	        while (rs.next()) {
	            User user = getUserById(rs.getInt("user_id"));
	            String tags = rs.getString("tags");
	            boolean isResolved = rs.getBoolean("is_resolved");
	            Question question = new Question(rs.getInt("id"), rs.getString("content"), user, tags);
	            question.setResolved(isResolved);
	            questions.add(question);
	        }
	    }
	    return questions;
	}

	// Retrieve questions sorted by resolution status (unresolved first)
	public List<Question> getQuestionsSortedByStatus() throws SQLException {
	    List<Question> questions = new ArrayList<>();
	    String query = "SELECT * FROM Questions ORDER BY is_resolved ASC, date_created DESC";

	    try (PreparedStatement pstmt = connection.prepareStatement(query);
	         ResultSet rs = pstmt.executeQuery()) {
	        while (rs.next()) {
	            User user = getUserById(rs.getInt("user_id"));
	            String tags = rs.getString("tags");
	            boolean isResolved = rs.getBoolean("is_resolved");
	            Question question = new Question(rs.getInt("id"), rs.getString("content"), user, tags);
	            question.setResolved(isResolved);
	            questions.add(question);
	        }
	    }
	    return questions;
	}



	// ************ ANSWER ************ // 
	
	// Create answer 
	
	public void addAnswer(Answer answer) throws SQLException {
	    if (answer.getUser().getId() == 0) {
	        throw new SQLException("Invalid user ID. Ensure user exists in the database before adding an answer.");
	    }
	    
	    String query = "INSERT INTO Answers (content, question_id, user_id) VALUES (?, ?, ?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, answer.getContent());
	        pstmt.setInt(2, answer.getQuestion().getId());
	        pstmt.setInt(3, answer.getUser().getId()); //  Valid user ID
	        pstmt.executeUpdate();
	    }
	}
	
	// Retrieve answer through ID 
	
	public Answer getAnswerById(int answerId) throws SQLException {
        String query = "SELECT * FROM Answers WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, answerId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = getUserById(rs.getInt("user_id"));
                Question question = getQuestionById(rs.getInt("question_id"));
                return new Answer(rs.getInt("id"), rs.getString("content"), user, question);
            }
        }
        return null;
    }


	// Marks answer as solution
	
	public void markAnswerAsSolution(int answerId) throws SQLException {
	    String query = "UPDATE Answers SET is_solution = TRUE WHERE id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, answerId);
	        int updatedRows = pstmt.executeUpdate();
	        if (updatedRows > 0) {
	            System.out.println(" Answer " + answerId + " marked as a solution.");
	        } else {
	            System.out.println("⚠️ ERROR: Answer ID " + answerId + " not found or update failed.");
	        }
	    }
	}

	// Add this method to your DatabaseHelper class
	public void deleteUser(int userId) throws SQLException {
	    String deleteAnswers = "DELETE FROM Answers WHERE user_id = ?";
	    String deleteQuestions = "DELETE FROM Questions WHERE user_id = ?";
	    String deleteUser = "DELETE FROM Users WHERE id = ?";

	    try (PreparedStatement pstmtAnswers = connection.prepareStatement(deleteAnswers);
	         PreparedStatement pstmtQuestions = connection.prepareStatement(deleteQuestions);
	         PreparedStatement pstmtUser = connection.prepareStatement(deleteUser)) {
	        
	        // Delete user's answers
	        pstmtAnswers.setInt(1, userId);
	        pstmtAnswers.executeUpdate();

	        // Delete user's questions
	        pstmtQuestions.setInt(1, userId);
	        pstmtQuestions.executeUpdate();

	        // Finally, delete the user
	        pstmtUser.setInt(1, userId);
	        int affectedRows = pstmtUser.executeUpdate();
	        
	        if (affectedRows > 0) {
	            System.out.println("User deleted successfully.");
	        } else {
	            System.out.println(" No user found with ID: " + userId);
	        }
	    }
	}

	
	// Marks answer as read
	
	public void markAnswerAsRead(int userId, int answerId) throws SQLException {
        String query = "INSERT INTO ReadAnswers (user_id, answer_id) VALUES (?, ?) ON DUPLICATE KEY UPDATE user_id = user_id";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, answerId);
            pstmt.executeUpdate();
        }
    }
	
	// Retrieve answers for a specific question, sorted by date
	// Retrieve answers for a specific question, ensuring each answer has an associated Question object
	public List<Answer> getAnswersByQuestionId(int questionId) throws SQLException {
	    List<Answer> answers = new ArrayList<>();
	    String query = "SELECT * FROM Answers WHERE question_id = ? ORDER BY date_created DESC";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, questionId);
	        ResultSet rs = pstmt.executeQuery();

	        // Fetch the Question object to link with Answers
	        Question relatedQuestion = getQuestionById(questionId);

	        while (rs.next()) {
	            int answerId = rs.getInt("id");
	            String content = rs.getString("content");
	            int userId = rs.getInt("user_id");

	            //  Fetch the User who posted the answer
	            User user = getUserById(userId);

	            //  Create an Answer object with the associated Question
	            Answer answer = new Answer(answerId, content, user, relatedQuestion);
	            answers.add(answer);
	        }
	    }
	    return answers;
	}

	// get sorted answers
	
	public List<Answer> getSortedAnswers(int questionId, String sortBy, int userId) throws SQLException {
	    List<Answer> answers = new ArrayList<>();
	    String query = "SELECT id, content, user_id, question_id, is_solution FROM Answers WHERE question_id = ? ORDER BY is_solution DESC, date_created DESC";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, questionId);
	        ResultSet rs = pstmt.executeQuery();

	        while (rs.next()) {
	            User user = getUserById(rs.getInt("user_id"));
	            Question question = getQuestionById(rs.getInt("question_id"));
	            boolean isSolution = rs.getBoolean("is_solution"); // Ensure this fetches correctly
	            System.out.println("DEBUG: Answer ID " + rs.getInt("id") + " isSolution: " + isSolution);

	            Answer answer = new Answer(rs.getInt("id"), rs.getString("content"), user, question);
	            answer.setSolution(isSolution); // Set the correct value
	            answers.add(answer);
	        }
	    }
	    return answers;
	}
	
	public void deleteAnswer(int answerId, int userId) throws SQLException {
	    String query = "DELETE FROM Answers WHERE id = ? AND user_id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, answerId);
	        pstmt.setInt(2, userId);
	        pstmt.executeUpdate();
	    }
	}


}
