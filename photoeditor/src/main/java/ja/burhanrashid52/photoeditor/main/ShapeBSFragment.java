package ja.burhanrashid52.photoeditor.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import ja.burhanrashid52.photoeditor.R;
import ja.burhanrashid52.photoeditor.shape.ShapeBuilder;
import ja.burhanrashid52.photoeditor.shape.ShapeType;

public class ShapeBSFragment extends BottomSheetDialogFragment implements SeekBar.OnSeekBarChangeListener {

    private CardView mColorSelected;
    private ShapeBuilder mShapeBuilder;

    public ShapeBSFragment() {
        // Required empty public constructor
    }

    private Properties mProperties;

    public interface Properties {
        void onColorChanged(int colorCode);

        void onOpacityChanged(int opacity);

        void onShapeSizeChanged(int shapeSize);

        void onShapePicked(ShapeType shapeType);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.sheet_dialog_style);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_shapes_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rvColor = view.findViewById(R.id.shapeColors);
        SeekBar sbOpacity = view.findViewById(R.id.shapeOpacity);
        SeekBar sbBrushSize = view.findViewById(R.id.shapeSize);
        RadioGroup shapeGroup = view.findViewById(R.id.shapeRadioGroup);
        mColorSelected = view.findViewById(R.id.colorSelector);

        // shape picker
        shapeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.lineRadioButton) {
                mProperties.onShapePicked(ShapeType.LINE);
            } else if (checkedId == R.id.ovalRadioButton) {
                mProperties.onShapePicked(ShapeType.OVAL);
            } else if (checkedId == R.id.rectRadioButton) {
                mProperties.onShapePicked(ShapeType.RECTANGLE);
            } else {
                mProperties.onShapePicked(ShapeType.BRUSH);
            }
        });

        sbOpacity.setOnSeekBarChangeListener(this);
        sbBrushSize.setOnSeekBarChangeListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        rvColor.setLayoutManager(layoutManager);
        rvColor.setHasFixedSize(true);
        ColorPickerAdapter colorPickerAdapter = new ColorPickerAdapter(getActivity());
        colorPickerAdapter.setOnColorPickerClickListener(colorCode -> {
            if (mProperties != null) {
                mProperties.onColorChanged(colorCode);
            }
            mColorSelected.setCardBackgroundColor(colorCode);
            dismiss();
        });

        if (mShapeBuilder != null) {
            mColorSelected.setCardBackgroundColor(mShapeBuilder.getShapeColor());
        }

        rvColor.setAdapter(colorPickerAdapter);
    }

    public void setPropertiesChangeListener(Properties properties) {
        mProperties = properties;
    }

    public void setShapeBuilder(ShapeBuilder shapeBuilder) {
        mShapeBuilder = shapeBuilder;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        int id = seekBar.getId();
        if (id == R.id.shapeOpacity) {
            if (mProperties != null) {
                mProperties.onOpacityChanged(i);
            }
        } else if (id == R.id.shapeSize) {
            if (mProperties != null) {
                mProperties.onShapeSizeChanged(i);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}
