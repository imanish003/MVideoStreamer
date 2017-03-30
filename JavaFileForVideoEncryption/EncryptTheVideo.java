package Encryption;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Manish Menaria on 18-Mar-17.
 */
public class EncryptTheVideo {

    public static void main(String[] args) throws Exception {
        encrypt();
    }

    public static void encrypt() throws Exception {
        final byte[] buf = new byte[8192];
        final Cipher c = Cipher.getInstance("AES/CTR/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec("1234567890123456".getBytes(), "AES"), new IvParameterSpec(new byte[16]));

        //video.mp4 is will video which will be encrypted to AfterEncryption.mp4 file. which will then be stored at server and used
        //for streaming purpose
        final InputStream is = new FileInputStream("video.mp4");
        final OutputStream os = new CipherOutputStream(new FileOutputStream("AfterEncryption.mp4"), c);
        while (true) {
            int n = is.read(buf);
            if (n == -1) break;
            os.write(buf, 0, n);
        }
        os.close(); is.close();
    }

}
