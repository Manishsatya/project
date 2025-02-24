package com.brillio.sts.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "HAZARDS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Hazards {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "HAZARD_ID")
	private int hazardId;    
 
 
	@Column(name = "UPDATED_BY_ID")
	private int updatedById;
 
	@Column(name = "UPDATED_BY_ROLE")
	private String updatedByRole; // Should contain values like "admin" or "engineer"
 
	@Column(name = "HAZARD_NAME")
	private String hazardName;
 
	@Column(name = "HAZARD_SEVERITY") // Assuming "HAZARD_SEVERITY" should be the name for hazardSeverity
	private String hazardSeverity; // Should contain values like "high", "medium", or "low"
 
	@Column(name = "HAZARD_LOCATION")
	private String hazardLocation;
 
	@Column(name = "HAZARD_PINCODE")
	private Integer hazardPincode; // Using Integer to allow null values if needed
 
	@Column(name = "HAZARD_STATUS")
	private String hazardStatus; // Should contain values like "active" or "inactive"
 
	@Column(name = "DETECTED_AT")
	private LocalDateTime detectedAt;

}
