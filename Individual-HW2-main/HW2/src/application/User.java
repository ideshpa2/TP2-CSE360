package application;

import java.util.ArrayList;
import java.util.List;

/**
 * The User class represents a user entity in the system.
 * It stores the user's details and all their associated questions & answers.
 */
public class User {
    private int id; // Unique identifier
    private String userName;
    private String password;
    private String role;
    private List<Question> questions; // User's questions
    private List<Answer> answers; // User's answers

    // Constructor for an existing user (fetched from DB)
    public User(int id, String userName, String password, String role) {
        this.id = id;
        this.userName = userName;
        this.password = password;
        this.role = role;
        this.questions = new ArrayList<>();
        this.answers = new ArrayList<>();
    }

    // Constructor for a new user before inserting into DB
    public User(String userName, String password, String role) {
        this.userName = userName;
        this.password = password;
        this.role = role;
        this.questions = new ArrayList<>();
        this.answers = new ArrayList<>();
    }

    // Getters
    public int getId() { return id; }
    public String getUserName() { return userName; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public List<Question> getQuestions() { return questions; }
    public List<Answer> getAnswers() { return answers; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setRole(String role) { this.role = role; }

    // Add a question to the user's list
    public void addQuestion(Question question) {
        questions.add(question);
    }

    // Add an answer to the user's list
    public void addAnswer(Answer answer) {
        answers.add(answer);
    }
}
