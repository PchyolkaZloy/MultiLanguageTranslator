package en.pchz.repository;

import en.pchz.exception.ConnectionPoolException;
import en.pchz.exception.TranslationHistoryException;
import en.pchz.util.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Repository
public class TranslationHistoryRepositoryImpl implements TranslationHistoryRepository {
    private final ConnectionPool connectionPool;
    private static final Logger log = LoggerFactory.getLogger(TranslationHistoryRepositoryImpl.class);

    @Autowired
    public TranslationHistoryRepositoryImpl(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    @Override
    public void save(LocalDateTime requestTime, String ipAddress, String inputText, String translatedText) {
        String sql = "INSERT INTO translation_requests (request_time, ip_address, input_text, translated_text) VALUES (?, ?, ?, ?)";
        Connection connection = null;

        try {
            connection = connectionPool.getConnection();
            executeUpdate(connection, sql, requestTime, ipAddress, inputText, translatedText);
            connection.commit();
            log.info("Successfully saved translation history for user with IP {}", ipAddress);
        } catch (ConnectionPoolException e) {
            handleException("Failed to get connection from pool", e);
        } catch (SQLException e) {
            handleSQLException(connection, e);
        } finally {
            releaseConnection(connection);
        }
    }

    private void executeUpdate(Connection connection, String sql, LocalDateTime requestTime, String ipAddress, String inputText, String translatedText) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setTimestamp(1, Timestamp.valueOf(requestTime));
            preparedStatement.setString(2, ipAddress);
            preparedStatement.setString(3, inputText);
            preparedStatement.setString(4, translatedText);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to execute update for translation history", e);
            throw e;
        }
    }

    private void handleException(String message, Exception e) {
        log.error(message, e);
        throw new TranslationHistoryException(message, e);
    }

    private void handleSQLException(Connection connection, SQLException e) {
        try {
            if (connection != null) {
                connection.rollback();
            }
        } catch (SQLException rollbackException) {
            log.error("Failed to rollback transaction", rollbackException);
        }
        handleException("Failed to save translation history", e);
    }

    private void releaseConnection(Connection connection) {
        if (connection != null) {
            try {
                connectionPool.releaseConnection(connection);
            } catch (ConnectionPoolException e) {
                log.warn("Failed to release connection back to pool", e);
            }
        }
    }
}