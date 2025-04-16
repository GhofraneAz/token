package com.bookstore.springboot.service;

public interface AuthService {
    String authenticate(String username, String password);
}