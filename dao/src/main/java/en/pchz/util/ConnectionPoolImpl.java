package en.pchz.util;

import en.pchz.exception.ConnectionPoolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(ConnectionPoolImpl.class);


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
                log.error("Failed to initialize connection pool", e);
                throw new ConnectionPoolException("Failed to initialize connection pool", e);
            }
        }

        this.url = url;
        this.username = username;
        this.password = password;
        this.maxPoolSize = maxPoolSize;
        this.maxTimeout = maxTimeout;
        this.connectionPool = pool;
        this.usedConnections = new ArrayList<>();
        log.info(String.format("""
                        Connection pool success initialized with params:
                        Url - %s
                        Username - %s
                        Max pool size - %d
                        Init pool size - %d
                        Max timeout - %d
                        """,
                url, username, maxPoolSize, initialPoolSize, maxTimeout));
    }


    @Override
    public Connection getConnection() {
        if (connectionPool.isEmpty()) {
            if (usedConnections.size() < maxPoolSize) {
                try {
                    connectionPool.add(DriverManager.getConnection(url, username, password));
                } catch (SQLException e) {
                    log.error("Failed to create new connection", e);
                    throw new ConnectionPoolException("Failed to create new connection", e);
                }
            } else {
                log.error("Maximum pool size reached, no available connections!");
                throw new ConnectionPoolException("Maximum pool size reached, no available connections!");
            }
        }

        Connection connection = connectionPool.removeLast();

        try {
            if (!connection.isValid(maxTimeout)) {
                connection = DriverManager.getConnection(url, username, password);
            }
        } catch (SQLException e) {
            log.error("Failed to validate or recreate connection", e);
            throw new ConnectionPoolException("Failed to validate or recreate connection", e);
        }

        usedConnections.add(connection);

        return connection;
    }

    @Override
    public boolean releaseConnection(Connection connection) {
        if (connection != null) {
            usedConnections.remove(connection);
            return connectionPool.add(connection);
        } else {
            log.warn("Attempted to release null connection");
            return false;
        }
    }

    @Override
    public void shutdown() {
        usedConnections.forEach(this::releaseConnection);
        for (Connection conn : connectionPool) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error("Failed to close connection during shutdown", e);
                throw new ConnectionPoolException("Failed to close connection during shutdown", e);
            }
        }
        connectionPool.clear();
        log.info("Connection pool has been shut down");
    }
}
