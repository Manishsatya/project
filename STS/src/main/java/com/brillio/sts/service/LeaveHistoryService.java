package com.brillio.sts.service;
 
import com.brillio.sts.model.LeaveHistory;
import com.brillio.sts.model.Accounts;
import com.brillio.sts.repo.LeaveHistoryRepository;
import com.brillio.sts.repo.AccountsRepository;
import jakarta.transaction.Transactional;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;
import java.util.Optional;
 
@Service
@Transactional
public class LeaveHistoryService {
 
    private static final Logger logger = Logger.getLogger(LeaveHistoryService.class);
    private final LeaveHistoryRepository leaveHistoryRepository;
    private final AccountsRepository accountsRepository;
 
    public LeaveHistoryService(LeaveHistoryRepository leaveHistoryRepository, AccountsRepository accountsRepository) {
        this.leaveHistoryRepository = leaveHistoryRepository;
        this.accountsRepository = accountsRepository;
    }
 
    public int calculateDays(Date leaveStartDate, Date leaveEndDate) {
        long ms = leaveEndDate.getTime() - leaveStartDate.getTime();
        long diff = ((ms) / (1000 * 60 * 60 * 24)) + 1;
        logger.info("Calculated leave days: "+ diff);
        return (int) diff;
    }
    public String applyLeave(LeaveHistory leaveHistory) {
        logger.info("Applying leave for Engineer ID: "+ leaveHistory.getEngineerId());
        Date today = new Date();
 
        int leaveDays = calculateDays(leaveHistory.getLeaveStartDate(), leaveHistory.getLeaveEndDate());
        int daysToStart = calculateDays(today, leaveHistory.getLeaveStartDate());
        int daysToEnd = calculateDays(today, leaveHistory.getLeaveEndDate());
 
        if (leaveDays < 1) {
            logger.warn("Invalid leave request: Leave start date is after leave end date.");
            return "Leave start date must not be greater than leave end date.";
        }
        if (daysToStart < 0) {
            logger.warn("Invalid leave request: Leave start date is in the past.");
            return "Leave start date cannot be in the past.";
        }
        if (daysToEnd < 0) {
            logger.warn("Invalid leave request: Leave end date is in the past.");
            return "Leave end date cannot be in the past.";
        }
 
        Optional<Accounts> accountOpt = accountsRepository.findById(leaveHistory.getEngineerId());
        if (accountOpt.isEmpty()) {
            logger.warn("Engineer not found with ID: "+leaveHistory.getEngineerId());
            return "Employee not found.";
        }
 
        leaveHistory.setLeaveNoOfDays(leaveDays);
        leaveHistory.setLeaveStatus("PENDING");
        leaveHistoryRepository.save(leaveHistory);
        logger.info("Leave applied successfully for Engineer ID: "+ leaveHistory.getEngineerId());
 
        return "Leave Applied Successfully.";
    }
 
    public boolean isEngineerOnLeave(int engineerId, Date date) {
        logger.info("Checking leave status for Engineer ID:  on date: "+ engineerId+ date);
        List<LeaveHistory> leaveRecords = leaveHistoryRepository.findByEngineerIdAndLeaveStatus(engineerId, "APPROVED");
 
        for (LeaveHistory leave : leaveRecords) {
            if (!date.before(leave.getLeaveStartDate()) && !date.after(leave.getLeaveEndDate())) {
                logger.info("Engineer ID:  is on leave on date: "+ engineerId+ date);
                return true;
            }
        }
        logger.info("Engineer ID:  is available on date: "+ engineerId+ date);
        return false;
    }
 
    
    public List<LeaveHistory> getLeavesByPincode(int pincode) {
        logger.info("Fetching leave requests for engineers in pincode: "+ pincode);
        List<Accounts> engineers = accountsRepository.findByRoleAndPincode("ENGINEER", pincode);
        List<Integer> engineerIds = engineers.stream().map(Accounts::getId).toList();
 
        if (engineerIds.isEmpty()) {
            logger.info("No engineers found in pincode: "+ pincode);
            return List.of();
        }
 
        List<LeaveHistory> pendingLeaves = leaveHistoryRepository.findByLeaveStatusAndEngineerIdIn("PENDING", engineerIds);
        logger.info("Found  pending leave requests in pincode: "+ pendingLeaves.size()+ pincode);
        return pendingLeaves;
    }
 
    public String approveOrRejectLeave(int leaveId, String status, String comments) {
        logger.info("Updating leave status for Leave ID:  to "+ leaveId+ status);
        LeaveHistory leaveHistory = leaveHistoryRepository.findById(leaveId).orElse(null);
        
        if (leaveHistory == null) {
            logger.warn("Leave request not found for Leave ID: "+ leaveId);
            return "Leave request not found";
        }
 
        leaveHistory.setLeaveStatus(status);
        leaveHistory.setAdminComments(comments);
        leaveHistoryRepository.save(leaveHistory);
        logger.info("Leave ID:  status updated to  with comments: "+ leaveId+ status+ comments);
        
        return "Leave status updated successfully";
    }
}
 