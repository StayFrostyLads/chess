import client.Repl;


public class ClientMain {
    public static void main(String[] args) {
        String url = "http://localhost:8080";
        if (args.length == 1) {
            url = args[0];
        }
        new Repl(url).run();
    }
}