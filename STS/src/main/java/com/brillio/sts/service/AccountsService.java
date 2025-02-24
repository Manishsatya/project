package com.brillio.sts.service;
 
import java.util.Collections;


import java.util.List;
import java.util.Optional;
 
import org.apache.log4j.Logger;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
 
import com.brillio.sts.exception.AccountNotFoundException;
import com.brillio.sts.exception.SecurityAnswerMismatchException;
import com.brillio.sts.model.Accounts;
import com.brillio.sts.model.Connections;
import com.brillio.sts.model.Constants;
import com.brillio.sts.model.Tickets;
import com.brillio.sts.repo.AccountsRepository;
import com.brillio.sts.repo.ConnectionsRepository;
 
import jakarta.transaction.Transactional;

/**
 * @author Manish.Chapala, Navin.kumar
 */

@Service
@Transactional
public class AccountsService implements UserDetailsService {
 
    private static final Logger logger = Logger.getLogger(AccountsService.class);
 
    private final PasswordEncoder encoder;
    private final ConnectionsRepository connectionsRepository;
    private final AccountsRepository accountsRepository;
    private final ConnectionsService connectionsService;
    private final TicketsService ticketsService;
    private final EmailService emailService;
 
    public AccountsService(PasswordEncoder encoder,
                           ConnectionsRepository connectionsRepository,
                           AccountsRepository accountsRepository,
                           ConnectionsService connectionsService,
                           TicketsService ticketsService,
                           EmailService emailService) {
        this.encoder = encoder;
        this.connectionsRepository = connectionsRepository;
        this.accountsRepository = accountsRepository;
        this.connectionsService = connectionsService;
        this.ticketsService = ticketsService;
        this.emailService = emailService;
    }
 
    public Optional<Accounts> getAccountById(int userId) {
        logger.info("Fetching account with ID: "+ userId);
        return accountsRepository.findById(userId);
    }
 
    public List<Accounts> showAccounts() {
        logger.info("Fetching all accounts from database");
        return accountsRepository.findAll();
    }
 
    public Accounts searchById(int id) {
        logger.info("Searching account by ID: "+ id);
        return accountsRepository.findById(id).orElseThrow(() -> {
            logger.error("Account with ID  not found"+ id);
            return new AccountNotFoundException("Account with the ID " + id + " not found in Records.");
        });
    }
	
