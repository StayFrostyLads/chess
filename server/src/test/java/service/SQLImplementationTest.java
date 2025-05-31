package service;

import dataaccess.*;
import dataaccess.databaseimplementation.SQLAuthDAO;
import dataaccess.databaseimplementation.SQLUserDAO;
import model.*;
import org.junit.jupiter.api.*;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

public class SQLImplementationTest {

    @BeforeEach
    public void clearDatabase() {
        new SQLUserDAO().clear();
        new SQLAuthDAO().clear();
    }

    @Nested
    class SQLUserDAOTests {
        private final UserDAO userDAO = new SQLUserDAO();

        @Test
        @DisplayName("Successfully Create and Retrieve User")
        void successfullyCreateAndRetrieve() throws DataAccessException {
            UserData user = new UserData("jack", "cs240test", "jneb2004@byu.edu");
            assertDoesNotThrow(() -> userDAO.createUser(user));
            Optional<UserData> testUser = userDAO.getUser("jack");
            assertTrue(testUser.isPresent());
            assertEquals("jack", testUser.get().username());
        }

        @Test
        @DisplayName("Unsuccessfully Create a User That Already Exists")
        void unsuccessfullyCreateADuplicateUser() {
            UserData user = new UserData("liv", "volleyball", "ogg@gmail.com");

            assertDoesNotThrow(() -> userDAO.createUser(user));

            Exception ex = assertThrows(DataAccessException.class, () -> userDAO.createUser(user));
            assertTrue(ex.getMessage().toLowerCase().contains("already exists"));
        }
    }

    @Nested
    class SQLAuthDAOTests {
        private final AuthDAO authDAO = new SQLAuthDAO();

        @Test
        @DisplayName("Successfully register a new auth token")
        void successfullyCreateNewToken() throws DataAccessException {
            SQLUserDAO userDAO = new SQLUserDAO();
            UserData user = new UserData("liv", "volleyball", "ogg@gmail.com");
            userDAO.createUser(user);

            AuthData token = authDAO.createAuth("liv");

            Optional<AuthData> testToken = authDAO.getAuth(token.authToken());
            assertTrue(testToken.isPresent(), "Auth Token should be found in the database after creation");
            assertEquals("liv", testToken.get().username());
        }

        @Test
        @DisplayName("Fail to create an auth token for non-existent user")
        void failCreateTokenUnknownUser() {
            Exception ex = assertThrows(DataAccessException.class, () -> authDAO.createAuth("fakejack"));
            assertTrue(ex.getMessage().toLowerCase().contains("error creating auth token"),
                    "Expected error due to failed auth token creation");
        }

        @Test
        @DisplayName("Successfully remove an auth token")
        void successfullyRemoveAuthToken() throws DataAccessException {
            SQLUserDAO userDAO = new SQLUserDAO();
            UserData user = new UserData("liv", "volleyball", "ogg@gmail.com");
            userDAO.createUser(user);

            AuthData token = authDAO.createAuth("liv");

            assertTrue(authDAO.removeToken(token.authToken()), "Auth Token should be removed");

            Optional<AuthData> authResult = authDAO.getAuth(token.authToken());
            assertTrue(authResult.isEmpty(), "Auth Token should no longer exist after removal");
        }

        @Test
        @DisplayName("Fail to remove non-existent auth token")
        void failRemoveNonexistentToken() throws DataAccessException {
            boolean removed = authDAO.removeToken("not-a-token");
            assertFalse(removed, "Expected false when trying to remove an auth token that doesn't exist");
        }

        @Test
        @DisplayName("Cascade Delete in Clear Removes Auth Token When User is Deleted")
        void cascadeDeletesAuthToken() throws DataAccessException {
            SQLUserDAO userDAO = new SQLUserDAO();
            UserData user = new UserData("liv", "volleyball", "ogg@gmail.com");
            userDAO.createUser(user);

            AuthData token = authDAO.createAuth("liv");

            userDAO.clear();

            Optional<AuthData> authResult = authDAO.getAuth(token.authToken());
            assertTrue(authResult.isEmpty(), "Auth Token should be deleted due to foreign key cascade");
        }
    }
}
