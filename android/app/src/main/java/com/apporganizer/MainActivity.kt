package com.apporganizer

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var detectedBrand = ""
    private var currentBrand = BrandStyle.XIAOMI
    private var currentPreference = OrganizePreference.GENERAL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 设置返回按钮
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        
        // 检测手机品牌
        detectPhoneBrand()
        
        // 设置品牌选择器
        setupBrandSelector()
        
        // 设置整理偏好选择器
        setupPreferenceSelector()
        
        // 设置整理按钮
        setupOrganizeButton()
        
        // 加载已安装的应用
        loadInstalledApps()
    }

    /**
     * 加载已安装的应用
     */
    private fun loadInstalledApps() {
        binding.progressBar.visibility = View.VISIBLE
        binding.organizeButton.isEnabled = false
        
        lifecycleScope.launch {
            AppData.allApps = withContext(Dispatchers.IO) {
                val apps = getInstalledApps()
                // 打印应用数量日志
                println("MainActivity - 加载的应用数量: ${apps.size}")
                apps
            }
            
            binding.progressBar.visibility = View.GONE
            binding.organizeButton.isEnabled = true
            binding.appCountText.text = AppData.allApps.size.toString()
            
            // 打印应用列表日志
            println("MainActivity - AppData.allApps 大小: ${AppData.allApps.size}")
        }
    }

    /**
     * 检测手机品牌
     */
    private fun detectPhoneBrand() {
        detectedBrand = Build.BRAND.uppercase()
        val displayBrand = when {
            detectedBrand.contains("XIAOMI") || detectedBrand.contains("REDMI") -> "小米"
            detectedBrand.contains("HUAWEI") || detectedBrand.contains("HONOR") -> "华为"
            detectedBrand.contains("OPPO") -> "OPPO"
            detectedBrand.contains("VIVO") -> "vivo"
            detectedBrand.contains("SAMSUNG") -> "三星"
            else -> detectedBrand
        }
        
        binding.detectedBrandText.text = "已检测: $displayBrand"
        
        // 自动选择对应品牌
        val brandIndex = when {
            detectedBrand.contains("XIAOMI") || detectedBrand.contains("REDMI") -> 0
            detectedBrand.contains("HUAWEI") || detectedBrand.contains("HONOR") -> 1
            detectedBrand.contains("OPPO") -> 2
            detectedBrand.contains("VIVO") -> 3
            else -> 4 // iPhone风格作为默认
        }
        binding.brandSpinner.setSelection(brandIndex)
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
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    /**
     * 设置整理偏好选择器
     */
    private fun setupPreferenceSelector() {
        val preferences = OrganizePreference.values().map { it.displayName }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, preferences)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        
        binding.preferenceSpinner.adapter = adapter
        binding.preferenceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentPreference = OrganizePreference.values()[position]
                binding.preferenceDescText.text = currentPreference.description
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    /**
     * 设置整理按钮
     */
    private fun setupOrganizeButton() {
        binding.organizeButton.setOnClickListener { 
            if (AppData.allApps.isEmpty()) {
                Toast.makeText(this, "正在加载应用，请稍候...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // 跳转到整理结果页面
            val intent = Intent(this, OrganizeResultActivity::class.java)
            intent.putExtra("brand", currentBrand.name)
            intent.putExtra("preference", currentPreference.name)
            startActivity(intent)
        }
    }

    /**
     * 获取所有已安装的应用
     */
    private fun getInstalledApps(): List<AppInfo> {
        val pm = packageManager
        val apps = mutableListOf<AppInfo>()
        
        // 使用更宽松的标志获取应用，确保获取所有已安装的应用
        val installedApps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledApplications(
                PackageManager.ApplicationInfoFlags.of(
                    0L // 不使用任何标志，获取所有应用
                )
            )
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledApplications(0) // 不使用任何标志，获取所有应用
        }
        
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
}

/**
 * ViewBinding 辅助类
 */
class ActivityMainBinding private constructor(
    val root: View,
    val toolbar: com.google.android.material.appbar.MaterialToolbar,
    val detectedBrandText: android.widget.TextView,
    val brandSpinner: android.widget.Spinner,
    val preferenceSpinner: android.widget.Spinner,
    val preferenceDescText: android.widget.TextView,
    val appCountText: android.widget.TextView,
    val organizeButton: com.google.android.material.button.MaterialButton,
    val progressBar: android.widget.ProgressBar
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
                detectedBrandText = root.findViewById(R.id.detectedBrandText),
                brandSpinner = root.findViewById(R.id.brandSpinner),
                preferenceSpinner = root.findViewById(R.id.preferenceSpinner),
                preferenceDescText = root.findViewById(R.id.preferenceDescText),
                appCountText = root.findViewById(R.id.appCountText),
                organizeButton = root.findViewById(R.id.organizeButton),
                progressBar = root.findViewById(R.id.progressBar)
            )
        }
    }
}







