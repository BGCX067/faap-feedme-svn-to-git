package de.faap.feedme.provider;

public class MockModel {
    private int fastCount;
    private int slowCount;

    private String[] fast = { "pizza diavolo", "pizza schinken",
	    "pizza speciale", "lasagne", "back-camentbert", "bratwürste",
	    "schlemmer-filet", "bacon-ei-käse-tomaten sandwich", "tortelini",
	    "maultaschen-suppe", "omelett", "schinken nudeln",
	    "bolognese s.65", "nudeln mit tomaten und anderem kraut" };

    private String[] slow = { "engl. frühstück", "chili", "wraps s.40",
	    "cheeseburger s.111", "curry aus arbeit", "chicken tikka",
	    "apfel curry autschi zeitung :S", "gebratene nudeln",
	    "curry-nudelpfanne", "reispfanne s.112", "kokos-orangensaft-pute",
	    "pizza-baguette", "crepes nach gusto s.20", "gyros-sandwich",
	    "cevapcici s.106", "ofen-geschnetzeltes s.95", "salat andrea",
	    "käse-sahne soße" };

    public MockModel() {
	fastCount = fast.length;
	slowCount = slow.length;
    }

    public String iWantFoodFast() {
	int rand = (int) (Math.random() * fastCount);
	return fast[rand];
    }

    public String IWillTakeMyTimeToCreateSomethingSpecial() {
	int rand = (int) (Math.random() * slowCount);
	return slow[rand];
    }

    public String[] getTheCoolFood() {
	return slow;
    }
}