	/**
	 * 
	 */	
    public List<Accounts> getUsersByPincode(int pincode) {
        logger.info("Fetching users with pincode: "+ pincode);
        return accountsRepository.findByRoleAndPincodeAndAccountStatus(Constants.USER, pincode, Constants.APPROVED);
    }
    
    
    /**
     * 
     * @param pincode
     * @return
     */
    public List<Accounts> getEngineerByPincode(int pincode) {
        logger.info("Fetching engineers with pincode: "+ pincode);
        return accountsRepository.findByRoleAndPincodeAndAccountStatus(Constants.ENGINEER, pincode, Constants.APPROVED);
    }
	
    
    /**
     * Retrieves the next available account ID by fetching the maximum existing account ID 
     * from the database and incrementing it. If no accounts exist, it starts from 1.
     *
     * @return The next account ID as an Integer.
     */
    public Integer getNextAccountId() {
        Integer maxId = accountsRepository.findMaxAccountId();
        int nextId = (maxId != null) ? maxId + 1 : 1;
        logger.info("Next Account ID: "+ nextId);
        return nextId;
    }
    
    
    /**
     * Registers a new account in the system. 
     * - Sets the account status to "PENDING" by default.
     * - Encrypts the provided password before storing.
     * - If this is the first account, it is automatically set as an "ADMIN" with "APPROVED" status.
     * - If the role is "ADMIN", it requires special handling for approval.
     * - Saves the account and processes additional logic for "ENGINEER" or "USER" roles.
     * - Returns a success message if the account is created or an error message if no admin is available for approval.
     * 
     * @param account The account details to be registered.
     * @param connection The connection details (if applicable) for USER or ENGINEER roles.
     * @return A success or failure message based on account creation conditions.
     */
    public String addAccount(Accounts account, Connections connection) {
        logger.info("Registering new account: "+ account.getEmail());
        account.setAccountStatus(Constants.PENDING);
        account.setPassword(encoder.encode(account.getPassword()));
 
        if (isFirstAccount()) {
            logger.info("First account detected, setting as ADMIN with APPROVED status");
            account.setAccountStatus(Constants.APPROVED);
            account.setRole(Constants.ADMIN);
            accountsRepository.save(account);
            return "First account created successfully as ADMIN with APPROVED status.";
        }
        
        if (Constants.ADMIN.equalsIgnoreCase(account.getRole())) {
	        return handleAdminAccount(account);
	    }
 
        accountsRepository.save(account);
 
        if (Constants.ENGINEER.equalsIgnoreCase(account.getRole()) || Constants.USER.equalsIgnoreCase(account.getRole())) {
            return handleUserOrEngineerAccount(account, connection);
        }
        return "Account creation failed: No eligible APPROVED ADMIN found for approval.";
    }
 
 
    /**
     * Determines if the database is empty, indicating that this is the first account creation.
     *
     * @return true if no accounts exist, otherwise false.
     */
    public boolean isFirstAccount() {
        boolean isFirst = accountsRepository.count() == 0;
        logger.info("Checking if first account: "+ isFirst);
        return isFirst;
    }
    
    
    /**
     * Handles the logic for adding an ADMIN account. This method ensures that:
     * 1. If there is already an approved ADMIN for the given pincode, a new ADMIN cannot be registered.
     * 2. If no approved ADMIN exists for the pincode, the account is saved and a request is sent to the first registered ADMIN.
     * 3. If no ADMIN exists in the system at all, registration fails.
     *
     * @param account The ADMIN account to be registered.
     * @return A message indicating the success or failure of the registration process.
     */
	public String handleAdminAccount(Accounts account) {
	    Accounts firstAdmin = accountsRepository.findFirstByRoleOrderByIdAsc(Constants.ADMIN);
 
	    if (firstAdmin != null) {
	        Accounts existingAdmin = accountsRepository.findFirstByRoleAndPincodeOrderByIdAsc(Constants.ADMIN, account.getPincode());
 
	        if (existingAdmin != null && Constants.APPROVED.equals(existingAdmin.getAccountStatus())) {
	            return "An approved ADMIN already exists for this pincode. Cannot register another.";
	        }
	        accountsRepository.save(account);
	        return "Admin approval request sent to the first registered ADMIN.";
	    }
	    return "Failed to register ADMIN.";
	}
	
 
	/**
	 * Handles the creation of a USER or ENGINEER account by checking for an approved ADMIN 
	 * in the same pincode. If an approved ADMIN exists, an approval request is sent. 
	 * For USER accounts, the associated connection is set to INACTIVE and saved.
	 *
	 * @param account The USER or ENGINEER account to be registered.
	 * @param connection The connection details (only applicable for USER accounts).
	 * @return A message indicating the success or failure of the account creation process.
	 */
	public String handleUserOrEngineerAccount(Accounts account, Connections connection) {
	    Accounts adminWithSamePincode = accountsRepository.findFirstByRoleAndPincodeOrderByIdAsc(Constants.ADMIN, account.getPincode());
 
	    if (adminWithSamePincode != null && Constants.APPROVED.equals(adminWithSamePincode.getAccountStatus())) {
	        if (Constants.USER.equalsIgnoreCase(account.getRole()) && connection != null) {
	            connection.setUserId(account.getId());
	            connection.setStatus(Constants.INACTIVE);
	            connectionsRepository.save(connection);
	        }
	        return "Approval request sent to the ADMIN with the same pincode.";
	    }
	    return "Account creation failed: No eligible APPROVED ADMIN found for approval.";
	}
	
	
	/**
     * Loads user details by email for authentication. If the email exists in the database, 
     * it returns the user details; otherwise, it throws a UsernameNotFoundException.
     *
     * @param username The email of the user.
     * @return UserDetails object containing user information.
     * @throws UsernameNotFoundException if the email is not found.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Loading user by email: "+ username);
        return accountsRepository.findByEmail(username).map(AccountDetails::new)
                .orElseThrow(() -> {
                    logger.error("Email  not found"+ username);
                    return new UsernameNotFoundException("Email not found: " + username);
                });
    }
    
    
 
	 	public Accounts updateUser(int id, Accounts updatedUser) {
	        logger.info("Updating user with ID: "+ id);
	        if (accountsRepository.existsById(id)) {
	            updatedUser.setId(id);
	            return accountsRepository.save(updatedUser);
	        }
	        logger.error("User with ID not found"+ id);
	        return null;
	    }
	 	
 
	    public List<Accounts> searchByRole(String role) {
	        logger.info("Fetching accounts by role: "+ role);
	        return accountsRepository.findByRole(role);
	    }
 
	
	/**
	 * Displays All the accounts in the Database that matches the mentioned Pincode.
	 */	
	    public List<Accounts> showAccountsByPincode(int pincode) {
	        logger.info("Fetching accounts by pincode: "+ pincode);
	        return accountsRepository.getByPincode(pincode);
	    }
	    
	
	/**
	 * Receives the requests of the
	 * @param role
	 * @param email
	 * @return
	 */	
	 public List<Accounts> getApprovalRequests(String role, String email, String requestType) {
		    Accounts currentAdmin = accountsRepository.findByEmail(email)
		    		.orElseThrow(() -> new AccountNotFoundException("Admin not found with email: " + email));
 
 
		    if (Constants.ADMIN.equalsIgnoreCase(role)) {
		        Accounts firstAdmin = accountsRepository.findFirstByRoleOrderByIdAsc(Constants.ADMIN);
 
		        if (firstAdmin != null && firstAdmin.getEmail().equals(email)) {
		            return accountsRepository.findByAccountStatusAndRole(Constants.PENDING, Constants.ADMIN);
		        }
 
		        if (Constants.USER.equalsIgnoreCase(requestType)) {
		            return accountsRepository.findByAccountStatusAndPincodeAndRole(Constants.PENDING, currentAdmin.getPincode(), Constants.USER);
		        } else if (Constants.ENGINEER.equalsIgnoreCase(requestType)) {
		            return accountsRepository.findByAccountStatusAndPincodeAndRole(Constants.PENDING, currentAdmin.getPincode(), Constants.ENGINEER);
		        }
 
		    }
 
		    return Collections.emptyList();
		}
 
