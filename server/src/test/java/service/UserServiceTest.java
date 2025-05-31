package service;

import dataaccess.*;
import dataaccess.memoryimplementation.*;
import model.*;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    private UserDAO userDAO;
    private AuthDAO authDAO;
    private AuthService authService;
    private UserService userService;

    @BeforeEach
    void setup() {
        userDAO = new InMemoryUserDAO();
        authDAO = new InMemoryAuthDAO();
        authService = new AuthService(authDAO, new InMemoryGameDAO(), userDAO);
        userService = new UserService(userDAO, authDAO, authService);
    }

    // Registration Tests

    @Test
    @DisplayName("Successful registration")
    void registerSuccessfully() throws DataAccessException {
        var result = userService.register("jack", "cs240test", "jneb2004@byu.edu");

        assertNotNull(result.authToken());
        assertEquals("jack", result.username());

        Optional<UserData> stored = userDAO.getUser("jack");
        assertTrue(stored.isPresent());
        assertEquals("jack", stored.get().username());
        assertTrue(BCrypt.checkpw("cs240test", stored.get().password()));
        assertEquals("jneb2004@byu.edu", stored.get().email());
    }

    @Test
    @DisplayName("Registering with existing user throws AlreadyTakenException")
    void registerExistingUser() throws DataAccessException {
        userDAO.createUser(new UserData("jack", BCrypt.hashpw("cs240test", BCrypt.gensalt()), "email"));

        assertThrows(AlreadyTakenException.class, () ->
                userService.register("jack", "newpass", "newemail"));
    }

    // Login Tests

    @Test
    @DisplayName("Successful login")
    void loginSuccessfully() throws DataAccessException {
        userDAO.createUser(new UserData("jack", BCrypt.hashpw("cs240test", BCrypt.gensalt()), "jneb2004@byu.edu"));

        var result = userService.login("jack", "cs240test");

        assertNotNull(result.authToken());
        assertEquals("jack", result.username());
        assertTrue(authDAO.getAuth(result.authToken()).isPresent());
    }

    @Test
    @DisplayName("Login fails with incorrect password")
    void loginIncorrectPassword() throws DataAccessException {
        userDAO.createUser(new UserData("jack", BCrypt.hashpw("cs240test", BCrypt.gensalt()), "email"));

        assertThrows(AuthenticationException.class,
                () -> userService.login("jack", "wrongpass"));
    }

    @Test
    @DisplayName("Login fails with unknown username")
    void loginUnknownUser() {
        assertThrows(AuthenticationException.class,
                () -> userService.login("notjack", "password"));
    }

    // Logout tests

    @Test
    @DisplayName("Successful logout")
    void logoutSuccessfully() throws DataAccessException {
        var result = userService.register("jack", "pass", "email");
        var logoutResult = userService.logout(result.authToken());

        assertTrue(logoutResult.success());
        assertTrue(authDAO.getAuth(result.authToken()).isEmpty());
    }

    @Test
    @DisplayName("Logout fails with invalid token")
    void logoutInvalidToken() {
        assertThrows(AuthenticationException.class,
                () -> userService.logout("invalid-token"));
    }
}
