package de.faap.feedme.util;

public class Ingredient {
    public double quantity;
    public Unit unit;
    public String name;

    public enum Unit {
	ml, g, count, none
    }

    public Ingredient() {
	quantity = Double.NaN;
	unit = null;
	name = null;
    }

    public Ingredient(double quantity, Unit unit, String name) {
	this.quantity = quantity;
	this.unit = unit;
	this.name = name;
    }

    /**
     * Multiplies quantity with a certain factor. Might take care of adapting
     * its unit in the future (e.g. from g to kg if necessary)
     * 
     * @param factor
     */
    public void adaptQuantity(double factor) {
	this.quantity *= factor;
    }

    public String getReadableUnit() {
	String unitString;
	if (unit.equals(Unit.count) || unit.equals(Unit.none))
	    unitString = "";
	else
	    unitString = unit.toString();
	return unitString;
    }

    private String getReadableQuantity() {
	if (quantity == 0 && unit.equals(Unit.none))
	    return "";

	return String.valueOf(quantity); // TODO: use nice format
    }

    @Override
    public String toString() {
	return getReadableQuantity() + getReadableUnit() + " " + name;
    }

    @Override
    public boolean equals(Object o) {
	if (!(o instanceof Ingredient))
	    return false;

	return ((Ingredient) o).name.equals(this.name);
    }

    @Override
    public int hashCode() {
	return name.hashCode();
    }
}
