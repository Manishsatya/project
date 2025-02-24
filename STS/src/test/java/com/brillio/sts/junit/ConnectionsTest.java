package com.brillio.sts.junit;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.brillio.sts.model.Connections;

class ConnectionsTest {

    @Test
    void testGettersAndSetters() {
        Connections connection = new Connections();
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime expiryDate = LocalDateTime.now().plusMonths(12);

        connection.setConnectionId(1);
        connection.setUserId(101);
        connection.setConnectionType("DTH");
        connection.setStartDate(startDate);
        connection.setValidityPeriod(12);
        connection.setExpiryDate(expiryDate);
        connection.setEndDate(null);
        connection.setStatus("ACTIVE");

        assertEquals(1, connection.getConnectionId());
        assertEquals(101, connection.getUserId());
        assertEquals("DTH", connection.getConnectionType());
        assertEquals(startDate, connection.getStartDate());
        assertEquals(12, connection.getValidityPeriod());
        assertEquals(expiryDate, connection.getExpiryDate());
        assertNull(connection.getEndDate());
        assertEquals("ACTIVE", connection.getStatus());
    }

    @Test
    void testNoArgsConstructor() {
        Connections newConnection = new Connections();
        assertNotNull(newConnection);
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime expiryDate = LocalDateTime.now().plusMonths(6);

        Connections newConnection = new Connections(1, 101, "WIFI", startDate, 6, expiryDate, null, "INACTIVE");

        assertEquals(1, newConnection.getConnectionId());
        assertEquals(101, newConnection.getUserId());
        assertEquals("WIFI", newConnection.getConnectionType());
        assertEquals(startDate, newConnection.getStartDate());
        assertEquals(6, newConnection.getValidityPeriod());
        assertEquals(expiryDate, newConnection.getExpiryDate());
        assertNull(newConnection.getEndDate());
        assertEquals("INACTIVE", newConnection.getStatus());
    }

    @Test
    void testToString() {
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime expiryDate = LocalDateTime.now().plusMonths(6);

        Connections newConnection = new Connections(1, 101, "WIFI", startDate, 6, expiryDate, null, "INACTIVE");

        String expectedString = "Connections(connectionId=1, userId=101, connectionType=WIFI, startDate="
                + startDate + ", validityPeriod=6, expiryDate=" + expiryDate + ", endDate=null, status=INACTIVE)";

        assertEquals(expectedString, newConnection.toString());
    }

    @Test
    void testNegativeGettersAndSetters() {
        Connections connection = new Connections();
        connection.setConnectionId(-1); // ❌ Negative ID
        connection.setUserId(-101); // ❌ Negative User ID
        connection.setConnectionType(""); // ❌ Empty Connection Type
        connection.setStartDate(null); // ❌ Null Start Date
        connection.setValidityPeriod(-6); // ❌ Negative Validity Period
        connection.setExpiryDate(null); // ❌ Null Expiry Date
        connection.setEndDate(LocalDateTime.now()); // ❌ Should be null
        connection.setStatus("Unknown"); // ❌ Invalid Status

        assertNotEquals(1, connection.getConnectionId());
        assertNotEquals(101, connection.getUserId());
        assertNotEquals("DTH", connection.getConnectionType());
        assertNull(connection.getStartDate());
        assertNotEquals(12, connection.getValidityPeriod());
        assertNull(connection.getExpiryDate());
        assertNotNull(connection.getEndDate()); // ❌ Should be null
        assertNotEquals("ACTIVE", connection.getStatus());
    }

    @Test
    void testNegativeToString() {
        LocalDateTime startDate = null; // ❌ Null Timestamp
        Connections connection = new Connections(-1, -101, "", startDate, -6, null, LocalDateTime.now(), "Unknown");

        String unexpectedResult = "Connections [connectionId=-1, userId=-101, connectionType=, startDate=null, "
                + "validityPeriod=-6, expiryDate=null, endDate=someDate, status=Unknown]";

        assertNotEquals(unexpectedResult, connection.toString());
    }

    @Test
    void testNegativeConstructor() {
        LocalDateTime startDate = null; // ❌ Null Start Date
        Connections connection = new Connections(-1, -101, "", startDate, -6, null, LocalDateTime.now(), "Unknown");

        assertNotEquals(1, connection.getConnectionId());
        assertNotEquals(101, connection.getUserId());
        assertNotEquals("DTH", connection.getConnectionType());
        assertNull(connection.getStartDate());
        assertNotEquals(12, connection.getValidityPeriod());
        assertNull(connection.getExpiryDate());
        assertNotNull(connection.getEndDate()); // ❌ Should be null
        assertNotEquals("ACTIVE", connection.getStatus());
    }
}
