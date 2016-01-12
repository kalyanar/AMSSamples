package com.adobe.ams;

import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.apache.commons.lang.time.StopWatch;

import java.io.File;

public class Main {
    private static String bucketName     = "kalyanar";
    private static String keyName        = "crx-quickstart.zip";
    private static String uploadFileName =
            "/Users/kalyanar/cqserver/6.1/author/crx-quickstart.zip";

    public static void main(String[] args) {
	// write your code here
        AmazonS3Client s3client = new AmazonS3Client(new ProfileCredentialsProvider
                ("turboprofile"));
        StopWatch stopWatch = new StopWatch();

        System.out.println("Uploading a new object to S3 from a file\n");
        s3client.setSignerRegionOverride(Regions.EU_WEST_1.getName());
     //   ((AmazonWebServiceClient)s3client).setSignerRegionOverride(Regions);
        File file = new File(args[0]);
        s3client.setEndpoint("s3-accelerate.amazonaws.com");
        stopWatch.start();
        s3client.putObject(new PutObjectRequest(
                bucketName, args[1], file));
        stopWatch.stop();
        System.out.println("Uploaded a new object to S3 from a file in \n"+
                stopWatch.getTime() +" milliseconds");
    }
}
