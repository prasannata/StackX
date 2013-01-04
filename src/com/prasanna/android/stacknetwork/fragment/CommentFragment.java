package com.prasanna.android.stacknetwork.fragment;

import java.util.ArrayList;

import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.adapter.ItemListAdapter;
import com.prasanna.android.stacknetwork.adapter.ItemListAdapter.ListItemView;
import com.prasanna.android.stacknetwork.model.Comment;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.DateTimeUtils;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class CommentFragment extends ItemListFragment<Comment> implements ListItemView<Comment>
{
    private static final String TAG = CommentFragment.class.getSimpleName();
    private ArrayList<Comment> comments;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreateView");

        if (itemsContainer == null)
        {
            itemsContainer = (LinearLayout) inflater.inflate(R.layout.items_fragment_container,
                    container, false);
            itemListAdapter = new ItemListAdapter<Comment>(getActivity(), R.layout.comment,
                    new ArrayList<Comment>(), this);
        }

        return itemsContainer;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        Log.d(TAG, "onActivityCreated");

        super.onActivityCreated(savedInstanceState);

        if (comments != null && !comments.isEmpty())
            itemListAdapter.addAll(comments);
    }

    @Override
    protected String getReceiverExtraName()
    {
        return StringConstants.COMMENTS;
    }

    @Override
    protected void startIntentService()
    {
        // TODO Auto-generated method stub

    }

    @Override
    protected String getLogTag()
    {
        return TAG;
    }

    @Override
    public View getView(Comment item, View convertView, ViewGroup parent)
    {
        RelativeLayout commentLayout = (RelativeLayout) getActivity().getLayoutInflater().inflate(
                R.layout.comment, null);

        TextView textView = (TextView) commentLayout.findViewById(R.id.commentScore);
        textView.setText(AppUtils.formatNumber(item.score));

        textView = (TextView) commentLayout.findViewById(R.id.commentTitle);
        textView.setText(Html.fromHtml(item.body));

        textView = (TextView) commentLayout.findViewById(R.id.commentOwner);
        textView.setText(DateTimeUtils.getElapsedDurationSince(item.creationDate) + " by "
                + Html.fromHtml(item.owner.displayName));

        return commentLayout;
    }

    public void setComments(ArrayList<Comment> comments)
    {
        this.comments = comments;
    }
}
