public class messageParser {
    public messageParser(){

    }
    public static String[] l2mParser(String msg){
        String args[] = msg.split(" ");
        return args;
    }

    public static String[] m2wParser(String msg){
        int space = msg.indexOf(" ");
        String[] args = new String[2];
        args[0] = msg.substring(0,space);
        args[1] = msg.substring(space+1);
        return args;
    }

    public static String[] w2mParser(String msg){
        String[] args = new String[3];
        int space = msg.indexOf(" ");
        args[0]=msg.substring(0,space);
        msg = msg.substring(space+1);
        space = msg.indexOf(" ");
        args[1] = msg.substring(0,space);
        msg = msg.substring(space+1);
        space = msg.indexOf(" ");
        args[2] = msg.substring(0,space);

        return args;
    }
}
