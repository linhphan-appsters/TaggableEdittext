package com.example.linh.taglistpopupwindow;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmBasedRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.RealmViewHolder;

public class RealmTagListAdapter extends RealmBasedRecyclerViewAdapter<FollowItemModel, RealmTagListAdapter.ViewHolder> {

    private ItemClickListener clickListener;

    public RealmTagListAdapter(Context context, RealmResults<FollowItemModel> realmResults, boolean automaticUpdate, boolean animateResults, ItemClickListener clickListener) {
        super(context, realmResults, automaticUpdate, animateResults);
        this.clickListener = clickListener;
    }

    @Override
    public ViewHolder onCreateRealmViewHolder(ViewGroup viewGroup, int viewType) {
        View v = inflater.inflate(R.layout.task_item, viewGroup, false);
        return new ViewHolder(v, clickListener);
    }

    @Override
    public void onBindRealmViewHolder(ViewHolder viewHolder, int position) {
        final FollowItemModel task = realmResults.get(position);
        viewHolder.taskName.setText(task.getDisplayName());
    }

    class ViewHolder extends RealmViewHolder implements View.OnClickListener {

        @BindView(R.id.task_item_task_name) TextView taskName;
        private ItemClickListener clickListener;

        public ViewHolder(View view, ItemClickListener clickListener) {
            super(view);
            this.clickListener = clickListener;
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(clickListener != null) {
                clickListener.onClick(v, realmResults.get(getAdapterPosition()));
            }
        }
    }

    public interface ItemClickListener {
        void onClick(View caller, FollowItemModel task);
    }
}
