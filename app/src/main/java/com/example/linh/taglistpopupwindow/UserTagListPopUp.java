package com.example.linh.taglistpopupwindow;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.moonmonkeylabs.realmrecyclerview.RealmRecyclerView;
import io.realm.RealmResults;

import static com.example.linh.taglistpopupwindow.RealmTagListAdapter.ItemClickListener;

/**
 * Created by linh on 20/06/2017.
 */

public class UserTagListPopUp extends PopupWindow {

    @BindView(R.id.main_task_list)
    RealmRecyclerView rv;
    RealmTagListAdapter mAdapter;

    private Unbinder mUnbinder;
    private Point location;

    public static UserTagListPopUp newInstance(Context context, RealmResults<FollowItemModel> items, ItemClickListener clickListener) {
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

    private UserTagListPopUp(View contentView, int width, int height, RealmResults<FollowItemModel> items, ItemClickListener clickListener) {
        super(contentView, width, height);
        mUnbinder = ButterKnife.bind(this, contentView);
        location = new Point();
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        setupRecyclerView(contentView.getContext(), items, clickListener);
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
        location.x = x;
        location.y = y;
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    public Point getLocation() {
        return location;
    }

    public void clear(){
        if (mUnbinder != null) {
            mUnbinder.unbind();
            mUnbinder = null;
        }
    }

    public void updateList(RealmResults<FollowItemModel> realmResults){
        mAdapter.updateRealmResults(realmResults);
    }

    private void setupRecyclerView(Context context, RealmResults<FollowItemModel> items, ItemClickListener clickListener){
        mAdapter = new RealmTagListAdapter(context, items, true, true, clickListener);
        rv.setAdapter(mAdapter);
    }
}