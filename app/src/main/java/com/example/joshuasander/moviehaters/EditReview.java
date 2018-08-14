/*
Copyright (c) 2018 Joshua Sander
This work is available under the "MIT License”.
Please see the file LICENSE in this distribution
for license terms.
*/

package com.example.joshuasander.moviehaters;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class EditReview extends AppCompatActivity {

    public String userName;
    public String reviewText;
    public String movieId;
    private final String awsEC2 = "http://ec2-13-59-63-149.us-east-2.compute.amazonaws.com:3000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_review);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                userName = "josh";
                reviewText = "";
                movieId = "";
            } else {
                userName = extras.getString("uname");
                reviewText = extras.getString("currentReview");
                movieId = extras.getString("movieId");
            }
        } else {
            userName = (String) savedInstanceState.getSerializable("uname");
            reviewText = (String) savedInstanceState.getSerializable("currentReview");
            movieId = (String) savedInstanceState.getSerializable("movieId");
        }

        ((EditText) findViewById(R.id.editReview)).setText(reviewText);
    }

    public void save(View view) {
        reviewText = ((EditText) findViewById(R.id.editReview)).getText().toString();

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    //Sourced from:
                    //https://stackoverflow.com/questions/47192882/how-send-get-request-with-value-of-parameter-containing-a-space
                    connect("/review?name=" + userName + "&id=" + movieId + "&text=" + URLEncoder.encode(reviewText,"utf-8"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        try {
            thread.join();
        } catch (Exception e) {e.printStackTrace();}

        Intent intent = new Intent(EditReview.this, MainActivity.class);
        intent.putExtra("uname", userName);
        startActivity(intent);
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

    public void sanitizeInput(String toSanitize) {

    }
}
