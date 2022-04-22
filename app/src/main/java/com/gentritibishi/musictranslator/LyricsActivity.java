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

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.zip.GZIPOutputStream;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LyricsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    public static int responseCode = 0;
    public static int responseCodes = 0;
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
                    //    detectLanguage();
        String text = adapterView.getItemAtPosition(position).toString();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(text.equalsIgnoreCase("Choose language")) {
                                    Toast.makeText(LyricsActivity.this, "Choose language if u want to translate!", Toast.LENGTH_SHORT).show();
                                }else {
                                    // do translate on run new thread
                                    translateFree(adapterView.getItemAtPosition(position).toString());
                                }
                            }
                        });



            }
        //Spinneri END

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

    void detectLanguage(){
        try {
            // split
            if(lyricsText == null || lyricsText.isEmpty()){
                Toast.makeText(LyricsActivity.this, "Lyrics not finded", Toast.LENGTH_SHORT).show();
            } else {
                String newStr = lyricsText.substring(0, 20);
                String newStrEncoded = URLEncoder.encode(newStr, String.valueOf(StandardCharsets.UTF_8));
                OkHttpClient client = new OkHttpClient();
                try {
                    // Build the request
                    RequestBody body = new FormBody.Builder()
                            .add("q", newStrEncoded)
                            .build();

                    Request request = new Request.Builder()
                            .url("https://google-translate1.p.rapidapi.com/language/translate/v2/detect")
                            .post(body)
                            .addHeader("content-type", "application/x-www-form-urlencoded")
                            .addHeader("Accept-Encoding", "application/gzip")
                            .addHeader("X-RapidAPI-Host", "google-translate1.p.rapidapi.com")
                            .addHeader("X-RapidAPI-Key", "b077b8b1c3mshe0093931dc39a6cp1371e0jsn39becc3f4640")
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
//                            if (language != null && !language.isEmpty()) {
//                                if (language.equals("en")) {
//                                    spinner1.setSelection(0);
//                                    translateFun(lyricsText);
//                                } else if (language.equals("it")) {
//                                    spinner1.setSelection(1);
//                                    translateFun(lyricsText);
//                                } else if (language.equals("de")) {
//                                    spinner1.setSelection(2);
//                                    translateFun(lyricsText);
//                                } else if (language.equals("es")) {
//                                    spinner1.setSelection(3);
//                                    translateFun(lyricsText);
//                                } else if (language.equals("fr")) {
//                                    spinner1.setSelection(4);
//                                    translateFun(lyricsText);
//                                } else if (language.equals("it")) {
//                                    spinner1.setSelection(5);
//                                    translateFun(lyricsText);
//                                }
//                            }

//                            Toast.makeText(LyricsActivity.this, language, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.getMessage();
                        }
                    }else {
                        JSONObject error = new JSONObject(responses.body().string());
                        String message = error.getString("message");
                        Toast.makeText(LyricsActivity.this, message, Toast.LENGTH_SHORT).show();
                    }


                } catch (IOException e) {
                    responseString = e.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            Toast.makeText(LyricsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    void translateFun(String text) {
//        try {
            // split cause of api don't accept long text, just need premium.
            String[] newStr = lyricsText.split("\n");
//                lyricsEncoded = URLEncoder.encode(newStr[0], String.valueOf(StandardCharsets.UTF_8));
                OkHttpClient client = new OkHttpClient();
                try {
                    // Build the request
                    RequestBody body = new FormBody.Builder()
                            .add("q", newStr[0])
                            .add("target", text)
                            .add("source", language)
                            .build();

                    Request request = new Request.Builder()
                            .url("https://google-translate1.p.rapidapi.com/language/translate/v2")
                            .post(body)
                            .addHeader("content-type", "application/x-www-form-urlencoded")
                            .addHeader("Accept-Encoding", "application/gzip")
                            .addHeader("X-RapidAPI-Host", "google-translate1.p.rapidapi.com")
                            .addHeader("X-RapidAPI-Key", "b077b8b1c3mshe0093931dc39a6cp1371e0jsn39becc3f4640")
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
                            JSONArray translations = data.getJSONArray("translations");
                            JSONObject rec = translations.getJSONObject(0);
                            String translatedText = rec.getString("translatedText");
                            tv_lyricsToSet.setText(translatedText);
                        } catch (JSONException e) {
                            e.getMessage();
                        }
                    }else {
                        Toast.makeText(LyricsActivity.this, responses.body().toString(), Toast.LENGTH_SHORT).show();
                    }

                } catch (IOException e) {
                    responseString = e.toString();
                }



//        } catch (IOException e) {
//            Toast.makeText(LyricsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
    }

    void translateFree(String targetLanguage_to_translate) {

        OkHttpClient client = new OkHttpClient();
        try {
            String encodedLyrics = URLEncoder.encode(lyricsText, String.valueOf(StandardCharsets.UTF_8));
            // Build the request
            RequestBody body = new FormBody.Builder()
                    .add("client", "dict-chrome-ex")
                    .add("sl", "auto")
                    .add("tl",targetLanguage_to_translate)
                    .add("q",encodedLyrics)
                    .build();

            Request request = new Request.Builder()
                    .url("https://clients5.google.com/translate_a/t?")
                    .post(body)
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.104 Safari/537.36")
                    .addHeader("Accept", "/*")
                    .addHeader("Accept-Encoding", "gzip, deflate, br")
                    .addHeader("Connection", "keep-alive")
                    .build();
            Response responses = null;

            // Reset the response code
            responseCode = 0;

            // Make the request
            responses = client.newCall(request).execute();

            if ((responseCode = responses.code()) == 200) {
                // Get response
                String jsonData = responses.body().string();

                // Transform reponse to JSon Object
                JSONArray jsonArray = new JSONArray(jsonData);
                JSONArray last = jsonArray.getJSONArray(0);
                String translate = last.getString(0);
                tv_lyricsToSet.setText(translate);

            } else {
                Toast.makeText(LyricsActivity.this, "To many requests Google Recaptcha block your ip!", Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {
            responseString = e.toString();
        } catch (JSONException e) {
            responseString = e.toString();
        }
    }

    void translateVolleyAPI(String targetLanguage_to_translate) {


        try {
            String encodedLyrics = URLEncoder.encode(lyricsText, String.valueOf(StandardCharsets.UTF_8));
            String url = "https://clients5.google.com/translate_a/t?client=dict-chrome-ex&sl=auto&tl="+targetLanguage_to_translate+"&q="+encodedLyrics;

            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

            //request data json from url
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(com.android.volley.Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    try {
                        JSONArray last = response.getJSONArray(0);
                        String translate = last.getString(0);
                        tv_lyricsToSet.setText(translate);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(LyricsActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                }
            });

            requestQueue.add(jsonArrayRequest);

        } catch (IOException e) {
            responseString = e.toString();
        }
    }

}