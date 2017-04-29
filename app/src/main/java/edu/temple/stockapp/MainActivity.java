package edu.temple.stockapp;

import android.app.AlarmManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements DetailsFragment.OnFragmentInteractionListener{

    final int STOCK_RESULT = 60;
    final String STOCK_KEY = "stock";
    final String STOCK_OBJ_KEY = "stock_obj";


    NavFragment navFragment;

    boolean twoPanes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //  Determine if only one or two panes are visible
        twoPanes = (findViewById(R.id.container2) != null);


        navFragment = NavFragment.newInstance(twoPanes);    //Create new instance of a NavFragment passing in result of 'twoPanes'


        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.container1, navFragment);
        fragmentTransaction.commit();



//        Runs the service every minute to update stocks
        ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                Intent i = new Intent(getBaseContext(), StockService.class);
                if(returnStockList() != null) {
                    i.putExtra("stock_list", returnStockList());    //If
                }

                startService(i);
            }
        }, 0, 1, TimeUnit.MINUTES); //Made a little longer so that didn't get "too many requests" error


        //If in landscape view, add a DetailsFragment to container2
        if (twoPanes) {
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.container2, new DetailsFragment());
            fragmentTransaction.commit();
        }

    }


    /*
    The add button in the Action Bar starts SearchActivity, and upon completion of that Activity, the program will end up here and deal with the information that was returned.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (STOCK_RESULT): {
                if (resultCode == SearchActivity.RESULT_OK) {   //If SearchActivity returns OK

                    String stockSymbol = data.getStringExtra(STOCK_KEY);    //Get the stock symbol that the user entered
                    Stock stock = (Stock) data.getSerializableExtra(STOCK_OBJ_KEY); //Get the Stock object that was created based on the stock symbol entered
                    getStock(stockSymbol);  //Pass into the NavFragment using the interface
                    getStockList(stock);  //Pass into the NavFragment using the interface

                    TextView prompt = (TextView) findViewById(R.id.prompt_message);
                    prompt.setText("List of stocks:");  //Update message

                }
                break;
            }
        }
    }


    /*
    MENU CODE
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.add_stock:    //If the add button in the Action Bar is selected, go to SearchActivity
                Intent i = new Intent(this, SearchActivity.class);
                startActivityForResult(i, STOCK_RESULT);

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }



    /*
    Adds the passed in String to the ArrayList of Strings in NavFragment that is used to populate the ListView
     */
    @Override
    public void getStock(String stock) {
        navFragment.addStock(stock);
    }

    /*
    Adds the passed in stock to the StockPortfolio in NavFragment
     */
    @Override
    public void getStockList(Stock stock) {
        navFragment.addToStockList(stock);
    }

    /*
    Gets the list of stocks from the NavFragment
     */
    @Override
    public ArrayList<String> returnStockList() {
        return navFragment.returnList();
    }



}
