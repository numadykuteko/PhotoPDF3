package com.xlythe.view.camera;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;

public class CameraActivity extends AppCompatActivity implements CameraView.OnImageCapturedListener, CameraView.OnVideoCapturedListener {
    private static final String[] REQUIRED_PERMISSIONS;
    private static final boolean IS_SUPPORT_TAKE_VIDEO = false;
    public static final String NEW_IMAGE_PATH = "NEW_IMAGE_PATH";

    private static final int FLASH_ON = 1;
    private static final int FLASH_OFF = 2;
    private static final int FLASH_AUTO = 0;
    private static final int[] FLASH_ICON = {R.drawable.ic_auto_flash, R.drawable.ic_flash_on, R.drawable.ic_flash_off};

    static {
        REQUIRED_PERMISSIONS = new String[] {
                Manifest.permission.CAMERA
        };
    }

    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String PHOTO_EXT = ".jpg";
    private static final String VIDEO_EXT = ".mp4";

    private View mCameraHolder;

    private View mPermissionPrompt;

    private CameraView mCamera;

    private View mCapture;

    private View mConfirm;

    private View mCancel;

    private View mBackPress;

    private ImageView mFlashLight;

    private final ProgressBarAnimator mAnimator = new ProgressBarAnimator();

    private DisplayManager.DisplayListener mDisplayListener;

    private int mCurrentFlash = FLASH_OFF;

