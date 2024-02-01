package com.example.varosok;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class RequestHandler {

    private RequestHandler() {
    }


    //backend és a frontend közötti kommunikáció megvalósítása
    private static HttpURLConnection setupConnection(String url) throws IOException {
        //urlObj létrehozása
        URL urlObj = new URL(url);
        //connection létrehozása
        HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
        //connection beállítása
        connection.setRequestProperty("Accept", "application/json");
        //connection timeout beállítása
        connection.setConnectTimeout(10000);
        //read timeout beállítása
        connection.setReadTimeout(10000);
        return connection;
    }

    //getResponse metódus létrehozása a responseCode és a content lekérdezéséhez
    private static Response getResponse(HttpURLConnection connection) throws IOException {
        //responseCode lekérdezése
        int responseCode = connection.getResponseCode();
        InputStream inputStream;
        //ha a responseCode kisebb, mint 400, akkor az inputStream a connection-ből olvas
        if (responseCode < 400) {
            inputStream = connection.getInputStream();
        } else {
            inputStream = connection.getErrorStream();
        }
        //content létrehozása
        StringBuilder content = new StringBuilder();
        //inputStream-ből olvasás és hozzáadás a contenthez
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        //sorok beolvasása
        String line = reader.readLine();
        //ha nem üres a sor, akkor hozzáadja a contenthez
        while (line != null) {
            content.append(line);
            line = reader.readLine();
        }
        //bezárás
        reader.close();
        inputStream.close();
        //responseCode és egy content
        return new Response(responseCode, content.toString());
    }

    //addRequestBody metódus létrehozása a requestBody hozzáadásához
    private static void addRequestBody(HttpURLConnection connection, String requestBody) throws IOException {
        connection.setRequestProperty("Content-Type", "application/json");
        //outputStream létrehozása
        connection.setDoOutput(true);
        OutputStream outputStream = connection.getOutputStream();
        //írás a outputStream-be
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
        //requestBody írása a writer-be
        writer.write(requestBody);
        //véglegesítés
        writer.flush();
        //bezárás
        writer.close();
        outputStream.close();
    }

    //get metódus létrehozása a GET kéréshez
    public static Response get(String url) throws IOException {
        //connection létrehozása
        HttpURLConnection connection = setupConnection(url);
        //connection típusának beállítása
        connection.setRequestMethod("GET");
        //response visszaadása
        return getResponse(connection);
    }

    //post metódus létrehozása a POST kéréshez
    public static Response post(String url, String requestBody) throws IOException {
        //connection létrehozása
        HttpURLConnection connection = setupConnection(url);
        //connection típusának beállítása
        connection.setRequestMethod("POST");
        //requestBody hozzáadása
        addRequestBody(connection, requestBody);
        //response visszaadása
        return getResponse(connection);
    }

    //put metódus létrehozása a PUT kéréshez
    public static Response put(String url, String requestBody) throws IOException {
        //connection létrehozása
        HttpURLConnection connection = setupConnection(url);
        //connection típusának beállítása
        connection.setRequestMethod("PUT");
        //requestBody hozzáadása
        addRequestBody(connection, requestBody);
        //response visszaadása
        return getResponse(connection);
    }

    //delete metódus létrehozása a DELETE kéréshez
    public static Response delete(String url) throws IOException {
        //connection létrehozása
        HttpURLConnection connection = setupConnection(url);
        //connection típusának beállítása
        connection.setRequestMethod("DELETE");
        //response visszaadása
        return getResponse(connection);
    }
}
