package service;

import chess.ChessGame;
import dataaccess.implementation.*;
import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CreateGameTest {

    private GameDAO gameDAO;
    private CreateGameService gameService;
    private String validToken;

    @BeforeEach
    void setup() throws DataAccessException {
        AuthDAO authDAO = new InMemoryAuthDAO();
        gameDAO = new InMemoryGameDAO();
        AuthData auth = authDAO.createAuth("jack");
        validToken = auth.authToken();

        gameService = new CreateGameService(authDAO, gameDAO);
    }

    @Test
    @DisplayName("Successful game creation")
    void createGameSuccessfully() {
        String name = "Test Game";
        CreateGameService.Request request = new CreateGameService.Request(validToken, name);
        CreateGameService.Result result = gameService.createGame(request);

        assertTrue(result.gameID() > 0, "gameID should have a positive value");

        List<GameData> gameList = gameDAO.listGames();
        assertEquals(1, gameList.size(),
                "The DAO should have exactly 1 game stored right now");
        GameData store = gameList.getFirst();
        assertEquals(result.gameID(), store.gameID());
        assertEquals("jack", store.whiteUsername());
        assertNull(store.blackUsername());
        assertEquals(name, store.gameName());
        assertNotNull(store.game(), "GameData in the database should contain a ChessGame instance");

    }

    @Test
    @DisplayName("Missing token throws an AuthenticationException")
    void createGameMissingToken() {
        CreateGameService.Request request = new CreateGameService.Request(null, "not-game");
        assertThrows(AuthenticationException.class, () -> gameService.createGame(request),
                "Expected AuthenticationException when authToken is missing"
        );
    }

    @Test
    @DisplayName("Invalid token throws an AuthenticationException")
    void createGameInvalidToken() {
        CreateGameService.Request request = new CreateGameService.Request("fake-token-haha",
                "not-game");
        assertThrows(AuthenticationException.class, () -> gameService.createGame(request),
                "Expected AuthenticationException when authToken is invalid"
        );
    }

    @Test
    @DisplayName("Missing gameName throws a BadRequestException")
    void createGameMissingName() {
        CreateGameService.Request request1 = new CreateGameService.Request(validToken, null);
        CreateGameService.Request request2 = new CreateGameService.Request(validToken, "");
        assertThrows(BadRequestException.class, () -> gameService.createGame(request1),
                "Expected BadRequestException when gameName is null"
                );
        assertThrows(BadRequestException.class, () -> gameService.createGame(request2),
                "Expected BadRequestException when gameName is empty"
        );
    }

}
