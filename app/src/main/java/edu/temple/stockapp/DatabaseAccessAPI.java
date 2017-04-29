package edu.temple.stockapp;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

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
import java.util.concurrent.ExecutionException;


public class DatabaseAccessAPI {

    private final String ADD_TYPE = "add";
    private final String RETRIEVE_TYPE = "retrieve";
    private final String UPDATE_TYPE = "update";
    private final String GET_API_DATA = "get_data";

    private String theURL;
    private String awsURL = "http://ec2-52-14-232-94.us-east-2.compute.amazonaws.com/StockAppWebService.php";
    private String apiURL = "http://dev.markitondemand.com/MODApis/Api/v2/Quote/json/?symbol=";

    private ArrayList<Stock> stockList;

    private String stockPrice = "";


    DatabaseAccessAPI() {
        stockList = new ArrayList<>();
    }


    void addStockToDatabase(Stock stock) {
        theURL = awsURL;
        String stockName = stock.getName();
        String stockPrice = stock.getPrice();
        String stockChart = stock.getChart();

        RetrieveFeedTask rft = new RetrieveFeedTask();
        try {
            rft.execute(ADD_TYPE, stockName, stockPrice, stockChart).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

    }


    ArrayList<Stock> RetrieveStocksFromDatabase() {
        theURL = awsURL;
        RetrieveFeedTask rft = new RetrieveFeedTask();
        try {
            rft.execute(RETRIEVE_TYPE, "", "", "").get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return stockList;
    }

    void updateStockPrices(ArrayList<String> stockList) {
        String[] updatedStockPrices = new String[stockList.size()];
        String stockSymbol = "";
        RetrieveFeedTask rft;

        for (int i = 0; i < stockList.size(); i++) {
            stockSymbol = stockList.get(i);
            theURL = apiURL + stockSymbol;
            rft = new RetrieveFeedTask();
            try {
                rft.execute(GET_API_DATA, "", "", "").get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            updatedStockPrices[i] = stockPrice;


        }

        theURL = awsURL;

        for (int i = 0; i < stockList.size(); i++) {
            stockSymbol = stockList.get(i);
            rft = new RetrieveFeedTask();
            try {
                rft.execute(UPDATE_TYPE, stockSymbol, updatedStockPrices[i], "").get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }


        }


    }


    private class RetrieveFeedTask extends AsyncTask<String, String, String> {


        // CONNECTION_TIMEOUT and READ_TIMEOUT are in milliseconds
        static final int CONNECTION_TIMEOUT = 10000;
        static final int READ_TIMEOUT = 15000;


        private Exception exception;

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... params) {
            // Do some validation here
            HttpURLConnection conn;

            String type = params[0];
            String stock_name = params[1];
            String stock_price = params[2];
            String stock_chart = params[3];

            try {
                URL url = new URL(theURL);
                conn = (HttpURLConnection) url.openConnection();
                try {
                    // Setup HttpURLConnection class to send and receive data from php and mysql
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(READ_TIMEOUT);
                    conn.setConnectTimeout(CONNECTION_TIMEOUT);
                    conn.setRequestMethod("POST");

                    // setDoInput and setDoOutput method depict handling of both send and receive
                    conn.setDoInput(true);
                    conn.setDoOutput(true);

                    String query = "";
                    if (type.equals(ADD_TYPE) || type.equals(RETRIEVE_TYPE) || type.equals(UPDATE_TYPE)) {
                        // Append parameters to URL
                        Uri.Builder builder = new Uri.Builder()
                                .appendQueryParameter("type", params[0])    //Passes the 'type' parameter to the web wervice to determine what SQL query should be run, and the types are defined at the top of my code in a long list.
                                .appendQueryParameter("symbol", params[1])    //Passes in the selected 'line' when the submit button is pressed.
                                .appendQueryParameter("price", params[2])
                                .appendQueryParameter("chart", params[3]);  //Passes in the selected 'stop' when the submit button is pressed.
                        query = builder.build().getEncodedQuery();
                    }


                    // Open connection for sending data
                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(os, "UTF-8"));
                    writer.write(query);
                    writer.flush();
                    //writer.close();
                    //os.close();
                    conn.connect();
                } catch (Exception e) {

                }
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    if (type.equals(RETRIEVE_TYPE)) {
                        while ((line = bufferedReader.readLine()) != null) {
                            Stock stock = new Stock();
                            stock.setName(line);
                            line = bufferedReader.readLine();
                            stock.setPrice(line);
                            line = bufferedReader.readLine();
                            stock.setChart(line);

                            stockList.add(stock);

                        }

                    } else if (type.equals(GET_API_DATA)) { //Get the prices from the API that will be used to update those in the database
                        JSONArray jsonArr = null;
                        line = bufferedReader.readLine();

                        jsonArr = new JSONArray("[" + line + "]");


                        JSONObject obj = jsonArr.getJSONObject(0);
                        stockPrice = obj.getString("LastPrice");
                    }
                    //Else do nothing


                    bufferedReader.close();
                    return "";
                } finally {
                    conn.disconnect();
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


        }
    }

}
