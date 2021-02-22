package com.nationwide.insights.api.exception;

import static java.lang.String.format;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(Long id) {
        super(format("Customer with id %d not found", id));
    }
}