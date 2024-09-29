public class Main {
    public static void main(String[] args) {
        Application app = Application.get_instance();
        int code = app.run();
        app.handle_exit(code);
    }
}
