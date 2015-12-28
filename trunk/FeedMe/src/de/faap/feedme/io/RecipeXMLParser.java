package de.faap.feedme.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.ContentValues;
import android.util.Log;
import de.faap.feedme.provider.RecipeDatabaseHelper;
import de.faap.feedme.util.Ingredient;
import de.faap.feedme.util.Recipe;
import de.faap.feedme.util.Recipe.Effort;

public class RecipeXMLParser {
    private static String FILE_PATH = "/mnt/sdcard/recipes.xml";
    private static String SCHEMA_PATH = "/mnt/sdcard/recipe.xsd";

    private static String LOG_TAG = "faap.feedme.xmlparse";

    HashSet<String> recipeNames = new HashSet<String>();

    ArrayList<Recipe> recipes = new ArrayList<Recipe>();

    private ParseStates state = ParseStates.BEGIN;

    private enum ParseStates {
	BEGIN, IN_RECIPES, IR_BEGIN_NAME_NEXT, IR_TYPES, IR_CUISINES, IR_PREPARATION, IR_INGREDIENTS, II_NAME, II_AMOUNT, II_END, IN_INNERMOST, END, IR_PORTIONS
    }

    enum ValidTags {
	recipes, recipe, name, type, preparation, portions, cuisine, ingredient, amount

    }

    boolean openingTagAllowed = true; // to check if elements are illegally
				      // nested

    boolean hadType = false; // additional flag to check if at least one type
			     // has been given

    enum ValidAttributes {
	effort, unit
    }

    private HashMap<String, ContentValues> ingredientsTable = new HashMap<String, ContentValues>();
    private HashMap<String, ContentValues> categoriesTable = new HashMap<String, ContentValues>();
    private HashMap<String, ContentValues> cuisinesTable = new HashMap<String, ContentValues>();
    private HashMap<String, ContentValues> effortsTable = new HashMap<String, ContentValues>();
    private HashMap<String, ContentValues> unitsTable = new HashMap<String, ContentValues>();

    private ArrayList<ContentValues> recipesTable = new ArrayList<ContentValues>();
    private ArrayList<ContentValues> ingredientsRecipeTable = new ArrayList<ContentValues>();
    private ArrayList<ContentValues> categoriesRecipeTable = new ArrayList<ContentValues>();

    public boolean reparseRecipeDatabase() {
	boolean parseSuccess = parseXMLFile(FILE_PATH);
	if (!parseSuccess)
	    return false;

	// now create data sets

	int referenceKey;
	Recipe recipe;
	for (int i = 0; i < recipes.size(); i++) {
	    recipe = recipes.get(i);
	    ContentValues recipesTableEntry = new ContentValues(5);
	    // set cuisine
	    referenceKey = pushCuisine(recipe.getCuisine(), cuisinesTable);
	    recipesTableEntry.put(ValidTags.cuisine.toString(), referenceKey);

	    // set effort
	    referenceKey = pushEffort(recipe.getEffort(), effortsTable);
	    recipesTableEntry.put(ValidAttributes.effort.toString(),
		    referenceKey);

	    // set portions
	    recipesTableEntry.put(ValidTags.portions.toString(),
		    recipe.getPortions());

	    // set preparation
	    recipesTableEntry.put(ValidTags.preparation.toString(),
		    recipe.getPreparation());

	    // set categories
	    ContentValues categoriesRecipeTableEntry = new ContentValues();
	    for (String category : recipe.getCategories()) {
		referenceKey = pushCategory(category, categoriesTable);
		categoriesRecipeTableEntry.put(ValidTags.recipe.toString(), i);
		categoriesRecipeTableEntry.put(ValidTags.type.toString(),
			referenceKey);
		categoriesRecipeTable.add(categoriesRecipeTableEntry);
	    }

	    // set ingredients
	    ContentValues ingredientsRecipeTableEntry = new ContentValues();
	    for (Ingredient ingredient : recipe.getIngredients()) {
		referenceKey = pushIngredient(ingredient, ingredientsTable,
			unitsTable);
		ingredientsRecipeTableEntry.put(ValidTags.recipe.toString(), i);
		ingredientsRecipeTableEntry.put(
			ValidTags.ingredient.toString(), referenceKey);
		ingredientsRecipeTableEntry.put(ValidTags.amount.toString(),
			ingredient.quantity);
		ingredientsRecipeTable.add(ingredientsRecipeTableEntry);
	    }

	    recipesTable.add(recipesTableEntry);

	}

	return true;
    }

