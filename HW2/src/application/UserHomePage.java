package application;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import databasePart1.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * User home page with options to ask, answer, and list questions/answers.
 */
public class UserHomePage {

    private final DatabaseHelper databaseHelper;
    private final User user;
    private final SortQuestions sortQuestions;
    private final SortAnswers sortAnswers;

    public UserHomePage(DatabaseHelper databaseHelper, User user) {
        this.databaseHelper = databaseHelper;
        this.user = user;
        this.sortQuestions = new SortQuestions(databaseHelper);
        this.sortAnswers = new SortAnswers(databaseHelper);
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox(15);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label userLabel = new Label("Hello, " + user.getUserName() + "!");
        userLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Buttons
        Button askQuestionButton = new Button("Ask a Question");
        Button listQuestionsButton = new Button("View All Questions");
        Button viewMyQuestionsButton = new Button("View My Questions"); // NEW
        Button searchByTagButton = new Button("Search Questions by Tag");


        // Event handlers
        askQuestionButton.setOnAction(e -> showAskQuestionPage(primaryStage));
        listQuestionsButton.setOnAction(e -> showListQuestionsPage(primaryStage));
        viewMyQuestionsButton.setOnAction(e -> showUserQuestionsPage(primaryStage)); // NEW
        searchByTagButton.setOnAction(e -> showSearchByTagPage(primaryStage));


        layout.getChildren().addAll(userLabel, askQuestionButton, listQuestionsButton, viewMyQuestionsButton, searchByTagButton);
        primaryStage.setScene(new Scene(layout, 800, 400));

       }

