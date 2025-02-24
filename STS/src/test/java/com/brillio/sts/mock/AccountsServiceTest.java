package com.brillio.sts.mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.brillio.sts.exception.AccountNotFoundException;
import com.brillio.sts.exception.SecurityAnswerMismatchException;
import com.brillio.sts.model.Accounts;
import com.brillio.sts.model.Connections;
import com.brillio.sts.model.Constants;
import com.brillio.sts.model.Tickets;
import com.brillio.sts.repo.AccountsRepository;
import com.brillio.sts.repo.ConnectionsRepository;
import com.brillio.sts.service.AccountDetails;
import com.brillio.sts.service.AccountsService;
import com.brillio.sts.service.ConnectionsService;
import com.brillio.sts.service.EmailService;
import com.brillio.sts.service.TicketsService;

@ExtendWith(MockitoExtension.class)
class AccountsServiceTest {

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private AccountsRepository accountsRepository;

    @Mock
    private ConnectionsRepository connectionsRepository;

    @Mock
    private ConnectionsService connectionsService;

    @Mock
    private TicketsService ticketsService;

    @Mock
    private EmailService emailService;
    
    @Mock
    private AccountsService accountsServiceMock;

    @InjectMocks
    @Spy
    private AccountsService accountsService;

    private Accounts account;
    private Connections connection;

    @BeforeEach
    void setUp() {
        account = new Accounts();
        account.setId(1);
        account.setEmail("test@example.com");
        account.setPassword("password");
        account.setRole(Constants.USER);
        account.setAccountStatus(Constants.PENDING);

        connection = new Connections();
        connection.setUserId(1);
        connection.setStatus(Constants.INACTIVE);
    }
    
    
    	
    @Test
    void testGetAccountById_Found() {
        when(accountsRepository.findById(1)).thenReturn(Optional.of(account));
        Optional<Accounts> result = accountsService.getAccountById(1);
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
    }

    @Test
    void testGetAccountById_NotFound() {
        when(accountsRepository.findById(1)).thenReturn(Optional.empty());
        Optional<Accounts> result = accountsService.getAccountById(1);
        assertFalse(result.isPresent());
    }
    
