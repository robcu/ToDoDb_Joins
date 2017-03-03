import org.h2.tools.Server;

import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    private static void createTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS todos (id IDENTITY, text VARCHAR, is_done BOOLEAN, user INT);");
        stmt.execute("CREATE TABLE IF NOT EXISTS users (id IDENTITY, name VARCHAR);");
    }

    public static void insertItem(Connection conn, String text, int userId) throws SQLException {

        PreparedStatement stmt = conn.prepareStatement("INSERT INTO todos VALUES (NULL, ?, FALSE, ?);");
        stmt.setString(1, text);
        stmt.setInt(2, userId);
        stmt.execute();
    }

    public static ArrayList<ToDoItem> selectUsersItems(Connection conn, int userId) throws SQLException {
        ArrayList<ToDoItem> items = new ArrayList<>();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM todos JOIN users ON todos.user = users.id WHERE users.id = ?;");
        stmt.setInt(1, userId);
        ResultSet results = stmt.executeQuery();
        while (results.next()) {
            int id = results.getInt("todos.id");
            String text = results.getString("todos.text");
            boolean isDone = results.getBoolean("todos.is_done");
            items.add(new ToDoItem(id, text, isDone, userId));
        }
        return items;
    }

    public static ToDoItem selectItem(Connection conn, int todoId) throws SQLException {
        ToDoItem item = new ToDoItem();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM todos JOIN users ON todos.user = users.id WHERE todos.id = ?;");
        stmt.setInt(1, todoId);
        ResultSet results = stmt.executeQuery();
        if (results.next()) {
            String text = results.getString("todos.text");
            Boolean isDone = results.getBoolean("todos.is_done");
            int userId = results.getInt("todos.user");
            item = new ToDoItem(todoId, text, isDone, userId);
        }
        return item;
    }

    public static void deleteItem(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM todos WHERE id=?;");
        stmt.setInt(1, id);
        stmt.execute();
    }

    public static void updateItem(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("UPDATE todos SET is_done = NOT is_done WHERE id = ?");
        stmt.setInt(1, id);
        stmt.execute();
    }

    public static void insertUser(Connection conn, String name) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO users VALUES (NULL, ?);");
        stmt.setString(1, name);
        stmt.execute();
    }

    public static User selectUser(Connection conn, String name) throws SQLException {
        User user = new User();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE name = ?;");
        stmt.setString(1, name);
        ResultSet results = stmt.executeQuery();
        if (results.next()) {
            int id = results.getInt("id");
            user = new User(id, name);
        }
        return user;
    }

    public static int promptMenu() {
        System.out.println("[1] - Create to-do item");
        System.out.println("[2] - Toggle to-do item");
        System.out.println("[3] - Delete to-do item");
        System.out.println("[4] - List to-do items");
        System.out.println("Enter your menu choice:");
        return captureNumber();
    }

    public static String captureText() {
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }

    public static int captureNumber(){
        Scanner scanner = new Scanner(System.in);
        return scanner.nextInt();
    }

    public static void processMenuChoice(Connection conn, int input, int userId) throws SQLException {
        switch (input){
            case 1:
                System.out.println("Enter your to-do item:");
                insertItem(conn, captureText(), userId);
                break;
            case 2:
                System.out.println("Enter the number of the item you wish to toggle:");
                updateItem(conn, captureNumber());
                break;
            case 3:
                System.out.println("Enter the number of the item you wish to delete:");
                deleteItem(conn, captureNumber());
                break;
            case 4:
                ArrayList<ToDoItem> list = selectUsersItems(conn, userId);
                String checkbox = "[ ] ";
                System.out.println("\nMy ToDo Items:");
                for (ToDoItem item : list) {
                    if (item.isDone) {
                        checkbox = "[X] ";
                    }
                    System.out.printf("%s %d. %s\n", checkbox, item.id, item.text);
                }
                System.out.println("\n");
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    public static int login(Connection conn) throws SQLException {
        System.out.println("Enter your account name:");
        String acctName = captureText();
        User user = selectUser(conn, acctName);

        if(user.name==null){
            insertUser(conn, acctName);
            user = selectUser(conn, acctName);
        }
        return user.id;
    }

    public static void main(String[] args) throws SQLException {

        Server.createWebServer().start();
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        createTables(conn);

        int currentUserId = login(conn);

        while (true) {
            processMenuChoice(conn, promptMenu(), currentUserId);
        }
    }
}
