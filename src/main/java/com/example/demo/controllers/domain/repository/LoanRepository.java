package com.example.demo.controllers.domain.repository;

import com.example.demo.controllers.domain.entity.Loan;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface LoanRepository extends MongoRepository<Loan, String> {

    List<Loan> findByUser_UserId(String userId);

    Optional<Loan> findByUser_CardNum(String cardNum);

}
