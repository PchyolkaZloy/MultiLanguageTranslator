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
        PreparedStatement preparedStatement = null;

        try {
            connection = connectionPool.getConnection();
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setTimestamp(1, Timestamp.valueOf(requestTime));
            preparedStatement.setString(2, ipAddress);
            preparedStatement.setString(3, inputText);
            preparedStatement.setString(4, translatedText);

            preparedStatement.executeUpdate();
            log.info("Successfully saved translation history for user with ip {}", ipAddress);
        } catch (NullPointerException e) {
            log.error("Failed to get prepared statement from connection", e);
            throw new TranslationHistoryException("Error saving translation history due to prepared statement issue", e);
        } catch (ConnectionPoolException e) {
            log.error("Failed to get connection from pool", e);
            throw new TranslationHistoryException("Error saving translation history due to connection pool issue", e);
        } catch (SQLException e) {
            log.error("Failed to save translation history", e);
            throw new TranslationHistoryException("Error saving translation history", e);
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (connection != null) {
                    connectionPool.releaseConnection(connection);
                }
            } catch (SQLException e) {
                log.warn("Failed to close resources after saving translation history", e);
            }
        }
    }
}
