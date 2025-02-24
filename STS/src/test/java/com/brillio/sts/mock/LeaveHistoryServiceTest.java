package com.brillio.sts.mock;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import com.brillio.sts.model.LeaveHistory;
import com.brillio.sts.model.Accounts;
import com.brillio.sts.repo.LeaveHistoryRepository;
import com.brillio.sts.service.LeaveHistoryService;
import com.brillio.sts.repo.AccountsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
 
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
 
@ExtendWith(MockitoExtension.class)
class LeaveHistoryServiceTest {
 
    @Mock
    private LeaveHistoryRepository leaveHistoryRepository;
 
    @Mock
    private AccountsRepository accountsRepository;
 
    @InjectMocks
    private LeaveHistoryService leaveHistoryService;
 
    private LeaveHistory mockLeave;
    private Accounts mockEngineer;
    private Date startDate;
    private Date endDate;
 
    @BeforeEach
    void setUp() {
        startDate = new Date(System.currentTimeMillis() + (1000 * 60 * 60 * 24 * 3)); // 3 days ahead
        endDate = new Date(System.currentTimeMillis() + (1000 * 60 * 60 * 24 * 5)); // 5 days ahead
        mockLeave = new LeaveHistory(1, 3, "Pending Approval", 101, startDate, endDate, "Annual", "PENDING", "Vacation");
        mockEngineer = new Accounts(101, "John", "Doe", "john@example.com", "password123",
                "ENGINEER", "9876543210", "Hyderabad", 500001,
                "What is your pet’s name?", "Charlie",
                "APPROVED", "Male", 17.3850, 78.4867);
    }
 
    // ✅ 1. Test Calculate Leave Days
    @Test
    void testCalculateDays_ShouldReturnCorrectDays() {
        int leaveDays = leaveHistoryService.calculateDays(startDate, endDate);
        assertEquals(3, leaveDays); // 3 days leave + 1 (inclusive)
    }
 
    // ✅ 2. Test Apply Leave Successfully
    @Test
    void testApplyLeave_ShouldReturnSuccessMessage() {
        when(accountsRepository.findById(101)).thenReturn(Optional.of(mockEngineer));
        when(leaveHistoryRepository.save(any(LeaveHistory.class))).thenReturn(mockLeave);
 
        String result = leaveHistoryService.applyLeave(mockLeave);
        assertEquals("Leave Applied Successfully.", result);
    }
 
    // ✅ 3. Test Engineer On Leave Check (Approved Leave)
    @Test
    void testIsEngineerOnLeave_ShouldReturnTrue() {
        when(leaveHistoryRepository.findByEngineerIdAndLeaveStatus(101, "APPROVED"))
                .thenReturn(Arrays.asList(mockLeave));
 
        boolean isOnLeave = leaveHistoryService.isEngineerOnLeave(101, startDate);
        assertTrue(isOnLeave);
    }
    
 
    // ✅ 4. Test Fetch Leaves by Pincode
    @Test
    void testGetLeavesByPincode_ShouldReturnLeaveList() {
        when(accountsRepository.findByRoleAndPincode("ENGINEER", 500001))
                .thenReturn(Arrays.asList(mockEngineer));
        when(leaveHistoryRepository.findByLeaveStatusAndEngineerIdIn("PENDING", Arrays.asList(101)))
                .thenReturn(Arrays.asList(mockLeave));
 
        List<LeaveHistory> leaves = leaveHistoryService.getLeavesByPincode(500001);
        assertFalse(leaves.isEmpty());
        assertEquals(1, leaves.size());
    }
 
    // ✅ 5. Test Approve or Reject Leave
    @Test
    void testApproveOrRejectLeave_ShouldReturnSuccessMessage() {
        when(leaveHistoryRepository.findById(1)).thenReturn(Optional.of(mockLeave));
        when(leaveHistoryRepository.save(any(LeaveHistory.class))).thenReturn(mockLeave);
 
        String result = leaveHistoryService.approveOrRejectLeave(1, "APPROVED", "Approved by Admin");
        assertEquals("Leave status updated successfully", result);
    }
 
