import org.h2.tools.Server;
import spark.Session;
import spark.Spark;

import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static void insertToDo(Connection conn, String text, int user) throws SQLException {

        PreparedStatement stmt = conn.prepareStatement("INSERT INTO todos VALUES (NULL, ?, FALSE, ?);");
        stmt.setString(1, text);
        stmt.setInt(2, user);
        stmt.execute();
    }

    public static ArrayList<ToDoItem> selectToDos(Connection conn) throws SQLException {
        ArrayList<ToDoItem> items = new ArrayList<>();

        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM todos");
        ResultSet results = stmt.executeQuery();
        while (results.next()) {
            int id = results.getInt("id");
            String text = results.getString("text");
            boolean isDone = results.getBoolean("is_done");
            int user = results.getInt("user");
            items.add(new ToDoItem(id, text, isDone, user));
        }
        return items;
    }

    public static void toggleToDo(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("UPDATE todos SET is_done = NOT is_done WHERE id = ?");
        stmt.setInt(1, id);
        stmt.execute();
    }

    public static void insertUser(Connection conn, String name, String password) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO users VALUES (NULL, ?, ?);");

    }

    public static void main(String[] args) throws SQLException {

        Server.createWebServer().start();
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS todos (id IDENTITY, text VARCHAR, is_done BOOLEAN, user INT);");
        stmt.execute("CREATE TABLE IF NOT EXISTS users (user_id IDENTITY, name VARCHAR, password VARCHAR);");

        Spark.post("/create-user", (request, response) -> {
            String name = request.queryParams("username");
            String passWord = request.queryParams("password");


//            User user = users.get(name);
//            if (user == null) {                   //replace with putting user into table
//                user = new User(name);
//                user.passWord = passWord;
//                users.put(name, user);
//            }

            Session session = request.session();
            session.attribute("username", name);
            session.attribute("password", passWord);

            response.redirect("/");
            return "";
        });


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
