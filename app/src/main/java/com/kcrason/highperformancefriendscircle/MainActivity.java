package com.kcrason.highperformancefriendscircle;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.graphics.PorterDuff;

import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.kcrason.highperformancefriendscircle.adapters.FriendCircleAdapter;
import com.kcrason.highperformancefriendscircle.beans.FriendCircleBean;
import com.kcrason.highperformancefriendscircle.interfaces.OnPraiseOrCommentClickListener;
import com.kcrason.highperformancefriendscircle.others.DataCenter;
import com.kcrason.highperformancefriendscircle.others.FriendsCircleAdapterDivideLine;
import com.kcrason.highperformancefriendscircle.others.GlideSimpleTarget;
import com.kcrason.highperformancefriendscircle.utils.Utils;
import com.kcrason.highperformancefriendscircle.widgets.EmojiPanelView;

import java.util.List;

import ch.ielse.view.imagewatcher.ImageWatcher;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener,
        OnPraiseOrCommentClickListener, ImageWatcher.OnPictureLongPressListener, ImageWatcher.Loader {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Disposable mDisposable;
    private FriendCircleAdapter mFriendCircleAdapter;
    private ImageWatcher mImageWatcher;
    private EmojiPanelView mEmojiPanelView;
    private ImageView iv_camera, iv_back;
    private RelativeLayout rl_1;
    private TextView tv_title;
    private int bar_type = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        if (1 + 6 > 0) {
//            return;
//        }
        changeBarTransparent();

        iv_camera = (ImageView) findViewById(R.id.iv_camera);
        tv_title = (TextView) findViewById(R.id.tv_title);
        iv_back = (ImageView) findViewById(R.id.img_back);
        rl_1 = (RelativeLayout) findViewById(R.id.rl_1);
        iv_camera.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_IN);
        iv_back.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_IN);

//方法效果清除


        mEmojiPanelView = findViewById(R.id.emoji_panel_view);
        mEmojiPanelView.initEmojiPanel(DataCenter.emojiDataSources);
        mSwipeRefreshLayout = findViewById(R.id.swpie_refresh_layout);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
//        RecyclerView recyclerView = null ;
        mSwipeRefreshLayout.setOnRefreshListener(this);

//        findViewById(R.id.img_back).setOnClickListener(v ->
//                startActivity(new Intent(MainActivity.this, EmojiPanelActivity.class)));

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Glide.with(MainActivity.this).resumeRequests();
                } else {
                    Glide.with(MainActivity.this).pauseRequests();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        mImageWatcher = findViewById(R.id.image_watcher);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new FriendsCircleAdapterDivideLine());
        mFriendCircleAdapter = new FriendCircleAdapter(this, recyclerView, mImageWatcher);
        recyclerView.setAdapter(mFriendCircleAdapter);


        int[] position = new int[2];
        int statusBarHeight = getStatusBarHeight(this);
        int dividerHeight = dip2px(this, 30);
        int topBarHeight = dip2px(this, 68);
        int headerHeight = dip2px(this, 280);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int mmRvScrollY = 0; // 列表滑动距离

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                rl_1.getLocationOnScreen(position);
                mmRvScrollY += dy;

                // 判断是否更改背景
