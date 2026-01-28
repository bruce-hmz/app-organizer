package com.apporganizer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.apporganizer.R
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

/**
 * 应用列表适配器
 */
class AppAdapter(
    private val useListLayout: Boolean,
    private val onAppClick: (AppInfo) -> Unit
) : ListAdapter<AppInfo, AppAdapter.AppViewHolder>(AppDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val layoutId = if (useListLayout) {
            R.layout.item_app_list
        } else {
            R.layout.item_app_grid
        }
        
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val appCard: MaterialCardView = itemView.findViewById(R.id.appCard)
        private val appIcon: ImageView = itemView.findViewById(R.id.appIcon)
        private val appName: TextView = itemView.findViewById(R.id.appName)
        private val categoryChips: ChipGroup = itemView.findViewById(R.id.categoryChips)

        fun bind(appInfo: AppInfo) {
            appIcon.setImageDrawable(appInfo.icon)
            appName.text = appInfo.appName
            
            // 清空并添加分类标签
            categoryChips.removeAllViews()
            appInfo.categories.forEach { category ->
                if (category != AppCategory.ALL && category != AppCategory.OTHER) {
                    val chip = Chip(itemView.context).apply {
                        text = category.displayName
                        isClickable = false
                        isCheckable = false
                        setChipBackgroundColorResource(android.R.color.darker_gray)
                        setTextAppearance(android.R.style.TextAppearance_Small)
                    }
                    categoryChips.addView(chip)
                }
            }
            
            // 点击事件
            appCard.setOnClickListener {
                onAppClick(appInfo)
            }
        }
    }

    /**
     * DiffUtil 用于高效更新列表
     */
    private class AppDiffCallback : DiffUtil.ItemCallback<AppInfo>() {
        override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem == newItem
        }
    }
}
