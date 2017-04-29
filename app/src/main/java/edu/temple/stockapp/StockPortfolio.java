package edu.temple.stockapp;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.ArrayList;


public class StockPortfolio extends BaseAdapter {

    private ArrayList<Stock> stocks;

    StockPortfolio(){
        stocks = new ArrayList<>();
    }


    void addStock(Stock stock){
        stocks.add(stock);
    }


    @Override
    public int getCount() {
        return stocks.size();
    }

    @Override
    public Object getItem(int position) {
        return stocks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        return null;
    }
}

