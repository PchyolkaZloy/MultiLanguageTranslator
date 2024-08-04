package en.pchz.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public final class ConnectionPoolImpl implements ConnectionPool {
    private final String url;
    private final String username;
    private final String password;
    private final Integer maxPoolSize;
    private final Integer maxTimeout;
    private final List<Connection> connectionPool;
    private final List<Connection> usedConnections;


    public ConnectionPoolImpl(
            @Value("${jdbc.url}") String url,
            @Value("${jdbc.username}") String username,
            @Value("${jdbc.password}") String password,
            @Value("${jdbc.pool.size.init}") Integer initialPoolSize,
            @Value("${jdbc.pool.size.max}") Integer maxPoolSize,
            @Value("${jdbc.timeout.max}") Integer maxTimeout) {
        List<Connection> pool = new ArrayList<>(initialPoolSize);
        for (int i = 0; i < initialPoolSize; i++) {
            try {
                pool.add(DriverManager.getConnection(url, username, password));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        this.url = url;
        this.username = username;
        this.password = password;
        this.maxPoolSize = maxPoolSize;
        this.maxTimeout = maxTimeout;
        this.connectionPool = pool;
        this.usedConnections = new ArrayList<>();
    }


    @Override
    public Connection getConnection() {
        if (connectionPool.isEmpty()) {
            if (usedConnections.size() < maxPoolSize) {
                try {
                    connectionPool.add(DriverManager.getConnection(url, username, password));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                throw new RuntimeException("Maximum pool size reached, no available connections!");
            }
        }

        Connection connection = connectionPool.removeLast();

        try {
            if (!connection.isValid(maxTimeout)) {
                connection = DriverManager.getConnection(url, username, password);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        usedConnections.add(connection);

        return connection;
    }

    @Override
    public boolean releaseConnection(Connection connection) {
        connectionPool.add(connection);
        return usedConnections.remove(connection);
    }

    @Override
    public void shutdown() {
        usedConnections.forEach(this::releaseConnection);
        for (Connection conn : connectionPool) {
            try {
                conn.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        connectionPool.clear();
    }
}
