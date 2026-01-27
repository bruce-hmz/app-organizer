package com.apporganizer

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
        
        // 设置执行整理按钮
        binding.executeButton.setOnClickListener {
            checkAccessibilityPermissionAndExecute()
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
     * 检查辅助功能权限并执行整理
     */
    private fun checkAccessibilityPermissionAndExecute() {
        if (isAccessibilityServiceEnabled()) {
            // 权限已授予，执行整理
            executeOrganization()
        } else {
            // 权限未授予，引导用户开启
            showAccessibilityPermissionDialog()
        }
    }

    /**
     * 检查辅助功能服务是否已启用
     */
    private fun isAccessibilityServiceEnabled(): Boolean {
        val serviceName = "${packageName}/.AppOrganizerAccessibilityService"
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return enabledServices?.contains(serviceName) == true
    }

    /**
     * 显示辅助功能权限请求对话框
     */
    private fun showAccessibilityPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("需要辅助功能权限")
            .setMessage("为了自动创建文件夹并整理应用，需要开启辅助功能服务。\n\n" +
                    "开启后，应用将能够模拟拖动操作来创建文件夹和移动应用。")
            .setPositiveButton("去设置") { _, _ ->
                openAccessibilitySettings()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 打开辅助功能设置页面
     */
    private fun openAccessibilitySettings() {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "无法打开设置页面", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 执行整理操作
     */
    private fun executeOrganization() {
        // 将整理方案传递给Accessibility Service
        val intent = Intent(this, AppOrganizerAccessibilityService::class.java)
        intent.action = AppOrganizerAccessibilityService.ACTION_ORGANIZE_APPS
        
        // 将文件夹信息转换为可序列化的格式
        val foldersData = ArrayList<Bundle>()
        for (folder in organizedFolders) {
            val folderBundle = Bundle()
            folderBundle.putString("category", folder.category.name)
            
            val appPackages = ArrayList<String>()
            for (app in folder.apps) {
                appPackages.add(app.packageName)
            }
            folderBundle.putStringArrayList("apps", appPackages)
            
            foldersData.add(folderBundle)
        }
        
        intent.putParcelableArrayListExtra("folders", foldersData)
        
        // 启动服务
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        
        Toast.makeText(this, "开始自动整理，请保持屏幕开启", Toast.LENGTH_LONG).show()
        
        // 显示进度对话框
        showProgressDialog()
    }

    /**
     * 显示进度对话框
     */
    private fun showProgressDialog() {
        AlertDialog.Builder(this)
            .setTitle("正在整理...")
            .setMessage("应用正在自动创建文件夹并移动应用，请保持屏幕开启。\n\n" +
                    "完成后会自动关闭此对话框。")
            .setCancelable(false)
            .setPositiveButton("完成", null)
            .show()
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