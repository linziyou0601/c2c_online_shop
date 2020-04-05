package com.example.c2c_online_shop.ui.search;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.c2c_online_shop.HttpURLConnectionUtil;
import com.example.c2c_online_shop.MainActivity;
import com.example.c2c_online_shop.R;
import com.example.c2c_online_shop.model.Product;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.LinkedList;

import static java.lang.Math.min;

public class SearchFragment extends Fragment {

    private SearchViewModel searchViewModel;
    private NavController navController;
    private BottomNavigationView navView;
    ConstraintLayout progressLayout, mainLayout;
    ScrollView productLayout;
    RecyclerView productRecyView;
    SwipeRefreshLayout swipeRefreshLayout;
    LinkedList<Product> myProducts = new LinkedList<>();

    TextView title, description, seller, price, stockQty;
    TextInputLayout order_qty_layout;
    EditText query, order_qty;
    Button search_btn, back_btn, order_btn;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //--------------------set View--------------------//
        MainActivity.fragmentName = "SearchFragment";
        MainActivity.updateNotifications();
        searchViewModel = ViewModelProviders.of(this).get(SearchViewModel.class);
        View root = inflater.inflate(R.layout.fragment_search, container, false);
        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        navView = getActivity().findViewById(R.id.nav_view);

        //--------------------Input Object--------------------//
        progressLayout = root.findViewById(R.id.progress);
        mainLayout = root.findViewById(R.id.main);
        productLayout = root.findViewById(R.id.product);
        swipeRefreshLayout = root.findViewById(R.id.swipe_refresh_layout);
        productRecyView = root.findViewById(R.id.productRecyView);

        title = root.findViewById(R.id.title);
        description = root.findViewById(R.id.description);
        description.setMovementMethod(new ScrollingMovementMethod());
        seller = root.findViewById(R.id.seller);
        price = root.findViewById(R.id.price);
        stockQty= root.findViewById(R.id.stockQty);

        query = root.findViewById(R.id.query);
        search_btn = root.findViewById(R.id.search_btn);
        order_qty_layout = root.findViewById(R.id.order_qty_layout);
        order_qty = root.findViewById(R.id.order_qty);
        back_btn = root.findViewById(R.id.back_btn);
        order_btn = root.findViewById(R.id.order_btn);

