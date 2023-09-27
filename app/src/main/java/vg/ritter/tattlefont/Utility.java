package vg.ritter.tattlefont;

import java.io.FileInputStream;
import java.security.MessageDigest;

public class Utility {
    public static String CalculateFileHash(String filePath) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        FileInputStream fis = new FileInputStream(filePath);

        byte[] buffer = new byte[8192];
        int bytesRead;

        while ((bytesRead = fis.read(buffer)) != -1) {
            md.update(buffer, 0, bytesRead);
        }

        byte[] digest = md.digest();

        // Convert the byte array to a hexadecimal string
        StringBuilder hashBuilder = new StringBuilder();
        for (byte b : digest) {
            hashBuilder.append(String.format("%02X", b));
        }

        fis.close();

        return hashBuilder.toString();
    }
}
