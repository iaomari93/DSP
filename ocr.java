import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;


public class ocr {
    public ocr(){

    }

    public static Tesseract getTesseract() {
        Tesseract instance = new Tesseract();
        instance.setDatapath("//usr//share//tesseract-ocr//4.00//tessdata");
//        instance.setLanguage("eng");
//        instance.setHocr(true);
        return instance;
    }

    public static String getImageType(String url){
        int dot = url.lastIndexOf(".");
        String type = url.substring(dot+1);
        return type;
    }

    public static String runOcr(String urlString) {
//        System.out.println(getImageType("https://i.stack.imgur.com/WiDpa.jpg"));
        URL url = null;
        BufferedImage image = null;
        File file = null ;
        Tesseract tesseract = null;
        String result = null;
        String type = getImageType(urlString);

        try {
            url = new URL(urlString);
            image = ImageIO.read(url);
            if (image == null){
                return "Image doesn't exist";
            }
            file = new File("image."+type);
            ImageIO.write(image, type, file);
            tesseract = getTesseract();
            result = tesseract.doOCR(file);
        } catch (TesseractException | IOException e) {
            return "Image doesn't exist";
        }
        System.out.println(result);
        return result;
    }
}
