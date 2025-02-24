package com.brillio.sts.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
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
import com.brillio.sts.exception.AccountNotFoundException;
import com.brillio.sts.exception.InvalidReassignmentException;
import com.brillio.sts.exception.TicketAlreadyExistsException;
import com.brillio.sts.exception.TicketNotFoundException;
import com.brillio.sts.exception.UnauthorizedEngineerException;
import com.brillio.sts.model.Connections;
import com.brillio.sts.model.Constants;
import com.brillio.sts.model.Tickets;
import com.brillio.sts.repo.TicketsRepository;
import com.brillio.sts.service.TicketsService;
import com.fasterxml.jackson.databind.ObjectMapper;

@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")

@RestController
@RequestMapping(value="/tickets")
public class TicketsController {
	
	private final TicketsService ticketService;
	private final TicketsRepository ticketsRepository;

	public TicketsController(TicketsService ticketService,
			TicketsRepository ticketsRepository) {
	    this.ticketService = ticketService;
	    this.ticketsRepository = ticketsRepository;
	}

	
	@GetMapping("/pending/{pincode}")
	public ResponseEntity<List<Tickets>> getPendingTicketsByPincode(@PathVariable int pincode) {
	    try {
	        List<Tickets> tickets = ticketService.getPendingTicketsByPincode(pincode);
	        return ResponseEntity.ok(tickets);
	    } catch (Exception e) {
	        return ResponseEntity.badRequest().body(Collections.emptyList());
	    }
	}


	
	@GetMapping("/user/{userId}")
	public ResponseEntity<List<Tickets>> getUserTickets(@PathVariable int userId) {
	    try {
	        List<Tickets> tickets = ticketService.getTicketsByUserId(userId);
	        return ResponseEntity.ok(tickets);
	    } catch (Exception e) {
	        return ResponseEntity.badRequest().body(Collections.emptyList());
	    }
	}

	
	@GetMapping("/getByEngineerId/{engineerId}")
    public List<Tickets> getTicketsByEngineerId(@PathVariable int engineerId) {
        return ticketService.getTicketsByEngineerId(engineerId);
    }
	@GetMapping("/getByEngineerIds/{engineerId}")
    public List<Tickets> getTicketsByEngineerIds(@PathVariable int engineerId) {
        return ticketService.getTicketsByEngineerIds(engineerId);
    }
		
