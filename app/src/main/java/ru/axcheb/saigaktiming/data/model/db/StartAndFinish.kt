package ru.axcheb.saigaktiming.data.model.db

import androidx.room.Embedded

data class StartAndFinish (

    @Embedded(prefix = "start_") val start: Start,
    @Embedded(prefix = "finish_") val finish: Finish

)