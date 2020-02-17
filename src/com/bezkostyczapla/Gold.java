package com.bezkostyczapla;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class Gold
{
    private String date;
    private float price;

    public String getDate()
    {
        return date;
    }

    public void setDate(String date)
    {
        this.date = date;
    }

    public float getPrice()
    {
        return price;
    }

    public void setPrice(float price)
    {
        this.price = price;
    }

    public Gold(String date, float price)
    {
        this.date = date;
        this.price = price;
    }

    public static ArrayList<Gold> parseGold(String jsonString)
    {
        if(jsonString == null) return null;
        ArrayList goldList = new ArrayList();
        JSONArray golds = new JSONArray(jsonString);
        for(int i = 0; i < golds.length(); i++)
        {
            JSONObject goldJson = golds.getJSONObject(i);
            Gold gold = new Gold(goldJson.getString("data"), goldJson.getFloat("cena"));
            goldList.add(gold);
        }
        return goldList;
    }
}
