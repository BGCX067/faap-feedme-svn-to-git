package de.faap.feedme.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;

import com.example.android.actionbarcompat.ActionBarActivity;

import de.faap.feedme.R;
import de.faap.feedme.io.DatabaseUpdater;
import de.faap.feedme.io.IUpdateDatabase;

public class DashboardActivity extends ActionBarActivity {

    public static final String ACTIONBAR_TITLE = "title";
    public static final String ACTIONBAR_ICON = "icon";
    public static final int iconPlan = R.drawable.ic_action_planer;
    public static final int iconRecipes = R.drawable.ic_action_recipes;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.dashboard);

	findViewById(R.id.dashboard_btn_plan).setOnClickListener(
		new OnClickListener() {
		    @Override
		    public void onClick(View v) {
			Intent mIntent = new Intent(getApplicationContext(),
				PlanActivity.class);
			mIntent.putExtra(ACTIONBAR_TITLE, getResources()
				.getString(R.string.title_plan));
			mIntent.putExtra(ACTIONBAR_ICON, iconPlan);
			startActivity(mIntent);
		    }
		});

	findViewById(R.id.dashboard_btn_recipes).setOnClickListener(
		new OnClickListener() {
		    @Override
		    public void onClick(View v) {
			Intent mIntent = new Intent(getApplicationContext(),
				RecipesActivity.class);
			mIntent.putExtra(ACTIONBAR_TITLE, getResources()
				.getString(R.string.title_recipes));
			mIntent.putExtra(ACTIONBAR_ICON, iconRecipes);
			startActivity(mIntent);
		    }
		});

	findViewById(R.id.dashboard_btn_update).setOnClickListener(
		new OnClickListener() {
		    @Override
		    public void onClick(View v) {
			// TODO update database
			IUpdateDatabase updater = new DatabaseUpdater();
			getActionBarHelper().setProgressBarState(true);
			updater.update();
			getActionBarHelper().setProgressBarState(false);
		    }
		});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	MenuInflater menuInflater = getMenuInflater();
	menuInflater.inflate(R.menu.dashboard, menu);
	// Calling super after populating the menu is necessary here to ensure
	// that the action bar helpers have a chance to handle this event.
	return super.onCreateOptionsMenu(menu);
    }
}