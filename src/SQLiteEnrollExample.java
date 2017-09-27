import org.sqlite.SQLiteConfig;

import java.sql.*;
import java.util.Date;
import java.util.Scanner;

/**
 * Created with IntelliJ IDEA.
 * User: ehsan
 * Date: 9/27/2017
 * Time: 12:50 AM
 */
public class SQLiteEnrollExample {

    private static final String JDBC_URL = "jdbc:sqlite::memory:";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private Connection con = null;

    /**
     * Opens a connection to the in-memory SQLite instance.
     * If the underlying connection is closed, it creates a new connection. Otherwise, the current instance is returned
     * @throws SQLException JDBC {@link DriverManager} exception
     */
    private void openConnection() throws SQLException {
        if (con == null || con.isClosed()) {
            SQLiteConfig config = new SQLiteConfig();
            config.enforceForeignKeys(true);
            con = DriverManager.getConnection(JDBC_URL, config.toProperties());
        }
    }

    /**
     * Closes the underlying connection to the in-memory SQLite instance.
     * It's a good practice to free up resources after you're done with them.
     * @throws SQLException JDBC {@link Connection} exception
     */
    private void closeConnection() throws SQLException {
        con.close();
    }

    /**
     * Creates the sample schema for enrollment database
     */
    void createSchema() {
        try {
            final Statement stmt = con.createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS course (course_id INTEGER, title TEXT, seats_available INTEGER, PRIMARY KEY(course_id))");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS student (student_id INTEGER, name TEXT, PRIMARY KEY(student_id))");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS take (course_id INTEGER, student_id INTEGER, enroll_date TEXT, PRIMARY KEY(student_id, course_id))");
        } catch (SQLException e) {
            System.err.println("[ERROR] createSchema : " + e.getMessage());
        }
    }

    /**
     * Initializes the sample schema with test data
     * Three courses: 1, 2, 3
     * Four students: 11, 12, 13, 14
     * Five takes
     */
    void initSchema() {
        final String[] courses = new String[] {
                "1,CMPUT291,200", "2,CMPUT274,70", "3,CMPUT301,80"
        };
        final String[] students = new String[] {
                "11,John", "12,Mary", "13,Steve", "14,Bob"
        };
        final String[] takes = new String[] {
                "1,11,2017-08-01",
                "2,13,2017-09-01", "2,14,2017-08-15",
                "3,11,2017-09-01", "3,12,2017-08-15"
        };
        try {

            try (PreparedStatement crsStmt = con.prepareStatement("INSERT INTO course VALUES (?, ?, ?)")) {
                for (String c : courses) {
                    final String[] cols = c.split(",");
                    crsStmt.setLong(1, Long.valueOf(cols[0]));
                    crsStmt.setString(2, cols[1]);
                    crsStmt.setInt(3, Integer.valueOf(cols[2]));
                    crsStmt.executeUpdate();
                }
            }

            try (PreparedStatement stdStmt = con.prepareStatement("INSERT INTO student VALUES (?, ?)")) {
                for (String s : students) {
                    final String[] cols = s.split(",");
                    stdStmt.setLong(1, Long.valueOf(cols[0]));
                    stdStmt.setString(2, cols[1]);
                    stdStmt.executeUpdate();
                }
            }

            try (PreparedStatement tkStmt = con.prepareStatement("INSERT INTO take VALUES (?, ?, ?)")) {
                for (String t : takes) {
                    final String[] cols = t.split(",");
                    tkStmt.setLong(1, Long.valueOf(cols[0]));
                    tkStmt.setLong(2, Long.valueOf(cols[1]));
                    tkStmt.setString(3, cols[2]);
                    tkStmt.executeUpdate();
                }
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] initSchema : " + e.getMessage());
        }
    }

    /**
     * Enrolls a student to a course.
     * First, the existence of student and course is checked.
     * Second, the course must have available seats.
     * Third, the student must not have already taken the course.
     * Once validated, a row will be inserted to "take" table and number of available seats will be decremented in course.
     * @param studentId read from Standard input
     * @param courseId read from Standard input
     */
    void enroll(long studentId, long courseId) {
        try {
            if (!validate(studentId, courseId)) return;

            final PreparedStatement tkStmt = con.prepareStatement("INSERT INTO take VALUES (?, ?, ?)");
            tkStmt.setLong(1, courseId);
            tkStmt.setLong(2, studentId);
            tkStmt.setString(3, String.format("%1$tY-%1$tm-%1$td %1$tT", new Date()));
            tkStmt.executeUpdate();

            final PreparedStatement crsStmt = con.prepareStatement("UPDATE course SET seats_available = seats_available - 1 WHERE course_id = ?");
            crsStmt.setLong(1, courseId);
            crsStmt.executeUpdate();

            System.out.printf("Student %d successfully enrolled in course %d\n", studentId, courseId);
        } catch (SQLException e) {
            System.err.println("[ERROR] enroll : " + e.getMessage());
        }
    }

    /**
     * All the necessary validations need be performed for enrollment.
     * Validations include:
     * <ul>
     *     <li>Whether the input student exists</li>
     *     <li>Whether the input course exists</li>
     *     <li>Whether the input course have sufficient seats</li>
     *     <li>Checks the input student have not taken the input course before</li>
     * </ul>
     * A {@link Statement} object is created from the underlying connection and will be shared among the validation methods.
     * @param studentId read from Standard input
     * @param courseId read from Standard input
     * @return true if no errors is found in inputs, otherwise false
     * @throws SQLException JDBC related exceptions
     */
    private boolean validate(long studentId, long courseId) throws SQLException {
        try (Statement stmt = con.createStatement()) {
            if (!studentExists(stmt, studentId)) {
                System.err.println(String.format("[ERROR] enroll : student %d not found.", studentId));
                return false;
            }

            if (!courseExists(stmt, courseId)) {
                System.err.println(String.format("[ERROR] enroll : course %d not found.", courseId));
                return false;
            }

            if (!isSeatsAvailable(stmt, courseId)) {
                System.err.println(String.format("[ERROR] enroll : course %d is full.", courseId));
                return false;
            }

            if (isCurrentlyEnrolled(stmt, studentId, courseId)) {
                System.err.println(String.format("[ERROR] enroll : student %d already enrolled in course %d.", studentId, courseId));
                return false;
            }
        }

        return true;
    }

    private boolean studentExists(Statement stmt, long studentId) throws SQLException {
        final ResultSet rs = stmt.executeQuery("SELECT student_id FROM student WHERE student_id = " + studentId);
        final boolean exists = rs.next();
        rs.close();
        return exists;
    }

    private boolean courseExists(Statement stmt, long courseId) throws SQLException {
        final ResultSet rs = stmt.executeQuery("SELECT course_id FROM course WHERE course_id = " + courseId);
        final boolean exists = rs.next();
        rs.close();
        return exists;
    }

    private boolean isSeatsAvailable(Statement stmt, long courseId) throws SQLException {
        final ResultSet rs = stmt.executeQuery("SELECT * FROM course WHERE course_id = " + courseId + " AND seats_available > 0");
        final boolean available = rs.next();
        rs.close();
        return available;
    }

    private boolean isCurrentlyEnrolled(Statement stmt, long studentId, long courseId) throws SQLException {
        final ResultSet rs = stmt.executeQuery("SELECT * FROM take WHERE course_id = " + courseId + " AND student_id = " + studentId);
        final boolean enrolled = rs.next();
        rs.close();
        return enrolled;
    }

    public static void main(String[] args) throws SQLException {
        SQLiteEnrollExample example = new SQLiteEnrollExample();
        example.openConnection();
        example.createSchema();
        example.initSchema();
        System.out.println("Initialization complete!!");

        final Scanner scanner = new Scanner(System.in);

        System.out.print("Please enter student id: ");
        final long studentId = scanner.nextLong();

        System.out.print("Please enter course id: ");
        final long courseId = scanner.nextLong();

        example.enroll(studentId, courseId);
        example.closeConnection();
    }

}