    @Test
    void testShowAccounts_WithData() {
        List<Accounts> accountsList = List.of(account, new Accounts()); // Mocking two accounts
        
        when(accountsRepository.findAll()).thenReturn(accountsList);
        
        List<Accounts> result = accountsService.showAccounts();
        
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testShowAccounts_EmptyList() {
        when(accountsRepository.findAll()).thenReturn(List.of()); // Mocking empty list
        
        List<Accounts> result = accountsService.showAccounts();
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testSearchById_Found() {
        when(accountsRepository.findById(1)).thenReturn(Optional.of(account));

        Accounts result = accountsService.searchById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    void testSearchById_NotFound() {
        when(accountsRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountsService.searchById(1));
    }
    
    
    @Test
    void testGetUsersByPincode_Found() {
        List<Accounts> accountsList = List.of(account);
        when(accountsRepository.findByRoleAndPincodeAndAccountStatus(Constants.USER, 123456, Constants.APPROVED))
                .thenReturn(accountsList);

        List<Accounts> result = accountsService.getUsersByPincode(123456);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(Constants.USER, result.get(0).getRole());
    }

    @Test
    void testGetUsersByPincode_NotFound() {
        when(accountsRepository.findByRoleAndPincodeAndAccountStatus(Constants.USER, 123456, Constants.APPROVED))
                .thenReturn(List.of());

        List<Accounts> result = accountsService.getUsersByPincode(123456);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testGetEngineerByPincode_EngineersFound() {
        Accounts engineer1 = new Accounts(1, "John", "Doe", "john.doe@example.com", "password",
                Constants.ENGINEER, "1234567890", "Address 1", 123456, "What is your pet's name?", 
                "Fluffy", Constants.APPROVED, "Male", 12.34, 56.78);

        Accounts engineer2 = new Accounts(2, "Jane", "Smith", "jane.smith@example.com", "password",
                Constants.ENGINEER, "9876543210", "Address 2", 123456, "What is your mother's maiden name?", 
                "Smith", Constants.APPROVED, "Female", 98.76, 54.32);
        when(accountsRepository.findByRoleAndPincodeAndAccountStatus(Constants.ENGINEER, 123456, Constants.APPROVED))
                .thenReturn(Arrays.asList(engineer1, engineer2));

        List<Accounts> result = accountsService.getEngineerByPincode(123456);

        assertEquals(2, result.size());
        assertEquals("John", result.get(0).getFirstName());
        assertEquals("Jane", result.get(1).getFirstName());

        verify(accountsRepository, times(1))
                .findByRoleAndPincodeAndAccountStatus(Constants.ENGINEER, 123456, Constants.APPROVED);
    }

    @Test
    void testGetEngineerByPincode_NoEngineersFound() {
        // Mock repository to return an empty list
        when(accountsRepository.findByRoleAndPincodeAndAccountStatus(Constants.ENGINEER, 999999, Constants.APPROVED))
                .thenReturn(Collections.emptyList());

        List<Accounts> result = accountsService.getEngineerByPincode(999999);

        assertTrue(result.isEmpty());
        verify(accountsRepository, times(1))
                .findByRoleAndPincodeAndAccountStatus(Constants.ENGINEER, 999999, Constants.APPROVED);
    }
    
    @Test
    void testGetNextAccountId_WithExistingAccounts() {
        when(accountsRepository.findMaxAccountId()).thenReturn(10);

        Integer nextId = accountsService.getNextAccountId();

        assertNotNull(nextId);
        assertEquals(11, nextId);
    }

    @Test
    void testGetNextAccountId_NoExistingAccounts() {
        when(accountsRepository.findMaxAccountId()).thenReturn(null);

        Integer nextId = accountsService.getNextAccountId();

        assertNotNull(nextId);
        assertEquals(1, nextId);
    }
    
    @Test
    void testAddAccount_FirstAdmin() {
        when(accountsRepository.count()).thenReturn(0L);
        when(accountsRepository.save(any(Accounts.class))).thenReturn(account);
        when(encoder.encode(anyString())).thenReturn("encodedPassword");

        String result = accountsService.addAccount(account, connection);

        assertEquals("First account created successfully as ADMIN with APPROVED status.", result);
        verify(accountsRepository, times(1)).save(account);
    }

    @Test
    void testAddAccount_RegularAdminApproval() {
        account.setRole(Constants.ADMIN);
        when(accountsRepository.count()).thenReturn(1L);
        when(accountsService.handleAdminAccount(any(Accounts.class)))
                .thenReturn("Admin approval request sent to the first registered ADMIN.");

        String result = accountsService.addAccount(account, connection);

        assertEquals("Admin approval request sent to the first registered ADMIN.", result);
        verify(accountsRepository, never()).save(account);
    }

    @Test
    void testAddAccount_AdminAlreadyExists() {
        account.setRole(Constants.ADMIN);
        Accounts existingAdmin = new Accounts();
        existingAdmin.setRole(Constants.ADMIN);
        existingAdmin.setAccountStatus(Constants.APPROVED);

        when(accountsRepository.count()).thenReturn(1L);
        when(accountsRepository.findFirstByRoleOrderByIdAsc(Constants.ADMIN)).thenReturn(existingAdmin);
        when(accountsRepository.findFirstByRoleAndPincodeOrderByIdAsc(Constants.ADMIN, account.getPincode()))
                .thenReturn(existingAdmin);

        String result = accountsService.addAccount(account, connection);

        assertEquals("An approved ADMIN already exists for this pincode. Cannot register another.", result);
        verify(accountsRepository, never()).save(account);
    }

    @Test
    void testAddAccount_EngineerOrUser_Success() {
        when(accountsRepository.count()).thenReturn(1L);
        when(accountsRepository.save(any(Accounts.class))).thenReturn(account);
        when(accountsService.handleUserOrEngineerAccount(account, connection))
                .thenReturn("Approval request sent to the ADMIN with the same pincode.");

        account.setRole(Constants.ENGINEER);  // Testing for ENGINEER role

        String result = accountsService.addAccount(account, connection);

        assertEquals("Approval request sent to the ADMIN with the same pincode.", result);
        verify(accountsRepository, times(1)).save(account);
    }

    @Test
    void testAddAccount_NoEligibleAdminFound() {
        Accounts invalidAccount = new Accounts(1, "Invalid", "User", "invalid@example.com", "password",
                "UNKNOWN_ROLE", "9876543210", "Random Address", 999999, "What is your color?",
                "Blue", Constants.PENDING, "Male", 11.11, 22.22);

        when(accountsRepository.count()).thenReturn(2L); // Not the first account

        String result = accountsService.addAccount(invalidAccount, null);

        assertEquals("Account creation failed: No eligible APPROVED ADMIN found for approval.", result);
    }


    
    @Test
    void testHandleAdminAccount_AdminApprovalRequestSent() {
        Accounts unapprovedAdmin = new Accounts();
        unapprovedAdmin.setAccountStatus(Constants.PENDING); // Admin exists but is not approved

        when(accountsRepository.findFirstByRoleOrderByIdAsc(Constants.ADMIN)).thenReturn(new Accounts());
        when(accountsRepository.findFirstByRoleAndPincodeOrderByIdAsc(Constants.ADMIN, account.getPincode()))
                .thenReturn(unapprovedAdmin);
        when(accountsRepository.save(any(Accounts.class))).thenReturn(account);

        String result = accountsService.handleAdminAccount(account);

        assertEquals("Admin approval request sent to the first registered ADMIN.", result);
        verify(accountsRepository, times(1)).save(account); // Ensure account is saved
    }

    @Test
    void testHandleAdminAccount_AdminAlreadyApproved() {
        Accounts existingAdmin = new Accounts();
        existingAdmin.setRole(Constants.ADMIN);
        existingAdmin.setAccountStatus(Constants.APPROVED);
        existingAdmin.setPincode(12345);

        account.setRole(Constants.ADMIN);
        account.setPincode(12345);

        when(accountsRepository.findFirstByRoleOrderByIdAsc(Constants.ADMIN)).thenReturn(existingAdmin);
        when(accountsRepository.findFirstByRoleAndPincodeOrderByIdAsc(Constants.ADMIN, 12345)).thenReturn(existingAdmin);

        String result = accountsService.handleAdminAccount(account);

        System.out.println("Actual Result: " + result); // Debugging

        assertEquals("An approved ADMIN already exists for this pincode. Cannot register another.", result);
        verify(accountsRepository, never()).save(any(Accounts.class)); // Ensure no save happens
    }


    @Test
    void testHandleAdminAccount_AdminExistsButNotApproved() {
        Accounts firstAdmin = new Accounts();
        firstAdmin.setRole(Constants.ADMIN);
        firstAdmin.setAccountStatus(Constants.PENDING);
        firstAdmin.setPincode(12345);

        account.setPincode(12345); // Ensure pincode matches

        when(accountsRepository.findFirstByRoleOrderByIdAsc(Constants.ADMIN)).thenReturn(firstAdmin);
        when(accountsRepository.findFirstByRoleAndPincodeOrderByIdAsc(Constants.ADMIN, 12345)).thenReturn(null);

        String result = accountsService.handleAdminAccount(account);

        System.out.println("Actual Result: " + result); // Debugging

        assertEquals("Admin approval request sent to the first registered ADMIN.", result);
        verify(accountsRepository, times(1)).save(account); // Ensure save is called
    }

    
    @Test
    void testHandleAdminAccount_ApprovedAdminExistsForPincode() {
        Accounts firstAdmin = new Accounts(); // First registered admin
        firstAdmin.setRole(Constants.ADMIN);
        firstAdmin.setAccountStatus(Constants.PENDING);
        firstAdmin.setPincode(12345);

        Accounts existingApprovedAdmin = new Accounts(); // Already approved admin for the same pincode
        existingApprovedAdmin.setRole(Constants.ADMIN);
        existingApprovedAdmin.setAccountStatus(Constants.APPROVED);
        existingApprovedAdmin.setPincode(12345);

        account.setPincode(12345); // ✅ Ensure `account` matches the expected pincode

        when(accountsRepository.findFirstByRoleOrderByIdAsc(Constants.ADMIN)).thenReturn(firstAdmin);
        when(accountsRepository.findFirstByRoleAndPincodeOrderByIdAsc(Constants.ADMIN, 12345))
            .thenReturn(existingApprovedAdmin);

        String result = accountsService.handleAdminAccount(account);

        System.out.println("Actual Result: " + result); // Debugging

        assertEquals("An approved ADMIN already exists for this pincode. Cannot register another.", result);
        verify(accountsRepository, never()).save(any(Accounts.class)); // Ensure no save occurs
    }



    @Test
    void testHandleAdminAccount_NoAdminExists() {
        when(accountsRepository.findFirstByRoleOrderByIdAsc(Constants.ADMIN)).thenReturn(null);

        String result = accountsService.handleAdminAccount(account);

        assertEquals("Failed to register ADMIN.", result);
        verify(accountsRepository, never()).save(any(Accounts.class));
    }
    
    @Test
    void testHandleUserAccountWithApprovedAdmin() {
        Accounts adminAccount = new Accounts();
        adminAccount.setAccountStatus(Constants.APPROVED);
        adminAccount.setRole(Constants.ADMIN);

        when(accountsRepository.findFirstByRoleAndPincodeOrderByIdAsc(Constants.ADMIN, account.getPincode()))
                .thenReturn(adminAccount);

        account.setRole(Constants.USER);
        connection.setUserId(0);

        String result = accountsService.handleUserOrEngineerAccount(account, connection);

        assertEquals("Approval request sent to the ADMIN with the same pincode.", result);
        assertEquals(account.getId(), connection.getUserId());
        assertEquals(Constants.INACTIVE, connection.getStatus());
        verify(connectionsRepository, times(1)).save(connection);
    }

    @Test
    void testHandleEngineerAccountWithApprovedAdmin() {
        Accounts adminAccount = new Accounts();
        adminAccount.setAccountStatus(Constants.APPROVED);
        adminAccount.setRole(Constants.ADMIN);

        when(accountsRepository.findFirstByRoleAndPincodeOrderByIdAsc(Constants.ADMIN, account.getPincode()))
                .thenReturn(adminAccount);

        account.setRole(Constants.ENGINEER);

        String result = accountsService.handleUserOrEngineerAccount(account, connection);

        assertEquals("Approval request sent to the ADMIN with the same pincode.", result);
        verify(connectionsRepository, never()).save(any(Connections.class));
    }

    @Test
    void testHandleUserAccountWithoutConnection() {
        Accounts adminAccount = new Accounts();
        adminAccount.setAccountStatus(Constants.APPROVED);
        adminAccount.setRole(Constants.ADMIN);

        when(accountsRepository.findFirstByRoleAndPincodeOrderByIdAsc(Constants.ADMIN, account.getPincode()))
                .thenReturn(adminAccount);

        account.setRole(Constants.USER);

        String result = accountsService.handleUserOrEngineerAccount(account, null);

        assertEquals("Approval request sent to the ADMIN with the same pincode.", result);
        verify(connectionsRepository, never()).save(any(Connections.class));
    }

    @Test
    void testHandleAccountWithNoApprovedAdmin() {
        when(accountsRepository.findFirstByRoleAndPincodeOrderByIdAsc(Constants.ADMIN, account.getPincode()))
                .thenReturn(null);

        account.setRole(Constants.USER);

        String result = accountsService.handleUserOrEngineerAccount(account, connection);

        assertEquals("Account creation failed: No eligible APPROVED ADMIN found for approval.", result);
        verify(connectionsRepository, never()).save(any(Connections.class));
    }
    
    
    
    @Test
    void testUpdateUser_Success() {
        int userId = 1;
        Accounts existingUser = new Accounts();
        existingUser.setId(userId);
        existingUser.setFirstName("oldUsername");

        Accounts updatedUser = new Accounts();
        updatedUser.setFirstName("newUsername");

        when(accountsRepository.existsById(userId)).thenReturn(true);
        when(accountsRepository.save(any(Accounts.class))).thenReturn(updatedUser);

        Accounts result = accountsService.updateUser(userId, updatedUser);

        assertNotNull(result);
        assertEquals("newUsername", result.getFirstName());
        assertEquals(userId, result.getId());
        verify(accountsRepository, times(1)).save(updatedUser);
    }
    
    @Test
    void testUpdateUser_UserDoesNotExist() {
        int userId = 2;
        Accounts updatedUser = new Accounts();
        updatedUser.setFirstName("newUsername");

        when(accountsRepository.existsById(userId)).thenReturn(false);

        Accounts result = accountsService.updateUser(userId, updatedUser);

        assertNull(result);
        verify(accountsRepository, never()).save(any(Accounts.class)); // Ensure no save occurs
    }
    
    @Test
    void testSearchByRole_Found() {
        String role = "ADMIN";

        List<Accounts> mockAccounts = Arrays.asList(
            new Accounts(1, "John", "Doe", "john@example.com", "pass123", role, "1234567890", "123 St, City", 12345, "Pet?", "Dog", "ACTIVE", "Male", 12.34, 56.78),
            new Accounts(2, "Jane", "Smith", "jane@example.com", "pass456", role, "0987654321", "456 St, City", 54321, "School?", "XYZ", "ACTIVE", "Female", 98.76, 54.32)
        );

        when(accountsRepository.findByRole(role)).thenReturn(mockAccounts);

        List<Accounts> result = accountsService.searchByRole(role);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(role, result.get(0).getRole());
        assertEquals(role, result.get(1).getRole());
        verify(accountsRepository, times(1)).findByRole(role);
    }

    @Test
    void testSearchByRole_NotFound() {
        String role = "MANAGER";

        when(accountsRepository.findByRole(role)).thenReturn(Collections.emptyList());

        List<Accounts> result = accountsService.searchByRole(role);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(accountsRepository, times(1)).findByRole(role);
    }
    
    @Test
    void testGetApprovalRequests_AdminNotFound() {
        String email = "admin@example.com";
        when(accountsRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountsService.getApprovalRequests(Constants.ADMIN, email, Constants.USER));

        verify(accountsRepository, times(1)).findByEmail(email);
    }

    @Test
    void testGetApprovalRequests_FirstAdminCheckingPendingAdmins() {
        String email = "firstadmin@example.com";

        Accounts firstAdmin = new Accounts();
        firstAdmin.setEmail(email);
        firstAdmin.setRole(Constants.ADMIN);

        Accounts pendingAdmin1 = new Accounts(1, "John", "Doe", "pending1@example.com", "pass", Constants.ADMIN, "1234567890", "Address", 12345, "Q", "A", Constants.PENDING, "M", 0.0, 0.0);
        Accounts pendingAdmin2 = new Accounts(2, "Jane", "Doe", "pending2@example.com", "pass", Constants.ADMIN, "9876543210", "Address", 12345, "Q", "A", Constants.PENDING, "F", 0.0, 0.0);

        when(accountsRepository.findByEmail(email)).thenReturn(Optional.of(firstAdmin));
        when(accountsRepository.findFirstByRoleOrderByIdAsc(Constants.ADMIN)).thenReturn(firstAdmin);
        when(accountsRepository.findByAccountStatusAndRole(Constants.PENDING, Constants.ADMIN)).thenReturn(Arrays.asList(pendingAdmin1, pendingAdmin2));

        List<Accounts> result = accountsService.getApprovalRequests(Constants.ADMIN, email, null);

        assertEquals(2, result.size());
        verify(accountsRepository, times(1)).findByAccountStatusAndRole(Constants.PENDING, Constants.ADMIN);
    }

    @Test
    void testGetApprovalRequests_NonFirstAdminCheckingAdmins() {
        String email = "nonfirstadmin@example.com";

        Accounts nonFirstAdmin = new Accounts();
        nonFirstAdmin.setEmail(email);
        nonFirstAdmin.setRole(Constants.ADMIN);

        Accounts firstAdmin = new Accounts();
        firstAdmin.setEmail("firstadmin@example.com");
        firstAdmin.setRole(Constants.ADMIN);

        when(accountsRepository.findByEmail(email)).thenReturn(Optional.of(nonFirstAdmin));
        when(accountsRepository.findFirstByRoleOrderByIdAsc(Constants.ADMIN)).thenReturn(firstAdmin);

        List<Accounts> result = accountsService.getApprovalRequests(Constants.ADMIN, email, null);

        assertTrue(result.isEmpty());
        verify(accountsRepository, never()).findByAccountStatusAndRole(anyString(), anyString());
    }

    @Test
    void testGetApprovalRequests_AdminCheckingPendingUsers() {
        String email = "admin@example.com";
        String requestType = Constants.USER;

        Accounts admin = new Accounts();
        admin.setEmail(email);
        admin.setRole(Constants.ADMIN);
        admin.setPincode(12345);

        Accounts pendingUser = new Accounts(3, "User", "One", "user@example.com", "pass", Constants.USER, "1112223333", "Address", 12345, "Q", "A", Constants.PENDING, "M", 0.0, 0.0);

        when(accountsRepository.findByEmail(email)).thenReturn(Optional.of(admin));
        when(accountsRepository.findFirstByRoleOrderByIdAsc(Constants.ADMIN)).thenReturn(null);
        when(accountsRepository.findByAccountStatusAndPincodeAndRole(Constants.PENDING, 12345, Constants.USER)).thenReturn(Arrays.asList(pendingUser));

        List<Accounts> result = accountsService.getApprovalRequests(Constants.ADMIN, email, requestType);

        assertEquals(1, result.size());
        assertEquals(pendingUser.getEmail(), result.get(0).getEmail());
        verify(accountsRepository, times(1)).findByAccountStatusAndPincodeAndRole(Constants.PENDING, 12345, Constants.USER);
    }

    @Test
    void testGetApprovalRequests_AdminCheckingPendingEngineers() {
        String email = "admin@example.com";
        String requestType = Constants.ENGINEER;

        Accounts admin = new Accounts();
        admin.setEmail(email);
        admin.setRole(Constants.ADMIN);
        admin.setPincode(67890);

        Accounts pendingEngineer = new Accounts(4, "Eng", "One", "engineer@example.com", "pass", Constants.ENGINEER, "4445556666", "Address", 67890, "Q", "A", Constants.PENDING, "M", 0.0, 0.0);

        when(accountsRepository.findByEmail(email)).thenReturn(Optional.of(admin));
        when(accountsRepository.findFirstByRoleOrderByIdAsc(Constants.ADMIN)).thenReturn(null);
        when(accountsRepository.findByAccountStatusAndPincodeAndRole(Constants.PENDING, 67890, Constants.ENGINEER)).thenReturn(Arrays.asList(pendingEngineer));

        List<Accounts> result = accountsService.getApprovalRequests(Constants.ADMIN, email, requestType);

        assertEquals(1, result.size());
        assertEquals(pendingEngineer.getEmail(), result.get(0).getEmail());
        verify(accountsRepository, times(1)).findByAccountStatusAndPincodeAndRole(Constants.PENDING, 67890, Constants.ENGINEER);
    }


    @Test
    void testGetApprovalRequests_AdminNoPendingRequests() {
        String email = "admin@example.com";
        String requestType = Constants.USER;

        Accounts admin = new Accounts();
        admin.setEmail(email);
        admin.setRole(Constants.ADMIN);
        admin.setPincode(12345);

        when(accountsRepository.findByEmail(email)).thenReturn(Optional.of(admin));
        when(accountsRepository.findFirstByRoleOrderByIdAsc(Constants.ADMIN)).thenReturn(null);
        when(accountsRepository.findByAccountStatusAndPincodeAndRole(Constants.PENDING, 12345, Constants.USER)).thenReturn(Collections.emptyList());

        List<Accounts> result = accountsService.getApprovalRequests(Constants.ADMIN, email, requestType);

        assertTrue(result.isEmpty());
    }
    
    
    @Test
    void testLoadUserByUsername_Success() {
        account.setEmail("test@test.com");
        account.setPassword("encodedPassword");
        account.setRole("USER"); // Make sure role is set!

        when(accountsRepository.findByEmail("test@test.com")).thenReturn(Optional.of(account));

        UserDetails userDetails = accountsService.loadUserByUsername("test@test.com");

        assertNotNull(userDetails);
        assertEquals("test@test.com", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertFalse(userDetails.getAuthorities().isEmpty()); // Ensure roles are set
    }
    
    
    @Test
    void testAccountDetails_UserAccountStatusMethods() {
        account.setEmail("test@test.com");
        account.setPassword("encodedPassword");
        account.setRole("USER");

        AccountDetails userDetails = new AccountDetails(account);

        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.isEnabled());
    }

    
    @Test
    void testShowAccountsByPincode() {
        List<Accounts> accounts = List.of(new Accounts(), new Accounts());
        when(accountsRepository.getByPincode(12345)).thenReturn(accounts);

        List<Accounts> result = accountsService.showAccountsByPincode(12345);

        assertEquals(2, result.size());
    }
    
    @Test
    void testUpdateAccountStatus_AccountNotFound() {
        int accountId = 1;
        String status = "APPROVED";

        when(accountsRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountsService.updateAccountStatus(accountId, status));

        verify(accountsRepository, times(1)).findById(accountId);
        verify(accountsRepository, never()).save(any(Accounts.class));
    }

    @Test
    void testUpdateAccountStatus_ApproveUserWithConnection() {
        int accountId = 1;
        String status = Constants.APPROVED;

        Accounts userAccount = new Accounts();
        userAccount.setId(accountId);
        userAccount.setRole(Constants.USER);
        userAccount.setEmail("user@example.com");
        userAccount.setFirstName("John");

        connection.setConnectionId(100);

        when(accountsRepository.findById(accountId)).thenReturn(Optional.of(userAccount));
        when(connectionsService.searchByUserId(accountId)).thenReturn(Arrays.asList(connection));

        accountsService.updateAccountStatus(accountId, status);

        verify(accountsRepository, times(1)).save(userAccount);
        verify(ticketsService, times(1)).raiseTicketInstallation(eq(connection), any(Tickets.class));
        verify(emailService, times(1)).sendAccountApprovalEmail("user@example.com", "John");
    }

    @Test
    void testUpdateAccountStatus_ApproveUserWithoutConnection() {
        int accountId = 2;
        String status = Constants.APPROVED;

        Accounts userAccount = new Accounts();
        userAccount.setId(accountId);
        userAccount.setRole(Constants.USER);
        userAccount.setEmail("user2@example.com");
        userAccount.setFirstName("Jane");

        when(accountsRepository.findById(accountId)).thenReturn(Optional.of(userAccount));
        when(connectionsService.searchByUserId(accountId)).thenReturn(Collections.emptyList());

        accountsService.updateAccountStatus(accountId, status);

        verify(accountsRepository, times(1)).save(userAccount);
        verify(ticketsService, never()).raiseTicketInstallation(any(), any());
        verify(emailService, times(1)).sendAccountApprovalEmail("user2@example.com", "Jane");
    }

    @Test
    void testUpdateAccountStatus_ApproveAdmin() {
        int accountId = 3;
        String status = Constants.APPROVED;

        Accounts adminAccount = new Accounts();
        adminAccount.setId(accountId);
        adminAccount.setRole(Constants.ADMIN);
        adminAccount.setEmail("admin@example.com");
        adminAccount.setFirstName("Alice");

        when(accountsRepository.findById(accountId)).thenReturn(Optional.of(adminAccount));

        accountsService.updateAccountStatus(accountId, status);

        verify(accountsRepository, times(1)).save(adminAccount);
        verify(emailService, times(1)).sendAdminOrEngineerApprovalEmail("admin@example.com", "Alice", Constants.ADMIN);
    }

    @Test
    void testUpdateAccountStatus_ApproveEngineer() {
        int accountId = 4;
        String status = Constants.APPROVED;

        Accounts engineerAccount = new Accounts();
        engineerAccount.setId(accountId);
        engineerAccount.setRole(Constants.ENGINEER);
        engineerAccount.setEmail("engineer@example.com");
        engineerAccount.setFirstName("Bob");

        when(accountsRepository.findById(accountId)).thenReturn(Optional.of(engineerAccount));

        accountsService.updateAccountStatus(accountId, status);

        verify(accountsRepository, times(1)).save(engineerAccount);
        verify(emailService, times(1)).sendAdminOrEngineerApprovalEmail("engineer@example.com", "Bob", Constants.ENGINEER);
    }

    @Test
    void testUpdateAccountStatus_RejectUserWithConnection() {
        int accountId = 5;
        String status = "REJECTED";

        Accounts userAccount = new Accounts();
        userAccount.setId(accountId);
        userAccount.setRole(Constants.USER);
        userAccount.setEmail("user3@example.com");
        userAccount.setFirstName("Charlie");
        connection.setConnectionId(200);

        when(accountsRepository.findById(accountId)).thenReturn(Optional.of(userAccount));
        when(connectionsService.searchByUserId(accountId)).thenReturn(Arrays.asList(connection));

        accountsService.updateAccountStatus(accountId, status);

        verify(accountsRepository, times(1)).save(userAccount);
        verify(connectionsRepository, times(1)).deleteById(200);
        verify(emailService, times(1)).sendAccountRejectionEmail("user3@example.com", "Charlie");
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        when(accountsRepository.findByEmail("notfound@test.com")).thenReturn(Optional.empty());

        Exception exception = assertThrows(UsernameNotFoundException.class, 
            () -> accountsService.loadUserByUsername("notfound@test.com"));

        assertEquals("Email not found: notfound@test.com", exception.getMessage()); // Fixed expected message
    }




    @Test
    void testSearchByEmail_Found() {
        when(accountsRepository.findByEmail("test@example.com")).thenReturn(Optional.of(account));
        Accounts result = accountsService.searchByEmail("test@example.com");
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void testSearchByEmail_NotFound() {
        when(accountsRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> accountsService.searchByEmail("test@example.com"));
    }

    @Test
    void testGetSecurityQuestionByEmail_ApprovedAccount() {
        account.setSecurityQuestion("What is your pet's name?");
        account.setAccountStatus("APPROVED"); // Ensure status is APPROVED

        when(accountsRepository.findByEmail("test@test.com")).thenReturn(Optional.of(account));

        String question = accountsService.getSecurityQuestionByEmail("test@test.com");

        assertEquals("What is your pet's name?", question);
    }

    @Test
    void testGetSecurityQuestionByEmail_NotApprovedAccount() {
        account.setSecurityQuestion("What is your pet's name?");
        account.setAccountStatus(Constants.PENDING); // Not approved

        when(accountsRepository.findByEmail("test@test.com")).thenReturn(Optional.of(account));

        Exception exception = assertThrows(AccountNotFoundException.class, 
            () -> accountsService.getSecurityQuestionByEmail("test@test.com"));

        assertEquals("Account with address test@test.com not available.", exception.getMessage());
    }

    @Test
    void testGetSecurityQuestionByEmail_NoAccountFound() {
        when(accountsRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        Exception exception = assertThrows(AccountNotFoundException.class, 
            () -> accountsService.getSecurityQuestionByEmail("test@test.com"));

        assertEquals("Account with address test@test.com not detected.", exception.getMessage());
    }





    @Test
    void testVerifySecurityAnswer_Success() {
        account.setEmail("test@test.com");
        account.setSecurityAnswer("Dog");

        when(accountsRepository.findByEmail("test@test.com")).thenReturn(Optional.of(account));

        assertTrue(accountsService.verifySecurityAnswer("test@test.com", "Dog"));
    }

    @Test
    void testVerifySecurityAnswer_IncorrectAnswer() {
        account.setEmail("test@test.com");
        account.setSecurityAnswer("Dog");

        when(accountsRepository.findByEmail("test@test.com")).thenReturn(Optional.of(account));

        SecurityAnswerMismatchException exception = assertThrows(
            SecurityAnswerMismatchException.class,
            () -> accountsService.verifySecurityAnswer("test@test.com", "Cat")
        );

        assertEquals("Security answer is incorrect.", exception.getMessage());
    }

    @Test
    void testVerifySecurityAnswer_AccountNotFound() {
        when(accountsRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        AccountNotFoundException exception = assertThrows(
            AccountNotFoundException.class,
            () -> accountsService.verifySecurityAnswer("unknown@test.com", "Dog")
        );

        assertEquals("Account with email unknown@test.com is not available.", exception.getMessage());
    }

    @Test
    void testUpdatePassword() {
        when(accountsRepository.findByEmail("test@test.com")).thenReturn(Optional.of(account));
        when(encoder.encode("newPass")).thenReturn("encodedPass");

        assertTrue(accountsService.updatePassword("test@test.com", "newPass"));
    }
    
    @Test
    void testUpdatePassword_AccountNotFound() {
        String email = "test@example.com";
        String newPassword = "newPassword123";

        when(accountsRepository.findByEmail(email)).thenReturn(Optional.empty()); // Account not found

        Exception exception = assertThrows(AccountNotFoundException.class, 
            () -> accountsService.updatePassword(email, newPassword));

        assertEquals("Account with email test@example.com not detected.", exception.getMessage());

        verify(accountsRepository, never()).save(any(Accounts.class)); // Ensure save is never called
    }
    
    @Test
    void testGetSuperAdmin_Success() {
        Accounts superAdmin = new Accounts();
        superAdmin.setId(1);
        superAdmin.setRole(Constants.ADMIN);
        superAdmin.setEmail("admin@example.com");

        when(accountsRepository.findFirstByRoleOrderByIdAsc(Constants.ADMIN)).thenReturn(superAdmin);

        Accounts result = accountsService.getSuperAdmin();

        assertNotNull(result);
        assertEquals(Constants.ADMIN, result.getRole());
        assertEquals("admin@example.com", result.getEmail());
        verify(accountsRepository, times(1)).findFirstByRoleOrderByIdAsc(Constants.ADMIN);
    }

    @Test
    void testGetSuperAdmin_NoAdminFound() {
        when(accountsRepository.findFirstByRoleOrderByIdAsc(Constants.ADMIN)).thenReturn(null);

        Accounts result = accountsService.getSuperAdmin();

        assertNull(result);
        verify(accountsRepository, times(1)).findFirstByRoleOrderByIdAsc(Constants.ADMIN);
    }

    /*** Test cases for getPendingEngineersAndUsersCount() ***/

    @Test
    void testGetPendingEngineersAndUsersCount_Success() {
        int pincode = 12345;
        long expectedCount = 5;

        when(accountsRepository.countByAccountStatusAndRoleInAndPincode(Constants.PENDING, List.of(Constants.ENGINEER, Constants.USER), pincode))
                .thenReturn(expectedCount);

        long result = accountsService.getPendingEngineersAndUsersCount(pincode);

        assertEquals(expectedCount, result);
        verify(accountsRepository, times(1))
                .countByAccountStatusAndRoleInAndPincode(Constants.PENDING, List.of(Constants.ENGINEER, Constants.USER), pincode);
    }

    @Test
    void testGetPendingEngineersAndUsersCount_NoPendingUsersOrEngineers() {
        int pincode = 67890;
        long expectedCount = 0;

        when(accountsRepository.countByAccountStatusAndRoleInAndPincode(Constants.PENDING, List.of(Constants.ENGINEER, Constants.USER), pincode))
                .thenReturn(expectedCount);

        long result = accountsService.getPendingEngineersAndUsersCount(pincode);

        assertEquals(expectedCount, result);
    }

    /*** Test cases for getPendingEngineersCount() ***/

    @Test
    void testGetPendingEngineersCount_Success() {
        int pincode = 12345;
        long expectedCount = 3;

        when(accountsRepository.countByAccountStatusAndRoleAndPincode(Constants.PENDING, Constants.ENGINEER, pincode))
                .thenReturn(expectedCount);

        long result = accountsService.getPendingEngineersCount(pincode);

        assertEquals(expectedCount, result);
        verify(accountsRepository, times(1))
                .countByAccountStatusAndRoleAndPincode(Constants.PENDING, Constants.ENGINEER, pincode);
    }

    @Test
    void testGetPendingEngineersCount_NoPendingEngineers() {
        int pincode = 67890;
        long expectedCount = 0;

        when(accountsRepository.countByAccountStatusAndRoleAndPincode(Constants.PENDING, Constants.ENGINEER, pincode))
                .thenReturn(expectedCount);

        long result = accountsService.getPendingEngineersCount(pincode);

        assertEquals(expectedCount, result);
    }

    /*** Test cases for getPendingUsersCount() ***/

    @Test
    void testGetPendingUsersCount_Success() {
        int pincode = 12345;
        long expectedCount = 2;

        when(accountsRepository.countByAccountStatusAndRoleAndPincode(Constants.PENDING, Constants.USER, pincode))
                .thenReturn(expectedCount);

        long result = accountsService.getPendingUsersCount(pincode);

        assertEquals(expectedCount, result);
        verify(accountsRepository, times(1))
                .countByAccountStatusAndRoleAndPincode(Constants.PENDING, Constants.USER, pincode);
    }

    @Test
    void testGetPendingUsersCount_NoPendingUsers() {
        int pincode = 67890;
        long expectedCount = 0;

        when(accountsRepository.countByAccountStatusAndRoleAndPincode(Constants.PENDING, Constants.USER, pincode))
                .thenReturn(expectedCount);

        long result = accountsService.getPendingUsersCount(pincode);

        assertEquals(expectedCount, result);
    }
}
