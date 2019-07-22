package ar.edu.itba.ati.Model;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.lept;
import org.bytedeco.javacpp.tesseract;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.bytedeco.javacpp.lept.*;

public class Ocr {

    private tesseract.TessBaseAPI api;
    private int counter = 0;


    public Ocr() {
        api = new tesseract.TessBaseAPI();

        // Initialize tesseract-ocr with English, without specifying tessdata path
        if (api.Init(".", "ENG") != 0) {
            System.err.println("Could not initialize tesseract.");
            System.exit(1);
        }

        api.SetPageSegMode(1);
        api.SetPageSegMode(7); // 7 is single line, 10 is single char

        String whiteList = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        api.SetVariable("tessedit_char_whitelist", whiteList);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        api.End();
    }

    public String getText(Image image) {
        BytePointer outText;

        // Algo ?
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        try {
//            ImageIO.write(image.getBufferedImage(), "png", baos);
//        } catch (IOException e) {
//            System.out.println("Unable to run tesseract");
//            return "";
//        }
//        byte[] imageBytes = baos.toByteArray();
//        ByteBuffer byteBuffer = ByteBuffer.wrap(imageBytes);
//        lept.PIX pixImage = pixReadMem(byteBuffer, imageBytes.length);

        image.saveImage(new File("./outImages/temp/temp.jpg"));
        PIX pixImage = pixRead("./outImages/temp/temp.jpg");

        api.SetImage(pixImage);

        // Get OCR result
        outText = api.GetUTF8Text();
        String string = outText.getString();

        outText.deallocate();
        pixDestroy(pixImage);

        // debug
        image.saveImage(new File("./outImages/temp/" + counter++ + "letter" + string.trim() + ".jpg"));

        return string;
    }

}
