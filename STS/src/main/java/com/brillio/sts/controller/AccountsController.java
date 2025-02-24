package com.brillio.sts.controller;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.brillio.sts.config.JwtService;
import com.brillio.sts.exception.AccountNotFoundException;
import com.brillio.sts.model.Accounts;
import com.brillio.sts.model.AuthRequest;
import com.brillio.sts.model.Connections;
import com.brillio.sts.model.Constants;
import com.brillio.sts.service.AccountDetails;
import com.brillio.sts.service.AccountsService;
import com.fasterxml.jackson.databind.ObjectMapper;

@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")

@RestController
@RequestMapping(value = "/accounts")
public class AccountsController {

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AccountsService accountsService;

    public AccountsController(JwtService jwtService, AuthenticationManager authenticationManager, AccountsService accountsService) {
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.accountsService = accountsService;
    }
	
	
	@GetMapping("/userBypincode/{pincode}")
	public List<Accounts> getUserByPincode(@PathVariable int pincode){
		return accountsService.getUsersByPincode(pincode);
	}
	
	@GetMapping("/engineerBypincode/{pincode}")
	public List<Accounts> getEnginnerByPincode(@PathVariable int pincode){
		return accountsService.getEngineerByPincode(pincode);
	}
	
	/**
	 * This endpoint handles user authentication and generates a JWT token upon successful authentication.
	 * 
	 * Fetches user details based on the provided username.
	 * Validates if the user has the correct role.
	 * Authenticates the user using the provided credentials.
	 * Checks if the user's account is approved before granting access.
	 * Returns a JWT token if authentication is successful.
	 * Throws appropriate exceptions for invalid credentials, unauthorized roles, unapproved accounts, or any other errors.
	 * 
	 * @param authRequest The authentication request containing username, password, and role.
	 * @return A JWT token if authentication is successful.
	 * @throws ResponseStatusException If authentication fails due to invalid credentials, 
	 *         unauthorized role, unapproved account, or any unexpected errors.
	 */
	@PostMapping("/generateToken")
	public String authenticateAndGetToken(@RequestBody AuthRequest authRequest) {
	    try {
	        // Fetch user details
	        AccountDetails userDetails = (AccountDetails) accountsService.loadUserByUsername(authRequest.getUsername());

	        // Validate role
	        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority(authRequest.getRole()))) {
	            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid role provided!");
	        }

	        // Authenticate user credentials
	        Authentication authentication = authenticationManager.authenticate(
	            new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
	        );

	        if (!authentication.isAuthenticated()) {
	            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid user request!");
	        }

	        // Fetch user account details
	        Accounts userAccount = accountsService.searchByEmail(authRequest.getUsername());

	        // Check if account is approved
	        if (!Constants.APPROVED.equalsIgnoreCase(userAccount.getAccountStatus())) {
	            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is not approved yet!");
	        }

