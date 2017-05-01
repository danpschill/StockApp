package edu.temple.stockapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


public class DetailsFragment extends Fragment {

    private static final String STOCK_PARAM = "stock";

    private Stock stock;

    Bitmap chartImage;

    private DetailsInterface mListener;

    public DetailsFragment() {
        // Required empty public constructor
    }


    public static DetailsFragment newInstance(Stock stock) {
        DetailsFragment fragment = new DetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(STOCK_PARAM, stock);   //When creating a new instance, pass in the Stock object which will be used for getting the chart image based on the chart link stored in the object
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            stock = (Stock) getArguments().getSerializable(STOCK_PARAM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_details, container, false);

        if (stock != null) {
            TextView stockName = (TextView) v.findViewById(R.id.stock_name);
            TextView stockPrice = (TextView) v.findViewById(R.id.stock_price);

            stockName.setText(stock.getName());
            stockPrice.setText(stock.getPrice());

            RetrieveFeedTask rft = new RetrieveFeedTask();  //Go to the internet using the chart image link to display it in the DetailsFragment
            try {
                rft.execute("", "", "").get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            ImageView imageView = (ImageView) v.findViewById(R.id.chart_image);
            imageView.setImageBitmap(chartImage);
        }


        return v;
        
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof DetailsInterface) {
            mListener = (DetailsInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface DetailsInterface {

        void getStock(String stock);

        void getStockList(Stock stockList);

        ArrayList<String> returnStockList();

    }


    private class RetrieveFeedTask extends AsyncTask<String, String, String> {


        private Exception exception;

        protected void onPreExecute() {
            super.onPreExecute();


        }

        protected String doInBackground(String... params) {
//            String email = emailText.getText().toString();
            // Do some validation here

            String search_query = params[0];

            try {
                URL url = new URL(stock.getChart());
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                    try {
                        chartImage = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    bufferedReader.close();
                    return "";
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

//            System.out.println("RESPONSE: " + response);


//            progressBar.setVisibility(View.GONE);
//            Log.i("INFO", response);
//            responseView.setText(response);
        }
    }
}
