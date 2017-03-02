import com.sun.tools.javac.comp.Todo;
import org.h2.tools.Server;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Main {

    public static void insertItem(Connection conn, String text, int userId) throws SQLException {

        PreparedStatement stmt = conn.prepareStatement("INSERT INTO todos VALUES (NULL, ?, FALSE, ?);");
        stmt.setString(1, text);
        stmt.setInt(2, userId);
        stmt.execute();
    }

    public static ArrayList<ToDoItem> selectItems(Connection conn) throws SQLException {
        ArrayList<ToDoItem> items = new ArrayList<>();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM todos JOIN users ON todos.user = users.id;");
        ResultSet results = stmt.executeQuery();
        while (results.next()) {
            int id = results.getInt("todos.id");
            String text = results.getString("todos.text");
            boolean isDone = results.getBoolean("todos.is_done");
            int userId = results.getInt("todos.user");

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
        PreparedStatement stmt = conn.prepareStatement("DELETE * FROM todos WHERE id=?;");
        stmt.setInt(1,id);
        stmt.execute();
    }

    public static void updateItem(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("UPDATE todos SET is_done = NOT is_done WHERE id = ?");
        stmt.setInt(1, id);
        stmt.execute();
    }

    public static void insertUser(Connection conn, String name, String password) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO users VALUES (NULL, ?, ?);");
        stmt.setString(1, name);
        stmt.setString(2, password);
        stmt.execute();
    } //todo: write test

    public static User selectUser(Connection conn, String name) throws SQLException {
        User user = new User();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE name = ?;");
        stmt.setString(1, name);
        ResultSet results = stmt.executeQuery();
        if (results.next()) {
            int id = results.getInt("user_id");
            String password = results.getString("password");
            user = new User(id, name, password);
        }
        return user;
    } //todo: write test

    public static void main(String[] args) throws SQLException {

        Server.createWebServer().start();
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS todos (id IDENTITY, text VARCHAR, is_done BOOLEAN, user INT);");
        stmt.execute("CREATE TABLE IF NOT EXISTS users (id IDENTITY, name VARCHAR, password VARCHAR);");


        Scanner scanner = new Scanner(System.in);

//todo: make methods to get this info
        while (true) {
            System.out.println("[1] - Create to-do item");
            System.out.println("[2] - Toggle to-do item");
            System.out.println("[3] - List to-do items");

            String option = scanner.nextLine();
            if (option.equals("1")) {
                System.out.println("Enter your to-do item:");
                String text = scanner.nextLine();
                insertToDo(conn, text, user);

            } else if (option.equals("2")) {
                System.out.println("Enter the number of the item you wish to toggle:");
                int itemNumber = Integer.parseInt(scanner.nextLine());
                toggleToDo(conn, itemNumber);

            } else if (option.equals("3")) {
                int i = 1;
                ArrayList<ToDoItem> items = selectToDos(conn);
                for (ToDoItem item : items) {
                    String checkbox = "[ ] ";
                    if (item.isDone) {
                        checkbox = "[X] ";
                    }
                    System.out.printf("FORMATTED: %s %d. %s\n", checkbox, item.id, item.text);
                    i++;
                }
            } else {
                System.out.println("Invalid option.");
            }
        }
    }
}
