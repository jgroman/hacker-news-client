package cz.jtek.hackernewsclient.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

public class BindingUtils {

    public static String getUrlHost(String url) {
        String urlHost;
        try {
            URL generatedUrl = new URL(url);
            urlHost = generatedUrl.getHost();
        }
        catch (MalformedURLException mex) {
            urlHost = "...";
        }
        return urlHost;
    }

    public static String convertIntToString(int number) {
        return String.format(Locale.getDefault(), "%d", number);
    }
}
