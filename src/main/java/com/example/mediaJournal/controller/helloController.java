package com.example.mediaJournal.controller;
import java.io.FileNotFoundException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;


import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.services.sts.model.AssumeRoleResponse;
import software.amazon.awssdk.services.sts.model.Credentials;

@RestController
public class helloController {
    private final S3Client s3Client;
    private final String s3BucketName;

    public helloController() {
        s3Client = S3Client.builder()
        .region(Region.US_EAST_2)
        .build();

        StsClient stsClient = StsClient.builder().region(Region.US_EAST_2).build();
        // AssumeRoleRequest assumeRoleRequest = AssumeRoleRequest.builder()
        //     .roleArn("arn:aws:iam::182643220141:policy/policy-for-my-naveen-access-test-bucket")
        //     .roleSessionName("mys3AccessSession")
        //     .build();
        
        // AssumeRoleResponse response = stsClient.assumeRole(assumeRoleRequest);
        // Credentials creds = response.credentials();
        s3BucketName = "my--naveen-access-test-bucket";
    }
    
    @GetMapping("/hello")
    public String sayHello() {
        return "Hello, Media Journal!";
    }

    @GetMapping("/image/{name}")
    public byte[] getImage(@PathVariable String name) {
        try{
            ClassPathResource resource = new ClassPathResource("static/images/JediTeam.png");
            InputStream fileInputStream = resource.getInputStream();            
            BufferedInputStream buf = new BufferedInputStream(fileInputStream);
            byte[] bytes = buf.readAllBytes();
            buf.close();
            return bytes;
        } catch(FileNotFoundException e){
            System.out.println(name + "not found" + e.getMessage());
        } catch(IOException io) {
            System.out.println("IO Exception: " + io.getMessage());
        } finally {
            System.out.println("Execution completed.");
        }
        return null;
    }

    @Value("file:./uploadedImages/")
    private Resource resource;

    @PostMapping(value="/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public boolean uploadImage(@RequestParam("fileName") String fileName, @RequestParam("file") MultipartFile file) throws IOException {
        File dir = resource.getFile();
        if(!dir.exists()){
            dir.mkdirs();
        }

        File uploadFile = new File(dir.getAbsolutePath() + File.separator + fileName);
        try {
            file.transferTo(uploadFile);
            s3Client.putObject(PutObjectRequest.builder()
            .bucket(s3BucketName)
            .key(fileName)
            .build(), RequestBody.fromFile(uploadFile));

        } catch (IOException e) {
            System.out.println("File upload failed: " + e.getMessage());
            return false;
        }   
        return true;
    }

    @GetMapping(value="/image/s3/{name}", produces=MediaType.IMAGE_PNG_VALUE)
    public byte[] getImageFromS3(@PathVariable String name) {
        try {
            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(
            GetObjectRequest.builder()
            .bucket(s3BucketName)
            .key(name)
            .build());
            BufferedInputStream buf = new BufferedInputStream(response);
            byte[] imageBytes = buf.readAllBytes();
            return imageBytes;
        } catch(IOException e){
            System.out.println(e.getMessage());
            return null;
        }
    }
}
