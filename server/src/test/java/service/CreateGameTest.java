package service;

import dataaccess.memoryimplementation.*;
import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CreateGameTest {

    private GameDAO gameDAO;
    private AuthDAO authDAO;
    private UserDAO userDAO;
    private GameService gameService;
    private AuthService authService;
    private String validToken;

    @BeforeEach
    void setup() throws DataAccessException {
        authDAO = new InMemoryAuthDAO();
        gameDAO = new InMemoryGameDAO();
        userDAO = new InMemoryUserDAO();
        authService = new AuthService(authDAO, gameDAO, userDAO);
        gameService = new GameService(gameDAO, authService);

        AuthData auth = authDAO.createAuth("jack");
        validToken = auth.authToken();
    }

    @Test
    @DisplayName("Successful game creation")
    void createGameSuccessfully() throws DataAccessException {
        String name = "Test Game";
        GameService.CreateGameResult result = gameService.createGame(name, validToken);
        GameService.GameEntry gameEntry = result.game();

        assertTrue(gameEntry.gameID() > 0, "gameID should have a positive value");

        List<GameData> gameList = gameDAO.listGames();
        assertEquals(1, gameList.size(),
                "The DAO should have exactly 1 game stored right now");
        GameData store = gameList.getFirst();
        assertEquals(gameEntry.gameID(), store.gameID());
        assertNull(store.whiteUsername());
        assertNull(store.blackUsername());
        assertEquals(name, store.gameName());
        assertNotNull(store.game(), "GameData in the database should contain a ChessGame instance");
    }

    @Test
    @DisplayName("Missing token throws an AuthenticationException")
    void createGameMissingToken() {
        assertThrows(AuthenticationException.class,
                () -> gameService.createGame("game", null),
                "Expected AuthenticationException when authToken is null"
        );
    }

    @Test
    @DisplayName("Invalid token throws an AuthenticationException")
    void createGameInvalidToken() {
        assertThrows(AuthenticationException.class,
                () -> gameService.createGame("game", "not-a-real-token"),
                "Expected AuthenticationException when authToken is invalid"
        );
    }

    @Test
    @DisplayName("Missing gameName throws a BadRequestException")
    void createGameMissingName() {
        assertThrows(BadRequestException.class, () -> gameService.createGame(null, validToken),
                "Expected BadRequestException when gameName is null"
                );
        assertThrows(BadRequestException.class, () -> gameService.createGame("", validToken),
                "Expected BadRequestException when gameName is empty"
        );
    }

}
