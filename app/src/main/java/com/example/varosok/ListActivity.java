package com.example.varosok;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    private ListView listViewData;
    private Button buttonBack2;
    private ProgressBar progressBar;
    private String url = "https://retoolapi.dev/ckzrng/varosok";
    private List<Varos> varosok = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        init();

        RequestTask task = new RequestTask(url, "GET");
        task.execute();

        // Vissza gombra kattintás, visszatérés a MainActivity-be
        buttonBack2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Listaelemre kattintás, ahol kiválaszthatjuk, hogy töröljük vagy módosítjuk a várost
        listViewData.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Varos selectedVaros = varosok.get(position);
                // Itt a kiválasztott város id-je alapján törölhetjük vagy módosíthatjuk azt
                //Toast.makeText(ListActivity.this, "" + selectedVaros.getId(), Toast.LENGTH_SHORT).show();
                showDeleteOrModifyDialog(selectedVaros.getId());
            }

            // Törlés vagy módosítás dialógus megjelenítése
            private void showDeleteOrModifyDialog(int id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ListActivity.this);
                builder.setTitle("Válassz műveletet").setMessage("Mit szeretnél tenni?").setPositiveButton("Törlés", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RequestTask task = new RequestTask(url, "DELETE", String.valueOf(id));
                        task.execute();
                        dialog.cancel();
                    }
                }).setNegativeButton("Módosítás", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(ListActivity.this, UpdateActivity.class);
                        intent.putExtra("id", id);
                        startActivity(intent);
                        finish();
                    }
                }).setNeutralButton("Mégse", null).create().show();
            }
        });
    }

    private void init() {
        listViewData = findViewById(R.id.listViewData);
        listViewData.setAdapter(new VarosAdapter());
        buttonBack2 = findViewById(R.id.buttonBack2);
        progressBar = findViewById(R.id.progressBar);
    }

    private class VarosAdapter extends ArrayAdapter<Varos> {
        public VarosAdapter() {
            super(ListActivity.this, R.layout.varosok_list_items, varosok);
        }

        // getView metódus létrehozása a listaelemek megjelenítéséhez
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            //inflater létrehozása
            LayoutInflater inflater = getLayoutInflater();
            //view létrehozása a varosok_list_items.xml-ből
            View view = inflater.inflate(R.layout.varosok_list_items, null, false);
            //varosok_list_items.xml-ben lévő elemek inicializálása
            TextView textViewVaros = view.findViewById(R.id.textViewVaros);
            TextView textViewOrszag = view.findViewById(R.id.textViewOrszag);
            TextView textViewLakossag = view.findViewById(R.id.textViewLakossag);
            //aktuális város létrehozása a varosok listából
            Varos varos = varosok.get(position);
            //adatok beállítása
            textViewVaros.setText(varos.getNev());
            textViewOrszag.setText(varos.getOrszag());
            textViewLakossag.setText(String.valueOf(varos.getLakossag()));
            return view;
        }
    }


    private class RequestTask extends AsyncTask<Void, Void, Response> {
        String requestUrl;
        String requestType;
        String requestParams;

        public RequestTask(String requestUrl, String requestType) {
            this.requestUrl = requestUrl;
            this.requestType = requestType;
        }
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
                if (requestType.equals("GET")) {
                    response = RequestHandler.get(requestUrl);
                }
                if (requestType.equals("DELETE")) {
                    response = RequestHandler.delete(requestUrl + "/" + requestParams);
                }
            } catch (IOException e) {
                Toast.makeText(ListActivity.this,
                        e.toString(), Toast.LENGTH_SHORT).show();
            }
            return response;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);

        }

        //onPostExecute metódus létrehozása a válasz feldolgozásához
        @Override
        protected void onPostExecute(Response response) {
            super.onPostExecute(response);
            progressBar.setVisibility(View.GONE);
            Gson converter = new Gson();
            if (response.getResponseCode() >= 400) {
                Toast.makeText(ListActivity.this,
                        "Hiba történt a kérés feldolgozása során", Toast.LENGTH_SHORT).show();
                Log.d("onPostExecuteError:", response.getContent());
            }
            if (requestType.equals("GET")) {
                Varos[] varosokArray = converter.fromJson(
                        response.getContent(), Varos[].class);
                varosok.clear();
                varosok.addAll(Arrays.asList(varosokArray));
                listViewData.invalidateViews();
                Toast.makeText(ListActivity.this,
                        "Sikeres adatlekérdezés", Toast.LENGTH_SHORT).show();
            }
            if (requestType.equals("DELETE")) {
                int id = Integer.parseInt(requestParams);
                //varosok lista frissítése a törölt elem nélkül
                varosok.removeIf(varos1 -> varos1.getId() == id);
                Toast.makeText(ListActivity.this, "Sikeres törlés", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

