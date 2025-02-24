package com.brillio.sts.mock;
 
import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
 
import com.brillio.sts.exception.AccountNotFoundException;
import com.brillio.sts.exception.EngineerNotFoundException;
import com.brillio.sts.exception.InvalidReassignmentException;
import com.brillio.sts.exception.TicketAlreadyExistsException;
import com.brillio.sts.exception.UnauthorizedEngineerException;
import com.brillio.sts.model.Accounts;
import com.brillio.sts.model.Connections;
import com.brillio.sts.model.Constants;
import com.brillio.sts.model.Tickets;
import com.brillio.sts.repo.AccountsRepository;
import com.brillio.sts.repo.ConnectionsRepository;
import com.brillio.sts.repo.TicketsRepository;
import com.brillio.sts.service.ConnectionsService;
import com.brillio.sts.service.EmailService;
import com.brillio.sts.service.LeaveHistoryService;
import com.brillio.sts.service.TicketsService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
 
@ExtendWith(MockitoExtension.class)
class TicketsServiceTest {
 
    @Mock
    private ConnectionsService connectionsService;
 
    @Mock
    private TicketsRepository ticketsRepository;
 
    @Mock
    private ConnectionsRepository connectionsRepository;
 
    @Mock
    private AccountsRepository accountsRepository;
 
    @Mock
    private LeaveHistoryService leaveHistoryService;
  
    @Mock
    private EmailService emailService; 
 
    @InjectMocks
    private TicketsService ticketsService;
 
    private Tickets ticket;
    private Tickets ticket1;
    private Accounts userAccount;
    private Connections connection;
 
    @BeforeEach
    void setUp() {
        // Create a sample ticket
        ticket = new Tickets();
        ticket.setTicketId(1);
        ticket.setUserId(100);
        ticket.setEngineerId(200);
        ticket.setConnectionId(300);
        ticket.setStatus("PENDING");
        ticket.setPincode(500001);
        ticket.setConnectionType("WIFI");
        ticket.setServiceType("INSTALLATION");
        LocalDateTime nowLDT = Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime();
        ticket.setCreatedAt(nowLDT);
        ticket.setUpdatedAt(LocalDateTime.now());
        
        ticket1 = new Tickets();
        ticket1.setTicketId(1);
        ticket1.setUserId(100);
        ticket1.setEngineerId(200);
        ticket1.setConnectionId(300);
        ticket1.setStatus("PENDING");
        ticket1.setPincode(500001);
        ticket1.setConnectionType("WIFI");
        ticket1.setServiceType("INSTALLATION");
        LocalDateTime nowLDT1= Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime();
        ticket1.setCreatedAt(nowLDT1);
 
        ticket.setUpdatedAt(LocalDateTime.now());
        

 
        // Create a sample user account (for userId)
        userAccount = new Accounts();
        userAccount.setId(100);
        userAccount.setEmail("user@example.com");
        userAccount.setRole("USER");
        userAccount.setPincode(500001);
        userAccount.setAddress("123 Main St");
        userAccount.setLatitude(17.3850);
        userAccount.setLongitude(78.4867);
 
        // Create a sample connection
        connection = new Connections();
        connection.setConnectionId(300);
        connection.setUserId(100);
        connection.setConnectionType("WIFI");
        connection.setStatus("INACTIVE");
    }

 
    // 1. Test updateTicketStatus (Success branch)
    @Test
    void testUpdateTicketStatus_Success() {
        // Setup: Ticket exists and the engineer is authorized
        ticket.setEngineerId(200);
        ticket.setStatus("COMPLETED");
        when(ticketsRepository.findById(1)).thenReturn(Optional.of(ticket));
        when(accountsRepository.findById(100)).thenReturn(Optional.of(userAccount));
        when(connectionsRepository.findById(300)).thenReturn(Optional.of(connection));
 
        // Stub ticketHistoryService to do nothing (void method)
        // Stub connection update (setExpiryDate is called inside updateTicketStatus)
        when(connectionsService.setExpiryDate(any(Connections.class))).thenReturn(LocalDateTime.now());
        // Stub email service call
        doNothing().when(emailService).sendTicketResolvedEmail(userAccount.getEmail(), ticket.getTicketId());
 
        ticketsService.updateTicketStatus(1, 200, "COMPLETED");
 
        // Verify that ticket, connection, and email update methods are called
        verify(ticketsRepository, times(1)).save(ticket);
        verify(connectionsRepository, times(1)).save(any(Connections.class));
        verify(emailService, times(1)).sendTicketResolvedEmail(userAccount.getEmail(), ticket.getTicketId());
    }
 
