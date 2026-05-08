package com.documentdb.benchmark.service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.documentdb.benchmark.model.Demo;
import com.documentdb.benchmark.repository.DemoRepo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Service
public class InsertService {
	
	@Autowired
	private DemoRepo repository;
	
    private final Timer dataInsertionTimer;
    private final Counter failedInsertionCounter;
    
    private static final Logger logger = LoggerFactory.getLogger(InsertService.class);
	
	@Autowired
    public InsertService(DemoRepo repository, MeterRegistry meterRegistry) {
        this.repository = repository;
        this.dataInsertionTimer = Timer.builder("data.insertion.timer")
            .description("Timer for data insertion")
            .register(meterRegistry);

        this.failedInsertionCounter = Counter.builder("data.insertion.failed.counter")
            .description("Counter for failed data insertions")
            .register(meterRegistry);
    }
	
	public void insertDataFromJsonFiles(String json) {
		try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<Demo> demos = objectMapper.readValue(json, new TypeReference<List<Demo>>() {});
            
            for (Demo demo : demos) {
                //repository.save(demo);
                dataInsertionTimer.record(() -> {repository.save(demo);});
                // Additional processing or metrics recording can be added here
            }
        } catch (IOException e) {
        	logger.error("Error during data insertion: {}", e.getMessage());
            failedInsertionCounter.increment();
            // Handle the exception as needed
        }
    }
	
	public void insertDataFromJsonFilesP(String json) {
	    try {
	        ObjectMapper objectMapper = new ObjectMapper();
	        List<Demo> demos = objectMapper.readValue(json, new TypeReference<List<Demo>>() {});

	        int batchSize = 1000; // Adjust the batch size as needed

	        demos.parallelStream()
	             .collect(Collectors.groupingByConcurrent(demo -> demos.indexOf(demo) / batchSize))
	             .values()
	             .parallelStream()
	             .forEach(batch -> {
	                 dataInsertionTimer.record(() -> {
	                     repository.saveAll(batch);
	                 });
	                 // Additional processing or metrics recording can be added here
	             });
	    } catch (IOException e) {
	        logger.error("Error during data insertion: {}", e.getMessage());
	        failedInsertionCounter.increment();
	        // Handle the exception as needed
	    }
	}
}
