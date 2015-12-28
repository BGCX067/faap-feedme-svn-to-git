package de.faap.feedme.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashSet;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import de.faap.feedme.io.RecipeXMLParser.ValidAttributes;
import de.faap.feedme.io.RecipeXMLParser.ValidTags;
import de.faap.feedme.util.Ingredient;
import de.faap.feedme.util.Recipe.Effort;

public class RecipeValidatingXmlPullParser implements XmlPullParser {

    private XmlPullParser parser;

    private HashSet<ValidNextTags> expected = new HashSet<ValidNextTags>();

    public RecipeValidatingXmlPullParser(XmlPullParser nonValidatingParser) {
	parser = nonValidatingParser;
	expected.add(ValidNextTags.recipes);
    }

    private boolean startNext = true;
    private boolean endNext = false;
    private ValidNextTags nextText = null;
    private boolean inIngredient = false;

    enum ValidNextTags {
	recipes, recipe, name, type, preparation, portions, cuisine, ingredient, amount, text

    }

    @Override
    public void defineEntityReplacementText(String entityName,
	    String replacementText) throws XmlPullParserException {
	parser.defineEntityReplacementText(entityName, replacementText);

    }

    @Override
    public int getAttributeCount() {
	return parser.getAttributeCount();
    }

    @Override
    public String getAttributeName(int index) {
	return parser.getAttributeName(index);
    }

    @Override
    public String getAttributeNamespace(int index) {
	return parser.getAttributeNamespace(index);
    }

    @Override
    public String getAttributePrefix(int index) {
	return parser.getAttributePrefix(index);
    }

    @Override
    public String getAttributeType(int index) {
	return parser.getAttributeType(index);
    }

    @Override
    public String getAttributeValue(int index) {
	// the amount attribute has a default value, provide it if none is given
	if (ValidTags.amount.toString().equals(parser.getName())) {
	    if (getAttributeCount() == 0) {
		return Ingredient.Unit.count.toString();
	    }
	}
	return parser.getAttributeValue(index);
    }

    @Override
    public String getAttributeValue(String namespace, String name) {
	// compare to overload taking only one integer parameter
	if (ValidTags.amount.toString().equals(parser.getName())) {
	    if (name.equals(ValidAttributes.unit.toString())) {
		if (getAttributeCount() == 0) {
		    return Ingredient.Unit.count.toString();
		}
	    }
	}
	return parser.getAttributeValue(namespace, name);
    }

    @Override
    public int getColumnNumber() {
	return parser.getColumnNumber();
    }

    @Override
    public int getDepth() {
	return parser.getDepth();
    }

    @Override
    public int getEventType() throws XmlPullParserException {
	return parser.getEventType();
    }

    @Override
    public boolean getFeature(String name) {
	return parser.getFeature(name);
    }

    @Override
    public String getInputEncoding() {
	return parser.getInputEncoding();
    }

    @Override
    public int getLineNumber() {
	return parser.getLineNumber();
    }

    @Override
    public String getName() {
	return parser.getName();
    }

    @Override
    public String getNamespace() {
	return parser.getNamespace();
    }

    @Override
    public String getNamespace(String prefix) {
	return parser.getNamespace(prefix);
    }

    @Override
    public int getNamespaceCount(int depth) throws XmlPullParserException {
	return parser.getNamespaceCount(depth);
    }

    @Override
    public String getNamespacePrefix(int pos) throws XmlPullParserException {
	return parser.getNamespacePrefix(pos);
    }

    @Override
    public String getNamespaceUri(int pos) throws XmlPullParserException {
	return parser.getNamespaceUri(pos);
    }

    @Override
    public String getPositionDescription() {
	return parser.getPositionDescription();
    }

    @Override
    public String getPrefix() {
	return parser.getPrefix();
    }

    @Override
    public Object getProperty(String name) {
	return parser.getProperty(name);
    }

