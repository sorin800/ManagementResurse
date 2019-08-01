package com.tsv.file.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tsv.file.model.Permissions;
import com.tsv.file.repos.PermissionsRepository;

@Service
public class PermissionsService {
	@Autowired
	private PermissionsRepository permissionsRepository;

	public void addPermission(Permissions permission) {
		permissionsRepository.save(permission);
	}
	
	public List<Permissions> findByUserId(Long userId){
		return permissionsRepository.findByUserId(userId);
	}

}
