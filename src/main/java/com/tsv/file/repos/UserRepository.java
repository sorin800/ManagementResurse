package com.tsv.file.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.tsv.file.model.User;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

	User findByEmail(String email);
	
	@Query(value = "SELECT email FROM user u", nativeQuery = true)
	List<String> getAllUserNames();


	

}
