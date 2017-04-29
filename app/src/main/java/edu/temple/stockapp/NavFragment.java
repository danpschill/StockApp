package edu.temple.stockapp;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

public class NavFragment extends Fragment {

    private static final String PANES_PARAM = "two_pane";

    boolean twoPanes;
    StocksAdapter stocksAdapter;
    StockPortfolio stockPortfolio;
    ArrayList<String> stocks;
    ListView lv;


    private OnFragmentInteractionListener mListener;

    public NavFragment() {
        // Required empty public constructor
    }


    public static NavFragment newInstance(boolean param1) {
        NavFragment fragment = new NavFragment();
        Bundle args = new Bundle();
        args.putBoolean(PANES_PARAM, param1);   //This is the twoPanes boolean passed in from MainActivity
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            twoPanes = getArguments().getBoolean(PANES_PARAM);  //Set this Fragment's twoPanes to the twoPanes from

        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_nav, container, false);

        DatabaseAccessAPI databaseAccessAPI = new DatabaseAccessAPI();
        ArrayList<Stock> stockListFromDatabase = databaseAccessAPI.RetrieveStocksFromDatabase();

        stockPortfolio = new StockPortfolio();
        stocks = new ArrayList<>();
        for (int i = 0; i < stockListFromDatabase.size(); i++) {
            stocks.add(stockListFromDatabase.get(i).getName());
            stockPortfolio.addStock(stockListFromDatabase.get(i));
        }


        TextView prompt = (TextView) v.findViewById(R.id.prompt_message);
        if (stocks.size() == 0) {
            prompt.setText(getResources().getString(R.string.empty_stocks));
        } else {
            prompt.setText("List of stocks:");
        }


        lv = (ListView) v.findViewById(R.id.list_view);
        stocksAdapter = new StocksAdapter(getActivity(), stocks);


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Stock clickedStock = (Stock) stockPortfolio.getItem(position);
                if (!twoPanes) {

                    getFragmentManager()
                            .beginTransaction()
                            .replace(R.id.container1, DetailsFragment.newInstance(clickedStock))
                            .addToBackStack(null)
                            .commit();
                } else {
                    //TODO replace details fragment with fragment corresponding to details
                    getFragmentManager()
                            .beginTransaction()
                            .replace(R.id.container2, DetailsFragment.newInstance(clickedStock))
                            .addToBackStack(null)
                            .commit();
                }
            }
        });


        lv.setAdapter(stocksAdapter);


        return v;
    }

    public void addStock(String stock) {
        if (!stocks.contains(stock)) {
            stocks.add(stock);
            stocksAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(getActivity(), "You already have this stock in your list.", Toast.LENGTH_SHORT).show();
        }
    }

    public void addToStockList(Stock stock) {
        stockPortfolio.addStock(stock);
    }

    public ArrayList<String> returnList(){
        return stocks;
    }



    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name


    }
}
