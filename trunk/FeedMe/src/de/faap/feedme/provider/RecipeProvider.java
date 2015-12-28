package de.faap.feedme.provider;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import de.faap.feedme.R;
import de.faap.feedme.util.Ingredient;
import de.faap.feedme.util.Recipe;

public class RecipeProvider implements IRecipeProvider {
    private static final int NO_TIME = 0;
    private static final int LITTLE_TIME = 1;
    private static final int MUCH_TIME = 2;

    private Context mContext;

    public RecipeProvider(Context context) {
	mContext = context;
    }

    @Override
    public Recipe getRecipe(String name) {
	int portions;
	double[] quantities;
	String[] units;
	String[] ingredients;
	String preperation;
	// String effortFromDB = "small";

	int _id;

	SQLiteDatabase db = new RecipeDatabaseHelper(mContext)
		.getReadableDatabase();

	// query to get _id, preperation and portions
	Cursor simpleRecipeData = db.rawQuery("SELECT _id,preperation,portion "
		+ "FROM " + RecipeDatabaseHelper.Tables.Recipes.toString()
		+ " " + "WHERE name = ? ", new String[] { name });

	_id = simpleRecipeData.getInt(0);
	preperation = simpleRecipeData.getString(1);
	portions = simpleRecipeData.getInt(2);
	simpleRecipeData.close();

	// query to get quantities, units and ingredients
	Cursor complexRecipeData = db
		.rawQuery(
			"SELECT temp.quantity,Ingredients.name,Ingredients.unit "
				+ "FROM (SELECT quantitiy,ingredient "
				+ "FROM One_takes "
				+ "WHERE name = "
				+ _id
				+ ") as temp "
				+ "INNER JOIN Ingredients on temp.ingredient = Ingredients._id ",
			null);

	int length = complexRecipeData.getCount();
	quantities = new double[length];
	units = new String[length];
	ingredients = new String[length];

	for (int i = 0; complexRecipeData.moveToNext(); i++) {
	    quantities[i] = complexRecipeData.getDouble(0);
	    ingredients[i] = complexRecipeData.getString(1);
	    units[i] = complexRecipeData.getString(2);
	}

	Ingredient.Unit[] internalUnits = new Ingredient.Unit[units.length];
	for (int i = 0; i < units.length; i++) {
	    internalUnits[i] = Ingredient.Unit.valueOf(units[i]);
	}

	db.close();
	return new Recipe(name, portions, quantities, internalUnits,
		ingredients, preperation);
    }

    @Override
    public String[] getNewWeek(int[] prefs) {
	// TODO query

	int[] time = convertPrefs(prefs);
	String[] week = new String[prefs.length];
	for (int i = 0; i < time.length; i++) {
	    if (time[i] == LITTLE_TIME) {
		// TODO choose either fertiggericht oder schnelles gericht
	    } else if (time[i] == MUCH_TIME) {
		// TODO choose großes gericht
		if (i != 6) {
		    if (time[i + 1] != NO_TIME) {
			// Cook for two days
			i++;
		    }
		}
	    } else {
		week[i] = "";
	    }
	}
	return week;
    }

    /**
     * Converts an array with radio-button ids into an with time indicators
     */
    private int[] convertPrefs(int[] prefs) {
	int[] convertedPrefs = new int[prefs.length];
	for (int i = 0; i < prefs.length; i++) {
	    if (prefs[i] == R.id.radio00 || prefs[i] == R.id.radio10
		    || prefs[i] == R.id.radio20 || prefs[i] == R.id.radio30
		    || prefs[i] == R.id.radio40 || prefs[i] == R.id.radio50
		    || prefs[i] == R.id.radio60) {
		convertedPrefs[i] = NO_TIME;
	    } else if (prefs[i] == R.id.radio01 || prefs[i] == R.id.radio11
		    || prefs[i] == R.id.radio21 || prefs[i] == R.id.radio31
		    || prefs[i] == R.id.radio41 || prefs[i] == R.id.radio51
		    || prefs[i] == R.id.radio61) {
		convertedPrefs[i] = LITTLE_TIME;
	    } else {
		convertedPrefs[i] = MUCH_TIME;
	    }
	}
	return convertedPrefs;
    }
}
