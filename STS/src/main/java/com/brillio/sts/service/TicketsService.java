package com.brillio.sts.service;
 
import java.util.*;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import com.brillio.sts.model.Constants;
import com.brillio.sts.exception.*;
import com.brillio.sts.model.*;
import com.brillio.sts.repo.*;
import jakarta.transaction.Transactional;
 
@Service
@Transactional
public class TicketsService {
    private static final Logger logger = Logger.getLogger(TicketsService.class);
    
    private final ConnectionsService connectionsService;
    private final TicketsRepository ticketsRepository;
    private final ConnectionsRepository connectionsRepository;
    private final AccountsRepository accountsRepository;
    private final LeaveHistoryService leaveHistoryService;
    private final EmailService emailService;
    
    public TicketsService(ConnectionsService connectionsService, TicketsRepository ticketsRepository,
                          ConnectionsRepository connectionsRepository, AccountsRepository accountsRepository,
                          LeaveHistoryService leaveHistoryService,
                          EmailService emailService) {
        this.connectionsService = connectionsService;
        this.ticketsRepository = ticketsRepository;
        this.connectionsRepository = connectionsRepository;
        this.accountsRepository = accountsRepository;
        this.leaveHistoryService = leaveHistoryService;
        this.emailService = emailService;
    }
    
    public List<Tickets> getPendingTicketsByPincode(int pincode) {
        logger.info("Fetching pending tickets for pincode: "+ pincode);
        return ticketsRepository.findByStatusAndPincodeOrderByPriorityAsc(Constants.IN_PROGRESS, pincode);
    }
    
    public List<Tickets> getTicketsByUserId(int userId) {
        logger.info("Fetching tickets for user ID: "+ userId);
        return ticketsRepository.findByUserIdAndStatusIn(userId, Arrays.asList(Constants.PENDING, Constants.IN_PROGRESS, Constants.DEFERRED));
    }
    
    public List<Tickets> getTicketsByEngineerId(int engineerId) {
        logger.info("Fetching pending tickets for engineer ID: "+ engineerId);
        return ticketsRepository.findByEngineerIdAndStatus(engineerId, Constants.PENDING);
    }
	
    /**
     * Retrieves a list of tickets assigned to a specific engineer that are currently in progress.
     * 
     * @param engineerId The ID of the engineer whose tickets need to be fetched.
     * @return A list of tickets that are assigned to the given engineer and have the status "IN_PROGRESS".
     * 
     * @author Manish.Chapala
     */
	public List<Tickets> getTicketsByEngineerIds(int engineerId){
		return ticketsRepository.findByEngineerIdAndStatus(engineerId, Constants.IN_PROGRESS);
	}
	
	
	/**
	 * Updates the status of a ticket and performs necessary actions based on the new status.
	 * Ensures the ticket exists and the engineer updating it is authorized.
	 * Updates the ticket status and timestamp in the database.
	 * If the status is COMPLETED, updates the connection status and sends appropriate emails.
	 * Sends notification emails to the user based on the updated status.
	 * 
	 * @param ticketId   The ID of the ticket to be updated.
	 * @param engineerId The ID of the engineer attempting to update the ticket.
	 * @param status     The new status to be set for the ticket.
	 * @throws TicketNotFoundException      if the ticket is not found.
	 * @throws UnauthorizedEngineerException if the engineer is not authorized for the ticket.
	 * @throws AccountNotFoundException     if the user account associated with the ticket is not found.
	 * @throws ConnectionNotFoundException  if the connection associated with the ticket is not found.
	 * 
	 * @author Manish.Chapala
	 */
	public void updateTicketStatus(int ticketId, int engineerId, String status) {
	    Tickets ticket = ticketsRepository.findById(ticketId)
	        .orElseThrow(() -> new TicketNotFoundException("Ticket not found with ID: " + ticketId));

	    if (ticket.getEngineerId() != engineerId) {
	        throw new UnauthorizedEngineerException("Engineer not authorized for this ticket");
	    }

	    ticket.setStatus(status);
	    ticket.setUpdatedAt(new Date());
	    ticketsRepository.save(ticket);

	    Accounts account = accountsRepository.findById(ticket.getUserId())
	        .orElseThrow(() -> new AccountNotFoundException("Account not found with ID: " + ticket.getUserId()));

	    if (Constants.COMPLETED.equals(status)) {
	        int connectionId = ticket.getConnectionId();
	        Connections connection = connectionsRepository.findById(connectionId)
	            .orElseThrow(() -> new ConnectionNotFoundException("Connection not found for ID: " + connectionId));
	        connection.setStatus(Constants.ACTIVE);
	        connection.setStartDate(new Date());
	        connection.setExpiryDate(connectionsService.setExpiryDate(connection));
	        connectionsRepository.save(connection);

	        if (Constants.INSTALLATION.equals(ticket.getServiceType())) {
	            emailService.sendServiceActivationEmail(account.getEmail(), ticket.getTicketId(), Constants.INSTALLATION);
	        } else {
	            emailService.sendTicketResolvedEmail(account.getEmail(), ticket.getTicketId());
	        }
	    } else if (Constants.FAILED.equals(status)) {
	        emailService.sendTicketFailedEmail(account.getEmail(), ticket.getTicketId());
	    } else if (Constants.DEFERRED.equals(status)) {
	        emailService.sendTicketDeferredEmail(account.getEmail(), ticket.getTicketId());
	    }
	}

	
	
