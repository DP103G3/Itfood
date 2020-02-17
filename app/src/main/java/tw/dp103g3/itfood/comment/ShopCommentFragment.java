package tw.dp103g3.itfood.comment;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import tw.dp103g3.itfood.Common;
import tw.dp103g3.itfood.R;
import tw.dp103g3.itfood.Url;
import tw.dp103g3.itfood.member.Member;
import tw.dp103g3.itfood.order.OrderAnimations;
import tw.dp103g3.itfood.shop.Shop;
import tw.dp103g3.itfood.task.CommonTask;
import tw.dp103g3.itfood.task.ImageTask;

import static android.view.View.GONE;
import static tw.dp103g3.itfood.Common.PREFERENCES_MEMBER;


public class ShopCommentFragment extends Fragment {
    private final static String TAG = "TAG_ShopCommentFragment";
    private Activity activity;
    private ImageView ivShop, ivBack;
    private TextView tvCommentsTotal, tvName, tvRatingTotal, tvAverageRating;
    private RecyclerView rvComments;
    private Shop shop;
    private CommonTask getCommentTask;
    private LinearLayout layoutCommentLoggedIn, layoutShopComment;
    private ConstraintLayout layoutCommentedTrue, layoutCommentedFalse, layoutReply, layoutExpandable;
    private Button btPostComment;
    private Member member;
    private int cmt_id;
    private BottomNavigationView bottomNavigationView;
    private Fragment thisFragment;
    private View view;

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
        thisFragment = this;
        this.view = view;
        layoutShopComment = view.findViewById(R.id.layoutShopComment);
        layoutReply = view.findViewById(R.id.layoutReply);
        layoutExpandable = view.findViewById(R.id.layoutExpandable);
        TextView tvUsername, tvCommentTime, tvCommentDetail;
        RatingBar ratingBar;
        ImageButton btCommentOptionMenu;
        tvUsername = view.findViewById(R.id.tvUsername);
        tvCommentTime = view.findViewById(R.id.tvCommentTime);
        tvCommentDetail = view.findViewById(R.id.tvCommentDetail);
        ratingBar = view.findViewById(R.id.ratingBarComment);
        btCommentOptionMenu = view.findViewById(R.id.btCommentOptionMenu);

        handleViews();

        //確認使用者為訪客或會員，從SharedPreferences取得"mem_id"，如果是0的就是沒有登入
        SharedPreferences preferences = activity.getSharedPreferences(PREFERENCES_MEMBER, Context.MODE_PRIVATE);

        int mem_id = preferences.getInt("mem_id", 0);
        member = getMember(mem_id);
        Bundle sendBundle = new Bundle();
        Bundle gotBundle = getArguments();
        shop = (Shop) gotBundle.getSerializable("shop");

        btPostComment.setOnClickListener(v -> {
            sendBundle.putString("action", "insert");
            sendBundle.putSerializable("member", member);
            sendBundle.putSerializable("shop", shop);
            Navigation.findNavController(v).navigate(R.id.action_shopCommentFragment_to_commentFragment, sendBundle);
        });

