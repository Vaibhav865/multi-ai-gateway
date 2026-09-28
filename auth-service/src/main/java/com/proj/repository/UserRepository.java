/**
 * 
 */
package com.proj.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proj.model.User;
import java.util.List;



public interface UserRepository extends JpaRepository<User,Integer> {
	
	Optional<User> findByEmail(String email);
}