    // 2. Test raiseTicketInstallation (Success branch)
    @Test
    void testRaiseTicketInstallation_Success() {
        // Assume connectionsService.addConnections returns the same connection
        when(connectionsService.addConnections(any(Connections.class))).thenReturn(connection);
        when(accountsRepository.findById(100)).thenReturn(Optional.of(userAccount));
        // We'll use a spy to override getBestEngineer to return a valid engineer id, e.g. 200.
        TicketsService spyService = spy(ticketsService);
        doReturn(200).when(spyService).getBestEngineer(anyDouble(), anyDouble(), anyInt(), isNull());
        // Stub email call (for raising ticket email)
        doNothing().when(emailService).sendTicketRaisedEmail(userAccount.getEmail(), ticket.getTicketId(), "INSTALLATION");
        // Stub saving the ticket
        when(ticketsRepository.save(any(Tickets.class))).thenReturn(ticket);
 
        String result = spyService.raiseTicketInstallation(connection, ticket);
        assertEquals("Connection Created & Ticket Raised Successfully", result);
    }
    
    @Test
    void testRaiseTicketInstallation_InvalidPriorityAssignment() {
        when(connectionsService.addConnections(any(Connections.class))).thenReturn(connection);
        when(accountsRepository.findById(100)).thenReturn(Optional.of(userAccount));
 
        TicketsService spyService = spy(ticketsService);
        doReturn(200).when(spyService).getBestEngineer(anyDouble(), anyDouble(), anyInt(), isNull());
 
        // Stub assignPriority to return null, forcing the IllegalArgumentException
        doReturn(null).when(spyService).assignPriority(anyString(), anyString());
 
        assertThrows(IllegalArgumentException.class, () -> spyService.raiseTicketInstallation(connection, ticket));
    }
 
    
 
    
    @Test
    void testRaiseTicketFault_Success() {
        // Ensure ticket does not already exist
        when(ticketsRepository.existsByConnectionIdAndStatusIn(eq(300), anyList())).thenReturn(false);
 
        // Ensure user account is found
        when(accountsRepository.findById(100)).thenReturn(Optional.of(userAccount));
 
        // Spy to override the behavior of assignPriority and getBestEngineer
        TicketsService spyService = spy(ticketsService);
        doReturn("P1").when(spyService).assignPriority(anyString(), anyString()); // Return a valid priority
        doReturn(200).when(spyService).getBestEngineer(anyDouble(), anyDouble(), anyInt(), isNull()); // Return a valid engineer ID
 
        // Stub saving the ticket
        when(ticketsRepository.save(any(Tickets.class))).thenReturn(ticket);
 
        // Stub email sending
        doNothing().when(emailService).sendTicketRaisedEmail(anyString(), anyInt(), anyString());
 
        // Execute the method
        String result = spyService.raiseTicketFault(ticket);
 
        // Verify expected behavior
        assertEquals("Ticket For Fault Raised Successfully", result);
        
        // Ensure the ticket was saved
        verify(ticketsRepository, times(1)).save(any(Tickets.class));
        
        // Ensure the email was sent
        verify(emailService, times(1)).sendTicketRaisedEmail(anyString(), anyInt(), anyString());
    }
 
 
 
    @Test
    void testRaiseTicketFault_TicketAlreadyExists() {
        when(ticketsRepository.existsByConnectionIdAndStatusIn(eq(300), anyList())).thenReturn(true);
 
        assertThrows(TicketAlreadyExistsException.class, () -> ticketsService.raiseTicketFault(ticket));
    }
 
