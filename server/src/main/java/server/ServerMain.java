package server;

import dataaccess.DataAccessException;
import dataaccess.sql.MySqlDatabase;

public class ServerMain {
    public static void main(String[] args) throws DataAccessException {
        MySqlDatabase.initialize();

        var port = 8080;
        Server server = new Server();
        server.run(port);

        System.out.println("♕ 240 Chess Server running on port " + port);
    }
}