	public String raiseTicketInstallation(Connections connection, Tickets ticket) {
		logger.info("Raising installation ticket for user ID: "+ connection.getUserId());
		Accounts account = accountsRepository.findById(connection.getUserId())
		    .orElseThrow(() -> new AccountNotFoundException("User account not found"));
 
        Connections savedConnection = connectionsService.addConnections(connection);
 
        ticket.setUserId(connection.getUserId());
        ticket.setConnectionId(savedConnection.getConnectionId());
        ticket.setConnectionType(connection.getConnectionType());
        ticket.setPincode(account.getPincode());
        ticket.setAddress(account.getAddress());
        ticket.setLatitude(account.getLatitude());
        ticket.setLongitude(account.getLongitude());
        ticket.setServiceType(Constants.INSTALLATION);
 
        Integer assignedEngineerId = getBestEngineer(ticket.getLatitude(), ticket.getLongitude(), ticket.getPincode(), null);
 
        if (assignedEngineerId != null) {
            ticket.setEngineerId(assignedEngineerId);
            ticket.setStatus(Constants.PENDING);
            logger.info("Assigned engineer ID:  to ticket"+ assignedEngineerId);
        } else {
            logger.warn("No available engineers found for pincode: "+ ticket.getPincode());
        }
 
        String priority = assignPriority(ticket.getServiceType(), ticket.getConnectionType());
        if (priority == null) {
            throw new IllegalArgumentException("Invalid service type or connection type for priority assignment.");
        }
        ticket.setPriority(priority);
 
        ticketsRepository.save(ticket);
        
        
        emailService.sendTicketRaisedEmail(account.getEmail(), ticket.getTicketId(),  Constants.INSTALLATION);
        logger.info("Installation ticket raised successfully for user ID: "+ connection.getUserId());
 
        return "Connection Created & Ticket Raised Successfully";
    }
	
