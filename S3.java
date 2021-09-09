import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.waiters.S3Waiter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;

public class S3 {
    public String bucketName;
    public S3(){

    }


    // Create a bucket by using a S3Waiter object
    // generate the bucket name from currentTimeMillis
    //return the bucket name
    public String createBucket(long id) {
        String name = String.valueOf(id);
        bucketName = name;
        Region region = Region.US_EAST_1;
        S3Client s3 = S3Client.builder()
                .region(region)
                .build();

        try {
            S3Waiter s3Waiter = s3.waiter();
            CreateBucketRequest bucketRequest = CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .createBucketConfiguration(
                            CreateBucketConfiguration.builder()
                                    .build())
                    .build();

            s3.createBucket(bucketRequest);
            HeadBucketRequest bucketRequestWait = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();

            // Wait until the bucket is created and print out the response
            WaiterResponse<HeadBucketResponse> waiterResponse = s3Waiter.waitUntilBucketExists(bucketRequestWait);
            waiterResponse.matched().response().ifPresent(System.out::println);
            System.out.println(bucketName +" is ready");
            return bucketName;

        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        return "";
    }
    public void uploadObject(String bucketName,String fileName )  {
        Region region = Region.US_EAST_1;
        S3Client s3 = S3Client.builder()
                .region(region)
                .build();
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        s3.putObject(objectRequest, RequestBody.fromFile(Paths.get("/home/ibrahim/IdeaProjects/untitled3/src/main/java/"+fileName)));
    }

    public void uploadFile(File file,String bucketName)  {
        String fileName = file.getName();
        Region region = Region.US_EAST_1;
        S3Client s3 = S3Client.builder()
                .region(region)
                .build();
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        s3.putObject(objectRequest, RequestBody.fromFile(file));
    }

    public String getObjectBytes(String keyName,String bucketName) {
        Region region = Region.US_EAST_1;
        S3Client s3 = S3Client.builder()
                .region(region)
                .build();
        ResponseInputStream<GetObjectResponse> s3objectResponse = s3.getObject(GetObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .build());
        BufferedReader reader = new BufferedReader(new InputStreamReader(s3objectResponse));
        String line = null;
        String file = "";
        while (true) {
            try {
                if ((line = reader.readLine()) == null) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            file += "\n" + line;
        }
//        System.out.println(file);
        return file;
    }



}
