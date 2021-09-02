package xyz.pinaki.android.camera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.util.List;

import xyz.pinaki.android.camera.dimension.AspectRatio;
import xyz.pinaki.android.camera.dimension.Size;
import xyz.pinaki.android.camera.orientation.DisplayOrientationDetector;
import xyz.pinaki.android.camera.preview.SurfaceViewPreview;
import xyz.pinaki.android.camera.preview.TextureViewPreview;
import xyz.pinaki.android.camera.preview.ViewFinderPreview;
import xyz.pinaki.androidcamera.R;

/**
 * Created by pinaki on 8/11/17.
 */

public class CameraFragment extends Fragment implements CameraView {
    private static final String TAG = CameraFragment.class.getName();
    private static final int REQUEST_CAMERA_PERMISSION = 0;
    protected static final int PICK_IMAGE_REQUEST = 2367;

    private CameraPresenter cameraPresenter;
    private ViewFinderPreview viewFinderPreview;
    private CameraAPI.PreviewType previewType;
    private View parentView;
    private AdjustableLayout autoFitCameraView;
    private DisplayOrientationDetector displayOrientationDetector;

    private boolean isReviewing = false;

    ViewGroup previewContainer;
    ImageView previewImage;
    ImageView flashIcon;

    private Context context;
    private int mCurrentFlash = FLASH_OFF;
    private static final int FLASH_ON = 1;
    private static final int FLASH_OFF = 2;
    private static final int FLASH_AUTO = 0;
    private static final int[] FLASH_ICON = {R.drawable.ic_auto_flash, R.drawable.ic_flash_on, R.drawable.ic_flash_off};

    private CameraAPIClient.Callback apiCallback;
    private CameraStatusCallback cameraStatusCallback = new CameraStatusCallback() {
        @Override
        public void onCameraOpen() {
            if (autoFitCameraView != null) {
                autoFitCameraView.setPreview(viewFinderPreview);

                if (cameraPresenter != null) {
                    autoFitCameraView.setAspectRatio(cameraPresenter.getAspectRatio());
                }
                autoFitCameraView.requestLayout();
            }

            if (apiCallback != null) {
                apiCallback.onCameraOpened();
            }
        }

        @Override
        public void onPhotoTaken(byte[] data) {
            if (apiCallback != null) {
                apiCallback.onPhotoTaken(data);
            }
        }

        @Override
        public void onBitmapProcessed(Bitmap bitmap, int cameraRotation) {
            bitmapProcessed(bitmap, cameraRotation);
        }

        @Override
        public void onCameraClosed() {
            if (apiCallback != null) {
                apiCallback.onCameraClosed();
            }
        }

        @Override
        public void onAspectRatioAvailable(AspectRatio desired, AspectRatio chosen, List<Size> availablePreviewSizes) {
            if (apiCallback != null) {
                apiCallback.onAspectRatioAvailable(desired, chosen, availablePreviewSizes);
            }
        }
    };

    public CameraFragment() {
        // doesn't do anything special
    }

    public static CameraFragment newInstance(CameraPresenter cameraPresenter) {
        CameraFragment f = new CameraFragment();
        f.setCameraPresenter(cameraPresenter);
        return f;
    }

