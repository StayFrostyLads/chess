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

}
