package com.vehiclecontacting.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

//二维码生成工具类
@Slf4j
public class QRcodeUtils {

    private static final String CHARSET = "utf-8";

    //private static final String FORMAT_NAME = "JPG";

    //二维码尺寸
    private static final int QRCODE_SIZE = 300;

    //LOGO宽度
    private static final int WIDTH = 60;

    //LOGO高度
    private static final int HEIGHT = 60;

    public static BufferedImage createQRcode(String contents,String imgPath,boolean needCompress)throws Exception{
        Map<EncodeHintType,Object> map = new HashMap<>();
        //容错级别最高
        map.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        //设置字符编码
        map.put(EncodeHintType.CHARACTER_SET,CHARSET);
        //二维码空白区域
        map.put(EncodeHintType.MARGIN,1);

        //读取文件转换为字节数组
        BitMatrix bitMatrix = new MultiFormatWriter().encode(contents, BarcodeFormat.QR_CODE,QRCODE_SIZE,QRCODE_SIZE,map);
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        if (imgPath == null || "".equals(imgPath)) {
            return image;
        }
        // 插入图片
        QRcodeUtils.insertImage(image, imgPath, needCompress);
        return image;

    }

    private static void insertImage(BufferedImage source, String imgPath, boolean needCompress) throws Exception {
        if(imgPath == null || imgPath.equals("")){
            //没有图片url直接返回
            return;
        }
        Image src = ImageIO.read(new URL(imgPath));
        int width = src.getWidth(null);
        int height = src.getHeight(null);
        if (needCompress) { // 压缩LOGO
            if (width > WIDTH) {
                width = WIDTH;
            }
            if (height > HEIGHT) {
                height = HEIGHT;
            }
            Image image = src.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics g = tag.getGraphics();
            g.drawImage(image, 0, 0, null); // 绘制缩小后的图
            g.dispose();
            src = image;
        }
        // 插入LOGO
        Graphics2D graph = source.createGraphics();
        int x = (QRCODE_SIZE - width) / 2;
        int y = (QRCODE_SIZE - height) / 2;
        graph.drawImage(src, x, y, width, height, null);
        Shape shape = new RoundRectangle2D.Float(x, y, width, width, 6, 6);
        graph.setStroke(new BasicStroke(3f));
        graph.draw(shape);
        graph.dispose();
    }

}
