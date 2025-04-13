package com.anxietystressselfmanagement

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel for the Exercises screen that handles business logic and navigation events
 * Uses LiveData for reactive UI updates and follows MVVM architecture
 */
class ExercisesViewModel : ViewModel() {

    // MutableLiveData for internal use
    private val _navigationEvent = MutableLiveData<NavigationEvent>()

    // Public LiveData exposed to the UI
    val navigationEvent: LiveData<NavigationEvent> = _navigationEvent

    /**
     * Trigger navigation to a specific exercise
     * @param destination The exercise type to navigate to
     */
    fun navigateToExercise(destination: ExerciseDestination) {
        _navigationEvent.value = NavigationEvent.NavigateToExercise(destination)
    }

    /**
     * Trigger navigation back to home screen
     */
    fun navigateBack() {
        _navigationEvent.value = NavigationEvent.NavigateBack
    }

    /**
     * Sealed class representing different navigation events
     */
    sealed class NavigationEvent {
        data class NavigateToExercise(val destination: ExerciseDestination) : NavigationEvent()
        object NavigateBack : NavigationEvent()
    }

    /**
     * Enum representing exercise destinations
     */
    enum class ExerciseDestination {
        SLEEP,
        DESTRESS,
        FOCUS,
        PSYCH_SIGH
    }
}