package de.faap.feedme.io;

public class DatabaseUpdater implements IUpdateDatabase {
    RecipeXMLParser xmlParser;

    public DatabaseUpdater() {
	xmlParser = new RecipeXMLParser();
    }

    @Override
    public boolean isUpToDate() {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public boolean update() {
	return xmlParser.reparseRecipeDatabase();
    }

}
