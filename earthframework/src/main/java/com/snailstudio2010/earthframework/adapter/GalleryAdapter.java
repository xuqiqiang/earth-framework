package com.snailstudio2010.earthframework.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.snailstudio2010.earthframework.R;
import com.snailstudio2010.earthframework.entity.ArticlePoint;
import com.snailstudio2010.earthframework.gallery.GalleryView;
import com.snailstudio2010.earthframework.utils.EarthUtils;
import com.snailstudio2010.libutils.ArrayUtils;
import com.snailstudio2010.libutils.DisplayUtils;
import com.snailstudio2010.libutils.NotNull;

import java.util.List;

public class GalleryAdapter extends PagerAdapter {

    private Context mContext;
    private ViewPager mVp;
    private List<ArticlePoint> mList;
    private LayoutInflater mInflater;

    private View[] mViews;
    private Vibrator mVibrator;

    private GalleryView.OnGalleryListener mOnGalleryListener;

    public GalleryAdapter(Context context, ViewPager vp, List<ArticlePoint> list) {
        this.mContext = context;
        this.mVp = vp;
        this.mList = list;
        mInflater = LayoutInflater.from(context);
        mViews = new View[list.size()];
        mVibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void setOnItemSelectListener(GalleryView.OnGalleryListener listener) {
        mOnGalleryListener = listener;
    }


    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull @NotNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        ArticlePoint item = mList.get(position);
        View view = mInflater.inflate(R.layout.gallery_item, null);
        View rlContainer = view.findViewById(R.id.rl_container);
        rlContainer.setOnClickListener(v -> {
            if (mOnGalleryListener != null)
                mOnGalleryListener.onGalleryItemClick(position, mList.get(position));
        });
        TextView tvTitle = view.findViewById(R.id.tv_title);
        tvTitle.setText(item.info);
        TextView tvLocation = view.findViewById(R.id.tv_location);
        tvLocation.setText(item.location);
        ImageView ivClose = view.findViewById(R.id.iv_close);
        ivClose.setVisibility(position == mVp.getCurrentItem() ? View.VISIBLE : View.GONE);
        ivClose.setOnClickListener(v -> {
            if (mOnGalleryListener != null) mOnGalleryListener.onGalleryClose();
        });
        ImageView ivAvatar = view.findViewById(R.id.iv_avatar);
        if (!ArrayUtils.isEmpty(mList) && mList.size() > 1) {
            LinearLayout.LayoutParams rll = (LinearLayout.LayoutParams) ivAvatar.getLayoutParams();
            rll.width = LinearLayout.LayoutParams.MATCH_PARENT;
            rll.height = (int) DisplayUtils.dip2px(mContext, 152);
            ivAvatar.setLayoutParams(rll);
        }

        if (!TextUtils.isEmpty(item.photo)) {
            new Thread() {
                public void run() {
                    try {
                        Bitmap bitmap = EarthUtils.getImageLoader(mContext)
                                .getBitmap(item.photo);
                        new Handler(Looper.getMainLooper()).post(
                                () -> ivAvatar.setImageBitmap(bitmap));
                    } catch (Exception e) {
                        e.printStackTrace();
                    } catch (Error e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }

        view.setTag(position);
        container.addView(view);
        mViews[position] = view;
        return view;
    }

    public void onPageSelected(int position) {
        mVibrator.vibrate(50);
        if (mViews == null) return;

        for (int i = 0; i < mViews.length; i++) {
            if (mViews[i] == null) continue;
            ImageView ivClose = mViews[i].findViewById(R.id.iv_close);
            ivClose.setVisibility(i == position ? View.VISIBLE : View.GONE);
        }
    }

    public void notifyDataSetChanged() {
        mViews = new View[mList.size()];
        super.notifyDataSetChanged();
    }
}