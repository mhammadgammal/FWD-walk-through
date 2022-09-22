/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.*
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
    val database: SleepDatabaseDao,
    application: Application
) : AndroidViewModel(application) {

    private val tonight = MutableLiveData<SleepNight?>()
    private val nights = database.getAllNights()
    private val _navigateToSleepQuality = MutableLiveData<SleepNight?>()
    val navigateToSleepQuality: LiveData<SleepNight?>
            get() = _navigateToSleepQuality

    val startButtonVisible = Transformations.map(tonight){
        it == null
    }

    val stopButtonVisible = Transformations.map(tonight){
        it != null
    }

    val clearButtonVisible = Transformations.map(nights){
        it?.isNotEmpty()
    }

    private val _showSnackbarEvent = MutableLiveData<Boolean>()
    val showSnackbarEvent:LiveData<Boolean>
        get() = _showSnackbarEvent

    fun doneShowingbar(){
        _showSnackbarEvent.value = false
    }
    init {
        initializeTonight()
    }

    val nightString = Transformations.map(nights) {
        formatNights(it, application.resources)
    }

    private fun initializeTonight() {
        viewModelScope.launch {
            var night: SleepNight?
            withContext(Dispatchers.IO){
                night = getTonightFromDatabase()
            }
            tonight.value = night
        }
    }

    private suspend fun getTonightFromDatabase(): SleepNight? {
        var night = database.getTonight()
        if (night?.endTimeMilli != night?.startTimeMilli) {
            night = null
        }
        return night
    }

    //Handling start BTN
    fun onStartTracking() {

        viewModelScope.launch {
            var night:SleepNight?
            withContext(Dispatchers.IO){
                night = getTonightFromDatabase()
                val newNight = SleepNight()
                insertNight(newNight)
            }
            tonight.value = night

        }
    }

    private suspend fun insertNight(newNight: SleepNight) {
        database.insertNight(newNight)
    }

    //Handling stop BTN
    fun onStopTracking() {
        viewModelScope.launch {
            var oldNight: SleepNight? = null
            withContext(Dispatchers.Default){
                oldNight = tonight.value ?: return@withContext
                oldNight?.endTimeMilli = System.currentTimeMillis()
                update(oldNight!!)

            }
            _navigateToSleepQuality.value = oldNight
        }
    }

    private suspend fun update(oldNight: SleepNight) {
        database.updateNight(oldNight)
    }

    fun doneNavigating(){
        _navigateToSleepQuality.value = null
    }

    //Handling clear BTN
    fun onCLear() {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                clear()

            }
            tonight.value = null
            _showSnackbarEvent.value = true
        }
    }

    private suspend fun clear() {
        database.daleteNights()
    }

}
