package com.documentdb.benchmark.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.documentdb.benchmark.model.Demo;
import com.documentdb.benchmark.repository.DemoRepo;
import com.documentdb.benchmark.service.InsertService;

@RestController
public class DemoController {
	
	@Autowired
	private DemoRepo repository;
	
	@Autowired
	private InsertService service;
	
	private static final Logger logger = LoggerFactory.getLogger(DemoController.class);
	
	@PostMapping("/insert")
	public String saveData(@RequestBody Demo demo) {
		repository.save(demo);
		return "Added data with ID: " + demo.getId();
	}
	
	@GetMapping("/")
	public List<Demo> getData() {
		return repository.findAll();
	}
	
	@GetMapping("/{id}")
	public Optional<Demo> getDataByID(@PathVariable int id) {
		return repository.findById(id);
	}
	
	@PostMapping("/insertData")
    public ResponseEntity<String> insertData(@RequestParam("file") MultipartFile jsonFile) throws IOException {
		if (jsonFile.isEmpty()) {
            return new ResponseEntity<>("File is empty", HttpStatus.BAD_REQUEST);
        }

        try {
            byte[] bytes = jsonFile.getBytes();
            String jsonString = new String(bytes);
            service.insertDataFromJsonFiles(jsonString);
            return new ResponseEntity<>("Data inserted successfully", HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error processing the file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
	
	@PostMapping("/insertDataParallel")
	public ResponseEntity<String> insertDataP(@RequestParam("file") MultipartFile jsonFile) throws IOException {
	    if (jsonFile.isEmpty()) {
	        return new ResponseEntity<>("File is empty", HttpStatus.BAD_REQUEST);
	    }

	    try {
	        byte[] bytes = jsonFile.getBytes();
	        String jsonString = new String(bytes);
	        service.insertDataFromJsonFilesP(jsonString);
	        return new ResponseEntity<>("Data inserted successfully", HttpStatus.OK);
	    } catch (IOException e) {
	        e.printStackTrace();
	        return new ResponseEntity<>("Error processing the file", HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
}
