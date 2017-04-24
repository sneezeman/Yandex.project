package com.example.android.yandexproject;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class HistoryActivity extends AppCompatActivity {

    MainActivity.DBHelper dbHelper;

    private TextView allHistory;

    Button clearHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        dbHelper = new MainActivity.DBHelper(this);
        clearHistory = (Button) findViewById(R.id.bt_clear_history);

        // Clear table. Refresh TextView with data.
        clearHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.delete("history", null, null);
                showDataBase();
            }
        });

        showDataBase();
    }

    // Fill TextView with data from table, from last to first.
    private void showDataBase () {
        allHistory = (TextView) findViewById(R.id.tv_history);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query("history", null, null, null, null, null, null);
        if (c.moveToLast()) {
            int wordFromColIndex = c.getColumnIndex("wordFrom");
            int wordToColIndex = c.getColumnIndex("wordTo");

            do {
                allHistory.append(c.getString(wordFromColIndex) +
                        "\n" + c.getString(wordToColIndex)+ "\n"+ "\n"+ "\n");
            } while (c.moveToPrevious());

        } else allHistory.setText("Still empty \n");

        c.close();
    }
}
