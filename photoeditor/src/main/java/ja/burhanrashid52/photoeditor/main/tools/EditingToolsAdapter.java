package ja.burhanrashid52.photoeditor.main.tools;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ja.burhanrashid52.photoeditor.R;
import ja.burhanrashid52.photoeditor.view.ColorUtils;

/**
 * @author <a href="https://github.com/burhanrashid52">Burhanuddin Rashid</a>
 * @version 0.1.2
 * @since 5/23/2018
 */
public class EditingToolsAdapter extends RecyclerView.Adapter<EditingToolsAdapter.ViewHolder> {

    private List<ToolModel> mToolList = new ArrayList<>();
    private OnItemSelected mOnItemSelected;
    private int mActiveFunction = -1;

    public static final int TYPE_SHAPE = 2;
    public static final int TYPE_ERASER = 3;
    public static final int TYPE_NONE = -1;

    public EditingToolsAdapter(OnItemSelected onItemSelected) {
        mOnItemSelected = onItemSelected;
        mToolList.add(new ToolModel("Sign", R.drawable.ic_sign, ToolType.SIGN));
        mToolList.add(new ToolModel("Filter", R.drawable.ic_photo_filter, ToolType.FILTER));
        mToolList.add(new ToolModel("Shape", R.drawable.ic_oval, ToolType.SHAPE));
        mToolList.add(new ToolModel("Eraser", R.drawable.ic_eraser, ToolType.ERASER));
        mToolList.add(new ToolModel("Text", R.drawable.ic_text, ToolType.TEXT));
        mToolList.add(new ToolModel("Emoji", R.drawable.ic_insert_emoticon, ToolType.EMOJI));
        mToolList.add(new ToolModel("Sticker", R.drawable.ic_sticker, ToolType.STICKER));
    }

    public interface OnItemSelected {
        void onToolSelected(ToolType toolType);
    }

    class ToolModel {
        private String mToolName;
        private int mToolIcon;
        private ToolType mToolType;

        ToolModel(String toolName, int toolIcon, ToolType toolType) {
            mToolName = toolName;
            mToolIcon = toolIcon;
            mToolType = toolType;
        }

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_editing_tools, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ToolModel item = mToolList.get(position);
        holder.txtTool.setText(item.mToolName);
        holder.imgToolIcon.setImageResource(item.mToolIcon);
        if (position == mActiveFunction) {
            holder.setActiveFunction();
        } else {
            holder.setInactiveFunction();
        }
    }

    @Override
    public int getItemCount() {
        return mToolList.size();
    }

    public void setActiveFunction(int type) {
        mActiveFunction = type;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgToolIcon;
        TextView txtTool;

        ViewHolder(View itemView) {
            super(itemView);
            imgToolIcon = itemView.findViewById(R.id.imgToolIcon);
            txtTool = itemView.findViewById(R.id.txtTool);
            itemView.setOnClickListener(v -> mOnItemSelected.onToolSelected(mToolList.get(getLayoutPosition()).mToolType));
        }

        void setActiveFunction() {
            int color = ColorUtils.getColorFromResource(itemView.getContext(), R.color.yellow_color_picker);
            imgToolIcon.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
            txtTool.setTextColor(color);
        }

        void setInactiveFunction() {
            int color = ColorUtils.getColorFromResource(itemView.getContext(), R.color.white);
            imgToolIcon.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
            txtTool.setTextColor(color);
        }
    }
}
