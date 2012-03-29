package com.prasanna.android.stacknetwork;

import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.prasanna.android.stacknetwork.utils.IntentUtils;
import com.prasanna.android.stacknetwork.utils.OperatingSite;

public abstract class AbstractUserActionBarActivity extends AbstractActivityWithBroadcastReceiver
{
    public abstract void refresh();

    public abstract Context getCurrentAppContext();

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getActionBar().setTitle(OperatingSite.getSite().getName());
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.action_menu, menu);

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
            Toast.makeText(this, "Tapped search", Toast.LENGTH_SHORT).show();
            break;

        case R.id.menu_profile:
            Intent userProfileIntent = IntentUtils.createUserProfileIntent(getCurrentAppContext(), 631937l);
            startActivity(userProfileIntent);
            break;
        }
        return super.onOptionsItemSelected(item);
    }
}
