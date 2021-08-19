package ja.burhanrashid52.photoeditor.signature.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import ja.burhanrashid52.photoeditor.R;
import ja.burhanrashid52.photoeditor.main.ColorPickerAdapter;
import ja.burhanrashid52.photoeditor.signature.views.SignaturePad;

public class SignatureActivity extends Activity {

    private SignaturePad mSignaturePad;
    private ImageView mCloseButton;
    private ImageView mSaveButton;
    private AppCompatButton mClearButton;
    private boolean mIsSigned = false;
    private RecyclerView mRvColor;
    private CardView mColorSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature);

        mSignaturePad = findViewById(R.id.signature_pad);
        mSignaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {
            }

            @Override
            public void onSigned() {
                mSaveButton.setEnabled(true);
                mIsSigned = true;
            }

            @Override
            public void onClear() {
                mSaveButton.setEnabled(false);
                mIsSigned = false;
            }
        });

        mCloseButton = findViewById(R.id.close_button);
        mSaveButton = findViewById(R.id.save_button);
        mClearButton = findViewById(R.id.clear_button);

        mClearButton.setOnClickListener(view -> mSignaturePad.clear());
        mCloseButton.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        mRvColor = findViewById(R.id.shapeColors);
        mColorSelected = findViewById(R.id.colorSelector);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRvColor.setLayoutManager(layoutManager);
        mRvColor.setHasFixedSize(true);
        ColorPickerAdapter colorPickerAdapter = new ColorPickerAdapter(this);
        colorPickerAdapter.setOnColorPickerClickListener(colorCode -> {
            mColorSelected.setCardBackgroundColor(colorCode);
            mSignaturePad.setPenColor(colorCode);
        });

        mRvColor.setAdapter(colorPickerAdapter);

        mSignaturePad.setVelocityFilterWeight(0.8f);

        mSaveButton.setOnClickListener(view -> {
            if (mIsSigned) {
                Toast.makeText(this, "Saving...", Toast.LENGTH_SHORT).show();

                Bitmap signatureBitmap = mSignaturePad.getSignatureBitmap();
                File directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                String timeStamp = System.currentTimeMillis() + "";
                String imageFileName = "signed_" + timeStamp + ".png";
                File newFile = new File(directory, imageFileName);

                try {
                    saveBitmapToPNG(signatureBitmap, newFile);

                    Intent finishIntent = new Intent();
                    finishIntent.putExtra("SIGNED_IMAGE_PATH", newFile.getAbsolutePath());
                    setResult(Activity.RESULT_OK, finishIntent);
                    finish();
                } catch (Exception e) {
                    setResult(RESULT_CANCELED);
                    finish();
                }

            } else {
                Toast.makeText(this, "You didn't sign yet. Please sign before saving!", Toast.LENGTH_SHORT).show();
            }

        });
    }

    public void saveBitmapToPNG(Bitmap bitmap, File photo) throws IOException {
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawBitmap(bitmap, 0, 0, null);
        OutputStream stream = new FileOutputStream(photo);
        newBitmap.compress(Bitmap.CompressFormat.PNG, 80, stream);
        stream.close();
    }
}
