package com.brillio.sts.junit;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.brillio.sts.model.Tickets;

class TicketsTest {

    @Test
    void testGettersAndSetters() {
        Tickets tickets = new Tickets();
        tickets.setTicketId(1);
        tickets.setUserId(2);
        tickets.setConnectionId(3);
        tickets.setConnectionType("WIFI");
        tickets.setServiceType("FAULT");
        tickets.setDescription("INTERNET OUTAGE");
        tickets.setStatus("IN_PROGRESS");
        tickets.setAddress("Sampige Road, Malleswaram, Bengaluru, Karnataka");
        tickets.setPincode(560050);
        tickets.setPriority("P1");
        tickets.setEngineerId(4);
        LocalDateTime detectedTime = LocalDateTime.of(2025, 2, 14, 9, 56, 50);
        tickets.setAssignmentDate(detectedTime);
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.of(2025, 2, 11, 11, 27, 6);
        tickets.setCreatedAt(createdAt);
        tickets.setUpdatedAt(updatedAt);
        tickets.setLongitude(77.57138139427872);
        tickets.setLatitude(12.998120451014515);

        assertEquals(1, tickets.getTicketId());
        assertEquals(2, tickets.getUserId());
        assertEquals(3, tickets.getConnectionId());
        assertEquals("WIFI", tickets.getConnectionType());
        assertEquals("FAULT", tickets.getServiceType());
        assertEquals("IN_PROGRESS", tickets.getStatus());
        assertEquals("INTERNET OUTAGE", tickets.getDescription());
        assertEquals("Sampige Road, Malleswaram, Bengaluru, Karnataka", tickets.getAddress());
        assertEquals(560050, tickets.getPincode());
        assertEquals("P1", tickets.getPriority());
        assertEquals(4, tickets.getEngineerId());
        assertEquals(77.57138139427872, tickets.getLongitude());
        assertEquals(12.998120451014515, tickets.getLatitude());
        assertEquals(createdAt, tickets.getCreatedAt());
        assertEquals(updatedAt, tickets.getUpdatedAt());
        assertEquals(detectedTime, tickets.getAssignmentDate());
    }

    @Test
    void testToConstructor() {
        LocalDateTime detectedTime = LocalDateTime.of(2025, 2, 14, 9, 56, 50);
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.of(2025, 2, 11, 11, 27, 6);

        Tickets tickets = new Tickets(1, 2, 3, "WIFI", "FAULT", "INTERNET OUTAGE",
                560050, "IN_PROGRESS", "Sampige Road, Malleswaram, Bengaluru, Karnataka", "P1", 4, detectedTime,
                createdAt, updatedAt, 77.57138139427872, 12.998120451014515);

        assertEquals(1, tickets.getTicketId());
        assertEquals(2, tickets.getUserId());
        assertEquals(3, tickets.getConnectionId());
        assertEquals("WIFI", tickets.getConnectionType());
        assertEquals("FAULT", tickets.getServiceType());
        assertEquals("INTERNET OUTAGE", tickets.getDescription());
        assertEquals(560050, tickets.getPincode());
        assertEquals("Sampige Road, Malleswaram, Bengaluru, Karnataka", tickets.getAddress());
        assertEquals("IN_PROGRESS", tickets.getStatus());
        assertEquals("P1", tickets.getPriority());
        assertEquals(4, tickets.getEngineerId());
        assertEquals(detectedTime, tickets.getAssignmentDate());
        assertEquals(createdAt, tickets.getCreatedAt());
        assertEquals(updatedAt, tickets.getUpdatedAt());
        assertEquals(77.57138139427872, tickets.getLongitude());
        assertEquals(12.998120451014515, tickets.getLatitude());
    }
}
