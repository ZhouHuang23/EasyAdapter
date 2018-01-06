package com.hz.android.easyadapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * 仿照原生RecyclerView.Adapter的实现，在原生适配器的基础上 支持监听item单击事件以及支持单选模式
 * Created by Administrator on 2018/1/4.
 */

public abstract class EasyAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> implements View.OnClickListener {
    private OnItemClickListener onItemClickListener;
    private OnItemSelectListener onItemSelectListener;
    private OnItemMultSelectListener onItemMultSelectListener;
    private SelectMode selectMode;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemSelectListener(OnItemSelectListener onItemSelectListener) {
        this.onItemSelectListener = onItemSelectListener;
    }

    public void setOnItemMultSelectListener(OnItemMultSelectListener onItemMultSelectListener) {
        this.onItemMultSelectListener = onItemMultSelectListener;
    }

    public abstract void whenBindViewHolder(VH holder, int position);

    private int singleSelected = 0; // 默认为第一个被选中
    private List<Integer> multSelected = new ArrayList<>();
    private int maxSelectedCount = -1;

    @Override
    public void onBindViewHolder(VH holder, int position) {
        whenBindViewHolder(holder, position);
        //
        holder.itemView.setTag(position);
        holder.itemView.setOnClickListener(this);

        if (selectMode == SelectMode.CLICK) { //点击
            holder.itemView.setSelected(false);
        } else if (selectMode == SelectMode.SINGLE_SELECT) { //单选
            if (singleSelected == position) {
                holder.itemView.setSelected(true);
            } else {
                holder.itemView.setSelected(false);
            }
        } else if (selectMode == SelectMode.MULT_SELECT) {//多选
            if (multSelected.contains(position)) {
                holder.itemView.setSelected(true);
            } else {
                holder.itemView.setSelected(false);
            }
        }
    }

    @Override
    public void onClick(View v) {

        int itemPosition = (int) v.getTag();

        if (onItemClickListener != null && selectMode == SelectMode.CLICK) {//点击模式
            onItemClickListener.onClicked(itemPosition);
        } else if (onItemSelectListener != null && selectMode == SelectMode.SINGLE_SELECT) { //单选模式
            onItemSelectListener.onSelected(itemPosition);
            singleSelected = itemPosition;
            notifyDataSetChanged();//通知刷新
        } else if (onItemMultSelectListener != null && selectMode == SelectMode.MULT_SELECT) {//多选模式
            if (maxSelectedCount <= 0  //选择不受限制
                    || multSelected.size() < maxSelectedCount) {  // 选择个数需要小于最大可选数
                onItemMultSelectListener.onMultSelected(itemPosition);
                if (multSelected.contains(itemPosition)) {
                    multSelected.remove((Object) itemPosition);
                } else {
                    multSelected.add(itemPosition);
                }
            } else if (multSelected.size() == maxSelectedCount&&multSelected.contains(itemPosition)){ //当等于最大数量并且点击的item包含在已选中 可清除
                multSelected.remove((Object) itemPosition);
            }
            notifyDataSetChanged();
        }
    }

    /**
     * 设置选择模式
     *
     * @param selectMode
     */

    public void setSelectMode(SelectMode selectMode) {
        this.selectMode = selectMode;
        notifyDataSetChanged();
    }

    /**
     * 设置默认选中项，一个或多个
     *
     * @param itemPositions
     */

    public void setSelected(int... itemPositions) {
        multSelected.clear();
        if (selectMode == SelectMode.SINGLE_SELECT) {
            singleSelected = itemPositions[0];
        } else {
            for (int itemPosition : itemPositions) {
                multSelected.add(itemPosition);
            }
        }
        notifyDataSetChanged();
    }

    /**
     * 清除选择项，只有在MULT_SELECT模式下有效
     */
    public void clearSelected() { // 这个方法只在多选模式下生效

        if (selectMode == SelectMode.MULT_SELECT) {
            multSelected.clear();
        }
        notifyDataSetChanged();
    }

    /**
     * 获取单选项位置
     */
    public int getSingleSelectedPosition() {
        return singleSelected;
    }

    /**
     * 获取多选项位置
     */
    public List<Integer> getMultSelectedPosition() {
        return multSelected;
    }

    /**
     * 设置最大可选数量，
     *
     * @param maxSelectedCount maxSelectedCount <= 0 表示不限制选择数
     */
    public void setMaxSelectedCount(int maxSelectedCount) {
        if (maxSelectedCount < multSelected.size()) {
            multSelected.clear();
        }
        this.maxSelectedCount = maxSelectedCount;
        notifyDataSetChanged();
    }

    /**
     * 选择全部，仅在maxSelectedCount <= 0 不限制选择数时有效
     */
    public void selectAll() {
        if (maxSelectedCount <= 0) {
            multSelected.clear();
            for (int i = 0; i < getItemCount(); i++) {
                multSelected.add(i);
            }
            notifyDataSetChanged();
        }
    }

    /**
     * 反选全部,仅在maxSelectedCount <= 0 不限制选择数时有效
     */
    public void reverseSelected() {
        if (maxSelectedCount <= 0) {
            for (int i = 0; i < getItemCount(); i++) {
                if (multSelected.contains(i)) {
                    multSelected.remove((Object) i);
                } else {
                    multSelected.add(i);
                }
            }
            notifyDataSetChanged();
        }
    }

    public interface OnItemClickListener {
        void onClicked(int itemPosition);
    }

    public interface OnItemSelectListener {
        void onSelected(int itemPosition);
    }

    public interface OnItemMultSelectListener {
        void onMultSelected(int itemPosition);
    }

    public enum SelectMode {
        CLICK, SINGLE_SELECT, MULT_SELECT
    }
}