//                if (mmRvScrollY < 0) {
                int offset = headerHeight - mmRvScrollY;
                Log.i("roy", "宽高：" + rl_1.getWidth() + "/" + headerHeight
                        + "，滑动：" + mmRvScrollY + " offset:" + offset + " / " + (statusBarHeight + topBarHeight));
                if (offset < statusBarHeight + topBarHeight) {
                    if (bar_type != 1) {
                        bar_type = 1;
                        changeBarColor(Color.parseColor("#ffcbcaca"));
                        rl_1.setBackgroundColor(Color.parseColor("#cbcaca"));
                        tv_title.setTextColor(Color.parseColor("#222230"));
                        iv_camera.setColorFilter(Color.parseColor("#222230"), PorterDuff.Mode.SRC_IN);
                        iv_back.setColorFilter(Color.parseColor("#222230"), PorterDuff.Mode.SRC_IN);
                    }

                } else if (offset > statusBarHeight + topBarHeight && offset <= statusBarHeight + topBarHeight + dividerHeight) {
                    int x = 255 - 255 * (offset - statusBarHeight - topBarHeight) / dividerHeight;
                    bar_type = 2;
                    Log.i("roy", "x：" + x);
                    String hexStr = numToHex16(x);
                    rl_1.setBackgroundColor(Color.parseColor("#" + hexStr + "cbcaca"));
                    tv_title.setTextColor(Color.parseColor("#" + hexStr + "222230"));
                    changeBarColor(Color.parseColor("#" + hexStr + "cbcaca"));
                    iv_camera.setColorFilter(Color.parseColor("#" + hexStr + "222230"), PorterDuff.Mode.SRC_IN);
                    iv_back.setColorFilter(Color.parseColor("#" + hexStr + "222230"), PorterDuff.Mode.SRC_IN);
                } else {
                    if (bar_type != 0) {
                        bar_type = 0;
                        changeBarTransparent();
                        rl_1.setBackgroundColor(Color.TRANSPARENT);
                        tv_title.setTextColor(Color.TRANSPARENT);
                        iv_camera.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_IN);
                        iv_back.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_IN);
                    }

                }

//                if (offset < statusBarHeight + topBarHeight) {// TopBar灰色
//                    rl_1.setBackgroundColor(Color.parseColor("#cbcaca"));
//                    tv_title.setTextColor(Color.parseColor("#666666"));
//                } else if (offset > statusBarHeight + topBarHeight
//                        && offset <= statusBarHeight + topBarHeight + dividerHeight) {// TopBar渐变色
//                    int x = 255 - 255 * (offset - statusBarHeight - topBarHeight) / dividerHeight;
//                    String hexStr = numToHex16(x);
//                    rl_1.setBackgroundColor(Color.parseColor("#" + hexStr + "cbcaca"));
//                    tv_title.setTextColor(Color.parseColor("#" + hexStr + "666666"));
//                } else {// TopBar透明
//                    rl_1.setBackgroundColor(Color.TRANSPARENT);
//                    tv_title.setTextColor(Color.TRANSPARENT);
//                }

            }

//            }
        });


        mImageWatcher.setTranslucentStatus(Utils.calcStatusBarHeight(this));
        mImageWatcher.setErrorImageRes(R.mipmap.error_picture);
        mImageWatcher.setOnPictureLongPressListener(this);
        mImageWatcher.setLoader(this);
        Utils.showSwipeRefreshLayout(mSwipeRefreshLayout, this::asyncMakeData);
    }


    private void asyncMakeData() {
        mDisposable = Single.create((SingleOnSubscribe<List<FriendCircleBean>>) emitter ->
                emitter.onSuccess(DataCenter.makeFriendCircleBeans(this)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((friendCircleBeans, throwable) -> {
                    Utils.hideSwipeRefreshLayout(mSwipeRefreshLayout);
                    if (friendCircleBeans != null && throwable == null) {
                        mFriendCircleAdapter.setFriendCircleBeans(friendCircleBeans);
                    }
                });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }

    @Override
    public void onRefresh() {
        asyncMakeData();
    }

    @Override
    public void onPraiseClick(int position) {
        Toast.makeText(this, "You Click Praise!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCommentClick(int position) {
//        Toast.makeText(this, "you click comment", Toast.LENGTH_SHORT).show();
        mEmojiPanelView.showEmojiPanel();
    }

    @Override
    public void onBackPressed() {
        if (!mImageWatcher.handleBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    public void onPictureLongPress(ImageView v, String url, int pos) {

    }


    @Override
    public void load(Context context, String url, ImageWatcher.LoadCallback lc) {
        Glide.with(context).asBitmap().load(url).into(new GlideSimpleTarget(lc));
    }


    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen",
                "android");
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static String numToHex16(int b) {
        return String.format("%02x", b);
    }


    private void changeBarTransparent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    private void changeBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
    }

}