    @Override
    public String getText() {
	String text = parser.getText();
	assert nextText != null;
	switch (nextText) {
	case type:
	case cuisine:
	    // remove line feeds and tabs
	    text = text.replaceAll("(\\n|\\r|\\t)+", " ");
	    Log.d("faap.feedme.xmlparse", "Type/Cuisine normalized: '" + text
		    + "'");
	    break;
	case name:
	    // remove any blocks of whitespace (including multispace and
	    String split[] = text.trim().split("(\\s)+");
	    StringBuffer xmlTokenString = new StringBuffer();
	    for (String part : split) {
		xmlTokenString.append(part);
		xmlTokenString.append(' ');
	    }
	    text = xmlTokenString.toString().trim();
	    Log.d("faap.feedme.xmlparse", "Name tokenized: '" + text + "'");
	    break;
	default:
	    text = text.trim();
	}

	return text;
    }

    @Override
    public char[] getTextCharacters(int[] holderForStartAndLength) {
	throw new UnsupportedOperationException(
		"getTextCharacters not supported");
	// return parser.getTextCharacters(holderForStartAndLength);
    }

    @Override
    public boolean isAttributeDefault(int index) {
	return parser.isAttributeDefault(index);
    }

    @Override
    public boolean isEmptyElementTag() throws XmlPullParserException {
	return parser.isEmptyElementTag();
    }

    @Override
    public boolean isWhitespace() throws XmlPullParserException {
	return parser.isWhitespace();
    }

    @Override
    public int next() throws XmlPullParserException, IOException {
	int event = parser.next();
	if (event == XmlPullParser.START_TAG) {
	    if (!startNext) {
		throw new XmlPullParserException(
			"A start tag is illegal here. Name: "
				+ parser.getName() + " (Line: "
				+ parser.getLineNumber() + ").");
	    }
	    ValidNextTags tag = ValidNextTags.valueOf(parser.getName());
	    if (!expected.contains(tag)) {
		throw new XmlPullParserException(
			"This start tag is illegal here: "
				+ parser.getName()
				+ ". Expected start tag (either): "
				+ Arrays.toString(expected
					.toArray(new ValidNextTags[expected
						.size()])));
	    }
	    nextText = tag; // holds with a view exceptions, which are handled
			    // below
	    startNext = false;
	    endNext = false;
	    switch (tag) {
	    case recipes:
		expected.clear();
		expected.add(ValidNextTags.recipe);
		startNext = true;
		nextText = null;
		break;
	    case recipe:
		startNext = true;
		nextText = null;
		expected.remove(ValidNextTags.recipes); // closing recipes not
							// allowed anymore
		expected.add(ValidNextTags.name);

		// check if the attribute has a legal format
		if (parser.getAttributeCount() != 1
			|| !parser.getAttributeName(0).equals(
				ValidAttributes.effort.toString())) {
		    throw new XmlPullParserException(
			    "Each recipe needs to have an effort! Types are: "
				    + Arrays.toString(Effort.values())
				    + "(Line: " + parser.getLineNumber() + ").");
		}
		break;
	    case ingredient:
		startNext = true;
		nextText = null;
		expected.clear();
		expected.add(ValidNextTags.name);
		inIngredient = true;
		break;
	    case amount:
		// it is legal to not give a unit explicitely, as there is a
		// default
		// this is returned in getAttributeValue(0) if so
		if (parser.getAttributeCount() > 1
			|| (parser.getAttributeCount() == 1 && !parser
				.getAttributeName(0).equals(
					ValidAttributes.unit.toString()))) {
		    throw new XmlPullParserException(
			    "Illegal unit type! Types are: "
				    + Arrays.toString(Ingredient.Unit.values())
				    + "(Line: " + parser.getLineNumber() + ").");
		}
		if (parser.getAttributeCount() == 1) {
		    try {
			Ingredient.Unit.valueOf(parser.getAttributeValue(0));
		    } catch (IllegalArgumentException e) {
			throw new XmlPullParserException(
				"Illegal unit type! Types are: "
					+ Arrays.toString(Ingredient.Unit
						.values()) + "(Line: "
					+ parser.getLineNumber() + ").");
		    }
		}
		break;
	    }
	} else if (event == XmlPullParser.END_TAG) {
	    if (!endNext) {
		throw new XmlPullParserException(
			"An end tag is illegal here. Name: " + parser.getName()
				+ " Line(" + parser.getLineNumber() + ").");
	    }
	    ValidNextTags tag = ValidNextTags.valueOf(parser.getName());
	    if (!expected.contains(tag)) {
		throw new XmlPullParserException(
			"This end tag is illegal here: "
				+ parser.getName()
				+ ". Expected end tag (either): "
				+ Arrays.toString(expected
					.toArray(new ValidNextTags[expected
						.size()])));
	    }
	    // There are some exceptions, but regularly after an end tag, there
	    // is a start tag
	    startNext = true;
	    endNext = false;
	    nextText = null;
	    switch (tag) {
	    case recipes:
		expected.clear();
		break;
	    case recipe:
		expected.add(ValidNextTags.recipes);
		startNext = true;
		endNext = true;
		break;
	    case name:
		expected.remove(ValidNextTags.name);
		if (inIngredient) {
		    expected.add(ValidNextTags.amount);
		} else {
		    expected.add(ValidNextTags.type);
		}
		break;
	    case type:
		expected.remove(ValidNextTags.type);
		expected.add(ValidNextTags.cuisine);
		break;
	    case cuisine:
		expected.add(ValidNextTags.preparation);
		break;
	    case preparation:
		expected.remove(ValidNextTags.cuisine);
		expected.remove(ValidNextTags.preparation);
		expected.add(ValidNextTags.portions);
		break;
	    case portions:
		expected.remove(ValidNextTags.portions);
		expected.add(ValidNextTags.ingredient);
	    case amount:
		expected.remove(ValidNextTags.amount);
		expected.add(ValidNextTags.ingredient);
		startNext = false;
		endNext = true;
		break;
	    case ingredient:
		expected.add(ValidNextTags.recipe);
		startNext = true;
		endNext = true;
		inIngredient = false;
		break;
	    }
	} else if (event == XmlPullParser.TEXT) {
	    if (nextText == null) {
		if (!parser.isWhitespace()) {
		    throw (new XmlPullParserException(
			    "Only whitespace is allowed here, no real text. (Line: "
				    + parser.getLineNumber() + ", Column: "
				    + parser.getColumnNumber() + ")."));
		}
		return next(); // skip ignorable whitespace
	    }

	    // normal text, format depends on tag. the formatting is done
	    // in getText()
	    switch (nextText) {
	    case amount:
		// must be a decimal, simply try to cast here, as this suffices
		String amountText = parser.getText();
		try {
		    Double.valueOf(amountText);
		} catch (NumberFormatException e) {
		    throw new XmlPullParserException(
			    "Amount must have decimal format (Line:"
				    + parser.getLineNumber() + ").");
		}
		break;
	    }
	    startNext = false;
	    endNext = true;
	}
	return event;
    }

