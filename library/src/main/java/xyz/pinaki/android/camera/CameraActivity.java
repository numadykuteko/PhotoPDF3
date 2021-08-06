package xyz.pinaki.android.camera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;

import xyz.pinaki.android.camera.dimension.AspectRatio;
import xyz.pinaki.android.camera.dimension.Size;
import xyz.pinaki.androidcamera.R;

public class CameraActivity extends AppCompatActivity {
    private static final String[] REQUIRED_PERMISSIONS;
    public static final String NEW_IMAGE_PATH = "NEW_IMAGE_PATH";

    static {
        REQUIRED_PERMISSIONS = new String[] {
                Manifest.permission.CAMERA
        };
    }

    private static final int REQUEST_CODE_PERMISSIONS = 10;

    private View mCameraHolder;

    private View mPermissionPrompt;

    private View mBackPress;

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

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initView();
    }

    @SuppressLint("MissingPermission")
    public void initView() {
        mCameraHolder = findViewById(R.id.layout_camera);
        mPermissionPrompt = findViewById(R.id.layout_permissions);
        View mPermissionRequest = findViewById(R.id.request_permissions);

        mBackPress = findViewById(R.id.toolbar_btn_back);
        mBackPress.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mPermissionRequest.setOnClickListener(v -> requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS));
        }

        if (PermissionChecker.hasPermissions(this, REQUIRED_PERMISSIONS)) {
            showCamera();
        } else {
            showPermissionPrompt();
        }
    }

    @Override
    public void onBackPressed() {
        if (apiClient != null) {
            if (apiClient.isReviewing()) {
                apiClient.cancelReviewing();
            } else {
                setResult(RESULT_CANCELED);
                finish();
            }
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    public void onDestroy() {
        if (apiClient != null) {
            apiClient.stop();
        }
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void showCamera() {
        mCameraHolder.setVisibility(View.VISIBLE);

        mPermissionPrompt.setVisibility(View.GONE);
        launchCamera();
    }

    private void showPermissionPrompt() {
        mCameraHolder.setVisibility(View.GONE);
        mPermissionPrompt.setVisibility(View.VISIBLE);
    }

    CameraAPIClient apiClient;
    private void launchCamera() {
        AspectRatio aspectRatio = AspectRatio.of(4, 3);
        apiClient = new CameraAPIClient.Builder(this).
                previewType(CameraAPI.PreviewType.TEXTURE_VIEW).
                maxSizeSmallerDimPixels(900).
                desiredAspectRatio(aspectRatio).
                build();
        CameraAPIClient.Callback callback = new CameraAPIClient.Callback() {
            @Override
            public void onCameraOpened() {
            }

            @Override
            public void onAspectRatioAvailable(AspectRatio desired, AspectRatio chosen, List<Size> availableSizes) {
            }

            @Override
            public void onCameraClosed() {
            }

            @Override
            public void onPhotoTaken(byte[] data) {
            }

            @Override
            public void onBitmapProcessed(Bitmap bitmap) {

            }

            @Override
            public void onSubmitBitmap(Bitmap bitmap, int cameraRotation) {
                String imageFileName = System.currentTimeMillis() + "_temp";
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                FileOutputStream fos = null;
                try {
                    File image = File.createTempFile(
                            imageFileName,  /* prefix */
                            ".jpg",         /* suffix */
                            storageDir      /* directory */
                    );
                    fos = new FileOutputStream(image);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

                    Exif exif = new Exif(image);
                    exif.attachTimestamp();
                    exif.rotate(0);

                    exif.save();

                    Intent finishIntent = new Intent();
                    finishIntent.putExtra(NEW_IMAGE_PATH, image.getAbsolutePath());

                    setResult(RESULT_OK, finishIntent);
                    finish();

                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                setResult(RESULT_CANCELED);
                finish();
            }
        };
        apiClient.start(R.id.container, callback);
    }
}
