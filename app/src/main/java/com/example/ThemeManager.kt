package com.starrynightstudio.dezhmessenger.xwpqrs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_settings")

enum class DezhTheme(val displayName: String) {
    SIMPLE_DARK("Simple Dark"),
    LIGHT_MODE("Light Mode"),
    CYBER("Cyber-Like"),
    PINKY("Pinky"),
    OBSIDIAN("Obsidian"),
    AMBER("Amber"),
    RGB_GAMING("RGB Gaming"),
    GALAXY("Galaxy"),
    EMERALD("Emerald")
}

object ThemeManager {
    private val ACTIVE_THEME_KEY = stringPreferencesKey("active_theme")
    private val IS_OBSIDIAN_UNLOCKED_KEY = booleanPreferencesKey("is_obsidian_unlocked")
    private val IS_AMBER_UNLOCKED_KEY = booleanPreferencesKey("is_amber_unlocked")
    private val IS_RGB_UNLOCKED_KEY = booleanPreferencesKey("is_rgb_unlocked")

    fun getActiveTheme(context: Context): Flow<DezhTheme> {
        return context.themeDataStore.data.map { preferences ->
            val themeStr = preferences[ACTIVE_THEME_KEY] ?: DezhTheme.SIMPLE_DARK.name
            try {
                DezhTheme.valueOf(themeStr)
            } catch (e: Exception) {
                DezhTheme.SIMPLE_DARK
            }
        }
    }

    suspend fun setActiveTheme(context: Context, theme: DezhTheme) {
        context.themeDataStore.edit { preferences ->
            preferences[ACTIVE_THEME_KEY] = theme.name
        }
    }

    suspend fun isThemeUnlocked(context: Context, theme: DezhTheme): Boolean {
        return isThemeUnlockedFlow(context, theme).first()
    }

    fun isThemeUnlockedFlow(context: Context, theme: DezhTheme): Flow<Boolean> {
        return context.themeDataStore.data.map { preferences ->
            when (theme) {
                DezhTheme.OBSIDIAN -> preferences[IS_OBSIDIAN_UNLOCKED_KEY] ?: false
                DezhTheme.AMBER -> preferences[IS_AMBER_UNLOCKED_KEY] ?: false
                DezhTheme.RGB_GAMING -> preferences[IS_RGB_UNLOCKED_KEY] ?: false
                else -> true
            }
        }
    }

    suspend fun unlockTheme(context: Context, theme: DezhTheme) {
        context.themeDataStore.edit { preferences ->
            when (theme) {
                DezhTheme.OBSIDIAN -> preferences[IS_OBSIDIAN_UNLOCKED_KEY] = true
                DezhTheme.AMBER -> preferences[IS_AMBER_UNLOCKED_KEY] = true
                DezhTheme.RGB_GAMING -> preferences[IS_RGB_UNLOCKED_KEY] = true
                else -> {}
            }
        }
    }

    fun getUnlockedThemes(context: Context): Flow<Set<DezhTheme>> {
        return context.themeDataStore.data.map { preferences ->
            val set = mutableSetOf(
                DezhTheme.SIMPLE_DARK,
                DezhTheme.LIGHT_MODE,
                DezhTheme.CYBER,
                DezhTheme.PINKY,
                DezhTheme.GALAXY,
                DezhTheme.EMERALD
            )
            if (preferences[IS_OBSIDIAN_UNLOCKED_KEY] == true) set.add(DezhTheme.OBSIDIAN)
            if (preferences[IS_AMBER_UNLOCKED_KEY] == true) set.add(DezhTheme.AMBER)
            if (preferences[IS_RGB_UNLOCKED_KEY] == true) set.add(DezhTheme.RGB_GAMING)
            set
        }
    }
}
