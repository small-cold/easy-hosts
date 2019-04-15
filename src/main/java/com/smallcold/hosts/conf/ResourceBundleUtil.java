package com.smallcold.hosts.conf;

import java.util.ResourceBundle;

/*
 * Created by smallcold on 2017/9/19.
 */
public class ResourceBundleUtil {

    // Translatable properties
    private static final String BASE_NAME = "lang";

    private ResourceBundleUtil() {
        // no-op
    }

    public static ResourceBundle getBundle(){
        return ResourceBundle.getBundle(BASE_NAME);
    }

    /*
     * Look up a string in the properties file corresponding to the
     * default locale (i.e. the application's locale). If not found, the
     * search then falls back to the base controls.properties file,
     * containing the default string (usually English).
     */
    public static String getString(String key) {
        return ResourceBundle.getBundle(BASE_NAME).getString(key);
    }
}
