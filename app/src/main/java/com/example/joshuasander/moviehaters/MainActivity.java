/*
Copyright (c) 2018 Joshua Sander
This work is available under the "MIT License”.
Please see the file LICENSE in this distribution
for license terms.
*/

//Used the following site for transfering data between activities:
//https://stackoverflow.com/questions/5265913/how-to-use-putextra-and-getextra-for-string-data

package com.example.joshuasander.moviehaters;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private String api = "";
    private final String awsEC2 = "http://ec2-13-59-63-149.us-east-2.compute.amazonaws.com:3000";
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                userName = "josh";
            } else {
                userName = extras.getString("uname");
            }
        } else {
            userName = (String) savedInstanceState.getSerializable("uname");
        }

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
                    //Used source:
                    //https://medium.com/@yossisegev/understanding-activity-runonuithread-e102d388fe93

                    final String fullResponse = connectOmdb(message);
                    final String movieDetails = parseIt(fullResponse);
                    float rating = -1;
                    String review = null;

                    if (movieDetails == null) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                ((TextView) findViewById(R.id.mainText)).setVisibility(View.VISIBLE);
                                ((TextView) findViewById(R.id.mainText)).setText("Movie not Found!");
                                ((RatingBar)findViewById(R.id.ratingBar)).setVisibility(View.INVISIBLE);
                                ((TextView) findViewById(R.id.review)).setVisibility(View.INVISIBLE);
                                ((Button) findViewById(R.id.reviewStatus)).setVisibility(View.INVISIBLE);
                            }
                        });
                        return;
                    }

                    runOnUiThread(new Runnable() {
                        public void run() {
                            ((RatingBar)findViewById(R.id.ratingBar)).setVisibility(View.VISIBLE);
                            ((TextView) findViewById(R.id.review)).setVisibility(View.VISIBLE);
                            ((Button) findViewById(R.id.reviewStatus)).setVisibility(View.VISIBLE);
                            ((TextView) findViewById(R.id.mainText)).setVisibility(View.VISIBLE);

                            RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);
                            ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                                @Override
                                public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                                    final float finalRating = rating;
                                    Thread thread = new Thread(new Runnable() {

                                        @Override
                                        public void run() {
                                            try  {
                                                connect("/update?name=" + userName + "&id=" + parseId(fullResponse) + "&rating=" + finalRating);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });

                                    thread.start();
                                }
                            });
                        }
                    });

                    final String yourData = getRatingFromServer(parseId(fullResponse), awsEC2);
                    if (yourData != null && yourData.equals("null") == false && yourData.equals("[]") == false) {
                        rating = parseRating(yourData);
                        review = parseReview(yourData);

                    }

                    if (rating != -1) {
                        final float ratingResult = rating;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                try {
                                    ((RatingBar) findViewById(R.id.ratingBar)).setRating(ratingResult);
                                    ((TextView) findViewById(R.id.review)).setText(parseReview(yourData));
                                    //Sourced from:
                                    //https://stackoverflow.com/questions/4602902/how-to-set-the-text-color-of-textview-in-code
                                    ((TextView) findViewById(R.id.review)).setTextColor(Color.RED);
                                    ((TextView) findViewById(R.id.review)).setMovementMethod(new ScrollingMovementMethod());
                                } catch (Exception e) {e.printStackTrace();}
                            }
                        });
                    }
                    else {

                    }

                    if (review != null && review.equals("null") == false && review.equals("[]") == false) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                try {
                                    ((Button) findViewById(R.id.reviewStatus)).setText("Edit Review");
                                    ((TextView) findViewById(R.id.review)).setText(parseReview(yourData));
                                    //Sourced from:
                                    //https://stackoverflow.com/questions/4602902/how-to-set-the-text-color-of-textview-in-code
                                    ((TextView) findViewById(R.id.review)).setTextColor(Color.RED);
                                    ((TextView) findViewById(R.id.review)).setMovementMethod(new ScrollingMovementMethod());
                                } catch (Exception e) {e.printStackTrace();}
                            }
                        });
                    }
                    else {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                ((Button) findViewById(R.id.reviewStatus)).setText("Add Review");
                                ((TextView) findViewById(R.id.review)).setTextColor(Color.RED);
                                ((TextView)findViewById(R.id.review)).setText("No review");
                            }
                        });
                    }

                    runOnUiThread(new Runnable() {
                        public void run() {
                            ((TextView)findViewById(R.id.mainText)).setText(movieDetails);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

    }

    public String parseIt(String input) throws Exception {

        JSONObject jsonObject = new JSONObject(input);

        if (jsonObject.getString("Response").equals("False")) {
            return null;
        }

        String result = jsonObject.getString("Title") + "\n";
        result += "year: " + jsonObject.getString("Year") + "\n";
        result += "imdb rating: " + jsonObject.getString("imdbRating") + "\n";

        String poster = jsonObject.getString("Poster");
        loadImage(poster);

        return result;

    }

    public float parseRating(String input) throws Exception {
        if (input.equals("[]")){
            return -1;
        }
        //Source:
        //https://stackoverflow.com/questions/7438612/how-to-remove-the-last-character-from-a-string
        input = input.substring(1, input.length() - 1);

        JSONObject jsonObject = new JSONObject(input);

        String result = jsonObject.getString("stars");

        return Float.parseFloat(result);
    }

    public String parseReview(String input) throws Exception {
        if (input.equals("[]")){
            return null;
        }
        //Source:
        //https://stackoverflow.com/questions/7438612/how-to-remove-the-last-character-from-a-string
        input = input.substring(1, input.length() - 1);

        JSONObject jsonObject = new JSONObject(input);

        return jsonObject.getString("review");
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
            URL url = new URL(server + "/data?name=" + userName + "&id=" + id);
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

    public String connectOmdb(String input) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try
        {
            URL url = new URL(input);
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
