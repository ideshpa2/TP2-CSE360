package application;

import databasePart1.DatabaseHelper;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Allows users to sort and list questions.
 */
public class SortQuestions {

    private final DatabaseHelper databaseHelper;

    public SortQuestions(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    // Get all questions sorted by date, unresolved, or tags
    public List<Question> getSortedQuestions(String sortBy, boolean onlyUnresolved, String tagFilter) throws SQLException {
        List<Question> questions = new ArrayList<>();
        StringBuilder query = new StringBuilder("SELECT * FROM Questions WHERE 1=1");

        if (onlyUnresolved) {
            query.append(" AND is_resolved = FALSE");
        }
        if (tagFilter != null && !tagFilter.isEmpty()) {
            query.append(" AND tags LIKE ?");
        }
        switch (sortBy) {
            case "date":
                query.append(" ORDER BY date_created DESC");
                break;
            default:
                query.append(" ORDER BY id");
        }

        try (PreparedStatement pstmt = ((Statement) databaseHelper).getConnection().prepareStatement(query.toString())) {
            if (tagFilter != null && !tagFilter.isEmpty()) {
                pstmt.setString(1, "%" + tagFilter + "%"); // Replace ? with tag filter
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    User user = databaseHelper.getUserByUserName(rs.getString("username"));
                    String tags = rs.getString("tags"); // ✅ Store as a single string
                    questions.add(new Question(rs.getInt("id"), rs.getString("content"), user, tags));
                }
            }
        }
        return questions;
    }
}