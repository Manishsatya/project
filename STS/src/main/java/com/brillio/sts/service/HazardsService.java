/**
* @author Anoop.Lakhinana
*/
 
 
package com.brillio.sts.service;
 
import com.brillio.sts.model.Hazards;
import com.brillio.sts.repo.HazardsRepository;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.util.List;
import java.util.NoSuchElementException;
 
@Service
@Transactional
public class HazardsService {
 
    private static final Logger logger = Logger.getLogger(HazardsService.class);
    private final HazardsRepository hazardsRepository;
 
    public HazardsService(HazardsRepository hazardsRepository) {
        this.hazardsRepository = hazardsRepository;
    }
 
    /**
     * Retrieves a list of all hazards present in the database.
     * Logs the total count of hazards retrieved.
     *
     * @return A list of all hazards.
     */
    public List<Hazards> showHazards() {
        logger.info("Fetching all hazards");
        List<Hazards> hazards = hazardsRepository.findAll();
        logger.info("Fetched hazards: " + hazards.size());
        return hazards;
    }
 
    /**
     * Searches for a hazard by its unique ID.
     * If the hazard is not found, an exception is thrown.
     *
     * @param id The ID of the hazard to be searched.
     * @return The hazard object if found.
     * @throws NoSuchElementException if the hazard is not found.
     */
    public Hazards searchById(int id) {
        logger.info("Searching hazard by ID: " + id);
        return hazardsRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Hazard not found with ID: " + id);
                    throw new NoSuchElementException("Hazard not found with ID: " + id);
                });
    }
 
    /**
     * Creates a new hazard and saves it to the database.
     * Checks for an existing active hazard with the same name, location, and pincode.
     * Logs appropriate messages for duplicate hazards.
     *
     * @param hazard The hazard object to be created.
     * @return The created hazard object.
     * @throws IllegalArgumentException if a duplicate active hazard exists.
     */
    public Hazards createHazard(Hazards hazard) {
        logger.info("Creating new hazard with name: " + hazard.getHazardName());
 
        List<Hazards> existingHazards = hazardsRepository.findByHazardNameAndHazardLocationAndHazardPincodeAndHazardStatus(
                hazard.getHazardName(), hazard.getHazardLocation(), hazard.getHazardPincode(), "ACTIVE"
        );
 
        if (!existingHazards.isEmpty()) {
            logger.error("Duplicate active hazard found for name: " + hazard.getHazardName() + ", location: " + hazard.getHazardLocation() + ", and pincode: " + hazard.getHazardPincode());
            throw new IllegalArgumentException("An active hazard with the same name, address, and pincode already exists.");
        }
 
        hazard.setDetectedAt(java.time.LocalDateTime.now());
        Hazards savedHazard = hazardsRepository.save(hazard);
        logger.info("Hazard created successfully with ID: " + savedHazard.getHazardId());
        return savedHazard;
    }
 
    /**
     * Retrieves hazards based on a specific address and pincode.
     * Logs the number of hazards found for the given address and pincode.
     *
     * @param address The address to filter hazards.
     * @param pincode The pincode to filter hazards.
     * @return A list of hazards matching the criteria.
     */
    public List<Hazards> getHazardsByAddressAndPincode(String address, int pincode) {
        logger.info("Fetching hazards for Address: " + address + ", Pincode: " + pincode);
        List<Hazards> hazards = hazardsRepository.findByHazardLocationAndHazardPincode(address, pincode);
        logger.info("Fetched hazards for Address: " + address + ", Pincode: " + pincode + " - Count: " + hazards.size());
        return hazards;
    }
 
    /**
     * Updates an existing hazard identified by hazardId to change status to inactive
     * Ensures there are no duplicate active hazards with the same name, location, and pincode.
     *
     * @param hazardId      The ID of the hazard to be updated.
     * @param hazardDetails The updated details of the hazard.
     * @return The updated hazard object.
     * @throws RuntimeException if the hazard is not found.
     * @throws IllegalArgumentException if a duplicate active hazard exists.
     */
   public Hazards updateHazard(int hazardId, Hazards hazardDetails) {
        logger.info("Updating hazard with ID: " + hazardId);
        Hazards existingHazard = hazardsRepository.findById(hazardId)
                .orElseThrow(() -> {
                    logger.error("Hazard not found with ID: " + hazardId);
                    throw new RuntimeException("Hazard not found with id: " + hazardId);
                });
     
       
        List<Hazards> existingHazards = hazardsRepository.findByHazardNameAndHazardLocationAndHazardPincodeAndHazardStatus(
                hazardDetails.getHazardName(), hazardDetails.getHazardLocation(), hazardDetails.getHazardPincode(), "ACTIVE"
        );
     
        
        if (!existingHazards.isEmpty() && !existingHazards.stream().anyMatch(h -> h.getHazardId() == hazardId) && !"INACTIVE".equals(hazardDetails.getHazardStatus())) {
            logger.error("Duplicate active hazard found for name: " + hazardDetails.getHazardName() + ", location: " + hazardDetails.getHazardLocation() + ", and pincode: " + hazardDetails.getHazardPincode());
            throw new IllegalArgumentException("An active hazard with the same name, address, and pincode already exists.");
        }
     
        existingHazard.setHazardName(hazardDetails.getHazardName());
        existingHazard.setHazardStatus(hazardDetails.getHazardStatus());
        existingHazard.setHazardSeverity(hazardDetails.getHazardSeverity());
        existingHazard.setHazardLocation(hazardDetails.getHazardLocation());
        existingHazard.setHazardPincode(hazardDetails.getHazardPincode());
        java.sql.Timestamp timestamp = new java.sql.Timestamp(System.currentTimeMillis());
        java.time.LocalDateTime localDateTime = timestamp.toLocalDateTime();
        existingHazard.setDetectedAt(localDateTime);
        Hazards updatedHazard = hazardsRepository.save(existingHazard);
        logger.info("Hazard updated successfully with ID: " + updatedHazard.getHazardId());
        
        return updatedHazard;
 
    }
}