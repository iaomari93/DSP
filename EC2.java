import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.Base64;

public class EC2 {

    public EC2(){

    }

    public String state(String tagName){
        Region region = Region.US_EAST_1;
        Ec2Client ec2 = Ec2Client.builder()
                .region(region)
                .build();
        boolean done = false;
        String nextToken = null;
        try {
            do {
                DescribeInstancesRequest request = DescribeInstancesRequest.builder().maxResults(6).nextToken(nextToken).build();
                DescribeInstancesResponse response = ec2.describeInstances(request);
                for (Reservation reservation : response.reservations()) {
                    for (Instance instance : reservation.instances()) {
                        for(Tag tag : instance.tags()){
                            if(tag.key().equals(tagName)){
                                ec2.close();
                                return instance.state().name().toString();
                            }
                        }
                    }
                }
                nextToken = response.nextToken();
            } while (nextToken != null);

        } catch (Ec2Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        ec2.close();
        return "does_not_exist";
    }
    public int instanceWithTag(String tagName){
        int count = 0;
        Region region = Region.US_EAST_1;
        Ec2Client ec2 = Ec2Client.builder()
                .region(region)
                .build();
        boolean done = false;
        String nextToken = null;
        try {
            do {
                DescribeInstancesRequest request = DescribeInstancesRequest.builder().maxResults(6).nextToken(nextToken).build();
                DescribeInstancesResponse response = ec2.describeInstances(request);
                for (Reservation reservation : response.reservations()) {
                    for (Instance instance : reservation.instances()) {
                        if(instance.state().name().equals("running"))
                        for(Tag tag : instance.tags()){
                            if(tag.key().equals(tagName)){
                                ec2.close();
                                count++;
                            }
                        }
                    }
                }
                nextToken = response.nextToken();
            } while (nextToken != null);

        } catch (Ec2Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        ec2.close();
        return count;
    }

    public String instanceId(String tagName){
        Region region = Region.US_EAST_1;
        Ec2Client ec2 = Ec2Client.builder()
                .region(region)
                .build();
        boolean done = false;
        String nextToken = null;
        try {
            do {
                DescribeInstancesRequest request = DescribeInstancesRequest.builder().maxResults(6).nextToken(nextToken).build();
                DescribeInstancesResponse response = ec2.describeInstances(request);
                for (Reservation reservation : response.reservations()) {
                    for (Instance instance : reservation.instances()) {
                        for(Tag tag : instance.tags()){
                            if(tag.key().equals(tagName)){
                                ec2.close();
                                return instance.instanceId();
                            }
                        }
                    }
                }
                nextToken = response.nextToken();
            } while (nextToken != null);

        } catch (Ec2Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        ec2.close();
        return "";
    }
    public static void createEC2KeyPair(String keyName ) {
        Ec2Client ec2 = Ec2Client.builder()
                .region(Region.US_EAST_1)
                .build();

        try {
            CreateKeyPairRequest request = CreateKeyPairRequest.builder()
                    .keyName(keyName).build();

            CreateKeyPairResponse response = ec2.createKeyPair(request);
            System.out.printf(
                    "Successfully created key pair named %s",
                    keyName);

        } catch (Ec2Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }

    public  String createEC2Instance(String tagName,String script) {
        String amiId = "ami-0a441f9c993ec41b6";
        IamInstanceProfileSpecification role = IamInstanceProfileSpecification.builder().name("ec2-s3-sqs").build();
        Ec2Client ec2 = Ec2Client.builder()
                .region(Region.US_EAST_1)
                .build();

        RunInstancesRequest runRequest = RunInstancesRequest.builder()
                .imageId(amiId)
                .iamInstanceProfile(role)
                .instanceType(InstanceType.T2_MICRO)
                .userData(Base64.getEncoder().encodeToString(script.getBytes()))
                .maxCount(1)
                .minCount(1)
                .keyName("ibrahim")
                .build();

        RunInstancesResponse response = ec2.runInstances(runRequest);
        String instanceId = response.instances().get(0).instanceId();

        Tag tag = Tag.builder()
                .key(tagName)
                .value("")
                .build();

        CreateTagsRequest tagRequest = CreateTagsRequest.builder()
                .resources(instanceId)
                .tags(tag)
                .build();

        try {
            ec2.createTags(tagRequest);

            return instanceId;

        } catch (Ec2Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        return "";
    }
    public  String createEC2Instance(String tagName ) {
        String amiId = "ami-0a441f9c993ec41b6";
        IamInstanceProfileSpecification role = IamInstanceProfileSpecification.builder().name("ec2-s3-sqs").build();
        Ec2Client ec2 = Ec2Client.builder()
                .region(Region.US_EAST_1)
                .build();

        RunInstancesRequest runRequest = RunInstancesRequest.builder()
                .imageId(amiId)
                .iamInstanceProfile(role)
                .instanceType(InstanceType.T2_MICRO)
                .maxCount(1)
                .minCount(1)
                .build();

        RunInstancesResponse response = ec2.runInstances(runRequest);
        String instanceId = response.instances().get(0).instanceId();

        Tag tag = Tag.builder()
                .key(tagName)
                .value("")
                .build();

        CreateTagsRequest tagRequest = CreateTagsRequest.builder()
                .resources(instanceId)
                .tags(tag)
                .build();

        try {
            ec2.createTags(tagRequest);
            return instanceId;

        } catch (Ec2Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        // snippet-end:[ec2.java2.create_instance.main]
        return "";
    }

    public  void startInstance(String instanceId) {
        Ec2Client ec2 = Ec2Client.builder()
                .region(Region.US_EAST_1)
                .build();

        StartInstancesRequest request = StartInstancesRequest.builder()
                .instanceIds(instanceId)
                .build();

        ec2.startInstances(request);
        System.out.printf("Successfully started instance %s", instanceId);
    }


    public void terminateInstances(String tag){
//        TODO write terminateInstances
    }

}