        //--------------------Listener Button--------------------//
        search_btn.setOnClickListener(arg0 -> search());
        back_btn.setOnClickListener(arg0 -> back());
        order_btn.setOnClickListener(arg0 -> order());
        //設置監聽器
        swipeRefreshLayout.setOnRefreshListener(() -> {
            inProgress();
            new GetProductListTask().execute("https://linziyou.nctu.me:7777/api/c2c_shop/select/products?query="+searchViewModel.getQuery().getValue());
            swipeRefreshLayout.setRefreshing(false);
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        //--------------------Listener Observe--------------------//
        searchViewModel.getQuery().observe(this, s -> {
            if(!query.getEditableText().toString().equals(s))
                query.setText(s);
        });
        query.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchViewModel.setQuery(s.toString());
            }
        });
        searchViewModel.getOrderQty().observe(this, s -> {
            if(!order_qty.getEditableText().toString().equals(s))
                order_qty.setText(s);
        });
        order_qty.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchViewModel.setOrderQty(s.toString());
            }
        });

        //--------------------IF HASN'T LOGIN--------------------//
        if(MainActivity.user == null)
            navController.navigate(R.id.navigation_login);

        //--------------------Main Function--------------------//
        MainActivity.temp_pd = null;
        outProgress();

        inProgress();
        new GetProductListTask().execute("https://linziyou.nctu.me:7777/api/c2c_shop/select/products?query="+searchViewModel.getQuery().getValue());
        return root;
    }

    //--------------------Process--------------------//
    private void inProgress(){
        progressLayout.setVisibility(View.VISIBLE);
        mainLayout.setVisibility(View.GONE);
        productLayout.setVisibility(View.GONE);
    }
    private void outProgress(){
        progressLayout.setVisibility(View.GONE);
        mainLayout.setVisibility(View.VISIBLE);
        productLayout.setVisibility(View.GONE);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        navView.setVisibility(View.VISIBLE);
    }
    private void showProduct(){
        progressLayout.setVisibility(View.GONE);
        mainLayout.setVisibility(View.GONE);
        productLayout.setVisibility(View.VISIBLE);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        navView.setVisibility(View.GONE);
    }
    private void search(){
        inProgress();
        new GetProductListTask().execute("https://linziyou.nctu.me:7777/api/c2c_shop/select/products?query="+searchViewModel.getQuery().getValue());
    }
    private void back(){
        MainActivity.temp_pd = null;
        outProgress();
    }
    private void order(){
        MainActivity.temp_od = MainActivity.temp_pd.createOrder(Integer.parseInt(searchViewModel.getOrderQty().getValue()));
        View order_detail = getLayoutInflater().inflate(R.layout.dialog_order_detail, null);
        LinearLayout od_detail_view = order_detail.findViewById(R.id.od_detail_view);
        TextView od_detail_title = od_detail_view.findViewById(R.id.od_title);
        TextView od_detail_price = od_detail_view.findViewById(R.id.od_price);
        TextView od_detail_quantity = od_detail_view.findViewById(R.id.od_quantity);
        TextView od_detail_amount = od_detail_view.findViewById(R.id.od_amount);
        od_detail_title.setText(MainActivity.temp_od.getProduct().getTitle());
        od_detail_price.setText(Integer.toString(MainActivity.temp_od.getProduct().getPrice()));
        od_detail_quantity.setText(Integer.toString(MainActivity.temp_od.getQuantity()));
        od_detail_amount.setText(Integer.toString(MainActivity.temp_od.getAmount()));

        MainActivity.temp_context = getContext();
        new MaterialAlertDialogBuilder(getContext(), R.style.MyThemeOverlayAlertDialog)
                .setTitle("Order Confirmation")
                .setView(order_detail)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Confirm", (arg0, arg1) -> {
                    MainActivity.temp_od.confirmOrder();
                    back();
                })
                .show();
    }

    //--------------------Function--------------------//
    private class GetProductListTask extends AsyncTask<String, Void, String> {
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
            myProducts = gson.fromJson(json, new TypeToken<LinkedList<Product>>(){}.getType());

            //--------------------設定ViewPager--------------------//

            GridLayoutManager gridLayoutManager=new GridLayoutManager(getContext(), 2);
            productRecyView.setLayoutManager(gridLayoutManager);
            productRecyView.setAdapter(new SearchFragment.productItemAdapter(getContext(),
                    new LinkedList<>(myProducts.subList(0, min(myProducts.size(),10))),
                    myProducts.size() > 10 ? true : false));
            productRecyView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    //當滾動事件停止時觸發一次
                    if(newState == RecyclerView.SCROLL_STATE_IDLE){
                        int lastVisibleItem = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                        SearchFragment.productItemAdapter adapter = (SearchFragment.productItemAdapter)recyclerView.getAdapter();
                        if (lastVisibleItem == adapter.getRealLastPosition())
                            adapter.updateRecyclerView(adapter.getRealLastPosition());
                    }
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                }
            });

            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                private final int NORMAL_ITEM = 0;
                private final int FOOT_ITEM = 1;
                @Override
                public int getSpanSize(int position) {
                    switch(productRecyView.getAdapter().getItemViewType(position)){
                        case NORMAL_ITEM:
                            return 1;

                        case  FOOT_ITEM:
                            return 2;

                        default:
                            return 1;
                    }
                }
            });
            outProgress();
        }
    }

    //--------------------Recycler View Adapter--------------------//
    private class productItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private Context context;
        private LinkedList<Product> products;
        private boolean hasMore;    //是否有資料增加

        private int pdPage;
        private final int NORMAL_ITEM = 0;
        private final int FOOT_ITEM = 1;

        productItemAdapter(Context context, LinkedList<Product> products, boolean hasMore){
            this.context = context;
            this.products = products;
            this.hasMore = hasMore;
            pdPage = 1;
        }

        //調用R.layout建立View，以此建立一個新的ViewHolder
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            RecyclerView.ViewHolder holder;
            if (viewType == NORMAL_ITEM) {
                view = LayoutInflater.from(context).inflate(R.layout.recycler_product_2col_item, parent, false);
                holder = new SearchFragment.productItemAdapter.NormalViewHolder(view);
            } else {
                view = LayoutInflater.from(context).inflate(R.layout.recycler_has_more, parent, false);
                holder = new SearchFragment.productItemAdapter.FootViewHolder(view);
            }
            return holder;
        }

        //透過position找到data，以此設置ViewHolder裡的View
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof SearchFragment.productItemAdapter.NormalViewHolder) {
                Product product= products.get(position);
                ((NormalViewHolder) holder).pd_title.setText(product.getTitle());
                ((NormalViewHolder) holder).pd_price.setText(Integer.toString(product.getPrice()));
                ((NormalViewHolder) holder).pd_stockQty.setText(Integer.toString(product.getStockQty()));
                ((NormalViewHolder) holder).pd_card.setClickable(true);
                ((NormalViewHolder) holder).pd_card.setOnClickListener(new ProductOnClick(product));
            } else {
                ((SearchFragment.productItemAdapter.FootViewHolder) holder).tips.setText((hasMore)? "Loading...": "Last one");
            }
        }

        //取得數量
        @Override
        public int getItemCount() {
            return products.size() + 1;
        }

        //取得末列位置（不含載入更多）
        public int getRealLastPosition() {
            return products.size();
        }

        //取得類型
        @Override
        public int getItemViewType(int position) {
            return (position == getItemCount()-1)? FOOT_ITEM: NORMAL_ITEM;
        }

        //一般資料ViewHolder
        class NormalViewHolder extends RecyclerView.ViewHolder{
            MaterialCardView pd_card;
            TextView pd_title, pd_price, pd_stockQty;
            NormalViewHolder(View itemView) {
                super(itemView);
                pd_card = itemView.findViewById(R.id.pd_card);
                pd_title = itemView.findViewById(R.id.pd_title);
                pd_price = itemView.findViewById(R.id.pd_price);
                pd_stockQty = itemView.findViewById(R.id.pd_stockQty);
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
        public void updateList(LinkedList<Product> products, boolean hasMore) {
            if(hasMore) this.products.addAll(products);
            this.hasMore = hasMore;
            notifyDataSetChanged();
        }

        //----------分頁資料----------//
        //更新子頁面RecyclerView
        private void updateRecyclerView(int startIndex) {
            new Thread() {
                public void run() {
                    LinkedList<Product> newProducts = (myProducts.size()<=startIndex)?
                            new LinkedList<>():
                            new LinkedList<>(myProducts.subList(startIndex, min(myProducts.size(),startIndex+10)));
                    getActivity().runOnUiThread(() -> updateList(newProducts, newProducts.size()>0));
                }
            }.start();
        }

        //----------點擊監聽----------//
        //轉跳產品編輯頁
        public class ProductOnClick implements View.OnClickListener {
            Product product;
            ProductOnClick(Product product) { this.product = product; }

            @Override
            public void onClick(View v){
                MainActivity.temp_pd = product;
                inProgress();
                new GetProductTask().execute("https://linziyou.nctu.me:7777/api/c2c_shop/select/product?id="+product.getId());
            }

            private class GetProductTask extends AsyncTask<String, Void, String> {
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
                    MainActivity.temp_pd = gson.fromJson(json, Product.class);

                    if(MainActivity.temp_pd.getSeller().getId() == MainActivity.user.getId()){
                        order_btn.setVisibility(View.GONE);
                        order_qty_layout.setVisibility(View.GONE);
                    }
                    else{
                        order_btn.setVisibility(View.VISIBLE);
                        order_qty_layout.setVisibility(View.VISIBLE);
                    }
                    showProduct();
                    title.setText(MainActivity.temp_pd.getTitle());
                    description.setText(MainActivity.temp_pd.getDescription());
                    seller.setText(MainActivity.temp_pd.getSeller().getProfile().getName());
                    stockQty.setText(Integer.toString(MainActivity.temp_pd.getStockQty()));
                    price.setText(Integer.toString(MainActivity.temp_pd.getPrice()));
                }
            }
        }
    }
}
