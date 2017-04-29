package edu.temple.stockapp;

import java.io.Serializable;

/*
For the creation of Stocks so that all the information is stored together on the same POJO.
 */
public class Stock implements Serializable {

    private String name;
    private String chart;
    private String price;


    public Stock(){
        this.name = "";
        this.chart = "";
        this.price = "";
    };


    public Stock(String n, String c, String p){
        this.name = n;
        this.chart = c;
        this.price = p;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getChart() {
        return chart;
    }

    public void setChart(String chart) {
        this.chart = chart;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
