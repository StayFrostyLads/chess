package service;

import chess.ChessGame;
import dataaccess.*;
import dataaccess.databaseimplementation.SQLAuthDAO;
import dataaccess.databaseimplementation.SQLGameDAO;
import dataaccess.databaseimplementation.SQLUserDAO;
import model.*;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

public class SQLImplementationTest {

    @BeforeEach
    public void clearDatabase() throws DataAccessException {
        new SQLAuthDAO().clear();
        new SQLGameDAO().clear();
        new SQLUserDAO().clear();
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

    @Nested
    class SQLGameDAOTests {
        private final GameDAO gameDAO = new SQLGameDAO();
        private final UserDAO userDAO = new SQLUserDAO();

        @BeforeEach
        void setupUser() throws DataAccessException {
            userDAO.createUser(new UserData("liv", "volleyball", "ogg@gmail.com"));
            userDAO.createUser(new UserData("josh", "salmon", "hhector@gmail.com"));
        }

        @Test
        @DisplayName("Successfully create and retrieve a game")
        void successfullyCreateAndRetrieveGame() throws DataAccessException {
            GameData newGame = gameDAO.createGame("Test Game");
            assertNotNull(newGame);
            assertEquals("Test Game", newGame.gameName());

            Optional<GameData> testGame = gameDAO.getGame(newGame.gameID());
            assertTrue(testGame.isPresent());
            assertEquals(newGame.gameID(), testGame.get().gameID());
            assertEquals("Test Game", testGame.get().gameName());
        }

        @Test
        @DisplayName("Unsuccessfully create a game with a null name")
        void unsuccessfullyCreateGame() {
            assertThrows(DataAccessException.class, () -> {
                gameDAO.createGame(null);
            });
        }

        @Test
        @DisplayName("Successfully list game after creation")
        void successfullyListGames() throws DataAccessException {
            gameDAO.clear();
            List<GameData> emptyList = gameDAO.listGames();
            assertNotNull(emptyList);
            assertTrue(emptyList.isEmpty(), "Game list should start out empty");

            GameData game1 = gameDAO.createGame("Rogue One");
            GameData game2 = gameDAO.createGame("Rogue Two");

            List<GameData> gameList = gameDAO.listGames();
            assertNotNull(gameList);
            assertEquals(2, gameList.size(), "Game list should show 2 games");

            assertTrue(gameList.stream().anyMatch(game -> game.gameID() == game1.gameID()),
                    "listGames() should show the game 'Rogue One'");
            assertTrue(gameList.stream().anyMatch(game -> game.gameID() == game2.gameID()),
                    "listGames() should show the game 'Rogue Two'");
        }

        @Test
        @DisplayName("Successfully join game as white and black")
        void joinGameWhiteThenBlack() throws DataAccessException{
            GameData newGame = gameDAO.createGame("Test Game");
            int id = newGame.gameID();

            gameDAO.joinGame(id, "liv", ChessGame.TeamColor.WHITE);
            Optional<GameData> livGame = gameDAO.getGame(id);
            assertTrue(livGame.isPresent());
            assertEquals("liv", livGame.get().whiteUsername(),
                    "White player should show as 'liv'");
            assertNull(livGame.get().blackUsername(),
                    "There should be no player as black right now");

            gameDAO.joinGame(id, "josh", ChessGame.TeamColor.BLACK);
            Optional<GameData> joshGame = gameDAO.getGame(id);
            assertTrue(joshGame.isPresent());
            assertEquals("liv", joshGame.get().whiteUsername(),
                    "White player should show as 'liv'");
            assertEquals("josh", joshGame.get().blackUsername(),
                    "Black player should show as 'josh'");
        }

    }
}