	        // Generate and return JWT token
	        return jwtService.generateToken(authRequest.getUsername());

	    } catch (BadCredentialsException e) {
	        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials!", e);
	    } catch (AccountNotFoundException e) {
	        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!", e);
	    } catch (Exception e) {
	        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred. Please try again.", e);
	    }
	}




    @PutMapping("/update/{id}")
    public ResponseEntity<Accounts> updateUser(@PathVariable int id, @RequestBody Accounts updatedUser) {
        Accounts user = accountsService.updateUser(id, updatedUser);
        return ResponseEntity.ok(user);
    }
 

	
	@GetMapping(value = "/showAccounts")
	public List<Accounts> showAccounts(){
		return accountsService.showAccounts();
	}
	
	@GetMapping(value = "/searchAccount/{id}")
	public ResponseEntity<Accounts> getById(@PathVariable int id){
		try {
			Accounts account = accountsService.searchById(id);
			return new ResponseEntity<>(account,HttpStatus.OK);
		} catch (NoSuchElementException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	
	@GetMapping("/nextAccountId")
    public ResponseEntity<Integer> getNextAccountId() {
        return ResponseEntity.ok(accountsService.getNextAccountId());
    }

	@PostMapping("/addAccount")
	public ResponseEntity<String> addAccount(@RequestBody Map<String, Object> payload) {
	    Accounts account = new ObjectMapper().convertValue(payload.get("account"), Accounts.class);
	    Connections connection = payload.containsKey("connection") 
	        ? new ObjectMapper().convertValue(payload.get("connection"), Connections.class) 
	        : null;

	    String result = accountsService.addAccount(account, connection);
	    return ResponseEntity.ok(result);
	}

	
	@GetMapping("/approvalRequests")
	public List<Accounts> getApprovalRequests(@RequestParam String role, @RequestParam String email, @RequestParam String requestType) {
	    return accountsService.getApprovalRequests(role, email, requestType);
	}

	
	
	@GetMapping(value = "/searchAccountByRole/{role}")
	public List<Accounts> getByRole(@PathVariable String role){
		return accountsService.searchByRole(role);
	}
	
	@GetMapping(value = "/showAccountsByPincode/{pincode}")
	public List<Accounts> getByPincode(@PathVariable int pincode){
		return accountsService.showAccountsByPincode(pincode);
	}
	
	@GetMapping("/search/{email}")
    public ResponseEntity<Accounts> getAccountByEmail(@PathVariable String email) {
        try {
            // Fetch account details
            Accounts account = accountsService.searchByEmail(email);
            return new ResponseEntity<>(account, HttpStatus.OK);
        } catch (AccountNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
	
	
	@PutMapping("/updateStatus/{id}")
	public ResponseEntity<Void> updateAccountStatus(@PathVariable int id, @RequestBody Map<String, String> requestBody) {
	    String status = requestBody.get("status");
	    accountsService.updateAccountStatus(id, status);
	    return ResponseEntity.ok().build();
	}

	
	@GetMapping("/securityQuestion/{email}")
	public ResponseEntity<String> getSecurityQuestion(@PathVariable String email) {
	    try {
	        String securityQuestion = accountsService.getSecurityQuestionByEmail(email);
	        return ResponseEntity.ok(securityQuestion);
	    } catch (AccountNotFoundException e) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
	    }
	}
	
	
	 @GetMapping("/superAdmin")
	    public ResponseEntity<Accounts> getSuperAdmin() {
	        Accounts superAdmin = accountsService.getSuperAdmin();
	        if (superAdmin != null) {
	            return ResponseEntity.ok(superAdmin);
	        } else {
	            return ResponseEntity.notFound().build();
	        }
	    }


    // Endpoint to verify the security answer
	 @PostMapping("/verifySecurityAnswer")
	 public boolean verifySecurityAnswer(@RequestBody Map<String, String> body) {
	     String email = body.get("email");
	     String answer = body.get("answer");
	     return accountsService.verifySecurityAnswer(email, answer);
	 }

	 

    // Endpoint to reset the password
	 @PostMapping("/resetPassword")
	 public ResponseEntity<Void> resetPassword(@RequestBody Map<String, String> payload) {
	     String email = payload.get("email");
	     String newPassword = payload.get("newPassword");

	     boolean isPasswordUpdated = accountsService.updatePassword(email, newPassword);
	     
	     if (isPasswordUpdated) {
	         return ResponseEntity.ok().build(); // 200 OK for success
	     } else {
	         return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // 400 BAD REQUEST for failure
	     }
	 }
	 
	 @GetMapping("/pending/engineer/{pincode}")
	    public Long countPendingEngineers(@PathVariable int pincode) {
	        return accountsService.getPendingEngineersCount(pincode);
	    }
	
	 @GetMapping("/pending/user/{pincode}")
	    public Long countPendingUsers(@PathVariable int pincode) {
	        return accountsService.getPendingUsersCount(pincode);
	    }
	 @GetMapping("/pending/total/{pincode}")
	    public Long countPendingRequests(@PathVariable int pincode) {
	        return accountsService.getPendingEngineersAndUsersCount(pincode);
	    }

}
