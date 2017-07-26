package cz.widget

import android.util.Log
import android.view.View

/**
 * Created by woodys on 2017/7/23.
 */
val DEBUG = false

inline fun View.debugLog(message: String) {
    if (DEBUG) {
        Log.e(this::class.java.simpleName, message)
    }
}