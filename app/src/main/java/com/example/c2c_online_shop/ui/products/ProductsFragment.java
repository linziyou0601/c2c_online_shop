package com.example.c2c_online_shop.ui.products;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import com.example.c2c_online_shop.HttpURLConnectionUtil;
import com.example.c2c_online_shop.MainActivity;
import com.example.c2c_online_shop.R;
import com.example.c2c_online_shop.model.Product;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.LinkedList;

import static java.lang.Math.min;


public class ProductsFragment extends Fragment {

    private ProductsViewModel productsViewModel;
    private NavController navController;
    private BottomNavigationView navView;
    ConstraintLayout progressLayout, mainLayout;
    RecyclerView productRecyView;
    SwipeRefreshLayout swipeRefreshLayout;
    LinkedList<Product> myProducts = new LinkedList<>();
    Button addProduct;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //--------------------set View--------------------//
        MainActivity.fragmentName = "ProductsFragment";
        MainActivity.updateNotifications();
        productsViewModel = ViewModelProviders.of(this).get(ProductsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_products, container, false);
        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        navView = getActivity().findViewById(R.id.nav_view);

        //--------------------Input Object--------------------//
        progressLayout = root.findViewById(R.id.progress);
        mainLayout = root.findViewById(R.id.main);
        swipeRefreshLayout = root.findViewById(R.id.swipe_refresh_layout);
        productRecyView = root.findViewById(R.id.productRecyView);
        addProduct = root.findViewById(R.id.addProduct);

        //--------------------Listener Button--------------------//
        addProduct.setOnClickListener(arg0 -> createProduct());
        //設置監聽器
        swipeRefreshLayout.setOnRefreshListener(() -> {
            inProgress();
            new GetProductListTask().execute("https://linziyou.nctu.me:7777/api/c2c_shop/select/products?id="+MainActivity.user.getId());
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
        MainActivity.temp_pd = null;
        outProgress();

        inProgress();
        new GetProductListTask().execute("https://linziyou.nctu.me:7777/api/c2c_shop/select/products?id="+MainActivity.user.getId());
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
    //createProduct
    private void createProduct(){
        navController.navigate(R.id.navigation_create_product);
    }

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
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            productRecyView.setLayoutManager(layoutManager);
            productRecyView.setAdapter(new ProductsFragment.productItemAdapter(getContext(),
                    new LinkedList<>(myProducts.subList(0, min(myProducts.size(),10))),
                    myProducts.size() > 10 ? true : false));
            productRecyView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    //當滾動事件停止時觸發一次
                    if(newState == RecyclerView.SCROLL_STATE_IDLE){
                        int lastVisibleItem = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                        ProductsFragment.productItemAdapter adapter = (ProductsFragment.productItemAdapter)recyclerView.getAdapter();
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
    private class productItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private Context context;
        private LinkedList<Product> products;
        private boolean hasMore;    //是否有資料增加

        private int pdPage;
        private int NORMAL_ITEM = 0;
        private int FOOT_ITEM = 1;

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
                view = LayoutInflater.from(context).inflate(R.layout.recycler_product_item, parent, false);
                holder = new ProductsFragment.productItemAdapter.NormalViewHolder(view);
            } else {
                view = LayoutInflater.from(context).inflate(R.layout.recycler_has_more, parent, false);
                holder = new ProductsFragment.productItemAdapter.FootViewHolder(view);
            }
            return holder;
        }

        //透過position找到data，以此設置ViewHolder裡的View
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof ProductsFragment.productItemAdapter.NormalViewHolder) {
                Product product= products.get(position);
                ((NormalViewHolder) holder).pd_title.setText(product.getTitle());
                ((NormalViewHolder) holder).pd_description.setText(product.getDescription());
                ((NormalViewHolder) holder).pd_price.setText(Integer.toString(product.getPrice()));
                ((NormalViewHolder) holder).pd_stockQty.setText(Integer.toString(product.getStockQty()));
                ((NormalViewHolder) holder).pd_card.setClickable(true);
                ((NormalViewHolder) holder).pd_card.setOnClickListener(new ProductOnClick(product));
            } else {
                ((ProductsFragment.productItemAdapter.FootViewHolder) holder).tips.setText((hasMore)? "Loading...": "Last one");
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
            TextView pd_title, pd_description, pd_price, pd_stockQty;
            NormalViewHolder(View itemView) {
                super(itemView);
                pd_card = itemView.findViewById(R.id.pd_card);
                pd_title = itemView.findViewById(R.id.pd_title);
                pd_description = itemView.findViewById(R.id.pd_description);
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
                navController.navigate(R.id.navigation_edit_product);
            }
        }
    }
}
