package edu.temple.stockapp;


import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class AutoTextCompleteAPI {

    String api_linkA = "http://dev.markitondemand.com/MODApis/Api/v2/Lookup/json?input=";
    String api_linkB = "&callback=myFunction";


    RetrieveFeedTask rft;

    String[] autoCompleteArrayNames;
    String[] autoCompleteArraySymbols;

    ArrayList<String> autoCompleteListNames;
    ArrayList<String> autoCompleteListSymbols;


    AutoTextCompleteAPI() {
    }


    public void ex(String currentInput) {
        RetrieveFeedTask rft = new RetrieveFeedTask();

        try {
            rft.execute(currentInput, "", "").get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }


    }

    public String[] getAutoCompleteNames() {




        return autoCompleteArrayNames;
    }

    public String[] getAutoCompleteSymbols() {




        return autoCompleteArraySymbols;
    }


    private void putListInArray(ArrayList<String> nameList, ArrayList<String> symbolList) {
        autoCompleteArrayNames = new String[nameList.size()];
        autoCompleteArraySymbols = new String[symbolList.size()];

        for (int i = 0; i < nameList.size(); i++) {
            autoCompleteArrayNames[i] = nameList.get(i);
            autoCompleteArraySymbols[i] = symbolList.get(i);
        }
    }



    private class RetrieveFeedTask extends AsyncTask<String, String, String> {


        private Exception exception;

        protected void onPreExecute() {
            super.onPreExecute();


//            //this method will be running on UI thread

//            progressBar.setVisibility(View.VISIBLE);
//            responseView.setText("");
        }

        protected String doInBackground(String... params) {


            String search_query = params[0];

            try {
                String editURL = api_linkA + search_query + api_linkB;
                editURL = editURL.replace(" ", "%20");

                URL url = new URL(editURL);
                System.out.println("URL: " + url);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                try {
                    JSONArray jsonArr = null;

                    System.out.println(urlConnection.getResponseCode());



                    InputStreamReader inputStream = new InputStreamReader(urlConnection.getInputStream());

                    System.out.println(urlConnection.getResponseCode());

                    if(urlConnection.getResponseCode() != 501) {

                        BufferedReader bufferedReader = new BufferedReader(inputStream);
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        line = bufferedReader.readLine();


                        if(!line.contains("Message")) {
//                    jsonArr = new JSONArray("[" + line + "]");
                            jsonArr = new JSONArray(line);


                            String stockName = "";
                            String stockSymbol = "";

//                    JSONArray innerArray = jsonArr.optJSONArray(0);


                            autoCompleteListNames = new ArrayList<>();
                            autoCompleteListSymbols = new ArrayList<>();


                            for (int i = 0; i < jsonArr.length(); i++) {
                                try {
                                    //TODO Create a Stock object from information retrieved
                                    if (jsonArr.getJSONObject(i) != null) {
                                        JSONObject obj = jsonArr.getJSONObject(i);
                                        if (obj.getString("Name") != null || !obj.getString("Name").equals("")) {
                                            stockName = obj.getString("Name");
//                                    System.out.println("NAME: " + stockName);
                                            autoCompleteListNames.add(stockName);
                                            stockSymbol = obj.getString("Symbol");
                                            autoCompleteListSymbols.add(stockSymbol);
                                        }
                                    }
                                } catch (Exception e) {
                                    System.out.println("ERROR: " + e);
                                }
                            }


                            bufferedReader.close();
                            putListInArray(autoCompleteListNames, autoCompleteListSymbols);
                        }
                    }

                    return "";
                }
                finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return "";
            }
        }

        protected void onPostExecute(String response) {
            if (response == null) {
                response = "THERE WAS AN ERROR";
            }


        }
    }

}
