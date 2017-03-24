package hnmn3.mechanic.optimist.mvideostreamer;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import fr.maxcom.http.LocalSingleHttpServer;
import fr.maxcom.libmedia.Licensing;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener {

    LocalSingleHttpServer server;
    VideoView videoView;
    private String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        videoView = (VideoView)findViewById(R.id.myVideo);

        MediaController vidControl = new MediaController(this);
        vidControl.setAnchorView(videoView);
        videoView.setMediaController(vidControl);

        //Required before any call to the libmedia library can be made
        Licensing.allow(getApplicationContext());
        videoView.setOnCompletionListener(this);

        //Getting the Intent
        Intent intent =  getIntent();

        key = intent.getStringExtra("KEY");
        myPlay(intent.getStringExtra("URL"));
    }

    private void myPlay(String path) {
        try {
            server = new LocalSingleHttpServer();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "IOException myPlay()", Toast.LENGTH_SHORT).show();
        }
        server.setCipher(myGetCipher());
        server.start()
        path = server.getURL(path);
        videoView.setVideoPath(path);
        videoView.start();
    }

    private Cipher myGetCipher() {
        // avoid the default security provider "AndroidOpenSSL" in Android 4.3+ (http://libeasy.alwaysdata.net/network/#provider)
        Cipher c = null;
        try {
            c = Cipher.getInstance("AES/CTR/NoPadding", "BC");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Toast.makeText(this, "NoSuchAlgorithmException", Toast.LENGTH_SHORT).show();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
            Toast.makeText(this, "NoSuchProviderException", Toast.LENGTH_SHORT).show();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            Toast.makeText(this, "NoSuchPaddingException", Toast.LENGTH_SHORT).show();
        }

        try {
            c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key.getBytes(), "AES"), new IvParameterSpec(new byte[16]));
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            Toast.makeText(this, "InvalidKeyException", Toast.LENGTH_SHORT).show();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            Toast.makeText(this, "InvalidAlgorithmParameterException", Toast.LENGTH_SHORT).show();
        }
        return c;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        server.stop();
    }
}