    // ****************************
    // ** Ask Question Page **
    // ****************************
    private void showAskQuestionPage(Stage primaryStage) {
        VBox layout = new VBox(15);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label titleLabel = new Label("Ask a Question");
        TextField questionField = new TextField();
        questionField.setPromptText("Enter your question...");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        // Buttons for predefined tags
        ToggleGroup tagGroup = new ToggleGroup();
        RadioButton assignmentTag = new RadioButton("Assignments");
        RadioButton examTag = new RadioButton("Exams");
        RadioButton generalTag = new RadioButton("General");

        assignmentTag.setToggleGroup(tagGroup);
        examTag.setToggleGroup(tagGroup);
        generalTag.setToggleGroup(tagGroup);

        Button submitButton = new Button("Submit");
        Button backButton = new Button("Back");

        submitButton.setOnAction(e -> {
            String content = questionField.getText().trim();
            RadioButton selectedTag = (RadioButton) tagGroup.getSelectedToggle();
            
            // **Input Validation**
            if (content.isEmpty()) {
                errorLabel.setText("Error: Question cannot be empty.");
                return;
            }
            if (selectedTag == null) {
                errorLabel.setText("Error: You must select a tag.");
                return;
            }

            try {
                User updatedUser = databaseHelper.getUserByUserName(user.getUserName());
                if (updatedUser == null) {
                    errorLabel.setText("Error: User not found.");
                    return;
                }

                String tag = selectedTag.getText(); // Get the selected tag
                Question question = new Question(0, content, updatedUser, tag);
                databaseHelper.addQuestion(question);
                show(primaryStage);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        backButton.setOnAction(e -> show(primaryStage));

        layout.getChildren().addAll(titleLabel, questionField, assignmentTag, examTag, generalTag, submitButton, errorLabel, backButton);
        primaryStage.setScene(new Scene(layout, 800, 400));
    }

    // ****************************
    // ** List Questions Page **
    // ****************************
    
    private void showListQuestionsPage(Stage primaryStage) {
        VBox layout = new VBox(15);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label titleLabel = new Label("All Questions");
        ScrollPane scrollPane = new ScrollPane();
        VBox questionContainer = new VBox(10);

        try {
            List<Question> questions = sortQuestions.getSortedQuestions("date", false, "");

            for (Question q : questions) {
                VBox questionBox = new VBox(5);
                String questionText = q.isResolved() ? "✅ " + q.getContent() : q.getContent(); 
                Label questionLabel = new Label(questionText + " [" + q.getTags() + "]");
                Button answerButton = new Button("Answer");

                // Fetch answers for this question
                VBox answersBox = new VBox(5);
                List<Answer> answers = sortAnswers.getSortedAnswers(q.getId(), "date", user.getId());

                for (Answer a : answers) {
                    String answerText = a.isSolution() ? "✅ " + a.getContent() : a.getContent(); 
                    Label answerLabel = new Label("→ " + answerText);

                    if (a.isSolution()) {
                        System.out.println("DEBUG: Marking answer as solution → " + a.getContent()); // Debug print
                        answerLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-padding: 5; -fx-background-color: #e8f5e9;"); // Green color for solutions
                    } else {
                        answerLabel.setStyle("-fx-padding: 5; -fx-background-color: #e8e8e8;");
                    }

                    answersBox.getChildren().add(answerLabel);
                }

                answerButton.setOnAction(e -> showAnswerForm(primaryStage, q));

                questionBox.getChildren().addAll(questionLabel, answerButton, answersBox);
                questionBox.setStyle("-fx-border-color: black; -fx-padding: 10; -fx-background-color: #f5f5f5;");
                questionContainer.getChildren().add(questionBox);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        scrollPane.setContent(questionContainer);
        scrollPane.setFitToWidth(true);

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> show(primaryStage));

        layout.getChildren().addAll(titleLabel, scrollPane, backButton);
        primaryStage.setScene(new Scene(layout, 800, 400));
    }

    
    private void showUserQuestionsPage(Stage primaryStage) {
        VBox layout = new VBox(15);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label titleLabel = new Label("Enter your username to view your questions:");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username...");
        Button fetchQuestionsButton = new Button("Fetch My Questions");
        VBox questionContainer = new VBox(10);
        ScrollPane scrollPane = new ScrollPane(questionContainer);
        scrollPane.setFitToWidth(true);

        fetchQuestionsButton.setOnAction(e -> {
            String enteredUsername = usernameField.getText().trim();
            if (enteredUsername.isEmpty()) {
                System.out.println("Error: Username cannot be empty.");
                return;
            }

            try {
                User fetchedUser = databaseHelper.getUserByUserName(enteredUsername);
                if (fetchedUser == null) {
                    System.out.println("Error: User not found.");
                    return;
                }

                questionContainer.getChildren().clear(); // Clear previous content
                List<Question> questions = databaseHelper.getQuestionsByUser(fetchedUser.getId());

                for (Question q : questions) {
                    VBox questionBox = new VBox(5);
                    String questionText = q.isResolved() ? "✅ " + q.getContent() : q.getContent();
                    Label questionLabel = new Label(questionText);
                    Button resolveButton = new Button("Mark as Resolved");

                    // Get answers for the question
                    VBox answersBox = new VBox(5);
                    List<Answer> answers = sortAnswers.getSortedAnswers(q.getId(), "date", fetchedUser.getId());
                    List<CheckBox> answerCheckboxes = new ArrayList<>();

                    for (Answer a : answers) {
                        String answerText = a.isSolution() ? "✅ " + a.getContent() : a.getContent();
                        CheckBox answerCheckBox = new CheckBox(answerText);
                        answerCheckBox.setUserData(a.getId()); // Store answer ID

                        if (a.isSolution()) {
                            System.out.print("DEBUG: Answer marked as solution! " + a.getContent());
                            answerCheckBox.setSelected(true); // ✅ Auto-select solutions
                            answerCheckBox.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-padding: 5; -fx-background-color: #e8f5e9;");
                        }

                        answerCheckboxes.add(answerCheckBox);
                        answersBox.getChildren().add(answerCheckBox);
                    }
                    
                    Button deleteButton = new Button("Delete");
                    deleteButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
                    deleteButton.setOnAction(event -> {
                        try {
                            databaseHelper.deleteQuestion(q.getId());  //  Delete without checking user
                            showUserQuestionsPage(primaryStage);  // Refresh UI
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    });
                    questionBox.getChildren().add(deleteButton);

                    // Handle resolving question and selecting answers as solutions
                    resolveButton.setOnAction(event -> {
                        try {
                            databaseHelper.markQuestionResolved(q.getId());

                            for (CheckBox checkBox : answerCheckboxes) {
                                if (checkBox.isSelected()) {
                                    int answerId = (int) checkBox.getUserData();

                                    // ✅ Mark as solution in database
                                    databaseHelper.markAnswerAsSolution(answerId);

                                    // ✅ Update UI to reflect solution status
                                    checkBox.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-padding: 5; -fx-background-color: #e8f5e9;");
                                    checkBox.setText("✅ " + checkBox.getText()); 
                                    
                                	System.out.print("YY"); 

                                }
                            }
                            
                            // ✅ Refresh UI to show updates
                            showUserQuestionsPage(primaryStage);

                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    });

                    questionBox.getChildren().addAll(questionLabel, answersBox, resolveButton);
                    questionBox.setStyle("-fx-border-color: black; -fx-padding: 10; -fx-background-color: #f5f5f5;");
                    questionContainer.getChildren().add(questionBox);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        
 
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> show(primaryStage));

        layout.getChildren().addAll(titleLabel, usernameField, fetchQuestionsButton, scrollPane, backButton);
        primaryStage.setScene(new Scene(layout, 800, 400));
    }

    private void showSearchByTagPage(Stage primaryStage) {
        VBox layout = new VBox(15);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label titleLabel = new Label("Search Questions by Tag:");
        
        // ✅ Tag buttons container
        HBox tagButtonContainer = new HBox(10);
        tagButtonContainer.setStyle("-fx-alignment: center;");

        // ✅ Tag buttons
        Button assignmentsButton = new Button("Assignments");
        Button examsButton = new Button("Exams");
        Button generalButton = new Button("General");

        tagButtonContainer.getChildren().addAll(assignmentsButton, examsButton, generalButton);

        // ✅ Question display area
        VBox questionContainer = new VBox(10);
        ScrollPane scrollPane = new ScrollPane(questionContainer);
        scrollPane.setFitToWidth(true);

        // Function to search and display questions
        EventHandler<ActionEvent> searchHandler = event -> {
            Button clickedButton = (Button) event.getSource();
            String selectedTag = clickedButton.getText(); // Get tag from button text

            try {
                questionContainer.getChildren().clear(); // Clear previous results
                List<Question> questions = databaseHelper.getQuestionsByTag(selectedTag);

                for (Question q : questions) {
                    VBox questionBox = new VBox(5);
                    
                    String questionText = q.isResolved() ? "✅ " + q.getContent() : q.getContent();
                    Label questionLabel = new Label(questionText + " [" + q.getTags() + "]");

                    Button answerButton = new Button("Answer");

                
                    VBox answersBox = new VBox(5);
                    List<Answer> answers = databaseHelper.getAnswersByQuestionId(q.getId());
                    for (Answer a : answers) {
                        String answerText = a.isSolution() ? "✅ " + a.getContent() : a.getContent();
                        Label answerLabel = new Label("→ " + answerText);

                        if (a.isSolution()) {
                            answerLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-padding: 5; -fx-background-color: #e8f5e9;");
                        } else {
                            answerLabel.setStyle("-fx-padding: 5; -fx-background-color: #e8e8e8;");
                        }

                        answersBox.getChildren().add(answerLabel);
                    }

                    answerButton.setOnAction(e -> showAnswerForm(primaryStage, q));

                    questionBox.getChildren().addAll(questionLabel, answerButton, answersBox);
                    questionBox.setStyle("-fx-border-color: black; -fx-padding: 10; -fx-background-color: #f5f5f5;");
                    questionContainer.getChildren().add(questionBox);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        };

        assignmentsButton.setOnAction(searchHandler);
        examsButton.setOnAction(searchHandler);
        generalButton.setOnAction(searchHandler);

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> show(primaryStage));

        layout.getChildren().addAll(titleLabel, tagButtonContainer, scrollPane, backButton);
        primaryStage.setScene(new Scene(layout, 800, 400));
    }


   

    // ****************************
    // ** Answer Question Page **
    // ****************************
  
    private void showAnswerForm(Stage primaryStage, Question question) {
        VBox layout = new VBox(15);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label titleLabel = new Label("Answer the Question:");
        Label questionLabel = new Label(question.toString());
        TextField answerField = new TextField();
        answerField.setPromptText("Enter your answer...");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        Button submitButton = new Button("Submit");
        Button backButton = new Button("Back");

        submitButton.setOnAction(e -> {
            String content = answerField.getText().trim();

            // **Input Validation**
            if (content.isEmpty()) {
                errorLabel.setText("Error: Answer cannot be empty.");
                return;
            }
            if (content.length() < 5) {
                errorLabel.setText("Error: Answer must be at least 5 characters long.");
                return;
            }

            try {
                User updatedUser = databaseHelper.getUserByUserName(user.getUserName());
                if (updatedUser == null) {
                    errorLabel.setText("Error: User does not exist.");
                    return;
                }

                Answer answer = new Answer(0, content, updatedUser, question);
                databaseHelper.addAnswer(answer);
                showListQuestionsPage(primaryStage);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        backButton.setOnAction(e -> showListQuestionsPage(primaryStage));

        layout.getChildren().addAll(titleLabel, questionLabel, answerField, submitButton, errorLabel, backButton);
        primaryStage.setScene(new Scene(layout, 800, 400));
    }
            
            
            
            
            
            
}
            
            
       