    @Override
    public int nextTag() throws XmlPullParserException, IOException {
	int eventType = next();
	if (eventType == TEXT && isWhitespace()) { // skip whitespace
	    eventType = next();
	    assert false; // should never hit, actually, as next already skips
			  // whitespace
	}
	if (eventType != START_TAG && eventType != END_TAG) {
	    throw new XmlPullParserException("expected start or end tag", this,
		    null);
	}
	return eventType;

    }

    @Override
    public String nextText() throws XmlPullParserException, IOException {
	if (parser.getEventType() != START_TAG) {
	    throw new XmlPullParserException(
		    "parser must be on START_TAG to read next text");
	}
	int eventType = next();
	if (eventType == TEXT) {
	    String result = getText();
	    eventType = next();
	    if (eventType != END_TAG) {
		throw new XmlPullParserException(
			"event TEXT it must be immediately followed by END_TAG");
	    }
	    return result;
	} else if (eventType == END_TAG) {
	    return "";
	} else {
	    throw new XmlPullParserException(
		    "parser must be on START_TAG or TEXT to read text");
	}

    }

    @Override
    public int nextToken() throws XmlPullParserException, IOException {
	throw new UnsupportedOperationException("Not supported.");
	// return parser.nextToken();
    }

    @Override
    public void require(int type, String namespace, String name)
	    throws XmlPullParserException, IOException {
	parser.require(type, namespace, name);

    }

    @Override
    public void setFeature(String name, boolean state)
	    throws XmlPullParserException {
	parser.setFeature(name, state);

    }

    @Override
    public void setInput(Reader in) throws XmlPullParserException {
	parser.setInput(in);

    }

    @Override
    public void setInput(InputStream inputStream, String inputEncoding)
	    throws XmlPullParserException {
	parser.setInput(inputStream, inputEncoding);

    }

    @Override
    public void setProperty(String name, Object value)
	    throws XmlPullParserException {
	parser.setProperty(name, value);

    }

}
