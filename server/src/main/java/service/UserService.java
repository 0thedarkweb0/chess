package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import service.records.LoginRequest;
import service.records.LoginResult;
import service.records.RegisterRequest;
import service.records.RegisterResult;

import java.util.UUID;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public RegisterResult register(RegisterRequest request) throws AlreadyTakenException, BadRequestException, DataAccessException {
        if (request == null
                || request.username() == null || request.username().isBlank()
                || request.password() == null || request.password().isBlank()
                || request.email() == null || request.email().isBlank()) {
            throw new BadRequestException("missing required field");
        }

        if (userDAO.getUser(request.username()) != null) {
            throw new AlreadyTakenException("already taken");
        }

        userDAO.createUser(
                new UserData(request.username(),request.password(),request.email())
        );


        String authToken = UUID.randomUUID().toString();
        authDAO.createAuth(new AuthData(authToken, request.username()));

        return(new RegisterResult(request.username(),authToken));
    }

    public LoginResult login(LoginRequest request) throws BadRequestException, UnauthorizedException, DataAccessException {
        if (request == null
                || request.username() == null || request.password() == null
                || request.username().isBlank() || request.password().isBlank()) {
            throw new BadRequestException("You need a username");
        }

        UserData user = userDAO.getUser(request.username());
        if (user == null || !user.password().equals(request.password())) {
            throw new UnauthorizedException("unauthorized");
        }

        String authToken = UUID.randomUUID().toString();
        authDAO.createAuth(new AuthData(authToken, request.username()));

        return new LoginResult(request.username(), authToken);

    }

    public void logout(String authToken) throws UnauthorizedException, DataAccessException {
        if(authToken == null || authDAO.getAuth(authToken) == null){
            throw new UnauthorizedException("unauthorized");
        }

        authDAO.deleteAuth(authToken);
    }
}
