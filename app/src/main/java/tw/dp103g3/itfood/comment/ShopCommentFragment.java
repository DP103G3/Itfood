package tw.dp103g3.itfood.comment;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import tw.dp103g3.itfood.Common;
import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.Url;
import tw.dp103g3.itfood.member.Member;
import tw.dp103g3.itfood.shop.Shop;
import tw.dp103g3.itfood.task.CommonTask;
import tw.dp103g3.itfood.task.ImageTask;


public class ShopCommentFragment extends Fragment {
    private final static String TAG = "TAG_ShopCommentFragment";
    private Activity activity;
    private ImageView ivShop, ivBack;
    private TextView tvCommentsTotal, tvName, tvRatingTotal, tvAverageRating;
    private RecyclerView rvComments;
    private Shop shop;
    private ImageTask shopImageTask;
    private CommonTask getMemberTask, getCommentTask;
    private List<Comment> comments;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shop_comment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initToolbar(R.id.toolbar, view);
        ivShop = view.findViewById(R.id.ivShop);
        ivBack = view.findViewById(R.id.ivBack);
        tvCommentsTotal = view.findViewById(R.id.tvCommentsTotal);
        tvName = view.findViewById(R.id.tvName);
        tvRatingTotal = view.findViewById(R.id.tvRatingTotal);
        tvAverageRating = view.findViewById(R.id.tvAverageRating);
        rvComments = view.findViewById(R.id.rvComments);

        Bundle bundle = getArguments();
        shop = (Shop) bundle.getSerializable("shop");
        String url = Url.URL + "/ShopServlet";
        int imageSize = getResources().getDisplayMetrics().widthPixels;
        shopImageTask = new ImageTask(url, shop.getId(), imageSize);
        try {
            Bitmap bitmap = shopImageTask.execute().get();
            ivShop.setImageBitmap(bitmap);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        tvName.setText(shop.getName());
        double rate = (double) shop.getTtscore() / shop.getTtrate();
        tvAverageRating.setText(String.format(Locale.getDefault(),"%.1f", rate));
        tvRatingTotal.setText(String.format("(%s)",String.valueOf(shop.getTtrate())));
        rvComments.setLayoutManager(new LinearLayoutManager(activity));
        comments = getComments();
        ShowComments(comments);
        tvCommentsTotal.setText(String.valueOf(comments.size()));
        ivBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

    }

    private List<Comment> getComments(){
        List<Comment> comments = new ArrayList<>();
        if (Common.networkConnected(activity)){
            String url = Url.URL + "/CommentServlet";
            JsonObject jsonObject = new JsonObject();
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
            jsonObject.addProperty("action", "findByCaseWithState");
            jsonObject.addProperty("id", shop.getId());
            jsonObject.addProperty("type", "shop");
            jsonObject.addProperty("state", 1);
            String jsonOut = jsonObject.toString();
            getCommentTask = new CommonTask(url, jsonOut);
            try{
                String jsonIn = getCommentTask.execute().get();
                Type listType = new TypeToken<List<Comment>>(){
                }.getType();
                comments = gson.fromJson(jsonIn, listType);
            } catch (Exception e){
                e.printStackTrace();
            }
        } else{
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return comments;
    }

    private Member getMember(int mem_id){
        Member member = null;
        if (Common.networkConnected(activity)){
            String url = Url.URL + "/MemberServlet";
            JsonObject jsonObject = new JsonObject();
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
            jsonObject.addProperty("action", "findById");
            jsonObject.addProperty("mem_id", mem_id);
            String jsonOut = jsonObject.toString();
            getMemberTask = new CommonTask(url, jsonOut);
            try{
                String jsonIn = getMemberTask.execute().get();
                member = gson.fromJson(jsonIn, Member.class);
            } catch(Exception e){
                e.printStackTrace();
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return  member;
    }




    private void ShowComments(List<Comment> comments){
        if(comments == null || comments.isEmpty()){
            if(Common.networkConnected(activity)){
                Common.showToast(activity,R.string.textNoComments);
            } else{
                Common.showToast(activity, R.string.textNoNetwork);
            }
        }
        CommentAdapter commentAdapter = (CommentAdapter) rvComments.getAdapter();
        if (commentAdapter == null){
            rvComments.setAdapter(new CommentAdapter(activity, comments));
        } else {
            commentAdapter.setComments(comments);
            commentAdapter.notifyDataSetChanged();
        }
    }

    private class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.MyViewHolder> {
        private Context context;
        private List<Comment> comments;

        CommentAdapter(Context context, List<Comment> comments){
            this.context = context;
            this.comments = comments;
        }

        void setComments(List<Comment> comments) {
            this.comments = comments;
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        private class MyViewHolder extends RecyclerView.ViewHolder{
            TextView tvUsername, tvCommentTime, tvCommentDetail;
            RatingBar ratingBar;
            MyViewHolder(@NonNull View itemView){
                super(itemView);
                tvUsername = itemView.findViewById(R.id.tvUsername);
                tvCommentTime = itemView.findViewById(R.id.tvCommentTime);
                tvCommentDetail = itemView.findViewById(R.id.tvCommentDetail);
                ratingBar = itemView.findViewById(R.id.ratingBar);

            }
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.comment_item_view,parent,false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final Comment comment = comments.get(position);
            Member member = getMember(comment.getMem_id());

            String rawMemberEmail = member.getMemEmail();
            String[] splitEmail = rawMemberEmail.split("@");
            //將會員的電子郵件由“＠”分開為兩部分，取前面顯示
            String memberEmail = splitEmail[0];

            holder.ratingBar.setRating(comment.getCmt_score());
            holder.tvUsername.setText(memberEmail);
            holder.tvCommentDetail.setText(comment.getCmt_detail());
            holder.tvCommentTime.setText(new SimpleDateFormat("MM月 dd, yyyy", Locale.getDefault()).format(comment.getCmt_time()));



        }



    }

    private void initToolbar(int resId, View view) {
        Toolbar toolbar = view.findViewById(resId);
        ViewGroup.LayoutParams params = toolbar.getLayoutParams();
        params.height += Common.getStatusBarHeight(activity);
        toolbar.setLayoutParams(params);
        toolbar.setPadding(0, Common.getStatusBarHeight(activity), 0, 0);
    }
}