	public String raiseTicketFault(Tickets ticket) {
	    List<String> restrictedStatuses = Arrays.asList(
	        Constants.PENDING,
	        Constants.DEFERRED,
	        Constants.IN_PROGRESS
	    );
	
	    
	    boolean ticketExists = ticketsRepository.existsByConnectionIdAndStatusIn(
	        ticket.getConnectionId(), restrictedStatuses);
 
	    if (ticketExists) {
	        throw new TicketAlreadyExistsException("A ticket with this connection is already in progress or pending.");
	    }
 
	    Accounts account = accountsRepository.findById(ticket.getUserId())
	        .orElseThrow(() -> new AccountNotFoundException("User account not found"));
 
	    ticket.setPincode(account.getPincode());
	    ticket.setAddress(account.getAddress());
	    ticket.setLatitude(account.getLatitude());
	    ticket.setLongitude(account.getLongitude());
	    ticket.setServiceType(Constants.FAULT);
 
	    String priority = assignPriority(ticket.getServiceType(), ticket.getConnectionType());
	    if (priority == null) {
	        throw new IllegalArgumentException("Invalid service type or connection type for priority assignment.");
	    }
	    ticket.setPriority(priority);
 
	    Integer assignedEngineerId = getBestEngineer(ticket.getLatitude(), ticket.getLongitude(), ticket.getPincode(), null);
 
	    if (assignedEngineerId == null) {
	        throw new AccountNotFoundException("No available engineers found in pincode: " + ticket.getPincode());
	    }
	    
	    ticket.setEngineerId(assignedEngineerId);
	    ticket.setStatus(Constants.PENDING);
 
	    ticketsRepository.save(ticket);
	    
	    emailService.sendTicketRaisedEmail(account.getEmail(), ticket.getTicketId(),Constants.FAULT );
	    
	    return "Ticket For Fault Raised Successfully";
	}
	  
	public Integer getBestEngineer(double userLat, double userLng, int pincode, Integer excludeEngineerId) {
		
	    Date today = new Date();
	    logger.info("Finding best engineer for pincode: , excluding engineer ID: "+ pincode+ excludeEngineerId);
	    List<Accounts> availableEngineers = accountsRepository.findByRoleAndPincode(Constants.ENGINEER, pincode)
	            .stream()
	            .filter(engineer -> !leaveHistoryService.isEngineerOnLeave(engineer.getId(), today))
	            .filter(engineer -> excludeEngineerId == null || engineer.getId() != excludeEngineerId)
	            .collect(Collectors.toList());
 
	    if (availableEngineers.isEmpty()) {
	    	 logger.warn("No engineers found for pincode: "+ pincode);
	        return null;
	    }
 
	    availableEngineers.sort(Comparator.comparingInt(engineer ->
	        (int) ticketsRepository.countByEngineerIdAndStatus(engineer.getId(), Constants.IN_PROGRESS)
	    ));
 
	    int minWorkload = (int) ticketsRepository.countByEngineerIdAndStatus(availableEngineers.get(0).getId(), Constants.IN_PROGRESS);
 
	    List<Accounts> leastBusyEngineers = availableEngineers.stream()
	    	    .filter(e -> ticketsRepository.countByEngineerIdAndStatus(e.getId(), Constants.IN_PROGRESS) == minWorkload)
	    	    .toList();
	    logger.info("Best engineer selected:  for pincode: "+ leastBusyEngineers+ pincode);
 
 
	    return (leastBusyEngineers.size() == 1)
	            ? leastBusyEngineers.get(0).getId()
	            : getClosestEngineer(leastBusyEngineers, userLat, userLng);
	}
 
	private Integer getClosestEngineer(List<Accounts> engineers, double userLat, double userLng) {
	    return engineers.stream()
	            .min(Comparator.comparingDouble(e -> calculateDistance(userLat, userLng, e.getLatitude(), e.getLongitude())))
	            .map(Accounts::getId)
	            .orElse(null);
	}
 
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
 
    public String assignPriority(String serviceType, String connectionType) {
        if (serviceType == null || connectionType == null) {
            return null;
        }
       
        serviceType = serviceType.toUpperCase();
        connectionType = connectionType.toUpperCase();
        
        if (Constants.FAULT.equals(serviceType)) {
            switch (connectionType) {
                case "WIFI": return "P1";
                case "DTH": return "P2";
                case "LANDLINE": return "P3";
                default: return null;
            }
        } else if (Constants.INSTALLATION.equals(serviceType)) {
            switch (connectionType) {
                case "WIFI": return "P4";
                case "DTH": return "P5";
                case "LANDLINE": return "P6";
                default: return null;
            }
        }
        return null;
    }
    
