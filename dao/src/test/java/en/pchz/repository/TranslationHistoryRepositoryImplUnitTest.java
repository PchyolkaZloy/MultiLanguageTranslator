package en.pchz.repository;

import en.pchz.exception.TranslationHistoryException;
import en.pchz.util.ConnectionPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TranslationHistoryRepositoryImplUnitTest {
    @Mock
    private ConnectionPool connectionPool;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @InjectMocks
    private TranslationHistoryRepositoryImpl translationHistoryRepository;

    private final String sql = "INSERT INTO translation_requests (request_time, ip_address, input_text, translated_text) VALUES (?, ?, ?, ?)";

    private LocalDateTime requestTime;
    private String ipAddress;
    private String inputText;
    private String translatedText;

    @BeforeEach
    void setUp() throws SQLException {
        requestTime = LocalDateTime.now();
        ipAddress = "127.0.0.1";
        inputText = "Hello";
        translatedText = "Привет";

        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
    }

    @Test
    void testSave_ValidData_Success() throws SQLException {
        // Act
        translationHistoryRepository.save(requestTime, ipAddress, inputText, translatedText);

        // Assert
        verify(preparedStatement).setTimestamp(1, Timestamp.valueOf(requestTime));
        verify(preparedStatement).setString(2, ipAddress);
        verify(preparedStatement).setString(3, inputText);
        verify(preparedStatement).setString(4, translatedText);
        verify(preparedStatement).executeUpdate();
        verify(preparedStatement).close();
        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    void testSave_ValidData_ThrowsSQLExceptionDuringSave() throws SQLException {
        // Arrange
        doThrow(new SQLException("Database error")).when(preparedStatement).executeUpdate();

        // Act & Assert
        assertThrows(TranslationHistoryException.class, () ->
                translationHistoryRepository.save(requestTime, ipAddress, inputText, translatedText));

        verify(preparedStatement).close();
        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    void testSave_PreparedStatementIsNull_ThrowsTranslationHistoryException() throws SQLException {
        // Arrange
        when(connection.prepareStatement(sql)).thenReturn(null);

        // Act & Assert
        assertThrows(TranslationHistoryException.class, () ->
                translationHistoryRepository.save(requestTime, ipAddress, inputText, translatedText));

        verify(connectionPool).releaseConnection(connection);
    }
}
