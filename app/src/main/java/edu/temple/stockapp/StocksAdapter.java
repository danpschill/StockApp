package edu.temple.stockapp;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by danpschill on 4/21/2017.
 */

public class StocksAdapter extends BaseAdapter {
    Context context;
    ArrayList<String> stocks;

    public StocksAdapter(Context c, ArrayList<String> stocks) {
        this.context = c;
        this.stocks = stocks;
    }


    @Override
    public int getCount() {
        return stocks.size();
    }

    @Override
    public Object getItem(int i) {
        return stocks.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        TextView textView = new TextView(context);
        String stockSymbol = stocks.get(i);
        stockSymbol = stockSymbol.toUpperCase();
        textView.setText(stockSymbol);
        textView.setTextColor(Color.parseColor("BLACK"));
        textView.setTextSize(20);


        return textView;
    }
}
