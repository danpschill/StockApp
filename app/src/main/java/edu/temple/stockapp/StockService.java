package edu.temple.stockapp;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class StockService extends Service {

    Intent i;
    ArrayList<String> stocks;

    public StockService() {

    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
        Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
//        i = new Intent();
        stocks = intent.getStringArrayListExtra("stock_list");
//        stocks = (ArrayList<String>)i.getExtras().get("stock_list");
        if(stocks != null) {
            DatabaseAccessAPI databaseAccessAPI = new DatabaseAccessAPI();
            databaseAccessAPI.updateStockPrices(stocks);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_SHORT).show();
    }

}
