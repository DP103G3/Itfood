package tw.dp103g3.itfood.shop;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import tw.dp103g3.itfood.R;

public class MainFragment extends Fragment {
    private AppCompatActivity activity;
    private RecyclerView rvNewShop, rvAllShop;
    private Toolbar toolbar;
    private List<Shop> shops;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (AppCompatActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        toolbar = view.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);
        rvNewShop = view.findViewById(R.id.rvNewShop);
        rvNewShop.setLayoutManager(new GridLayoutManager(
                activity, 1, RecyclerView.HORIZONTAL, false));
        shops = getShops();
    }

    private List<Shop> getShops() {

        return null;
    }
}
