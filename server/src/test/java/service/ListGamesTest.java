package service;

import chess.ChessGame;
import dataaccess.implementation.*;
import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class ListGamesTest {

    private GameDAO gameDAO;
    private ListGamesService gamesService;
    private String validToken;

    @BeforeEach
    void setup() throws DataAccessException {
        AuthDAO authDAO = new InMemoryAuthDAO();
        gameDAO = new InMemoryGameDAO();
        AuthData auth = authDAO.createAuth("jack");
        validToken = auth.authToken();

        gamesService = new ListGamesService(authDAO, gameDAO);
    }

    @Test
    @DisplayName("Empty game list correctly displays no games")
    void listGamesEmpty() {
        ListGamesRequest request = new ListGamesRequest(validToken);
        ListGamesResult result = gamesService.listGames(request);

        assertNotNull(result.games(), "games() should not return as null");
        assertEquals(0, result.games().length,
                "Expecting no games displayed while the DAO is empty");
    }

    @Test
    @DisplayName("Game list correctly displays after given valid authToken")
    void listGamesAfterValidated() throws DataAccessException {
        GameData game1 = new GameData(1, "jack",
                        null, "First Game", new ChessGame());
        GameData game2 = new GameData(2, "liv",
                        "josh", "Second Game", new ChessGame());
        gameDAO.createGame(game1);
        gameDAO.createGame(game2);

        ListGamesRequest request = new ListGamesRequest(validToken);
        ListGamesResult result = gamesService.listGames(request);
        GameEntry[] entries = result.games();

        assertEquals(2, entries.length, "There were not 2 games properly created");

        GameEntry entry1 = new GameEntry(1, "First Game",
                            "jack", null);
        GameEntry entry2 = new GameEntry(2, "Second Game",
                            "liv", "josh");

        assertArrayEquals(new GameEntry[]{entry1, entry2}, entries,
                "Returned entries did not match entries in the database");
    }

    @Test
    @DisplayName("Missing token throws an AuthenticationException")
    void listGamesMissingToken() {
        ListGamesRequest request = new ListGamesRequest(null);
        assertThrows(AuthenticationException.class, () -> gamesService.listGames(request),
                "Expected AuthenticationException when authToken is missing"
                );
    }

    @Test
    @DisplayName("Invalid token throws an AuthenticationException")
    void listGamesInvalidToken() {
        ListGamesRequest request = new ListGamesRequest("fake-token-haha");
        assertThrows(AuthenticationException.class, () -> gamesService.listGames(request),
                "Expected AuthenticationException when authToken is invalid"
        );
    }



}
