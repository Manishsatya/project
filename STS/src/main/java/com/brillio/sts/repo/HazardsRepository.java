package com.brillio.sts.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brillio.sts.model.Hazards;

public interface HazardsRepository extends JpaRepository<Hazards, Integer>{
	
	List<Hazards> findByUpdatedByRoleAndHazardPincode(String role, Integer pincode);
	List<Hazards> findByUpdatedByIdAndHazardPincode(int id, Integer pincode);
	List<Hazards> findByHazardPincode(Integer pincode);
	List<Hazards> findByHazardLocationAndHazardPincode(String address, Integer pincode);
	List<Hazards> findByHazardNameAndHazardLocationAndHazardPincodeAndHazardStatus(String hazardName, String hazardLocation, int hazardPincode, String hazardStatus);
	
}