	 /**
	  * 
	  * @param email
	  * @return
	  */
	 public Accounts searchByEmail(String email) {
	        logger.info("Searching account by email: "+ email);
	        return accountsRepository.findByEmail(email).orElseThrow(() -> {
	            logger.error("Account with email not found"+ email);
	            return new AccountNotFoundException("Account with the email " + email + " not found in.");
	        });
	    }
	
 
	 public void updateAccountStatus(int id, String status) {
		    logger.info("Updating account status for ID: " + id + " to " + status);
		    
		    Optional<Accounts> accountOpt = accountsRepository.findById(id);
		    if (!accountOpt.isPresent()) {
		        logger.error("Account with ID " + id + " not found");
		        throw new AccountNotFoundException("Account with ID " + id + " cannot be found.");
		    }

		    Accounts account = accountOpt.get();
		    account.setAccountStatus(status); // Set the status based on the parameter
		    accountsRepository.save(account);
		    
		    // Fetch user details
		    String userEmail = account.getEmail();
		    String firstName = account.getFirstName();
		    String role = account.getRole();

		    // If status is APPROVED and role is USER, create a ticket and send approval email
		    if (Constants.APPROVED.equals(status) && Constants.USER.equals(role)) {
		        List<Connections> connections = connectionsService.searchByUserId(id);

		        if (!connections.isEmpty()) { // Ensure the list is not empty
		            Connections connection = connections.get(0); // Fetch the first connection

		            Tickets ticket = new Tickets();
		            ticket.setDescription("My First Connection");

		            // Raise installation ticket
		            ticketsService.raiseTicketInstallation(connection, ticket);
		        }

		        // Send account approval email for USERS
		        emailService.sendAccountApprovalEmail(userEmail, firstName);
		    }

		    // If status is APPROVED and role is ADMIN or ENGINEER, send approval email
		    if (Constants.APPROVED.equals(status) && (Constants.ADMIN.equals(role) || Constants.ENGINEER.equals(role))) {
		        emailService.sendAdminOrEngineerApprovalEmail(userEmail, firstName, role);
		    }

		    // If status is REJECTED and role is USER, delete connection and send rejection email
		    if (Constants.REJECTED.equals(status) && Constants.USER.equals(role)) {
		        List<Connections> connections = connectionsService.searchByUserId(id);

		        if (!connections.isEmpty()) {
		            Connections connection = connections.get(0);
		            connectionsRepository.deleteById(connection.getConnectionId());
		        }

		        // Send account rejection email
		        emailService.sendAccountRejectionEmail(userEmail, firstName);
		    }
		}
	 
	
	