    private int pushIngredient(Ingredient ingredient,
	    Map<String, ContentValues> ingredientsTable,
	    Map<String, ContentValues> unitsTable)
	    throws IllegalArgumentException {
	if (ingredientsTable.containsKey(ingredient.name)) {
	    // check if the units match
	    ContentValues storedIngredient = ingredientsTable
		    .get(ingredient.name);
	    int storedUnitKey = storedIngredient
		    .getAsInteger(ValidAttributes.unit.toString());
	    Ingredient.Unit storedUnit = getUnitForKey(storedUnitKey,
		    unitsTable);
	    if (!storedUnit.equals(ingredient.unit)) {
		throw new IllegalArgumentException(
			"Ingredients with the same name, must have the same unit-type. "
				+ ingredient.name + " has "
				+ ingredient.unit.toString() + " as well as "
				+ storedUnit.toString());
	    }
	    return ingredientsTable.get(ingredient.name).getAsInteger("key");
	}
	ContentValues entry = new ContentValues(3);
	int unitKey = putUnit(ingredient.unit, unitsTable);
	entry.put("key", ingredientsTable.size());
	entry.put(ValidTags.name.toString(), ingredient.name.toString());
	entry.put(ValidAttributes.unit.toString(), unitKey);
	ingredientsTable.put(ingredient.name, entry);
	return 0;
    }

    private int putUnit(Ingredient.Unit unit,
	    Map<String, ContentValues> unitsTable) {
	if (unitsTable.containsKey(unit.toString())) {
	    return unitsTable.get(unit.toString()).getAsInteger("key");
	}

	ContentValues entry = new ContentValues(2);
	entry.put("key", unitsTable.size());
	entry.put(ValidTags.name.toString(), unit.toString());
	unitsTable.put(unit.toString(), entry);
	return unitsTable.size() - 1;
    }

    private Ingredient.Unit getUnitForKey(int key,
	    Map<String, ContentValues> unitsTable) {
	for (ContentValues unitRecord : unitsTable.values()) {
	    if (unitRecord.getAsInteger("key") == key) {
		return Ingredient.Unit.valueOf(unitRecord
			.getAsString(ValidTags.name.toString()));
	    }
	}
	return null;
    }

    private int pushCategory(String category,
	    Map<String, ContentValues> categoriesTable) {
	if (categoriesTable.containsKey(category)) {
	    return categoriesTable.get(category).getAsInteger("key");
	}

	ContentValues entry = new ContentValues(2);
	entry.put("key", categoriesTable.size());
	entry.put(ValidTags.name.toString(), category);
	categoriesTable.put(category, entry);
	return categoriesTable.size() - 1;
    }

    private int pushEffort(Effort effort,
	    Map<String, ContentValues> effortsTable) {
	if (effortsTable.containsKey(effort.toString())) {
	    return effortsTable.get(effort.toString()).getAsInteger("key");
	}

	ContentValues entry = new ContentValues(2);
	entry.put("key", effortsTable.size());
	entry.put(ValidTags.name.toString(), effort.toString());
	effortsTable.put(effort.toString(), entry);
	return effortsTable.size() - 1;
    }

    private int pushCuisine(String cuisine,
	    Map<String, ContentValues> cuisinesTable) {
	if (cuisinesTable.containsKey(cuisine)) {
	    return cuisinesTable.get(cuisine).getAsInteger("key");
	}

	ContentValues entry = new ContentValues(2);
	entry.put("key", cuisinesTable.size());
	entry.put(ValidTags.name.toString(), cuisine);
	cuisinesTable.put(cuisine, entry);
	return cuisinesTable.size() - 1;
    }

    public ContentValues[] getValuesForTable(RecipeDatabaseHelper.Tables table) {
	switch (table) {
	case Recipes:
	    return recipesTable.toArray(new ContentValues[recipesTable.size()]);
	case Categories:
	    return categoriesRecipeTable
		    .toArray(new ContentValues[categoriesRecipeTable.size()]);
	case Cuisine:
	    return cuisinesTable.values().toArray(
		    new ContentValues[cuisinesTable.size()]);
	case Effort:
	    return effortsTable.values().toArray(
		    new ContentValues[effortsTable.size()]);
	case Ingredients:
	    return ingredientsTable.values().toArray(
		    new ContentValues[ingredientsTable.size()]);
	case One_takes:
	    return ingredientsRecipeTable
		    .toArray(new ContentValues[ingredientsRecipeTable.size()]);
	case Type:
	    return categoriesTable.values().toArray(
		    new ContentValues[categoriesTable.size()]);
	default:
	    assert false;
	    return null;
	}

    }

