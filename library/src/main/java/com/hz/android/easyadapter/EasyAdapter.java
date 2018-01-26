package com.hz.android.easyadapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * 仿照原生RecyclerView.Adapter的实现，在原生适配器的基础上 支持监听item单击事件以及支持单选模式、多选模式
 * Created by Administrator on 2018/1/4.
 */

public abstract class EasyAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> implements View.OnClickListener {
    private OnItemClickListener onItemClickListener;
    private OnItemSingleSelectListener onItemSingleSelectListener;
    private OnItemMultiSelectListener onItemMultiSelectListener;
    private SelectMode selectMode;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemSingleSelectListener(OnItemSingleSelectListener onItemSingleSelectListener) {
        this.onItemSingleSelectListener = onItemSingleSelectListener;
    }

    public void setOnItemMultiSelectListener(OnItemMultiSelectListener onItemMultiSelectListener) {
        this.onItemMultiSelectListener = onItemMultiSelectListener;
    }

    public abstract void whenBindViewHolder(VH holder, int position);

    private int singleSelected = 0; // 默认为第一个被选中
    private List<Integer> multiSelected = new ArrayList<>();
    private int maxSelectedCount = -1;

    @Override
    public void onBindViewHolder(VH holder, int position) {
        whenBindViewHolder(holder, position);

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
        } else if (selectMode == SelectMode.MULTI_SELECT) {//多选
            if (multiSelected.contains(position)) {
                holder.itemView.setSelected(true);
            } else {
                holder.itemView.setSelected(false);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int itemPosition = (int) v.getTag();
        if (selectMode == SelectMode.CLICK) {//点击模式
            if (onItemClickListener != null) {
                onItemClickListener.onClicked(itemPosition);
            }
        } else if (selectMode == SelectMode.SINGLE_SELECT) { //单选模式
            if (onItemSingleSelectListener != null) {
                if (singleSelected == itemPosition) {
                    onItemSingleSelectListener.onSelected(itemPosition, false);
                } else {
                    singleSelected = itemPosition;
                    onItemSingleSelectListener.onSelected(itemPosition, true);
                }
            }
            notifyDataSetChanged();//通知刷新
        } else if (selectMode == SelectMode.MULTI_SELECT) {//多选模式
            if (maxSelectedCount <= 0  //选择不受限制
                    || multiSelected.size() < maxSelectedCount) {  // 选择个数需要小于最大可选数
                if (multiSelected.contains(itemPosition)) {
                    multiSelected.remove((Integer) itemPosition);
                    if (onItemMultiSelectListener != null) {
                        onItemMultiSelectListener.onSelected(Operation.ORDINARY, itemPosition, false);
                    }
                } else {
                    multiSelected.add(itemPosition);
                    if (onItemMultiSelectListener != null) {
                        onItemMultiSelectListener.onSelected(Operation.ORDINARY, itemPosition, true);
                    }
                }

            } else if (multiSelected.size() == maxSelectedCount && multiSelected.contains(itemPosition)) { //当等于最大数量并且点击的item包含在已选中 可清除
                multiSelected.remove((Integer) itemPosition);
                if (onItemMultiSelectListener != null) {
                    onItemMultiSelectListener.onSelected(Operation.ORDINARY, itemPosition, false);
                }
            }
            notifyDataSetChanged();
        }
    }

    //=========API=========

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
     * 获取选择模式
     *
     * @return
     */
    public SelectMode getSelectMode() {
        return selectMode;
    }

    /**
     * 设置默认选中项，一个或多个
     *
     * @param itemPositions
     */

    public void setSelected(int... itemPositions) {
        multiSelected.clear();
        if (selectMode == SelectMode.SINGLE_SELECT) {
            singleSelected = itemPositions[0];
            if (onItemSingleSelectListener != null) {
                onItemSingleSelectListener.onSelected(singleSelected, true);
            }
        } else {
            for (int itemPosition : itemPositions) {
                multiSelected.add(itemPosition);
                if (onItemMultiSelectListener != null) {
                    onItemMultiSelectListener.onSelected(Operation.ORDINARY, itemPosition, true);
                }
            }
        }
        notifyDataSetChanged();
    }

    /**
     * 获取单选模式选中Item位置
     *
     * @return
     */
    public int getSingleSelected() {
        return singleSelected;
    }

    /**
     * 清除选择项，只有在MULT_SELECT模式下有效
     */
    public void clearSelected() {
        if (selectMode == SelectMode.MULTI_SELECT) {
            multiSelected.clear();
            if (onItemMultiSelectListener != null) {
                onItemMultiSelectListener.onSelected(Operation.ALL_CANCEL, -1, false);
            }
            notifyDataSetChanged();
        }
    }

    /**
     * 获取单选项位置
     */
    public int getSingleSelectedPosition() {
        return singleSelected;
    }

    /**
     * 获取多选项位置，元素顺序按照选择顺序排列
     */
    public List<Integer> getMultiSelectedPosition() {
        return multiSelected;
    }

    /**
     * 设置最大可选数量
     *
     * @param maxSelectedCount maxSelectedCount <= 0 表示不限制选择数
     */
    public void setMaxSelectedCount(int maxSelectedCount) {
        if (maxSelectedCount < multiSelected.size()) {
            multiSelected.clear();
        }
        this.maxSelectedCount = maxSelectedCount;
        if (onItemMultiSelectListener != null) {
            onItemMultiSelectListener.onSelected(Operation.SET_MAX_COUNT, -1, false);
        }
        notifyDataSetChanged();
    }

    /**
     * 获取最大可选数目
     *
     * @return
     */
    public int getMaxSelectedCount() {
        return maxSelectedCount;
    }

    /**
     * 选择全部，仅在maxSelectedCount <= 0 不限制选择数时有效
     */
    public void selectAll() {
        if (maxSelectedCount <= 0) {
            multiSelected.clear();
            for (int i = 0; i < getItemCount(); i++) {
                multiSelected.add(i);
            }
            if (onItemMultiSelectListener != null) {
                onItemMultiSelectListener.onSelected(Operation.ALL_SELECTED, -1, false);
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
                if (multiSelected.contains(i)) {
                    multiSelected.remove((Integer) i);
                } else {
                    multiSelected.add(i);
                }
            }
            if (onItemMultiSelectListener != null) {
                onItemMultiSelectListener.onSelected(Operation.REVERSE_SELECTED, -1, false);
            }
            notifyDataSetChanged();
        }
    }

    /**
     * 判断某个item位置是否被选中
     *
     * @param position
     * @return
     */
    public boolean isSelected(int position) {
        if (selectMode == SelectMode.SINGLE_SELECT) {
            return position == singleSelected;
        } else if (selectMode == SelectMode.MULTI_SELECT) {
            return multiSelected.contains(position);
        }
        return false;
    }

    /**
     * 点选模式监听接口
     */
    public interface OnItemClickListener {
        /**
         * 点选模式下，点击item时回调
         *
         * @param itemPosition 点击的item位置
         */
        void onClicked(int itemPosition);
    }

    /**
     * 单选模式监听接口
     */
    public interface OnItemSingleSelectListener {
        /**
         * 单选模式下，点击Item选中时回调
         *
         * @param itemPosition 点击的item位置
         * @param isSelected   是否选中
         */
        void onSelected(int itemPosition, boolean isSelected);

    }

    /**
     * 多选模式监听接口
     */
    public interface OnItemMultiSelectListener {
        /**
         * 多选模式下，点击Item选中时回调
         *
         * @param operation    操作类型，分为普通，全选 反选 取消全部等。
         * @param itemPosition 点击的item位置 仅在操作类型为普通时生效
         * @param isSelected   是否选中 仅在操作类型为普通时生效
         */
        void onSelected(Operation operation, int itemPosition, boolean isSelected);

    }

    /**
     * 选择模式，分为点击，单选，多选。
     */
    public enum SelectMode {
        CLICK, SINGLE_SELECT, MULTI_SELECT
    }

    /**
     * 操作类型，分为普通，全选 反选 取消全部等。
     */
    public enum Operation {
        ORDINARY, ALL_SELECTED, REVERSE_SELECTED, ALL_CANCEL, SET_MAX_COUNT
    }
}
