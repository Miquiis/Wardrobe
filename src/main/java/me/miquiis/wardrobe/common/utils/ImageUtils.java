package me.miquiis.wardrobe.common.utils;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class ImageUtils {

    public static byte[] createImageHash(File file) {
        try
        {
            HashCode md5 = Files.asByteSource(file).hash(Hashing.md5());
            return md5.asBytes();
        } catch (Exception e)
        {
            return null;
        }
    }

    public static String byteToHex(byte[] hexBytes)
    {
        return Hex.encodeHexString(hexBytes);
    }

    public static byte[] hexToBytes(String hex) throws DecoderException {
        return Hex.decodeHex(hex.toUpperCase().toCharArray());
    }

    public static byte[] createImageHash(byte[] fileBytes) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        return md.digest(fileBytes);
    }

    public static boolean checkImagesHashes(byte[] hashOne, byte[] hashTwo)
    {
        return Arrays.equals(hashOne, hashTwo);
    }

    public static float compareImage(File fileA, File fileB) {
        float percentage = 0;
        try {
            // take buffer data from both image files //
            BufferedImage biA = ImageIO.read(fileA);
            DataBuffer dbA = biA.getData().getDataBuffer();
            int sizeA = dbA.getSize();
            BufferedImage biB = ImageIO.read(fileB);
            DataBuffer dbB = biB.getData().getDataBuffer();
            int sizeB = dbB.getSize();
            int count = 0;
            // compare data-buffer objects //
            if (sizeA == sizeB) {

                for (int i = 0; i < sizeA; i++) {

                    if (dbA.getElem(i) == dbB.getElem(i)) {
                        count = count + 1;
                    }

                }
                percentage = (count * 100) / sizeA;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return percentage;
    }

}
