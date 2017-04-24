/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.yandexproject.utilities;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * These utilities will be used to communicate with the network.
 */
public class NetworkUtils {

    final static String YANDEXTR_BASE_URL =
            "https://translate.yandex.net/api/v1.5/tr.json/translate";
    final static String API_KEY = "key";
    final static String validKey = "trnsl.1.1.20170420T133440Z.bc2fe2a6d436b15e.db07e89c7e70362653273e8517ac3e822dcac4f8";
    final static String PARAM_TEXT = "text";
    final static String LANG = "lang";
    /**
     * Builds the URL used to query Yandex API.
     *
     * @param translateQuery The text that will be queried for.
     * @return The URL to use to query the API.
     */
    public static URL buildUrl(String translateQuery, String choosenLang) {
        Uri builtUri = Uri.parse(YANDEXTR_BASE_URL).buildUpon()
                .appendQueryParameter(API_KEY, validKey)
                .appendQueryParameter(PARAM_TEXT, translateQuery)
                .appendQueryParameter(LANG, choosenLang)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}