    private boolean parseXMLFile(String name) {

	InputStream in;
	File inFile;
	try {
	    inFile = new File(name);
	    in = new FileInputStream(inFile);
	} catch (FileNotFoundException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    return false;
	}

	return parseStream(in);
    }

    private boolean parseStream(InputStream input) {

	try {
	    XmlPullParser pullParser = XmlValidatingParserFactory
		    .newValidatingParser(SCHEMA_PATH);
	    pullParser.setInput(input, "UTF-8");

	    int eventType = pullParser.next(); // skip start document

	    while (eventType != XmlPullParser.END_DOCUMENT) {
		switch (eventType) {
		case XmlPullParser.START_TAG:
		    System.out.println("Start: " + pullParser.getName());
		    ValidTags tag = ValidTags.valueOf(pullParser.getName());
		    switch (tag) {
		    case recipes:
			state = ParseStates.IN_RECIPES;
			break;
		    case recipe:
			Recipe parsedRecipe = parseRecipe(pullParser);
			addRecipeToData(parsedRecipe);
			break;
		    default:
			throw getStandardParseException(pullParser);
		    }
		    break;
		case XmlPullParser.END_TAG:
		    System.out.println("End: " + pullParser.getName());
		    break;
		}
		eventType = pullParser.next();
	    }
	} catch (IllegalArgumentException e) {
	    Log.d(LOG_TAG,
		    "Illegal XML Tag in recipe: " + e.getLocalizedMessage());
	    return false;
	} catch (XmlPullParserException e1) {
	    Log.e(LOG_TAG, "Could not parse the recipe file. See stack trace.");
	    e1.printStackTrace();
	    return false;

	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    return false;
	}

	return true;
    }

    private void addRecipeToData(Recipe recipe) {
	recipes.add(recipe);
    }

    private IllegalArgumentException getStandardParseException(
	    XmlPullParser pullParser) {
	return new IllegalArgumentException(
		"You need to keep to the xml-schema-file. The tag "
			+ pullParser.getName() + "is illegal here (Line: "
			+ pullParser.getLineNumber() + ").");
    }

