import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQS {
    public static SqsClient sqsClient;

    public SQS(){
        sqsClient  = SqsClient.builder()
                .region(Region.US_EAST_1)
                .build();
    }

    public static String createQueue(String queueName ) {
        Map<QueueAttributeName,String> att = new HashMap<QueueAttributeName,String>();
        att.put(QueueAttributeName.VISIBILITY_TIMEOUT, "30");
        try {
            CreateQueueRequest request = CreateQueueRequest.builder()
                    .queueName(queueName)
//                    .attributes(att)
                    .build();
            sqsClient.createQueue(request);
        } catch (QueueNameExistsException e) {

        }
        return "";
    }

    public void sendMessage(String queueName,String msg) {
        String queueUrl = getQueueUrl(queueName);
        try {
            SendMessageRequest request = SendMessageRequest.builder().queueUrl(queueUrl).messageBody(msg).build();
            sqsClient.sendMessage(request);
        } catch (SqsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }


    public List<Message> receiveMessages( String queueName,int maxNumMsgs) {
        String queueUrl = getQueueUrl(queueName);
        try {
            ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(maxNumMsgs) //TODO maxNumMsgs
//                    .waitTimeSeconds(10)
//                    .visibilityTimeout(10)
                    .build();
            List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).messages();
            return messages;
        } catch (SqsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        return null;
    }


    private static String getQueueUrl(String queueName) {
        String queueUrl = "https://sqs.us-east-1.amazonaws.com/149796790376/"+queueName;
        return  queueUrl;
    }



    public Message getMsg(String queueName, int MaxNumMsgs){
        List<Message> messages = receiveMessages(queueName,MaxNumMsgs);
        if(messages.size()>0){
            return messages.get(0);
        }
        return null;
    }

    public Message getMsgD(String queueName, String type) {
        JSONParser parser = new JSONParser();
        List<Message> messages = receiveMessages(queueName,1);
        if(messages.size()>0){
            Message msg = messages.get(0);
            deleteMessage(queueName,msg);
            return msg;
        }
        return null;
    }


    public void deleteMessage(String queueName,Message message){

        String queueUrl = getQueueUrl(queueName);
        try {
                DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .receiptHandle(message.receiptHandle())
                        .build();
                sqsClient.deleteMessage(deleteMessageRequest);

        } catch (SqsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }




}