    // ❌ 6. Negative Test: Apply Leave with Invalid Dates (Start > End)
    @Test
    void testApplyLeave_InvalidDateRange_ShouldReturnError() {
        mockLeave.setLeaveStartDate(endDate);
        mockLeave.setLeaveEndDate(startDate);
 
        String result = leaveHistoryService.applyLeave(mockLeave);
        assertEquals("Leave start date must not be greater than leave end date.", result);
    }
 
    // ✅ **3. Test Apply Leave with Same Start and End Date (1-Day Leave)**
    @Test
    void testApplyLeave_SameStartAndEndDate_ShouldPass() {
        mockLeave.setLeaveStartDate(startDate);
        mockLeave.setLeaveEndDate(startDate);
 
        when(accountsRepository.findById(101)).thenReturn(Optional.of(mockEngineer));
        when(leaveHistoryRepository.save(any(LeaveHistory.class))).thenReturn(mockLeave);
 
        String result = leaveHistoryService.applyLeave(mockLeave);
        assertEquals("Leave Applied Successfully.", result);
    }
// ❌ 6. Negative Test: Apply Leave with Past Start Date
    @Test
    void testApplyLeave_StartDateInPast_ShouldReturnError() {
        // Set start date in the past
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -2); // 2 days before today
        Date pastStartDate = calendar.getTime();
 
        mockLeave.setLeaveStartDate(pastStartDate);
 
