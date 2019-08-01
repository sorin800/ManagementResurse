package com.tsv.file.controllers;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.tsv.file.model.PermissionType;
import com.tsv.file.model.Permissions;
import com.tsv.file.model.User;
import com.tsv.file.payload.UploadFileResponse;
import com.tsv.file.service.FileStorageService;
import com.tsv.file.service.PermissionsService;
import com.tsv.file.service.UserDetailsServiceImpl;

@RestController
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private PermissionsService permissionsService;

    @Autowired
    private UserDetailsServiceImpl userService;
    
    private List<String> returnedFilesRecursive = new ArrayList<>();

    @PostMapping("/uploadFile")
    public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("path") String path) {
        logger.info("Path where the file will be saved {}", path);
    	String getNameUntilCharAt = SecurityContextHolder.getContext().getAuthentication().getName().toString().split("\\@")[0];
    	Path storageLocation = Paths.get("./uploads/", getNameUntilCharAt,"/", path);
    	System.out.println(storageLocation.toString());
        String fileName = fileStorageService.storeFile(file,storageLocation);
        System.out.println("PATH FISIER " + fileName);
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(fileName)
                .toUriString();
        return new UploadFileResponse(fileName, fileDownloadUri,
                file.getContentType(), file.getSize());
    }

    @PostMapping("/uploadMultipleFiles")
    public List<UploadFileResponse> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        return Arrays.asList(files)
                .stream()
                .map(file -> uploadFile(file, ""))
                .collect(Collectors.toList());
    }

    @GetMapping("/downloadFile/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
    	//String getNameUntilCharAt = SecurityContextHolder.getContext().getAuthentication().getName().toString().split("\\@")[0];
    	fileName = "C:\\Users\\Andrei\\Downloads\\filedemo2latestfinalsecondversion (1)\\filedemo2\\uploads\\a\\bb\\cc\\people.png";
        Path storageLocation = Paths.get(fileName);
    	//Path storageLocation = Paths.get(fileName);
        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(storageLocation);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.info("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
    
    @RequestMapping(value="/givePermission", method = RequestMethod.GET)
    @ResponseBody
    public String givePermission(@RequestParam String user, @RequestParam String filePath, @RequestParam PermissionType permission) {

        User newUser = userService.getUserByUsername(user);
        Permissions permissions = new Permissions(filePath, permission.name(), newUser);
        permissionsService.addPermission(permissions);

    	System.out.println(user);
    	System.out.println(filePath);
    	System.out.println(permission);
    	return "Cevasadasd";
    }



    @RequestMapping(value="/performAction", method = RequestMethod.GET)
    @ResponseBody
    public Object performAction(@RequestParam String fileName, @RequestParam String action) {
        org.springframework.security.core.userdetails.User auth = (org.springframework.security.core.userdetails.User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User userByUsername = userService.getUserByUsername(auth.getUsername());
        List<Permissions> permissionsByUserId = permissionsService.findByUserId(userByUsername.getId());
        //permissionsByUserId -> permisiunile din baza de date care au doar folder gen ./upload/test/a
        //fileName -> calea catre tot fisierul
        
        boolean foundElem = false;
        
        for (Permissions permission : permissionsByUserId) {
        	List<String> altaLista = displayDirectoryContents3(new File(permission.getPath()));
        	
        	for(String elem : altaLista) {
        		if(elem.contains(fileName)) {
        			if(permission.getPermissions().contains(action)) {
        				foundElem = true;
        				switch(action) {
        	            case "R":
                            System.out.println("A facut R");
                            return readFileAction(fileName);
//        	                break; // break is optional
        	            case "W":
        	            	System.out.println("A facut W");
                            return downloadFile(fileName);
//        	                break; // break is optional
        	            case "D":
        	                System.out.println("A facut D");
                            deleteFile(fileName);
        	                break; // break is optional
        	            default:
        	                // Statements
        				}
        				
        			}
        		}
        	}
            
        }
        
        if(foundElem == false) {
        	return "unsuccessful";
        }
        
        if(foundElem == true) {
        	foundElem = false;
        	return "success";
        }
        
        // pe baza la permissionsByUserId folosind o chestie dinaia recursiva incercam sa aflam o noua lista
        // de fisiere/foldere. parcurgem lista asta si vedem daca are elementul fileName prin eaș daca da e ok daca nu papa

        return "success";
    }

    private String readFileAction(String fileName) {
        Charset encoding = Charset.defaultCharset();
        byte[] encoded = new byte[0];
        try {
            encoded = Files.readAllBytes(Paths.get(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(encoded, encoding);
    }


    public List<String> displayDirectoryContents3(File dir3) {
		try {
			File[] files = dir3.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					displayDirectoryContents3(file);
				}else {
					returnedFilesRecursive.add(file.getCanonicalPath());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return returnedFilesRecursive;
	}

    private boolean deleteFile(String fileName) {
        File file = new File(fileName);
        return file.delete();
    }

    private String downloadFile(String fileName) {
    		byte[] array = null;
    		String base64Value = null;
    		
    		try {
				array = Files.readAllBytes(new File(fileName).toPath());
				base64Value = new String(Base64.getEncoder().encodeToString(array));
			} catch (IOException e) {
				e.printStackTrace();
			}
    		
        	return base64Value;
    }
    
}
