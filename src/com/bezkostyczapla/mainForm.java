package com.bezkostyczapla;

import javax.swing.*;

import datechooser.beans.DateChooserCombo;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class mainForm {
    private JPanel mainPanel;
    private JButton refreshButton;
    private JButton drawGoldButton;
    private JPanel currencyPanel;
    private JPanel goldPanel;
    private JPanel northPanel;
    private JLabel goldRateLabel;
    private DateChooserCombo goldFromDatePicker;
    private DateChooserCombo goldToDatePicker;
    private JPanel parametersPanel;
    private JPanel chooseCurrenciesPanel;
    private JButton removeCurrenciesButton;
    private JButton addCurrenciesButton;
    public JList currenciesList;
    public JList selectedCurrenciesList;
    private JButton drawCurrencyButton;
    private JPanel drawPanel;
    private DateChooserCombo currencyToDatePicker;
    private DateChooserCombo currencyFromDatePicker;
    private JComboBox currencyCurrencyComboBox;
    private JLabel currencyCurrencyLabel;
    private JPanel selectCurrenciesLabelsPanel;
    private JButton sameDayCurrencyButton;
    private JButton sameDayGoldButton;
    private JScrollPane currenciesLabelPanel;
    private JScrollPane selectedCurrenciesPanel;

    private long DAY_IN_MS = 1000 * 60 * 60 * 24;


    public JList getCurrenciesList()
    {
        return currenciesList;
    }

    public void setCurrenciesList(JList currenciesList)
    {
        this.currenciesList = currenciesList;
    }

    public JList getSelectedCurrenciesList()
    {
        return selectedCurrenciesList;
    }

    public void setSelectedCurrenciesList(JList selectedCurrenciesList) { this.selectedCurrenciesList = selectedCurrenciesList; }

    private void loadCurrenciesAndGold()
    {
        String request = Main.getRequest("http://api.nbp.pl/api/cenyzlota");
        if(request == null) System.exit(1);

        ArrayList<Gold> golds = Gold.parseGold(request);

        if(golds != null && golds.size() > 0) {
            float price = golds.get(0).getPrice();
            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(3);
            df.setMinimumFractionDigits(3);
            goldRateLabel.setText(df.format(price) + " zł / g");
        }

        DefaultListModel model=(DefaultListModel)currenciesList.getModel();

        request = Main.getRequest("http://api.nbp.pl/api/exchangerates/tables/A/");
        if(request == null) System.exit(1);

        JSONArray currencies = new JSONArray(request);
        JSONObject currencyJSON = currencies.getJSONObject(0);
        var rates = currencyJSON.getJSONArray("rates");
        ArrayList<String> currList = new ArrayList<>();

        for(int i = 0; i < rates.length(); i++)
        {
            JSONObject rate = rates.getJSONObject(i);
            String curr = rate.getString("code") + " (" + rate.getString("currency") + ")";
            currList.add(curr);
        }

        currList.sort(String::compareTo);

        for(var c:currList)
        {
            model.addElement(c);
            currencyCurrencyComboBox.addItem(c);
        }

        if(currList.size() > 0)
        {
            String url = "http://api.nbp.pl/api/exchangerates/rates/A/" + currList.get(0).substring(0, 3) + "/";
            request = "[" + Main.getRequest(url) + "]";
            ArrayList<Currency> currs = Currency.parseCurrency(request);
            if(currs != null)
            {
                DecimalFormat df = new DecimalFormat();
                df.setMaximumFractionDigits(3);
                df.setMinimumFractionDigits(3);
                currencyCurrencyLabel.setText(": " + df.format(currs.get(0).getMid()) + " zł");
            }
        }
    }

    mainForm()
    {
        chooseCurrenciesPanel.revalidate();

        loadCurrenciesAndGold();

        drawGoldButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(goldFromDatePicker.getText().split("\\.").length > 3 || goldToDatePicker.getText().split("\\.").length > 3){
                    JOptionPane.showMessageDialog(new JFrame(), "Too many dates inserted.", "Incorrect dates.", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Calendar calFrom = goldFromDatePicker.getSelectedDate();
                Calendar calTo = goldToDatePicker.getSelectedDate();
                Date dateFrom = calFrom.getTime();
                Date dateTo = calTo.getTime();

                String min = "2013/01/02";
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                Date minDate = new Date();
                try {
                    minDate = sdf.parse(min);
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }
                if(dateFrom.getTime() < minDate.getTime()){
                    JOptionPane.showMessageDialog(new JFrame(), "Too far back. Minimum date is 2.01.2013.", "Incorrect dates.", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if(dateFrom.getTime() > dateTo.getTime()){
                    JOptionPane.showMessageDialog(new JFrame(), "From date cannot be later than To date.", "Incorrect dates.", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Calendar currentCalendar = Calendar.getInstance();

                if(calFrom.getTimeInMillis() > currentCalendar.getTimeInMillis() || calTo.getTimeInMillis() > currentCalendar.getTimeInMillis()){
                    JOptionPane.showMessageDialog(new JFrame(), "Cannot check future gold price.", "Incorrect dates.", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                long diff = dateTo.getTime() - dateFrom.getTime();
                diff = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

                ArrayList<Gold> golds = new ArrayList<Gold>();

                if(diff == 0){
                    String month =  String.valueOf(dateFrom.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonthValue());
                    if(month.length() == 1) month = "0" + month;
                    String day =  String.valueOf(dateFrom.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getDayOfMonth());
                    if(day.length() == 1) day = "0" + day;
                    String year =  String.valueOf(dateFrom.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear());

                    String url = "http://api.nbp.pl/api/cenyzlota/" + year + "-" + month + "-" + day;
                    String request = Main.getRequest(url);
                    golds = Gold.parseGold(request);

                    JOptionPane.showMessageDialog(new JFrame(), "Value of gold: " + golds.get(0).getPrice() + " zł / g.", "Gold value on: " + day + "/" + month + "/" + year, JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                else if(diff > 93){
                    int multiplier = (int)(diff / 93);
                    for(int i = 0; i < multiplier; i++){
                        Date tmpDate = new Date(dateFrom.getTime() + (93 * DAY_IN_MS));

                        String monthFrom =  String.valueOf(dateFrom.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonthValue());
                        if(monthFrom.length() == 1) monthFrom = "0" + monthFrom;
                        String dayFrom =  String.valueOf(dateFrom.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getDayOfMonth());
                        if(dayFrom.length() == 1) dayFrom = "0" + dayFrom;
                        String yearFrom =  String.valueOf(dateFrom.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear());

                        String monthTo =  String.valueOf(tmpDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonthValue());
                        if(monthTo.length() == 1) monthTo = "0" + monthTo;
                        String dayTo =  String.valueOf(tmpDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getDayOfMonth());
                        if(dayTo.length() == 1) dayTo = "0" + dayTo;
                        String yearTo =  String.valueOf(tmpDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear());

                        dateFrom = tmpDate;

                        String url = "http://api.nbp.pl/api/cenyzlota/" + yearFrom + "-" + monthFrom+ "-" + dayFrom + "/" + yearTo + "-" + monthTo + "-" + dayTo;
                        String request = Main.getRequest(url);
                        golds.addAll(Gold.parseGold(request));
                    }
                    String monthFrom =  String.valueOf(dateFrom.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonthValue());
                    if(monthFrom.length() == 1) monthFrom = "0" + monthFrom;
                    String dayFrom =  String.valueOf(dateFrom.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getDayOfMonth());
                    if(dayFrom.length() == 1) dayFrom = "0" + dayFrom;
                    String yearFrom =  String.valueOf(dateFrom.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear());

                    String monthTo =  String.valueOf(dateTo.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonthValue());
                    if(monthTo.length() == 1) monthTo = "0" + monthTo;
                    String dayTo =  String.valueOf(dateTo.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getDayOfMonth());
                    if(dayTo.length() == 1) dayTo = "0" + dayTo;
                    String yearTo =  String.valueOf(dateTo.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear());


                    String url = "http://api.nbp.pl/api/cenyzlota/" + yearFrom + "-" + monthFrom+ "-" + dayFrom + "/" + yearTo + "-" + monthTo + "-" + dayTo;
                    String request = Main.getRequest(url);
                    golds.addAll(Gold.parseGold(request));
                }
                else
                {
                    String monthFrom =  String.valueOf(dateFrom.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonthValue());
                    if(monthFrom.length() == 1) monthFrom = "0" + monthFrom;
                    String dayFrom =  String.valueOf(dateFrom.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getDayOfMonth());
                    if(dayFrom.length() == 1) dayFrom = "0" + dayFrom;
                    String yearFrom =  String.valueOf(dateFrom.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear());

                    String monthTo =  String.valueOf(dateTo.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonthValue());
                    if(monthTo.length() == 1) monthTo = "0" + monthTo;
                    String dayTo =  String.valueOf(dateTo.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getDayOfMonth());
                    if(dayTo.length() == 1) dayTo = "0" + dayTo;
                    String yearTo =  String.valueOf(dateTo.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear());


                    String url = "http://api.nbp.pl/api/cenyzlota/" + yearFrom + "-" + monthFrom+ "-" + dayFrom + "/" + yearTo + "-" + monthTo + "-" + dayTo;
                    String request = Main.getRequest(url);
                    golds.addAll(Gold.parseGold(request));
                }
                if(golds == null) return;
                var stat=Statistics.calculateStats(golds);
                JFrame frameChart = new Chart("Currency exchange rate chart", golds, stat);
                frameChart.setSize(800, 400);
                frameChart.setLocationRelativeTo(null);
                frameChart.setVisible(true);

            }
        });

        selectedCurrenciesList.setSelectionModel(new DefaultListSelectionModel() {
            @Override
            public void setSelectionInterval(int index0, int index1) {
                if(super.isSelectedIndex(index0)) {
                    super.removeSelectionInterval(index0, index1);
                }
                else {
                    super.addSelectionInterval(index0, index1);
                }
            }
        });

        currenciesList.setSelectionModel(new DefaultListSelectionModel() {
            @Override
            public void setSelectionInterval(int index0, int index1) {
                if(super.isSelectedIndex(index0)) {
                    super.removeSelectionInterval(index0, index1);
                }
                else {
                    super.addSelectionInterval(index0, index1);
                }
            }
        });

        refreshButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String request = Main.getRequest("http://api.nbp.pl/api/cenyzlota");
                ArrayList<Gold> golds = Gold.parseGold(request);
                if(golds != null && golds.size() > 0) {
                    float f = golds.get(0).getPrice();
                    DecimalFormat df = new DecimalFormat();
                    df.setMaximumFractionDigits(3);
                    df.setMinimumFractionDigits(3);
                    goldRateLabel.setText(df.format(f) + " zł / g");
                }
            }
        });


        addCurrenciesButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                var list = currenciesList.getSelectedValuesList();
                DefaultListModel<String> model = (DefaultListModel<String>) currenciesList.getModel();

                DefaultListModel<String> model2 = (DefaultListModel<String>) selectedCurrenciesList.getModel();

                for (var el:list)
                {
                    model.removeElement(el);
                    model2.addElement(el.toString());
                }
            }
        });

        removeCurrenciesButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                var list = selectedCurrenciesList.getSelectedValuesList();
                DefaultListModel<String> model = (DefaultListModel<String>) currenciesList.getModel();

                DefaultListModel<String> model2 = (DefaultListModel<String>) selectedCurrenciesList.getModel();

                for (var el:list)
                {
                    model2.removeElement(el);
                    model.addElement(el.toString());
                }
            }
        });

        drawCurrencyButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(selectedCurrenciesList.getModel().getSize() <= 0) return;

                if(currencyFromDatePicker.getText().split("\\.").length > 3 || currencyToDatePicker.getText().split("\\.").length > 3){
                    JOptionPane.showMessageDialog(new JFrame(), "Too many dates inserted.", "Incorrect dates.", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                var calFrom = currencyFromDatePicker.getSelectedDate();
                var calTo = currencyToDatePicker.getSelectedDate();
                Date dateFrom = calFrom.getTime();
                Date dateTo = calTo.getTime();

                String min = "2002/01/02";
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                Date minDate = new Date();
                try {
                    minDate = sdf.parse(min);
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }

                if(dateFrom.getTime() < minDate.getTime()){
                    JOptionPane.showMessageDialog(new JFrame(), "Too far back. Minimum date is 2.01.2002.", "Incorrect dates.", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if(dateFrom.getTime() > dateTo.getTime()){
                    JOptionPane.showMessageDialog(new JFrame(), "From date cannot be later than To date.", "Incorrect dates.", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Calendar currentCalendar = Calendar.getInstance();

                if(calFrom.getTimeInMillis() > currentCalendar.getTimeInMillis() || calTo.getTimeInMillis() > currentCalendar.getTimeInMillis()){
                    JOptionPane.showMessageDialog(new JFrame(), "Cannot check future exchange rates.", "Incorrect dates.", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                ArrayList<String> chosenCurrencies = new ArrayList<>();

                for(int i = 0; i < selectedCurrenciesList.getModel().getSize(); i++)
                {
                    chosenCurrencies.add(selectedCurrenciesList.getModel().getElementAt(i).toString());

                }
                ArrayList<ArrayList<Currency>> resultCurrencies = new ArrayList<>();

                long diff = dateTo.getTime() - dateFrom.getTime();
                diff = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

                String monthFrom =  String.valueOf(dateFrom.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonthValue());
                if(monthFrom.length() == 1) monthFrom = "0" + monthFrom;
                String dayFrom =  String.valueOf(dateFrom.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getDayOfMonth());
                if(dayFrom.length() == 1) dayFrom = "0" + dayFrom;
                String yearFrom =  String.valueOf(dateFrom.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear());

                String monthTo =  String.valueOf(dateTo.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonthValue());
                if(monthTo.length() == 1) monthTo = "0" + monthTo;
                String dayTo =  String.valueOf(dateTo.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getDayOfMonth());
                if(dayTo.length() == 1) dayTo = "0" + dayTo;
                String yearTo =  String.valueOf(dateTo.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear());


                if(diff > 93){
                    int multiplier = (int)(diff / 93);
                    ArrayList<Date> dates = new ArrayList<Date>();

                    dates.add(dateFrom);
                    Date tmpDate = new Date(dateFrom.getTime());
                    for(int i = 0; i < multiplier; i++) {
                        tmpDate = new Date(tmpDate.getTime() + (93 * DAY_IN_MS));

                        dates.add(tmpDate);
                    }
                    if(tmpDate.getTime() != dateTo.getTime()){
                        dates.add(dateTo);
                    }
                    for(int i = 0; i < dates.size() - 1; i++){
                        Calendar calendarFrom = Calendar.getInstance();
                        calendarFrom.setTime(dates.get(i));
                        Calendar calendarTo = Calendar.getInstance();
                        calendarTo.setTime(dates.get(i + 1));

                        for(String s:chosenCurrencies){
                            String code = s.substring(0, 3);
                            String monthF = Integer.toString(calendarFrom.get(Calendar.MONTH) + 1);
                            if(monthF.length() == 1) monthF = "0" + monthF;
                            String monthT = Integer.toString(calendarTo.get(Calendar.MONTH) + 1);
                            if(monthT.length() == 1) monthT = "0" + monthT;
                            String dayF = Integer.toString(calendarFrom.get(Calendar.DAY_OF_MONTH));
                            if(dayF.length() == 1) dayF = "0" + dayF;
                            String dayT = Integer.toString(calendarTo.get(Calendar.DAY_OF_MONTH));
                            if(dayT.length() == 1) dayT = "0" + dayT;
                            String url = "http://api.nbp.pl/api/exchangerates/rates/A/" + code + "/" + calendarFrom.get(Calendar.YEAR) + "-" + monthF + "-" + dayF + "/" + calendarTo.get(Calendar.YEAR) + "-" + monthT + "-" + dayT + "/?format=json";
                            String request = "[" + Main.getRequest(url) + "]";
                            ArrayList<Currency> currencies = Currency.parseCurrency(request);
                            boolean alreadyInSet = false;
                            for(int j = 0; j < resultCurrencies.size(); j++){
                                if(resultCurrencies.get(j).get(0).getCode().compareTo(currencies.get(0).getCode()) == 0){
                                    resultCurrencies.get(j).addAll(currencies);
                                    alreadyInSet = true;
                                    break;
                                }
                            }
                            if(alreadyInSet == false) resultCurrencies.add(currencies);
                        }
                    }
                }
                else{
                    for(String s:chosenCurrencies)
                    {
                        String code = s.substring(0, 3);
                        dateFrom = calFrom.getTime();

                        if(diff == 0){
                            String url = "http://api.nbp.pl/api/exchangerates/rates/A/" + code + "/" + yearFrom + "-" + monthFrom + "-" + dayFrom + "/?format=json";
                            String request = "[" + Main.getRequest(url) + "]";
                            ArrayList<Currency> currencies = Currency.parseCurrency(request);
                            resultCurrencies.add(currencies);

                            JOptionPane.showMessageDialog(new JFrame(), "Value of " + resultCurrencies.get(resultCurrencies.size() - 1).get(0).getCurrency() + ": " +
                                    resultCurrencies.get(resultCurrencies.size() - 1).get(0).getMid() + " zł.", code + " value on: " + dayFrom + "/" + monthFrom + "/" + yearFrom, JOptionPane.INFORMATION_MESSAGE);
                            continue;
                        }
                        else
                        {
                            String url = "http://api.nbp.pl/api/exchangerates/rates/A/" + code + "/" + yearFrom + "-" + monthFrom + "-" + dayFrom + "/" + yearTo + "-" + monthTo + "-" + dayTo + "/?format=json";
                            String request = "[" + Main.getRequest(url) + "]";
                            ArrayList<Currency> currencies = Currency.parseCurrency(request);
                            resultCurrencies.add(currencies);
                        }
                    }
                }
                if(resultCurrencies == null || diff == 0) return;

                ArrayList<Statistics> stats=new ArrayList<>();
                for(var el:resultCurrencies)
                {
                    stats.add(Statistics.calculateStatistics(el));
                }

                JFrame frameChart = new Chart("Currency exchange rate chart", resultCurrencies, stats);
                frameChart.setSize(800, 400);
                frameChart.setLocationRelativeTo(null);
                frameChart.setVisible(true);
            }
        });

        currencyCurrencyComboBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String currency = ((String) currencyCurrencyComboBox.getSelectedItem()).substring(0, 3);
                String url = "http://api.nbp.pl/api/exchangerates/rates/A/" + currency + "/";
                String request = "[" + Main.getRequest(url) + "]";



                ArrayList<Currency> currencies = Currency.parseCurrency(request);
                if(currencies != null)
                {
                    DecimalFormat df = new DecimalFormat();
                    df.setMaximumFractionDigits(3);
                    df.setMinimumFractionDigits(3);
                    currencyCurrencyLabel.setText(": " + df.format(currencies.get(0).getMid()) + " zł");
                }
            }
        });

        sameDayCurrencyButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                currencyToDatePicker.setSelectedDate(currencyFromDatePicker.getSelectedDate());
            }
        });

        sameDayGoldButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                goldToDatePicker.setSelectedDate(goldFromDatePicker.getSelectedDate());
            }
        });
    }

    public JPanel getMainPanel()
    {
        return mainPanel;
    }

}
