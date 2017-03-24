package hnmn3.mechanic.optimist.mvideostreamer;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.example.manishmenaria.myapplication.backend.myApi.MyApi;
import com.example.manishmenaria.myapplication.backend.myApi.model.ResponseEntity;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.math.BigInteger;
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
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DHParameterSpec;

/**
 * Created by Manish Menaria on 18-Mar-17.
 */

public class FetchingVideoData extends AppCompatActivity{

    //Show the current status while fetching video data from server
    TextView tv_current_status;

    //Activity Context
    Context context;

    //G,P,L are the parameters used by Diffie-Hellman algorithm
    BigInteger P;
    BigInteger G;
    int L;

    /*This is an external encoded form for the key used when a standard representation of the key is
    needed outside the Java Virtual Machine, as when transmitting the key to some other party.
    The key is encoded according to a standard format (such as X.509 SubjectPublicKeyInfo or PKCS#8),
     and is returned using the getEncoded method.*/
    byte appPublicKey[];

    //String array to show current status
    String[] statusStrings = new String[6];


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fetching_data);

        tv_current_status = (TextView) findViewById(R.id.textView_status);

        context = this;

        statusStrings[0] = "generating a key pair";
        statusStrings[1] = "App is sending the public key and the Diffie-Hellman key parameters to GCE";
        statusStrings[2] = "App is performing the first phase of the protocol with it's private key";
        statusStrings[3] = "App is performing the second phase of the protocol with GCE public key";
        statusStrings[4] = "App is generating the secret key";
        statusStrings[5] = "App is receiving the encrypted text and decrypting it";

        new getDataFromServer().execute();
    }


    private class getDataFromServer extends AsyncTask<Void,Integer,Void> {

        private byte plainURL[],plainKey[];
        //Is data from server loaded succesfully?
        private boolean isDataLoaded=false;

        @Override
        protected Void doInBackground(Void... params) {

            KeyPairGenerator kpg = null;
            try {
                // Step 1:  App generates a key pair
                publishProgress(0);

                kpg = KeyPairGenerator.getInstance("DH");  //Throws NoSuchAlgorithmException
                kpg.initialize(512);
                KeyPair kp = kpg.generateKeyPair();

                // Step 2:  App sends the public key and the
                // Diffie-Hellman key parameters to GCE
                publishProgress(1);

                DHParameterSpec dhSpec = (
                        (DHPublicKey) kp.getPublic()).getParams();
                G = dhSpec.getG();
                P = dhSpec.getP();
                L = dhSpec.getL();
                appPublicKey = kp.getPublic().getEncoded();

                //Sending data to GCE
                MyApi myApi=null;
                if (myApi == null) {
                    MyApi.Builder builder = new MyApi.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                            .setRootUrl("https://videostreamer003.appspot.com/_ah/api/");
                    myApi = builder.build();
                }

                String listAppPublicKey = Base64.encodeBase64String(appPublicKey);

                ResponseEntity responseEntity = myApi.getPublicKey(P.toString(), G.toString(),512+"",listAppPublicKey).execute();

                publishProgress(2);
                // Step 4 part 1:  App performs the first phase of the
                //		protocol with it's private
                KeyAgreement ka = KeyAgreement.getInstance("DH");
                ka.init(kp.getPrivate());

                publishProgress(3);
                // Step 4 part 2:  App performs the second phase of the
                //		protocol with GCE public key
                byte gcePublicKey[];
                gcePublicKey = Base64.decodeBase64(responseEntity.getGcePublicKey());

                KeyFactory kf = KeyFactory.getInstance("DH");
                X509EncodedKeySpec x509Spec = new X509EncodedKeySpec(gcePublicKey);
                PublicKey pk = kf.generatePublic(x509Spec);
                ka.doPhase(pk, true);

                publishProgress(4);
                // Step 4 part 3:  App can generate the secret key
                byte secret[] = ka.generateSecret();

                // Step 6:  App generates a DES key
                SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
                DESKeySpec desSpec = new DESKeySpec(secret);
                SecretKey key = skf.generateSecret(desSpec);

                publishProgress(5);
                // Step 8:  App receives the encrypted text and decrypts it
                Cipher c = Cipher.getInstance("DES/ECB/PKCS5Padding");
                c.init(Cipher.DECRYPT_MODE, key);
                plainURL = c.doFinal(Base64.decodeBase64(responseEntity.getCipherURL()));
                plainKey = c.doFinal(Base64.decodeBase64(responseEntity.getCipherKey()));

                publishProgress(50);

                isDataLoaded = true;


            } catch (NoSuchAlgorithmException e) {
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
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if(values[0]==50){
                Toast.makeText(FetchingVideoData.this, new String(plainURL) +"\n" +new String(plainKey), Toast.LENGTH_LONG).show();
            }else{
                tv_current_status.setText(statusStrings[values[0]]);
            }

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(isDataLoaded){
                tv_current_status.setText(new String(plainURL) +"\n" +new String(plainKey));

                //Intent to open Video Streaming Activity
                Intent intent = new Intent(context,MainActivity.class);
                intent.putExtra("URL",new String(plainURL));
                intent.putExtra("KEY",new String(plainKey));
                startActivity(intent);
                finish();

            }else{
                tv_current_status.setText("Can't fetch data from server");
            }
        }
    }
}
