package com.tsv.file.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class Permissions extends AbstractEntity {

	private String path;
	private String permissions;

	@ManyToOne
	private User user;

	public Permissions(String path, String permissions) {
		this.path = path;
		this.permissions = permissions;
	}

	public Permissions(String path, String permissions, User user) {
		this.path = path;
		this.permissions = permissions;
		this.user = user;
	}

	public Permissions() {
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPermissions() {
		return permissions;
	}

	public void setPermissions(String permissions) {
		this.permissions = permissions;
	}

	@Override
	public String toString() {
		return "Permissions [path=" + path + ", permissions=" + permissions + "]";
	}

}
