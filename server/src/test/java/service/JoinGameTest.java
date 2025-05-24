package service;

import chess.ChessGame;
import dataaccess.implementation.*;
import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JoinGameTest {

    private GameDAO gameDAO;
    private JoinGameService gameService;
    private String validToken;
    private int gameID;


    @BeforeEach
    void setup() throws DataAccessException {
        AuthDAO authDAO = new InMemoryAuthDAO();
        gameDAO = new InMemoryGameDAO();
        AuthData auth = authDAO.createAuth("jack");
        validToken = auth.authToken();

        GameData originalGame = new GameData(0, "jack",
                                null, "The first of many", new ChessGame());
        gameID = gameDAO.createGame(originalGame);
    }

    @Test
    @DisplayName("Join as white fail because occupied")
    void joinWhiteAlreadyTaken() {
        JoinGameService.Request request = new JoinGameService.Request(validToken, "WHITE", gameID);
        assertThrows(ForbiddenException.class, () -> gameService.joinGame(request),
                "Expected ForbiddenException for occupied white slot");
    }

    @Test
    @DisplayName("Join as black success because unoccupied")
    void joinBlackSuccessfully() {
        JoinGameService.Request request = new JoinGameService.Request(validToken, "WHITE", gameID);
        JoinGameService.Result result = gameService.joinGame(request);
        assertNotNull(result, "Empty slot for black expected");

        GameData updatedData = gameDAO.getGame(gameID).orElseThrow();
        assertEquals("jack", updatedData.whiteUsername(), "White user remains unchanged");
        assertEquals("jack", updatedData.blackUsername(), "Black user set to jack");
    }

    @Test
    @DisplayName("Join twice as black unsuccessfully because illegal")
    void joinBlackTwice() {
        gameService.joinGame(new JoinGameService.Request(validToken, "BLACK", gameID));
        assertThrows(ForbiddenException.class, () -> gameService.joinGame(
                new JoinGameService.Request(validToken, "BLACK", gameID)
                ),
                "Expected ForbiddenException for occupied black slot"
        );
    }

    @Test
    @DisplayName("Invalid color throws BadRequestException")
    void joinBadColor() {
        JoinGameService.Request request = new JoinGameService.Request(validToken, "PURPLE", gameID);
        assertThrows(BadRequestException.class,
                () -> gameService.joinGame(request),
                "Expected BadRequestException for invalid color");
    }

    @Test
    @DisplayName("Unknown game throws BadRequestException")
    void joinUnknownGame() {
        JoinGameService.Request request = new JoinGameService.Request(validToken,
                                                            "WHITE", 4444);
        assertThrows(BadRequestException.class,
                () -> gameService.joinGame(request),
                "Expected BadRequestException for unknown game");
    }

    @Test
    @DisplayName("Invalid token throws AuthenticationException")
    void joinInvalidToken() {
        JoinGameService.Request request = new JoinGameService.Request("not-a-real-token",
                                                            "WHITE", gameID);
        assertThrows(AuthenticationException.class,
                () -> gameService.joinGame(request),
                "Expected AuthenticationException when an invalid token is given");
    }

    @Test
    @DisplayName("Missing token throws AuthenticationException")
    void joinMissingToken() {
        JoinGameService.Request request = new JoinGameService.Request(null,
                "WHITE", gameID);
        assertThrows(AuthenticationException.class,
                () -> gameService.joinGame(request),
                "Expected AuthenticationException when the token is null");
    }


}
