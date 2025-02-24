package com.brillio.sts.junit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
 
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
 
import org.junit.jupiter.api.Test;

import com.brillio.sts.model.Tickets;

 class TicketsTest {

	@Test
	 void testGettersAndSetters(){
		
		Tickets tickets = new Tickets();
		/*
		 * TICKET_ID, USER_ID, CONNECTION_ID, CONNECTION_TYPE,
		 * SERVICE_TYPE, DESCRIPTION, ADDRESS, PINCODE, STATUS,
		 * PRIORITY, ENGINEER_ID, ASSIGNMENT_DATE, CREATED_AT, UPDATED_AT, longitude, latitude
		 */
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
		 LocalDateTime detect = LocalDateTime.of(2025, 2, 14, 9, 56, 50);
	        Date detectedTime = Date.from(detect.atZone(ZoneId.systemDefault()).toInstant());
	        tickets.setAssignmentDate(detectedTime);
	        
	        LocalDateTime createdAt = LocalDateTime.now();
	        
	        LocalDateTime update = LocalDateTime.of(2025, 2, 11, 11, 27, 6);
	        Date updatedAt = Date.from(update.atZone(ZoneId.systemDefault()).toInstant());
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
		assertEquals(createdAt,tickets.getCreatedAt());
		assertEquals(updatedAt,tickets.getUpdatedAt());
		assertEquals(detectedTime,tickets.getAssignmentDate());
	}
	
 
	    @Test
	     void testToConstructor() {
	        // Creating a Tickets object using constructor with LocalDateTime
	    	
	    	 
	        
		     LocalDateTime update = LocalDateTime.of(2025, 2, 11, 11, 27, 6);
		     Date updatedAt = Date.from(update.atZone(ZoneId.systemDefault()).toInstant());
		     LocalDateTime createdAt = LocalDateTime.now();
		     LocalDateTime detect = LocalDateTime.of(2025, 2, 14, 9, 56, 50);
		     Date detectedTime = Date.from(detect.atZone(ZoneId.systemDefault()).toInstant());
		        			
	        
	        /*
			 * TICKET_ID, USER_ID, CONNECTION_ID, CONNECTION_TYPE,
			 * SERVICE_TYPE, DESCRIPTION, ADDRESS, PINCODE, STATUS,
			 * PRIORITY, ENGINEER_ID, ASSIGNMENT_DATE, CREATED_AT, UPDATED_AT, longitude, latitude
			 */
	        
	        Tickets tickets = new Tickets(1, 2, 3, "WIFI", "FAULT", "INTERNET OUTAGE",
	                                      560050, "IN_PROGRESS",
	                                      "Sampige Road, Malleswaram, Bengaluru, Karnataka", "P1", 4, detectedTime,
	                                      createdAt, updatedAt,
	                                      77.57138139427872, 12.998120451014515);
 
	        // Assertions to verify values
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
			assertEquals(detectedTime,tickets.getAssignmentDate());
			assertEquals(createdAt,tickets.getCreatedAt());
			assertEquals(updatedAt,tickets.getUpdatedAt());
			assertEquals(77.57138139427872, tickets.getLongitude());
			assertEquals(12.998120451014515, tickets.getLatitude());
			
		}
			
 
	    @Test
	     void testToString() {
	    	 LocalDateTime update = LocalDateTime.of(2025, 2, 11, 11, 27, 6);
		     Date updatedAt = Date.from(update.atZone(ZoneId.systemDefault()).toInstant());
		     LocalDateTime createdAt = LocalDateTime.now();
		     LocalDateTime detect = LocalDateTime.of(2025, 2, 14, 9, 56, 50);
		     Date detectedTime = Date.from(detect.atZone(ZoneId.systemDefault()).toInstant());
	        
	        Tickets tickets = new Tickets(2, 6, 3, "DTH", "INSTALLATION", "DTH Installation",
                    560050, "IN_PROGRESS",
                    "Sampige Road, Malleswaram, Bengaluru, Karnataka", "P5", 7, detectedTime,
                    createdAt, updatedAt,
                    77.57138139427872, 12.998120451014515);
 
	        // Expected toString format (ensure it matches your actual implementation)
	        String expectedToString = "Tickets [ticketId=" + 2 + ", userId=" + 6 + ", connectionId=" + 3
					+ ", connectionType=" + "DTH" + ", serviceType=" + "INSTALLATION" + ", description=" + "DTH Installation"
					+ ", pincode=" + 560050 + ", status=" + "IN_PROGRESS" + ", address=" + "Sampige Road, Malleswaram, Bengaluru, Karnataka" + ", priority=" + "P5"
					+ ", engineerId=" + 7 + ", assignmentDate=" + detectedTime + ", createdAt=" + createdAt
					+ ", updatedAt=" + updatedAt + ", longitude=" + 77.57138139427872 + ", latitude=" + 12.998120451014515 + "]";
 
	        assertEquals(expectedToString, tickets.toString());
	    }
	    
	    @Test
	     void testGettersAndSettersNegative() {
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
	        LocalDateTime detect = LocalDateTime.of(2025, 2, 14, 9, 56, 50);
	        Date detectedTime = Date.from(detect.atZone(ZoneId.systemDefault()).toInstant());
	        tickets.setAssignmentDate(detectedTime);
	        
	        LocalDateTime createdAt = LocalDateTime.now();
	        
	        LocalDateTime update = LocalDateTime.of(2025, 2, 11, 11, 27, 6);
	        Date updatedAt = Date.from(update.atZone(ZoneId.systemDefault()).toInstant());
	        tickets.setCreatedAt(createdAt);
	        tickets.setUpdatedAt(updatedAt);
	        tickets.setLongitude(77.57138139427872);
	        tickets.setLatitude(12.998120451014515);
 
	        // ❌ Intentionally wrong assertions
	        assertNotEquals(5, tickets.getTicketId()); // Wrong ticketId
	        assertNotEquals(10, tickets.getUserId()); // Wrong userId
	        assertNotEquals(999, tickets.getConnectionId()); // Invalid connectionId
	        assertNotEquals("DSL", tickets.getConnectionType()); // Wrong type
	        assertNotEquals("INSTALLATION", tickets.getServiceType()); // Mismatched type
	        assertNotEquals("NETWORK ISSUE", tickets.getDescription()); // Different description
	        assertNotEquals("COMPLETED", tickets.getStatus()); // Different status
	        assertNotEquals("Some Other Address", tickets.getAddress()); // Address mismatch
	        assertNotEquals(999999, tickets.getPincode()); // Invalid pincode
	        assertNotEquals("P3", tickets.getPriority()); // Incorrect priority
	        assertNotEquals(99, tickets.getEngineerId()); // Wrong engineerId
	        assertNotEquals(100.0, tickets.getLongitude(), 0.00001); // Wrong longitude
	        assertNotEquals(50.0, tickets.getLatitude(), 0.00001); // Wrong latitude
	        assertNotEquals(LocalDateTime.of(2020, 1, 1, 0, 0), tickets.getCreatedAt()); // Wrong createdAt
	        assertNotEquals(LocalDateTime.of(2030, 1, 1, 0, 0), tickets.getUpdatedAt()); // Wrong updatedAt
	        assertNotEquals(LocalDateTime.of(2022, 1, 1, 0, 0), tickets.getAssignmentDate()); // Wrong assignmentDate
	    }
 
	    @Test
	     void testToConstructorNegative() {
	    	 LocalDateTime update = LocalDateTime.of(2025, 2, 11, 11, 27, 6);
		     Date updatedAt = Date.from(update.atZone(ZoneId.systemDefault()).toInstant());
		     LocalDateTime createdAt = LocalDateTime.now();
		     LocalDateTime detect = LocalDateTime.of(2025, 2, 14, 9, 56, 50);
		     Date detectedTime = Date.from(detect.atZone(ZoneId.systemDefault()).toInstant());
 
	        Tickets tickets = new Tickets(1, 2, 3, "WIFI", "FAULT", "INTERNET OUTAGE",
	                560050, "IN_PROGRESS", "Sampige Road, Malleswaram, Bengaluru, Karnataka",
	                "P1", 4, detectedTime, createdAt, updatedAt, 77.57138139427872, 12.998120451014515);
 
	        // ❌ Intentionally failing assertions
	        assertNotEquals(99, tickets.getTicketId());
	        assertNotEquals(88, tickets.getUserId());
	        assertNotEquals(77, tickets.getConnectionId());
	        assertNotEquals("Fiber", tickets.getConnectionType());
	        assertNotEquals("Maintenance", tickets.getServiceType());
	        assertNotEquals("Some other issue", tickets.getDescription());
	        assertNotEquals(999999, tickets.getPincode());
	        assertNotEquals("Some other location", tickets.getAddress());
	        assertNotEquals("COMPLETED", tickets.getStatus());
	        assertNotEquals("P5", tickets.getPriority());
	        assertNotEquals(100, tickets.getEngineerId());
	        assertNotEquals(LocalDateTime.of(2022, 5, 1, 10, 0), tickets.getAssignmentDate());
	        assertNotEquals(LocalDateTime.of(2023, 5, 1, 10, 0), tickets.getCreatedAt());
	        assertNotEquals(LocalDateTime.of(2026, 5, 1, 10, 0), tickets.getUpdatedAt());
	        assertNotEquals(90.0, tickets.getLongitude(), 0.00001);
	        assertNotEquals(45.0, tickets.getLatitude(), 0.00001);
	    }
 
	    @Test
	     void testToStringNegative() {
	    	 LocalDateTime update = LocalDateTime.of(2025, 2, 11, 11, 27, 6);
		     Date updatedAt = Date.from(update.atZone(ZoneId.systemDefault()).toInstant());
		     LocalDateTime createdAt = LocalDateTime.now();
		     LocalDateTime detect = LocalDateTime.of(2025, 2, 14, 9, 56, 50);
		     Date detectedTime = Date.from(detect.atZone(ZoneId.systemDefault()).toInstant());
 
	        Tickets tickets = new Tickets(2, 6, 3, "DTH", "INSTALLATION", "DTH Installation",
	                560050, "IN_PROGRESS", "Sampige Road, Malleswaram, Bengaluru, Karnataka",
	                "P5", 7, detectedTime, createdAt, updatedAt, 77.57138139427872, 12.998120451014515);
 
	        // ❌ Intentionally incorrect expected string
	        String incorrectToString = "Tickets [ticketId=99, userId=99, connectionId=99, connectionType='Fiber', "
	                + "serviceType='Maintenance', description='Wrong Description', "
	                + "pincode=999999, status='COMPLETED', address='Some other address', "
	                + "priority='P10', engineerId=999, assignmentDate=" + detectedTime
	                + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt
	                + ", longitude=90.0, latitude=45.0]";
 
	        assertNotEquals(incorrectToString, tickets.toString());
	    }
 
}
