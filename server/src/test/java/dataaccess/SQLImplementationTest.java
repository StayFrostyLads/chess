package dataaccess;

import chess.ChessGame;
import dataaccess.databaseimplementation.SQLAuthDAO;
import dataaccess.databaseimplementation.SQLGameDAO;
import dataaccess.databaseimplementation.SQLUserDAO;
import model.*;
import org.junit.jupiter.api.*;
import service.ForbiddenException;

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
        @DisplayName("Successfully Create a User")
        void successfullyCreateUser() throws DataAccessException {
            UserData user = new UserData("jack", "cs240test", "jneb2004@byu.edu");
            assertDoesNotThrow(() -> userDAO.createUser(user));
            Optional<UserData> testUser = userDAO.getUser("jack");
            assertTrue(testUser.isPresent());
        }

        @Test
        @DisplayName("Successfully Retrieve a User")
        void successfullyGetExistingUser() throws DataAccessException {
            var user = new UserData("liv", "volleyball", "ogg@gmail.com");
            userDAO.createUser(user);

            Optional<UserData> possibleUser = userDAO.getUser("liv");
            assertTrue(possibleUser.isPresent(),
                    "getUser() should return a UserData object after creation");
            assertEquals("liv", possibleUser.get().username());
            assertEquals("ogg@gmail.com", possibleUser.get().email());
        }

        @Test
        @DisplayName("Unsuccessfully Create a User That Already Exists")
        void unsuccessfullyCreateADuplicateUser() {
            UserData user = new UserData("liv", "volleyball", "ogg@gmail.com");

            assertDoesNotThrow(() -> userDAO.createUser(user));

            Exception ex = assertThrows(DataAccessException.class, () -> userDAO.createUser(user));
            assertTrue(ex.getMessage().toLowerCase().contains("already exists"));
        }

        @Test
        @DisplayName("Unsuccessfully Retrieve Non-Existent User")
        void unsuccessfullyGetUser() throws DataAccessException {
            Optional<UserData> falseData = userDAO.getUser("matthew");
            assertTrue(falseData.isEmpty(), "getUser() should return empty for non-existent user");
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

        @Test
        @DisplayName("Retrieving Non-existent Auth Token Returns Empty")
        void unsuccessfullyGetAuth() throws DataAccessException {
            Optional<AuthData> falseAuth = authDAO.getAuth("not-a-real-token");
            assertTrue(falseAuth.isEmpty(), "getAuth() returns empty for a non-existent auth");
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
        @DisplayName("Successfully Create A Game")
        void successfullyCreateAGame() throws DataAccessException {
            GameData newGame = gameDAO.createGame("Test Game");
            assertNotNull(newGame, "createGame() should not return null");
            assertEquals("Test Game", newGame.gameName());
            assertTrue(newGame.gameID() > 0, "Returned gameID should be positive");
            assertNull(newGame.whiteUsername());
            assertNull(newGame.blackUsername());
        }

        @Test
        @DisplayName("Successfully Retrieve A Game")
        void successfullyGetGame() throws DataAccessException {
            GameData gameData = gameDAO.createGame("Test Game");
            int id = gameData.gameID();

            Optional<GameData> testGame = gameDAO.getGame(id);
            assertTrue(testGame.isPresent(),
                    "getGame() should properly return a GameData object given a valid ID");
            GameData newData = testGame.get();
            assertEquals(id, newData.gameID());
            assertEquals("Test Game", newData.gameName());
            assertNull(newData.whiteUsername());
            assertNull(newData.blackUsername());
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

        @Test
        @DisplayName("Unsuccessfully Join Game With Invalid GameID")
        void unsuccessfullyJoinGameBadID() {
            int falseID = 3283;
            DataAccessException ex =
                    assertThrows(DataAccessException.class,
                            () -> gameDAO.joinGame(falseID, "liv", ChessGame.TeamColor.WHITE),
                            "Expecting DataAccessException to be thrown with invalid gameID");
            String message = ex.getMessage();
            assertTrue(message.contains("Invalid"), "Error message should mention invalid gameID");
        }

        @Test
        @DisplayName("Unsuccessfully Retrieve Game Given An Invalid ID")
        void unsuccessfullyGetGameInvalidID() throws DataAccessException {
            Optional<GameData> falseID = gameDAO.getGame(3283);
            assertTrue(falseID.isEmpty(), "getGame() on an invalid ID returns empty");
        }

        @Test
        @DisplayName("Joining a game where color taken throws ForbiddenException")
        void joinGameColorTaken() throws DataAccessException {
            GameData newGame = gameDAO.createGame("Already Taken");
            int id = newGame.gameID();

            gameDAO.joinGame(id, "liv", ChessGame.TeamColor.WHITE);

            Assertions.assertThrows(ForbiddenException.class,
                    () -> gameDAO.joinGame(id, "josh", ChessGame.TeamColor.WHITE),
                    "Expected ForbiddenException when Color Was Already Taken");
        }
    }
}
