package com.starrynightstudio.dezhmessenger.xwpqrs

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object TriggerService {
    private var obsidianTapCount = 0
    private var lastTapTime = 0L
    private var languageSwitchCount = 0

    fun onAppTitleClicked(context: Context, scope: CoroutineScope, onUnlocked: () -> Unit) {
        val now = System.currentTimeMillis()
        if (now - lastTapTime < 600) {
            obsidianTapCount++
        } else {
            obsidianTapCount = 1
        }
        lastTapTime = now

        if (obsidianTapCount >= 5) {
            obsidianTapCount = 0
            scope.launch {
                val isAlreadyUnlocked = ThemeManager.isThemeUnlocked(context, DezhTheme.OBSIDIAN)
                if (!isAlreadyUnlocked) {
                    ThemeManager.unlockTheme(context, DezhTheme.OBSIDIAN)
                    onUnlocked()
                }
            }
        }
    }

    fun onLanguageSwitched(context: Context, scope: CoroutineScope, onUnlocked: () -> Unit) {
        languageSwitchCount++
        if (languageSwitchCount >= 4) {
            languageSwitchCount = 0
            scope.launch {
                val isAlreadyUnlocked = ThemeManager.isThemeUnlocked(context, DezhTheme.AMBER)
                if (!isAlreadyUnlocked) {
                    ThemeManager.unlockTheme(context, DezhTheme.AMBER)
                    onUnlocked()
                }
            }
        }
    }

    fun handleIncomingSecretFile(intent: Intent?): Boolean {
        if (intent == null) return false
        val action = intent.action
        val data: Uri? = intent.data
        if (Intent.ACTION_VIEW == action && data != null) {
            val path = data.path ?: ""
            if (path.endsWith(".dezhsecret") || data.toString().contains(".dezhsecret")) {
                return true
            }
        }
        return false
    }

    fun verifySecretPassword(password: String): Boolean {
        val clean = password.trim()
        return clean.equals("owl", ignoreCase = true) || clean.equals("joghd", ignoreCase = true) || clean.equals("starrynight", ignoreCase = true)
    }

    suspend fun unlockRgbTheme(context: Context) {
        ThemeManager.unlockTheme(context, DezhTheme.RGB_GAMING)
    }
}
