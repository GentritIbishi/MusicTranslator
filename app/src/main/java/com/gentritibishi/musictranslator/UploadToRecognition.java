package com.gentritibishi.musictranslator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class UploadToRecognition extends AppCompatActivity {

    FirebaseStorage storage;
    // Create a Cloud Storage reference from the app
    StorageReference storageRef = storage.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_to_recognition);

        //Me bo per me upload ne firebase storage dhe me bo copy link e me ja qu api

        //choose music to upload


        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://shazam8.p.rapidapi.com/track/recognize")
                .post(null)
                .addHeader("content-type", "application/octet-stream")
                .addHeader("X-RapidAPI-Host", "shazam8.p.rapidapi.com")
                .addHeader("X-RapidAPI-Key", "ea63097dc9mshaa01df1bfec0cd5p1cf390jsn9a9ddfa1c9f9")
                .build();

        try {
            Response response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}