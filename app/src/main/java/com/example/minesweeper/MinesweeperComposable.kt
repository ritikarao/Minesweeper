package com.example.minesweeper

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min

const val NUMBER_OF_ROWS = 20
const val NUMBER_OF_COLUMNS = 10
const val NUMBER_OF_POTHOLES = 10
const val IS_DEBUG = true

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MinesweeperComposable(
    innerPadding: PaddingValues
) {
    val viewModel = MinesweeperViewmodel()
    val boxList by viewModel.boxList.collectAsState()


    var displayDialog by remember { mutableStateOf(false) }
    if (displayDialog) {
        GameOverDialog(
            onDismissRequest = {
                displayDialog = false
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding()
            ),
        contentAlignment = Alignment.Center
    ) {
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            columns = GridCells.Fixed(count = NUMBER_OF_COLUMNS)
        ) {
            for (i in 0..< NUMBER_OF_ROWS) {
                for (j in 0..<NUMBER_OF_COLUMNS) {
                    item {
                        val box by boxList[i][j].collectAsState()

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .fillMaxSize()
                                .border(width = 2.dp, color = Color.Black)
                                .background(box.color)
                                .combinedClickable(
                                    onClick = {
                                        if (box.isClickable) {
                                            if (box.isPothole) {
                                                displayDialog = true
                                            } else {
                                                viewModel.updateClickedBox(i, j)
                                                if (box.adjacentPotholes == 0) {
                                                    val startRowIndex = max(0, i - 1)
                                                    val endRowIndex = min(i + 1, NUMBER_OF_ROWS - 1)
                                                    val startColIndex = max(0, j - 1)
                                                    val endColIndex = min(j + 1, NUMBER_OF_COLUMNS - 1)

                                                    for (row in startRowIndex..endRowIndex) {
                                                        for (col in startColIndex..endColIndex) {
                                                            viewModel.updateClickedBox(row,col)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    onLongClick = {
                                        viewModel.updateBoxClickedAndFlaggedState(i, j)
                                    }
                                )
                        ) {
                            if (box.isFlagged) {
                                Text(
                                    modifier = Modifier.align(Alignment.Center),
                                    text = "F",
                                    fontSize = 30.sp
                                )
                            }
                            if (box.shouldDisplayPotholes) {
                                Text(
                                    modifier = Modifier.align(Alignment.Center),
                                    text = box.adjacentPotholes.toString(),
                                    fontSize = 30.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameOverDialog(
    onDismissRequest: (() -> Unit)
) {
    BasicAlertDialog(
        onDismissRequest = {
            onDismissRequest.invoke()
        }
    ) {
        Column(
            modifier = Modifier
                .background(Color.White)
                .padding(10.dp)
        ) {
            Text(
                text = "Game over",
                fontWeight = FontWeight.Bold
            )
            Text("You ran over a pothole!")
            Button(
                modifier = Modifier.align(Alignment.End),
                shape = RoundedCornerShape(4.dp),
                onClick = {
                    onDismissRequest.invoke()
                }
            ) { Text("Reset") }
        }
    }
}