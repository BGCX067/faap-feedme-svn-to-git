package de.faap.feedme.util;

public interface IPreferences {
    public int[] getCheckedButtons();

    public void saveCheckedButtons(int[] checkedButtons);

    // TODO remove deprecated code
    public String[] getType();

    public void saveType(String[] type);

    public String[] getEffort();

    public void saveEffort(String[] effort);

    public String[] getCuisine();

    public void saveCuisine(String[] cuisine);

    // deprecated code end

    public String[] getWeek();

    public void saveWeek(String[] week);

    public int[] getNextUpdate();

    public void saveNextUpdate(int year, int month, int day);
}
