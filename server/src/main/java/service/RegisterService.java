package service;

import dataaccess.*;
import model.UserData;
import java.util.UUID;

public class RegisterService {
    private final UserDAO userDao;

    public RegisterService(UserDAO userDAO) {
        this.userDao = userDAO;
    }

    public RegisterResult register(RegisterRequest request) {
        try {
            if (userDao.getUser(request.username()).isPresent()) {
                throw new AlreadyTakenException("The username '" + request.username() + "' is already taken");
            }

        UserData newUser = new UserData(request.username(),
                hashPassword(request.password()),
                request.email()
        );

        userDao.createUser(newUser);

        String authToken = UUID.randomUUID().toString();

        return new RegisterResult(authToken, request.username());
        } catch (DataAccessException e) {
            throw new ServerException("Database connection error during registration", e);
        }
    }

    private String hashPassword(String plain) {
        return Integer.toHexString(plain.hashCode()); 
    }

}