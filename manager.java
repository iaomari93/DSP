import com.fasterxml.jackson.annotation.JsonAlias;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import software.amazon.awssdk.services.sqs.model.Message;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class manager {
    private static EC2 ec2 = new EC2();
    private static S3 s3 = new S3();
    private static SQS sqs = new SQS();

    public static void main(String[] args) {
        String bucketName = null; //localID
        long n = 5;
        boolean terminate = false;

        ExecutorService pool = Executors.newFixedThreadPool(5);
        while(!terminate){
            String[] arg = new String[3];
            Message msg = sqs.getMsgD("l2m","new task");
            if(msg!=null){
                arg = messageParser.l2mParser(msg.body());
                bucketName = arg[0];
                n = Long.parseLong(arg[1]);
                terminate = Boolean.parseBoolean(arg[2]);
                String inputFile = s3.getObjectBytes("input.txt",bucketName);
                int msgNum = sendMsgToWorkers(inputFile,bucketName);
                System.out.println("msg number = "+msgNum);
                int runningWorkers = ec2.instanceWithTag("worker");
                int workerNum = msgNum/(int)n - runningWorkers;
//                initWorkers(workerNum);
//                managerHandler handler = new managerHandler(msgNum,bucketName);
//                pool.execute(handler);
                try {
                    handler(msgNum,bucketName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        checkIfWorkersDone();
        terminateManager();
        terminateWorkers();

    }




    public static void handler(int msgNum, String bucketName) throws IOException {
        System.out.println("manager handler started");
        int counter=0;
        File file = new File("summary.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
        String summary = null;
        while(counter < msgNum) {
            Message msg = sqs.getMsg("w2m", 1);
            if (msg != null) {
                String arg[] = new String[3];
                arg = messageParser.w2mParser(msg.body());
                if (arg[0].equals(bucketName)) {
                    sqs.deleteMessage("w2m", msg);
                    counter++;
                    summary += arg[1] + " " + arg[2] + "^&*";
                }
            }
        }
            writer.write(summary);
            writer.close();
            System.out.println(summary);
            s3.uploadFile(file,bucketName);
            System.out.println("Summary file uploaded");
            sqs.sendMessage("m2l",bucketName);
            System.out.println("Done task");


    }


    private static void terminateManager() {
        ec2.terminateInstances("manager");
    }

    private static void terminateWorkers() {
        ec2.terminateInstances("worker");
    }
    private static void checkIfWorkersDone() {
    }
//    private static void sendSummaryFile(int msgNum,long localId) {
//        int count = 0;
//        JSONParser parser = new JSONParser();
//        JSONObject jsonMsg = null;
//        FileWriter summary = null;
//        File file = new File("summary.txt");
//        try {
//            summary = new FileWriter(file.getAbsoluteFile());
//            while(count != msgNum) {
//                Message msg = sqs.getMsg("w2m", 1);
//                jsonMsg = (JSONObject) parser.parse(msg.body());
//                if (jsonMsg.get("msg type").equals("done OCR task") && jsonMsg.get("local id").equals(localId)) {
//                    summary.write(jsonMsg.get("url").toString() +" "+ jsonMsg.get("text").toString());
//                }
//                sqs.deleteMessage("w2m", msg);
//            }
//            summary.close();
//            s3.uploadFile(file);
//        } catch (IOException | ParseException e) {
//            e.printStackTrace();
//        }
//
//    }

    private static String[] initWorkers(int num) {
        String instanceIds[] = new String[num];
        for(int i = 0; i<num;i++){
            String worker = "worker";
            String script =
                    "#!/bin/bash\n"
                            + "wget https://ironman321.s3.amazonaws.com/worker.jar  -O /home/ubuntu/worker.jar\n"
                            + "java -jar /home/ubuntu/worker.jar > /home/ubuntu/"+worker+".log 2>&1\n";
            instanceIds[i] = ec2.createEC2Instance(worker);
        }
        return instanceIds;
    }

    private static int sendMsgToWorkers(String inputFile,String id) {
        String lines[] = inputFile.split("\n");
        int key = 0;
        for(String line : lines) {
            JSONObject obj = new JSONObject();
            obj.put("type","new image task");
            obj.put("id",id);
            obj.put("key",key++);
            obj.put("url",line);
//            sqs.sendMessage("m2w",obj.toString());
            sqs.sendMessage("m2w",id+" "+line);
            System.out.println("send message"+line);
        }
        return lines.length;
    }



}
