package com.tsv.file.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.tsv.file.exception.FileStorageException;
import com.tsv.file.exception.MyFileNotFoundException;
import com.tsv.file.property.FileStorageProperties;

@Service
public class FileStorageService {

    private Path fileStorageLocation;

    
/*    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir(SecurityContextHolder.getContext().getAuthentication().getName()))
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }*/

    public String storeFile(MultipartFile file, Path targetLocation) {
        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            File newFile = new File(targetLocation.toString());
            newFile.getAbsoluteFile().mkdirs();
            Files.copy(file.getInputStream(), targetLocation.resolve(file.getOriginalFilename()));

            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(Path targetLocation) {
        try {
            /*Path filePath = this.fileStorageLocation.resolve(fileName).normalize();*/
//            Resource resource = new UrlResource(targetLocation.toUri());
//        	Resource resource = new UrlResource(targetLocation.toString());
//        	System.out.println(targetLocation.toString());
            Path filePath = this.fileStorageLocation.resolve(targetLocation).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("File not found " + targetLocation.toString());
            }
        } catch (MalformedURLException ex) {
            throw new MyFileNotFoundException("File not found " + targetLocation.toString(), ex);
        }
    }
}
