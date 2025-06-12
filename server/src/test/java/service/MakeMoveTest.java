package service;

import chess.ChessGame.TeamColor;
import chess.ChessMove;
import chess.ChessPosition;
import dataaccess.memoryimplementation.*;
import dataaccess.*;
import model.GameData;
import org.junit.jupiter.api.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class MakeMoveTest {
    private AuthDAO authDAO;
    private GameDAO gameDAO;
    private UserDAO userDAO;
    private AuthService authService;
    private GameService gameService;
    private String whiteAuthToken;
    private String blackAuthToken;
    private int gameID;

    @BeforeEach
    void setup() throws DataAccessException {
        authDAO     = new InMemoryAuthDAO();
        gameDAO     = new InMemoryGameDAO();
        userDAO     = new InMemoryUserDAO();
        authService = new AuthService(authDAO, gameDAO, userDAO);
        gameService = new GameService(gameDAO, authService);

        whiteAuthToken = authDAO.createAuth("jack").authToken();
        blackAuthToken = authDAO.createAuth("liv").authToken();

        GameService.CreateGameResult create =
                gameService.createGame("TestGame", whiteAuthToken);
        gameID = create.game().gameID();

        gameService.joinGame(whiteAuthToken, gameID, TeamColor.WHITE.name());
        gameService.joinGame(blackAuthToken, gameID, TeamColor.BLACK.name());
    }

    @Test
    @DisplayName("Successfully make a basic move, and the turn goes to the other player")
    void successfullyMakeMove() throws DataAccessException {
        ChessMove move = new ChessMove(
                new ChessPosition(2,5),
                new ChessPosition(4,5),
                null
        );

        GameService.MakeMoveResult moveResult = gameService.makeMove(whiteAuthToken, gameID, move);

        assertTrue(moveResult.success(),               "Move should succeed");
        assertEquals("jack moved e2→e4",       moveResult.notification());
        assertFalse(moveResult.isCheck(),              "Should not be check");
        assertFalse(moveResult.isCheckmate(),          "Should not be checkmate");
        assertEquals(TeamColor.BLACK,              moveResult.game().getTeamTurn(),
                "Turn should flip to black");

        Optional<GameData> stored = gameDAO.getGame(gameID);
        assertTrue(stored.isPresent());
        assertEquals(moveResult.game(), stored.get().game());
    }

    @Test
    @DisplayName("Unsuccessfully make an illegal move")
    void unsuccessfullyMakeIllegalMove() throws DataAccessException {
        ChessMove bad = new ChessMove(
                new ChessPosition(2,5),
                new ChessPosition(5,5),
                null
        );

        GameService.MakeMoveResult moveResult = gameService.makeMove(whiteAuthToken, gameID, bad);

        assertFalse(moveResult.success(),"Move should be rejected");
        assertNotNull(moveResult.message());
        assertTrue(moveResult.message().toLowerCase().contains("illegal"), "Error should mention illegality");
        assertEquals(TeamColor.WHITE, moveResult.game().getTeamTurn(),"Turn should stay on white");

        GameData stored = gameDAO.getGame(gameID).get();
        assertEquals(TeamColor.WHITE, stored.game().getTeamTurn());
    }
}