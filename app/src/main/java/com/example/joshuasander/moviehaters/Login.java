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
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by joshuasander on 7/30/18.
 */

public class Login extends AppCompatActivity{

    private final String awsEC2 = "http://ec2-13-59-63-149.us-east-2.compute.amazonaws.com:3000";
    private String userName = "josh";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
    }

    public void login(View view) throws IOException {
        EditText userNameRaw = (EditText) findViewById(R.id.userName);
        EditText passwordRaw = (EditText) findViewById(R.id.password);

        String userName = userNameRaw.getText().toString();
        String password = passwordRaw.getText().toString();

        final String message = awsEC2 + "/login" + "?name=" + userName + "&pass=" + password;

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    String fullResponse = connect(message);
                    if (resultIsNull(fullResponse)) {
                        ((TextView)findViewById(R.id.loginResponse)).setText("Invalid login!");
                    }
                    else {
                        Intent intent = new Intent(Login.this, MainActivity.class);
                        startActivity(intent);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();


    }

    public String connect(String input) {
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


        } catch(MalformedURLException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();}
        }

        return null;
    }

    public static boolean resultIsNull(String input) {
        if (input.equals("[]")) {return true;}
        return false;
    }
}
