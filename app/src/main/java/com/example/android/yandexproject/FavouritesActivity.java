package com.example.android.yandexproject;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class FavouritesActivity extends AppCompatActivity {

    MainActivity.DBHelper dbHelper;

    private TextView allFavourites;

    Button clearFavouriteWords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);

        dbHelper = new MainActivity.DBHelper(this);
        clearFavouriteWords = (Button) findViewById(R.id.bt_clear_favourites);

        // Clear table. Refresh TextView with data.
        clearFavouriteWords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.delete("favourites", null, null);
                showDataBase();
            }
        });

        showDataBase();
    }

    // Fill TextView with data from table, from last to first.
    private void showDataBase () {
        allFavourites = (TextView) findViewById(R.id.tv_favourite_words);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query("favourites", null, null, null, null, null, null);
        if (c.moveToLast()) {
            int wordFromColIndex = c.getColumnIndex("wordFrom");
            int wordToColIndex = c.getColumnIndex("wordTo");

            do {
                allFavourites.append(
                        c.getString(wordFromColIndex) +
                                "\n" + c.getString(wordToColIndex)+ "\n" + "\n"+ "\n");
            } while (c.moveToPrevious());
        } else allFavourites.setText("Still empty \n");

        c.close();
    }
}