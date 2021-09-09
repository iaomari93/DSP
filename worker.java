
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import software.amazon.awssdk.services.sqs.model.Message;

public class worker {

    private static SQS sqs = new SQS();

    public static void main(String[] args) {
        String localId = null;
        String urlString = null ;
        String text = null;
        Message msg = null;
        int c=0;

        while(true){
           msg = sqs.getMsgD("m2w","new image task");
           if(msg!=null){
               System.out.println("/////"+msg.body());
               String[] arg = messageParser.m2wParser(msg.body());
               localId = arg[0];
               urlString = arg[1];
               text = ocr.runOcr(urlString);
               sendMessageToW2m(urlString,text,localId,c++);
           }
           System.out.println(c);
        }
    }

    private static void sendMessageToW2m(String urlString,String text,String localId,int c) {
        sqs.sendMessage("w2m",localId+" "+urlString+" "+text+" "+c);
    }


}

