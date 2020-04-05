package com.example.c2c_online_shop;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpURLConnectionUtil{

    public static String postDataHttpUriConnection(String uri, String query){
        try{
            URL url = new URL(uri);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/json");

            DataOutputStream outputStream = new DataOutputStream(con.getOutputStream());
            outputStream.write(query.getBytes("UTF-8"));
            outputStream.flush();
            outputStream.close();

            String result = inputStreamToString(con.getInputStream());
            return result;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public static String getDataHttpUriConnection(String uri){
        try {
            URL url = new URL(uri);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            String result = inputStreamToString(con.getInputStream());
            return result;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public static String inputStreamToString(InputStream stream)  {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder sb = new StringBuilder();
        String line = "";
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            return sb.toString();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}