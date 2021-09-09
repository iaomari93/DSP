import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import software.amazon.awssdk.services.sqs.model.Message;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class managerHandler implements Runnable{
    String bucketName;
    int msgNum;
    private static EC2 ec2 = new EC2();
    private static S3 s3 = new S3();
    private static SQS sqs = new SQS();

    public managerHandler(int msgNum, String bucketName) {
        this.bucketName=bucketName;
        this.msgNum=msgNum;
    }

    @Override
    public void run() {
        handler(this.msgNum,this.bucketName);
    }


    private void handler(int msgNum, String bucketName) {
            System.out.println("manager handler started");
            int counter=0;
            File file = new File("summary.txt");
            FileWriter summary=null;
            while(counter < 15) {
                Message msg = sqs.getMsg("w2m", 1);
                if (msg != null) {
                    JSONParser parser = new JSONParser();
                    JSONObject jsonMsg = null;
                    JSONArray jsonArray = new JSONArray();
                    JSONObject obj = new JSONObject();
                    JSONObject obj2 = new JSONObject();
                    try {
                        summary = new FileWriter(file.getAbsoluteFile());
                        jsonMsg = (JSONObject) parser.parse(msg.body());
                        if (jsonMsg.get("id").equals(bucketName)) {
                            sqs.deleteMessage("w2m", msg);
                            counter++;
                            obj.put("url",jsonMsg.get("url").toString());
                            obj.put("text",jsonMsg.get("text").toString());
                            jsonArray.add(obj);
                            obj2.put("texts",jsonArray);
                            summary.write(obj2.toJSONString());

                        }
                    } catch (ParseException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                summary.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            s3.uploadFile(file,bucketName);
            JSONObject msg = new JSONObject();
            msg.put("type","done task");
            msg.put("id",bucketName);
//        sqs.sendMessage("m2l",msg.toString());
        sqs.sendMessage("m2l",bucketName);
    }


}
