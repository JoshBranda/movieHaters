/*
Copyright (c) 2018 Joshua Sander
This work is available under the "MIT License”.
Please see the file LICENSE in this distribution
for license terms.
*/

package com.example.joshuasander.moviehaters;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private final String api = "http://www.omdbapi.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void getOmdbApi(View view) throws IOException {
        EditText editText = (EditText) findViewById(R.id.editText);
        final String message = api + "&t=" + editText.getText().toString();

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    String result = parseIt(downloadUrl(message));
                    ((TextView)findViewById(R.id.mainText)).setText(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

    }

    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 10000;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(is, len);
            contentAsString = contentAsString.replace("\u0000", "");
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }

    public String parseIt(String input) throws Exception {

//        String input2 = "{ \"title\":\"John\" }";
        JSONObject jsonObject = new JSONObject(input);

        String result = jsonObject.getString("Title") + "\n";
        result += "year: " + jsonObject.getString("Year") + "\n";
        result += "imdb rating: " + jsonObject.getString("imdbRating") + "\n";

        return result;

//        return input;
    }
}
