package com.brillio.sts.junit;
 
import static org.junit.jupiter.api.Assertions.*;
 
import java.util.Date;
 
import org.junit.jupiter.api.Test;
 
import com.brillio.sts.model.LeaveHistory;
 
class LeaveHistoryTest {
 
    // ✅ 1. Test Getters and Setters (Positive)
    @Test
    void testGettersAndSetters() {
        LeaveHistory leave = new LeaveHistory();
        Date startDate = new Date();
        Date endDate = new Date(startDate.getTime() + (1000 * 60 * 60 * 24 * 3)); // 3 days later
 
        leave.setLeaveId(1);
        leave.setLeaveNoOfDays(3);
        leave.setAdminComments("Approved");
        leave.setEngineerId(101);
        leave.setLeaveStartDate(startDate);
        leave.setLeaveEndDate(endDate);
        leave.setLeaveType("Annual");
        leave.setLeaveStatus("APPROVED");
        leave.setLeaveReason("Vacation");
 
        assertEquals(1, leave.getLeaveId());
        assertEquals(3, leave.getLeaveNoOfDays());
        assertEquals("Approved", leave.getAdminComments());
        assertEquals(101, leave.getEngineerId());
        assertEquals(startDate, leave.getLeaveStartDate());
        assertEquals(endDate, leave.getLeaveEndDate());
        assertEquals("Annual", leave.getLeaveType());
        assertEquals("APPROVED", leave.getLeaveStatus());
        assertEquals("Vacation", leave.getLeaveReason());
    }
 
    // ✅ 2. Test No-Args Constructor (Positive)
    @Test
    void testNoArgsConstructor() {
        LeaveHistory newLeave = new LeaveHistory();
        assertNotNull(newLeave);
    }
 
    // ✅ 3. Test All-Args Constructor (Positive)
    @Test
    void testAllArgsConstructor() {
        Date startDate = new Date();
        Date endDate = new Date(startDate.getTime() + (1000 * 60 * 60 * 24 * 7)); // 7 days later
 
        LeaveHistory newLeave = new LeaveHistory(2, 7, "Manager Approved", 102, startDate, endDate, "Sick", "PENDING", "Medical");
 
        assertEquals(2, newLeave.getLeaveId());
        assertEquals(7, newLeave.getLeaveNoOfDays());
        assertEquals("Manager Approved", newLeave.getAdminComments());
        assertEquals(102, newLeave.getEngineerId());
        assertEquals(startDate, newLeave.getLeaveStartDate());
        assertEquals(endDate, newLeave.getLeaveEndDate());
        assertEquals("Sick", newLeave.getLeaveType());
        assertEquals("PENDING", newLeave.getLeaveStatus());
        assertEquals("Medical", newLeave.getLeaveReason());
    }
 
    // ✅ 4. Test toString() Method (Positive)
    @Test
    void testToString() {
        Date startDate = new Date();
        Date endDate = new Date(startDate.getTime() + (1000 * 60 * 60 * 24 * 5)); // 5 days later
 
        LeaveHistory newLeave = new LeaveHistory(3, 5, "Admin Approved", 103, startDate, endDate, "Casual", "APPROVED", "Family Event");
 
        String expectedString = "LeaveHistory [leaveId=3, leaveNoOfDays=5, adminComments=Admin Approved, engineerId=103, "
                + "leaveStartDate=" + startDate + ", leaveEndDate=" + endDate + ", leaveType=Casual, leaveStatus=APPROVED, leaveReason=Family Event]";
 
        assertEquals(expectedString, newLeave.toString());
    }
 
    // ❌ 5. Negative Test: Getters and Setters with Invalid Data
    @Test
    void testNegativeGettersAndSetters() {
        LeaveHistory leave = new LeaveHistory();
        leave.setLeaveId(-1); // ❌ Negative ID
        leave.setLeaveNoOfDays(-3); // ❌ Negative leave days
        leave.setAdminComments(null); // ❌ Null Admin Comments
        leave.setEngineerId(-101); // ❌ Negative Engineer ID
        leave.setLeaveStartDate(null); // ❌ Null Start Date
        leave.setLeaveEndDate(null); // ❌ Null End Date
        leave.setLeaveType(""); // ❌ Empty Leave Type
        leave.setLeaveStatus("Unknown"); // ❌ Invalid Status
        leave.setLeaveReason(null); // ❌ Null Reason
 
        assertNotEquals(1, leave.getLeaveId());
        assertNotEquals(3, leave.getLeaveNoOfDays());
        assertNull(leave.getAdminComments());
        assertNotEquals(101, leave.getEngineerId());
        assertNull(leave.getLeaveStartDate());
        assertNull(leave.getLeaveEndDate());
        assertNotEquals("Annual", leave.getLeaveType());
        assertNotEquals("APPROVED", leave.getLeaveStatus());
        assertNull(leave.getLeaveReason());
    }
 
    // ❌ 6. Negative Test: toString() should not match incorrect format
    @Test
    void testNegativeToString() {
        Date startDate = new Date();
        Date endDate = new Date(startDate.getTime() + (1000 * 60 * 60 * 24 * 4)); // 4 days later
 
        LeaveHistory leave = new LeaveHistory(4, 4, "Manager Rejected", 104, startDate, endDate, "Sick", "REJECTED", "Personal");
 
        String unexpectedResult = "LeaveHistory(leaveId=4, leaveNoOfDays=4, adminComments=Wrong Comment, engineerId=104, "
                + "leaveStartDate=" + startDate + ", leaveEndDate=" + endDate + ", leaveType=Sick, leaveStatus=REJECTED, leaveReason=Personal)";
 
        assertNotEquals(unexpectedResult, leave.toString());
    }
 
    // ❌ 7. Negative Test: Constructor with Invalid Data
    @Test
    void testNegativeConstructor() {
        Date startDate = null; // ❌ Null Start Date
        LeaveHistory leave = new LeaveHistory(-1, -5, null, -100, startDate, null, "", "InvalidStatus", null);
 
        assertNotEquals(1, leave.getLeaveId());
        assertNotEquals(5, leave.getLeaveNoOfDays());
        assertNull(leave.getAdminComments());
        assertNotEquals(100, leave.getEngineerId());
        assertNull(leave.getLeaveStartDate());
        assertNull(leave.getLeaveEndDate());
        assertNotEquals("Annual", leave.getLeaveType());
        assertNotEquals("APPROVED", leave.getLeaveStatus());
        assertNull(leave.getLeaveReason());
    }
}

