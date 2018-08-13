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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

//    private final String api = "http://www.omdbapi.com/";
    private final String server = "http://ec2-13-59-63-149.us-east-2.compute.amazonaws.com:3000";

    @Test
    public void getHomeTest() {

        String path = "";
       String result = connect(path);

       assertNotNull(result);
    }

    @Test
    public void getSubTest() {
        boolean success = false;

        String path = "/login";
        String name = "josh";
        String pass = "password";
        String result = connect(path + "?" + "name=" + name + "&pass=" + pass);

        success = resultIsNull(result);

        assertFalse(success);
        assertNotNull(result);
    }

    @Test
    public void getOmdbInfo() {

        String api = getKey();

        String result = connectOmdb(api + "&t=shawshank_");

        int x = 5 + 5;
    }

    public String connect(String input) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try
        {
            URL url = new URL(server + input);
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

    public static boolean resultIsNull(String input) {
        if (input.equals("[]")) {return true;}
        return false;
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

    public String getKey() {
        try  {
            return connect("/api");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}