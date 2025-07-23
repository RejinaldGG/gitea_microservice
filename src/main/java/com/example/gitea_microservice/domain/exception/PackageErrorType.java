package com.example.gitea_microservice.domain.exception;

public enum PackageErrorType {
    INVALID_FORMAT,          
    MISSING_REQUIRED_FIELD,  
    SIGNATURE_VERIFICATION,  
    SIZE_LIMIT_EXCEEDED,     
    VERSION_CONFLICT,        
    UNSUPPORTED_ARCHITECTURE, UNSUPPORTED_PACKAGE_MANAGER, EMPTY_PACKAGE 
}