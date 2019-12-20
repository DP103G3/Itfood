package tw.dp103g3.itfood.shop;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.material.appbar.CollapsingToolbarLayout;

import tw.dp103g3.itfood.Common;
import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.task.ImageTask;


/**
 * A simple {@link Fragment} subclass.
 */
public class ShopFragment extends Fragment {
    private final static String TAG = "TAG_ShopFragment";
    private AppCompatActivity activity;
    private Toolbar toolbar;
    private ImageView ivBack, ivShop;
    private ImageTask shopImageTask, dishImageTask;

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
        ViewGroup.LayoutParams params = toolbar.getLayoutParams();
        params.height += Common.getStatusBarHeight(activity);
        toolbar.setLayoutParams(params);
        toolbar.setPadding(0, Common.getStatusBarHeight(activity), 0, 0);
        activity.setSupportActionBar(toolbar);
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);

        ivBack = view.findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> {
            Navigation.findNavController(v).popBackStack();
        });

        ivShop = view.findViewById(R.id.ivShop);

    }
}
