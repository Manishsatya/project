package com.brillio.sts.mock;
 
 
import static org.junit.jupiter.api.Assertions.*;
 
import static org.mockito.Mockito.*;
import java.time.LocalDateTime;
 
import java.util.Arrays;
 
import java.util.Collections;
 
import java.util.List;
 
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
 
import org.junit.jupiter.api.Test;
 
import org.junit.jupiter.api.extension.ExtendWith;
 
import org.mockito.InjectMocks;
 
import org.mockito.Mock;
 
import org.mockito.junit.jupiter.MockitoExtension;
import com.brillio.sts.model.Hazards;
import com.brillio.sts.repo.HazardsRepository;
import com.brillio.sts.service.HazardsService;
 
 

@ExtendWith(MockitoExtension.class) // Enables Mockito
 
class HazardsServiceTest {
    @Mock
 
    private HazardsRepository hazardRepository; // Mocked database
    @InjectMocks
 
    private HazardsService hazardService; // Injects the mock repository into service
    private Hazards mockHazard1;
 
    private Hazards mockHazard2;
    @BeforeEach
 
    void setUp() {
 
        mockHazard1 = new Hazards(1, 2, "ADMIN", "Flood", "High", "Downtown", 560001, "Active", LocalDateTime.now());
 
        mockHazard2 = new Hazards(2, 3, "ENGINEER", "Fire", "Medium", "Uptown", 560002, "Inactive", LocalDateTime.now());
 
    }
    // ✅ Test: Fetch all hazards
 
    @Test
 
     void testShowHazards_ShouldReturnHazardList() {
 
        when(hazardRepository.findAll()).thenReturn(Arrays.asList(mockHazard1, mockHazard2));
 
       // Calls the actual service method showHazards().
 
        List<Hazards> hazardsList = hazardService.showHazards();
 
  // Ensures that hazardService.showHazards() returns a valid (non-null) list.
 
        assertNotNull(hazardsList);
 
        // Ensures that the returned list contains exactly 2 hazards
 
        assertEquals(2, hazardsList.size());
 
        // Checks whether findAll() was actually called exactly once.
 
        verify(hazardRepository, times(1)).findAll();
 
    }
 
 
   // This tests if showHazards() returns an empty list when no hazards exist,
 
    //instead of throwing an error.
 
    @Test
 
     void testShowHazards_ShouldReturnEmptyList_WhenNoHazardsFound() {
 
        when(hazardRepository.findAll()).thenReturn(Collections.emptyList());
        List<Hazards> hazardsList = hazardService.showHazards();
        assertNotNull(hazardsList); // ✅ Should not be null, even if empty
 
        assertEquals(0, hazardsList.size()); // ❌ Expected 0 hazards
        verify(hazardRepository, times(1)).findAll();
 
    }

    // ✅ Test: Search hazard by ID
 
    @Test
 
     void testSearchById_ShouldReturnHazard() {
 
        when(hazardRepository.findById(1)).thenReturn(Optional.of(mockHazard1));
        Hazards hazard = hazardService.searchById(1);
        assertNotNull(hazard);
 
        assertEquals("Flood", hazard.getHazardName());
 
        verify(hazardRepository, times(1)).findById(1);
 
    }
    // ❌ Test: Search hazard by ID (Not Found)
 
    // This test verifies that when we try to search for a hazard that does not exist (hazardId = 10),
 
    //the searchById() method should throw a RuntimeException instead of returning null.
 
    @Test
 
     void testSearchById_HazardNotFound_ShouldThrowException() {
 
        when(hazardRepository.findById(10)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> hazardService.searchById(10));
 
        verify(hazardRepository, times(1)).findById(10);
 
    }
    // ✅ Test: Create a new hazard
 
    @Test
 
     void testCreateHazard_ShouldReturnSavedHazard() {
 
        when(hazardRepository.save(any(Hazards.class))).thenReturn(mockHazard1);
        Hazards savedHazard = hazardService.createHazard(mockHazard1);
        assertNotNull(savedHazard);
 
        assertEquals("Flood", savedHazard.getHazardName());
 
        verify(hazardRepository, times(1)).save(mockHazard1);
 
    }
    // ✅ Test: Get hazards by address and pincode
 
    @Test
 
     void testGetHazardsByAddressAndPincode_ShouldReturnMatchingHazards() {
 
        when(hazardRepository.findByHazardLocationAndHazardPincode("Downtown", 560001))
 
                .thenReturn(Arrays.asList(mockHazard1));
        List<Hazards> hazards = hazardService.getHazardsByAddressAndPincode("Downtown", 560001);
        assertNotNull(hazards);
 
        assertEquals(1, hazards.size());
 
        assertEquals("Flood", hazards.get(0).getHazardName());
 
        verify(hazardRepository, times(1)).findByHazardLocationAndHazardPincode("Downtown", 560001);
 
    }
 
    // ❌ 8. Fetch hazards by address and pincode (Not Found)
 
    @Test
 
     void testGetHazardsByAddressAndPincode_NoMatchingHazards_ShouldReturnEmptyList() {
 
        when(hazardRepository.findByHazardLocationAndHazardPincode("Unknown", 999999))
 
                .thenReturn(Collections.emptyList()); // ✅ Mocking empty list return
        List<Hazards> hazards = hazardService.getHazardsByAddressAndPincode("Unknown", 999999);
        assertNotNull(hazards); // ✅ Ensures the method does not return null
 
        assertEquals(0, hazards.size()); // ✅ Ensures the result is an empty list
 
        verify(hazardRepository, times(1)).findByHazardLocationAndHazardPincode("Unknown", 999999);
 
    }

