package client;

import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Repl {
    private final ChessClient client;

    public Repl(String url) {
        this.client = new ChessClient(url);
    }

    public void run() {
        System.out.println(SET_TEXT_COLOR_MAGENTA + "♟" + SET_TEXT_COLOR_YELLOW +
                            " Welcome to Jack's Chess Client " + SET_TEXT_COLOR_MAGENTA + "♟" + RESET_TEXT_COLOR);
        System.out.print(SET_TEXT_COLOR_BLUE + client.help() + RESET_TEXT_COLOR);

        Scanner scanner = new Scanner(System.in);
        String result = "";

        while (true) {
            printPrompt();
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }

            try {
                result = client.eval(line);

                if (result.equals("quit")) {
                    System.out.println(SET_TEXT_COLOR_YELLOW + "Goodbye!" + RESET_TEXT_COLOR);
                    break;
                }

                System.out.print(SET_TEXT_COLOR_BLUE + result + RESET_TEXT_COLOR + "\n");
            } catch (Throwable ex) {
                System.out.print(SET_TEXT_COLOR_RED + ex.getMessage() + RESET_TEXT_COLOR + "\n");
            }

        }
    }

    private void printPrompt() {
        if (client.isPostLogin()) {
            System.out.print(SET_TEXT_COLOR_GREEN + "LOGGED IN AS [" + client.getUsername() + "] >>>" + RESET_TEXT_COLOR);
        } else {
            System.out.print(SET_TEXT_COLOR_MAGENTA + "PRELOGIN >>> " + RESET_TEXT_COLOR);
        }

    }
}