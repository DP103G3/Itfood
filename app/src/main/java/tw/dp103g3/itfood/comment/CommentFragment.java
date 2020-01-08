package tw.dp103g3.itfood.comment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import tw.dp103g3.itfood.Common;
import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.Url;
import tw.dp103g3.itfood.member.Member;
import tw.dp103g3.itfood.shop.Shop;
import tw.dp103g3.itfood.task.CommonTask;


public class CommentFragment extends Fragment {

    private Activity activity;
    private Button btPost, btClose;
    private TextView tvShopName, tvMemberName;
    private RatingBar rbRating;
    private EditText etComment;
    private Shop shop;
    private Member member;
    private final static String TAG = "TAG_CommentFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        btPost = view.findViewById(R.id.btPost);
        btClose = view.findViewById(R.id.btClose);
        tvShopName = view.findViewById(R.id.tvShopName);
        tvMemberName = view.findViewById(R.id.tvMemberName);
        rbRating = view.findViewById(R.id.rbRating);
        etComment = view.findViewById(R.id.etComment);

        btClose.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        Bundle bundle = getArguments();
        try {
            shop = (Shop) bundle.getSerializable("shop");
            member = (Member) bundle.getSerializable("member");
            tvShopName.setText(shop.getName());

            String rawMemberEmail = member.getMemEmail();
            String[] splitEmail = rawMemberEmail.split("@");
            //將會員的電子郵件由“＠”分開為兩部分，取前面顯示
            String memberEmail = splitEmail[0];
            tvMemberName.setText(memberEmail);

        } catch (NullPointerException e) {
            Common.showToast(activity, "error");
            e.printStackTrace();
            Navigation.findNavController(view).popBackStack();
        }

        btPost.setOnClickListener(v -> {
            int cmt_score = (int) rbRating.getRating();
            String cmt_detail = etComment.getText().toString();
            int shop_id = shop.getId();
            int mem_id = member.getMemId();
            int cmt_state = 1;


            if (Common.networkConnected(activity)) {
                String URL = Url.URL + "/CommentServlet";
                Comment comment = new Comment(cmt_score, cmt_detail, shop_id, mem_id, cmt_state);
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action", "commentInsert");
                jsonObject.addProperty("comment", new Gson().toJson(comment));

                int count = 0;
                try {
                    String result = new CommonTask(URL, jsonObject.toString()).execute().get();
                    count = Integer.valueOf(result);
                } catch (Exception e){
                    Log.e(TAG, e.toString());
                }
                if (count == 0){
                    Common.showToast(getActivity(), R.string.textPostCommentFail);
                } else{
                    Common.showToast(getActivity(), R.string.textPostCommentSuccess);
                }

            } else {
                Common.showToast(activity, R.string.textNoNetwork);
            }
            if (Common.networkConnected(activity)) {
                String URL = Url.URL + "/ShopServlet";
                int ttScore = shop.getTtscore();
                int ttRate = shop.getTtrate();
                ttScore += cmt_score;
                ttRate += 1;
                shop.setTtscore(ttScore);
                shop.setTtrate(ttRate);
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action", "update");
                jsonObject.addProperty("shop", new Gson().toJson(shop));

                int count = 0;
                try {
                    String result = new CommonTask(URL, jsonObject.toString()).execute().get();
                    count = Integer.valueOf(result);
                } catch (Exception e){
                    Log.e(TAG, e.toString());
                }
                if (count == 0){
                    Common.showToast(getActivity(), R.string.textPostCommentFail);
                } else{
                    Common.showToast(getActivity(), R.string.textPostCommentSuccess);
                }

            } else {
                Common.showToast(activity, R.string.textNoNetwork);
            }

            Navigation.findNavController(view).popBackStack();

        });


    }
}
