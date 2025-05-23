package service;

import chess.ChessGame;
import dataaccess.*;
import dataaccess.implementation.*;
import model.*;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


class ClearTest {

    private AuthDAO authDAO;
    private GameDAO gameDAO;
    private UserDAO userDAO;
    private ClearService clearService;

    @BeforeEach
    public void setup() throws DataAccessException {
        authDAO = new InMemoryAuthDAO();
        gameDAO = new InMemoryGameDAO();
        userDAO = new InMemoryUserDAO();
        clearService = new ClearService(authDAO, gameDAO, userDAO);

        authDAO.createAuth( "Jack");
        gameDAO.createGame(new GameData(1234, "Jack",
                      "Liv", "test", new ChessGame()));
        userDAO.createUser(new UserData("Jack", "cs240test", "jneb2004@byu.edu"));
    }

    @Test
    @DisplayName("Successful Clear")
    public void clearSuccessfully() {
        ClearRequest req = new ClearRequest();
        ClearResult res = clearService.clear(req);

        assertTrue(res.isSuccess());
        assertEquals("Database successfully cleared!", res.getMessage());

        assertTrue(authDAO.findAll().isEmpty(), "AuthDAO should be empty!");
        assertTrue(gameDAO.findAll().isEmpty(), "GameDAO should be empty!");
        assertTrue(userDAO.findAll().isEmpty(), "UserDAO should be empty!");
    }

    @Test
    @DisplayName("Still Clears when Empty")
    public void emptyClear() {
        clearSuccessfully();
        clearSuccessfully();
    }

    @Test
    @DisplayName("Throws Exception when Expected")
    public void clearException() {
        AuthDAO failAuth = new AuthDAO() {
            @Override
            public void clear() {
                throw new RuntimeException("Failed");
            }

            @Override
            public AuthData createAuth(String username) {
                return new AuthData(UUID.randomUUID().toString(), username); }

            @Override
            public Optional<AuthData> getAuth(String authToken) {
                return Optional.empty();
            }

            @Override
            public List<AuthData> findAll() {
                return List.of();
            }
        };

        ClearService testService = new ClearService(failAuth,
                                                    new InMemoryGameDAO(),
                                                    new InMemoryUserDAO());
        assertThrows(ClearFailedException.class,
                    () -> testService.clear(new ClearRequest()),
                "Expected a ClearFailedException error when DAO.clear() fails");
    }
}
