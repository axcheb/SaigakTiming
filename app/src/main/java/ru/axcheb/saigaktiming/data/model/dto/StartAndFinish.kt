package ru.axcheb.saigaktiming.data.model.dto

import androidx.room.Embedded

data class StartAndFinish (

    @Embedded(prefix = "start_") val start: Start,
    @Embedded(prefix = "finish_") val finish: Finish

)