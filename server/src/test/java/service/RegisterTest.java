package service;

import dataaccess.*;
import dataaccess.implementation.*;
import model.*;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class RegisterTest {

    private UserDAO userDAO;
    private RegisterService registerService;

    @BeforeEach
    public void setup() {
        userDAO = new InMemoryUserDAO();
        registerService = new RegisterService(userDAO);
    }

    @Test
    @DisplayName("Successful New User Registration")
    public void registerSuccessfully() throws DataAccessException {
        RegisterRequest req = new RegisterRequest("jack",
                                                "cs240test",
                                                "jneb2004.byu.edu"
        );
        RegisterResult result = registerService.register(req);

        assertNotNull(result.authToken(), "Auth token was not generated correctly");
        assertEquals("jack", result.username());

        Optional<UserData> store = userDAO.getUser("jack");
        assertTrue(store.isPresent(), "UserData was not properly stored");
        assertEquals("jack", store.get().username());
        assertEquals(Integer.toHexString("cs240test".hashCode()),
                                        store.get().password(),
                                "Password was not properly hashed"
        );
        assertEquals("jneb2004.byu.edu", store.get().email());
    }

    @Test
    @DisplayName("Unsuccessful Existing User Registration")
    public void registerAlreadyExistingUser() throws DataAccessException {
        UserData existingUser = new UserData("liv",
                                            Integer.toHexString("volleyball".hashCode()),
                                            "ogg@gmail.com"
        );
        userDAO.createUser(existingUser);

        RegisterRequest request = new RegisterRequest("liv",
                                            "golf",
                                                "goo@yahoo.com"
        );

        assertThrows(AlreadyTakenException.class, () -> registerService.register(request),
            "AlreadyTakenException was not properly thrown for an already existing user"
        );

        Optional<UserData> store = userDAO.getUser("liv");
        assertTrue(store.isPresent());
        assertEquals(existingUser, store.get());
    }

}
