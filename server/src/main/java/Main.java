import server.Server;

public class Main {
    public static void main(String[] args) {
        Server testServer = new Server();
        int testPort = testServer.run(8080);
    }
}
