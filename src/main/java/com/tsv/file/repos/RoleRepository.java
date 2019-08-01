package com.tsv.file.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tsv.file.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {

}
