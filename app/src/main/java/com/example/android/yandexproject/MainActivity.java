package com.example.android.yandexproject;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.yandexproject.utilities.NetworkUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    DBHelper dbHelper;

    private EditText mSearchBoxEditText;
    private TextView mSearchResultsTextView;
    private TextView mErrorMessageDisplay;
    private ProgressBar mLoadingIndicator;
    private Button mAddToFavourites;

    Button mSearchButton;
    Button mToFavourites;
    Button mToHistory;
    String spinnerFromChoice;
    String spinnerToChoice;

    HashMap<String, String> langMap = new HashMap<String, String>();

    final String[] languageArray = {"English", "Russian", "German", "French", "Spanish"};




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(this);

        fillTheHashMap(langMap);

        mSearchBoxEditText = (EditText) findViewById(R.id.et_search_box);
        mSearchResultsTextView = (TextView) findViewById(R.id.tv_translation_results_json);
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);
        mAddToFavourites = (Button) findViewById(R.id.add_to_favourites);
        mToFavourites = (Button) findViewById(R.id.go_to_favourites);
        mSearchButton = (Button) findViewById(R.id.search_button);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mToHistory = (Button) findViewById(R.id.history);
        final Spinner spinnerFrom = (Spinner) findViewById(R.id.spinner_lang_from);
        final Spinner spinnerTo = (Spinner) findViewById(R.id.spinner_lang_to);

        // "Translate" button onClickListener. Click leads to virtual keyboard hiding and
        // executes method makeTranslationQuery.
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
                makeTranslationQuery();
            }
        });

        // "Go to history" button onClickListener. Click leads to jump to HistoryActivity
        mToHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = MainActivity.this;
                Class destinationActivity = HistoryActivity.class;
                Intent startChildActivityIntent = new Intent(context, destinationActivity);
                startActivity(startChildActivityIntent);
            }
        });

        // "Go to history" button onClickListener. Click leads to jump to FavouritesActivity
        mToFavourites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = MainActivity.this;
                Class destinationActivity = FavouritesActivity.class;
                Intent startChildActivityIntent = new Intent(context, destinationActivity);
                startActivity(startChildActivityIntent);
            }
        });

        // "Add to favourites" button onClickListener. Button is shown if the translation was
        // successful.
        mAddToFavourites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItemIfUnique("favourites", mSearchResultsTextView.getText().toString(), true);
            }
        });


        // Adapter for 1 language
        ArrayAdapter<String> adapterFrom = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, languageArray);
        adapterFrom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrom.setAdapter(adapterFrom);
        spinnerFrom.setSelection(0);
        spinnerFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                spinnerFromChoice = spinnerFrom.getSelectedItem().toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });


        // Adapter for 2 language
        ArrayAdapter<String> adapterTo = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, languageArray);
        adapterTo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTo.setAdapter(adapterTo);
        spinnerTo.setSelection(1);
        spinnerTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                spinnerToChoice = spinnerTo.getSelectedItem().toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

    }

    // Fill the HashMap for URL creation
    private void fillTheHashMap(HashMap<String, String> hashMap) {
        hashMap.put("English","en");
        hashMap.put("Russian","ru");
        hashMap.put("German","de");
        hashMap.put("French","fr");
        hashMap.put("Spanish","es");
    }

    // Get data from spinner
    private String getLangKey (String spinnerChoise) {
        return langMap.get(spinnerChoise);
    }

    // Returns "language" part or URL.
    // Example of return: "en-ru".
    private String makeLangString() {
        StringBuilder chosenLangConnected = new StringBuilder();
        chosenLangConnected.append(getLangKey(spinnerFromChoice));
        chosenLangConnected.append("-");
        chosenLangConnected.append(getLangKey(spinnerToChoice));
        return chosenLangConnected.toString();
    }

    /**
     * This method will make the View for the JSON data visible and
     * hide the error message.
     */
    private void showJsonDataView() {
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mSearchResultsTextView.setVisibility(View.VISIBLE);
    }

    /**
     * This method will make the error message visible and hide the JSON
     */
    private void showErrorMessage() {
        mSearchResultsTextView.setVisibility(View.INVISIBLE);
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    // Two methods to show or hide "Add to favourites" button.
    private void showToFavButton() {
        mAddToFavourites.setVisibility(View.VISIBLE);
    }
    private void removeToFavButton() {
        mAddToFavourites.setVisibility(View.INVISIBLE);
    }

    // If Yandex.Translator returns code, different from 200, show Toast message
    private void checkAnswerCode(String code) {
        if (code.equals("401")) {
            Toast.makeText(getBaseContext(), "Неправильный API-ключ"
                    , Toast.LENGTH_SHORT).show();
        } else if (code.equals("402")) {
            Toast.makeText(getBaseContext(), "API-ключ заблокирован"
                    , Toast.LENGTH_SHORT).show();
        } else if (code.equals("404")) {
            Toast.makeText(getBaseContext(), "Превышено суточное ограничение на объем переведенного текста"
                    , Toast.LENGTH_SHORT).show();
        } else if (code.equals("413")) {
            Toast.makeText(getBaseContext(), "Превышен максимально допустимый размер текста"
                    , Toast.LENGTH_SHORT).show();
        } else if (code.equals("422")) {
            Toast.makeText(getBaseContext(), "Текст не может быть переведен"
                    , Toast.LENGTH_SHORT).show();
        } else if (code.equals("501")) {
            Toast.makeText(getBaseContext(), "Заданное направление перевода не поддерживается"
                    , Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * This method retrieves the search text from the EditText, constructs the
     * URL (using {@link NetworkUtils}), displays that URL in a TextView, and finally
     * fires off an AsyncTask to perform the GET request using {@link translationQueryTask}
     */
    private void makeTranslationQuery() {
        String translationQuery = mSearchBoxEditText.getText().toString();
        URL translationUrl = NetworkUtils.buildUrl(translationQuery, makeLangString());
        new translationQueryTask().execute(translationUrl);
    }

    public class translationQueryTask extends AsyncTask<URL, Void, String> {
        // Override onPreExecute to set the loading indicator to visible
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(URL... params) {
            URL searchUrl = params[0];
            String translationResults = null;
            try {
                translationResults = NetworkUtils.getResponseFromHttpUrl(searchUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return translationResults;
        }

        @Override
        protected void onPostExecute(String translationResults) {
            // As soon as the loading is complete, hide the loading indicator
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (translationResults != null && !translationResults.equals("")) {
                // Call showJsonDataView if we have valid, non-null results
                showJsonDataView();
                // If everything is alright, and translation pair of text is unique,
                // add it to history using #addItemIfUnique
                try {
                    JSONObject answer = new JSONObject(translationResults);
                    String answerString = answer.getString("text");
                    String statusCode = answer.getString("code");
                    String answerStringCorrected = answerString
                            .substring(2, answerString.length() - 2)
                            .replace("\\n","\n");
                    checkAnswerCode(statusCode);
                    mSearchResultsTextView.setText(answerStringCorrected);

                    addItemIfUnique("history", answerStringCorrected, false);
                    showToFavButton();


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {

                showErrorMessage();
                removeToFavButton();
            }
        }
    }

    /** Check does this translation pair of text already exists in table.
     *  @param tableName - name of table ("history"/"favourites"),
     *  @param answer - text from translation server
     *  @param fromFavourites - if true, shows toast message about adding pair of text to favouries
     */
    private void addItemIfUnique(String tableName, String answer,
                                 boolean fromFavourites) throws SQLException {
        boolean isUnique = true;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String translationQuery = mSearchBoxEditText.getText().toString();

        Cursor c = db.query(tableName, null, null, null, null, null, null);

        if (c.moveToFirst()) {
            int nameColIndex = c.getColumnIndex("wordFrom");
            int emailColIndex = c.getColumnIndex("wordTo");

            do {
                if (c.getString(nameColIndex).equals(translationQuery) && c.
                        getString(emailColIndex).equals(answer) ) {
                    isUnique = false;
                }
            } while (c.moveToNext());

            if (isUnique)
            {
                addItemToTable(tableName);
                if (fromFavourites) showAddedToast();
            }

        } else {
            addItemToTable(tableName);
            if (fromFavourites) showAddedToast();
        }


        c.close();


    }

    // Show toast "Translation added!"
    private void showAddedToast() {
        Toast toast = Toast.makeText(getApplicationContext(),
                "Translation added!", Toast.LENGTH_SHORT);
        toast.show();
    }

    // Adds pair of text to table
    private void addItemToTable (String tableName) {
        ContentValues cv = new ContentValues();
        String firstLang = mSearchBoxEditText.getText().toString();
        String secondLang = mSearchResultsTextView.getText().toString();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        cv.put("wordFrom", firstLang);
        cv.put("wordTo", secondLang);
        db.insert(tableName, null, cv);
    }

    //
    static class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, "dictionary", null, 1);
        }

        String tableHistory = ("create table history ("
                + "id integer primary key autoincrement,"
                + "wordFrom text,"
                + "wordTo text" + ");");

        String tableFavourites = ("create table favourites ("
                + "id integer primary key autoincrement,"
                + "wordFrom text,"
                + "wordTo text" + ");");

        @Override

        // Create 2 tables
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(tableHistory);
            db.execSQL(tableFavourites);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

}