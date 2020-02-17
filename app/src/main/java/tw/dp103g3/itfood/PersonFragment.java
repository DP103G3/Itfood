package tw.dp103g3.itfood;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tw.dp103g3.itfood.shopping_cart.LoginDialogFragment;

public class PersonFragment extends Fragment implements LoginDialogFragment.LoginDialogContract {
    private Activity activity;
    private ListAdapter memberAdapter, guestAdapter;
    private int[] memberAction, guestAction;
    private List<Map<String, Object>> memberList, guestList;
    private ListView listView;
    private int memId;
    private SharedPreferences preferences;
    private ImageView ivCart;
    private View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        preferences =
                activity.getSharedPreferences(Common.PREFERENCES_MEMBER, Context.MODE_PRIVATE);
        memId = Common.getMemId(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_person, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        this.view = view;
        Common.disconnectServer();
        initListMap();
        ivCart = view.findViewById(R.id.ivCart);
        ivCart.setVisibility(View.GONE);
        listView = view.findViewById(R.id.listView);
        int backgroundColor = getResources().getColor(R.color.colorItemBackground, activity.getTheme());
        listView.setBackgroundColor(backgroundColor);
        if (memId == 0) {
            guestAdapter = new SimpleAdapter(activity, guestList, R.layout.basic_list_item,
                    new String[]{"icon", "title"}, new int[]{R.id.ivIcon, R.id.tvTitle});
            listView.setAdapter(guestAdapter);
            listView.setOnItemClickListener(((parent, v, position, id) ->
                    Common.showLoginDialog(this)));
        } else {
            memberAdapter = new SimpleAdapter(activity, memberList, R.layout.basic_list_item,
                    new String[]{"icon", "title"}, new int[]{R.id.ivIcon, R.id.tvTitle});
            listView.setAdapter(memberAdapter);
            listView.setOnItemClickListener(((parent, v, position, id) -> {
                NavController navController = Navigation.findNavController(v);
                if (position != 4) {
                    navController.navigate(memberAction[position]);
                } else {
                    preferences.edit().putInt("mem_id", 0).apply();
                    navController.popBackStack(R.id.mainFragment, false);
                    Common.showToast(activity, "已登出。");
                }
            }));
        }
    }

    private void initListMap() {
        memberList = new ArrayList<>();
        int[] memberIcon = new int[]{R.drawable.person, R.drawable.payment, R.drawable.location, R.drawable.ic_local_dining_black_24dp, R.drawable.logout};
        String[] memberTitle = new String[]{getString(R.string.textPersonInfo), getString(R.string.textPayment), getString(R.string.textSendLocation), getString(R.string.textFavoriteShops), getString(R.string.textLogout)};
        memberAction = new int[]{R.id.action_personFragment_to_personalInfoFragment,
                R.id.action_personFragment_to_paymentFragment,
                R.id.action_personFragment_to_addressFragment,
                R.id.action_personFragment_to_favoriteFragment,};
        guestList = new ArrayList<>();
        int[] guestIcon = new int[]{R.drawable.login};
        String[] guestTitle = new String[]{getString(R.string.textLogin)};
        for (int i = 0; i < memberIcon.length; i++) {
            Map<String, Object> memberItem = new HashMap<>();
            memberItem.put("icon", memberIcon[i]);
            memberItem.put("title", memberTitle[i]);
            memberList.add(memberItem);
        }
        for (int i = 0; i < guestIcon.length; i++) {
            Map<String, Object> guestItem = new HashMap<>();
            guestItem.put("icon", guestIcon[i]);
            guestItem.put("title", guestTitle[i]);
            guestList.add(guestItem);
        }
    }

    @Override
    public void sendLoginResult(boolean isSuccessful) {
        if (isSuccessful) {
            Navigation.findNavController(view).popBackStack(R.id.mainFragment, false);
        }

    }
}
