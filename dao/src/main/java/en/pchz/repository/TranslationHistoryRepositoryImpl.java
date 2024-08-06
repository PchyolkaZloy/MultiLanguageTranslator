package en.pchz.repository;

import en.pchz.util.ConnectionPool;
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
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error saving translation history", e);
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (connection != null) {
                    connectionPool.releaseConnection(connection);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
