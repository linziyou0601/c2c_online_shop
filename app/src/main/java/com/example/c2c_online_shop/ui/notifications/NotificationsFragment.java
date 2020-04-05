package com.example.c2c_online_shop.ui.notifications;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.c2c_online_shop.MainActivity;
import com.example.c2c_online_shop.R;
import com.example.c2c_online_shop.model.Notification;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;

import java.util.LinkedList;

import static java.lang.Math.min;

public class NotificationsFragment extends Fragment {

    private NotificationsViewModel notificationsViewModel;
    private NavController navController;
    ConstraintLayout progressLayout, mainLayout;
    RecyclerView notificationRecyView;
    SwipeRefreshLayout swipeRefreshLayout;
    LinkedList<Notification> myNotifications = new LinkedList<>();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //--------------------set View--------------------//
        MainActivity.fragmentName = "NotificationFragment";
        MainActivity.updateNotifications();
        notificationsViewModel = ViewModelProviders.of(this).get(NotificationsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);
        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);

        //--------------------Input Object--------------------//
        progressLayout = root.findViewById(R.id.progress);
        mainLayout = root.findViewById(R.id.main);
        swipeRefreshLayout = root.findViewById(R.id.swipe_refresh_layout);
        notificationRecyView = root.findViewById(R.id.notificationRecyView);

        //--------------------Listener Button--------------------//
        //設置監聽器
        swipeRefreshLayout.setOnRefreshListener(() -> {
            inProgress();
            getNotifications();
            swipeRefreshLayout.setRefreshing(false);
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        //--------------------IF HASN'T LOGIN--------------------//
        if(MainActivity.user == null)
            navController.navigate(R.id.navigation_login);

        //--------------------Main Function--------------------//
        outProgress();

        inProgress();
        getNotifications();

        return root;
    }

    //--------------------Function--------------------//
    //get notifications
    private void getNotifications(){
        myNotifications = new LinkedList<>();
        for(Notification nt: MainActivity.notifications)
            if(nt.getUserId()==MainActivity.user.getId())
                myNotifications.add(nt);

        //--------------------設定ViewPager--------------------//
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        notificationRecyView.setLayoutManager(layoutManager);
        notificationRecyView.setAdapter(new notificationItemAdapter(getContext(),
                new LinkedList<>(myNotifications.subList(0, min(myNotifications.size(),10))),
                myNotifications.size() > 10 ? true : false));
        notificationRecyView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //當滾動事件停止時觸發一次
                if(newState == RecyclerView.SCROLL_STATE_IDLE){
                    int lastVisibleItem = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                    notificationItemAdapter adapter = (notificationItemAdapter)recyclerView.getAdapter();
                    if (lastVisibleItem == adapter.getRealLastPosition())
                        adapter.updateRecyclerView(adapter.getRealLastPosition());
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        outProgress();
        MainActivity.updateNotifications();
    }

    //--------------------Process--------------------//
    private void inProgress(){
        progressLayout.setVisibility(View.VISIBLE);
        mainLayout.setVisibility(View.GONE);
    }
    private void outProgress(){
        progressLayout.setVisibility(View.GONE);
        mainLayout.setVisibility(View.VISIBLE);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    //--------------------Recycler View Adapter--------------------//
    private class notificationItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private Context context;
        private LinkedList<Notification> notifications;
        private boolean hasMore;    //是否有資料增加

        private int ntPage;
        private int NORMAL_ITEM = 0;
        private int FOOT_ITEM = 1;

        notificationItemAdapter(Context context, LinkedList<Notification> notifications, boolean hasMore){
            this.context = context;
            this.notifications = notifications;
            this.hasMore = hasMore;
            ntPage = 1;
        }

        //調用R.layout建立View，以此建立一個新的ViewHolder
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            RecyclerView.ViewHolder holder;
            if (viewType == NORMAL_ITEM) {
                view = LayoutInflater.from(context).inflate(R.layout.recycler_notification_item, parent, false);
                holder = new NormalViewHolder(view);
            } else {
                view = LayoutInflater.from(context).inflate(R.layout.recycler_has_more, parent, false);
                holder = new FootViewHolder(view);
            }
            return holder;
        }

        //透過position找到data，以此設置ViewHolder裡的View
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof NormalViewHolder) {
                Notification notification = notifications.get(position);

                ((NormalViewHolder) holder).nt_title.setText(notification.getTitle());
                ((NormalViewHolder) holder).nt_description.setText(notification.getDescription());
                ((NormalViewHolder) holder).nt_readed.setText((notification.getReaded()? "Read": "Unread"));
                ((NormalViewHolder) holder).nt_card.setClickable(true);
                ((NormalViewHolder) holder).nt_card.setOnClickListener(v -> {
                    notification.setReaded(true);
                    MainActivity.updateNotifications();
                    new MaterialAlertDialogBuilder(getContext(), R.style.MyThemeOverlayAlertDialog)
                            .setTitle(notification.getTitle())
                            .setMessage(notification.getDescription())
                            .setPositiveButton("OK", null)
                            .show();
                    Gson gson = new Gson();
                });
            } else {
                ((FootViewHolder) holder).tips.setText((hasMore)? "Loading...": "Last one");
            }
        }

        //取得數量
        @Override
        public int getItemCount() {
            return notifications.size() + 1;
        }

        //取得末列位置（不含載入更多）
        public int getRealLastPosition() {
            return notifications.size();
        }

        //取得類型
        @Override
        public int getItemViewType(int position) {
            return (position == getItemCount()-1)? FOOT_ITEM: NORMAL_ITEM;
        }

        //一般資料ViewHolder
        class NormalViewHolder extends RecyclerView.ViewHolder{
            MaterialCardView nt_card;
            TextView nt_title, nt_description, nt_readed;
            NormalViewHolder(View itemView) {
                super(itemView);
                nt_card = itemView.findViewById(R.id.nt_card);
                nt_title = itemView.findViewById(R.id.nt_title);
                nt_description = itemView.findViewById(R.id.nt_description);
                nt_readed = itemView.findViewById(R.id.nt_readed);
            }
        }
        //頁尾資料ViewHolder
        class FootViewHolder extends RecyclerView.ViewHolder {
            TextView tips;
            public FootViewHolder(View itemView) {
                super(itemView);
                tips = itemView.findViewById(R.id.tips);
            }
        }

        //----------分頁資料功能函式----------//
        //更新資料內容，並設定hasMore值
        public void updateList(LinkedList<Notification> notifications, boolean hasMore) {
            if(hasMore) this.notifications.addAll(notifications);
            this.hasMore = hasMore;
            notifyDataSetChanged();
        }

        //----------分頁資料----------//
        //更新子頁面RecyclerView
        private void updateRecyclerView(int startIndex) {
            new Thread() {
                public void run() {
                    LinkedList<Notification> newTransactions = (myNotifications.size()<=startIndex)?
                            new LinkedList<>():
                            new LinkedList<>(myNotifications.subList(startIndex, min(myNotifications.size(),startIndex+10)));
                    getActivity().runOnUiThread(() -> updateList(myNotifications, myNotifications.size()>0));
                }
            }.start();
        }
    }
}
