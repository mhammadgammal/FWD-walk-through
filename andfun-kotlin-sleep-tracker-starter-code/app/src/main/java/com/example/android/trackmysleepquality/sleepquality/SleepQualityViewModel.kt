package com.example.android.trackmysleepquality.sleepquality

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SleepQualityViewModel(
    val database: SleepDatabaseDao,
    private val sleepNightKey: Long = 0L
): ViewModel() {
    private val _navToSleepTracker = MutableLiveData<Boolean?>()
    val navToSleepQuality: LiveData<Boolean?>
        get() = _navToSleepTracker
    fun doneNavigating(){
        _navToSleepTracker.value = false
    }
    //Quality Images Click Handler
    fun onSetSleepQuality(quality: Int){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                val tonight = database.getNight(sleepNightKey) ?: return@withContext
                tonight.sleepQuality = quality
                database.updateNight(tonight)
            }
            withContext(Dispatchers.Main){
                _navToSleepTracker.value = true

            }
        }

    }
}