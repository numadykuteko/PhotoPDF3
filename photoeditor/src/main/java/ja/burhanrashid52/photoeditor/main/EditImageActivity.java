package ja.burhanrashid52.photoeditor.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.ChangeBounds;
import androidx.transition.TransitionManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.File;

import ja.burhanrashid52.photoeditor.R;
import ja.burhanrashid52.photoeditor.main.base.BaseActivity;
import ja.burhanrashid52.photoeditor.main.filters.FilterListener;
import ja.burhanrashid52.photoeditor.main.filters.FilterViewAdapter;
import ja.burhanrashid52.photoeditor.main.tools.EditingToolsAdapter;
import ja.burhanrashid52.photoeditor.main.tools.ToolType;
import ja.burhanrashid52.photoeditor.shape.ShapeBuilder;
import ja.burhanrashid52.photoeditor.shape.ShapeType;
import ja.burhanrashid52.photoeditor.signature.activity.SignatureActivity;
import ja.burhanrashid52.photoeditor.view.OnPhotoEditorListener;
import ja.burhanrashid52.photoeditor.view.PhotoEditor;
import ja.burhanrashid52.photoeditor.view.PhotoEditorView;
import ja.burhanrashid52.photoeditor.view.PhotoFilter;
import ja.burhanrashid52.photoeditor.view.SaveSettings;
import ja.burhanrashid52.photoeditor.view.TextStyleBuilder;
import ja.burhanrashid52.photoeditor.view.ViewType;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static ja.burhanrashid52.photoeditor.main.FileSaveHelper.isSdkHigherThan28;

