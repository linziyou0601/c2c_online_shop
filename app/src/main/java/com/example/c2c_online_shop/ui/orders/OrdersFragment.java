package com.example.c2c_online_shop.ui.orders;

import android.content.Context;
import android.os.AsyncTask;
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

import com.example.c2c_online_shop.Blockchain.Blockchain;
import com.example.c2c_online_shop.HttpURLConnectionUtil;
import com.example.c2c_online_shop.MainActivity;
import com.example.c2c_online_shop.R;
import com.example.c2c_online_shop.model.Order;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.LinkedList;

import static java.lang.Math.min;


public class OrdersFragment extends Fragment {

    private OrdersViewModel ordersViewModel;
    private NavController navController;
    private BottomNavigationView navView;
    ConstraintLayout progressLayout, mainLayout;
    RecyclerView orderRecyView;
    SwipeRefreshLayout swipeRefreshLayout;
    LinkedList<Order> myOrders = new LinkedList<>();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //--------------------set View--------------------//
        MainActivity.fragmentName = "OrdersFragment";
        MainActivity.updateNotifications();
        ordersViewModel = ViewModelProviders.of(this).get(OrdersViewModel.class);
        View root = inflater.inflate(R.layout.fragment_orders, container, false);
        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        navView = getActivity().findViewById(R.id.nav_view);

        //--------------------Input Object--------------------//
        progressLayout = root.findViewById(R.id.progress);
        mainLayout = root.findViewById(R.id.main);
        swipeRefreshLayout = root.findViewById(R.id.swipe_refresh_layout);
        orderRecyView = root.findViewById(R.id.orderRecyView);

