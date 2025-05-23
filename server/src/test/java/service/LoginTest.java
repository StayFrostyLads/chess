package service;

import dataaccess.*;
import dataaccess.implementation.*;
import model.*;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class LoginTest {

    private UserDAO userDAO;
    private AuthDAO authDAO;
    private LoginService loginService;

    @BeforeEach
    public void setup() throws DataAccessException {
        userDAO = new InMemoryUserDAO();
        authDAO = new InMemoryAuthDAO();
        loginService = new LoginService(userDAO, authDAO);

        String hashed = Integer.toHexString("cs240test".hashCode());
        userDAO.createUser(new UserData("jack", hashed, "jneb2004@byu.edu"));
    }

    @Test
    @DisplayName("Successful Login")
    public void loginSuccessfully() throws DataAccessException {
        LoginRequest req = new LoginRequest("jack",
                "cs240test"
        );
        LoginResult result = loginService.login(req);

        assertNotNull(result.authToken(), "Auth token was not generated correctly");
        assertEquals("jack", result.username());

        Optional<AuthData> auth = authDAO.getAuth(result.authToken());
        assertTrue(auth.isPresent(), "AuthDAO does not contain the authToken");
        assertEquals("jack", auth.get().username());
    }

    @Test
    @DisplayName("Wrong password throws AuthenticationException")
    public void loginWrongPassword() throws AuthenticationException {
        LoginRequest request = new LoginRequest("jack", "wrongpassword");
        assertThrows(AuthenticationException.class, () -> loginService.login(request),
                "AuthenticationException was not properly thrown for a wrong password"
        );
    }

    @Test
    @DisplayName("Invalid username throws AuthenticationException")
    public void loginInvalidUser() throws AuthenticationException {
        LoginRequest request = new LoginRequest("liv", "volleyball");
        assertThrows(AuthenticationException.class, () -> loginService.login(request),
                "AuthenticationException was not properly thrown for a non-existing user"
        );
    }

}
