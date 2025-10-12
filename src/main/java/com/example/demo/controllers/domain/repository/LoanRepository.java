package com.example.demo.controllers.domain.repository;

import com.example.demo.controllers.domain.entity.Loan;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LoanRepository extends MongoRepository<Loan, String> {
}
