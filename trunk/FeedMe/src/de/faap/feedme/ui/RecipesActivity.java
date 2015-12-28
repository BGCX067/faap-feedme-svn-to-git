package de.faap.feedme.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.example.android.actionbarcompat.ActionBarActivity;
import com.viewpagerindicator.TitlePageIndicator;
import com.viewpagerindicator.TitleProvider;

import de.faap.feedme.R;
import de.faap.feedme.util.Preferences;

public class RecipesActivity extends ActionBarActivity {
    static final int NUM_ITEMS = 3;

    static Context mContext;
    static Preferences preferences;

    private mFPAdapter mFPAdapter;
    private ViewPager mViewPager;
    private TitlePageIndicator mTPIndicator;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.recipes);

	mContext = getApplicationContext();
	preferences = new Preferences(this);

	mFPAdapter = new mFPAdapter(getSupportFragmentManager());
	mViewPager = (ViewPager) findViewById(R.id.recipes_viewpager);
	mViewPager.setAdapter(mFPAdapter);
	mTPIndicator = (TitlePageIndicator) findViewById(R.id.recipes_indicator);
	mTPIndicator.setViewPager(mViewPager, 1);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	case android.R.id.home:
	    finish();
	    break;
	}
	return super.onOptionsItemSelected(item);
    }

    private class mFPAdapter extends FragmentPagerAdapter implements
	    TitleProvider {

	public mFPAdapter(FragmentManager fm) {
	    super(fm);
	}

	@Override
	public Fragment getItem(int position) {
	    if (0 <= position && position <= 2) {
		return CategoryFragment.newInstance(position);
	    }
	    return null;
	}

	@Override
	public int getCount() {
	    return NUM_ITEMS;
	}

	@Override
	public String getTitle(int position) {
	    if (position == 0) {
		return mContext.getResources().getString(R.string.ind_effort);
	    } else if (position == 1) {
		return mContext.getResources().getString(R.string.ind_type);
	    } else if (position == 2) {
		return mContext.getResources().getString(R.string.ind_cuisine);
	    } else {
		return null;
	    }
	}
    }

    /**
     * Creates fragments according to the category and loads the according
     * recipe-lists
     * 
     * @author joe
     * 
     */
    private static class CategoryFragment extends Fragment {

	private int pos;

	static CategoryFragment newInstance(int position) {
	    CategoryFragment mCF = new CategoryFragment();

	    Bundle args = new Bundle();
	    args.putInt("num", position);
	    mCF.setArguments(args);

	    return mCF;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    pos = getArguments().getInt("num", -1);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
		Bundle savedInstanceState) {
	    ExpandableListView v = (ExpandableListView) inflater.inflate(
		    R.layout.recipe_categories, container, false);

	    // TODO query category/recipes
	    // dont save them in preferences
	    String[] mock = { "0" };
	    preferences.saveEffort(mock);
	    mock[0] = "1";
	    preferences.saveType(mock);
	    mock[0] = "2";
	    preferences.saveCuisine(mock);

	    String[] category = { "foobar" };
	    String[][] recipes = new String[1][1];
	    // load the right list
	    if (pos == 0) {
		recipes[0] = preferences.getEffort();
	    } else if (pos == 1) {
		recipes[0] = preferences.getType();
	    } else {
		recipes[0] = preferences.getCuisine();
	    }

	    v.setAdapter(new CategoryListAdapter(category, recipes));

	    return v;
	}

	private class CategoryListAdapter extends BaseExpandableListAdapter {

	    private String[] categories;
	    private String[][] recipes;

	    public CategoryListAdapter(String[] categories, String[][] recipes) {
		this.categories = categories;
		this.recipes = recipes;
	    }

	    @Override
	    public Object getChild(int groupPosition, int childPosition) {
		return recipes[groupPosition][childPosition];
	    }

	    @Override
	    public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	    }

	    @Override
	    public View getChildView(int groupPosition, int childPosition,
		    boolean isLastChild, View convertView, ViewGroup parent) {
		TextView textView = getGenericView();
		textView.setText(recipes[groupPosition][childPosition]);
		textView.setOnClickListener(new OnClickListener() {
		    @Override
		    public void onClick(View v) {
			Intent mIntent = new Intent(mContext,
				PreperationActivity.class);
			mIntent.putExtra(DashboardActivity.ACTIONBAR_TITLE,
				((TextView) v).getText());
			mIntent.putExtra(DashboardActivity.ACTIONBAR_ICON,
				DashboardActivity.iconRecipes);
			startActivity(mIntent);
		    }
		});
		return textView;
	    }

	    @Override
	    public int getChildrenCount(int groupPosition) {
		return recipes[groupPosition].length;
	    }

	    @Override
	    public Object getGroup(int groupPosition) {
		return categories[groupPosition];
	    }

	    @Override
	    public int getGroupCount() {
		return categories.length;
	    }

	    @Override
	    public long getGroupId(int groupPosition) {
		return groupPosition;
	    }

	    @Override
	    public View getGroupView(int groupPosition, boolean isExpanded,
		    View convertView, ViewGroup parent) {
		TextView textView = getGenericView();
		textView.setText(categories[groupPosition]);
		return textView;
	    }

	    @Override
	    public boolean hasStableIds() {
		return true;
	    }

	    @Override
	    public boolean isChildSelectable(int groupPosition,
		    int childPosition) {
		return true;
	    }

	    /**
	     * Returns a new TextView which can be used as a Child- or
	     * GroudView.
	     */
	    private TextView getGenericView() {
		AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT, 60);

		TextView textView = new TextView(mContext);
		textView.setLayoutParams(lp);
		textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
		textView.setPadding(60, 0, 0, 0);
		return textView;
	    }

	}
    }
}
