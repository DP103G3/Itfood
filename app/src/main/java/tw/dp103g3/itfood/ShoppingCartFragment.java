package tw.dp103g3.itfood;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import static tw.dp103g3.itfood.Common.PREFERENCES_CART;


/**
 * A simple {@link Fragment} subclass.
 */
public class ShoppingCartFragment extends Fragment {
    private Toolbar toolbarShoppingCart;
    private SharedPreferences pref;
    private Activity activity;
    private boolean cartIsEmpty;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        pref = activity.getSharedPreferences(PREFERENCES_CART, Context.MODE_PRIVATE);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shopping_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbarShoppingCart = view.findViewById(R.id.toolbarShoppingCart);
        toolbarShoppingCart.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).popBackStack();
            }
        });
    }
}
