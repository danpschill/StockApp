package edu.temple.stockapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

    AutoCompleteTextView actv;
    AutoTextCompleteAPI acAPI;

    String[] namesArray;
    String[] symbolsArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);


        actv = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);

        actv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if(s.length() > 2 && s.length() < 6) {

                    actv.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            actv.setText(symbolsArray[position]);
                            System.out.println(symbolsArray[position]);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                    acAPI = new AutoTextCompleteAPI();

                    acAPI.ex(s.toString());

                    namesArray = acAPI.getAutoCompleteNames();
                    symbolsArray = acAPI.getAutoCompleteSymbols();

//                System.out.println("namesArray size11: " + namesArray.length);
//                System.out.println("symbolsArray size11: " + symbolsArray.length);

                    if(namesArray != null) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(SearchActivity.this, android.R.layout.simple_list_item_1, namesArray);

                        actv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                if (namesArray.length > 1) {
                                    namesArray = acAPI.getAutoCompleteNames();
                                    symbolsArray = acAPI.getAutoCompleteSymbols();
//                                System.out.println("namesArray size: " + namesArray.length);
//                                System.out.println("symbolsArray size: " + symbolsArray.length);
                                    actv.setText(symbolsArray[position]);
                                } else {
                                    actv.setText(symbolsArray[0]);
                                }
                            }
                        });

                        actv.setAdapter(adapter);
                    }


//                }


            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });



        addButton = (Button) findViewById(R.id.add_button);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                stockEditText = (EditText) findViewById(R.id.enter_stock);
//                stockToAdd = stockEditText.getText().toString();
                stockToAdd = actv.getText().toString();

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

                } else {
                    Toast.makeText(SearchActivity.this, "This stock symbol was not found.", Toast.LENGTH_LONG).show();
                    setResult(SearchActivity.RESULT_CANCELED, null);
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

                    jsonArr = new JSONArray("[" + line + "]");


                    String stockSymbol = "";
                    String stockPrice = "";
                    JSONObject obj = jsonArr.getJSONObject(0);
                    try {
                        //TODO Create a Stock object from information retrieved
                        stockSymbol = obj.getString("Symbol");
                        stockPrice = obj.getString("LastPrice");
                        stock = new Stock(stockSymbol, "https://chart.yahoo.com/z?t=1d&s=" + stockSymbol, stockPrice);  //Add a Stock object with the stock symbol, chart link with the symbol appended to it, and the most recent price
                    } catch (Exception e) {
                        System.out.println("ERROR: " + e);
                    }


                    bufferedReader.close();
                    return stockSymbol;
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
