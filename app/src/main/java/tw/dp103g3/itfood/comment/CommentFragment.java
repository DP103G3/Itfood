package tw.dp103g3.itfood.comment;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import tw.dp103g3.itfood.Common;
import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.Url;
import tw.dp103g3.itfood.member.Member;
import tw.dp103g3.itfood.shop.Shop;
import tw.dp103g3.itfood.task.CommonTask;

import static tw.dp103g3.itfood.Common.DATE_FORMAT;


public class CommentFragment extends Fragment {

    private Activity activity;
    private RatingBar rbRating;
    private EditText etComment;
    private Shop shop;
    private Member member;
    private final static String TAG = "TAG_CommentFragment";
    private Comment comment;

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
        Button btPost = view.findViewById(R.id.btPost);
        ImageButton btClose = view.findViewById(R.id.btClose);
        TextView tvShopName = view.findViewById(R.id.tvShopName);
        TextView tvMemberName = view.findViewById(R.id.tvMemberName);
        rbRating = view.findViewById(R.id.rbRating);
        etComment = view.findViewById(R.id.etComment);

        btClose.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        Bundle bundle = getArguments();
        if (bundle.getString("action").equals("edit")) {
            int cmt_id = bundle.getInt("cmt_id");
            comment = getComment(cmt_id);
            etComment.setText(comment.getCmt_detail());
            rbRating.setRating(comment.getCmt_score());
        }
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
                int cmt_id = 0;
                if (comment != null) {
                    cmt_id = comment.getCmt_id();
                }

                comment = new Comment(cmt_score, cmt_detail, shop_id, mem_id, cmt_state);

                Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();

                JsonObject jsonObject = new JsonObject();
                if (bundle.getString("action").equals("insert")) {
                    jsonObject.addProperty("action", "commentInsert");
                } else if (bundle.getString("action").equals("edit")) {
                    jsonObject.addProperty("action", "commentUpdate");
                    comment.setCmt_id(cmt_id);
                }
                int ttScore = shop.getTtscore();
                int ttRate = shop.getTtrate();
                if (bundle.getString("action").equals("insert")) {
                    ttScore += cmt_score;
                    ttRate += 1;
                } else if (bundle.getString("action").equals("edit")) {
                    ttScore = ttScore - comment.getCmt_score() + cmt_score;
                }

                shop.setTtscore(ttScore);
                shop.setTtrate(ttRate);

                jsonObject.addProperty("comment", gson.toJson(comment));
                jsonObject.addProperty("shop", gson.toJson(shop));

                int count = 0;
                try {
                    String result = new CommonTask(URL, jsonObject.toString()).execute().get();
                    count = Integer.valueOf(result);
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
                if (count == 0) {
                    Common.showToast(getActivity(), R.string.textPostCommentFail);
                } else {
                    Common.showToast(getActivity(), R.string.textPostCommentSuccess);
                }

            } else {
                Common.showToast(activity, R.string.textNoNetwork);
            }

            Navigation.findNavController(view).popBackStack();

        });


    }

    private Comment getComment(int cmt_id) {
        Comment comment = null;
        if (Common.networkConnected(activity)) {
            String url = Url.URL + "/CommentServlet";
            JsonObject jsonObject = new JsonObject();
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
            jsonObject.addProperty("action", "findByCommentId");
            jsonObject.addProperty("cmt_id", cmt_id);
            String jsonOut = jsonObject.toString();
            CommonTask getMemberTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = getMemberTask.execute().get();
                comment = gson.fromJson(jsonIn, Comment.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return comment;
    }
}
