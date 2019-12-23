package tw.dp103g3.itfood.shop;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import tw.dp103g3.itfood.Common;
import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.Url;
import tw.dp103g3.itfood.task.CommonTask;
import tw.dp103g3.itfood.task.ImageTask;

public class ShopFragment extends Fragment {
    private final static String TAG = "TAG_ShopFragment";
    private AppCompatActivity activity;
    private Toolbar toolbar, tbTitle;
    private ImageView ivBack, ivShop;
    private ImageTask shopImageTask, dishImageTask;
    private CommonTask getdishTask;
    private Shop shop;
    private List<Dish> dishes;
    private TextView tvName, tvRate;
    private RecyclerView rvMenu;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (AppCompatActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shop, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        toolbar = view.findViewById(R.id.toolbar);
        tbTitle = view.findViewById(R.id.tbTitle);
        ViewGroup.LayoutParams params = toolbar.getLayoutParams();
        ViewGroup.LayoutParams titleParams = tbTitle.getLayoutParams();
        params.height += Common.getStatusBarHeight(activity);
        titleParams.height += Common.getStatusBarHeight(activity);
        toolbar.setLayoutParams(params);
        tbTitle.setLayoutParams(titleParams);
        toolbar.setPadding(0, Common.getStatusBarHeight(activity), 0, 0);
        tbTitle.setPadding(0, Common.getStatusBarHeight(activity), 0, 0);
        activity.setSupportActionBar(toolbar);
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        ivBack = view.findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> {
            Navigation.findNavController(v).popBackStack();
        });

        Bundle bundle = getArguments();
        shop = (Shop) bundle.getSerializable("shop");
        ivShop = view.findViewById(R.id.ivShop);
        String url = Url.URL + "/ShopServlet";
        int imageSize = getResources().getDisplayMetrics().widthPixels;
        shopImageTask = new ImageTask(url, shop.getId(), imageSize);
        try {
            Bitmap bitmap = shopImageTask.execute().get();
            ivShop.setImageBitmap(bitmap);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        tvName = view.findViewById(R.id.tvName);
        tvRate = view.findViewById(R.id.tvRate);
        tvName.setText(shop.getName());
        double rate = (double) shop.getTtscore() / shop.getTtrate();
        tvRate.setText(String.format(Locale.getDefault(),
                "%.1f(%d)", rate, shop.getTtrate()));

        rvMenu = view.findViewById(R.id.rvMenu);
        rvMenu.setLayoutManager(new LinearLayoutManager(activity));

    }

//    private void setAdapter(List<Dish> dishes) {
//        rvMenu.setAdapter(new MenuAdapter(activity, dishes));
//    }

//    private class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MyViewHolder> {
//        private Context context;
//        private List<Dish> dishes;
//        private int imageSize;
//
//        public MenuAdapter(Context context, List<Dish> dishes) {
//            this.context = context;
//            this.dishes = dishes;
//        }
//    }
}
