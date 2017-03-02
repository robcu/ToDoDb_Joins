

public class ToDoItem {
    int id;
    String text;
    boolean isDone;
    int user;


    public ToDoItem(int id, String text, boolean isDone, int user) {
        this.id = id;
        this.text = text;
        this.isDone = isDone;
        this.user = user;
    }

    public ToDoItem() {}
}