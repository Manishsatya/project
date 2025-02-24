package com.brillio.sts.mock;
 
import com.brillio.sts.model.Hazards;
import com.brillio.sts.repo.HazardsRepository;
import com.brillio.sts.service.HazardsService;
 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
 
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
 
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
 
@ExtendWith(MockitoExtension.class)
class HazardsServiceTest {
 
    @Mock
    private HazardsRepository hazardsRepository;
 
    @InjectMocks
    private HazardsService hazardsService;
 
    private Hazards hazard;
    
    private Hazards updatedHazard;
    
    private Hazards duplicateHazard;
   
    
    @BeforeEach
    void setUp() {
        hazard = new Hazards();
        hazard.setHazardId(1);
        hazard.setHazardName("Flood");
        hazard.setHazardStatus("ACTIVE");
        hazard.setHazardLocation("Downtown");
        hazard.setHazardPincode(12345);
        hazard.setHazardSeverity("HIGH");
        
     // Updated version of the same hazard
        updatedHazard = new Hazards();
        updatedHazard.setHazardId(1);
        updatedHazard.setHazardName("Flood");
        updatedHazard.setHazardStatus("INACTIVE");  // Simulating status update
        updatedHazard.setHazardLocation("Downtown");
        updatedHazard.setHazardPincode(12345);
        updatedHazard.setHazardSeverity("HIGH");
 
        // Duplicate active hazard that should cause failure
        duplicateHazard = new Hazards();
        duplicateHazard.setHazardId(2);
        duplicateHazard.setHazardName("Flood");
        duplicateHazard.setHazardStatus("ACTIVE"); // Still active, causing a duplicate issue
        duplicateHazard.setHazardLocation("Downtown");
        duplicateHazard.setHazardPincode(12345);
        duplicateHazard.setHazardSeverity("HIGH");
        
       
    }
 
    @Test
    void testShowHazards_Positive() {
        when(hazardsRepository.findAll()).thenReturn(Arrays.asList(hazard));
        List<Hazards> hazardsList = hazardsService.showHazards();
        assertEquals(1, hazardsList.size());
    }
 
    @Test
    void testShowHazards_Negative() {
        when(hazardsRepository.findAll()).thenReturn(Arrays.asList());
        List<Hazards> hazardsList = hazardsService.showHazards();
        assertTrue(hazardsList.isEmpty());
    }
 
    @Test
    void testSearchById_Positive() {
        when(hazardsRepository.findById(1)).thenReturn(Optional.of(hazard));
        Hazards foundHazard = hazardsService.searchById(1);
        assertNotNull(foundHazard);
        assertEquals("Flood", foundHazard.getHazardName());
    }
 
    @Test
    void testSearchById_Negative() {
        when(hazardsRepository.findById(2)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> hazardsService.searchById(2));
    }
 
    @Test
    void testCreateHazard_Positive() {
        when(hazardsRepository.findByHazardNameAndHazardLocationAndHazardPincodeAndHazardStatus(any(), any(), anyInt(), any())).thenReturn(Arrays.asList());
        when(hazardsRepository.save(any(Hazards.class))).thenReturn(hazard);
        Hazards createdHazard = hazardsService.createHazard(hazard);
        assertNotNull(createdHazard);
        assertEquals("Flood", createdHazard.getHazardName());
    }
 
    @Test
    void testCreateHazard_Negative() {
        when(hazardsRepository.findByHazardNameAndHazardLocationAndHazardPincodeAndHazardStatus(any(), any(), anyInt(), any())).thenReturn(Arrays.asList(hazard));
        assertThrows(IllegalArgumentException.class, () -> hazardsService.createHazard(hazard));
    }
 
    @Test
    void testGetHazardsByAddressAndPincode_Positive() {
        when(hazardsRepository.findByHazardLocationAndHazardPincode("Downtown", 12345)).thenReturn(Arrays.asList(hazard));
        List<Hazards> hazardsList = hazardsService.getHazardsByAddressAndPincode("Downtown", 12345);
        assertEquals(1, hazardsList.size());
    }
 
    @Test
    void testGetHazardsByAddressAndPincode_Negative() {
        when(hazardsRepository.findByHazardLocationAndHazardPincode("Downtown", 12345)).thenReturn(Arrays.asList());
        List<Hazards> hazardsList = hazardsService.getHazardsByAddressAndPincode("Downtown", 12345);
        assertTrue(hazardsList.isEmpty());
    }
 
    
 
        @Test
        void testUpdateHazard_Success() {
            Mockito.when(hazardsRepository.findById(1)).thenReturn(Optional.of(hazard));
            Mockito.when(hazardsRepository.findByHazardNameAndHazardLocationAndHazardPincodeAndHazardStatus(
                    updatedHazard.getHazardName(), updatedHazard.getHazardLocation(), updatedHazard.getHazardPincode(), "ACTIVE"
            )).thenReturn(Collections.emptyList());
            Mockito.when(hazardsRepository.save(Mockito.any(Hazards.class))).thenReturn(updatedHazard);
 
            Hazards result = hazardsService.updateHazard(1, updatedHazard);
            assertNotNull(result);
            assertEquals("INACTIVE", result.getHazardStatus());
        }
 
        @Test
        void testUpdateHazard_NotFound() {
            Mockito.when(hazardsRepository.findById(1)).thenReturn(Optional.empty());
            
            Exception exception = assertThrows(RuntimeException.class, () -> {
                hazardsService.updateHazard(1, updatedHazard);
            });
            assertEquals("Hazard not found with id: 1", exception.getMessage());
        }
 
        @Test
        void testUpdateHazard_DuplicateActiveHazard() {
            Mockito.when(hazardsRepository.findById(1)).thenReturn(Optional.of(hazard));
            Mockito.when(hazardsRepository.findByHazardNameAndHazardLocationAndHazardPincodeAndHazardStatus(
                    duplicateHazard.getHazardName(), duplicateHazard.getHazardLocation(), duplicateHazard.getHazardPincode(), "ACTIVE"
            )).thenReturn(Collections.singletonList(duplicateHazard));
 
            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                hazardsService.updateHazard(1, duplicateHazard);
            });
            assertEquals("An active hazard with the same name, address, and pincode already exists.", exception.getMessage());
        }
    }