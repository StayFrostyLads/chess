package service;

import dataaccess.*;
import dataaccess.implementation.*;
import model.*;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class LogoutTest {

    private AuthDAO authDAO;
    private LogoutService logoutService;
    private String validToken;

    @BeforeEach
    public void setup() throws DataAccessException {
        authDAO = new InMemoryAuthDAO();
        logoutService = new LogoutService(authDAO);
        AuthData auth = authDAO.createAuth("jack");
        validToken = auth.authToken();
    }

    @Test
    @DisplayName("Successful Logout")
    public void logoutSuccessfully() {
        LogoutRequest request = new LogoutRequest(validToken);
        LogoutResult result = logoutService.logout(request);
        assertTrue(result.success());
        assertTrue(authDAO.getAuth(validToken).isEmpty());
    }

    @Test
    @DisplayName("Wrong authToken throws AuthenticationException")
    void logoutWrongToken() {
        assertThrows(
                AuthenticationException.class,
                () -> logoutService.logout(new LogoutRequest("some-invalid-token"))
        );
    }

}