public class EditImageActivity extends BaseActivity implements OnPhotoEditorListener,
        View.OnClickListener,
        PropertiesBSFragment.Properties,
        ShapeBSFragment.Properties,
        EmojiBSFragment.EmojiListener,
        StickerBSFragment.StickerListener, EditingToolsAdapter.OnItemSelected, FilterListener {

    private static final String TAG = EditImageActivity.class.getSimpleName();
    public static final String PINCH_TEXT_SCALABLE_INTENT_KEY = "PINCH_TEXT_SCALABLE";

    PhotoEditor mPhotoEditor;
    private PhotoEditorView mPhotoEditorView;
    private PropertiesBSFragment mPropertiesBSFragment;
    private ShapeBSFragment mShapeBSFragment;
    private ShapeBuilder mShapeBuilder;
    private EmojiBSFragment mEmojiBSFragment;
    private StickerBSFragment mStickerBSFragment;
    private TextView mTxtCurrentTool;
    private ImageView mCancelButton;
    private RecyclerView mRvTools, mRvFilters;
    private final EditingToolsAdapter mEditingToolsAdapter = new EditingToolsAdapter(this);
    private final FilterViewAdapter mFilterViewAdapter = new FilterViewAdapter(this);
    private ConstraintLayout mRootView;
    private final ConstraintSet mConstraintSet = new ConstraintSet();
    private boolean mIsFilterVisible;
    private boolean mIsEraser = false;
    private boolean mIsShape = false;

    private String oldImagePath;
    private String newImagePath;
    private int oldImagePosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        makeFullScreen();
        setContentView(R.layout.activity_edit_image);

        Intent intent = getIntent();
        oldImagePath = intent.getStringExtra("EXTRA_FILE_PATH");
        oldImagePosition = intent.getIntExtra("EXTRA_POSITION", -1);

        if (oldImagePath == null) {
            Toast.makeText(this, "Sorry. Your image is not valid", Toast.LENGTH_LONG).show();
            setResult(Activity.RESULT_CANCELED);
            finish();
            return;
        }

        initViews();

        mPropertiesBSFragment = new PropertiesBSFragment();
        mEmojiBSFragment = new EmojiBSFragment();
        mStickerBSFragment = new StickerBSFragment();
        mShapeBSFragment = new ShapeBSFragment();
        mStickerBSFragment.setStickerListener(this);
        mEmojiBSFragment.setEmojiListener(this);
        mPropertiesBSFragment.setPropertiesChangeListener(this);
        mShapeBSFragment.setPropertiesChangeListener(this);

        LinearLayoutManager llmTools = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRvTools.setLayoutManager(llmTools);
        mRvTools.setAdapter(mEditingToolsAdapter);

        LinearLayoutManager llmFilters = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRvFilters.setLayoutManager(llmFilters);
        mRvFilters.setAdapter(mFilterViewAdapter);

        // NOTE(lucianocheng): Used to set integration testing parameters to PhotoEditor
        boolean pinchTextScalable = getIntent().getBooleanExtra(PINCH_TEXT_SCALABLE_INTENT_KEY, true);

        //Typeface mTextRobotoTf = ResourcesCompat.getFont(this, R.font.roboto_medium);
        //Typeface mEmojiTypeFace = Typeface.createFromAsset(getAssets(), "emojione-android.ttf");

        mPhotoEditor = new PhotoEditor.Builder(this, mPhotoEditorView)
                .setPinchTextScalable(pinchTextScalable) // set flag to make text scalable when pinch
                //.setDefaultTextTypeface(mTextRobotoTf)
                //.setDefaultEmojiTypeface(mEmojiTypeFace)
                .build(); // build photo editor sdk

        mPhotoEditor.setOnPhotoEditorListener(this);

        mShapeBuilder = new ShapeBuilder();
        mPhotoEditor.setShape(mShapeBuilder);

        mShapeBSFragment.setShapeBuilder(mShapeBuilder);

        mPhotoEditorView.getSource().setImageURI(Uri.fromFile(new File(oldImagePath)));
    }

    private void initViews() {
        ImageView imgUndo;
        ImageView imgRedo;
        ImageView imgSave;

        mPhotoEditorView = findViewById(R.id.photoEditorView);
        mTxtCurrentTool = findViewById(R.id.txtCurrentTool);
        mRvTools = findViewById(R.id.rvConstraintTools);
        mRvFilters = findViewById(R.id.rvFilterView);
        mRootView = findViewById(R.id.rootView);

        imgUndo = findViewById(R.id.imgUndo);
        imgUndo.setOnClickListener(this);

        imgRedo = findViewById(R.id.imgRedo);
        imgRedo.setOnClickListener(this);

        imgSave = findViewById(R.id.imgSave);
        imgSave.setOnClickListener(this);

        mCancelButton = findViewById(R.id.imgClose);
        mCancelButton.setOnClickListener(this);
    }

    @Override
    public void onEditTextChangeListener(final View rootView, String text, int colorCode) {
        TextEditorDialogFragment textEditorDialogFragment =
                TextEditorDialogFragment.show(this, text, colorCode);
        textEditorDialogFragment.setOnTextEditorListener((inputText, newColorCode) -> {
            final TextStyleBuilder styleBuilder = new TextStyleBuilder();
            styleBuilder.withTextColor(newColorCode);

            mPhotoEditor.editText(rootView, inputText, styleBuilder);
        });
    }

    @Override
    public void onAddViewListener(ViewType viewType, int numberOfAddedViews) {
        Log.d(TAG, "onAddViewListener() called with: viewType = [" + viewType + "], numberOfAddedViews = [" + numberOfAddedViews + "]");
    }

    @Override
    public void onRemoveViewListener(ViewType viewType, int numberOfAddedViews) {
        Log.d(TAG, "onRemoveViewListener() called with: viewType = [" + viewType + "], numberOfAddedViews = [" + numberOfAddedViews + "]");
    }

    @Override
    public void onStartViewChangeListener(ViewType viewType) {
        Log.d(TAG, "onStartViewChangeListener() called with: viewType = [" + viewType + "]");
    }

    @Override
    public void onStopViewChangeListener(ViewType viewType) {
        Log.d(TAG, "onStopViewChangeListener() called with: viewType = [" + viewType + "]");
    }

    @Override
    public void onTouchSourceImage(MotionEvent event) {
        Log.d(TAG, "onTouchView() called with: event = [" + event + "]");
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.imgUndo) {
            mPhotoEditor.undo();
        } else if (id == R.id.imgRedo) {
            mPhotoEditor.redo();
        } else if (id == R.id.imgSave) {
            if (mIsFilterVisible) {
                showFilter(false);
                mTxtCurrentTool.setText("Edit image");
                mCancelButton.setImageDrawable(getDrawable(R.drawable.ic_back_black));
            } else {
                saveImage();
            }
        } else if (id == R.id.imgClose) {
            onBackPressed();
        }
    }

    private void saveImage() {
        final boolean hasStoragePermission =
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED;
        if (hasStoragePermission || isSdkHigherThan28()) {
            showLoading("Saving...");
            SaveSettings saveSettings = new SaveSettings.Builder()
                    .setClearViewsEnabled(true)
                    .setTransparencyEnabled(true)
                    .build();

            File directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            String timeStamp = System.currentTimeMillis() + "";
            String imageFileName = "edited_" + timeStamp + ".png";
            File newFile = new File(directory, imageFileName);
            newImagePath = newFile.getAbsolutePath();

            mPhotoEditor.saveAsFile(newImagePath, saveSettings, new PhotoEditor.OnSaveListener() {
                @Override
                public void onSuccess(@NonNull String imagePath) {
                    hideLoading();
                    showSnackbar("Image Saved Successfully");

                    if (newImagePath != null && newImagePath.length() > 0) {
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
                }

                @Override
                public void onFailure(@NonNull Exception exception) {
                    hideLoading();
                    showSnackbar("Failed to save Image");
                }
            });
        } else {
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onColorChanged(int colorCode) {
        mPhotoEditor.setShape(mShapeBuilder.withShapeColor(colorCode));
    }

    @Override
    public void onOpacityChanged(int opacity) {
        mPhotoEditor.setShape(mShapeBuilder.withShapeOpacity(opacity));
    }

    @Override
    public void onShapeSizeChanged(int shapeSize) {
        mPhotoEditor.setShape(mShapeBuilder.withShapeSize(shapeSize));
    }

    @Override
    public void onShapePicked(ShapeType shapeType) {
        mPhotoEditor.setShape(mShapeBuilder.withShapeType(shapeType));
    }

    @Override
    public void onEmojiClick(String emojiUnicode) {
        mPhotoEditor.addEmoji(emojiUnicode);
    }

    @Override
    public void onStickerClick(Bitmap bitmap) {
        mPhotoEditor.addImage(bitmap);
    }

    @Override
    public void isPermissionGranted(boolean isGranted, String permission) {
        if (isGranted) {
            saveImage();
        }
    }

    private void showSaveDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.msg_save_image));
        builder.setPositiveButton("Save", (dialog, which) -> saveImage());
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.setNeutralButton("Discard", (dialog, which) -> finish());
        builder.create().show();
    }

    @Override
    public void onFilterSelected(PhotoFilter photoFilter) {
        mPhotoEditor.setFilterEffect(photoFilter);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onToolSelected(ToolType toolType) {
        switch (toolType) {
            case SHAPE:
                mIsShape = true;
                mIsEraser = false;

                setForShapeOrEraser();

                mPhotoEditor.setBrushDrawingMode(true);

                showBottomSheetDialogFragment(mShapeBSFragment);

                mCancelButton.setImageDrawable(getDrawable(R.drawable.ic_cancel));
                mTxtCurrentTool.setText(R.string.label_shape);

                mPhotoEditor.clearHelperBox();

                break;
            case TEXT:
                TextEditorDialogFragment textEditorDialogFragment = TextEditorDialogFragment.show(this);
                textEditorDialogFragment.setOnTextEditorListener((inputText, colorCode) -> {
                    final TextStyleBuilder styleBuilder = new TextStyleBuilder();
                    styleBuilder.withTextColor(colorCode);

                    mPhotoEditor.addText(inputText, styleBuilder);
                });
                break;
            case ERASER:
                mIsEraser = true;
                mIsShape = false;

                setForShapeOrEraser();

                mPhotoEditor.brushEraser();

                mCancelButton.setImageDrawable(getDrawable(R.drawable.ic_cancel));
                mTxtCurrentTool.setText(R.string.label_eraser_mode);

                mPhotoEditor.clearHelperBox();

                break;
            case FILTER:
                mTxtCurrentTool.setText(R.string.label_filter);
                mCancelButton.setImageDrawable(getDrawable(R.drawable.ic_cancel));
                showFilter(true);
                break;
            case EMOJI:
                showBottomSheetDialogFragment(mEmojiBSFragment);
                break;
            case STICKER:
                showBottomSheetDialogFragment(mStickerBSFragment);
                break;
            case SIGN:
                startSignActivity();
                break;
        }
    }

    private void setForShapeOrEraser() {
        if (mIsShape) {
            mEditingToolsAdapter.setActiveFunction(EditingToolsAdapter.TYPE_SHAPE);
        } else if (mIsEraser) {
            mEditingToolsAdapter.setActiveFunction(EditingToolsAdapter.TYPE_ERASER);
        } else {
            mEditingToolsAdapter.setActiveFunction(EditingToolsAdapter.TYPE_NONE);
        }
    }

    private void showBottomSheetDialogFragment(BottomSheetDialogFragment fragment) {
        if (fragment == null || fragment.isAdded()) {
            return;
        }
        fragment.show(getSupportFragmentManager(), fragment.getTag());
    }

    private void startSignActivity() {
        Intent intent = new Intent(EditImageActivity.this, SignatureActivity.class);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data == null) return;
            String path = data.getStringExtra("SIGNED_IMAGE_PATH");
            if (path == null || path.length() == 0) {
                return;
            }

            File file = new File(path);
            if (!file.exists()) return;

            try {
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);
                mPhotoEditor.addImage(bitmap);

            } catch (Exception e) {
                // do nothing
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    void showFilter(boolean isVisible) {
        mIsFilterVisible = isVisible;
        mConstraintSet.clone(mRootView);

        if (isVisible) {
            mConstraintSet.clear(mRvFilters.getId(), ConstraintSet.START);
            mConstraintSet.connect(mRvFilters.getId(), ConstraintSet.START,
                    ConstraintSet.PARENT_ID, ConstraintSet.START);
            mConstraintSet.connect(mRvFilters.getId(), ConstraintSet.END,
                    ConstraintSet.PARENT_ID, ConstraintSet.END);
        } else {
            mConstraintSet.connect(mRvFilters.getId(), ConstraintSet.START,
                    ConstraintSet.PARENT_ID, ConstraintSet.END);
            mConstraintSet.clear(mRvFilters.getId(), ConstraintSet.END);
        }

        ChangeBounds changeBounds = new ChangeBounds();
        changeBounds.setDuration(350);
        changeBounds.setInterpolator(new AnticipateOvershootInterpolator(1.0f));
        TransitionManager.beginDelayedTransition(mRootView, changeBounds);

        mConstraintSet.applyTo(mRootView);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBackPressed() {
        if (mIsFilterVisible) {
            showFilter(false);
            mCancelButton.setImageDrawable(getDrawable(R.drawable.ic_back_black));
            mTxtCurrentTool.setText("Edit image");
        } else if (mIsShape) {
            mIsShape = false;
            if (mPhotoEditor != null) {
                mPhotoEditor.setBrushDrawingMode(false);
            }
            mCancelButton.setImageDrawable(getDrawable(R.drawable.ic_back_black));
            mTxtCurrentTool.setText("Edit image");
            setForShapeOrEraser();
        } else if (mIsEraser) {
            mIsEraser = false;
            if (mPhotoEditor != null) {
                mPhotoEditor.stopBrushEraser();
            }
            mCancelButton.setImageDrawable(getDrawable(R.drawable.ic_back_black));
            mTxtCurrentTool.setText("Edit image");
            setForShapeOrEraser();
        } else if (!mPhotoEditor.isCacheEmpty()) {
            showSaveDialog();
        } else {
            super.onBackPressed();
        }
    }
}
