package service;

import dataaccess.*;
import dataaccess.databaseimplementation.SQLUserDAO;
import model.*;
import org.junit.jupiter.api.*;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

public class SQLImplementationTest {

    @BeforeEach
    public void clearDatabase() {
        new SQLUserDAO().clear();
    }

    @Nested
    class SQLUserDaoTests {
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
}
