package com.simplesdental.product.exception;

public record ErrorResponse(
    String error,
    String message
) {} 
