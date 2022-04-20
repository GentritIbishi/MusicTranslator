package com.gentritibishi.musictranslator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LyricsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    public static int responseCode = 0;
    public static String responseString = "";
    TextView tv_lyricsToSet;
    String language = null;
    Spinner spinner1;
    String lyricsText = null;
    String lyricsEncoded = null;
    String savedEnglishLyrics = null;
    String sourceByDefault = "en";
    String[] targetLanguage=new String[] {"en","it", "de", "es","fr","tr"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyrics);

        tv_lyricsToSet = findViewById(R.id.tv_lyricsToSet);
        spinner1 = findViewById(R.id.spinner1);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            lyricsText = extras.getString("lyrics");
            savedEnglishLyrics = extras.getString("lyrics");
            tv_lyricsToSet.setText(lyricsText);
            //The key argument here must match that used in the other activity

        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(LyricsActivity.this, R.array.targets, R.layout.spinner_style);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter);
        spinner1.setOnItemSelectedListener(this);

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        String text = adapterView.getItemAtPosition(position).toString();
        Toast.makeText(adapterView.getContext(), text, Toast.LENGTH_SHORT).show();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(text.equals("EN")){
                        // split
                        String newStr = lyricsText.substring(0,20);
                        String newStrEncoded = URLEncoder.encode(newStr, String.valueOf(StandardCharsets.UTF_8));
                        OkHttpClient client = new OkHttpClient();
                        try {
                            // Build the request
                            RequestBody body = new FormBody.Builder()
                                    .add("q",newStrEncoded)
                                    .build();

                            Request request = new Request.Builder()
                                    .url("https://google-translate1.p.rapidapi.com/language/translate/v2/detect")
                                    .post(body)
                                    .addHeader("content-type", "application/x-www-form-urlencoded")
                                    .addHeader("Accept-Encoding", "application/gzip")
                                    .addHeader("X-RapidAPI-Host", "google-translate1.p.rapidapi.com")
                                    .addHeader("X-RapidAPI-Key", "cb5af54ae2msh8519ab6c4d423d1p1c6cd3jsn8d8003432950")
                                    .build();
                            Response responses = null;

                            // Reset the response code
                            responseCode = 0;

                            // Make the request
                            responses = client.newCall(request).execute();

                            if ((responseCode = responses.code()) == 200) {
                                // Get response
                                String jsonData = responses.body().string();

                                try {
                                    // Transform reponse to JSon Object
                                    JSONObject json = new JSONObject(jsonData);
                                    JSONObject data = json.getJSONObject("data");
                                    JSONArray detections = data.getJSONArray("detections");
                                    JSONArray last = detections.getJSONArray(0);
                                    JSONObject lastobj = last.getJSONObject(0);
                                    language = lastobj.getString("language");

                                        Toast.makeText(LyricsActivity.this, language, Toast.LENGTH_SHORT).show();
                                }catch (JSONException e) {
                                    e.getMessage();
                                }
                            }

                        } catch (IOException e) {
                            responseString = e.toString();
                        }
                    }
                }catch (Exception e){
                    Toast.makeText(LyricsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    //Spinneri END
    }
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public static String compress(String str) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream(str.length());
        GZIPOutputStream gos = new GZIPOutputStream(os);
        gos.write(str.getBytes());
        os.close();
        gos.close();
        return Base64.encodeToString(os.toByteArray(),Base64.DEFAULT);
    }

}