package com.example.tictactoecustomviewapp.Object

import android.graphics.Rect

enum class Player(val symbol: String) {
    O("0"),
    X("X"),
    NONE(""),
}

data class TicTacToeItem(val rect: Rect, var player: Player)