    @Test
    void testRaiseTicketFault_UserAccountNotFound() {
        when(ticketsRepository.existsByConnectionIdAndStatusIn(eq(300), anyList())).thenReturn(false);
        when(accountsRepository.findById(100)).thenReturn(Optional.empty());
 
        assertThrows(AccountNotFoundException.class, () -> ticketsService.raiseTicketFault(ticket));
    }
 
    @Test
    void testRaiseTicketFault_InvalidPriorityAssignment() {
        when(ticketsRepository.existsByConnectionIdAndStatusIn(eq(300), anyList())).thenReturn(false);
        when(accountsRepository.findById(100)).thenReturn(Optional.of(userAccount));
        TicketsService spyService = spy(ticketsService);
        doReturn(null).when(spyService).assignPriority(anyString(), anyString());
 
        assertThrows(IllegalArgumentException.class, () -> spyService.raiseTicketFault(ticket));
    }
    
    @Test
    void testRaiseTicketFault_NoEngineerAvailable() {
        // Ensure ticket does not already exist
        when(ticketsRepository.existsByConnectionIdAndStatusIn(eq(300), anyList())).thenReturn(false);
 
        // Ensure user account is found
        when(accountsRepository.findById(100)).thenReturn(Optional.of(userAccount));
 
        // Spy on the service to return a null engineer
        TicketsService spyService = spy(ticketsService);
        doReturn("P1").when(spyService).assignPriority(anyString(), anyString()); // Assign valid priority
        doReturn(null).when(spyService).getBestEngineer(anyDouble(), anyDouble(), anyInt(), isNull()); // No engineer found
 
        // Execute and assert exception
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () -> spyService.raiseTicketFault(ticket));
        
        // Verify exception message
        assertEquals("No available engineers found in pincode: " + ticket.getPincode(), exception.getMessage());
 
        // Ensure ticket is NOT saved
        verify(ticketsRepository, never()).save(any(Tickets.class));
 