        //如果preferences中的mem_id為0即代表為未登入
        List<Comment> comments;
        if (mem_id == 0) {
            layoutCommentLoggedIn.setVisibility(GONE);
        } else {
            if (Common.networkConnected(activity)) {
                String url = Url.URL + "/CommentServlet";
                JsonObject jsonObject = new JsonObject();
                Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
                jsonObject.addProperty("action", "findByCaseWithState");
                jsonObject.addProperty("id", mem_id);
                jsonObject.addProperty("type", "member");
                jsonObject.addProperty("state", 1);
                String jsonOut = jsonObject.toString();
                getCommentTask = new CommonTask(url, jsonOut);

                try {
                    Comment comment;
                    String jsonIn = getCommentTask.execute().get();
                    Type listType = new TypeToken<List<Comment>>() {
                    }.getType();
                    comments = gson.fromJson(jsonIn, listType);

                    List<Comment> filteredComments = comments.stream()
                            .filter(cmt -> cmt.getShop_id() == shop.getId())
                            .collect(Collectors.toList());
                    if (!filteredComments.isEmpty()) {
                        comment = filteredComments.get(0);
                    } else {
                        comment = null;
                    }

                    System.out.println(TAG + "會員comment:" + jsonIn);
                    if (comment != null) {
                        layoutCommentedTrue.setVisibility(View.VISIBLE);
                        layoutReply.setVisibility(GONE);
                        layoutExpandable.setVisibility(GONE);

                        Member member = getMember(mem_id);
                        String rawMemberEmail = member.getMemEmail();
                        String[] splitEmail = rawMemberEmail.split("@");
                        //將會員的電子郵件由“＠”分開為兩部分，取前面顯示
                        String memberEmail = splitEmail[0];

                        Log.d(TAG, "COMMENT SCORE : " + comment.getCmt_score());

                        ratingBar.setIsIndicator(true);
                        ratingBar.setVisibility(View.VISIBLE);
                        ratingBar.setNumStars(5);
                        ratingBar.setStepSize(1);
                        ratingBar.setRating(comment.getCmt_score());
                        tvUsername.setText(memberEmail);
                        tvCommentDetail.setText(comment.getCmt_detail());
                        tvCommentTime.setText(new SimpleDateFormat("MM月 dd, yyyy", Locale.getDefault()).format(comment.getCmt_time()));

                        btCommentOptionMenu.setOnClickListener(v -> {
                            cmt_id = comment.getCmt_id();
                            showOptionMenu();
                        });

                        if (comment.getCmt_feedback() != null && !comment.getCmt_feedback().isEmpty() && comment.getCmt_feedback_state() == 1) {
                            TextView tvShopName = view.findViewById(R.id.tvShopName);
                            TextView tvReplyDate = view.findViewById(R.id.tvReplyDate);
                            TextView tvReplayContent = view.findViewById(R.id.tvReplyContent);
                            TextView tvExpandReply = view.findViewById(R.id.tvExpandReply);
                            ImageButton ibExpandable = view.findViewById(R.id.ibExpandable);
                            layoutExpandable.setVisibility(View.VISIBLE);

                            tvShopName.setText("店主");
                            tvReplayContent.setText(comment.getCmt_feedback());
                            tvReplyDate.setText(new SimpleDateFormat("MM月 dd, yyyy", Locale.getDefault()).format(comment.getCmt_feedback_time()));
                            View.OnClickListener onClickListener = v -> {
                                boolean show = toggleLayout(!comment.isExpanded(), ibExpandable, layoutReply);
                                swapView(layoutExpandable, show);
                                comment.setExpanded(show);
                            };
                            layoutExpandable.setOnClickListener(onClickListener);
                            ibExpandable.setOnClickListener(onClickListener);
                            tvExpandReply.setOnClickListener(onClickListener);


                        }

                    } else {
                        layoutCommentedFalse.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    System.out.println(TAG + e.toString());
                }
            } else {
                Common.showToast(activity, R.string.textNoNetwork);
            }
        }

        int color = Color.argb(255, 255, 255, 255);
        ivBack.setColorFilter(color);

        Bundle bundle = getArguments();
        shop = (Shop) bundle.getSerializable("shop");
        String url = Url.URL + "/ShopServlet";
        int imageSize = getResources().getDisplayMetrics().widthPixels;
        ImageTask shopImageTask = new ImageTask(url, shop.getId(), imageSize);
        try {
            Bitmap bitmap = shopImageTask.execute().get();
            ivShop.setImageBitmap(bitmap);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        tvName.setText(shop.getName());
        double rate = (double) shop.getTtscore() / shop.getTtrate();
        tvAverageRating.setText(String.format(Locale.getDefault(), "%.1f", rate));
        tvRatingTotal.setText(String.format("(%s)", String.valueOf(shop.getTtrate())));
        rvComments.setPadding(0, 0, 0, Common.getNavigationBarHeight(activity));
        rvComments.setLayoutManager(new LinearLayoutManager(activity));

        comments = getComments();

        int index = -1;
        for (Comment comment : comments) {
            if (comment.getMem_id() == mem_id) {
                index = comments.indexOf(comment);
            }
        }
        ShowComments(comments);
        if (index != -1) {
            comments.remove(index);
            tvCommentsTotal.setText(String.valueOf(comments.size() + 1));
        } else {
            tvCommentsTotal.setText(String.valueOf(comments.size()));
        }

        ivBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

    }

    private List<Comment> getComments() {
        List<Comment> comments = new ArrayList<>();
        if (Common.networkConnected(activity)) {
            String url = Url.URL + "/CommentServlet";
            JsonObject jsonObject = new JsonObject();
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
            jsonObject.addProperty("action", "findByCaseWithState");
            jsonObject.addProperty("id", shop.getId());
            jsonObject.addProperty("type", "shop");
            jsonObject.addProperty("state", 1);
            String jsonOut = jsonObject.toString();
            getCommentTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = getCommentTask.execute().get();
                Type listType = new TypeToken<List<Comment>>() {
                }.getType();
                comments = gson.fromJson(jsonIn, listType);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return comments;
    }

    private Member getMember(int mem_id) {
        Member member = null;
        if (Common.networkConnected(activity)) {
            String url = Url.URL + "/MemberServlet";
            JsonObject jsonObject = new JsonObject();
            Gson gson = new GsonBuilder().setDateFormat(Common.DATE_FORMAT).create();
            jsonObject.addProperty("action", "findById");
            jsonObject.addProperty("mem_id", mem_id);
            String jsonOut = jsonObject.toString();
            CommonTask getMemberTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = getMemberTask.execute().get();
                member = gson.fromJson(jsonIn, Member.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return member;
    }


    private Comment getComment(int cmt_id) {
        Comment comment = null;
        if (Common.networkConnected(activity)) {
            String url = Url.URL + "/CommentServlet";
            JsonObject jsonObject = new JsonObject();
            Gson gson = new GsonBuilder().setDateFormat(Common.DATE_FORMAT).create();
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


    private void handleViews() {
        ivShop = layoutShopComment.findViewById(R.id.ivShop);
        ivBack = layoutShopComment.findViewById(R.id.ivBack);
        tvCommentsTotal = layoutShopComment.findViewById(R.id.tvCommentsTotal);
        tvName = layoutShopComment.findViewById(R.id.tvName);
        tvRatingTotal = layoutShopComment.findViewById(R.id.tvRatingTotal);
        tvAverageRating = layoutShopComment.findViewById(R.id.tvAverageRating);
        rvComments = layoutShopComment.findViewById(R.id.rvComments);
        layoutCommentLoggedIn = layoutShopComment.findViewById(R.id.layoutCommentLoggedIn);
        layoutCommentedFalse = layoutShopComment.findViewById(R.id.layoutCommentedFalse);
        layoutCommentedTrue = layoutShopComment.findViewById(R.id.layoutCommentedTrue);
        btPostComment = layoutShopComment.findViewById(R.id.btPostComment);
    }

    private void showOptionMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        String[] options = new String[]{"編輯評論", "刪除評論"};
        builder.setTitle("評論選項");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0){
                Bundle bundle = new Bundle();
                bundle.putString("action", "edit");
                bundle.putSerializable("member", member);
                bundle.putSerializable("cmt_id", cmt_id);
                bundle.putSerializable("shop", shop);
                System.out.println(TAG + "output:" + bundle.toString());
                dialog.dismiss();
                Navigation.findNavController(view).navigate(R.id.action_shopCommentFragment_to_commentFragment, bundle);
            } else {
                AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(activity);
                deleteBuilder.setTitle("刪除評論");
                deleteBuilder.setMessage("你確定要刪除這筆評論？");
                deleteBuilder.setPositiveButton("確定", (dialog12, which12) -> {
                    if (Common.networkConnected(activity)) {
                        Comment comment = getComment(cmt_id);
                        if (comment.getCmt_state() != 0) {
                            comment.setCmt_state(0);
                            int originalTtrate = shop.getTtrate();
                            int originalTtscroe = shop.getTtscore();
                            shop.setTtrate(shop.getTtrate() - 1);
                            shop.setTtscore(shop.getTtscore() - comment.getCmt_score());
                            JsonObject jsonObject = new JsonObject();
                            Gson gson = new GsonBuilder().setDateFormat(Common.DATE_FORMAT).create();
                            String jsonOut = gson.toJson(comment, Comment.class);
                            String shopJson = gson.toJson(shop, Shop.class);

                            jsonObject.addProperty("action", "commentUpdate");
                            jsonObject.addProperty("comment", jsonOut);
                            jsonObject.addProperty("shop", shopJson);

                            CommonTask commonTask = new CommonTask(Url.URL + "/CommentServlet", jsonObject.toString());

                            int count = 0;
                            try {
                                String jsonIn = commonTask.execute().get();
                                count = Integer.valueOf(jsonIn);
                            } catch (Exception e) {
                                Log.e(TAG, e.toString());
                            }
                            if (count == 0) {
                                shop.setTtscore(originalTtscroe);
                                shop.setTtrate(originalTtrate);
                                Common.showToast(activity, "刪除評論失敗");
                            } else {
                                Common.showToast(activity, "刪除評論成功");
                                FragmentTransaction ft = getFragmentManager().beginTransaction();
                                if (Build.VERSION.SDK_INT >= 26) {
                                    ft.setReorderingAllowed(false);
                                }
                                dialog12.dismiss();
                                ft.detach(thisFragment).attach(thisFragment).commit();
                            }
                        } else {
                            Common.showToast(activity, "刪除評論失敗");
                        }
                    } else {
                        Common.showToast(activity, R.string.textNoNetwork);
                    }
                });
                deleteBuilder.setNegativeButton("取消", (dialog1, which1) -> dialog1.dismiss());
                Dialog deleteDialog = deleteBuilder.create();
                Common.setDialogUi(deleteDialog, activity);
                dialog.dismiss();
                deleteDialog.show();
            }
        });
        Dialog dialog = builder.create();
        Common.setDialogUi(dialog, activity);
        dialog.show();
    }

    private void ShowComments(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            if (Common.networkConnected(activity)) {
                Common.showToast(activity, R.string.textNoComments);
            } else {
                Common.showToast(activity, R.string.textNoNetwork);
            }
        }
        CommentAdapter commentAdapter = (CommentAdapter) rvComments.getAdapter();
        if (commentAdapter == null) {
            rvComments.setAdapter(new CommentAdapter(activity, comments));
        } else {
            commentAdapter.setComments(comments);
            commentAdapter.notifyDataSetChanged();
        }
    }

    private class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.MyViewHolder> {
        private Context context;
        private List<Comment> comments;

        CommentAdapter(Context context, List<Comment> comments) {
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

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final Comment comment = comments.get(position);
            holder.layoutReply.setVisibility(GONE);
            Member member = getMember(comment.getMem_id());
            String rawMemberEmail = member.getMemEmail();
            String[] splitEmail = rawMemberEmail.split("@");
            //將會員的電子郵件由“＠”分開為兩部分，取前面顯示
            String memberEmail = splitEmail[0];
            holder.tvUsername.setText(memberEmail);
            holder.ratingBar.setRating(comment.getCmt_score());
            holder.tvCommentDetail.setText(comment.getCmt_detail());
            holder.tvCommentTime.setText(new SimpleDateFormat("MM月 dd, yyyy", Locale.getDefault()).format(comment.getCmt_time()));
            if (comment.getCmt_feedback() == null || comment.getCmt_feedback().isEmpty() || comment.getCmt_feedback_state() == 0) {
                holder.layoutExpandable.setVisibility(GONE);
            } else {
                holder.tvShopName.setText("店主");
                holder.tvReplayContent.setText(comment.getCmt_feedback());
                holder.tvReplyDate.setText(new SimpleDateFormat("MM月 dd, yyyy", Locale.getDefault()).format(comment.getCmt_feedback_time()));
                View.OnClickListener onClickListener = v -> {
                    boolean show = toggleLayout(!comment.isExpanded(), holder.ibExpandable, holder.layoutReply);
                    swapView(holder.layoutExpandable, show);
                    comment.setExpanded(show);
                };
                holder.layoutExpandable.setOnClickListener(onClickListener);
                holder.ibExpandable.setOnClickListener(onClickListener);
                holder.tvExpandReply.setOnClickListener(onClickListener);

            }
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.comment_item_view, parent, false);
            return new MyViewHolder(itemView);
        }




        private class MyViewHolder extends RecyclerView.ViewHolder {
            TextView tvUsername, tvCommentTime, tvCommentDetail, tvShopName, tvReplyDate, tvReplayContent, tvExpandReply;
            ImageButton ibExpandable;
            RatingBar ratingBar;
            ConstraintLayout layoutReply, layoutExpandable;

            MyViewHolder(@NonNull View itemView) {
                super(itemView);
                tvUsername = itemView.findViewById(R.id.tvUsername);
                tvCommentTime = itemView.findViewById(R.id.tvCommentTime);
                tvCommentDetail = itemView.findViewById(R.id.tvCommentDetail);
                ratingBar = itemView.findViewById(R.id.ratingBar);
                layoutReply = itemView.findViewById(R.id.layoutReply);
                layoutExpandable = itemView.findViewById(R.id.layoutExpandable);
                tvReplyDate = itemView.findViewById(R.id.tvReplyDate);
                tvShopName = itemView.findViewById(R.id.tvShopName);
                tvReplayContent = itemView.findViewById(R.id.tvReplyContent);
                tvExpandReply = itemView.findViewById(R.id.tvExpandReply);
                ibExpandable = itemView.findViewById(R.id.ibExpandable);
            }
        }
    }

    private void swapView(ConstraintLayout v, boolean show) {
        ConstraintSet csOld = new ConstraintSet();
        ConstraintSet csNew = new ConstraintSet();
        csOld.clone(activity, R.layout.comment_reply_not_expanded_view);
        csNew.clone(activity, R.layout.comment_reply_expanded_view);
        if (show) {
            csNew.applyTo(v);
        } else {
            csOld.applyTo(v);
        }
    }

    private boolean toggleLayout(boolean isExpanded, View v, ConstraintLayout cl) {
        OrderAnimations.toggleArrow(v, isExpanded);
        if (isExpanded) {
            OrderAnimations.expand(cl);
        } else {
            OrderAnimations.collapse(cl);
        }
        return isExpanded;
    }
}