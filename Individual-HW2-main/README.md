# Individual-HW2

# Student Question and Answer System

## Overview
JavaFX application that allows users to:
- **Ask and answer questions**
- **Mark questions as resolved** and highlight solutions
- **Sort and filter questions** by tags, date, and resolution status
- **Manage user accounts** (register, login, delete)
- **Store data** using an H2 database

## Screencast Demo
ScreenCast Link: (https://asu.zoom.us/rec/share/zEkwyWWMCZavDWzvrzAzqewO8HSf26SoMRKTSB5njIYk1UIFfbTYyoa1lE4TcbbC.srgAeEKnMO4e_ROA
Passcode: j$0+7sfL

## Repository Structure

### **application/**
Main files to be looked at: 

- **`StartCSE360.java`** - Main entry point (JavaFX Application)
- **`UserHomePage.java`** - User dashboard (ask, answer, list questions)
- **`Question.java`** - Represents a question (id, content, user, tags, resolved status)
- **`Answer.java`** - Represents an answer (id, content, user, question, solution status)
- **`SortQuestions.java`** - Sorting and filtering for questions
- **`SortAnswers.java`** - Sorting and filtering for answers
- **`DatabaseHelper.java`** - Handles database operations (CRUD)

### **tests/**
Contains console-based test cases for key features.

- **`TestAskQuestion.java`** - Tests adding a new question
- **`TestAnswerQuestion.java`** - Tests submitting an answer
- **`TestResolveQuestion.java`** - Tests marking an answer as the solution
- **`TestViewUnresolved.java`** - Tests viewing unresolved questions
- **`TestDeleteQuestion.java`** - Tests deleting a question

## How to Run
1. **Clone the Repository**
   ```sh
   git clone https://github.com/hhua/Individual-HW2.git
   cd Individual-HW2
