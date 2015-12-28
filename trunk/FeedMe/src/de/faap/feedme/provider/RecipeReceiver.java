package de.faap.feedme.provider;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

public class RecipeReceiver implements IRecipeReceiver {

    private RecipeDatabaseHelper openHelper;
    private SQLiteDatabase db;

    public RecipeReceiver(RecipeDatabaseHelper rd) {
	openHelper = rd;
    }

    @Override
    public void open() {
	db = openHelper.getWritableDatabase();
    }

    @Override
    public void addTable(String tableName, ContentValues[] valuesTable) {
	if (db.isOpen()) {
	    for (ContentValues values : valuesTable) {
		db.insert(tableName, null, values);
	    }
	}
    }

    @Override
    public void close() {
	db.close();
    }

}
