package com.gentritibishi.musictranslator;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class SearchActivity extends AppCompatActivity {

    private EditText et_artist_name;
    private EditText et_song_name;
    private Button bt_findLyrisc;
    ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        et_artist_name = findViewById(R.id.et_artist_name);
        et_song_name = findViewById(R.id.et_song_name);
        bt_findLyrisc = findViewById(R.id.bt_findLyrisc);
        mProgress = new ProgressDialog(this);


        bt_findLyrisc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mProgress.setMessage("Finding lyrics...");
                mProgress.setProgress(50);
                mProgress.show();
                String url = "https://api.lyrics.ovh/v1/" + et_artist_name.getText().toString() + "/" +et_song_name.getText().toString();
                url.replace(" ", "20%");

                RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

                //request data json from url
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String lyricsFromSearch = response.getString("lyrics");

                            if(lyricsFromSearch!=null){
                                mProgress.setMessage("Successfully lyrics was found!");
                                mProgress.setProgress(100);
                                Intent intentFromSearch = new Intent(SearchActivity.this, LyricsActivity.class);
                                intentFromSearch.putExtra("lyrics",lyricsFromSearch);
                                startActivity(intentFromSearch);
                                mProgress.dismiss();
                            }else {
                                mProgress.setMessage("Lyrics not found!");
                                mProgress.dismiss();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(SearchActivity.this, R.string.lyrics_not_found, Toast.LENGTH_SHORT).show();
                    }
                });

                requestQueue.add(jsonObjectRequest);


            }
        });

    }
}