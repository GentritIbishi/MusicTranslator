package com.gentritibishi.musictranslator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

public class LyricsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    TextView tv_lyricsToSet;
    Spinner spinner1;
    String lyricsText = null;
    String lyricsEncoded = null;
    String sourceByDefault = "en";
    String[] targetLanguage=new String[] {"en","it", "de", "es","fr","tr"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyrics);

        tv_lyricsToSet = findViewById(R.id.tv_lyricsToSet);
        spinner1 = findViewById(R.id.spinner1);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            lyricsText = extras.getString("lyrics");
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
        //ktu na duhet me manipulu me seletimet nese eshte selektet english translate to english
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}