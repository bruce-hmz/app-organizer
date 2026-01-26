package com.apporganizer

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class OrganizeResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrganizeResultBinding
    private lateinit var categoryAdapter: CategoryFolderAdapter
    
    private var allApps = listOf<AppInfo>()
    private var organizedFolders = listOf<FolderInfo>()
    private var preference = OrganizePreference.GENERAL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrganizeResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 设置返回按钮
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        
        // 获取传入的数据
        val prefName = intent.getStringExtra("preference") ?: OrganizePreference.GENERAL.name
        preference = OrganizePreference.values().find { it.name == prefName } ?: OrganizePreference.GENERAL
        
        // 直接使用全局应用列表
        allApps = AppData.allApps
        
        // 打印应用数量日志
        println("OrganizeResultActivity - 应用总数: ${allApps.size}")
        
        // 重新加载图标
        allApps = allApps.map { app ->
            try {
                val icon = packageManager.getApplicationIcon(app.packageName)
                app.copy(icon = icon)
            } catch (e: Exception) {
                app
            }
        }
        
        // 组织应用到文件夹
        organizeAppsIntoFolders()
        
        // 打印文件夹数量日志
        println("OrganizeResultActivity - 文件夹数量: ${organizedFolders.size}")
        println("OrganizeResultActivity - 每个文件夹的应用数量:")
        organizedFolders.forEach { folder ->
            println("${folder.category.displayName}: ${folder.apps.size}")
        }
        
        // 更新顶部提示信息
        updateTipCard()
        
        // 设置RecyclerView
        setupRecyclerView()
    }

    /**
     * 将应用整理到文件夹
     */
    private fun organizeAppsIntoFolders() {
        val folderMap = mutableMapOf<AppCategory, MutableList<AppInfo>>()
        val preferredCategories = preference.getPreferredCategories()
        
        // 按分类整理应用
        for (app in allApps) {
            var addedToAnyFolder = false
            
            for (category in app.categories) {
                // 只使用偏好中包含的分类
                if (category in preferredCategories) {
                    folderMap.getOrPut(category) { mutableListOf() }.add(app)
                    addedToAnyFolder = true
                }
            }
            
            // 如果应用没有被任何偏好分类包含，添加到OTHER
            if (!addedToAnyFolder) {
                folderMap.getOrPut(AppCategory.OTHER) { mutableListOf() }.add(app)
            }
        }
        
        // 转换为FolderInfo列表
        organizedFolders = folderMap.entries
            .sortedByDescending { it.value.size }
            .map { (category, apps) ->
                FolderInfo(
                    category = category,
                    apps = apps.distinctBy { it.packageName }.sortedBy { it.appName }
                )
            }
    }

    /**
     * 更新提示卡片
     */
    private fun updateTipCard() {
        val totalApps = organizedFolders.sumOf { it.apps.size }
        binding.tipTitle.text = "📊 整理方案：${preference.displayName}"
        binding.tipContent.text = "建议创建 ${organizedFolders.size} 个文件夹，整理 $totalApps 个应用。" +
                "\n\n💡 在桌面长按应用图标，拖动到另一个应用上可创建文件夹，然后继续添加其他应用。"
    }

    /**
     * 设置RecyclerView
     */
    private fun setupRecyclerView() {
        categoryAdapter = CategoryFolderAdapter(organizedFolders)
        binding.categoriesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@OrganizeResultActivity)
            adapter = categoryAdapter
        }
    }
}

/**
 * 文件夹信息
 */
data class FolderInfo(
    val category: AppCategory,
    val apps: List<AppInfo>
)

/**
 * 分类文件夹适配器
 */
class CategoryFolderAdapter(
    private val folders: List<FolderInfo>
) : RecyclerView.Adapter<CategoryFolderAdapter.FolderViewHolder>() {

    class FolderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val folderIcon: TextView = view.findViewById(R.id.folderIcon)
        val folderName: TextView = view.findViewById(R.id.folderName)
        val appCount: TextView = view.findViewById(R.id.appCount)
        val expandButton: com.google.android.material.button.MaterialButton = view.findViewById(R.id.expandButton)
        val appsRecyclerView: RecyclerView = view.findViewById(R.id.appsRecyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_folder, parent, false)
        return FolderViewHolder(view)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folder = folders[position]
        var isExpanded = false
        
        // 设置文件夹图标
        holder.folderIcon.text = getCategoryIcon(folder.category)
        holder.folderName.text = folder.category.displayName
        holder.appCount.text = "${folder.apps.size}个应用"
        
        // 设置应用列表
        val appAdapter = AppSimpleAdapter(folder.apps)
        holder.appsRecyclerView.apply {
            layoutManager = LinearLayoutManager(holder.itemView.context)
            adapter = appAdapter
        }
        
        // 展开/收起功能
        holder.expandButton.setOnClickListener {
            isExpanded = !isExpanded
            holder.appsRecyclerView.visibility = if (isExpanded) View.VISIBLE else View.GONE
            holder.expandButton.text = if (isExpanded) "▲" else "▼"
        }
    }

    override fun getItemCount() = folders.size

    private fun getCategoryIcon(category: AppCategory): String {
        return when (category) {
            AppCategory.SOCIAL -> "💬"
            AppCategory.ENTERTAINMENT -> "🎬"
            AppCategory.SHOPPING -> "🛒"
            AppCategory.TOOLS -> "🔧"
            AppCategory.EDUCATION -> "📚"
            AppCategory.FINANCE -> "💰"
            AppCategory.TRAVEL -> "✈️"
            AppCategory.HEALTH -> "💊"
            AppCategory.NEWS -> "📰"
            AppCategory.OFFICE -> "💼"
            AppCategory.PHOTO -> "📷"
            AppCategory.MUSIC -> "🎵"
            AppCategory.GAME -> "🎮"
            AppCategory.SYSTEM -> "⚙️"
            else -> "📁"
        }
    }
}

/**
 * 简单应用适配器
 */
class AppSimpleAdapter(
    private val apps: List<AppInfo>
) : RecyclerView.Adapter<AppSimpleAdapter.AppViewHolder>() {

    class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appIcon: ImageView = view.findViewById(R.id.appIcon)
        val appName: TextView = view.findViewById(R.id.appName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app_simple, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]
        holder.appIcon.setImageDrawable(app.icon)
        holder.appName.text = app.appName
    }

    override fun getItemCount() = apps.size
}

/**
 * ViewBinding 辅助类
 */
class ActivityOrganizeResultBinding private constructor(
    val root: View,
    val toolbar: com.google.android.material.appbar.MaterialToolbar,
    val tipTitle: TextView,
    val tipContent: TextView,
    val categoriesRecyclerView: RecyclerView
) {
    companion object {
        fun inflate(inflater: LayoutInflater): ActivityOrganizeResultBinding {
            val root = inflater.inflate(R.layout.activity_organize_result, null)
            return bind(root)
        }
        
        fun bind(root: View): ActivityOrganizeResultBinding {
            return ActivityOrganizeResultBinding(
                root = root,
                toolbar = root.findViewById(R.id.toolbar),
                tipTitle = root.findViewById(R.id.tipTitle),
                tipContent = root.findViewById(R.id.tipContent),
                categoriesRecyclerView = root.findViewById(R.id.categoriesRecyclerView)
            )
        }
    }
}


