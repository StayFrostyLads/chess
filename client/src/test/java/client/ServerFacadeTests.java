package client;

import com.mysql.cj.log.Log;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;
import server.ServerFacade;
import server.ServerFacade.*;


import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;
    private static String url;

    private final String username = "jack";
    private final String password = "cs240test";
    private final String email = "jneb2004@byu.edu";
    private final String username2 = "liv";
    private final String password2 = "volleyball";
    private final String email2 = "ogg@gmail.com";

    @BeforeAll
    public static void init() {
        server = new Server();
        int port = server.run(0);
        url = "http://localhost:" + port;
        facade = new ServerFacade(url);
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void clearDatabases() {
        facade.setAuthToken(null);
        facade.clearDatabases();
    }

    @Test
    @DisplayName("Successfully register a new user")
    void successfullyRegister() {
        AuthResult result = facade.register(new ServerFacade.RegisterRequest(username, password, email));
        assertTrue(result.success(), "Registration should have been successful");
        assertNotNull(result.authToken(), "Auth Token should not be null");
        assertEquals(username, result.username(), "Returned username should match the requested username");
    }

    @Test
    @DisplayName("Unsuccessfully register a new user due to duplicated username")
    void unsuccessfullyRegisterGivenDuplicateUsername() {
        AuthResult firstResult = facade.register(new ServerFacade.RegisterRequest(username2, password2, email2));
        assertTrue(firstResult.success());

        AlreadyTakenException ex = assertThrows(AlreadyTakenException.class,
                () -> facade.register(new ServerFacade.RegisterRequest(username2, "salmon",
                                                                "steakburger@gmail.com"))
        );
        assertTrue(ex.getMessage().toLowerCase().contains("already taken"));
    }

    @Test
    @DisplayName("Successfully login with valid credentials")
    void successfullyLogin() {
        facade.register(new ServerFacade.RegisterRequest(username, password, email));

        AuthResult loginResult = facade.login(new ServerFacade.LoginRequest(username, password));
        assertTrue(loginResult.success(), "Login should succeed given valid credentials");
        assertNotNull(loginResult.authToken(), "Login should have registered a new auth token");
        assertEquals(username, loginResult.username());

        facade.setAuthToken(null);
    }

    @Test
    @DisplayName("Unsuccessfully login with invalid credentials")
    void unsuccessfullyLogin() {
        AuthenticationException ex = assertThrows(AuthenticationException.class,
                () -> facade.login(new ServerFacade.LoginRequest("fakeuser", "libraryman"))
        );
        assertTrue(ex.getMessage().toLowerCase().contains("invalid"),
                "Error message should mention authentication failure");
    }

    @Test
    @DisplayName("Successfully logout with valid auth token")
    void successfullyLogout() {
        facade.register(new ServerFacade.RegisterRequest(username, password, email));
        AuthResult auth = facade.login(new ServerFacade.LoginRequest(username, password));
        facade.setAuthToken(auth.authToken());

        LogoutResult logoutResult = facade.logout();
        assertTrue(logoutResult.success(), "Logout should return true for success");
        assertNotNull(logoutResult.message(), "Logout should return: User successfully logged out");

        ServerFacade tempClient = new ServerFacade(url);
        tempClient.setAuthToken(auth.authToken());
        AuthenticationException ex = assertThrows(
                AuthenticationException.class,
                tempClient::listGames
        );
        assertTrue(ex.getMessage().toLowerCase().contains("invalid auth token"),
                "After logging out, the old auth token should no longer work");
    }

    @Test
    @DisplayName("Unsuccessfully logout due to a missing token")
    void unsuccessfullyLogoutNoToken() {
        facade.setAuthToken(null);
        AuthenticationException ex = assertThrows(AuthenticationException.class,
                () -> facade.logout()
        );
        assertTrue(ex.getMessage().toLowerCase().contains("missing"),
                "Attempting to logout with no auth token should inform the user why they can't logout");
    }

    @Test
    @DisplayName("Successfully list an empty list of games")
    void successfullyListGamesEmpty() {
        facade.register(new ServerFacade.RegisterRequest(username, password, email));
        AuthResult auth = facade.login(new ServerFacade.LoginRequest(username, password));
        facade.setAuthToken(auth.authToken());

        ListGamesResult listGamesResult = facade.listGames();
        assertTrue(listGamesResult.success(), "listGames() should return successful");
        assertNotNull(listGamesResult.games(), "Games should not be null");
        assertEquals(0, listGamesResult.games().length, "There should be zero games listed");
    }

    @Test
    @DisplayName("Unsuccessfully list games with no auth token")
    void unsuccessfullyListGamesNoToken() {
        facade.setAuthToken(null);
        AuthenticationException ex = assertThrows(
                AuthenticationException.class,
                () -> facade.listGames()
        );
        assertTrue(ex.getMessage().toLowerCase().contains("missing"),
                "Attempting to list the games with no auth token should inform the user why they can't");
    }

    @Test
    @DisplayName("Successfully create a game")
    void successfullyCreateGame() {
        facade.register(new ServerFacade.RegisterRequest(username, password, email));
        AuthResult auth = facade.login(new ServerFacade.LoginRequest(username, password));
        facade.setAuthToken(auth.authToken());

        CreateGameResult createGameResult = facade.createGame(new ServerFacade.CreateGameRequest("Test Game"));

        assertTrue(createGameResult.success(), "Create game should return success if provided a valid name");
        assertNotNull(createGameResult.game(), "The returned GameEntry shouldn't be null");

        GameEntry gameEntry = createGameResult.game();
        assertEquals("Test Game", gameEntry.gameName(), "Returned game name should match provided one");
        assertTrue(gameEntry.gameID() > 0, "GameID should be a positive integer");
        assertTrue(gameEntry.whiteUsername().isBlank(), "White player spot should be open");
        assertTrue(gameEntry.blackUsername().isBlank(), "Black player spot should be open");
    }

    @Test
    @DisplayName("Unsuccessfully create a game with no given game name")
    void unsuccessfullyCreateGameInvalidName() {
        facade.register(new ServerFacade.RegisterRequest(username, password, email));
        AuthResult auth = facade.login(new ServerFacade.LoginRequest(username, password));
        facade.setAuthToken(auth.authToken());

        BadRequestException ex1 = assertThrows(BadRequestException.class,
                () -> facade.createGame(new CreateGameRequest(null))
        );
        assertTrue(ex1.getMessage().toLowerCase().contains("missing"),
                "User should be informed why their game failed to create");

        BadRequestException ex2 = assertThrows(BadRequestException.class,
                () -> facade.createGame(new CreateGameRequest(""))
        );
        assertTrue(ex2.getMessage().toLowerCase().contains("empty"),
                "User should be informed why their game failed to create");
    }

    @Test
    @DisplayName("Successfully join a game")
    void successfullyJoinGame() {
        facade.register(new ServerFacade.RegisterRequest(username, password, email));
        AuthResult auth1 = facade.login(new ServerFacade.LoginRequest(username, password));
        facade.setAuthToken(auth1.authToken());

        CreateGameResult createGameResult = facade.createGame(new CreateGameRequest("Join Game Test"));
        GameEntry gameEntry = createGameResult.game();
        int gameID = gameEntry.gameID();

        JoinGameResult joinGameResult1 = facade.joinGame(new JoinGameRequest(gameID, "WHITE"));
        GameEntry joinGameEntry1 = joinGameResult1.game();

        facade.register(new ServerFacade.RegisterRequest(username2, password2, email2));
        AuthResult auth2 = facade.login(new ServerFacade.LoginRequest(username2, password2));
        facade.setAuthToken(auth2.authToken());

        JoinGameResult joinGameResult2 = facade.joinGame(new JoinGameRequest(gameID, "BLACK"));
        GameEntry joinGameEntry2 = joinGameResult2.game();

        assertTrue(joinGameResult1.success(), "Jack should have joined the game successfully as white");
        assertTrue(joinGameResult2.success(), "Liv should have joined the game successfully as black");
        assertEquals(gameID, joinGameEntry1.gameID(), "Returned gameID should match the created gameID");
        assertEquals(gameID, joinGameEntry2.gameID(), "Returned gameID should match the created gameID");
        assertEquals(username, joinGameEntry1.whiteUsername(), "White player should be jack");
        assertEquals(username2, joinGameEntry2.blackUsername(), "Black player should be liv");
    }

    @Test
    @DisplayName("Unsuccessfully join a game due to a missing auth token, invalid gameID or invalid color")
    void unsuccessfullyJoinGame() {
        AuthResult jackRegisterAuth = facade.register(new RegisterRequest(username, password, email));
        AuthResult jackAuth = facade.login(new LoginRequest(username, password));
        facade.setAuthToken(jackAuth.authToken());

        CreateGameResult createGameResult = facade.createGame(new CreateGameRequest("Join Game Test"));
        int realGameID = createGameResult.game().gameID();

        facade.setAuthToken(null);
        AuthenticationException exAuth = assertThrows(AuthenticationException.class,
                () -> facade.joinGame(new JoinGameRequest(realGameID, "WHITE"))
        );
        assertTrue(exAuth.getMessage().toLowerCase().contains("missing"),
                "Missing token should yield AuthenticationException");

        facade.setAuthToken(jackAuth.authToken());
        BadRequestException exBadGameID = assertThrows(BadRequestException.class,
                () -> facade.joinGame(new JoinGameRequest(-11, "WHITE"))
        );
        assertTrue(exBadGameID.getMessage().toLowerCase().contains("invalid"),
                "Invalid game ID should yield BadRequestException");

        BadRequestException exInvalidColor = assertThrows(BadRequestException.class,
                () -> facade.joinGame(new JoinGameRequest(realGameID, "PURPLE"))
        );
        assertTrue(exInvalidColor.getMessage().toLowerCase().contains("invalid"),
                "Invalid player color should yield BadRequestException");

        AuthResult livRegisterAuth = facade.register(new RegisterRequest(username2, password2, email2));
        AuthResult livAuth = facade.login(new LoginRequest(username2, password2));
        facade.setAuthToken(livAuth.authToken());
        facade.joinGame(new JoinGameRequest(realGameID, "BLACK"));

        AuthResult joshRegisterAuth = facade.register(new RegisterRequest(
                                                    "josh", "salmon", "valorant@gmail.com"));
        AuthResult joshAuth = facade.login(new LoginRequest("josh", "salmon"));
        facade.setAuthToken(joshAuth.authToken());

        ForbiddenException exForbidden = assertThrows(ForbiddenException.class,
                () -> facade.joinGame(new JoinGameRequest(realGameID, "BLACK"))
        );
        assertTrue(exForbidden.getMessage().toLowerCase().contains("already playing")
                || exForbidden.getMessage().toLowerCase().contains("forbidden"),
                "Attempting to join twice as black should yield ForbiddenException"
                );
    }

}
