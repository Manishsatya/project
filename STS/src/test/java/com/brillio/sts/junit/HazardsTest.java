package com.brillio.sts.junit;
 
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
 
import java.time.LocalDateTime;
 
import org.junit.jupiter.api.Test;
 
import com.brillio.sts.model.Hazards;
 
 
class HazardsTest {
	@Test
	   void testGettersAndSetters()  {
        Hazards hazards=new Hazards();
         hazards.setHazardId(1);
         hazards.setUpdatedById(2);
         hazards.setUpdatedByRole("user");
         hazards.setHazardName("cloudy");
         hazards.setHazardSeverity("High");
         hazards.setHazardLocation("Banashankari 1st Stage, Bengaluru, Karnataka");
         hazards.setHazardPincode(560050);
         hazards.setHazardStatus("Inactive");
         LocalDateTime detectedTime = LocalDateTime.of(2025, 2, 14, 9, 56, 50);
         hazards.setDetectedAt(detectedTime);
 
         assertEquals(1,hazards.getHazardId());
         assertEquals(2,hazards.getUpdatedById());
         assertEquals("user",hazards.getUpdatedByRole());
         assertEquals("cloudy",hazards.getHazardName());
         assertEquals("High",hazards.getHazardSeverity());
         assertEquals("Banashankari 1st Stage, Bengaluru, Karnataka", hazards.getHazardLocation());
         assertEquals(560050,hazards.getHazardPincode(),0);
         assertEquals("Inactive",hazards.getHazardStatus());
         assertEquals(detectedTime,hazards.getDetectedAt());
 
}
	@Test
     void testToString()  {
		LocalDateTime detectedTime = LocalDateTime.of(2025, 2, 14, 9, 56, 50);
		Hazards hazards1=new Hazards(1,2,"user","cloudy","High","Banashankari 1st Stage, Bengaluru, Karnataka",560050,"Inactive",detectedTime);
		String result="Hazards [hazardId=1, updatedById=2, updatedByRole=user, hazardName=cloudy, hazardSeverity=High,"
				+ " hazardLocation=Banashankari 1st Stage, Bengaluru, Karnataka, hazardPincode=560050,"
				+ " hazardStatus=Inactive, detectedAt=" + detectedTime +"]";
		assertEquals(result,hazards1.toString());
	}
	@Test
     void testConstructor() {
		LocalDateTime detectedTime = LocalDateTime.of(2025, 2, 14, 9, 56, 50);
		Hazards hazards1=new Hazards(1,2,"user","cloudy","High","Banashankari 1st Stage, Bengaluru, Karnataka",560050,"Inactive",detectedTime);
        assertEquals(1,hazards1.getHazardId());
        assertEquals(2,hazards1.getUpdatedById());
        assertEquals("user",hazards1.getUpdatedByRole());
        assertEquals("cloudy",hazards1.getHazardName());	
        assertEquals("High",hazards1.getHazardSeverity());
        assertEquals("Banashankari 1st Stage, Bengaluru, Karnataka", hazards1.getHazardLocation());
        assertEquals(560050,hazards1.getHazardPincode(),0);
        assertEquals("Inactive",hazards1.getHazardStatus());
        assertEquals(detectedTime,hazards1.getDetectedAt());
	}
	@Test
     void testNegativeGettersAndSetters() {
        Hazards hazards = new Hazards();
        hazards.setHazardId(-1); // ❌ Negative ID
        hazards.setUpdatedById(-2); // ❌ Negative UpdatedById
        hazards.setUpdatedByRole(null); // ❌ Null Role
        hazards.setHazardName(""); // ❌ Empty Name
        hazards.setHazardSeverity("Invalid"); // ❌ Invalid Severity
        hazards.setHazardLocation(""); // ❌ Empty Location
        hazards.setHazardPincode(-999999); // ❌ Invalid Pincode
        hazards.setHazardStatus("Unknown"); // ❌ Invalid Status
        LocalDateTime detectedTime = null; // ❌ Null Detected Time
        hazards.setDetectedAt(detectedTime);
 
        assertNotEquals(1, hazards.getHazardId()); // ❌ ID should not be valid
        assertNotEquals(2, hazards.getUpdatedById()); // ❌ UpdatedById should not be valid
        assertNull(hazards.getUpdatedByRole()); // ✅ Role should be null
        assertNotEquals("cloudy", hazards.getHazardName()); // ❌ Name should not match valid data
        assertNotEquals("High", hazards.getHazardSeverity()); // ❌ Severity should not match valid data
        assertNotEquals("Banashankari 1st Stage, Bengaluru, Karnataka", hazards.getHazardLocation()); // ❌ Location should not match
        assertNotEquals(560050, hazards.getHazardPincode(),0); // ❌ Pincode should not match valid data
        assertNotEquals("Inactive", hazards.getHazardStatus()); // ❌ Status should not match valid data
        assertNull(hazards.getDetectedAt()); // ✅ Detected time should be null
    }
 
    @Test
     void testNegativeToString()  {
        LocalDateTime detectedTime = null; // ❌ Null Timestamp
        Hazards hazards1 = new Hazards(-1, -2, null, "", "Invalid", "", -999999, "Unknown", detectedTime);
        String unexpectedResult = "Hazards [hazardId=-1, updatedById=-2, updatedByRole=null, hazardName=, hazardSeverity=Invalid, "
                + "hazardLocation=, hazardPincode=-999999, hazardStatus=Unknown, detectedAt=ditectedtime]";
 
        assertNotEquals(unexpectedResult, hazards1.toString()); // ❌ Should not match a valid toString format
    }
 
    @Test
     void testNegativeConstructor()  {
        LocalDateTime detectedTime = null; // ❌ Null Timestamp
        Hazards hazards1 = new Hazards(-1, -2, null, "", "Invalid", "", -999999, "Unknown", detectedTime);
 
        assertNotEquals(1, hazards1.getHazardId()); // ❌ ID should not be valid
        assertNotEquals(2, hazards1.getUpdatedById()); // ❌ UpdatedById should not be valid
        assertNull(hazards1.getUpdatedByRole()); // ✅ Role should be null
        assertNotEquals("cloudy", hazards1.getHazardName()); // ❌ Name should not match valid data
        assertNotEquals("High", hazards1.getHazardSeverity()); // ❌ Severity should not match valid data
        assertNotEquals("Banashankari 1st Stage, Bengaluru, Karnataka", hazards1.getHazardLocation()); // ❌ Location should not match
        assertNotEquals(560050, hazards1.getHazardPincode(),0); // ❌ Pincode should not match valid data
        assertNotEquals("Inactive", hazards1.getHazardStatus()); // ❌ Status should not match valid data
        assertNull(hazards1.getDetectedAt()); // ✅ Detected time should be null
    }	
}
 
 
 
 