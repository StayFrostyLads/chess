package service;

import chess.ChessGame;
import dataaccess.*;
import dataaccess.databaseimplementation.*;
import dataaccess.memoryimplementation.*;
import model.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class JoinGameTest {

    private GameService gameService;
    private AuthService authService;
    private GameDAO gameDAO;
    private AuthDAO authDAO;
    private UserDAO userDAO;
    private String validToken;
    private int gameID;

    @BeforeEach
    void setup() throws DataAccessException {
        gameDAO = new SQLGameDAO();
        authDAO = new SQLAuthDAO();
        userDAO = new SQLUserDAO();
        authService = new AuthService(authDAO, gameDAO, userDAO);
        gameService = new GameService(gameDAO, authService);

        authDAO.clear();
        gameDAO.clear();
        userDAO.clear();

        UserData user = new UserData("jack", "cs240test", "jneb2004.byu.edu");
        userDAO.createUser(user);

        validToken = authDAO.createAuth("jack").authToken();

        GameData game = gameDAO.createGame("Test Game");
        gameID = game.gameID();
    }

    @Test
    @DisplayName("Join as white throws ForbiddenException if slot taken")
    void joinWhiteTaken() {
        assertThrows(ForbiddenException.class,
                () -> gameService.joinGame(validToken, gameID, "WHITE"));
    }

    @Test
    @DisplayName("Join as black successfully")
    void joinBlackSuccess() throws DataAccessException {
        GameService.JoinGameResult result = gameService.joinGame(validToken, gameID, "BLACK");
        assertNotNull(result);

        GameData updated = gameDAO.getGame(gameID).orElseThrow();
        assertEquals("jack", updated.whiteUsername());
        assertEquals("jack", updated.blackUsername());
    }

    @Test
    @DisplayName("Joining again to same color throws ForbiddenException")
    void joinBlackTwice() {
        gameService.joinGame(validToken, gameID, "BLACK");
        assertThrows(ForbiddenException.class,
                () -> gameService.joinGame(validToken, gameID, "BLACK"));
    }

    @Test
    @DisplayName("Joining with invalid color throws BadRequestException")
    void joinInvalidColor() {
        assertThrows(BadRequestException.class,
                () -> gameService.joinGame(validToken, gameID, "PURPLE"));
    }

    @Test
    @DisplayName("Joining unknown game throws BadRequestException")
    void joinUnknownGame() {
        assertThrows(BadRequestException.class,
                () -> gameService.joinGame(validToken, 9999, "WHITE"));
    }

    @Test
    @DisplayName("Joining with invalid token throws AuthenticationException")
    void joinInvalidToken() {
        assertThrows(AuthenticationException.class,
                () -> gameService.joinGame("fake-token", gameID, "BLACK"));
    }
}