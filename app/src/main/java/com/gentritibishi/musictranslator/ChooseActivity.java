package com.gentritibishi.musictranslator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;


public class ChooseActivity extends AppCompatActivity {

    static final String LOG_TAG = "Record_log";
    static final int RECORD_AUDIO = 1;
    int AUDIO = 0;
    @Nullable
    Uri uriAudio;
    Button bt_searchByTitle_Artist, bt_fileToUpload, bt_recordAudio;
    TextView tapHold;
    StorageReference storageReference;
    String urlInFirebase, name_of_file_uploaded;
    ProgressBar progressBar;
    UploadTask uploadTask;
    String lyricsfromAPI = null;
    String title = null;
    String artist = null;
    MediaRecorder mRecorder;
    static String mFilename = null;
    Animation scaleUp, scaleDown;
    ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);

        bt_searchByTitle_Artist = findViewById(R.id.bt_searchByTitle_Artist);
        bt_fileToUpload = findViewById(R.id.bt_fileToUpload);
        bt_recordAudio = findViewById(R.id.bt_recordAudio);
        progressBar = findViewById(R.id.progressBar);
        tapHold = findViewById(R.id.tapHold);
        mProgress = new ProgressDialog(this);
        scaleUp = AnimationUtils.loadAnimation(ChooseActivity.this, R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(ChooseActivity.this, R.anim.scale_down);

        storageReference = FirebaseStorage.getInstance().getReference();
        mFilename = getExternalCacheDir().getAbsolutePath();
        // muna me ja ndrru me ja lon mp3 po tani duhet edhe  mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); me ndrru to Default
        mFilename += "/audiorecordtest.mp4";

        bt_recordAudio.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (ActivityCompat.checkSelfPermission(ChooseActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(ChooseActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO);

                } else {
                    //user tapped on button
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        tapHold.setText("Recognizing");
                        bt_recordAudio.startAnimation(scaleUp);
                        startRecording();
                        return true;
                    } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        tapHold.setText("Tap and hold to recognize");
                        bt_recordAudio.startAnimation(scaleDown);
                        stopRecording();
                    }
                }
                return true;
            }
        });


        bt_searchByTitle_Artist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgress.show();
                Intent intent = new Intent(ChooseActivity.this, SearchActivity.class);
                startActivity(intent);
                mProgress.dismiss();
            }
        });

        bt_searchByTitle_Artist.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View view, MotionEvent motionEvent) {
                bt_searchByTitle_Artist.setBackgroundResource(R.drawable.bt_default_hover);
                return true;
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
        mProgress.setMessage("Uploading....");
        mProgress.setProgress(50);
        mProgress.show();
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
                                String key = "30ef0c63ae847eb26f38a9a2e7e86bb6";
                                String returnAPI = "lyrics,apple_music,spotify";

                                try {
                                    String encodedURL = URLEncoder.encode(URL, "UTF-8");
                                    String url = "https://api.audd.io/?url=" + encodedURL + "&return=" + returnAPI + "&api_token=" + key;
                                    RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

                                    //request data json from url
                                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            try {
                                                JSONObject result = response.getJSONObject("result");
                                                JSONObject lyricsObj = result.getJSONObject("lyrics");

                                                lyricsfromAPI = lyricsObj.getString("lyrics");
                                                title = result.getString("title");
                                                artist = result.getString("artist");

                                                if(lyricsfromAPI!=null){
                                                    mProgress.setMessage("Successfully found!");
                                                    mProgress.setProgress(100);
                                                    Intent intentUpload = new Intent(ChooseActivity.this, LyricsActivity.class);
                                                    intentUpload.putExtra("lyrics",lyricsfromAPI);
                                                    startActivity(intentUpload);
                                                    mProgress.dismiss();
                                                }else{
                                                    Toast.makeText(ChooseActivity.this, R.string.lyrics_from_uploaded_file_not_found, Toast.LENGTH_SHORT).show();
                                                }


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
                                } catch (Exception e) {
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

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(mFilename);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }


    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        uploadAudioRecord();
    }

    private void uploadAudioRecord() {
        mProgress.setMessage("Uploading Recorded starting....");
        mProgress.setProgress(50);
        mProgress.show();
        String nameOnAudio = getAlphaNumericString(6);
        StorageReference fileUploadAudio = storageReference.child("Recorded").child("record_audio.mp4");
        Uri uriAudioRecorded = Uri.fromFile(new File(mFilename));
        fileUploadAudio.putFile(uriAudioRecorded).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileUploadAudio.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        mProgress.setMessage("Uploaded successfully!");
                        mProgress.dismiss();
                        String recURL = uri.toString();
                        String recKey = "30ef0c63ae847eb26f38a9a2e7e86bb6";
                        String returnAPI = "lyrics,apple_music,spotify";

                        try {
                            String rec_encodedURL = URLEncoder.encode(recURL, "UTF-8");
                            String url_rec = "https://api.audd.io/?url=" + rec_encodedURL + "&return=" + returnAPI + "&api_token=" + recKey;
                            RequestQueue rec_requestQueue = Volley.newRequestQueue(getApplicationContext());

                            //request data json from url
                            JsonObjectRequest rec_jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url_rec, null, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        JSONObject result = response.getJSONObject("result");
                                        JSONObject lyricsObj = result.getJSONObject("lyrics");

                                        String lyricsFromRecorded = lyricsObj.getString("lyrics");
                                        String titleFromRecorded = result.getString("title");
                                        String artistFromRecorded = result.getString("artist");
                                        if(lyricsFromRecorded!=null){
                                            mProgress.setProgress(100);
                                            mProgress.setMessage("Successfully found!");
                                            Intent i = new Intent(ChooseActivity.this, LyricsActivity.class);
                                            i.putExtra("lyrics",lyricsFromRecorded);
                                            startActivity(i);
                                            mProgress.dismiss();
                                        }else{
                                            Toast.makeText(ChooseActivity.this, R.string.lyrics_from_recorded_audio_not_find, Toast.LENGTH_SHORT).show();
                                        }

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

                            rec_requestQueue.add(rec_jsonObjectRequest);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });
                Toast.makeText(ChooseActivity.this, "Recognition failed. Try again closer to the sound.", Toast.LENGTH_SHORT).show();


            }
        });
    }

    static String getAlphaNumericString(int n) {

        // chose a Character random from this String
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int) (AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

    }
}
