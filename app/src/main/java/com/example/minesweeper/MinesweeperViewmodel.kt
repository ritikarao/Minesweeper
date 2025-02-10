package com.example.minesweeper

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

class MinesweeperViewmodel(): ViewModel() {
    private val _boxList: MutableStateFlow<MutableList<MutableList<MutableStateFlow<Box>>>> = MutableStateFlow(MutableList(NUMBER_OF_ROWS, { MutableList(NUMBER_OF_COLUMNS, { MutableStateFlow(Box()) }) }))
    val boxList: StateFlow<MutableList<MutableList<MutableStateFlow<Box>>>> = _boxList.asStateFlow()

    init {
        setUpGrid()
    }

    fun setUpGrid() {
        viewModelScope.launch {
            val list = MutableList(NUMBER_OF_ROWS * NUMBER_OF_COLUMNS, { false })
            for (i in 0..<NUMBER_OF_POTHOLES) {
                list[i] = true
            }
            list.shuffle()

            for (i in 0..< NUMBER_OF_ROWS) {
                for (j in 0..<NUMBER_OF_COLUMNS) {
                    val element = MutableStateFlow(Box())
                    element.emit(
                        element.value.copy(
                            isPothole = list[(i * NUMBER_OF_COLUMNS) + j],
                            color = if (IS_DEBUG && list[(i * NUMBER_OF_COLUMNS) + j]) Color.Red else Color.White,
                            adjacentPotholes = countAdjacentPotholes(i, j, list)
                        )
                    )
                    _boxList.value[i][j].emit(
                        element.value
                    )
                }
            }
        }
    }

    private fun countAdjacentPotholes(row: Int, col: Int, list: MutableList<Boolean>): Int {
        var adjacentPotholes = 0

        val startRowIndex = max(0, row - 1)
        val endRowIndex = min(row + 1, NUMBER_OF_ROWS - 1)
        val startColIndex = max(0, col - 1)
        val endColIndex = min(col + 1, NUMBER_OF_COLUMNS - 1)

        for (i in startRowIndex..endRowIndex) {
            for (j in startColIndex..endColIndex) {
                if (list[(i * NUMBER_OF_COLUMNS) + j]) {
                    adjacentPotholes++
                }
            }
        }

        return adjacentPotholes
    }

    fun updateBoxClickedAndFlaggedState(row: Int, col: Int) {
        viewModelScope.launch {
            val currentBox = boxList.value[row][col]
            _boxList.value[row][col].emit(
                _boxList.value[row][col].value.copy(
                    isFlagged = currentBox.value.isFlagged.not(),
                    isClickable = currentBox.value.isClickable.not()
                )
            )
        }
    }

    fun updateClickedBox(row: Int, col: Int) {
        viewModelScope.launch {
            _boxList.value[row][col].emit(
                _boxList.value[row][col].value.copy(
                    color = Color.Gray,
                    isClickable = false,
                    shouldDisplayPotholes = true
                )
            )
        }
    }
}

data class Box(
    var isPothole: Boolean = false,
    var color: Color = Color.White,
    var adjacentPotholes: Int = 0,
    var isFlagged: Boolean = false,
    var isClickable: Boolean = true,
    var shouldDisplayPotholes: Boolean = false
)