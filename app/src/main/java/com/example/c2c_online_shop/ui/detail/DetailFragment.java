package com.example.c2c_online_shop.ui.detail;

import android.content.Context;
import android.graphics.Color;
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

import com.example.c2c_online_shop.Blockchain.Deposit;
import com.example.c2c_online_shop.Blockchain.Payment;
import com.example.c2c_online_shop.Blockchain.Transaction;
import com.example.c2c_online_shop.Blockchain.Withdraw;
import com.example.c2c_online_shop.MainActivity;
import com.example.c2c_online_shop.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedList;

import static java.lang.Math.min;


public class DetailFragment extends Fragment {

    private DetailViewModel detailViewModel;
    private NavController navController;
    private BottomNavigationView navView;
    ConstraintLayout progressLayout, mainLayout;
    RecyclerView detailRecyView;
    SwipeRefreshLayout swipeRefreshLayout;
    LinkedList<Transaction> myTransactions = new LinkedList<>();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //--------------------set View--------------------//
        MainActivity.fragmentName = "DetailFragment";
        detailViewModel = ViewModelProviders.of(this).get(DetailViewModel.class);
        View root = inflater.inflate(R.layout.fragment_detail, container, false);
        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        navView = getActivity().findViewById(R.id.nav_view);

        //--------------------Input Object--------------------//
        progressLayout = root.findViewById(R.id.progress);
        mainLayout = root.findViewById(R.id.main);
        swipeRefreshLayout = root.findViewById(R.id.swipe_refresh_layout);
        detailRecyView = root.findViewById(R.id.detailRecyView);

        //--------------------Listener Button--------------------//
        //設置監聽器
        swipeRefreshLayout.setOnRefreshListener(() -> {
            inProgress();
            getTransactionDetail();
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
        getTransactionDetail();

        return root;
    }

    //--------------------Function--------------------//
    //get transaction detail
    private void getTransactionDetail(){
        myTransactions = MainActivity.user.getWallet().getDetail();
        Collections.reverse(myTransactions);

        //--------------------設定ViewPager--------------------//
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        detailRecyView.setLayoutManager(layoutManager);
        detailRecyView.setAdapter(new transactionItemAdapter(getContext(),
                new LinkedList<>(myTransactions.subList(0, min(myTransactions.size(),10))),
                myTransactions.size() > 10 ? true : false));
        detailRecyView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //當滾動事件停止時觸發一次
                if(newState == RecyclerView.SCROLL_STATE_IDLE){
                    int lastVisibleItem = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                    transactionItemAdapter adapter = (transactionItemAdapter)recyclerView.getAdapter();
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navView.setVisibility(View.GONE);
    }

    //--------------------Recycler View Adapter--------------------//
    private class transactionItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private Context context;
        private LinkedList<Transaction> transactions;
        private boolean hasMore;    //是否有資料增加

        private int txPage;
        private int NORMAL_ITEM = 0;
        private int FOOT_ITEM = 1;

        transactionItemAdapter(Context context, LinkedList<Transaction> transactions, boolean hasMore){
            this.context = context;
            this.transactions = transactions;
            this.hasMore = hasMore;
            txPage = 1;
        }

        //調用R.layout建立View，以此建立一個新的ViewHolder
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            RecyclerView.ViewHolder holder;
            if (viewType == NORMAL_ITEM) {
                view = LayoutInflater.from(context).inflate(R.layout.recycler_transaction_item, parent, false);
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
                Transaction transaction = transactions.get(position);

                DateTimeFormatter ftf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                String tx_time_str = ftf.format(LocalDateTime.ofInstant(Instant.ofEpochSecond(transaction.getTimestamp()),ZoneId.of("Asia/Taipei")));
                ((NormalViewHolder) holder).tx_time.setText(tx_time_str);

                ((NormalViewHolder) holder).tx_detail.setText(transaction.getDetail());

                String tx_amouont_str = new String();
                if(transaction.getClassType().equals("Payment") && ((Payment)transaction).getPayerId() == MainActivity.user.getId())
                    tx_amouont_str = "-" + Integer.toString(transaction.getAmount());
                if(transaction.getClassType().equals("Payment") && ((Payment)transaction).getReceiverId() == MainActivity.user.getId())
                    tx_amouont_str = "+" + Integer.toString(transaction.getAmount());
                if(transaction.getClassType().equals("Withdraw") && ((Withdraw)transaction).getPayerId() == MainActivity.user.getId())
                    tx_amouont_str = "-" + Integer.toString(transaction.getAmount());
                if(transaction.getClassType().equals("Deposit") && ((Deposit)transaction).getReceiverId() == MainActivity.user.getId())
                    tx_amouont_str = "+" + Integer.toString(transaction.getAmount());
                if(tx_amouont_str.substring(0,1).equals("-")) ((NormalViewHolder) holder).tx_amount.setTextColor(Color.parseColor("#B00020"));
                else ((NormalViewHolder) holder).tx_amount.setTextColor(Color.parseColor("#009688"));

                ((NormalViewHolder) holder).tx_amount.setText(tx_amouont_str);
            } else {
                ((FootViewHolder) holder).tips.setText((hasMore)? "Loading...": "Last one");
            }
        }

        //取得數量
        @Override
        public int getItemCount() {
            return transactions.size() + 1;
        }

        //取得末列位置（不含載入更多）
        public int getRealLastPosition() {
            return transactions.size();
        }

        //取得類型
        @Override
        public int getItemViewType(int position) {
            return (position == getItemCount()-1)? FOOT_ITEM: NORMAL_ITEM;
        }

        //一般資料ViewHolder
        class NormalViewHolder extends RecyclerView.ViewHolder{
            MaterialCardView tx_card;
            TextView tx_time, tx_detail, tx_amount;
            NormalViewHolder(View itemView) {
                super(itemView);
                tx_card = itemView.findViewById(R.id.tx_card);
                tx_time = itemView.findViewById(R.id.tx_time);
                tx_detail = itemView.findViewById(R.id.tx_detail);
                tx_amount = itemView.findViewById(R.id.tx_amount);
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
        public void updateList(LinkedList<Transaction> transactions, boolean hasMore) {
            if(hasMore) this.transactions.addAll(transactions);
            this.hasMore = hasMore;
            notifyDataSetChanged();
        }

        //----------分頁資料----------//
        //更新子頁面RecyclerView
        private void updateRecyclerView(int startIndex) {
            new Thread() {
                public void run() {
                    LinkedList<Transaction> newTransactions = (myTransactions.size()<=startIndex)?
                                                              new LinkedList<>():
                                                              new LinkedList<>(myTransactions.subList(startIndex, min(myTransactions.size(),startIndex+10)));
                    getActivity().runOnUiThread(() -> updateList(newTransactions, newTransactions.size()>0));
                }
            }.start();
        }
    }
}
