package com.apporganizer

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.*

/**
 * 应用整理辅助功能服务
 * 用于自动创建文件夹并移动应用到对应文件夹
 */
class AppOrganizerAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "AppOrganizerService"
        private const val ACTION_START_ORGANIZE = "com.apporganizer.START_ORGANIZE"
        private const val ACTION_STOP_ORGANIZE = "com.apporganizer.STOP_ORGANIZE"
        const val ACTION_ORGANIZE_APPS = "com.apporganizer.ORGANIZE_APPS"
        
        private var instance: AppOrganizerAccessibilityService? = null
        
        fun getInstance(): AppOrganizerAccessibilityService? = instance
        
        fun isServiceEnabled(): Boolean = instance != null
    }

    private val handler = Handler(Looper.getMainLooper())
    private var organizeJob: Job? = null
    private var isOrganizing = false

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.d(TAG, "AppOrganizerAccessibilityService created")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        organizeJob?.cancel()
        Log.d(TAG, "AppOrganizerAccessibilityService destroyed")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 不需要处理特定事件，因为我们通过手势操作
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted")
        stopOrganizing()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let { action ->
            when (action) {
                ACTION_START_ORGANIZE -> {
                    val folders = intent.getSerializableExtra("folders") as? List<FolderInfo>
                    if (folders != null) {
                        startOrganizing(folders)
                    }
                }
                ACTION_ORGANIZE_APPS -> {
                    val foldersData = intent.getParcelableArrayListExtra<Bundle>("folders")
                    if (foldersData != null) {
                        val folders = parseFoldersData(foldersData)
                        startOrganizing(folders)
                    }
                }
                ACTION_STOP_ORGANIZE -> {
                    stopOrganizing()
                }
            }
        }
        return START_STICKY
    }

    /**
     * 解析文件夹数据
     */
    private fun parseFoldersData(foldersData: ArrayList<Bundle>): List<FolderInfo> {
        return foldersData.mapNotNull { bundle ->
            val categoryName = bundle.getString("category") ?: return@mapNotNull null
            val appPackages = bundle.getStringArrayList("apps") ?: return@mapNotNull null
            
            val category = try {
                AppCategory.valueOf(categoryName)
            } catch (e: Exception) {
                AppCategory.OTHER
            }
            
            val apps = appPackages.mapNotNull { packageName ->
                AppData.allApps.find { it.packageName == packageName }
            }
            
            FolderInfo(category, apps)
        }
    }

    /**
     * 开始整理应用
     */
    private fun startOrganizing(folders: List<FolderInfo>) {
        if (isOrganizing) {
            Log.w(TAG, "Already organizing")
            return
        }
        
        isOrganizing = true
        Log.d(TAG, "Starting organize with ${folders.size} folders")
        
        organizeJob = CoroutineScope(Dispatchers.Main).launch {
            try {
                // 等待用户回到桌面
                delay(2000)
                
                // 开始创建文件夹并移动应用
                for (folder in folders) {
                    if (!isOrganizing) break
                    
                    createFolderAndMoveApps(folder)
                    delay(1000) // 每个文件夹之间延迟1秒
                }
                
                Log.d(TAG, "Organizing completed")
            } catch (e: Exception) {
                Log.e(TAG, "Error during organizing", e)
            } finally {
                isOrganizing = false
            }
        }
    }

    /**
     * 停止整理
     */
    private fun stopOrganizing() {
        isOrganizing = false
        organizeJob?.cancel()
        Log.d(TAG, "Organizing stopped")
    }

    /**
     * 创建文件夹并移动应用
     */
    private suspend fun createFolderAndMoveApps(folder: FolderInfo) {
        Log.d(TAG, "Creating folder: ${folder.category.displayName} with ${folder.apps.size} apps")
        
        if (folder.apps.isEmpty()) return
        
        // 获取桌面根节点
        val rootNode = rootInActiveWindow ?: run {
            Log.e(TAG, "Root node is null")
            return
        }
        
        // 查找第一个应用图标
        val firstAppNode = findAppIcon(rootNode, folder.apps[0].appName)
        if (firstAppNode == null) {
            Log.w(TAG, "First app icon not found: ${folder.apps[0].appName}")
            return
        }
        
        // 获取第一个应用图标的位置
        val firstAppBounds = Rect()
        firstAppNode.getBoundsInScreen(firstAppBounds)
        
        // 查找第二个应用图标
        val secondAppNode = if (folder.apps.size > 1) {
            findAppIcon(rootNode, folder.apps[1].appName)
        } else null
        
        if (secondAppNode != null) {
            // 将第一个应用拖动到第二个应用上创建文件夹
            val secondAppBounds = Rect()
            secondAppNode.getBoundsInScreen(secondAppBounds)
            
            // 执行拖动手势
            performDragGesture(
                firstAppBounds.centerX().toFloat(),
                firstAppBounds.centerY().toFloat(),
                secondAppBounds.centerX().toFloat(),
                secondAppBounds.centerY().toFloat()
            )
            
            delay(1500) // 等待文件夹创建完成
            
            // 移动剩余的应用到文件夹
            for (i in 2 until folder.apps.size) {
                if (!isOrganizing) break
                
                val appNode = findAppIcon(rootNode, folder.apps[i].appName)
                if (appNode != null) {
                    val appBounds = Rect()
                    appNode.getBoundsInScreen(appBounds)
                    
                    // 将应用拖动到文件夹位置（使用第二个应用的位置作为文件夹位置）
                    performDragGesture(
                        appBounds.centerX().toFloat(),
                        appBounds.centerY().toFloat(),
                        secondAppBounds.centerX().toFloat(),
                        secondAppBounds.centerY().toFloat()
                    )
                    
                    delay(800) // 每个应用之间延迟
                }
            }
        }
    }

    /**
     * 查找应用图标节点
     */
    private fun findAppIcon(rootNode: AccessibilityNodeInfo, appName: String): AccessibilityNodeInfo? {
        val nodes = rootNode.findAccessibilityNodeInfosByText(appName)
        return nodes.firstOrNull { node ->
            node.text?.toString()?.equals(appName, ignoreCase = true) == true
        }
    }

    /**
     * 执行拖动手势
     */
    private fun performDragGesture(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float
    ) {
        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        
        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 1000))
        
        val gesture = gestureBuilder.build()
        dispatchGesture(gesture, null, null)
        
        Log.d(TAG, "Performed drag gesture: ($startX, $startY) -> ($endX, $endY)")
    }

    /**
     * 获取服务配置
     */
    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Accessibility service connected")
    }
}