package service;

import chess.ChessGame;
import dataaccess.*;
import dataaccess.memoryimplementation.*;
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
    private AuthService authService;

    @BeforeEach
    public void setup() throws DataAccessException {
        authDAO = new InMemoryAuthDAO();
        gameDAO = new InMemoryGameDAO();
        userDAO = new InMemoryUserDAO();
        authService = new AuthService(authDAO, gameDAO, userDAO);

        authDAO.createAuth( "Jack");
        gameDAO.addGame(new GameData(1234, "Jack",
                      "Liv", "test", new ChessGame()));
        userDAO.createUser(new UserData("Jack", "cs240test", "jneb2004@byu.edu"));
    }

    @Test
    @DisplayName("Successful Clear")
    public void clearSuccessfully() throws DataAccessException {
        AuthService.ClearResult res = authService.clearDatabase();

        assertTrue(res.success());
        assertEquals("Database successfully cleared!", res.message());

        assertTrue(authDAO.findAll().isEmpty(), "AuthDAO should be empty!");
        assertTrue(gameDAO.listGames().isEmpty(), "GameDAO should be empty!");
        assertTrue(userDAO.findAll().isEmpty(), "UserDAO should be empty!");
    }

    @Test
    @DisplayName("Still Clears when Empty")
    public void emptyClear() throws DataAccessException {
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

            @Override
            public boolean removeToken(String token) throws DataAccessException {
                return false;
            }
        };

        AuthService failingService = new AuthService(failAuth, new InMemoryGameDAO(), new InMemoryUserDAO());
        assertThrows(ClearFailedException.class,
                failingService::clearDatabase,
                "Expected a ClearFailedException error when authDAO.clear() fails");
    }
}
