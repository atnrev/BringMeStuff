package com.bringmestuff.bringmestuff;


import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Antoine on 04/11/2015.
 */

public class HttpURLConnectionExample extends AsyncTask<String, Void, Void> {

    private final String USER_AGENT = "Mozilla/5.0";
    public static String response;

    public static String getResponse() {
        return response;
    }

    public HttpURLConnectionExample() {
    }

    @Override
    protected Void doInBackground(String... params) {
        try {
            HttpURLConnectionExample http = new HttpURLConnectionExample(params[0], params[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public HttpURLConnectionExample(String phpUrl, String params) throws Exception {

        //System.out.println("Testing 1 - Send Http GET request");
        //sendGet(phpUrl);

        System.out.println("\nSend Http POST request");
        sendPost(phpUrl, params);
    }



    // HTTP GET request
    private void sendGet(String phpUrl) throws Exception {

        URL obj = new URL(phpUrl);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + phpUrl);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        System.out.println(response.toString());

    }

    // HTTP POST request
    private void sendPost(String phpUrl, String params) throws Exception {

        URL obj = new URL(phpUrl);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        String urlParameters = params;

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + phpUrl);
        System.out.println("Post parameters : " + urlParameters);
        System.out.println("Response Code : " + responseCode);



        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer resp = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            resp.append(inputLine);
        }

        in.close();

        //print result
        System.out.println(resp.toString());
        response=resp.toString();
    }

}