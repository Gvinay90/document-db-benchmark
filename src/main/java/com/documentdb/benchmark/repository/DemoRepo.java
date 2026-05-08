package com.documentdb.benchmark.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.documentdb.benchmark.model.Demo;

public interface DemoRepo extends MongoRepository<Demo, Integer>{

}
