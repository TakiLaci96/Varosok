package com.example.varosok;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button buttonListazas;
    private Button buttonUjFelvetel;
    private List<Varos> varosok = new ArrayList<>();
    private String url = "https://retoolapi.dev/ckzrng/varosok";
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        buttonUjFelvetel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //RequestTask task = new RequestTask(url, "GET", "id");
                Intent intent = new Intent(MainActivity.this, InsertActivity.class);
                startActivity(intent);
                finish();
            }
        });
        buttonListazas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ListActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public void init() {
        buttonListazas = findViewById(R.id.buttonListazas);
        buttonUjFelvetel = findViewById(R.id.buttonUjFelvetel);
        progressBar = findViewById(R.id.progressBar);
    }


    private class RequestTask extends AsyncTask<Void, Void, Response> {
        String requestUrl;
        String requestType;
        String requestParams;

        public RequestTask(String requestUrl, String requestType, String requestParams) {
            this.requestUrl = requestUrl;
            this.requestType = requestType;
            this.requestParams = requestParams;
        }

        //doInBackground metódus létrehozása a kérés elküldéséhez
        @Override
        protected Response doInBackground(Void... voids) {
            Response response = null;
            try {
                switch (requestType) {
                    case "GET":
                        response = RequestHandler.get(requestUrl + requestParams);
                        break;
                    case "POST":
                        response = RequestHandler.post(requestUrl, requestParams);
                        break;
                }
            } catch (IOException e) {
                Toast.makeText(MainActivity.this,
                        e.toString(), Toast.LENGTH_SHORT).show();
            }
            return response;
        }

        //onPreExecute metódus létrehozása a ProgressBar megjelenítéséhez
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        //onPostExecute metódus létrehozása a válasz feldolgozásához
        //A válasz feldolgozása a különböző kérés típusoknak megfelelően történik
        //A GET kérés esetén a válasz tartalmát Varos[] tömbbe konvertáljuk
        //A POST, PUT és DELETE kérések esetén a válasz tartalmát Varos objektumba konvertáljuk
        @Override
        protected void onPostExecute(Response response) {
            super.onPostExecute(response);
            progressBar.setVisibility(View.GONE);
            Gson converter = new Gson();
            if (response.getResponseCode() >= 400) {
                Toast.makeText(MainActivity.this,
                        "Hiba történt a kérés feldolgozása során", Toast.LENGTH_SHORT).show();
                Log.d("onPostExecuteError:", response.getContent());
            }
            switch (requestType) {
                case "GET":
                    Varos[] varosokArray = converter.fromJson(
                            response.getContent(), Varos[].class);
                    if (varosokArray.length > 0) {
                        Toast.makeText(MainActivity.this, "Sikeres adatlekérdezés", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, ListActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else {
                        Toast.makeText(MainActivity.this,
                                "Hibás adatlekérdezés", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case "POST":
                    Varos varos = converter.fromJson(
                            response.getContent(), Varos.class);
                    varosok.add(0, varos);
                    Toast.makeText(MainActivity.this, "Sikeres hozzáadás", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}