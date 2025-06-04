package client;

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
        assertTrue(ex1.getMessage().toLowerCase().contains("empty"),
                "User should be informed why their game failed to create");
    }

}
