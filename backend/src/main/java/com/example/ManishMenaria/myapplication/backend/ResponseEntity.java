package com.example.ManishMenaria.myapplication.backend;

/**
 * Created by Manish Menaria on 19-Mar-17.
 */


public class ResponseEntity {
    private String gcePublicKey;
    private String cipherURL;
    private String cipherKey;

    public String getGcePublicKey() {
        return gcePublicKey;
    }

    public void setGcePublicKey(String gcePublicKey) {
        this.gcePublicKey = gcePublicKey;
    }

    public String getCipherURL() {
        return cipherURL;
    }

    public void setCipherURL(String cipherURL) {
        this.cipherURL = cipherURL;
    }

    public String getCipherKey() {
        return cipherKey;
    }

    public void setCipherKey(String cipherKey) {
        this.cipherKey = cipherKey;
    }
}
