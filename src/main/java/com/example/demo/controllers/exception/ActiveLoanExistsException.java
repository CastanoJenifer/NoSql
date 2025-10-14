package com.example.demo.controllers.exception;

import com.example.demo.controllers.response.LoanSummaryResponse;

public class ActiveLoanExistsException extends RuntimeException {
    private final LoanSummaryResponse activeLoan;

    public ActiveLoanExistsException(String message, LoanSummaryResponse activeLoan) {
        super(message);
        this.activeLoan = activeLoan;
    }

    public LoanSummaryResponse getActiveLoan() {
        return activeLoan;
    }
}

