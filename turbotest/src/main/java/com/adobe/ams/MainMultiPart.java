package com.adobe.ams;

import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import org.apache.commons.lang.time.StopWatch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainMultiPart {
    private static String bucketName     = "kalyanar";
    private static String keyName        = "crx-quickstart_multipart.zip";
    private static String uploadFileName =
            "/Users/kalyanar/sourcecode/turbotest/crx-quickstart.zip";

    public static void main(String[] args) {
        // write your code here
        AmazonS3Client s3client = new AmazonS3Client(new ProfileCredentialsProvider
                ("turboprofile"));
        StopWatch stopWatch = new StopWatch();
        List<PartETag> partETags = new ArrayList<PartETag>();
        InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(
                bucketName, args[1]);
        InitiateMultipartUploadResult initResponse =
                s3client.initiateMultipartUpload(initRequest);
        System.out.println("Uploading a new object to S3 from a file\n");
        s3client.setSignerRegionOverride(Regions.EU_WEST_1.getName());
        //   ((AmazonWebServiceClient)s3client).setSignerRegionOverride(Regions);
        File file = new File(args[0]);
        long contentLength = file.length();
        long partSize = 5 * 1024 * 1024; // Set part size to 5 MB.
        s3client.setEndpoint("s3-accelerate.amazonaws.com");
        stopWatch.start();
        long filePosition = 0;
        for (int i = 1; filePosition < contentLength; i++) {
            // Last part can be less than 5 MB. Adjust part size.
            partSize = Math.min(partSize, (contentLength - filePosition));
            // Create request to upload a part.
            UploadPartRequest uploadRequest = new UploadPartRequest()
                    .withBucketName(bucketName).withKey(args[1])
                    .withUploadId(initResponse.getUploadId()).withPartNumber(i)
                    .withFileOffset(filePosition)
                    .withFile(file)
                    .withPartSize(partSize);
            // Upload part and add response to our list.
            partETags.add(s3client.uploadPart(uploadRequest).getPartETag());
            filePosition += partSize;

        }
        // Step 3: Complete.
        CompleteMultipartUploadRequest compRequest = new
                CompleteMultipartUploadRequest(bucketName,
                args[1],
                initResponse.getUploadId(),
                partETags);

        s3client.completeMultipartUpload(compRequest);
        stopWatch.stop();
        System.out.println("Uploaded a new object to S3 from a file in \n"+
                stopWatch.getTime() +" milliseconds");
    }
}
