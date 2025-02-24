package com.brillio.sts.service;
 
import java.time.LocalDateTime;
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
    
    /**
     * Retrieves a list of pending tickets for a given pincode.
     * The tickets are filtered by status "IN_PROGRESS" and sorted by priority in ascending order.
     *
     * @param pincode The pincode for which to fetch pending tickets.
     * @return A list of pending tickets sorted by priority.
     *
     *
     */
    public List<Tickets> getPendingTicketsByPincode(int pincode) {
        logger.info("Fetching pending tickets for pincode: " + pincode);
        return ticketsRepository.findByStatusAndPincodeOrderByPriorityAsc(Constants.IN_PROGRESS, pincode);
    }
    
    /**
     * Retrieves tickets for a given user ID with specific statuses.
     *
     * @param userId The ID of the user.
     * @return List of tickets with statuses: PENDING, IN_PROGRESS, or DEFERRED.
     * @author Soniya.Pol
     */
 
    public List<Tickets> getTicketsByUserId(int userId) {
        logger.info("Fetching tickets for user ID: "+ userId);
        return ticketsRepository.findByUserIdAndStatusIn(userId, Arrays.asList(Constants.PENDING, Constants.IN_PROGRESS, Constants.DEFERRED));
    }
    
    /**
     * Retrieves pending tickets assigned to a specific engineer.
     *
     * @param engineerId The ID of the engineer.
     * @return List of pending tickets assigned to the engineer.
     * @author Soniya.Pol
     */
 
    public List<Tickets> getTicketsByEngineerId(int engineerId) {
        logger.info("Fetching pending tickets for engineer ID: "+ engineerId);
        return ticketsRepository.findByEngineerIdAndStatus(engineerId, Constants.PENDING);
    }
    
    /**
     * Retrieves a list of tickets assigned to a specific engineer that are currently in progress.
     *
     * @param engineerId The ID of the engineer whose in-progress tickets need to be fetched.
     * @return A list of tickets where the assigned engineer matches the given engineerId
     *         and the status is "IN_PROGRESS".
     * @author Soniya.Pol
     */
	
	public List<Tickets> getTicketsByEngineerIds(int engineerId){
		return ticketsRepository.findByEngineerIdAndStatus(engineerId, Constants.IN_PROGRESS);
	}
	
	/**
	 * Updates the status of a ticket and performs additional actions if the ticket is completed.
	 *
	 * @param ticketId   The ID of the ticket to be updated.
	 * @param engineerId The ID of the engineer attempting to update the ticket.
	 * @param status     The new status to be assigned to the ticket.
	 * @throws TicketNotFoundException       If the ticket with the given ID does not exist.
	 * @throws UnauthorizedEngineerException If the engineer is not authorized to update the ticket.
	 * @throws AccountNotFoundException      If the account associated with the ticket is not found.
	 * @throws ConnectionNotFoundException   If the connection associated with the ticket is not found.
	 *
	 * @author Vuppala.Geethika
	 */
	
	public void updateTicketStatus(int ticketId, int engineerId, String status) {
		logger.info("Updating status of ticket ID:  to  by engineer ID: "+ ticketId+ status+ engineerId);
		Tickets ticket = ticketsRepository.findById(ticketId)
		    .orElseThrow(() -> new TicketNotFoundException("Ticket not found"));
		
		if (ticket.getEngineerId() != engineerId) {
			logger.error("Unauthorized engineer ID:  tried updating ticket ID: "+ engineerId+ ticketId);
		    throw new UnauthorizedEngineerException("Engineer not authorized for this ticket");
		}
 
        ticket.setStatus(status);
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketsRepository.save(ticket);
        logger.info("Ticket ID:  updated successfully to status: "+ ticketId+ status);
        Accounts account = accountsRepository.findById(ticket.getUserId())
    		    .orElseThrow(() -> new AccountNotFoundException("Account account not found"));
 
       
           
            if (Constants.COMPLETED.equals(status)) {
            	int connectionId = ticket.getConnectionId();
            	Connections connection = connectionsRepository.findById(connectionId)
            	    .orElseThrow(() -> new ConnectionNotFoundException("Connection not found for ID: " + connectionId));
                
                connection.setStatus("ACTIVE");
                connection.setStartDate(LocalDateTime.now());
                connection.setExpiryDate(connectionsService.setExpiryDate(connection));
                connectionsRepository.save(connection);
                emailService.sendTicketResolvedEmail(account.getEmail(), ticket.getTicketId());
            }
        
    }
	 /**
     * Raises an installation ticket for a new connection.
     *
     * @param connection The connection details for installation.
     * @param ticket The ticket to be created.
     * @return Success message after raising the ticket.
     * @throws AccountNotFoundException if the user account is not found.
     * @throws IllegalArgumentException if priority assignment fails due to invalid inputs.
     *
     * @author Soniya.Pol
     */
 
	
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
	 /**
     * Raises a fault ticket for an existing connection.
     *
     * @param ticket The ticket details to be created.
     * @return Success message after raising the ticket.
     * @throws TicketAlreadyExistsException if a ticket for the same connection is already in progress or pending.
     * @throws AccountNotFoundException if the user account or an available engineer is not found.
     * @throws IllegalArgumentException if priority assignment fails due to invalid inputs.
     *
     * @author Soniya.Pol
     */
 
	
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
	  
	
    
    
    /**
     * Determines the priority level based on the service type and connection type.
     *
     * @param serviceType    The type of service (e.g., "FAULT", "INSTALLATION").
     * @param connectionType The type of connection (e.g., "WIFI", "DTH", "LANDLINE").
     * @return The assigned priority as a string (e.g., "P1", "P2", etc.), or null if inputs are invalid.
     * @author Vuppala.Geethika
     */
 
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
    
    
    /**
     * Retrieves a list of deferred tickets for a given pincode.
     * This method fetches all tickets that have been marked as "DEFERRED"
     * within a specific geographical area identified by the pincode.
     *
     * @param pincode The pincode for which deferred tickets need to be fetched.
     * @return A list of tickets that have the status "DEFERRED" in the specified pincode.
     *
     *
     */
	public List<Tickets> getDeferredTicketsByPincode(int pincode) {
        return ticketsRepository.findByStatusAndPincode(Constants.DEFERRED, pincode);
    }
	
	
	/**
	 * Retrieves a list of rejected tickets for a given pincode.
	 * This method fetches all tickets that have been marked as "REJECTED"
	 * within a specific geographical area identified by the pincode.
	 *
	 * @param pincode The pincode for which rejected tickets need to be fetched.
	 * @return A list of tickets that have the status "REJECTED" in the specified pincode.
	 *
	 *
	 */
	public List<Tickets> getRejectedTicketsByPincode(int pincode) {
        return ticketsRepository.findByStatusAndPincode(Constants.REJECTED, pincode);
    }
	/**
 
	* Selects the best available engineer based on workload and proximity.
 
	* Prioritizes engineers with the least "IN_PROGRESS" tickets.
 
	* If multiple engineers have the same workload, chooses the closest one.
 
	* Excludes engineers who are on leave or a specific engineer if needed.
 
	*
 
	* @author [Mounika]
 
	* @param userLat          Latitude of the user (ticket location).
 
	* @param userLng          Longitude of the user (ticket location).
 
	* @param pincode          Pincode to filter engineers.
 
	* @param excludeEngineerId Engineer ID to exclude (optional).
 
	* @return The ID of the most suitable engineer or null if none are available.
 
	*/
 
	public Integer getBestEngineer(double userLat, double userLng, int pincode, Integer excludeEngineerId) {
 
	    Date today = new Date();
	
	    List<Accounts> availableEngineers = accountsRepository.findByRoleAndPincode("ENGINEER", pincode)
 
	        .stream()
 
	        .filter(engineer -> !leaveHistoryService.isEngineerOnLeave(engineer.getId(), today))
 
	        .filter(engineer -> excludeEngineerId == null || engineer.getId() != excludeEngineerId)
 
	        .collect(Collectors.toList());
	
	    if (availableEngineers.isEmpty()) {
 
	        return null;
 
	    }
	
	    availableEngineers.sort(Comparator.comparingInt(engineer ->
 
	        (int) ticketsRepository.countByEngineerIdAndStatus(engineer.getId(), "IN_PROGRESS")
 
	    ));
	
	    int minWorkload = (int) ticketsRepository.countByEngineerIdAndStatus(availableEngineers.get(0).getId(), "IN_PROGRESS");
	
	    List<Accounts> leastBusyEngineers = availableEngineers.stream()
 
	        .filter(e -> ticketsRepository.countByEngineerIdAndStatus(e.getId(), "IN_PROGRESS") == minWorkload)
 
	        .toList();
	
	    return (leastBusyEngineers.size() == 1)
 
	        ? leastBusyEngineers.get(0).getId()
 
	        : getClosestEngineer(leastBusyEngineers, userLat, userLng);
 
	}
	
	/**
 
	* Finds the closest engineer from a given list based on geographical distance.
 
	*
 
	* @author [Mounika]
 
	* @param engineers List of engineers with the least workload.
 
	* @param userLat   Latitude of the user.
 
	* @param userLng   Longitude of the user.
 
	* @return The ID of the closest engineer or null if the list is empty.
 
	*/
 
	private Integer getClosestEngineer(List<Accounts> engineers, double userLat, double userLng) {
 
	    return engineers.stream()
 
	        .min(Comparator.comparingDouble(e -> calculateDistance(userLat, userLng, e.getLatitude(), e.getLongitude())))
 
	        .map(Accounts::getId)
 
	        .orElse(null);
 
	}
	
	/**
 
	* Calculates the geographical distance (in km) between two latitude-longitude points
 
	* using the Haversine formula.
 
	*
 
	* @author [Mounika]
 
	* @param lat1 Latitude of the first location.
 
	* @param lon1 Longitude of the first location.
 
	* @param lat2 Latitude of the second location.
 
	* @param lon2 Longitude of the second location.
 
	* @return The distance in kilometers between the two locations.
 
	*/
 
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
	
	/**
 
	* Retrieves the location details (latitude & longitude) of both the user and the assigned engineer
 
	* for a given ticket.
 
	*
 
	* @author [Mounika]
 
	* @param ticketId The ID of the ticket.
 
	* @return A map containing user and engineer latitude and longitude.
 
	* @throws TicketNotFoundException   If the ticket ID is not found.
 
	* @throws EngineerNotFoundException If no engineer is assigned to the ticket.
 
	*/
 
	public Map<String, Double> getBestEngineerLocation(int ticketId) {
 
	    Tickets ticket = ticketsRepository.findById(ticketId)
 
	        .orElseThrow(() -> new TicketNotFoundException("Ticket not found with ID: " + ticketId));
	
	    if (ticket.getEngineerId() == 0) {
 
	        throw new EngineerNotFoundException("No suitable engineer found for ticket ID: " + ticket.getTicketId());
 
	    }
	
	    Accounts engineer = accountsRepository.findById(ticket.getEngineerId())
 
	        .orElseThrow(() -> new AccountNotFoundException("Assigned engineer not found with ID: " + ticket.getEngineerId()));
	
	    Map<String, Double> locations = new HashMap<>();
 
	    locations.put("userLat", ticket.getLatitude());
 
	    locations.put("userLng", ticket.getLongitude());
 
	    locations.put("engineerLat", engineer.getLatitude());
 
	    locations.put("engineerLng", engineer.getLongitude());
 
	    return locations;
 
	}
 
	
	
	
	
	/**
	 * Reassigns an engineer to a ticket if the ticket is in a DEFERRED or REJECTED state.
	 *
	 * @param ticketId The ID of the ticket to be reassigned.
	 * @return A message confirming the reassignment of the ticket.
	 * @throws TicketNotFoundException if the ticket with the given ID is not found.
	 * @throws InvalidReassignmentException if the ticket is not in DEFERRED or REJECTED status.
	 * @throws AccountNotFoundException if no suitable alternative engineer is found in the same pincode.
	 *
	 * @author Vuppala.Geethika
	 */
	
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
        return ticketsRepository.findByUserIdAndStatusIn(userId,List.of("COMPLETED", "FAILED") );
    }
	
	/**
	 * Retrieves a list of tickets assigned to a specific engineer that have either
	 * been completed or marked as failed.
	 *
	 * @param engineerId The ID of the engineer whose tickets need to be fetched.
	 * @return A list of tickets that are either in "COMPLETED" or "FAILED" status.
	 *
	 *@author Soniya.Pol
	 */
	public List<Tickets> getCompletedOrFailedTicketsByEngineer(int engineerId) {
        return ticketsRepository.findByEngineerIdAndStatusIn(engineerId,List.of("COMPLETED", "FAILED") );
    }
	
	
	/**
	 * Retrieves a list of tickets for a specific pincode that have either been
	 * completed or marked as failed. This method is intended for admin users
	 * to monitor ticket resolutions within a given area.
	 *
	 * @param pincode The pincode for which completed or failed tickets need to be fetched.
	 * @return A list of tickets in the specified pincode that are either in "COMPLETED" or "FAILED" status.
	 */
	public List<Tickets> getCompletedOrFailedTicketsByAdmin(int pincode) {
        return ticketsRepository.findByPincodeAndStatusIn(pincode, List.of("COMPLETED", "FAILED"));
    }
	
	
	/**
	 * Counts the number of tickets with a specific status in a given pincode.
	 * This method helps in tracking the workload or issue resolution progress
	 * within a particular geographic area.
	 * @param status  The status of the tickets to be counted (e.g., "PENDING", "IN_PROGRESS", "COMPLETED").
	 * @param pincode The pincode for which the ticket count needs to be retrieved.
	 * @return The total number of tickets matching the given status and pincode.
	 *
	 */
	public Long countByStatusAndPincode(String status, int pincode) {
        return ticketsRepository.countByStatusAndPincode(status, pincode);
    }
	
	/**
	 * Retrieves the total count of tickets in a given pincode that have specific statuses.
	 * This method helps in monitoring the number of ongoing or unresolved tickets
	 * within a particular geographic area.
	 *
	 * @param pincode The pincode for which the ticket count needs to be retrieved.
	 * @return The total number of tickets with statuses such as "Failed", "IN_PROGRESS", "DEFERRED", and "REJECTED".
	 */
	public long getTotalTicketCount(int pincode) {
        List<String> statuses = Arrays.asList("Failed", "IN_PROGRESS", "DEFERRED", "REJECTED");
        return ticketsRepository.countByStatusInAndPincode(statuses, pincode);
    }
	
	/**
	 * Retrieves the count of pending tickets assigned to a specific engineer.
	 * This method helps in tracking the workload of an engineer by counting
	 * the number of tickets that are yet to be addressed.
	 *
	 * @param engineerId The ID of the engineer whose pending tickets need to be counted.
	 * @return The number of tickets with status "PENDING" assigned to the given engineer.
	 */
	 public int getPendingTicketCount(Long engineerId) {
	        return ticketsRepository.countByStatusAndEngineerId("PENDING", engineerId);
	    }
}