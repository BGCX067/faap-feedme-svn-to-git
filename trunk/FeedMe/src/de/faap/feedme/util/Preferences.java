package de.faap.feedme.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Preferences implements IPreferences {

    private static final String PREFS = "de.faap.FeedMe_preferences";

    private SharedPreferences preferences;
    private Editor editor;

    public Preferences(Context context) {
	preferences = context.getSharedPreferences(PREFS, 0);
	editor = preferences.edit();
    }

    @Override
    public int[] getCheckedButtons() {
	int[] checkedButtons = new int[7];
	for (int i = 0; i < checkedButtons.length; i++) {
	    checkedButtons[i] = preferences.getInt("radioGroup" + i, -1);
	}
	return checkedButtons;
    }

    @Override
    public void saveCheckedButtons(int[] checkedButtons) {
	for (int i = 0; i < checkedButtons.length; i++) {
	    editor.putInt("radioGroup" + i, checkedButtons[i]);
	}
	editor.commit();
    }

    @Override
    public String[] getType() {
	String[] type = new String[preferences.getInt("type", 0)];
	for (int i = 0; i < type.length; i++) {
	    type[i] = preferences.getString("type" + i, "");
	}
	return type;
    }

    @Override
    public void saveType(String[] type) {
	editor.putInt("type", type.length);
	for (int i = 0; i < type.length; i++) {
	    editor.putString("type" + i, type[i]);
	}
	editor.commit();
    }

    @Override
    public String[] getEffort() {
	String[] effort = new String[preferences.getInt("effort", 0)];
	for (int i = 0; i < effort.length; i++) {
	    effort[i] = preferences.getString("effort" + i, "");
	}
	return effort;
    }

    @Override
    public void saveEffort(String[] effort) {
	editor.putInt("effort", effort.length);
	for (int i = 0; i < effort.length; i++) {
	    editor.putString("effort" + i, effort[i]);
	}
	editor.commit();
    }

    @Override
    public String[] getCuisine() {
	String[] cuisine = new String[preferences.getInt("cuisine", 0)];
	for (int i = 0; i < cuisine.length; i++) {
	    cuisine[i] = preferences.getString("cuisine" + i, "");
	}
	return cuisine;
    }

    @Override
    public void saveCuisine(String[] cuisine) {
	editor.putInt("cuisine", cuisine.length);
	for (int i = 0; i < cuisine.length; i++) {
	    editor.putString("cuisine" + i, cuisine[i]);
	}
	editor.commit();
    }

    @Override
    public String[] getWeek() {
	String[] week = new String[7];
	for (int i = 0; i < week.length; i++) {
	    week[i] = preferences.getString("week" + i, "");
	}
	return week;
    }

    @Override
    public void saveWeek(String[] week) {
	for (int i = 0; i < week.length; i++) {
	    editor.putString("week" + i, week[i]);
	}
	editor.commit();
    }

    @Override
    public int[] getNextUpdate() {
	int[] lastUpdate = new int[3];
	lastUpdate[0] = preferences.getInt("upd_year", 0);
	lastUpdate[1] = preferences.getInt("upd_month", 1);
	lastUpdate[2] = preferences.getInt("upd_day", 1);
	return lastUpdate;
    }

    @Override
    public void saveNextUpdate(int year, int month, int day) {
	editor.putInt("upd_year", year);
	editor.putInt("upd_month", month);
	editor.putInt("upd_day", day);
	editor.commit();
    }

}
