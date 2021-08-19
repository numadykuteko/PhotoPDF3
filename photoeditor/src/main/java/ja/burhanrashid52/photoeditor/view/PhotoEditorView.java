package ja.burhanrashid52.photoeditor.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import ja.burhanrashid52.photoeditor.R;

/**
 * <p>
 * This ViewGroup will have the {@link DrawingView} to draw paint on it with {@link ImageView}
 * which our source image
 * </p>
 *
 * @author <a href="https://github.com/burhanrashid52">Burhanuddin Rashid</a>
 * @version 0.1.1
 * @since 1/18/2018
 */

public class PhotoEditorView extends RelativeLayout {

    private static final String TAG = "PhotoEditorView";

    private FilterImageView mImgSource;
    private FilterImageView mImgAfterEffect;
    private DrawingView mDrawingView;
    private ImageFilterView mImageFilterView;
    private boolean clipSourceImage;
    private static final int imgSrcId = 1, shapeSrcId = 2, glFilterId = 3;
    private PhotoFilter mCurrentFilter;

    private AttributeSet mAttrs;

    public PhotoEditorView(Context context) {
        super(context);
        init(null);
    }

    public PhotoEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public PhotoEditorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PhotoEditorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        mAttrs = attrs;

        //Setup image attributes
        mImgSource = new FilterImageView(getContext());
        RelativeLayout.LayoutParams sourceParam = setupImageSource(attrs);

        mImgSource.setOnImageChangedListener(new FilterImageView.OnImageChangedListener() {
            @Override
            public void onBitmapLoaded(@Nullable Bitmap sourceBitmap) {
                mImageFilterView.setFilterEffect(PhotoFilter.NONE);
                mImageFilterView.setSourceBitmap(sourceBitmap);
                Log.d(TAG, "onBitmapLoaded() called with: sourceBitmap = [" + sourceBitmap + "]");
            }
        });

        mImgAfterEffect = new FilterImageView(getContext());
        RelativeLayout.LayoutParams afterEffectParam = setupImageSource(attrs);

        //Setup GLSurface attributes
        mImageFilterView = new ImageFilterView(getContext());
        RelativeLayout.LayoutParams filterParam = setupFilterView();

        //Setup drawing view
        mDrawingView = new DrawingView(getContext());
        RelativeLayout.LayoutParams brushParam = setupDrawingView();

        //Add image source
        addView(mImgSource, sourceParam);

        //Add image after effect
        mImgAfterEffect.setVisibility(GONE);
        addView(mImgAfterEffect, afterEffectParam);

        //Add Gl FilterView
        addView(mImageFilterView, filterParam);

        //Add brush view
        addView(mDrawingView, brushParam);
    }


    @SuppressLint("Recycle")
    private RelativeLayout.LayoutParams setupImageSource(@Nullable AttributeSet attrs) {
        mImgSource.setId(imgSrcId);
        mImgSource.setAdjustViewBounds(true);

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PhotoEditorView);
            Drawable imgSrcDrawable = a.getDrawable(R.styleable.PhotoEditorView_photo_src);
            if (imgSrcDrawable != null) {
                mImgSource.setImageDrawable(imgSrcDrawable);
            }
        }

        int widthParam = ViewGroup.LayoutParams.MATCH_PARENT;
        if (clipSourceImage) {
            widthParam = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                widthParam, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        return params;
    }


    private RelativeLayout.LayoutParams setupDrawingView() {
        mDrawingView.setVisibility(GONE);
        mDrawingView.setId(shapeSrcId);

        // Align drawing view to the size of image view
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_TOP, imgSrcId);
        params.addRule(RelativeLayout.ALIGN_BOTTOM, imgSrcId);
        params.addRule(RelativeLayout.ALIGN_LEFT, imgSrcId);
        params.addRule(RelativeLayout.ALIGN_RIGHT, imgSrcId);
        return params;
    }


    private RelativeLayout.LayoutParams setupFilterView() {
        mImageFilterView.setVisibility(GONE);
        mImageFilterView.setId(glFilterId);

        //Align brush to the size of image view
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_TOP, imgSrcId);
        params.addRule(RelativeLayout.ALIGN_BOTTOM, imgSrcId);

        return params;
    }


    /**
     * Source image which you want to edit
     *
     * @return source ImageView
     */
    public ImageView getSource() {
        return mImgSource;
    }

    DrawingView getDrawingView() {
        return mDrawingView;
    }


    void saveFilter(@NonNull final OnSaveBitmap onSaveBitmap) {
        if (mImgAfterEffect.getVisibility() == VISIBLE) {
            onSaveBitmap.onBitmapReady(mImgAfterEffect.getBitmap());
        } else {
            onSaveBitmap.onBitmapReady(mImgSource.getBitmap());
        }
    }

    void setFilterEffect(PhotoFilter filterType) {
        if (mCurrentFilter == filterType) {
            return;
        }

        mImageFilterView.setVisibility(VISIBLE);
        mImageFilterView.setSourceBitmap(mImgSource.getBitmap());
        mImageFilterView.setFilterEffect(filterType);

        mImageFilterView.saveBitmap(new OnSaveBitmap() {
            @Override
            public void onBitmapReady(final Bitmap saveBitmap) {
                mImgAfterEffect.setImageBitmap(saveBitmap);
                mImgAfterEffect.setVisibility(VISIBLE);
                mImageFilterView.setVisibility(GONE);
                mCurrentFilter = filterType;
            }

            @Override
            public void onFailure(Exception e) {
                mImageFilterView.setVisibility(GONE);
            }
        });
    }

    void setFilterEffect(CustomEffect customEffect) {
        mImageFilterView.setVisibility(VISIBLE);
        mImageFilterView.setSourceBitmap(mImgSource.getBitmap());
        mImageFilterView.setFilterEffect(customEffect);
    }

    void setClipSourceImage(boolean clip) {
        clipSourceImage = clip;
        RelativeLayout.LayoutParams param = setupImageSource(null);
        mImgSource.setLayoutParams(param);
    }

    // endregion
}