        // Ensure email is NOT sent
        verify(emailService, never()).sendTicketRaisedEmail(anyString(), anyInt(), anyString());
    }
 
    
    @Test
    void testGetPendingTicketsByPincode_WithTickets() {
        int pincode = 500001;
        ticket1.setTicketId(1);
        ticket1.setStatus(Constants.IN_PROGRESS);
        ticket1.setPincode(pincode);
        Tickets ticket2 = new Tickets();
        ticket2.setTicketId(2);
        ticket2.setStatus(Constants.IN_PROGRESS);
        ticket2.setPincode(pincode);
        
        List<Tickets> expectedTickets = Arrays.asList(ticket1, ticket2);
        
        // Stub the repository method call
        when(ticketsRepository.findByStatusAndPincodeOrderByPriorityAsc(Constants.IN_PROGRESS, pincode))
                .thenReturn(expectedTickets);
        
        // Call the service method
        List<Tickets> actualTickets = ticketsService.getPendingTicketsByPincode(pincode);
        
        // Assert that the list returned is as expected
        assertEquals(expectedTickets, actualTickets);
        
        // Verify that the repository method was called exactly once with the correct parameters
        verify(ticketsRepository, times(1))
                .findByStatusAndPincodeOrderByPriorityAsc(Constants.IN_PROGRESS, pincode);
    }
    
    @Test
    void testGetBestEngineer_LeastBusyEngineerSelected() {
        // Mock engineer list
        Accounts engineer1 = new Accounts();
        engineer1.setId(101);
        engineer1.setPincode(500001);
 
        Accounts engineer2 = new Accounts();
        engineer2.setId(102);
        engineer2.setPincode(500001);
 
        List<Accounts> engineers = Arrays.asList(engineer1, engineer2);
 
        // Mock repository to return engineers
        when(accountsRepository.findByRoleAndPincode(Constants.ENGINEER, 500001))
            .thenReturn(engineers);
 
        // Mock leave service (both engineers are available)
        when(leaveHistoryService.isEngineerOnLeave(anyInt(), any(Date.class))).thenReturn(false);
 
        // Mock ticket workload (both engineers have the same workload)
        when(ticketsRepository.countByEngineerIdAndStatus(101, Constants.IN_PROGRESS)).thenReturn((long)3);
        when(ticketsRepository.countByEngineerIdAndStatus(102, Constants.IN_PROGRESS)).thenReturn((long)3);
 
        // Spy service to avoid calling real method
        TicketsService spyService = spy(ticketsService);
 
        // Execute method
        Integer bestEngineerId = spyService.getBestEngineer(17.3850, 78.4867, 500001, null);
 
        // Assert least busy engineer is selected
        assertNotNull(bestEngineerId);
        assertEquals(101, bestEngineerId); // or 102, since both have equal workload
 
        // Verify interactions
        verify(accountsRepository).findByRoleAndPincode(Constants.ENGINEER, 500001);
        verify(ticketsRepository, times(2)).countByEngineerIdAndStatus(anyInt(), eq(Constants.IN_PROGRESS));
    }
 
 
    @Test
    void testGetPendingTicketsByPincode_NoTickets() {
        int pincode = 123456;
        
        // Stub the repository to return an empty list for this pincode
        when(ticketsRepository.findByStatusAndPincodeOrderByPriorityAsc(Constants.IN_PROGRESS, pincode))
                .thenReturn(Collections.emptyList());
        
        // Call the service method
        List<Tickets> actualTickets = ticketsService.getPendingTicketsByPincode(pincode);
        
        // Assert that an empty list is returned
        assertTrue(actualTickets.isEmpty());
        
        // Verify that the repository method was called exactly once with the correct parameters
        verify(ticketsRepository, times(1))
                .findByStatusAndPincodeOrderByPriorityAsc(Constants.IN_PROGRESS, pincode);
    }
    @Test
    void testGetTicketsByUserId_WithTickets() {
        int userId = 100;
        List<Tickets> expectedTickets = Arrays.asList(ticket, ticket);
 
        // Stub the repository call with expected statuses.
        when(ticketsRepository.findByUserIdAndStatusIn((userId),
                eq(Arrays.asList(Constants.PENDING, Constants.IN_PROGRESS, Constants.DEFERRED))))
            .thenReturn(expectedTickets);
 
        List<Tickets> actualTickets = ticketsService.getTicketsByUserId(userId);
 
        // Verify the returned list matches our expected list.
        assertEquals(expectedTickets, actualTickets);
        verify(ticketsRepository, times(1)).findByUserIdAndStatusIn((userId),
                eq(Arrays.asList(Constants.PENDING, Constants.IN_PROGRESS, Constants.DEFERRED)));
    }
 
    // ✅ Positive Test: When no tickets exist for the user
    @Test
    void testGetTicketsByUserId_NoTickets() {
        int userId = 200;
        
        // Stub the repository to return an empty list.
        when(ticketsRepository.findByUserIdAndStatusIn((userId),
                eq(Arrays.asList(Constants.PENDING, Constants.IN_PROGRESS, Constants.DEFERRED))))
            .thenReturn(Collections.emptyList());
 
        List<Tickets> actualTickets = ticketsService.getTicketsByUserId(userId);
 
        // Assert that the result is an empty list.
        assertTrue(actualTickets.isEmpty());
        verify(ticketsRepository, times(1)).findByUserIdAndStatusIn((userId),
                eq(Arrays.asList(Constants.PENDING, Constants.IN_PROGRESS, Constants.DEFERRED)));
    }
    @Test
    void testGetTicketsByEngineerId_WithTickets() {
        int engineerId = 200;
        List<Tickets> expectedTickets = Arrays.asList(ticket, ticket);
 
        when(ticketsRepository.findByEngineerIdAndStatus(engineerId, Constants.PENDING))
                .thenReturn(expectedTickets);
 
        List<Tickets> actualTickets = ticketsService.getTicketsByEngineerId(engineerId);
 
        assertNotNull(actualTickets);
        assertEquals(2, actualTickets.size());
        assertEquals(expectedTickets, actualTickets);
 
        verify(ticketsRepository, times(1))
                .findByEngineerIdAndStatus(engineerId, Constants.PENDING);
    }
 
    // ✅ Positive Test Case: When there are no pending tickets for the engineer
    @Test
    void testGetTicketsByEngineerId_NoTickets() {
        int engineerId = 300; // engineer id with no pending tickets
        
        when(ticketsRepository.findByEngineerIdAndStatus(engineerId, Constants.PENDING))
                .thenReturn(Collections.emptyList());
 
        List<Tickets> actualTickets = ticketsService.getTicketsByEngineerId(engineerId);
 
        assertNotNull(actualTickets);
        assertTrue(actualTickets.isEmpty());
 
        verify(ticketsRepository, times(1))
                .findByEngineerIdAndStatus(engineerId, Constants.PENDING);
    }
    // Positive Test: When tickets exist for the given engineer with status IN_PROGRESS
    @Test
    void testGetTicketsByEngineerIds_WithTickets() {
        int engineerId = 300;
        List<Tickets> expectedTickets = Arrays.asList(ticket, ticket);
 
        // Stub the repository call to return our expected list
        when(ticketsRepository.findByEngineerIdAndStatus(engineerId, Constants.IN_PROGRESS))
                .thenReturn(expectedTickets);
 
        List<Tickets> actualTickets = ticketsService.getTicketsByEngineerIds(engineerId);
 
        assertNotNull(actualTickets);
        assertEquals(expectedTickets.size(), actualTickets.size());
        assertEquals(expectedTickets, actualTickets);
 
        verify(ticketsRepository, times(1))
                .findByEngineerIdAndStatus(engineerId, Constants.IN_PROGRESS);
    }
 
    // Positive Test: When no tickets exist for the given engineer with status IN_PROGRESS
    @Test
    void testGetTicketsByEngineerIds_NoTickets() {
        int engineerId = 400; // Assume engineer with id 400 has no tickets IN_PROGRESS
 
        // Stub repository to return an empty list
        when(ticketsRepository.findByEngineerIdAndStatus(engineerId, Constants.IN_PROGRESS))
                .thenReturn(Collections.emptyList());
 
        List<Tickets> actualTickets = ticketsService.getTicketsByEngineerIds(engineerId);
 
        assertNotNull(actualTickets);
        assertTrue(actualTickets.isEmpty());
 
        verify(ticketsRepository, times(1))
                .findByEngineerIdAndStatus(engineerId, Constants.IN_PROGRESS);
    }
    @Test
    void testUpdateTicketStatus_UnauthorizedEngineerException() {
        // When fetching the ticket, return our sample ticket
        when(ticketsRepository.findById(1)).thenReturn(Optional.of(ticket));
        
        // Attempt to update the ticket status with a mismatching engineer id (e.g. 100)
        UnauthorizedEngineerException exception = assertThrows(UnauthorizedEngineerException.class, () -> {
            ticketsService.updateTicketStatus(1, 100,Constants.COMPLETED);
        });
        
        // Verify the exception message
        assertEquals("Engineer not authorized for this ticket", exception.getMessage());
        
        // Ensure that ticketsRepository.save() is never called since the exception is thrown early.
        verify(ticketsRepository, never()).save(any(Tickets.class));
    }
    
    @Test
    void testGetBestEngineer_Success() {
        Accounts engineer1 = new Accounts();
        engineer1.setId(1);
        engineer1.setPincode(500001);
 
        Accounts engineer2 = new Accounts();
        engineer2.setId(2);
        engineer2.setPincode(500001);
 
        when(accountsRepository.findByRoleAndPincode(Constants.ENGINEER, 500001))
            .thenReturn(Arrays.asList(engineer1, engineer2));
        when(leaveHistoryService.isEngineerOnLeave(anyInt(), any(Date.class))).thenReturn(false);
        when(ticketsRepository.countByEngineerIdAndStatus(anyInt(), eq(Constants.IN_PROGRESS))).thenReturn((long) 2);
 
        Integer bestEngineerId = ticketsService.getBestEngineer(17.3850, 78.4867, 500001, null);
        assertNotNull(bestEngineerId);
    }
 
    @Test
    void testGetBestEngineer_NoEngineersAvailable() {
        when(accountsRepository.findByRoleAndPincode(Constants.ENGINEER, 500001)).thenReturn(Collections.emptyList());
 
        Integer bestEngineerId = ticketsService.getBestEngineer(17.3850, 78.4867, 500001, null);
        assertNull(bestEngineerId);
    }
    
    @Test
    void testReassignEngineer_Success() {
        // Create a sample ticket
        ticket.setStatus(Constants.DEFERRED); // Ensure the ticket is eligible for reassignment
        ticket.setEngineerId(1); // Current engineer
 
        // Mock ticket retrieval
        when(ticketsRepository.findById(1)).thenReturn(Optional.of(ticket));
 
        // Spy on ticketsService to control `getBestEngineer`
        TicketsService spyService = spy(ticketsService);
        doReturn(2).when(spyService).getBestEngineer(anyDouble(), anyDouble(), anyInt(), anyInt()); // Return a new engineer
 
        // Mock ticket save
        when(ticketsRepository.save(any(Tickets.class))).thenReturn(ticket);
 
        // Execute method
        String result = spyService.reassignEngineer(1);
 
        // Assert expected output
        assertEquals("Ticket 1 successfully reassigned from engineer 1 to engineer 2 in pincode 500001", result);
 
        // Verify ticket was updated and saved
        verify(ticketsRepository, times(1)).save(any(Tickets.class));
    }
 
 
 
    @Test
    void testReassignEngineer_NoEngineerAvailable() {
        // Create a sample ticket
        ticket.setStatus(Constants.REJECTED); // Ensure ticket is eligible
        ticket.setEngineerId(1); // Current engineer
 
        // Mock ticket retrieval
        when(ticketsRepository.findById(1)).thenReturn(Optional.of(ticket));
 
        // Spy to return null engineer (no engineer found)
        TicketsService spyService = spy(ticketsService);
        doReturn(null).when(spyService).getBestEngineer(anyDouble(), anyDouble(), anyInt(), anyInt());
 
        // Execute & expect exception
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () -> spyService.reassignEngineer(1));
 
        // Assert expected exception message
        assertEquals("No suitable alternative engineer found in pincode: " + ticket.getPincode(), exception.getMessage());
 
        // Verify ticket is NOT saved
        verify(ticketsRepository, never()).save(any(Tickets.class));
    }
    
    @Test
    void testReassignEngineer_TicketNotEligible() {
        // Set the ticket to an ineligible status (e.g., IN_PROGRESS)
        ticket.setStatus(Constants.IN_PROGRESS);
        ticket.setEngineerId(1);
 
        // Mock ticket retrieval
        when(ticketsRepository.findById(1)).thenReturn(Optional.of(ticket));
 
        // Execute & expect exception
        InvalidReassignmentException exception = assertThrows(InvalidReassignmentException.class, () -> ticketsService.reassignEngineer(1));
 
        // Assert expected exception message
        assertEquals("Ticket is not eligible for reassignment. Status must be DEFERRED or REJECTED", exception.getMessage());
 
        // Verify that ticket is NOT saved
        verify(ticketsRepository, never()).save(any(Tickets.class));
    }
 
 
    
    @Test
    void testAssignPriority_Success() {
        assertEquals("P1", ticketsService.assignPriority("FAULT", "WIFI"));
        assertEquals("P2", ticketsService.assignPriority("FAULT", "DTH"));
        assertEquals("P3", ticketsService.assignPriority("FAULT", "LANDLINE"));
        assertEquals("P4", ticketsService.assignPriority("INSTALLATION", "WIFI"));
        assertEquals("P5", ticketsService.assignPriority("INSTALLATION", "DTH"));
        assertEquals("P6", ticketsService.assignPriority("INSTALLATION", "LANDLINE"));
    }
 
    @Test
    void testAssignPriority_InvalidInputs() {
        assertNull(ticketsService.assignPriority(null, "WIFI"));
        assertNull(ticketsService.assignPriority("FAULT", null));
        assertNull(ticketsService.assignPriority("UNKNOWN", "WIFI"));
        assertNull(ticketsService.assignPriority("FAULT", "UNKNOWN"));
    }
 
    @Test
    void testGetBestEngineerLocation_Success() {
        // Mock ticket data
        ticket.setEngineerId(1);
        when(ticketsRepository.findById(1)).thenReturn(Optional.of(ticket));
 
        // Spy service to return a mock engineer ID
        TicketsService spyService = spy(ticketsService);
        doReturn(200).when(spyService).getBestEngineer(anyDouble(), anyDouble(), anyInt(), anyInt());
 
        // Mock engineer data
        Accounts engineer = new Accounts();
        engineer.setId(200);
        engineer.setLatitude(17.4000);
        engineer.setLongitude(78.5000);
        when(accountsRepository.findById(200)).thenReturn(Optional.of(engineer));
 
        // Execute method
        Map<String, Double> result = spyService.getBestEngineerLocation(1);
 
        // Assertions
        assertNotNull(result);
        assertEquals(17.3850, result.get("userLat"));
        assertEquals(78.4867, result.get("userLng"));
        assertEquals(17.4000, result.get("engineerLat"));
        assertEquals(78.5000, result.get("engineerLng"));
 
        // Verify interactions
        verify(ticketsRepository).findById(1);
        verify(accountsRepository).findById(200);
    }
    @Test
    void testGetBestEngineerLocation_EngineerNotFound() {
        when(ticketsRepository.findById(1)).thenReturn(Optional.of(ticket));
 
        // Spy to return no engineer (null)
        TicketsService spyService = spy(ticketsService);
        doReturn(null).when(spyService).getBestEngineer(anyDouble(), anyDouble(), anyInt(), anyInt());
 
        // Execute & expect exception
        EngineerNotFoundException exception = assertThrows(EngineerNotFoundException.class, () -> spyService.getBestEngineerLocation(1));
 
        // Assertion
        assertEquals("No suitable engineer found for ticket ID: 1", exception.getMessage());
 
        // Verify interactions
        verify(ticketsRepository).findById(1);
    }
    @Test
    void testGetBestEngineerLocation_AssignedEngineerNotFound() {
        when(ticketsRepository.findById(1)).thenReturn(Optional.of(ticket));
 
        // Spy service to return an engineer ID
        TicketsService spyService = spy(ticketsService);
        doReturn(200).when(spyService).getBestEngineer(anyDouble(), anyDouble(), anyInt(), anyInt());
 
        // Engineer ID is not found in the repository
        when(accountsRepository.findById(200)).thenReturn(Optional.empty());
 
        // Execute & expect exception
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () -> spyService.getBestEngineerLocation(1));
 
        // Assertion 
        assertEquals("Assigned engineer not found with ID: 200", exception.getMessage());
 
        // Verify interactions
        verify(ticketsRepository).findById(1);
        verify(accountsRepository).findById(200);
    }
 
 
    @Test
    void testGetDeferredTicketsByPincode() {
        List<Tickets> deferredTickets = Arrays.asList(new Tickets(), new Tickets());
        when(ticketsRepository.findByStatusAndPincode(Constants.DEFERRED, 500001)).thenReturn(deferredTickets);
        assertEquals(2, ticketsService.getDeferredTicketsByPincode(500001).size());
    }
 
    @Test
    void testGetRejectedTicketsByPincode() {
        List<Tickets> rejectedTickets = Arrays.asList(new Tickets(), new Tickets(), new Tickets());
        when(ticketsRepository.findByStatusAndPincode(Constants.REJECTED, 500001)).thenReturn(rejectedTickets);
        assertEquals(3, ticketsService.getRejectedTicketsByPincode(500001).size());
    }
    
    @Test
    void testGetCompletedOrFailedTicketsByUser_Positive() {
        when(ticketsRepository.findByUserIdAndStatusIn(101, List.of("COMPLETED", "FAILED")))
                .thenReturn(List.of(ticket));

        List<Tickets> result = ticketsService.getCompletedOrFailedTicketsByUser(101);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("COMPLETED", result.get(0).getStatus());
    }

    // ❌ NEGATIVE: Get Completed/Failed Tickets by User (No Tickets Found)
    @Test
    void testGetCompletedOrFailedTicketsByUser_Negative() {
        when(ticketsRepository.findByUserIdAndStatusIn(999, List.of("COMPLETED", "FAILED")))
                .thenReturn(Collections.emptyList());

        List<Tickets> result = ticketsService.getCompletedOrFailedTicketsByUser(999);

        assertTrue(result.isEmpty());
    }

    // ✅ POSITIVE: Get Completed/Failed Tickets by Engineer
    @Test
    void testGetCompletedOrFailedTicketsByEngineer_Positive() {
        when(ticketsRepository.findByEngineerIdAndStatusIn(201, List.of("COMPLETED", "FAILED")))
                .thenReturn(List.of(ticket,ticket1));

        List<Tickets> result = ticketsService.getCompletedOrFailedTicketsByEngineer(201);

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
    }

    // ❌ NEGATIVE: Get Completed/Failed Tickets by Engineer (Invalid Engineer ID)
    @Test
    void testGetCompletedOrFailedTicketsByEngineer_Negative() {
        when(ticketsRepository.findByEngineerIdAndStatusIn(999, List.of("COMPLETED", "FAILED")))
                .thenReturn(Collections.emptyList());

        List<Tickets> result = ticketsService.getCompletedOrFailedTicketsByEngineer(999);

        assertTrue(result.isEmpty());
    }

    // ✅ POSITIVE: Get Completed/Failed Tickets by Admin (Pincode)
    @Test
    void testGetCompletedOrFailedTicketsByAdmin_Positive() {
        when(ticketsRepository.findByPincodeAndStatusIn(12345, List.of("COMPLETED", "FAILED")))
                .thenReturn(List.of(ticket,ticket1));

        List<Tickets> result = ticketsService.getCompletedOrFailedTicketsByAdmin(12345);

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
    }

    // ❌ NEGATIVE: Get Completed/Failed Tickets by Admin (No Data)
    @Test
    void testGetCompletedOrFailedTicketsByAdmin_Negative() {
        when(ticketsRepository.findByPincodeAndStatusIn(99999, List.of("COMPLETED", "FAILED")))
                .thenReturn(Collections.emptyList());

        List<Tickets> result = ticketsService.getCompletedOrFailedTicketsByAdmin(99999);

        assertTrue(result.isEmpty());
    }

    // ✅ POSITIVE: Count Tickets by Status & Pincode
    @Test
    void testCountByStatusAndPincode_Positive() {
        when(ticketsRepository.countByStatusAndPincode("IN_PROGRESS", 12345)).thenReturn(5L);

        Long count = ticketsService.countByStatusAndPincode("IN_PROGRESS", 12345);

        assertEquals(5L, count);
    }

    // ❌ NEGATIVE: Count Tickets by Status & Pincode (Invalid Pincode)
    @Test
    void testCountByStatusAndPincode_Negative() {
        when(ticketsRepository.countByStatusAndPincode("IN_PROGRESS", 99999)).thenReturn(0L);

        Long count = ticketsService.countByStatusAndPincode("IN_PROGRESS", 99999);

        assertEquals(0L, count);
    }

    // ✅ POSITIVE: Get Total Ticket Count by Pincode
    @Test
    void testGetTotalTicketCount_Positive() {
        when(ticketsRepository.countByStatusInAndPincode(Arrays.asList("FAILED", "IN_PROGRESS", "DEFERRED", "REJECTED"), 12345))
                .thenReturn(10L);

        long count = ticketsService.getTotalTicketCount(12345);

        assertEquals(10L, count);
    }

    // ❌ NEGATIVE: Get Total Ticket Count by Pincode (No Tickets)
    @Test
    void testGetTotalTicketCount_Negative() {
        when(ticketsRepository.countByStatusInAndPincode(anyList(), eq(99999)))
                .thenReturn(0L);

        long count = ticketsService.getTotalTicketCount(99999);

        assertEquals(0L, count);
    }

    // ✅ POSITIVE: Get Pending Ticket Count by Engineer ID
    @Test
    void testGetPendingTicketCount_Positive() {
        when(ticketsRepository.countByStatusAndEngineerId("PENDING", 201L)).thenReturn(3);

        int count = ticketsService.getPendingTicketCount(201L);

        assertEquals(3, count);
    }

    // ❌ NEGATIVE: Get Pending Ticket Count (Invalid Engineer ID)
    @Test
    void testGetPendingTicketCount_Negative() {
        when(ticketsRepository.countByStatusAndEngineerId("PENDING", 999L)).thenReturn(0);

        int count = ticketsService.getPendingTicketCount(999L);

        assertEquals(0, count);
    }
 
}