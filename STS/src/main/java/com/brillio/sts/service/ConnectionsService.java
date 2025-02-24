package com.brillio.sts.service;
 
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
 
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
 
import com.brillio.sts.exception.ConnectionNotFoundException;
import com.brillio.sts.model.Connections;
import com.brillio.sts.repo.ConnectionsRepository;
 
import jakarta.transaction.Transactional;
 
@Service
@Transactional
public class ConnectionsService {
    
    private static final Logger logger = Logger.getLogger(ConnectionsService.class);
    private final ConnectionsRepository connectionsRepository;
 
    public ConnectionsService(ConnectionsRepository connectionsRepository) {
        this.connectionsRepository = connectionsRepository;
    }
	
    public List<Connections> showConnections(){
        logger.info("Fetching all connections from database");
        return connectionsRepository.findAll();
    }
    public Connections searchById(int connectionId) {
        logger.info("Searching connection by ID: "+ connectionId);
        Optional<Connections> connection = connectionsRepository.findById(connectionId);
        if (!connection.isPresent()) {
            logger.error("Connection with ID  not found"+ connectionId);
            throw new ConnectionNotFoundException("Connection with ID " + connectionId + " not found.");
        }
        return connection.get();
    }
	
	
    public Connections addConnections(Connections connection) {
        logger.info("Adding new connection with User ID: "+ connection.getUserId());
        connection.setStatus("INACTIVE");
        Connections savedConnection = connectionsRepository.save(connection);
        logger.info("Connection added successfully with ID: "+ savedConnection.getConnectionId());
        return savedConnection;
    }
	
	public List<Connections> searchByUserId(int userId){
        logger.info("Fetching connections for User ID: "+ userId);
        return connectionsRepository.findByuserId(userId);
    }    
    
    public List<Connections> searchByUserAndStatus(int userId){
        logger.info("Fetching ACTIVE connections for User ID: "+ userId);
        return connectionsRepository.findByuserIdAndStatus(userId, "ACTIVE");
    }
    
    public LocalDateTime setExpiryDate(Connections connection) {
        if (connection.getStartDate() != null) {
            logger.info("Setting expiry date for connection ID: " + connection.getConnectionId());
            
            LocalDateTime expiryDateTime = connection.getStartDate().plusMonths(connection.getValidityPeriod());
            
            logger.info("Expiry date set to: " + expiryDateTime);
            return expiryDateTime;
        }
 
        // Log warning before returning null
        logger.warn("Start date is null for connection ID: " + connection.getConnectionId());
        return null;
    }
}