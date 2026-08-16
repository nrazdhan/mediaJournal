package store.razdhan.mediajournal.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;
import store.razdhan.mediajournal.MediaService;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import store.razdhan.mediajournal.MediaService.StreamingMedia;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@RestController
public class MediaController {
    private static final String DEFAULT_REGION = "us-east-2";
    private static final String BUCKET = "my--naveen-access-test-bucket";

    private final S3Client s3Client;
    private final MediaService mediaService;

    public MediaController() {
        this(DEFAULT_REGION);
    }

    public MediaController(String region) {
        this.s3Client = S3Client.builder().region(Region.of(region)).build();
        this.mediaService = new MediaService(region);
    }

    @GetMapping("/media/s3")
    public List<String> getImageList() {
        ListObjectsV2Response mediaList = s3Client.listObjectsV2(ListObjectsV2Request.builder()
                .bucket(BUCKET)
                .maxKeys(20)
                .build());

        List<String> lst = new ArrayList<>();
        mediaList.contents().stream().sorted((x, y)->y.lastModified().compareTo(x.lastModified())).forEach(x -> lst.add(x.key()));
        return lst;
    }

    //NOT USED IN APP, BUT CAN BE CALLED
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping("/image/local/{name}")
    public byte[] getLocalImage(@PathVariable String name) {
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

    // @GetMapping(value="/image/s3/{name}", produces=MediaType.IMAGE_PNG_VALUE)
    // public byte[] getImageFromS3(@PathVariable String name) {
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping("/media/s3/{key}")
    public org.springframework.core.io.Resource getImage(@PathVariable String key) {
        ResponseInputStream<GetObjectResponse> responseStream = s3Client.getObject(
            GetObjectRequest.builder()
            .bucket(BUCKET)
            .key(key)
            .build());
        
    //         BufferedInputStream buf = new BufferedInputStream(response);
    //         byte[] imageBytes = buf.readAllBytes();
    //         return imageBytes;
        return new org.springframework.core.io.InputStreamResource(responseStream);
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping("/media/s3/video/{key}")
    public void getVideo(@PathVariable String key, HttpServletRequest request, HttpServletResponse response) {
        StreamingMedia streamingMedia = mediaService.getMedia(key, request, response);
        if (streamingMedia == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        long contentLength = streamingMedia.getContentLength();
        response.setContentType(getContentType(key));
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Cache-Control", "public, max-age=31536000");
        response.setHeader("Content-Disposition", "inline; filename=\"" + key + "\"");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setBufferSize(64 * 1024);

        long start = 0;
        long end = contentLength > 0 ? contentLength - 1 : -1;
        String rangeHeader = request.getHeader("range");

        if (contentLength > 0 && rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            String range = rangeHeader.substring(6);
            String[] values = range.split("-");
            if (values.length > 0 && !values[0].isEmpty()) {
                start = Long.parseLong(values[0]);
            }
            if (values.length > 1 && !values[1].isEmpty()) {
                end = Long.parseLong(values[1]);
            }
            if (start > end || start >= contentLength) {
                response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                response.setHeader("Content-Range", "bytes */" + contentLength);
                return;
            }
            if (end >= contentLength) {
                end = contentLength - 1;
            }
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + contentLength);
            response.setContentLengthLong(end - start + 1);
        } else if (contentLength > 0) {
            response.setContentLengthLong(contentLength);
        }

        try (InputStream inputStream = streamingMedia.getInputStream(); OutputStream out = response.getOutputStream()) {
            if (contentLength > 0 && start > 0) {
                inputStream.skip(start);
            }
            byte[] buffer = new byte[8192];
            long remaining = contentLength > 0 ? end - start + 1 : Long.MAX_VALUE;
            while (remaining > 0) {
                int bytesRead = inputStream.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                if (bytesRead == -1) {
                    break;
                }
                out.write(buffer, 0, bytesRead);
                out.flush();
                remaining -= bytesRead;
            }
            out.flush();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to stream video", ex);
        }
    }

    private String getContentType(String key) {
        String extension = key.substring(key.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        return switch (extension) {
            case "mp4" -> "video/mp4";
            case "mov" -> "video/quicktime";
            case "webm" -> "video/webm";
            default -> "application/octet-stream";
        };
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
            .bucket(BUCKET)
            .key(fileName)
            .build(), RequestBody.fromFile(uploadFile));

        } catch (IOException e) {
            System.out.println("File upload failed: " + e.getMessage());
            return false;
        }   
        return true;
    }
}