    /**
     * Parses a single recipe from the xml file.
     * 
     * @param pullParser
     *            The pull parser used for xml parsing. Must be pointing at
     *            opening recipe-tag.
     * @throws IllegalArgumentException
     *             If the recipe does not have the correct recipe format.
     */
    private Recipe parseRecipe(XmlPullParser pullParser)
	    throws IllegalArgumentException {
	Recipe newRecipe = null;
	hadType = false;
	try {
	    int eventType = pullParser.getEventType();
	    assert eventType == XmlPullParser.START_TAG
		    && pullParser.getName().equals(ValidTags.recipe.toString());

	    newRecipe = new Recipe(pullParser.getName());
	    newRecipe
		    .setEffort(Effort.valueOf(pullParser.getAttributeValue(0)));
	    eventType = pullParser.next();
	    state = ParseStates.IR_BEGIN_NAME_NEXT;

	    ValidTags currentTag;
	    Ingredient currentIngredient;
	    while (!(eventType == XmlPullParser.END_TAG && pullParser.getName()
		    .equals(ValidTags.recipe.toString()))) {
		if (eventType == XmlPullParser.START_TAG) {
		    currentTag = ValidTags.valueOf(pullParser.getName());
		    switch (currentTag) {
		    case name:
			state = ParseStates.IR_BEGIN_NAME_NEXT;
			break;
		    case ingredient:
			state = ParseStates.IR_INGREDIENTS;
			currentIngredient = parseIngredient(pullParser);
			if (!newRecipe.addIngredient(currentIngredient)) {
			    throw new IllegalArgumentException(
				    "Duplicate ingredient "
					    + currentIngredient.name
					    + " (Line: "
					    + pullParser.getLineNumber() + ").");
			}
			assert pullParser.getName().equals(
				ValidTags.ingredient.toString())
				&& pullParser.getEventType() == XmlPullParser.END_TAG;
			break;
		    case cuisine:
			state = ParseStates.IR_CUISINES;
			break;
		    case type:
			state = ParseStates.IR_TYPES;
			break;
		    case preparation:
			state = ParseStates.IR_PREPARATION;
			break;
		    case portions:
			state = ParseStates.IR_PORTIONS;
		    default:
			throw new IllegalStateException("Illegal parse state!");
		    }
		} else if (eventType == XmlPullParser.TEXT) {
		    switch (state) {
		    case IR_BEGIN_NAME_NEXT:
			assert newRecipe.getName() == null; // xml schema
			// validation guarantuees uniqueneness (of name per
			// recipe)
			// now check global uniqueness of recipe names
			newRecipe.setName(pullParser.getText());
			if (recipeNames.contains(newRecipe.getName())) {
			    throw new IllegalArgumentException(
				    "Duplicate recipe name "
					    + newRecipe.getName() + " (Line: "
					    + pullParser.getLineNumber() + ").");
			}
			recipeNames.add(newRecipe.getName());
			break;
		    case IR_CUISINES:
			assert newRecipe.getCuisine() == null; // validation
							       // invariant
			newRecipe.setCuisine(pullParser.getText());
			break;
		    case IR_TYPES:
			if (!newRecipe.addType(pullParser.getText())) {
			    throw new IllegalArgumentException(
				    "Duplicate type " + pullParser.getText()
					    + " (Line: "
					    + pullParser.getLineNumber() + ").");
			}
			break;
		    case IR_PREPARATION:
			assert newRecipe.getPreparation() == null; // "xml schema validation"
								   // guarantees
								   // uniqueness
			newRecipe.setPreparation(pullParser.getText());
			break;
		    case IR_PORTIONS:
			assert newRecipe.getPortions() == -1;
			newRecipe.setPortions(Integer.valueOf(pullParser
				.getText()));
			break;
		    default:
			throw new IllegalStateException("Illegal parse state.");
		    }
		} else if (eventType == XmlPullParser.END_TAG) {
		    currentTag = ValidTags.valueOf(pullParser.getName());
		    switch (currentTag) {
		    case name:
			state = ParseStates.IR_TYPES;
			break;
		    case preparation:
			state = ParseStates.IR_INGREDIENTS;
		    }
		}
		eventType = pullParser.next();
	    }

	    // reached end tag, check if everything necessary was there
	    if (!recipeIsComplete(newRecipe)) {
		throw new IllegalArgumentException(
			"A recipe needs all elements as specified in the .xsd file.");
	    }
	} catch (XmlPullParserException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	Log.d(LOG_TAG, "Recipe: " + newRecipe);

	return newRecipe;
    }

    private Ingredient parseIngredient(XmlPullParser pullParser)
	    throws XmlPullParserException, IOException {
	int eventType = pullParser.getEventType();
	assert pullParser.getName().equals(ValidTags.ingredient.toString());
	Ingredient ingredient = new Ingredient();
	state = ParseStates.II_NAME;
	eventType = pullParser.next();
	assert eventType == XmlPullParser.START_TAG
		&& pullParser.getName().equals(ValidTags.name.toString());
	ingredient.name = pullParser.nextText();
	eventType = pullParser.next();
	state = ParseStates.II_AMOUNT;
	assert eventType == XmlPullParser.START_TAG
		&& pullParser.getName().equals(ValidTags.amount.toString());
	ingredient.unit = Ingredient.Unit.valueOf(pullParser
		.getAttributeValue(0));
	ingredient.quantity = Double.valueOf(pullParser.nextText());
	eventType = pullParser.next();
	state = ParseStates.II_END;
	assert eventType == XmlPullParser.END_TAG
		&& pullParser.getName().equals(ValidTags.ingredient.toString());

	assert ingredient.name != null && ingredient.quantity != Double.NaN
		&& ingredient.unit != null : "Ingredient incomplete!";
	Log.d(LOG_TAG, "Ingredient: " + ingredient);
	return ingredient;
    }

    private boolean recipeIsComplete(Recipe newRecipe) {
	return (newRecipe.getIngredients().length >= 1
		&& newRecipe.getName() != null
		&& newRecipe.getName().length() >= 1
		&& newRecipe.getPortions() != -1 && newRecipe.getPreparation() != null);
    }
}
