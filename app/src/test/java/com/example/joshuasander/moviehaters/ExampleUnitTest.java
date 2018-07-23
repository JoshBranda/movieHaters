/*
Copyright (c) 2018 Joshua Sander
This work is available under the "MIT License”.
Please see the file LICENSE in this distribution
for license terms.

This code for rest architecture was modelled after:
https://stackoverflow.com/questions/40702774/httpurlconnection-getinputstream-stop-working
and
https://developer.android.com/reference/java/net/HttpURLConnection

*/
package com.example.joshuasander.moviehaters;

import android.util.JsonReader;

import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    private final String api = "http://www.omdbapi.com/";

    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testApi() throws Exception {

        boolean connectionEstablished = false;
        String response = "";


        URL omdbEndpoint = new URL(api);

        // Create connection
        HttpURLConnection myConnection =
                (HttpURLConnection) omdbEndpoint.openConnection();

        myConnection.setRequestMethod("GET");
        myConnection.setDoInput(true);

        if (myConnection.getResponseCode() == 200) {
            connectionEstablished = true;
        }

        myConnection.connect();

        InputStream responseBody = myConnection.getInputStream();

        InputStreamReader responseBodyReader =
                new InputStreamReader(responseBody, "UTF-8");

        JsonReader jsonReader = new JsonReader(responseBodyReader);

        jsonReader.beginObject(); // Start processing the JSON object

        while (jsonReader.hasNext()) { // Loop through all keys

            String key = jsonReader.nextName(); // Fetch the next key

            if (key.equals("Title")) { // Check if desired key

                // Fetch the value as a String
                response = jsonReader.nextString();


                break; // Break out of the loop
            } else {
                jsonReader.skipValue(); // Skip values of other keys
            }
        }

        jsonReader.close();

        myConnection.disconnect();

        assertEquals(connectionEstablished, true);
    }

    @Test
    public void testApi2() throws Exception{

        String omdbEndpoint = api;

        String response = parseIt(downloadUrl(omdbEndpoint));

        int x = 5+ 5;
    }

    // Given a URL, establishes an HttpUrlConnection and retrieves
// the web page content as a InputStream, which it returns as
// a string.
    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 5000;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
//            int response = conn.getResponseCode();
//            Log.d(DEBUG_TAG, "The response is: " + response);
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
        String input2 = "{ \"name\":\"John\" }";
        JSONObject jsonObject = new JSONObject(input2);

        String result = jsonObject.getString("title") + "\n";
        result += "year: " + jsonObject.getString("year") + "\n";
        result += "imdb rating: " + jsonObject.getString("imdbRating") + "\n";

        return result;

//        return input;
    }
}