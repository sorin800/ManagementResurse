package com.tsv.file.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tsv.file.model.Permissions;
@Repository
public interface PermissionsRepository extends JpaRepository<Permissions, Long>{

	public List<Permissions> findByUserId(Long userId);

	@Query(value = "SELECT path FROM permissions p where p.user_id= :userId ", nativeQuery = true)
	List<String> getPermissionByUserId(@Param("userId") Long userId);
	
}