	 /**
	  * Retrieves the security question for a given email if the account exists and is approved.
	  * 
	  * @param email The email address of the account.
	  * @return The security question associated with the account.
	  * @throws AccountNotFoundException if the account does not exist or is not approved.
	  */
	public String getSecurityQuestionByEmail(String email) {
	    Optional<Accounts> accountOpt = accountsRepository.findByEmail(email);
	    
	    if (!accountOpt.isPresent() || !Constants.APPROVED.equals(accountOpt.get().getAccountStatus())) {
	        throw new AccountNotFoundException("Account with address" + email + " not found.");
	    }
 
	    return accountOpt.get().getSecurityQuestion();
	}
 
 
 
	/**
	 * Verifies the security answer for a given email.
	 * 
	 * @param email  The email address of the account.
	 * @param answer The security answer provided by the user.
	 * @return true if the provided answer matches the stored security answer.
	 * @throws AccountNotFoundException if the account does not exist.
	 * @throws SecurityAnswerMismatchException if the provided answer is incorrect.
	 */
	public boolean verifySecurityAnswer(String email, String answer) {
        Optional<Accounts> account = accountsRepository.findByEmail(email);
        if (!account.isPresent()) {
            throw new AccountNotFoundException("Account with email " + email + " is not available.");
        }
        if (!account.get().getSecurityAnswer().equalsIgnoreCase(answer)) {
            throw new SecurityAnswerMismatchException("Security answer is incorrect.");
        }
        return true;
    }
  	
    
    // Update password if security answer is correct
	public boolean updatePassword(String email, String newPassword) {
        logger.info("Updating password for email: "+ email);
        Accounts account = accountsRepository.findByEmail(email).orElseThrow(() -> {
            logger.error("Account with email  not found"+ email);
            return new AccountNotFoundException("Account with email " + email + " not detected.");
        });
        account.setPassword(encoder.encode(newPassword));
        accountsRepository.save(account);
        return true;
    }
	
	public Accounts getSuperAdmin() {
		return accountsRepository.findFirstByRoleOrderByIdAsc(Constants.ADMIN);
	}
	
	public long getPendingEngineersAndUsersCount(int pincode) {
	    return accountsRepository.countByAccountStatusAndRoleInAndPincode(Constants.PENDING, List.of(Constants.ENGINEER, Constants.USER), pincode);
	}
 
	public long getPendingEngineersCount(int pincode) {
	    return accountsRepository.countByAccountStatusAndRoleAndPincode(Constants.PENDING, Constants.ENGINEER, pincode);
	}
 
	public long getPendingUsersCount(int pincode) {
	    return accountsRepository.countByAccountStatusAndRoleAndPincode(Constants.PENDING, Constants.USER, pincode);
	}
   
}