        String result = leaveHistoryService.applyLeave(mockLeave);
        assertEquals("Leave start date cannot be in the past.", result);
    }
 
 
 
    // ✅ **4. Test Apply Leave for Engineer with Valid Account**
    @Test
    void testApplyLeave_ValidEngineer_ShouldPass() {
        when(accountsRepository.findById(mockLeave.getEngineerId())).thenReturn(Optional.of(mockEngineer));
        when(leaveHistoryRepository.save(any(LeaveHistory.class))).thenReturn(mockLeave);
 
        String result = leaveHistoryService.applyLeave(mockLeave);
        assertEquals("Leave Applied Successfully.", result);
    }
 
    // ✅ **5. Test Apply Leave with Valid Leave Type**
    @Test
    void testApplyLeave_ValidLeaveType_ShouldPass() {
        mockLeave.setLeaveType("PL"); // Paid Leave
 
        when(accountsRepository.findById(mockLeave.getEngineerId())).thenReturn(Optional.of(mockEngineer));
        when(leaveHistoryRepository.save(any(LeaveHistory.class))).thenReturn(mockLeave);
 
        String result = leaveHistoryService.applyLeave(mockLeave);
        assertEquals("Leave Applied Successfully.", result);
    }
 
 
    // ❌ 9. Negative Test: Apply Leave for Non-Existing Engineer
    @Test
    void testApplyLeave_NonExistingEngineer_ShouldReturnError() {
        when(accountsRepository.findById(999)).thenReturn(Optional.empty());
 
        mockLeave.setEngineerId(999);
        String result = leaveHistoryService.applyLeave(mockLeave);
        assertEquals("Employee not found.", result);
    }
 
    // ❌ 10. Negative Test: Engineer Not on Leave
    @Test
    void testIsEngineerOnLeave_ShouldReturnFalse() {
        when(leaveHistoryRepository.findByEngineerIdAndLeaveStatus(101, "APPROVED")).thenReturn(List.of());
 
        boolean isOnLeave = leaveHistoryService.isEngineerOnLeave(101, startDate);
        assertFalse(isOnLeave);
    }
 
    // ❌ 11. Negative Test: Approve or Reject Non-Existing Leave Request
    @Test
    void testApproveOrRejectLeave_NonExistingLeave_ShouldReturnError() {
        when(leaveHistoryRepository.findById(999)).thenReturn(Optional.empty());
 
        String result = leaveHistoryService.approveOrRejectLeave(999, "REJECTED", "Not Eligible");
        assertEquals("Leave request not found", result);
    }
    
    @Test
    void testApplyLeave_EndDateInPast_ShouldReturnError() {
        // Set leave start date to tomorrow
        Calendar futureCalendar = Calendar.getInstance();
        futureCalendar.add(Calendar.DAY_OF_MONTH, 2); // 2 days in the future
        Date futureStartDate = futureCalendar.getTime();
 
        // Set leave end date to yesterday
        Calendar pastCalendar = Calendar.getInstance();
        pastCalendar.add(Calendar.DAY_OF_MONTH, -1); // 1 day in the past
        Date pastEndDate = pastCalendar.getTime();
 
        mockLeave.setLeaveStartDate(futureStartDate);
        mockLeave.setLeaveEndDate(pastEndDate);
 
        String result = leaveHistoryService.applyLeave(mockLeave);
        // With current ordering, the first condition (leaveDays < 1) triggers:
        assertEquals("Leave start date must not be greater than leave end date.", result);
    }
    @Test
    void testGetLeavesByPincode_NoEngineersFound_ShouldReturnEmptyList() {
        int pincode = 999999;  // Assume this pincode has no engineers
 
        // Mock the accountsRepository to return an empty list for the given pincode.
        when(accountsRepository.findByRoleAndPincode("ENGINEER", pincode))
                .thenReturn(Collections.emptyList());
 
        // Call the method under test
        List<LeaveHistory> result = leaveHistoryService.getLeavesByPincode(pincode);
 
        // Verify that the result is an empty list
        assertNotNull(result);
        assertTrue("Expected an empty list when no engineers are found", result.isEmpty());
 
 
        // Verify that leaveHistoryRepository.findByLeaveStatusAndEngineerIdIn is never called
        verify(leaveHistoryRepository, never()).findByLeaveStatusAndEngineerIdIn(anyString(), anyList());
    }
    @Test
    void testApplyLeave_EndDateInPast_ShouldReturnError_SpyOverride() {
        // Create fixed dates for the test.
        Calendar futureCalendar = Calendar.getInstance();
        futureCalendar.add(Calendar.DAY_OF_MONTH, 2); // leaveStartDate: 2 days in the future
        Date futureStartDate = futureCalendar.getTime();
        
        Calendar futureCalendar2 = Calendar.getInstance();
        futureCalendar2.add(Calendar.DAY_OF_MONTH, 5); // leaveEndDate: 5 days in the future (normally valid)
        Date futureEndDate = futureCalendar2.getTime();
        
        // Set the leave dates in the leaveHistory object.
        mockLeave.setLeaveStartDate(futureStartDate);
        mockLeave.setLeaveEndDate(futureEndDate);
        
        // Create a spy of your service so we can override calculateDays for the end date.
        LeaveHistoryService spyService = spy(leaveHistoryService);
        
        // For the leave duration from today to leave start date, let it return a positive number.
        doReturn(2).when(spyService).calculateDays(any(Date.class), eq(futureStartDate));
        // For the duration from today to leave end date, force a negative value.
        doReturn(-1).when(spyService).calculateDays(any(Date.class), eq(futureEndDate));
        // For the duration between leaveStartDate and leaveEndDate, return a valid positive number.
        doReturn(4).when(spyService).calculateDays(futureStartDate, futureEndDate);

        
        // When the engineer is looked up, return a valid engineer.
        when(accountsRepository.findById(mockLeave.getEngineerId())).thenReturn(Optional.of(mockEngineer));
        // Also, let the save method return the same object (for simplicity)
        when(leaveHistoryRepository.save(any(LeaveHistory.class))).thenReturn(mockLeave);
        
        // Now call applyLeave on the spy.
        String result = spyService.applyLeave(mockLeave);
        
        // Since daysToEnd (from today to leave end date) returns -1 (i.e. negative),
        // the branch should trigger and return:
        assertEquals("Leave end date cannot be in the past.", result);
    } 
 
}
 