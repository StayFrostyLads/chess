package service;

import chess.ChessGame;
import dataaccess.*;
import dataaccess.memoryimplementation.*;
import model.*;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.List;

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
        GameData game1 = gameDAO.createGame("One");
        GameData game2 = gameDAO.createGame("Two");

        gameDAO.joinGame(game1.gameID(), "jack", ChessGame.TeamColor.WHITE);
        gameDAO.joinGame(game2.gameID(), "liv", ChessGame.TeamColor.WHITE);
        gameDAO.joinGame(game2.gameID(), "josh", ChessGame.TeamColor.BLACK);

        GameService.ListGamesResult result = gameService.listGames(validToken);
        assertEquals(2, result.games().length);

        var games = result.games();
        List<String> names = Arrays.stream(games).map(GameService.GameEntry::gameName).toList();

        assertTrue(names.contains("One"));
        assertTrue(names.contains("Two"));
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