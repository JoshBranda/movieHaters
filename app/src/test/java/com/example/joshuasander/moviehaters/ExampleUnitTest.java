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
    public void insertReview() {
        boolean success = false;

        String path = "/insertReview";
        String checkPath = "/check";
        String name = "josh";
        String id = "tt0073195";
        String result = connect(checkPath + "?" + "name=" + name + "&id=" + id);

        if (result.equals("[]")) {
            result = connect(path + "?" + "name=" + name + "&id=" + id);
        }

        result = connect(checkPath + "?" + "name=" + name + "&id=" + id);

        success = resultIsNull(result);

        assertFalse(success);
        assertNotNull(result);

    }

    @Test
    public void getFriends() throws Exception{
        boolean success = false;

        String path = "/aggregate";
        String path2 = "/aggregate2";
        String name = "josh";
        String id = "tt0111161";

        double starsTotal   = 0;
        double starsTaste   = 0;
        double starsBad     = 0;
        int count           = 0;
        int countTaste      = 0;
        int countBad        = 0;

        String result = connect(path + "?" + "name=" + name + "&id=" + id);
        String [] friends = result.split(",");
        String [] friendsResults;
        String test;

        success = resultIsNull(result);

        for (int x = 0; x < friends.length; x+=2) {
            test = connect(path2 + "?" + "name=" + friends[x] + "&id=" + id);
            friendsResults = parseFriendsReview(test);

            if (friendsResults == null) {
                continue;
            }

            starsTotal += Double.parseDouble(friendsResults[0]);
            count++;

            if (friends[x + 1].equals("1")) {
                starsBad += Double.parseDouble(friendsResults[0]);
                countBad++;
            }
            else if (friends[x + 1].equals("2")) {
                starsTaste += Double.parseDouble(friendsResults[0]);
                countTaste++;
            }
        }

        if (count != 0) {
            starsTotal /= count;
        }
        if (countTaste != 0) {
            starsTaste /= countTaste;
        }
        if (countBad != 0) {
            starsBad /= countBad;
        }

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

    public String [] parseFriendsReview(String input) throws Exception {
        if (input.equals("[]")){
            return null;
        }

        input = input.substring(1, input.length() - 1);

        JSONObject jsonObject = new JSONObject(input);


        String result = jsonObject.getString("stars");
        String result2 = jsonObject.getString("review");

        String [] finalResult = new String [] {result, result2};

        return finalResult;
    }
}