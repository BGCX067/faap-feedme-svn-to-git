package de.faap.feedme.provider;

import de.faap.feedme.util.Recipe;

public interface IRecipeProvider {
    public Recipe getRecipe(String name);

    public String[] getNewWeek(int[] prefs);
}
