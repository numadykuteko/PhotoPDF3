package xyz.pinaki.android.camera;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import xyz.pinaki.android.camera.dimension.AspectRatio;
import xyz.pinaki.android.camera.dimension.Size;

/**
 * Created by pinaki on 10/8/17.
 */

public final class CameraAPIClient {
    private static String TAG = CameraAPIClient.class.getSimpleName();
    private final AppCompatActivity activity;
    private final CameraAPI.PreviewType previewType;
    private final AspectRatio desiredAspectRatio;
    private CameraFragment cameraView;
    private final Integer maxSizeSmallerDim;
    private boolean shouldFixOrientation = true;

    private CameraAPIClient(Builder builder) {
        activity = builder.activity;
        previewType = builder.previewType;
        desiredAspectRatio = builder.desiredAspectRatio;
        maxSizeSmallerDim = builder.maxSizeSmallerDim;
    }

    public interface Callback {
        void onCameraOpened();
        void onAspectRatioAvailable(AspectRatio desired, AspectRatio chosen, List<Size> availablePreviewSizes);
        void onCameraClosed();
        void onPhotoTaken(byte[] data);
        void onBitmapProcessed(Bitmap bitmap);
        void onSubmitBitmap(Bitmap bitmap, int cameraRotation);
    }

    // interface that might be useful when we have an API to trigger image capture.
    public interface PhotoTakenCallback {
        void onPhotoTaken(byte[] data);
        void onBitmapProcessed(Bitmap bitmap);
    }

    public void start(int containerID, Callback callback) {
        if (shouldFixOrientation) {
            fixOrientation(activity);
        }

        CameraPresenter cameraPresenter;
        if (!isCamera2Supported(activity)) {
            // use dep camera api
            cameraPresenter = new Camera1Presenter(activity);
        } else {
            // use camera 2 api
            cameraPresenter = new Camera2Presenter(activity);
        }
        cameraPresenter.setDesiredAspectRatio(desiredAspectRatio);
        if (maxSizeSmallerDim != null) {
            cameraPresenter.setMaxWidthSizePixels(maxSizeSmallerDim);
        }

        cameraView = CameraFragment.newInstance(cameraPresenter);

        cameraView.setPreviewType(previewType);
        cameraView.setCallback(callback);
        activity.getSupportFragmentManager().beginTransaction().
                replace(containerID, cameraView, "CameraFragment").addToBackStack("CameraFragment").commit();
    }

    public boolean isReviewing() {
        return (cameraView != null && cameraView.isReviewing());
    }

    public void cancelReviewing() {
        cameraView.cancelReviewing();
    }

    public void stop() {
        cameraView.setCallback(null);
        cameraView = null;
    }

    private static void fixOrientation(AppCompatActivity activity) {
        int orientation = activity.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        else {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    private static boolean isCamera2Supported(Context context) {
        CameraManager cameraManager = (CameraManager)context.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (cameraManager.getCameraIdList().length == 0) {
                return false;
            }
            for (String cameraIdStr : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraIdStr);
                Integer hardwareSupport = cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                if ( hardwareSupport == null ) {
                    return false;
                }
                if (hardwareSupport == CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY ) {
                    return false;
                }
                if (hardwareSupport == CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED) {
                    return false;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static class Builder {
        private final AppCompatActivity activity;
        private CameraAPI.PreviewType previewType = CameraAPI.PreviewType.SURFACE_VIEW;
        private AspectRatio desiredAspectRatio = CameraAPI.DEFAULT_ASPECT_RATIO;
        private Integer maxSizeSmallerDim = CameraAPI.DEFAULT_MAX_IMAGE_WIDTH;

        public Builder(AppCompatActivity a) {
            activity = a;
        }
        public Builder previewType(CameraAPI.PreviewType p) {
            previewType = p;
            return this;
        }
        public Builder desiredAspectRatio(AspectRatio a) {
            desiredAspectRatio = a;
            return this;
        }
        public Builder maxSizeSmallerDimPixels(int s) {
            maxSizeSmallerDim = s;
            return this;
        }
        public CameraAPIClient build() {
            return new CameraAPIClient(this);
        }
    }

}
