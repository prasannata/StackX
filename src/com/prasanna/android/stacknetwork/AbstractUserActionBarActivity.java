package com.prasanna.android.stacknetwork;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import com.prasanna.android.stacknetwork.utils.IntentUtils;
import com.prasanna.android.stacknetwork.utils.OperatingSite;

public abstract class AbstractUserActionBarActivity extends Activity implements SearchView.OnQueryTextListener
{
    private SearchView searchView;

    public abstract void refresh();

    public abstract Context getCurrentAppContext();

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getActionBar().setTitle(OperatingSite.getSite().name);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.action_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("Search in title");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        case android.R.id.home:
            Toast.makeText(this, "Tapped home", Toast.LENGTH_SHORT).show();
            break;

        case R.id.menu_refresh:
            refresh();
            break;

        case R.id.menu_search:
            break;

        case R.id.menu_profile:
            Intent userProfileIntent = IntentUtils.createUserProfileIntent(getCurrentAppContext(), 631937l);
            startActivity(userProfileIntent);
            break;
        }
        return super.onOptionsItemSelected(item);
    }
}
