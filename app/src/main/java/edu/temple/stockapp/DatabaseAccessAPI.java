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

    private final String ADD_KEY = "add";
    private final String RETRIEVE_KEY = "retrieve";
    private final String UPDATE_KEY = "update";
    private final String GET_API_DATA_KEY = "get_data";

    private String theURL;
    private String awsURL = "http://ec2-52-14-232-94.us-east-2.compute.amazonaws.com/StockAppWebService.php";
    private String apiURL = "http://dev.markitondemand.com/MODApis/Api/v2/Quote/json/?symbol=";

    private ArrayList<Stock> stockList;

    private String stockPrice = "";


    /*
    When initializing this class, initialize a new ArrayList of Stock objects to be associated with the DatabaseAccessAPI class
     */
    DatabaseAccessAPI() {
        stockList = new ArrayList<>();
    }


    /*
    When the user adds a stock to the application, this method will access the API, pass in the information stored in the Stock object that was passed into the method, and add it into the database.
     */
    void addStockToDatabase(Stock stock) {
        theURL = awsURL;    //Set the url to use the AWS URL that my API/database is stored on
        String stockName = stock.getName();
        String stockPrice = stock.getPrice();
        String stockChart = stock.getChart();

        RetrieveFeedTask rft = new RetrieveFeedTask();
        try {
            rft.execute(ADD_KEY, stockName, stockPrice, stockChart).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

    }


    /*
    When populating the ListView in the NavFragment, this method will query the database to get the names of the symbols saved by the user.
     */
    ArrayList<Stock> RetrieveStocksFromDatabase() {
        theURL = awsURL;    //Set the url to use the AWS URL that my API/database is stored on
        RetrieveFeedTask rft = new RetrieveFeedTask();
        try {
            rft.execute(RETRIEVE_KEY, "", "", "").get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return stockList;
    }

    /*
    Each time the StockService runs, need to update the information in the database so that the user can get the most up to date information on their saved stocks. First need to get the updated stock price for each of the stock symbols that exist in the ArrayList that was passed in, then for each of those symbols, pass the new price on to the database so that when the ListView is repopulated, it is repopulated with the most up-to-date Stock objects.
     */
    void updateStockPrices(ArrayList<String> stockList) {
        String[] updatedStockPrices = new String[stockList.size()];
        String stockSymbol = "";
        RetrieveFeedTask rft;

        for (int i = 0; i < stockList.size(); i++) {    //Get updated prices for each stock symbol in the ArrayList
            stockSymbol = stockList.get(i);
            theURL = apiURL + stockSymbol;    //Set the url to use the API URL to retrieve stock information
            rft = new RetrieveFeedTask();
            try {
                rft.execute(GET_API_DATA_KEY, "", "", "").get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            updatedStockPrices[i] = stockPrice;


        }

        theURL = awsURL;

        for (int i = 0; i < stockList.size(); i++) {    //Update the corresponding stock symbols' prices in the database
            stockSymbol = stockList.get(i);
            rft = new RetrieveFeedTask();
            try {
                rft.execute(UPDATE_KEY, stockSymbol, updatedStockPrices[i], "").get();
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

            String key = params[0];
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
                    if (key.equals(ADD_KEY) || key.equals(RETRIEVE_KEY) || key.equals(UPDATE_KEY)) {    //Append parameters if the key is one of these (don't want these parameters when accessing the API)
                        // Append parameters to URL
                        Uri.Builder builder = new Uri.Builder()
                                .appendQueryParameter("key", params[0])
                                .appendQueryParameter("symbol", params[1])
                                .appendQueryParameter("price", params[2])
                                .appendQueryParameter("chart", params[3]);
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
                    String line;
                    if (key.equals(RETRIEVE_KEY)) { //If we have the RETRIEVE_KEY, we are assigning the new Stock object with the information obtained from the database
                        while ((line = bufferedReader.readLine()) != null) {
                            Stock stock = new Stock();
                            stock.setName(line);
                            line = bufferedReader.readLine();
                            stock.setPrice(line);
                            line = bufferedReader.readLine();
                            stock.setChart(line);

                            stockList.add(stock);   //Add the newly created Stock object to the list

                        }

                    } else if (key.equals(GET_API_DATA_KEY)) { //If we have the GET_API_DATA_KEY, get the prices from the API that will be used to update those in the database
                        JSONArray jsonArr = null;
                        line = bufferedReader.readLine();

                        jsonArr = new JSONArray("[" + line + "]");


                        JSONObject obj = jsonArr.getJSONObject(0);
                        stockPrice = obj.getString("LastPrice");
                    }
                    //Else no valuable information returned

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
