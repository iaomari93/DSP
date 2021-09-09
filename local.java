
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import software.amazon.awssdk.services.sqs.model.Message;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;


public class local {
    private static EC2 ec2 = new EC2();
    private static S3 s3 = new S3();
    private static SQS sqs = new SQS();

    private static String inputFileName = "input";
    private static String outputFileName = "output";
    private static int n = 5;
    private static boolean terminate = false;


    public static void main(String[] args) {
//        getArgs(args);
        long localId = System.currentTimeMillis();
        initSqs();
        checkManager();
        s3.createBucket(localId);
        s3.uploadObject(String.valueOf(localId),inputFileName+".txt");
        sendMessageToL2m(localId,n,terminate);
        waitForDoneTaskMessage(localId);
        System.out.println("workers are done");
        File output = new File(outputFileName+".html");
        createHtmlFile(String.valueOf(localId),output);
    }

    private static void waitForDoneTaskMessage(long localId) {
        System.out.println("waiting for manager to finish..");
        while(true){
            Message msg = sqs.getMsg("m2l",1);
            if(msg!=null) {
                if(msg.body().equals(String.valueOf(localId))){
                    sqs.deleteMessage("m2l", msg);
                    break;
                }
            }
        }
    }

    private static void sendMessageToL2m(long localId,int n,boolean terminate) {
        String bucketUrl =  "https://" + localId + ".s3.amazonaws.com/"+inputFileName+".txt";
        sqs.sendMessage("l2m",localId+" "+n+" "+terminate);
        System.out.println("send message to the manager to start working");
    }

    private static void checkManager() {
        String script = null;
        if(terminate==false){
             script =
                    "#!/bin/bash\n"
                            + "wget https://ironman321.s3.amazonaws.com/worker.jar -O /home/ubuntu/worker.jar\n"
                            + "java -jar /home/ubuntu/worker.jar "+n+" > /home/ubuntu/workerlog.txt 2>&1\n" ;
        }else {
             script =
                    "#!/bin/bash\n"
                            + "wget https://ironman321.s3.amazonaws.com/worker.jar -O /home/ubuntu/worker.jar\n"
                            + "java -jar /home/ubuntu/worker.jar "+n+" terminate"+" > /home/ubuntu/workerlog.txt 2>&1\n" ;
        }

        String state = ec2.state("manager");
        if(state.equals("stopped")){
            ec2.startInstance(ec2.instanceId("manager"));
        }else if(state.equals("does_not_exist")||state.equals("terminated")){
            ec2.createEC2Instance("manager",script);
        }
        System.out.println("manager instance is ready");
    }

    private static void getArgs(String[] args) {
        if (args.length < 3 || args.length > 4) {
            throw new IllegalArgumentException("wrong input format");
        }
        inputFileName = args[0];
        outputFileName = args[1];
        n = Integer.parseInt(args[2]);
        if (args.length == 4 && args[3].equals("terminate")) {
            terminate = true;
        }
    }


    private static File createHtmlFile(String bucketName,File outputt) {
        String summary = s3.getObjectBytes("summary.txt", bucketName);//bucketName
        JSONParser parser = new JSONParser();
        JSONObject obj = null;
        JSONObject obj2 = null;
        JSONArray msg = null;
        File file = new File("output.html");
        FileWriter output=null;
        try {
            output = new FileWriter(file.getAbsoluteFile());
            output.write("<html><body>");
            obj = (JSONObject) parser.parse(summary);
            msg = (JSONArray) obj.get("texts");
            Iterator<JSONObject> iterator = msg.iterator();
            while (iterator.hasNext()) {
                obj2 =iterator.next();
                output.write("<p><img src="+obj2.get("url")+"><br>"+obj2.get("text")+"</p>");
            }
            output.write("</body></html>");
            output.close();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return file;
    }

    private static void initSqs(){
        sqs.createQueue("l2m");
        sqs.createQueue("m2l");
        sqs.createQueue("m2w");
        sqs.createQueue("w2m");
        System.out.println("sqs queues are ready");
    }
}