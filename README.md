# MVideoStreamer
This is a copy protected video streaming app. This app encrypts the video such that nobody will be able to copy the content and will be able to view it offline. Link of the video is not accessible by the user.

### This project contains three different parts
1. Java File to generate encrypted video
2. Google cloud Endpoint module(GCE)
3. Android Application

1.) **Java file** is used to convert any video file to encrypted form. It uses AES for encryption using a private key.This same private key is used by the application to decrypt the video while streaming.

2.) **Google cloud endpoint** module is a server side code which is used to Generate private symmetric keys for encryption & decryption of data which will be exchanged between the app & GCE. It uses Diffie-Hellman algorithm to generate symmetric private keys. Using this private key GCE send the URL of video and Key in encrypted form(AES) which is then decrypted by the android application.

3.) **Android Application**
The app contains two activities. **MainActivity** and **FetchingVideoData**.

FetchingVideoData Activity is used to get URL and Key from the GCE securely.
MainActivity uses this URL and key to stream the Encrypted video which is stored on the server in encrypted form.This activity uses LibMedia library for streaming encrypted video.


![alt tag](https://github.com/Gr8manish/MVideoStreamer/blob/master/ScreenShots/one.jpg "MVideoStreamer") ![alt tag](https://github.com/Gr8manish/MVideoStreamer/blob/master/ScreenShots/two.jpg "MVideoStreamer") ![alt tag](https://github.com/Gr8manish/MVideoStreamer/blob/master/ScreenShots/three.jpg "MVideoStreamer")
