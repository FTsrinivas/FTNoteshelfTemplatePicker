package com.fluidtouch.noteshelf.commons;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StringLocalizer {
    Map<String, String> androidStrings;
    Map<String, String> iosStrings;
    Map<String, String> iosStrings2;
    ArrayList<String> iosStrings3 = new ArrayList<>();
    private Context context;

    public StringLocalizer(Context context) {
        this.context = context;
        AsyncTask.execute(this::printStrings);
    }

    private void printStrings() {

        //convert ios data to key value pair
        iosStrings = generateKeyValuePairs("iosen.txt", true);
        iosStrings2 = generateKeyValuePairs("iosger.txt", false);

        //print matching strings
        print("androiden.txt", false);
//
//        List<String> androidValues = new ArrayList<>(androidStrings.values());
//        List<String> iosValues = new ArrayList<>(iosStrings.values());
//
//        //Validate android strings
//        List<String> androidInvalidChars = new ArrayList<>();
//        androidInvalidChars.add("\n");
//        for (int i = 0; i <= 10; i++) {
//            String androidInvalidChar = "%" + i + "$d";
//            androidInvalidChars.add(androidInvalidChar);
//            String iosInvalidChar = "%" + i + "$s";
//            androidInvalidChars.add(iosInvalidChar);
//        }
//        for (String androidValue : androidValues) {
//            for (String androidInvalidChar : androidInvalidChars) {
//                androidValue = androidValue.replaceAll(androidInvalidChar, "");
//            }
//        }
//
//        //Validate ios strings
//        String[] iosInvalidChars = {"%@", "%d", "%ld", "\n"};
//        for (String iosValue : iosValues) {
//            for (String iosInvalidChar : iosInvalidChars) {
//                iosValue = iosValue.replaceAll(iosInvalidChar, "");
//            }
//        }
//
//        //Check if strings match
//        int count = 0;
//        for (String androidValue : iosStrings3) {
//            String key = androidValue;
//            System.out.println(iosStrings2.get(key));
//        }
//
//        Iterator iterator = iosStrings.keySet().iterator();
//        while (iterator.hasNext()) {
//            String key = iterator.next().toString();
//            System.out.println(iosStrings2.get(key));
//
//        }
//        Log.e("vineetx", "Non-matching words = " + count);
    }

    private Map<String, String> generateKeyValuePairs(String fileName, boolean isReverse) {
        Map<String, String> keyValuePairs = new HashMap<>();
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                //Ignore comments lines
                if (line.contains("//::::::::::::::::::::::::") || line.contains("//MARK:") || line.contains("<!--")) {
                    continue;
                }
                String[] pair = line.split("=");

                // result string should contain LHS and RHS.
                if (pair.length == 2) {
                    if (isReverse) {
                        String key = pair[1].trim();
                        String value = pair[0].trim();

                        //System.out.println(key + "," + androidStrings.get(key) + "," + value);
                        keyValuePairs.put(key, value);
                        iosStrings3.add(key);
                    } else {
                        String key = pair[0].trim();
                        String value = pair[1].trim();

                        keyValuePairs.put(key, value);
                    }
                } else {
                    String key = pair[0].trim();
                    //String value = pair[1].trim();

                    keyValuePairs.put(key, "0");
                    iosStrings3.add(key);
                }
            }
            reader.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return keyValuePairs;
    }

    private Map<String, String> print(String fileName, boolean isMatching) {
        Map<String, String> keyValuePairs = new HashMap<>();
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                //Ignore comments lines
                if (line.contains("//::::::::::::::::::::::::") || line.contains("//MARK:") || line.contains("<!--")) {
                    if (line.contains("<!--")) {
                        System.out.println(line);
                    }
                    continue;
                }
                String[] pair = line.split("=");

                // result string should contain LHS and RHS.
                if (pair.length == 2) {
                    String key = pair[0].trim();
                    String value = pair[1].trim();
                    if (isMatching) {
                        //print matching strings
                        if (iosStrings.get(value) != null) {
                            System.out.println("<string name='" + key + "'>" + iosStrings2.get(iosStrings.get(value)) + "</string>");
                        }
                    } else {
                        //print non matching strings
                        if (iosStrings.get(value) == null) {
                            System.out.println("<string name='" + key + "'>" + value + "</string>");
                        }
                    }
                }
            }
            reader.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return keyValuePairs;
    }
}
