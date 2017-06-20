package com.example.linh.taglistpopupwindow;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.moonmonkeylabs.realmrecyclerview.RealmRecyclerView;
import io.realm.RealmResults;

/**
 * Created by linh on 20/06/2017.
 */

public class UserTagListPopUp extends PopupWindow implements RealmTasksAdapter.ItemClickListener {

    @BindView(R.id.main_task_list)
    RealmRecyclerView rv;

    RealmResults<FollowItemModel> items;
    private Unbinder mUnbinder;

    public static UserTagListPopUp newInstance(Context context, RealmResults<FollowItemModel> items, View.OnClickListener clickListener) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.popup_user_tag_list, null);
        UserTagListPopUp popupWindow = new UserTagListPopUp(
                customView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, items, clickListener
        );
        if(Build.VERSION.SDK_INT>=21){
            popupWindow.setElevation(5.0f);
        }
        return popupWindow;
    }

    public UserTagListPopUp(View contentView, int width, int height, RealmResults<FollowItemModel> items, View.OnClickListener clickListener) {
        super(contentView, width, height);
        setHeight(250);
        mUnbinder = ButterKnife.bind(contentView);
        this.items = items;
        setupRecyclerView(contentView.getContext());
        contentView.setOnClickListener(clickListener);
    }

    @Override
    public void dismiss() {
        mUnbinder.unbind();
        super.dismiss();
    }

    private void setupRecyclerView(Context context){
        RealmTasksAdapter tasksAdapter = new RealmTasksAdapter(context, items, true, true, this);
        rv.setAdapter(tasksAdapter);
    }

    @Override
    public void onClick(View caller, FollowItemModel task) {

    }
}