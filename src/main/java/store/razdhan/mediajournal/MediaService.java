package store.razdhan.mediajournal;

import java.io.IOException;
import java.io.InputStream;
import java.util.OptionalLong;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

public class MediaService {
    private static final String DEFAULT_REGION = "us-east-2";
    private static final String BUCKET = "my--naveen-access-test-bucket";

    private final S3Client s3Client;

    public MediaService() {
        this(DEFAULT_REGION);
    }

    public MediaService(String region) {
        this.s3Client = S3Client.builder().region(Region.of(region)).build();
    }

    public StreamingMedia getMedia(String key, HttpServletRequest request, HttpServletResponse response) {
        try {
            HeadObjectResponse head = s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(BUCKET)
                    .key(key)
                    .build());

            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(GetObjectRequest.builder()
                    .bucket(BUCKET)
                    .key(key)
                    .build());

            long contentLength = OptionalLong.of(head.contentLength()).orElse(-1L);
            return new StreamingMedia(s3Object, contentLength);
        } catch (Exception ex) {
            return null;
        }
    }

    public static class StreamingMedia {
        private final InputStream inputStream;
        private final long contentLength;

        public StreamingMedia(InputStream inputStream, long contentLength) {
            this.inputStream = inputStream;
            this.contentLength = contentLength;
        }

        public InputStream getInputStream() {
            return inputStream;
        }

        public long getContentLength() {
            return contentLength;
        }

        public void close() throws IOException {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }
}
