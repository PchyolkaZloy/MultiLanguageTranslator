package en.pchz.util;

import en.pchz.exception.ConnectionPoolException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ConnectionPoolImplUnitTest {
    private final String url = "jdbc:testdb";
    private final String username = "user";
    private final String password = "";
    private final Integer initialPoolSize = 2;
    private final Integer maxPoolSize = 4;
    private final Integer maxTimeout = 5;

    private static MockedStatic<DriverManager> driverManager;

    @BeforeAll
    public static void init() {
        driverManager = mockStatic(DriverManager.class);
    }

    @AfterAll
    public static void close() {
        driverManager.close();
    }

    @Test
    void testGetConnection_ValidData_Success() throws SQLException {
        // Arrange
        Connection mockConnection = mock(Connection.class);
        when(mockConnection.isValid(maxTimeout)).thenReturn(true);
        when(DriverManager.getConnection(url, username, password)).thenReturn(mockConnection);

        ConnectionPoolImpl connectionPoolImpl = new ConnectionPoolImpl(url, username, password, initialPoolSize, maxPoolSize, maxTimeout);

        // Act
        Connection connection = connectionPoolImpl.getConnection();

        // Assert
        assertNotNull(connection);
        verify(mockConnection, never()).close();
    }

    @Test
    void testReleaseConnection_ValidData_Success() throws SQLException {
        // Arrange
        Connection mockConnection = mock(Connection.class);
        when(mockConnection.isValid(maxTimeout)).thenReturn(true);
        when(DriverManager.getConnection(url, username, password)).thenReturn(mockConnection);

        ConnectionPoolImpl connectionPoolImpl = new ConnectionPoolImpl(url, username, password, initialPoolSize, maxPoolSize, maxTimeout);

        // Act
        Connection connection = connectionPoolImpl.getConnection();
        boolean released = connectionPoolImpl.releaseConnection(connection);

        // Assert
        assertTrue(released);
        verify(mockConnection, never()).close();
    }

    @Test
    void testGetConnectionPoolSizeExceeded_ValidData_Throws() throws SQLException {
        // Arrange
        Connection mockConnection = mock(Connection.class);
        when(mockConnection.isValid(maxTimeout)).thenReturn(true);
        when(DriverManager.getConnection(url, username, password)).thenReturn(mockConnection);

        ConnectionPoolImpl connectionPoolImpl = new ConnectionPoolImpl(url, username, password, initialPoolSize, maxPoolSize, maxTimeout);

        // Act
        List<Connection> connections = new ArrayList<>();
        for (int i = 0; i < maxPoolSize; i++) {
            connections.add(connectionPoolImpl.getConnection());
        }

        // Assert
        assertThrows(ConnectionPoolException.class, connectionPoolImpl::getConnection);
    }

    @Test
    void testShutdown_ValidData_Success() throws SQLException {
        // Arrange
        Connection mockConnection = mock(Connection.class);
        when(mockConnection.isValid(maxTimeout)).thenReturn(true);
        when(DriverManager.getConnection(url, username, password)).thenReturn(mockConnection);

        ConnectionPoolImpl connectionPoolImpl = new ConnectionPoolImpl(url, username, password, initialPoolSize, maxPoolSize, maxTimeout);

        // Act
        Connection connection = connectionPoolImpl.getConnection();
        connectionPoolImpl.releaseConnection(connection);
        connectionPoolImpl.shutdown();

        // Assert
        verify(mockConnection, times(2)).close();
    }
}
