package com.example.exoplayerkotlindemo.ui

import io.reactivex.rxjava3.subjects.PublishSubject

enum class AppState{
    resume,
    pause,
    destroy,
}

object FragmentStateForVideo {
    val appStateObserver: PublishSubject<AppState> = PublishSubject.create();

    fun changeAppState(state: AppState) {
        appStateObserver.onNext(state)
    }

}