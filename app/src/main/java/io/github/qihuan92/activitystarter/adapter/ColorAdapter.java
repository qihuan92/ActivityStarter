package io.github.qihuan92.activitystarter.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import io.github.qihuan92.activitystarter.R;
import io.github.qihuan92.activitystarter.entity.ColorItem;

/**
 * ColorAdapter
 *
 * @author qi
 * @since 2021/8/17
 */
public class ColorAdapter extends ListAdapter<ColorItem, ColorAdapter.ViewHolder> {

    private final OnItemClickListener onItemClickListener;

    public ColorAdapter(OnItemClickListener onItemClickListener) {
        super(new DiffCallback());
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_color, parent, false), onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final View viewColor;
        private final TextView tvSelected;
        private ColorItem colorItem;

        public ViewHolder(@NonNull View itemView, OnItemClickListener onItemClickListener) {
            super(itemView);
            viewColor = itemView.findViewById(R.id.view_color);
            tvSelected = itemView.findViewById(R.id.tv_selected);
            itemView.setOnClickListener(view -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(colorItem);
                }
            });
        }

        void bind(ColorItem colorItem) {
            this.colorItem = colorItem;
            viewColor.setBackgroundColor(Color.parseColor(colorItem.getColor()));
            tvSelected.setVisibility(colorItem.isSelected() ? View.VISIBLE : View.GONE);
        }
    }

    public static class DiffCallback extends DiffUtil.ItemCallback<ColorItem> {

        @Override
        public boolean areItemsTheSame(@NonNull ColorItem oldItem, @NonNull ColorItem newItem) {
            return newItem.getColor().equals(oldItem.getColor());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ColorItem oldItem, @NonNull ColorItem newItem) {
            return newItem.getColor().equals(oldItem.getColor());
        }
    }

    public interface OnItemClickListener {
        void onItemClick(ColorItem colorItem);
    }
}
