package service;

import chess.ChessGame;
import dataaccess.*;
import dataaccess.memoryimplementation.*;
import model.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class ListGamesTest {

    private GameService gameService;
    private GameDAO gameDAO;
    private AuthDAO authDAO;
    private AuthService authService;
    private String validToken;

    @BeforeEach
    void setup() throws DataAccessException {
        gameDAO = new InMemoryGameDAO();
        authDAO = new InMemoryAuthDAO();
        authService = new AuthService(authDAO, gameDAO, new InMemoryUserDAO());
        gameService = new GameService(gameDAO, authService);

        validToken = authDAO.createAuth("jack").authToken();
    }

    @Test
    @DisplayName("List is empty initially")
    void listGamesEmpty() throws DataAccessException {
        GameService.ListGamesResult result = gameService.listGames(validToken);
        assertNotNull(result.games());
        assertEquals(0, result.games().length);
    }

    @Test
    @DisplayName("List contains games after creation")
    void listGamesPopulated() throws DataAccessException {
        gameDAO.createGame(new GameData(1, "jack", null,
                            "One", new ChessGame()));
        gameDAO.createGame(new GameData(2, "liv", "josh",
                            "Two", new ChessGame()));

        GameService.ListGamesResult result = gameService.listGames(validToken);
        assertEquals(2, result.games().length);

        var names = result.games();
        assertEquals("One", names[0].gameName());
        assertEquals("Two", names[1].gameName());
    }

    @Test
    @DisplayName("Missing token throws AuthenticationException")
    void listMissingToken() {
        assertThrows(AuthenticationException.class,
                () -> gameService.listGames(null));
    }

    @Test
    @DisplayName("Invalid token throws AuthenticationException")
    void listInvalidToken() {
        assertThrows(AuthenticationException.class,
                () -> gameService.listGames("bad-token"));
    }
}
