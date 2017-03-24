/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package com.example.ManishMenaria.myapplication.backend;


import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DHParameterSpec;
import javax.inject.Named;

/**
 * An endpoint class we are exposing
 */
@Api(
        name = "myApi",
        version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = "backend.myapplication.ManishMenaria.example.com",
                ownerName = "backend.myapplication.ManishMenaria.example.com",
                packagePath = ""
        )
)
public class MyEndpoint {

    @ApiMethod(name = "getPublicKey")
    public ResponseEntity getPublicKey(@Named("P") String P, @Named("G") String G, @Named("L") String L,@Named("appPublicKey") String appPublicKey) {
        //Step 3:  GCE uses the parameters supplied by App
        //		to generate a key pair and sends the public key

        KeyPairGenerator kpg = null;

        //Response to be returned to App
        ResponseEntity response=null;

        //Contains GCE public key
        byte gcePublicKey[];
        try {
            kpg = KeyPairGenerator.getInstance("DH");

            DHParameterSpec dhSpec = new DHParameterSpec(
                    new BigInteger(P), new BigInteger(G), Integer.parseInt(L));
            kpg.initialize(dhSpec);
            KeyPair kp = kpg.generateKeyPair();
            gcePublicKey = kp.getPublic().getEncoded();

            //Todo
            //Return the GCE public key

            // Step 5 part 1:  GCE uses his private key to perform the
            //		first phase of the protocol
            KeyAgreement ka = KeyAgreement.getInstance("DH");
            ka.init(kp.getPrivate());

            // Step 5 part 2:  GCE uses App's public key to perform
            //		the second phase of the protocol.
            KeyFactory kf = KeyFactory.getInstance("DH");

            //Getting Byte array from list
            byte[] appPublicKeyArr = com.google.appengine.repackaged.com.google.api.client.util.Base64.decodeBase64(appPublicKey);

            X509EncodedKeySpec x509Spec =
                    new X509EncodedKeySpec(appPublicKeyArr);
            //Todo Complete
            //getting public key of App

            PublicKey pk = kf.generatePublic(x509Spec);
            ka.doPhase(pk, true);

            // Step 5 part 3:  Bob generates the secret key
            byte secret[] = ka.generateSecret();

            // Step 6:  Bob generates a DES key
            SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
            DESKeySpec desSpec = new DESKeySpec(secret);
            SecretKey key = skf.generateSecret(desSpec);

            // Step 7:  GCE encrypts data with the key and sends
            //		the encrypted data to Bob
            Cipher c = Cipher.getInstance("DES/ECB/PKCS5Padding");
            c.init(Cipher.ENCRYPT_MODE, key);
            byte[] cipherURL = c.doFinal(
                    "https://optimist003.000webhostapp.com/AfterEncryption.mp4".getBytes());
            byte[] cipherKey = c.doFinal(
                    "1234567890123456".getBytes());

            //Adding data to be returned
            response = new ResponseEntity();
            response.setGcePublicKey(Base64.encodeBase64String(gcePublicKey));
            response.setCipherURL(Base64.encodeBase64URLSafeString(cipherURL));
            response.setCipherKey(Base64.encodeBase64String(cipherKey));


        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return response;
    }

}
