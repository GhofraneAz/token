package com.bookstore.springboot.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import com.bookstore.springboot.entity.User;




public interface UserRepository extends JpaRepository<User, Long> {
	public Optional<User> findByUsername(String username); 
    public Boolean existsByUsername(String username);
	public List<User> findAllByOrderByUsernameAsc();
	public Page<User> findAllByOrderByIdDesc(org.springframework.data.domain.Pageable pageable);
	public Page<User> findByUsernameContaining(String lib, org.springframework.data.domain.Pageable paging);
	
}