    public Map<String, Double> getBestEngineerLocation(int ticketId) {
        Tickets ticket = ticketsRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with ID: " + ticketId));
 
        Integer assignedEngineerId = getBestEngineer(ticket.getLatitude(), ticket.getLongitude(),
                                                   ticket.getPincode(), ticket.getEngineerId());
 
        if (assignedEngineerId == null) {
            throw new EngineerNotFoundException("No suitable engineer found for ticket ID: " + ticketId);
        }
 
        Accounts engineer = accountsRepository.findById(assignedEngineerId)
                .orElseThrow(() -> new AccountNotFoundException("Assigned engineer not found with ID: " + assignedEngineerId));
 
        Map<String, Double> locations = new HashMap<>();
        locations.put("userLat", ticket.getLatitude());
        locations.put("userLng", ticket.getLongitude());
        locations.put("engineerLat", engineer.getLatitude());
        locations.put("engineerLng", engineer.getLongitude());
 
        return locations;
    }
	
	public List<Tickets> getDeferredTicketsByPincode(int pincode) {
        return ticketsRepository.findByStatusAndPincode(Constants.DEFERRED, pincode);
    }
	
	public List<Tickets> getRejectedTicketsByPincode(int pincode) {
        return ticketsRepository.findByStatusAndPincode(Constants.REJECTED, pincode);
    }
	
	public String reassignEngineer(int ticketId) {
		logger.info("Reassigning engineer for ticket ID: "+ ticketId);
	    Tickets ticket = ticketsRepository.findById(ticketId)
	        .orElseThrow(() -> new TicketNotFoundException("Ticket not found"));
	    
	    if (!Constants.DEFERRED.equals(ticket.getStatus()) && !Constants.REJECTED.equals(ticket.getStatus())) {
	        throw new InvalidReassignmentException("Ticket is not eligible for reassignment. Status must be DEFERRED or REJECTED");
	    }
 
	    int currentEngineerId = ticket.getEngineerId();
	    Integer bestEngineerId = getBestEngineer(ticket.getLatitude(), ticket.getLongitude(), ticket.getPincode(), ticket.getEngineerId());
 
	    if (bestEngineerId == null) {
	    	logger.error("No suitable alternative engineer found for ticket ID: "+ ticketId);
	        throw new AccountNotFoundException("No suitable alternative engineer found in pincode: " + ticket.getPincode());
	    }
 
	    ticket.setEngineerId(bestEngineerId);
	    ticket.setStatus(Constants.PENDING);
	    ticketsRepository.save(ticket);
	    logger.info("Ticket  reassigned from engineer  to engineer "+ ticketId+ currentEngineerId+ bestEngineerId);
 
	    return String.format("Ticket %d successfully reassigned from engineer %d to engineer %d in pincode %d",
	                        ticketId, currentEngineerId, bestEngineerId, ticket.getPincode());
	}
	
	public List<Tickets> getCompletedOrFailedTicketsByUser(int userId) {
        return ticketsRepository.findByUserIdAndStatusIn(userId,List.of(Constants.COMPLETED, Constants.FAILED) );
    }
	public List<Tickets> getCompletedOrFailedTicketsByEngineer(int engineerId) {
        return ticketsRepository.findByEngineerIdAndStatusIn(engineerId,List.of("COMPLETED", "FAILED") );
    }
	public List<Tickets> getCompletedOrFailedTicketsByAdmin(int pincode) {
        return ticketsRepository.findByPincodeAndStatusIn(pincode, List.of("COMPLETED", "FAILED"));
    }
	
	public Long countByStatusAndPincode(String status, int pincode) {
        return ticketsRepository.countByStatusAndPincode(status, pincode);
    }
	
	public long getTotalTicketCount(int pincode) {
        List<String> statuses = Arrays.asList("Failed", "IN_PROGRESS", "DEFERRED", "REJECTED");
        return ticketsRepository.countByStatusInAndPincode(statuses, pincode);
    }
	
	 public int getPendingTicketCount(Long engineerId) {
	        return ticketsRepository.countByStatusAndEngineerId("PENDING", engineerId);
	    }
	
}