        //--------------------Listener Button--------------------//
        //設置監聽器
        swipeRefreshLayout.setOnRefreshListener(() -> {
            inProgress();
            MainActivity.updateNotifications();
            new GetOrderListTask().execute("https://linziyou.nctu.me:7777/api/c2c_shop/select/orders?id="+MainActivity.user.getId());
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
        MainActivity.temp_od = null;
        outProgress();

        inProgress();
        new GetOrderListTask().execute("https://linziyou.nctu.me:7777/api/c2c_shop/select/orders?id="+MainActivity.user.getId());
        return root;
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

    //--------------------Function--------------------//
    private class GetOrderListTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return HttpURLConnectionUtil.getDataHttpUriConnection(params[0]);
        }
        @Override
        protected void onPostExecute(String result) {
            processGetProductList(result);
        }

        public void processGetProductList(String json){
            Gson gson = new Gson();
            myOrders = gson.fromJson(json, new TypeToken<LinkedList<Order>>(){}.getType());

            //--------------------設定ViewPager--------------------//
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            orderRecyView.setLayoutManager(layoutManager);
            orderRecyView.setAdapter(new OrdersFragment.orderItemAdapter(getContext(),
                    new LinkedList<>(myOrders.subList(0, min(myOrders.size(),10))),
                    myOrders.size() > 10 ? true : false));
            orderRecyView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    //當滾動事件停止時觸發一次
                    if(newState == RecyclerView.SCROLL_STATE_IDLE){
                        int lastVisibleItem = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                        OrdersFragment.orderItemAdapter adapter = (OrdersFragment.orderItemAdapter)recyclerView.getAdapter();
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
    }

    //--------------------Recycler View Adapter--------------------//
    private class orderItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private Context context;
        private LinkedList<Order> orders;
        private boolean hasMore;    //是否有資料增加

        private int odPage;
        private int NORMAL_ITEM = 0;
        private int FOOT_ITEM = 1;

        orderItemAdapter(Context context, LinkedList<Order> orders, boolean hasMore){
            this.context = context;
            this.orders = orders;
            this.hasMore = hasMore;
            odPage = 1;
        }

        //調用R.layout建立View，以此建立一個新的ViewHolder
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            RecyclerView.ViewHolder holder;
            if (viewType == NORMAL_ITEM) {
                view = LayoutInflater.from(context).inflate(R.layout.recycler_order_item, parent, false);
                holder = new OrdersFragment.orderItemAdapter.NormalViewHolder(view);
            } else {
                view = LayoutInflater.from(context).inflate(R.layout.recycler_has_more, parent, false);
                holder = new OrdersFragment.orderItemAdapter.FootViewHolder(view);
            }
            return holder;
        }

        //透過position找到data，以此設置ViewHolder裡的View
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof OrdersFragment.orderItemAdapter.NormalViewHolder) {
                Order order= orders.get(position);
                ((NormalViewHolder) holder).od_title.setText(order.getProduct().getTitle());
                ((NormalViewHolder) holder).od_seller.setText(order.getProduct().getSeller().getProfile().getName());
                ((NormalViewHolder) holder).od_quantity.setText(Integer.toString(order.getQuantity()));
                ((NormalViewHolder) holder).od_price.setText(Integer.toString(order.getAmount()));
                switch(order.getStatus()){
                    case 0:
                        ((NormalViewHolder) holder).od_status.setText("Ordered");
                        break;
                    case 1:
                        ((NormalViewHolder) holder).od_status.setText("Paid");
                        break;
                    case 2:
                        ((NormalViewHolder) holder).od_status.setText("Shipping");
                        break;
                    case 3:
                        ((NormalViewHolder) holder).od_status.setText("Completed");
                        break;
                }
                ((NormalViewHolder) holder).od_card.setClickable(true);
                ((NormalViewHolder) holder).od_card.setOnClickListener(new OrderOnClick(order));
            } else {
                ((OrdersFragment.orderItemAdapter.FootViewHolder) holder).tips.setText((hasMore)? "Loading...": "Last one");
            }
        }

        //取得數量
        @Override
        public int getItemCount() {
            return orders.size() + 1;
        }

        //取得末列位置（不含載入更多）
        public int getRealLastPosition() {
            return orders.size();
        }

        //取得類型
        @Override
        public int getItemViewType(int position) {
            return (position == getItemCount()-1)? FOOT_ITEM: NORMAL_ITEM;
        }

        //一般資料ViewHolder
        class NormalViewHolder extends RecyclerView.ViewHolder{
            MaterialCardView od_card;
            TextView od_title, od_seller, od_quantity, od_price, od_status;
            NormalViewHolder(View itemView) {
                super(itemView);
                od_card = itemView.findViewById(R.id.od_card);
                od_title = itemView.findViewById(R.id.od_title);
                od_seller = itemView.findViewById(R.id.od_seller);
                od_quantity = itemView.findViewById(R.id.od_quantity);
                od_price = itemView.findViewById(R.id.od_price);
                od_status = itemView.findViewById(R.id.od_status);
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
        public void updateList(LinkedList<Order> orders, boolean hasMore) {
            if(hasMore) this.orders.addAll(orders);
            this.hasMore = hasMore;
            notifyDataSetChanged();
        }

        //----------分頁資料----------//
        //更新子頁面RecyclerView
        private void updateRecyclerView(int startIndex) {
            new Thread() {
                public void run() {
                    LinkedList<Order> newOrders = (myOrders.size()<=startIndex)?
                            new LinkedList<>():
                            new LinkedList<>(myOrders.subList(startIndex, min(myOrders.size(),startIndex+10)));
                    getActivity().runOnUiThread(() -> updateList(newOrders, newOrders.size()>0));
                }
            }.start();
        }

        //----------點擊監聽----------//
        //轉跳產品編輯頁
        public class OrderOnClick implements View.OnClickListener {
            Order order;
            OrderOnClick(Order order) { this.order = order; }

            @Override
            public void onClick(View v){
                MainActivity.temp_od = order;
                Blockchain.updateBlockchain();
                new GetOrderTask().execute("https://linziyou.nctu.me:7777/api/c2c_shop/select/order?id="+order.getId());
            }

            private class GetOrderTask extends AsyncTask<String, Void, String> {
                @Override
                protected String doInBackground(String... params) {
                    return HttpURLConnectionUtil.getDataHttpUriConnection(params[0]);
                }
                @Override
                protected void onPostExecute(String result) {
                    processGetOrder(result);
                }

                public void processGetOrder(String json){
                    Gson gson = new Gson();
                    MainActivity.temp_od = gson.fromJson(json, Order.class);
                    navController.navigate(R.id.navigation_order);
                }
            }
        }
    }
}
