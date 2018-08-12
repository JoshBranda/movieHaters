/*
Copyright (c) 2018 Joshua Sander
This work is available under the "MIT License”.
Please see the file LICENSE in this distribution
for license terms.
*/

package com.example.joshuasander.moviehaters;

import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private String api = "";
    private final String awsEC2 = "http://ec2-13-59-63-149.us-east-2.compute.amazonaws.com:3000";
    private String userName = "josh";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    api = connect("/api");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();


        int x = 5 + 5;
    }

    public void getOmdbApi(View view) throws IOException {
        EditText editText = (EditText) findViewById(R.id.editText);
        String userSelection = editText.getText().toString();
        userSelection = userSelection.replaceAll("\\s+","_");
        final String message = api + "&t=" + userSelection;

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    ((RatingBar)findViewById(R.id.ratingBar)).setVisibility(View.VISIBLE);
                    String fullResponse = downloadUrl(message);
                    String result = parseIt(fullResponse);
                    String yourRating = getRatingFromServer(parseId(fullResponse), awsEC2);

                    if (yourRating.equals("null") == false) {
                        result += yourRating;
                    }

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
        int len = 3000;

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
        Reader reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }

    public String parseIt(String input) throws Exception {

        JSONObject jsonObject = new JSONObject(input);

        String result = jsonObject.getString("Title") + "\n";
        result += "year: " + jsonObject.getString("Year") + "\n";
        result += "imdb rating: " + jsonObject.getString("imdbRating") + "\n";

        String poster = jsonObject.getString("Poster");
        loadImage(poster);

        return result;

    }

    public String parseId(String input) throws Exception {

        return new JSONObject(input).getString("imdbID");
    }

    public void loadImage (String url) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable drawable = Drawable.createFromStream(is, "src name");
            ImageView img= (ImageView) findViewById(R.id.poster);
            img.setImageDrawable(drawable);
            return;
        } catch (Exception e) {
            return;
        }
    }

    public String getRatingFromServer(String id, String server) throws Exception {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(server + "?name=" + userName + "&id=" + id);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuffer buffer = new StringBuffer();
            String line = "";

            buffer.append(reader.readLine());

            return buffer.toString();
    } catch(
    MalformedURLException e)

    {
        e.printStackTrace();
    } catch(
    IOException e)

    {
        e.printStackTrace();
    } finally

    {
        if (connection != null) {
            connection.disconnect();
        }
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        }
        return null;
    }

    public String connect(String input) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try
        {
            URL url = new URL(awsEC2 + input);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuffer buffer = new StringBuffer();
            String line = "";

            buffer.append(reader.readLine());

            return buffer.toString();


        } catch(
                MalformedURLException e)

        {
            e.printStackTrace();
        } catch(
                IOException e)

        {
            e.printStackTrace();
        } finally

        {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
