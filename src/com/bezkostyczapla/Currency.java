package com.bezkostyczapla;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class Currency
{
    private String currency;
    private String code;
    private float mid;

    public String getCurrency()
    {
        return currency;
    }

    public String getCode()
    {
        return code;
    }

    public float getMid()
    {
        return mid;
    }

    public Currency(String currency, String code, float mid)
    {
        this.currency = currency;
        this.code = code;
        this.mid = mid;
    }

    public static ArrayList<Currency> parseCurrency(String jsonString)
    {
        if(jsonString == null) return null;
        if(jsonString.contains("NotFound")) return null;

        ArrayList currencyList = new ArrayList();
        JSONArray currencies = new JSONArray(jsonString);

        JSONObject currencyJSON = currencies.getJSONObject(0);
        String name = "";
        if(jsonString.contains("currency")){
            name = currencyJSON.getString("currency");
        }
        String code = currencyJSON.getString("code");

        JSONArray rates = currencyJSON.getJSONArray("rates");

        for(int i = 0; i < rates.length(); i++)
        {
            currencyJSON = rates.getJSONObject(i);
            Currency currency = new Currency(name, code, currencyJSON.getFloat("mid"));
            currencyList.add(currency);

        }
        return currencyList;
    }
}
