package server;

import dataaccess.DataAccessException;

public class ServerMain {
    public static void main(String[] args) throws DataAccessException {

        var port = 8080;
        Server server = new Server();
        server.run(port);

        System.out.println("♕ 240 Chess Server running on port " + port);
    }
}