	@PutMapping("/updateStatus/{ticketId}")
    public ResponseEntity<String> updateTicketStatus(
            @PathVariable int ticketId,
            @RequestParam int engineerId,
            @RequestParam String status) {
        try {
            ticketService.updateTicketStatus(ticketId, engineerId, status);
            return ResponseEntity.ok("Ticket status updated successfully");
        } catch (TicketNotFoundException | UnauthorizedEngineerException e) {
            return ResponseEntity.status(403).body( e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
	
	@PostMapping("/raiseInstallationTicket")
    public ResponseEntity<String> raiseInstallationTicket(@RequestBody Map<String, Object> requestData) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Connections connection = objectMapper.convertValue(requestData.get("connection"), Connections.class);
            Tickets ticket = objectMapper.convertValue(requestData.get("ticket"), Tickets.class);
            String response = ticketService.raiseTicketInstallation(connection, ticket);
            return ResponseEntity.ok(response);
        } catch (AccountNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
	
	@GetMapping("/getBestEngineer/{ticketId}")
	  public ResponseEntity<Map<String, Double>> getBestEngineerLocation(@PathVariable int ticketId) {
			Map<String, Double> response = ticketService.getBestEngineerLocation(ticketId);
			  return ResponseEntity.ok(response);
	}
	
	
	
	@GetMapping(value = "/searchTicket/{id}")
	public ResponseEntity<Tickets> getById(@PathVariable int id) {
	    try {
	        Optional<Tickets> ticketOptional = ticketsRepository.findById(id);
	        if (ticketOptional.isPresent()) {
	            return new ResponseEntity<>(ticketOptional.get(), HttpStatus.OK);
	        } else {
	            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	        }
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
 
	
	@PostMapping("/raiseTicketsFault")
    public ResponseEntity<String> raiseTicketsFault(@RequestBody Tickets ticket) {
        try {
            String response = ticketService.raiseTicketFault(ticket);
            return ResponseEntity.ok(response);
        } catch (TicketAlreadyExistsException | AccountNotFoundException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
	
	@GetMapping("/deferred/{pincode}")
    public List<Tickets> getDeferredTicketsByPincode(@PathVariable int pincode) {
        return ticketService.getDeferredTicketsByPincode(pincode);
    }

	@GetMapping("/rejected/{pincode}")
    public List<Tickets> getRejectedTicketsByPincode(@PathVariable int pincode) {
        return ticketService.getRejectedTicketsByPincode(pincode);
    }
	
	 @PutMapping("/reassign/{ticketId}")
     public ResponseEntity<String> reassignEngineer(@PathVariable int ticketId) {
         try {
             String result = ticketService.reassignEngineer(ticketId);
             return ResponseEntity.ok(result);
         } catch (TicketNotFoundException | InvalidReassignmentException e) {
             return ResponseEntity.status(400).body(e.getMessage());
         } catch (AccountNotFoundException e) {
             return ResponseEntity.status(404).body(e.getMessage());
         } catch (Exception e) {
             return ResponseEntity.badRequest().body(e.getMessage());
         }
     }
	 
	 @GetMapping("userTiketHistory/{userId}")
	    public List<Tickets> getCompletedOrFailedTicketsUser(@PathVariable int userId) {
	        return ticketService.getCompletedOrFailedTicketsByUser(userId);
	    }
	 @GetMapping("engineerTiketHistory/{engineerId}")
	    public List<Tickets> getCompletedOrFailedTicketsEngineer(@PathVariable int engineerId) {
	        return ticketService.getCompletedOrFailedTicketsByEngineer(engineerId);
	    }
	 @GetMapping("adminTiketHistory/{pincode}")
	    public List<Tickets> getCompletedOrFailedTicketsAdmin(@PathVariable int pincode) {
	        return ticketService.getCompletedOrFailedTicketsByAdmin(pincode);
	    }
	
	 @GetMapping("/pendingCount/{pincode}")
	    public Long countPendingTickets( @PathVariable int pincode) {
	        return ticketService.countByStatusAndPincode(Constants.PENDING, pincode);
	    }

	    @GetMapping("/inprogressCount/{pincode}")
	    public Long countInProgressTickets(@PathVariable int pincode) {
	    	return ticketService.countByStatusAndPincode(Constants.IN_PROGRESS, pincode);
	    }

	    @GetMapping("/deferredCount/{pincode}")
	    public Long countDeferredTickets(@PathVariable int pincode) {
	    	return ticketService.countByStatusAndPincode(Constants.DEFERRED, pincode);
	    }

	    @GetMapping("/rejectedCount/{pincode}")
	    public Long countRejectedTickets(@PathVariable int pincode) {
	    	return ticketService.countByStatusAndPincode(Constants.REJECTED, pincode);
	    }
	    
	    @GetMapping("/failedCount/{pincode}")
	    public Long countFailedTickets(@PathVariable int pincode) {
	    	return ticketService.countByStatusAndPincode(Constants.FAILED, pincode);
	    }
	    
	    @GetMapping("/totalCount/{pincode}")
	    public long totalCount(@PathVariable int pincode) {
	        return ticketService.getTotalTicketCount(pincode);
	    }
	    
	    @GetMapping("/pendingCountTickets/{engineerId}")
	    public int getPendingTicketCount(@PathVariable Long engineerId) {
	        return ticketService.getPendingTicketCount(engineerId);
	    }
	

}
