/*
 * *
 *  * Created by Ali YÜCE on 3/2/20 11:18 PM
 *  * https://github.com/mayuce/
 *  * Copyright (c) 2020 . All rights reserved.
 *  * Last modified 3/2/20 11:10 PM
 *
 */

package com.pdfconverter.jpg2pdf.pdf.converter.lib;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.ads.control.Admod;
import com.pdfconverter.jpg2pdf.pdf.converter.BuildConfig;
import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.lib.base.CropperErrorType;
import com.pdfconverter.jpg2pdf.pdf.converter.lib.base.DocumentScanActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.lib.helpers.ScannerConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.lib.libraries.PolygonView;

import org.opencv.core.CvException;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ImageScanActivity extends DocumentScanActivity {

    private FrameLayout holderImageCrop;
    private ImageView imageView;
    private PolygonView polygonView;

    private boolean isInverted;
    private boolean isAuto;

    private ProgressBar progressBar;
    private Bitmap cropImage;
    private Bitmap outputImage;

    private String oldImagePath;
    private String newImagePath;
    private int oldImagePosition;

    private OnClickListener btnImageEnhanceClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            showProgressBar();
            disposable.add(
                    Observable.fromCallable(() -> {
                        outputImage = getCroppedImage();
                        newImagePath = saveToInternalStorage(outputImage);

                        return newImagePath != null;
                    })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe((result) -> {
                                hideProgressBar();
                                if (result) {
                                    if (cropImage != null && newImagePath != null && newImagePath.length() > 0) {
                                        Intent finishIntent = new Intent();
                                        finishIntent.putExtra("NEW_IMAGE_PATH", newImagePath);
                                        if (oldImagePosition != -1) {
                                            finishIntent.putExtra("EXTRA_POSITION", oldImagePosition);
                                        }
                                        setResult(Activity.RESULT_OK, finishIntent);
                                    } else {
                                        setResult(Activity.RESULT_CANCELED);
                                    }
                                    finish();
                                } else {
                                    showError(CropperErrorType.CROP_ERROR);
                                }
                            })
            );
        }
    };

    private OnClickListener btnReset = v -> {
        resetBitmap();
        startCropping();
    };

    private OnClickListener btnCloseClick = v -> {
        setResult(Activity.RESULT_CANCELED);
        finish();
    };

    private OnClickListener btnInvertColor = new OnClickListener() {
        @Override
        public void onClick(View v) {
            showProgressBar();
            disposable.add(
                    Observable.fromCallable(() -> {
                        invertColor();
                        return false;
                    })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe((result) -> {
                                hideProgressBar();
                                Bitmap scaledBitmap = scaledBitmap(cropImage, holderImageCrop.getWidth(), holderImageCrop.getHeight());
                                imageView.setImageBitmap(scaledBitmap);
                            })
            );
        }
    };

    private OnClickListener btnAutoColor = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isAuto) return;

            showProgressBar();
            disposable.add(
                    Observable.fromCallable(() -> {
                        autoColor();
                        return false;
                    })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe((result) -> {
                                hideProgressBar();
                                Bitmap scaledBitmap = scaledBitmap(cropImage, holderImageCrop.getWidth(), holderImageCrop.getHeight());
                                imageView.setImageBitmap(scaledBitmap);
                            })
            );
        }
    };

    private OnClickListener onRotateClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            showProgressBar();
            disposable.add(
                    Observable.fromCallable(() -> {
                        cropImage = rotateBitmap(cropImage, 90);
                        return false;
                    })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe((result) -> {
                                hideProgressBar();
                                startCropping();
                            })
            );
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_scan);

        Intent intent = getIntent();
        oldImagePath = intent.getStringExtra("EXTRA_FILE_PATH");
        oldImagePosition = intent.getIntExtra("EXTRA_POSITION", -1);

        if (oldImagePath != null) {
            resetBitmap();
        }

        isInverted = false;
        isAuto = false;

        if (cropImage != null)
            initView();
        else {
            Toast.makeText(this, ScannerConstants.imageError, Toast.LENGTH_LONG).show();
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
    }

    @Override
    protected FrameLayout getHolderImageCrop() {
        return holderImageCrop;
    }

    @Override
    protected ImageView getImageView() {
        return imageView;
    }

    @Override
    protected PolygonView getPolygonView() {
        return polygonView;
    }

    @Override
    protected void showProgressBar() {
        ConstraintLayout rlContainer = findViewById(R.id.rlContainer);
        setViewInteract(rlContainer, false);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void hideProgressBar() {
        ConstraintLayout rlContainer = findViewById(R.id.rlContainer);
        setViewInteract(rlContainer, true);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void showError(CropperErrorType errorType) {
        switch (errorType) {
            case CROP_ERROR:
                Toast.makeText(this, ScannerConstants.cropError, Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    protected Bitmap getBitmapImage() {
        return cropImage;
    }

    private void resetBitmap() {
        try {

            Bitmap bitmap = null;

            try {
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bitmap = BitmapFactory.decodeFile(oldImagePath, bmOptions);

            } catch (Exception e) {
                // do nothing
            }

            if (bitmap == null) {
                bitmap = MediaStore.Images.Media.getBitmap(
                        getContentResolver(),
                        Uri.parse(oldImagePath)
                );
            }

            if (bitmap != null) {
                ExifInterface exif = null;
                try {
                    exif = new ExifInterface(oldImagePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (exif != null) {
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                    if (orientation != ExifInterface.ORIENTATION_UNDEFINED) {
                        cropImage = rotateBitmapByExif(bitmap, orientation);
                    } else {
                        cropImage = bitmap;
                    }
                } else {
                    cropImage = bitmap;
                }
            }

            isAuto = false;
            isInverted = false;
        } catch (Exception | OutOfMemoryError e) {
            // do nothing
        }

    }

    public static Bitmap rotateBitmapByExif(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        }
        catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setViewInteract(View view, boolean canDo) {
        view.setEnabled(canDo);
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                setViewInteract(((ViewGroup) view).getChildAt(i), canDo);
            }
        }
    }

    private void initView() {
        Admod.getInstance().loadBanner(this, BuildConfig.banner_id);

        Button btnImageCrop = findViewById(R.id.btnImageCrop);
        ImageView btnClose = findViewById(R.id.btnClose);
        holderImageCrop = findViewById(R.id.holderImageCrop);
        imageView = findViewById(R.id.imageView);
        ImageView ivRotate = findViewById(R.id.ivRotate);
        ImageView ivInvert = findViewById(R.id.ivInvert);
        ImageView ivAuto = findViewById(R.id.ivAuto);
        TextView resetBtn = findViewById(R.id.toolbar_action_text);
        btnImageCrop.setText(getString(R.string.crop_done));
        polygonView = findViewById(R.id.polygonView);
        progressBar = findViewById(R.id.progressBar);
        if (progressBar.getIndeterminateDrawable() != null && ScannerConstants.progressColor != null)
            progressBar.getIndeterminateDrawable().setColorFilter(Color.parseColor(ScannerConstants.progressColor), android.graphics.PorterDuff.Mode.MULTIPLY);
        else if (progressBar.getProgressDrawable() != null && ScannerConstants.progressColor != null)
            progressBar.getProgressDrawable().setColorFilter(Color.parseColor(ScannerConstants.progressColor), android.graphics.PorterDuff.Mode.MULTIPLY);
        btnImageCrop.setOnClickListener(btnImageEnhanceClick);
        btnClose.setOnClickListener(btnCloseClick);
        ivRotate.setOnClickListener(onRotateClick);
        ivInvert.setOnClickListener(btnInvertColor);
        ivAuto.setOnClickListener(btnAutoColor);
        resetBtn.setOnClickListener(btnReset);
        startCropping();
    }

    private void invertColor() {
        if (!isInverted) {
            try {
                Bitmap bmpMonochrome = Bitmap.createBitmap(cropImage.getWidth(), cropImage.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bmpMonochrome);
                ColorMatrix ma = new ColorMatrix();
                ma.setSaturation(0);
                Paint paint = new Paint();
                paint.setColorFilter(new ColorMatrixColorFilter(ma));
                canvas.drawBitmap(cropImage, 0, 0, paint);
                cropImage = bmpMonochrome.copy(bmpMonochrome.getConfig(), true);

                isInverted = true;
                isAuto = false;
            } catch (Exception | OutOfMemoryError ignored) {
            }
        } else {
            resetBitmap();
        }
    }

    private Mat bitmapToMat(Bitmap bitmap) {
        System.gc();
        Mat image = new Mat();
        Bitmap bmp32 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        org.opencv.android.Utils.bitmapToMat(bmp32, image);
        return image;
    }

    private Bitmap matToBitmap(Mat mat) {
        try {
            Bitmap bmp = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
            org.opencv.android.Utils.matToBitmap(mat, bmp);
            return bmp;
        } catch (CvException e) {
            throw new RuntimeException("Not able to convert mat to bitmap", e);
        }
    }

    private void autoColor() {
        if (isAuto)
            return;

        if (isInverted) {
            resetBitmap();
        }

        try {
            Mat mbgra = bitmapToMat(cropImage);
            Mat dst = mbgra.clone();
            // init our output image
            float alpha = 1.9f;
            float beta = -80;
            dst.convertTo(dst, -1, alpha, beta);

            cropImage = matToBitmap(dst);
            isAuto = true;
        } catch (Exception | OutOfMemoryError ignored) {
        }
    }

    private String saveToInternalStorage(Bitmap bitmapImage) {
        if (bitmapImage == null) return null;

        FileOutputStream fos = null;
        File myPath;

        try {
            File directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            String timeStamp = System.currentTimeMillis() + "";
            String imageFileName = "cropped_" + timeStamp + ".jpg";
            myPath = new File(directory, imageFileName);

            fos = new FileOutputStream(myPath);
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 90, fos);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return myPath.getAbsolutePath();
    }
}
