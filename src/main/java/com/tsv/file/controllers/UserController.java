package com.tsv.file.controllers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.tsv.file.repos.PermissionsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.tsv.file.model.FileName;
import com.tsv.file.model.Permissions;
import com.tsv.file.model.Role;
import com.tsv.file.model.User;
import com.tsv.file.model.UserPermissions;
import com.tsv.file.repos.RoleRepository;
import com.tsv.file.repos.UserRepository;
import com.tsv.file.service.PermissionsService;
import com.tsv.file.service.SecurityService;

@Controller
public class UserController {
	
	@Autowired
	UserRepository userRepository;

	@Autowired
	PermissionsRepository permissionsRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private BCryptPasswordEncoder encoder;
	
	 @Autowired
	 private SessionRegistry sessionRegistry;
	 
	 @Autowired
	 private PermissionsService permissionsService;

	private List<String> list = new ArrayList<>();
	private List<String> list2 = new ArrayList<>();

	@RequestMapping("/showReg")
	public String showRegistrationPage() {
		return "register";
	}
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String getLogin(HttpServletRequest request) {
		
		HttpSession session = request.getSession(false);
		if (session == null) {
			System.out.println("A intrat aici");
			return "login";
		} else {
			if(SecurityContextHolder.getContext().getAuthentication().getName() == "anonymousUser") {
				return "login";
			}
			String auth = SecurityContextHolder.getContext().getAuthentication().getName();
			System.out.println(auth);
		    return "home";
		}
		
		
	}
	
	@RequestMapping(value = "/registerUser", method = RequestMethod.POST)
	public String register(@ModelAttribute("user") User user) {
		user.setPassword(encoder.encode(user.getPassword()));
		Role role = new Role();
		role.setName("USER");
		Set<Role> roles = new HashSet();
		roles.add(role);
		user.setRoles(roles);
		userRepository.save(user);
		
		//Save user default path access
		String userPathName = user.getEmail().toString().split("\\@")[0];
		Permissions permission = new Permissions("./uploads/" + userPathName, "RWD", user);
		permissionsService.addPermission(permission);
		
		
		//CREATE DIRECTORY
		String getNameUntilCharAt = user.getEmail().split("\\@")[0];
		String dirPath ="./uploads/" + getNameUntilCharAt;
		new File(dirPath).mkdir();
		
		return "login";
	}
	
	@RequestMapping(value = "/logoutUser", method=RequestMethod.POST)
	public String logout() {
		list.clear();
		list2.clear();
		return "login";
	}
	
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public String login(@RequestParam("email") String email, @RequestParam("password") String password, Model model) {
		try {
			boolean loginResponse = securityService.login(email, password);
			if (loginResponse) {
				User user = userRepository.findByEmail(email);

				//GET THE PATH PERMISSIONS OF THE LOGGED IN USER
				List<Permissions> pathPermissions = permissionsService.findByUserId(user.getId());
				List<UserPermissions> userPermissionsPath = new ArrayList<>();
				
				// SAVE USER PATH PERMISSIONS TO userPermissionsPath list 
				for(int i=0; i<pathPermissions.size(); i++) {
					userPermissionsPath.add(new UserPermissions(pathPermissions.get(i).getPath().toString()));
				}
				
				
				List<String> usernames = userRepository.getAllUserNames();
				List<String> permissions = permissionsRepository.getPermissionByUserId(user.getId());
				List<String> listOfDirectoriesAndFiles = new ArrayList<>();

				for(String permission : permissions ) {
					listOfDirectoriesAndFiles.addAll(displayDirectoryContents(new File(permission)));
				}

				for(int i=0; i<userPermissionsPath.size(); i++) {
					File folder = new File(permissions.get(i));
					displayDirectoryContents2(folder);
				}
				
				List<String> listaNoua = new ArrayList<>();
				for(int i=0; i<list2.size(); i++) {
					//listaNoua.add(list2.get(i).substring(1));
					listaNoua.add(list2.get(i));
				}
				
				System.out.println(usernames);
				model.addAttribute("usernames",usernames);
				model.addAttribute("fileNames", listaNoua);
				model.addAttribute("listOfDirectoriesAndFiles", listOfDirectoriesAndFiles);

				list2.clear();
				list.clear();

				return "home";
			} else {
				model.addAttribute("msg", "Invalid username or password.Please try again!");
			}
		} catch (Exception e) {
			model.addAttribute("msg", "Invalid username or password.Please try again!");
			e.printStackTrace();
		}
		return "login";
	}

	public List<String> displayDirectoryContents(File dir) {
		try {
			File[] files = dir.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					//System.out.println("directory:" + file.getCanonicalPath());
					list.add(file.getCanonicalPath());
					displayDirectoryContents(file);
				}
//				else {
//					//System.out.println("file:" + file.getCanonicalPath());
//					list.add(file.getCanonicalPath());
//				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	public List<String> displayDirectoryContents2(File dir2) {
		try {
			File[] files = dir2.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					displayDirectoryContents2(file);
				}else {
					System.out.println("file:" + file.getCanonicalPath());
					list2.add(file.getCanonicalPath());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list2;
	}

}
