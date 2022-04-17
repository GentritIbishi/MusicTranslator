package com.gentritibishi.musictranslator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ChooseActivity extends AppCompatActivity {

    int AUDIO = 0;
    @Nullable
    Uri uriAudio;
    Button bt_searchByTitle_Artist, bt_fileToUpload, bt_recordAudio;
    StorageReference storageReference;
    String urlInFirebase, name_of_file_uploaded;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);

        bt_searchByTitle_Artist = findViewById(R.id.bt_searchByTitle_Artist);
        bt_fileToUpload = findViewById(R.id.bt_fileToUpload);
        bt_recordAudio = findViewById(R.id.bt_recordAudio);
        progressBar = findViewById(R.id.progressBar);

        storageReference = FirebaseStorage.getInstance().getReference("Uploads");

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
        if(resultCode == RESULT_OK){
            if(requestCode == AUDIO){
                uriAudio = data.getData();
//                Cursor returnCursor =
//                        getContentResolver().query(uriAudio, null, null, null, null);
//                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
//                name_of_file_uploaded = returnCursor.getString(nameIndex);
                upload();
                progressBar.setVisibility(View.VISIBLE);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void upload() {
        StorageReference filePath = storageReference.child("Audio").child(uriAudio.getLastPathSegment());
        filePath.putFile(uriAudio).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            urlInFirebase = uri.toString();
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(ChooseActivity.this, uri.toString(), Toast.LENGTH_LONG).show();

                            // url me ja qu api qe me bo detekt kongen

                        }
                    });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.VISIBLE);
                Toast.makeText(ChooseActivity.this, R.string.audio_failed_to_upload, Toast.LENGTH_SHORT).show();
            }
        });

    }
}