package com.apporganizer

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appAdapter: AppAdapter
    
    private var allApps = listOf<AppInfo>()
    private var filteredApps = listOf<AppInfo>()
    
    private var currentBrand = BrandStyle.XIAOMI
    private var currentCategory = AppCategory.ALL
    private var searchQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupBrandSelector()
        setupCategoryChips()
        setupSearchBar()
        loadInstalledApps()
    }

    /**
     * 设置品牌选择器
     */
    private fun setupBrandSelector() {
        val brands = BrandStyle.values().map { it.displayName }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, brands)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        
        binding.brandSpinner.adapter = adapter
        binding.brandSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentBrand = BrandStyle.values()[position]
                updateRecyclerViewLayout()
                filterAndDisplayApps()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    /**
     * 设置分类标签
     */
    private fun setupCategoryChips() {
        AppCategory.values().forEach { category ->
            val chip = Chip(this).apply {
                text = category.displayName
                isCheckable = true
                isChecked = (category == AppCategory.ALL)
                
                setOnClickListener {
                    currentCategory = category
                    filterAndDisplayApps()
                }
            }
            binding.categoryChipGroup.addView(chip)
        }
    }

    /**
     * 设置搜索栏
     */
    private fun setupSearchBar() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s.toString().trim().toLowerCase()
                filterAndDisplayApps()
            }
        })
    }

    /**
     * 更新RecyclerView布局
     */
    private fun updateRecyclerViewLayout() {
        val useListLayout = currentBrand.useListLayout
        
        // 创建新适配器
        appAdapter = AppAdapter(useListLayout) { appInfo ->
            launchApp(appInfo)
        }
        
        // 设置布局管理器
        binding.appsRecyclerView.layoutManager = if (useListLayout) {
            LinearLayoutManager(this)
        } else {
            GridLayoutManager(this, currentBrand.spanCount)
        }
        
        binding.appsRecyclerView.adapter = appAdapter
    }

    /**
     * 加载已安装的应用
     */
    private fun loadInstalledApps() {
        binding.progressBar.visibility = View.VISIBLE
        binding.appsRecyclerView.visibility = View.GONE
        
        lifecycleScope.launch {
            allApps = withContext(Dispatchers.IO) {
                getInstalledApps()
            }
            
            binding.progressBar.visibility = View.GONE
            binding.appsRecyclerView.visibility = View.VISIBLE
            
            updateRecyclerViewLayout()
            filterAndDisplayApps()
        }
    }

    /**
     * 获取所有已安装的应用
     */
    private fun getInstalledApps(): List<AppInfo> {
        val pm = packageManager
        val apps = mutableListOf<AppInfo>()
        
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        
        for (appInfo in installedApps) {
            // 过滤掉系统核心应用
            if (AppClassifier.isSystemApp(appInfo.packageName)) {
                continue
            }
            
            try {
                val appName = pm.getApplicationLabel(appInfo).toString()
                val icon = pm.getApplicationIcon(appInfo)
                val packageName = appInfo.packageName
                
                // 自动分类
                val categories = AppClassifier.classify(packageName, appName).toMutableList()
                
                apps.add(AppInfo(packageName, appName, icon, categories))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        // 按应用名称排序
        return apps.sortedBy { it.appName }
    }

    /**
     * 过滤并显示应用
     */
    private fun filterAndDisplayApps() {
        filteredApps = allApps.filter { app ->
            // 分类过滤
            val categoryMatch = if (currentCategory == AppCategory.ALL) {
                true
            } else {
                app.categories.contains(currentCategory)
            }
            
            // 搜索过滤
            val searchMatch = if (searchQuery.isEmpty()) {
                true
            } else {
                app.appName.toLowerCase().contains(searchQuery) ||
                app.packageName.toLowerCase().contains(searchQuery)
            }
            
            categoryMatch && searchMatch
        }
        
        // 更新UI
        if (filteredApps.isEmpty()) {
            binding.appsRecyclerView.visibility = View.GONE
            binding.emptyView.visibility = View.VISIBLE
        } else {
            binding.appsRecyclerView.visibility = View.VISIBLE
            binding.emptyView.visibility = View.GONE
            appAdapter.submitList(filteredApps)
        }
    }

    /**
     * 启动应用
     */
    private fun launchApp(appInfo: AppInfo) {
        try {
            val intent = packageManager.getLaunchIntentForPackage(appInfo.packageName)
            if (intent != null) {
                startActivity(intent)
            } else {
                Toast.makeText(this, "无法打开 ${appInfo.appName}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "启动失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * ViewBinding 辅助类
 */
class ActivityMainBinding private constructor(
    val root: View,
    val toolbar: com.google.android.material.appbar.MaterialToolbar,
    val brandSpinner: android.widget.Spinner,
    val searchEditText: android.widget.EditText,
    val categoryChipGroup: com.google.android.material.chip.ChipGroup,
    val appsRecyclerView: androidx.recyclerview.widget.RecyclerView,
    val progressBar: android.widget.ProgressBar,
    val emptyView: android.widget.LinearLayout
) {
    companion object {
        fun inflate(inflater: android.view.LayoutInflater): ActivityMainBinding {
            val root = inflater.inflate(R.layout.activity_main, null)
            return bind(root)
        }
        
        fun bind(root: View): ActivityMainBinding {
            return ActivityMainBinding(
                root = root,
                toolbar = root.findViewById(R.id.toolbar),
                brandSpinner = root.findViewById(R.id.brandSpinner),
                searchEditText = root.findViewById(R.id.searchEditText),
                categoryChipGroup = root.findViewById(R.id.categoryChipGroup),
                appsRecyclerView = root.findViewById(R.id.appsRecyclerView),
                progressBar = root.findViewById(R.id.progressBar),
                emptyView = root.findViewById(R.id.emptyView)
            )
        }
    }
}
