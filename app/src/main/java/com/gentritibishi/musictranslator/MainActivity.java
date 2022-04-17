package com.gentritibishi.musictranslator;

import androidx.appcompat.app.AppCompatActivity;

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
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.RequestBody;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private TextView lyrics_view_result;
    private EditText et_artist_name;
    private EditText et_song_name;
    private Button bt_findLyrisc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lyrics_view_result = findViewById(R.id.lyrics_view_result);
        et_artist_name = findViewById(R.id.et_artist_name);
        et_song_name = findViewById(R.id.et_song_name);
        bt_findLyrisc = findViewById(R.id.bt_findLyrisc);


        bt_findLyrisc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String url = "https://api.lyrics.ovh/v1/" + et_artist_name.getText().toString()+ "/" +et_song_name.getText().toString();
                url.replace(" ", "20%");

                RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

                //request data json from url
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            lyrics_view_result.setText(response.getString("lyrics"));
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


            }
        });

    }
}