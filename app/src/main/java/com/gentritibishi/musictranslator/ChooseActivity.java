package com.gentritibishi.musictranslator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.JsonObject;
import com.squareup.okhttp.MediaType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class ChooseActivity extends AppCompatActivity {

    int AUDIO = 0;
    @Nullable
    Uri uriAudio;
    Button bt_searchByTitle_Artist, bt_fileToUpload, bt_recordAudio;
    StorageReference storageReference;
    String urlInFirebase, name_of_file_uploaded;
    ProgressBar progressBar;
    UploadTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);

        bt_searchByTitle_Artist = findViewById(R.id.bt_searchByTitle_Artist);
        bt_fileToUpload = findViewById(R.id.bt_fileToUpload);
        bt_recordAudio = findViewById(R.id.bt_recordAudio);
        progressBar = findViewById(R.id.progressBar);

        storageReference = FirebaseStorage.getInstance().getReference();

        bt_searchByTitle_Artist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChooseActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });

        bt_fileToUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //We will make uploading...
                Intent intent = new Intent().setType("audio/*").setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select mp3 audio"), AUDIO);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == AUDIO) {
                uriAudio = data.getData();
                uploadMethod();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadMethod() {
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReferenceProfilePic = firebaseStorage.getReference();
        StorageReference imageRef = storageReferenceProfilePic.child("Audio" + "/" + uriAudio.getLastPathSegment() + ".mp3");

        imageRef.putFile(uriAudio)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //if the upload is successful
                        //hiding the progress dialog
                        //and displaying a success toast
                        Task<Uri> uri = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                        uri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String URL = uri.toString();
                                String key = "f23b0770602f1a51658f651dcb49f3e3";
                                String returnAPI = "apple_music,spotify";

                                try {
                                      String encodedURL = URLEncoder.encode(URL,"UTF-8");
                                      String url = "https://api.audd.io/?url=" + encodedURL + "&return="+returnAPI+"&api_token="+key;
//
                                RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

                                //request data json from url
                                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        try {
                                            JSONObject result = response.getJSONObject("result");
                                            String title = result.getString("title");
                                            String artist = result.getString("artist");

                                            Toast.makeText(ChooseActivity.this, title, Toast.LENGTH_SHORT).show();
                                            Toast.makeText(ChooseActivity.this, artist, Toast.LENGTH_SHORT).show();

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                                requestQueue.add(jsonObjectRequest);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        //if the upload is not successful
                        //hiding the progress dialog
                        //and displaying error message
                        Toast.makeText(getApplicationContext(), exception.getCause().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                });

    }

}
