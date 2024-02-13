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

        // Módosítás gombra kattintás
        buttonModosit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Módosítási logika itt
                //RequestTask task = new RequestTask(url, "GET");
                //task.execute();
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

    private void init() {
        editTextId1 = findViewById(R.id.editTextId1);
        editTextVaros1 = findViewById(R.id.editTextVaros1);
        editTextOrszag1 = findViewById(R.id.editTextOrszag1);
        editTextLakossag1 = findViewById(R.id.editTextLakossag1);
        buttonModosit = findViewById(R.id.buttonModosit);
        buttonBack1 = findViewById(R.id.buttonBack1);
        progressBar1 = findViewById(R.id.progressBar1);
    }

    private void modifyVaros() {

        int varosId = Integer.parseInt(editTextId1.getText().toString());
        String varosNev = editTextVaros1.getText().toString();
        String varosOrszag = editTextOrszag1.getText().toString();
        int varosLakossag = Integer.parseInt(editTextLakossag1.getText().toString());

        // Módosítási logika itt
        Varos varos = new Varos(varosId, varosNev, varosOrszag, varosLakossag);
        Gson jsonConverter = new Gson();
        RequestTask task = new RequestTask(url + "/" + varosId, "PUT", jsonConverter.toJson(varos));
        task.execute();
        // Sikeres módosítás esetén visszatérés a ListActivity-be
        Toast.makeText(this, "Sikeres módosítás", Toast.LENGTH_SHORT).show();
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
                    Varos[] varosokArray = converter.fromJson(
                            response.getContent(), Varos[].class);
                    if (varosokArray.length > 0) {
                        Toast.makeText(UpdateActivity.this, "Sikeres adatlekérdezés", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(UpdateActivity.this, ListActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else {
                        Toast.makeText(UpdateActivity.this,
                                "Hibás adatlekérdezés", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case "POST":
                    Varos varos = converter.fromJson(
                            response.getContent(), Varos.class);
                    varosok.add(0, varos);
                    Toast.makeText(UpdateActivity.this, "Sikeres hozzáadás", Toast.LENGTH_SHORT).show();
                    break;
                case "PUT":
                    Varos updateVaros = converter.fromJson(
                            response.getContent(), Varos.class);
                    //varosok lista frissítése a módosított elemmel
                    varosok.replaceAll(varos1 -> varos1.getId() == updateVaros.getId() ? updateVaros : varos1);
                    Toast.makeText(UpdateActivity.this, "Sikeres módosítás", Toast.LENGTH_SHORT).show();
                    break;
                case "DELETE":
                    int id = Integer.parseInt(requestParams);
                    //varosok lista frissítése a törölt elem nélkül
                    varosok.removeIf(varos1 -> varos1.getId() == id);
                    Toast.makeText(UpdateActivity.this, "Sikeres törlés", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}