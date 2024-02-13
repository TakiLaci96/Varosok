package com.example.varosok;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UpdateActivity extends AppCompatActivity {

    private EditText editTextId1;
    private EditText editTextVaros1;
    private EditText editTextOrszag1;
    private EditText editTextLakossag1;
    private Button buttonModosit;
    private Button buttonBack1;

    private List<Varos> varosok = new ArrayList<>();
    private String url = "https://retoolapi.dev/ckzrng/varosok";
    private ProgressBar progressBar1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        init();

        // Ellenőrizzük, hogy az intent tartalmaz-e adatot
        if (getIntent().hasExtra("id")) {
            int varosId = getIntent().getIntExtra("id", 0);
            loadVarosDetails(varosId);
        }

        // Módosítás gombra kattintás
        buttonModosit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modifyVaros();
            }
        });

        // Vissza gombra kattintás
        buttonBack1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Visszatérés a ListActivity-be
                //finish(); // valamiért már nem működik, ezért az alábbi kódot használjuk
                Intent intent = new Intent(UpdateActivity.this, ListActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    private void loadVarosDetails(int id) {
        // Adatlekérdezés a megadott azonosítóval
        RequestTask task = new RequestTask(url, "GET", "/" + id);
        task.execute();
    }

    private void init() {
        editTextId1 = findViewById(R.id.editTextId1);
        editTextVaros1 = findViewById(R.id.editTextVaros1);
        editTextOrszag1 = findViewById(R.id.editTextOrszag1);
        editTextLakossag1 = findViewById(R.id.editTextLakossag1);
        buttonModosit = findViewById(R.id.buttonModosit);
        buttonBack1 = findViewById(R.id.buttonBack1);
        progressBar1 = findViewById(R.id.progressBar1);
    }
    // Módosítás metódus létrehozása
    private void modifyVaros() {

        int varosId = Integer.parseInt(editTextId1.getText().toString());
        String varosNev = editTextVaros1.getText().toString();
        String varosOrszag = editTextOrszag1.getText().toString();
        int varosLakossag;

        // Lakosság ellenőrzése
        try {
            varosLakossag = Integer.parseInt(editTextLakossag1.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(UpdateActivity.this,
                    "A lakosság mező csak számot tartalmazhat", Toast.LENGTH_SHORT).show();
            return;
        }

        if (varosNev.isEmpty() || varosOrszag.isEmpty()) {
            Toast.makeText(UpdateActivity.this,
                    "Minden mező kitöltése kötelező", Toast.LENGTH_SHORT).show();
            return;
        } else if (varosLakossag <= 0) {
            Toast.makeText(UpdateActivity.this,
                    "A lakosság mező nem lehet nulla!", Toast.LENGTH_SHORT).show();
            return;
        }


        // Módosítás elküldése a szervernek
        Varos varos = new Varos(varosId, varosNev, varosOrszag, varosLakossag);
        Gson jsonConverter = new Gson();
        RequestTask task = new RequestTask(url + "/" + varosId, "PUT", jsonConverter.toJson(varos));
        task.execute();
        // Sikeres módosítás esetén visszatérés a ListActivity-be
        Toast.makeText(this, "Sikeres módosítás", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(UpdateActivity.this, ListActivity.class);
        startActivity(intent);
        finish();
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

        public RequestTask(String requestUrl, String requestType) {
            this.requestUrl = requestUrl;
            this.requestType = requestType;
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
                    case "PUT":
                        response = RequestHandler.put(requestUrl, requestParams);
                        break;
                    case "DELETE":
                        response = RequestHandler.delete(requestUrl + "/" + requestParams);
                        break;
                }
            } catch (IOException e) {
                Toast.makeText(UpdateActivity.this,
                        e.toString(), Toast.LENGTH_SHORT).show();
            }
            return response;
        }

        //onPreExecute metódus létrehozása a ProgressBar megjelenítéséhez
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar1.setVisibility(View.VISIBLE);

        }

        //onPostExecute metódus létrehozása a válasz feldolgozásához
        @Override
        protected void onPostExecute(Response response) {
            super.onPostExecute(response);
            progressBar1.setVisibility(View.GONE);
            Gson converter = new Gson();
            if (response.getResponseCode() >= 400) {
                Toast.makeText(UpdateActivity.this,
                        "Hiba történt a kérés feldolgozása során", Toast.LENGTH_SHORT).show();
                Log.d("onPostExecuteError:", response.getContent());
            }
            switch (requestType) {
                case "GET":
                    Varos varosDetails = converter.fromJson(response.getContent(), Varos.class);
                    if (varosDetails != null) {
                        editTextId1.setText(String.valueOf(varosDetails.getId()));
                        editTextVaros1.setText(varosDetails.getNev());
                        editTextOrszag1.setText(varosDetails.getOrszag());
                        editTextLakossag1.setText(String.valueOf(varosDetails.getLakossag()));
                        Toast.makeText(UpdateActivity.this,
                                "Sikeres adatlekérdezés", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(UpdateActivity.this,
                                "Hibás adatlekérdezés", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case "PUT":
                    Varos updateVaros = converter.fromJson(
                            response.getContent(), Varos.class);
                    //varosok lista frissítése a módosított elemmel
                    varosok.replaceAll(varos1 -> varos1.getId() == updateVaros.getId() ? updateVaros : varos1);
                    Toast.makeText(UpdateActivity.this, "Sikeres módosítás", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}