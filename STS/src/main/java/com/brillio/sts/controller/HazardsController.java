package com.brillio.sts.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.brillio.sts.model.Hazards;
import com.brillio.sts.repo.HazardsRepository;
import com.brillio.sts.service.HazardsService;

@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")

@RestController
@RequestMapping(value="/hazards")
public class HazardsController {
	
	private final HazardsRepository hazardsRepository;
	private final HazardsService hazardsService;
	
	
	public HazardsController(
			HazardsRepository hazardsRepository,
			HazardsService hazardsService
			) {
		this.hazardsRepository =hazardsRepository;
		this.hazardsService =hazardsService;
	}
	
	@GetMapping(value = "/searchHazards/{id}")
	public Optional<Hazards> searchById(@PathVariable int id) {
	return hazardsRepository.findById(id);
	}
	
	@PostMapping(value="/addHazard")
	public Hazards createHazard(@RequestBody Hazards hazards) {
		return hazardsService.createHazard(hazards);
	}
	
	@GetMapping(value = "/showHazards")
	public List<Hazards> showHazards(){
		return hazardsRepository.findAll();
	}
	
	@PutMapping("/updateHazard/{hazardId}")
	public ResponseEntity<Hazards> updateHazard(@PathVariable int hazardId, @RequestBody Hazards hazardDetails) {    
	 
		Hazards updatedHazard = hazardsService.updateHazard(hazardId, hazardDetails);         
    	// Return the updated hazard entity as the response
    	return ResponseEntity.ok(updatedHazard);
	
	}
	
	@GetMapping("/byAddressAndPincode")
    public ResponseEntity<List<Hazards>> getHazardsByAddressAndPincode(@RequestParam String address, @RequestParam int pincode) {         
    	List<Hazards> hazards = hazardsRepository.findByHazardLocationAndHazardPincode(address, pincode);
    	return ResponseEntity.ok(hazards);
    			
	}
	

}
