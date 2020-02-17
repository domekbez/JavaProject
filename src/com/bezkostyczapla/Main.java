package com.bezkostyczapla;

import com.formdev.flatlaf.FlatDarculaLaf;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Main
{
    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException
    {
        FlatDarculaLaf.install();
        JFrame frame = new JFrame("Currency and gold exchange rate");
        setFrameParametres(frame);
    }

    private static void setFrameParametres(JFrame frame)
    {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200,900);
        frame.setMinimumSize(new Dimension(1200, 900));
        frame.setResizable(true);
        frame.setLocationRelativeTo(null);
        mainForm mainF = new mainForm();
        frame.setContentPane(mainF.getMainPanel());
        frame.setVisible(true);
    }

    public static String getRequest(String url)
    {
        if(netIsAvailable("https://www.google.com") != true){
            JOptionPane.showMessageDialog(new JFrame(), "Check Your internet connection and try again.", "No internet connection", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        if(netIsAvailable(url) != true){
            JOptionPane.showMessageDialog(new JFrame(), "Api has no such data.", "Api error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
            String response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body).join();
            return response;
    }

    private static boolean netIsAvailable(String siteURL) {
        try {
            final URL url = new URL(siteURL);
            final URLConnection conn = url.openConnection();
            conn.connect();
            conn.getInputStream().close();
            return true;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            return false;
        }
    }
}