    public void setCameraPresenter(CameraPresenter cameraPresenter) {
        this.cameraPresenter = cameraPresenter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.camera_view_main, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View pView, Bundle savedInstanceState) {
        parentView = pView;
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
        } else { // permissions have already been granted
            startPreviewAndCamera();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    private void startPreviewAndCamera() {
        View shutterIcon = parentView.findViewById(R.id.capture);
        shutterIcon.setOnClickListener(view -> shutterClicked());

        flashIcon = parentView.findViewById(R.id.flash);
        flashIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentFlash == FLASH_AUTO) {
                    mCurrentFlash = FLASH_OFF;
                } else if (mCurrentFlash == FLASH_OFF) {
                    mCurrentFlash = FLASH_ON;
                } else if (mCurrentFlash == FLASH_ON) {
                    mCurrentFlash = FLASH_AUTO;
                }
                CameraFragment.this.updateFlashLight();
            }
        });

        previewContainer = (ViewGroup) parentView.findViewById(R.id.preview_container);
        previewImage = (ImageView) parentView.findViewById(R.id.preview_image);
        final LinearLayout previewCloseButton = parentView.findViewById(R.id.cancel);
        previewCloseButton.setOnClickListener(view -> {
            previewContainer.setVisibility(View.INVISIBLE);
            isReviewing = false;
        });

        autoFitCameraView = (AdjustableLayout) parentView.findViewById(R.id.camera_adjust);
        ViewFinderPreview.Callback viewFinderCallback = new ViewFinderPreview.Callback() {
            @Override
            public void onSurfaceChanged() {
                if (cameraPresenter != null) {
                    cameraPresenter.setPreview(viewFinderPreview);
                    cameraPresenter.onStart(); // starts the camera
                }

            }

            @Override
            public void onSurfaceDestroyed() {
                if (cameraPresenter != null) {
                    cameraPresenter.onStop();
                }
            }

            @Override
            public void onSurfaceCreated() {
            }
        };
        if(previewType == CameraAPI.PreviewType.TEXTURE_VIEW) {
            viewFinderPreview = new TextureViewPreview(getContext(), autoFitCameraView, viewFinderCallback);
        } else {
            viewFinderPreview = new SurfaceViewPreview(getContext(), autoFitCameraView, viewFinderCallback);
        }
        viewFinderPreview.start();

        if (displayOrientationDetector == null) {
            // the constructor has to be within one of the lifecycle event to make sure the context is not null;
            displayOrientationDetector = new DisplayOrientationDetector(getContext()) {
                @Override
                public void onDisplayOrientationChanged(int displayOrientation) {
                    // update listeners
                    if (cameraPresenter != null) {
                        cameraPresenter.setDisplayOrientation(displayOrientation);
                        autoFitCameraView.setDisplayOrientation(displayOrientation);
                    }

                }
            };
        }
        if (context != null && context instanceof Activity) {
            displayOrientationDetector.enable(((Activity) context).getWindowManager().getDefaultDisplay());
        }

        ImageView getPicture = parentView.findViewById(R.id.get_image);
        getPicture.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        try {
            if (requestCode == PICK_IMAGE_REQUEST && data != null) {
                if (data.getData() != null) {
                    Uri imageUri = data.getData();
                    String imagePath = RealPathUtil.getInstance().getRealPath(getContext(), imageUri);
                    if (imagePath != null) {
                        File file = new File(imagePath);
                        if (file.exists()) {
                            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                            Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bmOptions);

                            bitmapProcessed(bitmap, 0);
                        }
                    }

                }
            }
            super.onActivityResult(requestCode, resultCode, data);
        } catch (Exception e) {
            // donothing
        }

    }

    private void bitmapProcessed(Bitmap bitmap, int cameraRotation) {
        if (bitmap != null) {
            previewContainer.setVisibility(View.VISIBLE);
            previewImage.setImageBitmap(bitmap);

            if (apiCallback != null) {
                apiCallback.onBitmapProcessed(bitmap);
            }

            final LinearLayout previewConfirmButton = parentView.findViewById(R.id.confirm);
            previewConfirmButton.setOnClickListener(view -> apiCallback.onSubmitBitmap(bitmap, cameraRotation));

            isReviewing = true;
        } else {
            Toast.makeText(context, "Sorry. Can not take photo now.", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isReviewing() {
        return isReviewing;
    }

    public void cancelReviewing() {
        previewContainer.setVisibility(View.INVISIBLE);
        isReviewing = false;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void updateFlashLight() {
        if (context != null) {
            flashIcon.setImageDrawable(context.getDrawable(FLASH_ICON[mCurrentFlash]));
        }

        if (cameraPresenter != null) {
            cameraPresenter.setFlashType(mCurrentFlash);
        }
    }

    @Override
    public void onDestroyView() {
        if (displayOrientationDetector != null) {
            displayOrientationDetector.disable();
        }
        if (cameraPresenter != null) {
            cameraPresenter.onStop();
        }
        super.onDestroyView();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (cameraPresenter != null) {
            cameraPresenter.setCameraStatusCallback(cameraStatusCallback);
            cameraPresenter.onCreate();
        }
    }

    private void requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            // TODO: Add a DialogFragment to Show the details.
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                 startPreviewAndCamera();
            } else {
                Log.i(TAG, "CAMERA permission was NOT granted.");
            }
        }
    }

    @Override
    public void onDestroy() {
        if (cameraPresenter != null) {
            cameraPresenter.onDestroy();
        }

        if (cameraStatusCallback != null) {
            cameraStatusCallback.onCameraClosed();
        }
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() instanceof AppCompatActivity &&
                ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        } else if ( getActivity() !=  null && getActivity().getActionBar() != null) {
            getActivity().getActionBar().hide();
        }
        // TODO: enable orientation listener: cameraPresenter helps somehow
//        orientationListener = new DeviceOrientationListener(getActivity());
    }

    @Override
    public void setPresenter(@NonNull CameraPresenter c) {
        cameraPresenter = c;
    }

    @Override
    public void setPreviewType(CameraAPI.PreviewType p) {
        previewType = p;
    }

    public void setCallback(CameraAPIClient.Callback c) {
        apiCallback = c;
    }

    @Override
    public void shutterClicked() {
        if (cameraPresenter != null) {
            cameraPresenter.takePicture();
        }
    }
}
