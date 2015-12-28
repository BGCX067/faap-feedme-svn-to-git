package de.faap.feedme.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RecipeDatabaseHelper extends SQLiteOpenHelper {
    public enum Tables {
	Recipes, Ingredients, Type, Effort, Cuisine, One_takes, Categories
    }

    private static final String DATABASE_NAME = "Recipe_database";
    private static final int DATABASE_VERSION = 1;

    public RecipeDatabaseHelper(Context context) {
	super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
	db.execSQL("CREATE TABLE " + Tables.Effort.toString() + " ("
		+ "_id INTEGER PRIMARY KEY," + "name TEXT" + ");");

	db.execSQL("CREATE TABLE " + Tables.Cuisine.toString() + " ("
		+ "_id INTEGER PRIMARY KEY," + "name TEXT" + ");");

	db.execSQL("CREATE TABLE " + Tables.Recipes.toString() + " ("
		+ "_id INTEGER PRIMARY KEY," + "name TEXT,"
		+ "preperation TEXT," + "effort INTEGER," + "cuisine INTEGER,"
		+ "portion INTEGER," + "FOREIGN KEY (effort) REFERENCES "
		+ Tables.Effort.toString() + "(_id),"
		+ "FOREIGN KEY (cuisine) REFERENCES "
		+ Tables.Cuisine.toString() + "(_id)" + ");");

	db.execSQL("CREATE TABLE " + Tables.Ingredients.toString() + " ("
		+ "_id INTEGER PRIMARY KEY," + "name TEXT," + "unit TEXT"
		+ ");");

	db.execSQL("CREATE TABLE " + Tables.Type.toString() + " ("
		+ "_id INTEGER PRIMARY KEY," + "name TEXT" + ");");

	db.execSQL("CREATE TABLE " + Tables.One_takes.toString() + " ("
		+ "name INTEGER," + "ingredient INTEGER,"
		+ "quantitiy INTEGER," + "FOREIGN KEY (name) REFERENCES "
		+ Tables.Recipes.toString() + "(_id),"
		+ "FOREIGN KEY (ingredient) REFERENCES "
		+ Tables.Ingredients.toString() + "(_id),"
		+ "PRIMARY KEY (name, ingredient)" + ");");

	db.execSQL("CREATE TABLE " + Tables.Categories.toString() + " ("
		+ "name INTEGER," + "type INTEGER,"
		+ "FOREIGN KEY (name) REFERENCES " + Tables.Recipes.toString()
		+ "(_id)," + "FOREIGN KEY (type) REFERENCES "
		+ Tables.Type.toString() + "(_id),"
		+ "PRIMARY KEY (name, type)" + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	db.execSQL("DROP TABLE IF EXISTS" + Tables.Recipes.toString());
	db.execSQL("DROP TABLE IF EXISTS" + Tables.Ingredients.toString());
	db.execSQL("DROP TABLE IF EXISTS" + Tables.Type.toString());
	db.execSQL("DROP TABLE IF EXISTS" + Tables.Effort.toString());
	db.execSQL("DROP TABLE IF EXISTS" + Tables.Cuisine.toString());
	db.execSQL("DROP TABLE IF EXISTS" + Tables.One_takes.toString());
	db.execSQL("DROP TABLE IF EXISTS" + Tables.Categories.toString());
	onCreate(db);
    }

}