    @SuppressWarnings({"MissingPermission"})
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (PermissionChecker.hasPermissions(this, REQUIRED_PERMISSIONS)) {
                showCamera();
            } else {
                showPermissionPrompt();
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);
        initView();
    }

    @SuppressLint("MissingPermission")
    public void initView() {
        mCameraHolder = findViewById(R.id.layout_camera);
        mPermissionPrompt = findViewById(R.id.layout_permissions);
        View mPermissionRequest = findViewById(R.id.request_permissions);
        mCamera = findViewById(R.id.camera);
        mCapture = mCameraHolder.findViewById(R.id.capture);
        mCancel = findViewById(R.id.cancel);
        mConfirm = findViewById(R.id.confirm);
        mBackPress = findViewById(R.id.toolbar_btn_back);
        mFlashLight = findViewById(R.id.toolbar_action_flash);

        mBackPress.setOnClickListener(v -> super.onBackPressed());

        mCamera.setOnImageCapturedListener(this);
        mCamera.setOnVideoCapturedListener(this);

        if (IS_SUPPORT_TAKE_VIDEO) {
            mCapture.setOnTouchListener(new OnTouchListener(this));
        } else {
            mCapture.setOnClickListener(v -> {
                mCamera.takePicture(new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), System.currentTimeMillis() + PHOTO_EXT));
            });
        }

        mConfirm.setOnClickListener(v -> mCamera.confirmPicture());
        mCamera.setImageConfirmationEnabled(true);
        mCamera.setVideoConfirmationEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mPermissionRequest.setOnClickListener(v -> requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS));
        }

        mConfirm.setVisibility(View.GONE);
        mCancel.setVisibility(View.GONE);

        mDisplayListener = new DisplayManager.DisplayListener() {
            @Override
            public void onDisplayAdded(int displayId) {}

            @Override
            public void onDisplayRemoved(int displayId) {}

            @SuppressWarnings({"MissingPermission"})
            @Override
            public void onDisplayChanged(int displayId) {
                if (PermissionChecker.hasPermissions(CameraActivity.this, REQUIRED_PERMISSIONS)) {
                    if (mCamera.isOpen()) {
                        mCamera.close();
                        mCamera.open();
                    }
                }
            }
        };
        DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        if (displayManager != null) {
            displayManager.registerDisplayListener(mDisplayListener, new Handler());
        }

        if (PermissionChecker.hasPermissions(this, REQUIRED_PERMISSIONS)) {
            showCamera();
        } else {
            showPermissionPrompt();
        }
    }

    @Override
    public void onDestroy() {
        DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        if (displayManager != null) {
            displayManager.unregisterDisplayListener(mDisplayListener);
        }

        super.onDestroy();
    }

    @Override
    public void onStop() {
        if (mCamera != null) {
            mCamera.close();
        }
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (PermissionChecker.hasPermissions(this, REQUIRED_PERMISSIONS)) {
            if (mCapture.getVisibility() == View.VISIBLE) {
                super.onBackPressed();
            } else {
                mCamera.rejectPicture();

                // After rejecting, show our buttons again
                mConfirm.setVisibility(View.GONE);
                mCancel.setVisibility(View.GONE);
                mCapture.setVisibility(View.VISIBLE);
            }
        } else {
            onBackPressed();
        }
    }

    public void setEnabled(boolean enabled) {
        mCapture.setEnabled(enabled);
    }

    public boolean isEnabled() {
        return mCapture.isEnabled();
    }

    public void setQuality(CameraView.Quality quality) {
        mCamera.setQuality(quality);
    }

    public CameraView.Quality getQuality() {
        return mCamera.getQuality();
    }

    public void setMaxVideoDuration(long duration) {
        mCamera.setMaxVideoDuration(duration);
    }

    public long getMaxVideoDuration() {
        return mCamera.getMaxVideoDuration();
    }

    public void setMaxVideoSize(long size) {
        mCamera.setMaxVideoSize(size);
    }

    public long getMaxVideoSize() {
        return mCamera.getMaxVideoSize();
    }

    @RequiresPermission(allOf = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    })
    private void showCamera() {
        mCameraHolder.setVisibility(View.VISIBLE);
        mCapture.setVisibility(View.VISIBLE);
        mCancel.setVisibility(View.GONE);
        mConfirm.setVisibility(View.GONE);

        mPermissionPrompt.setVisibility(View.GONE);
        mFlashLight.setVisibility(View.VISIBLE);
        updateFlashLight();
        mFlashLight.setOnClickListener(v -> {
            if (mCurrentFlash == FLASH_AUTO) {
                mCurrentFlash = FLASH_OFF;
            } else if (mCurrentFlash == FLASH_OFF) {
                mCurrentFlash = FLASH_ON;
            } else if (mCurrentFlash == FLASH_ON) {
                mCurrentFlash = FLASH_AUTO;
            }
            updateFlashLight();
        });
        mCamera.open();
    }

    private void showPermissionPrompt() {
        mCameraHolder.setVisibility(View.GONE);
        mPermissionPrompt.setVisibility(View.VISIBLE);
        mFlashLight.setVisibility(View.GONE);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void updateFlashLight() {
        if (mCamera != null) {
            mCamera.setFlash(CameraView.Flash.fromId(mCurrentFlash));
            mFlashLight.setImageDrawable(getDrawable(FLASH_ICON[mCurrentFlash]));
        }
    }

    @Override
    public void onFailure() {
        runOnUiThread(() -> {
            Toast.makeText(this, "Sorry. We can not take photo. Please try again.", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onImageConfirmation() {
        View.OnClickListener listener = view -> {
            if (view == mCancel) {
                mCamera.rejectPicture();

                // After confirming/rejecting, show our buttons again
                mConfirm.setVisibility(View.GONE);
                mCancel.setVisibility(View.GONE);
                mCapture.setVisibility(View.VISIBLE);
            } else {
                mCamera.confirmPicture();
            }
        };

        mCancel.setVisibility(View.VISIBLE);
        mCancel.setOnClickListener(listener);

        mCapture.setVisibility(View.GONE);
        mConfirm.setVisibility(View.VISIBLE);
        mConfirm.setOnClickListener(listener);
    }

    @Override
    public void onImageCaptured(File file) {
        Intent finishIntent = new Intent();
        finishIntent.putExtra(NEW_IMAGE_PATH, file.getAbsolutePath());

        setResult(RESULT_OK, finishIntent);
        finish();
    }

    @Override
    public void onVideoConfirmation() {
        View.OnClickListener listener = v -> {
            if (v == mCancel) {
                mCamera.rejectVideo();
            } else {
                mCamera.confirmVideo();
            }

            // After confirming/rejecting, show our buttons again
            mConfirm.setVisibility(View.GONE);
            mCapture.setVisibility(View.VISIBLE);
        };
        mCancel.setOnClickListener(listener);
        mCapture.setVisibility(View.GONE);
        mConfirm.setVisibility(View.VISIBLE);
        mConfirm.setOnClickListener(listener);
    }

    @Override
    public void onVideoCaptured(File file) {

    }

    private class ProgressBarAnimator extends ValueAnimator {
        private ProgressBarAnimator() {
            setInterpolator(new LinearInterpolator());
            setFloatValues(0f, 1f);
            addUpdateListener(animation -> {
                float percent = (float) animation.getAnimatedValue();
                onUpdate(percent);
            });
        }

        void onUpdate(float percent) {
        }
    }

    @SuppressLint("HandlerLeak")
    private class OnTouchListener implements View.OnTouchListener {
        private final int TAP = 1;
        private final int HOLD = 2;
        private final int RELEASE = 3;

        private final long LONG_PRESS = ViewConfiguration.getLongPressTimeout();

        private final Context mContext;

        private long mDownEventTimestamp;
        private Rect mViewBoundsRect;
        private final Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case TAP:
                        onTap();
                        break;
                    case HOLD:
                        onHold();
                        if (mCamera.getMaxVideoDuration() > 0) {
                            sendEmptyMessageDelayed(RELEASE, mCamera.getMaxVideoDuration());
                        }
                        break;
                    case RELEASE:
                        onRelease();
                        break;
                }
            }
        };

        OnTouchListener(Context context) {
            mContext = context;
        }

        Context getContext() {
            return mContext;
        }

        void onTap() {
            mCamera.takePicture(new File(getContext().getCacheDir(), System.currentTimeMillis() + PHOTO_EXT));
        }

        void onHold() {
            vibrate();
            mCamera.startRecording(new File(getContext().getCacheDir(), System.currentTimeMillis() + VIDEO_EXT));
            if (mCamera.isRecording()) {
                if (mCamera.getMaxVideoDuration() > 0) {
                    mAnimator.setDuration(mCamera.getMaxVideoDuration()).start();
                }
            }
        }

        void onRelease() {
            mCamera.stopRecording();
            mAnimator.cancel();
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mDownEventTimestamp = System.currentTimeMillis();
                    mViewBoundsRect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                    mHandler.sendEmptyMessageDelayed(HOLD, LONG_PRESS);
                    v.setPressed(true);
                    break;
                case MotionEvent.ACTION_MOVE:
                    // If the user moves their finger off the button, trigger RELEASE
                    if (mViewBoundsRect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {
                        break;
                    }
                    // Fall-through
                case MotionEvent.ACTION_CANCEL:
                    clearHandler();
                    if (delta() > LONG_PRESS && (mCamera.getMaxVideoDuration() <= 0 || delta() < mCamera.getMaxVideoDuration())) {
                        mHandler.sendEmptyMessage(RELEASE);
                    }
                    v.setPressed(false);
                    break;
                case MotionEvent.ACTION_UP:
                    clearHandler();
                    if (delta() < LONG_PRESS) {
                        mHandler.sendEmptyMessage(TAP);
                    } else if ((mCamera.getMaxVideoDuration() <= 0 || delta() < mCamera.getMaxVideoDuration())) {
                        mHandler.sendEmptyMessage(RELEASE);
                    }
                    v.setPressed(false);
                    break;
            }
            return true;
        }

        private long delta() {
            return System.currentTimeMillis() - mDownEventTimestamp;
        }

        @SuppressWarnings({"MissingPermission"})
        private void vibrate() {
            Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && PermissionChecker.hasPermissions(getContext(), Manifest.permission.VIBRATE) && vibrator.hasVibrator()) {
                vibrator.vibrate(25);
            }
        }

        private void clearHandler() {
            mHandler.removeMessages(TAP);
            mHandler.removeMessages(HOLD);
            mHandler.removeMessages(RELEASE);
        }
    }

    private static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
