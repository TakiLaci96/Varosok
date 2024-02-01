package com.example.varosok;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InsertActivity extends AppCompatActivity {


    private Button buttonFelvetel;
    private Button buttonBack;
    private EditText editTextId;
    private EditText editTextVaros;
    private EditText editTextOrszag;
    private EditText editTextLakossag;
    private String url = "https://retoolapi.dev/ckzrng/varosok";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert);
        init();
        buttonFelvetel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String varos = editTextVaros.getText().toString();
                String orszag = editTextOrszag.getText().toString();
                int lakossag;

                // Lakosság ellenőrzése
                try {
                    lakossag = Integer.parseInt(editTextLakossag.getText().toString());
                } catch (NumberFormatException e) {
                    Toast.makeText(InsertActivity.this,
                            "A lakosság mező csak számot tartalmazhat", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (varos.isEmpty() || orszag.isEmpty() || lakossag == 0) {
                    Toast.makeText(InsertActivity.this,
                            "Minden mező kitöltése kötelező", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Új város felvétele
                Varos ujVaros = new Varos(0, varos, orszag, lakossag);
                Gson jsonConverter = new Gson();
                RequestTask task = new RequestTask(url, "POST", jsonConverter.toJson(ujVaros));
                task.execute();
            }
        });
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InsertActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public void init() {
        buttonFelvetel = findViewById(R.id.buttonFelvetel);
        buttonBack = findViewById(R.id.buttonBack);
        editTextId = findViewById(R.id.editTextId);
        editTextVaros = findViewById(R.id.editTextVaros);
        editTextOrszag = findViewById(R.id.editTextOrszag);
        editTextLakossag = findViewById(R.id.editTextLakossag);
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
                    case "POST":
                        response = RequestHandler.post(requestUrl, requestParams);
                        break;
                }
            } catch (IOException e) {
                Toast.makeText(InsertActivity.this,
                        e.toString(), Toast.LENGTH_SHORT).show();
            }
            return response;
        }

        @Override
        protected void onPostExecute(Response response) {
            super.onPostExecute(response);
            if (response.getResponseCode() >= 400) {
                Toast.makeText(InsertActivity.this,
                        "Hiba történt a kérés feldolgozása során", Toast.LENGTH_SHORT).show();
                return;
            }
            if (requestType.equals("POST")) {
                Toast.makeText(InsertActivity.this,
                        "Sikeres felvétel", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(InsertActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
    }
}