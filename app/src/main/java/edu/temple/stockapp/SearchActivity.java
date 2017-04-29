package edu.temple.stockapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class SearchActivity extends AppCompatActivity {

    String api_link = "http://dev.markitondemand.com/MODApis/Api/v2/Quote/json/?symbol=";

    Stock stock;

    Button addButton;
    EditText stockEditText;
    String stockToAdd;
    final String STOCK_KEY = "stock";
    final String STOCK_OBJ_KEY = "stock_obj";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        addButton = (Button) findViewById(R.id.add_button);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stockEditText = (EditText) findViewById(R.id.enter_stock);
                stockToAdd = stockEditText.getText().toString();

                String result = "";
                RetrieveFeedTask rft = new RetrieveFeedTask();
                try {
                    result = rft.execute(stockToAdd, "", "").get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }

//                System.out.println("STOCK NAME: " + stock.getName());
//                System.out.println("STOCK CHART: " + stock.getChart());
//                System.out.println("STOCK PRICE: " + stock.getPrice());


                if (result.length() > 0) {
                        DatabaseAccessAPI databaseAccessAPI = new DatabaseAccessAPI();
                        databaseAccessAPI.addStockToDatabase(stock);

                        Intent resultData = new Intent();
                        resultData.putExtra(STOCK_KEY, stockToAdd);
                        resultData.putExtra(STOCK_OBJ_KEY, stock);
                        setResult(SearchActivity.RESULT_OK, resultData);
                        finish();

                }


            }
        });


    }


    private class RetrieveFeedTask extends AsyncTask<String, String, String> {

        ProgressDialog pdLoading = new ProgressDialog(SearchActivity.this);


        private Exception exception;

        protected void onPreExecute() {
            super.onPreExecute();


//            //this method will be running on UI thread
            pdLoading.setMessage("\tLoading...");
            pdLoading.setCancelable(false);
            pdLoading.show();
//            progressBar.setVisibility(View.VISIBLE);
//            responseView.setText("");
        }

        protected String doInBackground(String... params) {


            String search_query = params[0];

            try {
                URL url = new URL(api_link + search_query);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    JSONArray jsonArr = null;
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    line = bufferedReader.readLine();
//                    while ((line = bufferedReader.readLine()) != null) {
//                    System.out.println("Line: " + line);
                    jsonArr = new JSONArray("[" + line + "]");

//                        stringBuilder.append(line).append("\n");
//                    }


                    String stockName = "";
                    JSONObject obj = jsonArr.getJSONObject(0);
                    try {
                        //TODO Create a Stock object from information retrieved
                        stockName = obj.getString("Symbol");
                        stock = new Stock(obj.getString("Symbol"), "https://chart.yahoo.com/z?t=1d&s=" + obj.getString("Symbol"), obj.getString("LastPrice"));
                    } catch (Exception e) {
                        System.out.println("ERROR: " + e);
                    }


                    bufferedReader.close();
                    return stockName;
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        protected void onPostExecute(String response) {
            if (response == null) {
                response = "THERE WAS AN ERROR";
            }

            pdLoading.dismiss();

        }
    }

}