    // ✅ Test: Update an existing hazard
 
    @Test
    void testUpdateHazard_ShouldUpdateAndReturnHazard() {
        // Mock existing hazard
        Hazards existingHazard = new Hazards();
        existingHazard.setHazardId(1);
        existingHazard.setHazardName("Flood");
        existingHazard.setHazardStatus("Active");
        existingHazard.setHazardSeverity("High");
        existingHazard.setHazardLocation("New York");
        existingHazard.setHazardPincode(10001);

        when(hazardRepository.findById(1)).thenReturn(Optional.of(existingHazard));
        when(hazardRepository.findByHazardNameAndHazardLocationAndHazardPincodeAndHazardStatus(
                anyString(), anyString(), anyInt(), eq("ACTIVE"))).thenReturn(List.of());

        when(hazardRepository.save(any(Hazards.class))).thenReturn(existingHazard);

        // Create updated details
        Hazards updatedDetails = new Hazards();
        updatedDetails.setHazardName("Earthquake");
        updatedDetails.setHazardStatus("Resolved");
        updatedDetails.setHazardSeverity("Critical");

        // Perform update
        Hazards updatedHazard = hazardService.updateHazard(1, updatedDetails);

        // Assertions
        assertNotNull(updatedHazard);
        assertEquals("Earthquake", updatedHazard.getHazardName());
        assertEquals("Resolved", updatedHazard.getHazardStatus());
        assertEquals("Critical", updatedHazard.getHazardSeverity());

        // Verify interactions
        verify(hazardRepository, times(1)).findById(1);
        verify(hazardRepository, times(1)).save(existingHazard);
    }
    
    @Test
    void testUpdateHazard_ShouldCheckForExistingActiveHazards() {
        // Mock existing hazard
        Hazards existingHazard = new Hazards();
        existingHazard.setHazardId(1);
        existingHazard.setHazardName("Flood");
        existingHazard.setHazardStatus("ACTIVE");
        existingHazard.setHazardLocation("New York");
        existingHazard.setHazardPincode(10001);

        // Mock an active hazard (same name, location, and pincode)
        Hazards duplicateHazard = new Hazards();
        duplicateHazard.setHazardId(2);
        duplicateHazard.setHazardName("Flood");
        duplicateHazard.setHazardLocation("New York");
        duplicateHazard.setHazardPincode(10001);
        duplicateHazard.setHazardStatus("ACTIVE");

        when(hazardRepository.findById(1)).thenReturn(Optional.of(existingHazard));

        // Return a duplicate active hazard, so the condition is triggered
        when(hazardRepository.findByHazardNameAndHazardLocationAndHazardPincodeAndHazardStatus(
                "Flood", "New York", 10001, "ACTIVE")).thenReturn(List.of(duplicateHazard));

        Hazards updatedDetails = new Hazards();
        updatedDetails.setHazardName("Flood");
        updatedDetails.setHazardStatus("Resolved");
        updatedDetails.setHazardSeverity("Critical");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            hazardService.updateHazard(1, updatedDetails);
        });

        assertEquals("An active hazard with the same name, address, and pincode already exists.", exception.getMessage());

        // Verify that this method was called
        verify(hazardRepository, times(1))
                .findByHazardNameAndHazardLocationAndHazardPincodeAndHazardStatus(
                        "Flood", "New York", 10001, "ACTIVE");
    }

    @Test
    void testUpdateHazard_ShouldThrowException_WhenHazardNotFound() {
        when(hazardRepository.findById(99)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            hazardService.updateHazard(99, new Hazards());
        });

        assertEquals("Hazard not found with id: 99", exception.getMessage());
        verify(hazardRepository, times(1)).findById(99);
        verify(hazardRepository, never()).save(any(Hazards.class));
    }

    @Test
    void testUpdateHazard_ShouldThrowException_WhenDuplicateActiveHazardExists() {
        // Mock existing hazard
        Hazards existingHazard = new Hazards();
        existingHazard.setHazardId(1);
        existingHazard.setHazardName("Flood");
        existingHazard.setHazardLocation("New York");
        existingHazard.setHazardPincode(10001);
        existingHazard.setHazardStatus("ACTIVE");

        when(hazardRepository.findById(1)).thenReturn(Optional.of(existingHazard));

        // Mock duplicate hazard
        Hazards duplicateHazard = new Hazards();
        duplicateHazard.setHazardId(2);
        duplicateHazard.setHazardName("Earthquake");
        duplicateHazard.setHazardLocation("New York");
        duplicateHazard.setHazardPincode(10001);
        duplicateHazard.setHazardStatus("ACTIVE");

        when(hazardRepository.findByHazardNameAndHazardLocationAndHazardPincodeAndHazardStatus(
                "Earthquake", "New York", 10001, "ACTIVE")).thenReturn(Arrays.asList(duplicateHazard));

        Hazards updatedDetails = new Hazards();
        updatedDetails.setHazardName("Earthquake");
        updatedDetails.setHazardStatus("Resolved");
        updatedDetails.setHazardSeverity("Critical");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            hazardService.updateHazard(1, updatedDetails);
        });

        assertEquals("An active hazard with the same name, address, and pincode already exists.", exception.getMessage());

        verify(hazardRepository, times(1)).findById(1);
        verify(hazardRepository, never()).save(any(Hazards.class));
    }

}