package com.brillio.sts.mock;
 
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
 
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
 
import com.brillio.sts.model.Connections;
import com.brillio.sts.repo.ConnectionsRepository;
import com.brillio.sts.service.ConnectionsService;
import com.brillio.sts.exception.ConnectionNotFoundException;
 
@ExtendWith(MockitoExtension.class)
class ConnectionsServiceTest {
 
    @Mock
    private ConnectionsRepository connectionsRepository;
 
    @InjectMocks
    private ConnectionsService connectionsService;
 
    private Connections mockConnection1;
    private Connections mockConnection2;
 
    @BeforeEach
    void setUp() {
        mockConnection1 = new Connections(1, 101, "DTH", LocalDateTime.now(), 12, null, null, "ACTIVE");
        mockConnection2 = new Connections(2, 102, "WIFI", LocalDateTime.now(), 6, null, null, "INACTIVE");
    }
 
    @Test
    void testShowConnections_ShouldReturnConnectionList() {
        when(connectionsRepository.findAll()).thenReturn(Arrays.asList(mockConnection1, mockConnection2));
 
        List<Connections> connectionsList = connectionsService.showConnections();
 
        assertNotNull(connectionsList);
        assertEquals(2, connectionsList.size());
        verify(connectionsRepository, times(1)).findAll();
    }
 
    @Test
    void testShowConnections_ShouldReturnEmptyList() {
        when(connectionsRepository.findAll()).thenReturn(Arrays.asList());
 
        List<Connections> connectionsList = connectionsService.showConnections();
 
        assertNotNull(connectionsList);
        assertEquals(0, connectionsList.size());
        verify(connectionsRepository, times(1)).findAll();
    }
 
    @Test
    void testSearchById_ShouldReturnConnection() {
        when(connectionsRepository.findById(1)).thenReturn(Optional.of(mockConnection1));
 
        Connections connection = connectionsService.searchById(1);
 
        assertNotNull(connection);
        assertEquals(101, connection.getUserId());
        verify(connectionsRepository, times(1)).findById(1);
    }
 
    @Test
    void testSearchById_ConnectionNotFound_ShouldThrowException() {
        when(connectionsRepository.findById(999)).thenReturn(Optional.empty());
 
        assertThrows(ConnectionNotFoundException.class, () -> connectionsService.searchById(999));
        verify(connectionsRepository, times(1)).findById(999);
    }
    
    @Test
    void testSearchByUserId_ShouldReturnConnections() {
        when(connectionsRepository.findByuserId(101)).thenReturn(Arrays.asList(mockConnection1, mockConnection2));

        List<Connections> connections = connectionsService.searchByUserId(101);

        assertNotNull(connections);
        assertEquals(2, connections.size());
        verify(connectionsRepository, times(1)).findByuserId(101);
    }

    @Test
    void testSearchByUserId_NoConnectionsFound_ShouldReturnEmptyList() {
        when(connectionsRepository.findByuserId(999)).thenReturn(Arrays.asList());

        List<Connections> connections = connectionsService.searchByUserId(999);

        assertNotNull(connections);
        assertTrue(connections.isEmpty());
        verify(connectionsRepository, times(1)).findByuserId(999);
    }

    @Test
    void testSearchByUserAndStatus_ShouldReturnActiveConnections() {
        when(connectionsRepository.findByuserIdAndStatus(101, "ACTIVE")).thenReturn(Arrays.asList(mockConnection1));

        List<Connections> activeConnections = connectionsService.searchByUserAndStatus(101);

        assertNotNull(activeConnections);
        assertEquals(1, activeConnections.size());
        assertEquals("ACTIVE", activeConnections.get(0).getStatus());
        verify(connectionsRepository, times(1)).findByuserIdAndStatus(101, "ACTIVE");
    }

    @Test
    void testSearchByUserAndStatus_NoActiveConnections_ShouldReturnEmptyList() {
        when(connectionsRepository.findByuserIdAndStatus(102, "ACTIVE")).thenReturn(Arrays.asList());

        List<Connections> activeConnections = connectionsService.searchByUserAndStatus(102);

        assertNotNull(activeConnections);
        assertTrue(activeConnections.isEmpty());
        verify(connectionsRepository, times(1)).findByuserIdAndStatus(102, "ACTIVE");
    }
 
    @Test
    void testAddConnections_ShouldReturnSavedConnection() {
        Connections newConnection = new Connections(3, 103, "LANDLINE", LocalDateTime.now(), 12, null, null, null);
        when(connectionsRepository.save(any(Connections.class))).thenReturn(newConnection);
 
        Connections savedConnection = connectionsService.addConnections(newConnection);
 
        assertNotNull(savedConnection);
        assertEquals("INACTIVE", savedConnection.getStatus()); // ✅ Ensures default status
        verify(connectionsRepository, times(1)).save(newConnection);
    }
 
    @Test
    void testAddConnections_WithMissingFields_ShouldThrowException() {
        Connections invalidConnection = new Connections();
        assertThrows(NullPointerException.class, () -> connectionsService.addConnections(invalidConnection));
    }
 
    @Test
    void testSetExpiryDate_ValidStartDateAndValidityPeriod_ShouldReturnCorrectExpiryDate() {
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        int validityPeriod = 12; // 12 months
        mockConnection1.setStartDate(startDate);
        mockConnection1.setValidityPeriod(validityPeriod);
 
        LocalDateTime expiryDate = connectionsService.setExpiryDate(mockConnection1);
 
        assertNotNull(expiryDate, "Expiry date should not be null.");
        LocalDateTime expectedExpiryDate = startDate.plusMonths(validityPeriod);
        
        assertEquals(expectedExpiryDate, expiryDate, "Expiry date does not match expected date.");
    }
 
    @Test
    void testSetExpiryDate_NullStartDate_ShouldReturnNull() {
        mockConnection1.setStartDate(null);
        mockConnection1.setValidityPeriod(12);
 
        LocalDateTime expiryDate = connectionsService.setExpiryDate(mockConnection1);
 
        assertNull(expiryDate, "Expiry date should be null when start date is missing.");
    }
}
