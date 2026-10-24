package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;

import java.util.UUID;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public RegisterResult register(RegisterRequest request) throws AlreadyTakenException, BadRequestException {
        if(userDAO.getUser(request.username())!=null){
            throw new AlreadyTakenException("already taken");
        }

        if (request.username() == null || request.username().isBlank()
                || request.password() == null || request.password().isBlank()
                || request.email() == null || request.email().isBlank()) {
            throw new BadRequestException("missing required field");
        }

        userDAO.createUser(
                new UserData(request.username(),request.password(),request.email())
        );


        String authToken = UUID.randomUUID().toString();
        authDAO.createAuth(new AuthData(authToken, request.username()));

        return(new RegisterResult(request.username(),authToken));
    }

}
