package com.github.capntrips.kernelflasher.ui.screens.slot

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.github.capntrips.kernelflasher.R
import com.github.capntrips.kernelflasher.ui.components.DataCard

@ExperimentalUnitApi
@ExperimentalMaterial3Api
@Composable
fun ColumnScope.SlotFlashContent(
    viewModel: SlotViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    if (navController.currentDestination!!.route == "slot{slotSuffix}/flash" && !viewModel.isRefreshing && !viewModel.wasFlashed) {
        viewModel.flash(context)
    }
    val filteredOutput = viewModel.flashOutput.filter { it.startsWith("ui_print") }
    val listState = rememberLazyListState()
    var hasDragged by remember { mutableStateOf(false) }
    val isDragged by listState.interactionSource.collectIsDraggedAsState()
    if (isDragged) {
        hasDragged = true
    }
    var shouldScroll = false
    if (!hasDragged) {
        if (listState.layoutInfo.visibleItemsInfo.firstOrNull()?.index != null) {
            if (listState.layoutInfo.totalItemsCount - listState.layoutInfo.visibleItemsInfo.size > listState.layoutInfo.visibleItemsInfo.firstOrNull()?.index!!) {
                shouldScroll = true
            }
        }
    }
    LaunchedEffect(shouldScroll) {
        listState.animateScrollToItem(filteredOutput.size)
    }
    DataCard (title = stringResource(R.string.flash))
    LazyColumn(
        Modifier
            .weight(1.0f)
            .fillMaxSize()
            .scrollbar(listState),
        listState
    ) {
        items(filteredOutput) { message ->
            Text(message.substring("ui_print".length + 1),
                style = LocalTextStyle.current.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = TextUnit(12.0f, TextUnitType.Sp),
                    lineHeight = TextUnit(18.0f, TextUnitType.Sp)
                )
            )
        }
    }
    AnimatedVisibility(!viewModel.isRefreshing && viewModel.wasFlashed) {
        Column {
            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                onClick = { viewModel.saveLog(context) }
            ) {
                Text(stringResource(R.string.save_ak3_log))
            }
            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                onClick = { viewModel.reboot() }
            ) {
                Text(stringResource(R.string.reboot))
            }
        }
    }
}

// https://stackoverflow.com/a/68056586/434343
@Composable
fun Modifier.scrollbar(
    state: LazyListState,
    width: Dp = 6.dp
): Modifier {
    var visibleItemsCountChanged = false
    var visibleItemsCount by remember { mutableStateOf(state.layoutInfo.visibleItemsInfo.size) }
    if (visibleItemsCount != state.layoutInfo.visibleItemsInfo.size) {
        visibleItemsCountChanged = true
        @Suppress("UNUSED_VALUE")
        visibleItemsCount = state.layoutInfo.visibleItemsInfo.size
    }

    val hidden = state.layoutInfo.visibleItemsInfo.size == state.layoutInfo.totalItemsCount
    val targetAlpha = if (!hidden && (state.isScrollInProgress || visibleItemsCountChanged)) 0.5f else 0f
    val delay = if (!hidden && (state.isScrollInProgress || visibleItemsCountChanged)) 0 else 250
    val duration = if (hidden || visibleItemsCountChanged) 0 else if (state.isScrollInProgress) 150 else 500

    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(delayMillis = delay, durationMillis = duration)
    )

    return drawWithContent {
        drawContent()

        val firstVisibleElementIndex = state.layoutInfo.visibleItemsInfo.firstOrNull()?.index
        val needDrawScrollbar = state.isScrollInProgress || visibleItemsCountChanged || alpha > 0.0f

        if (needDrawScrollbar && firstVisibleElementIndex != null) {
            val elementHeight = this.size.height / state.layoutInfo.totalItemsCount
            val scrollbarOffsetY = firstVisibleElementIndex * elementHeight
            val scrollbarHeight = state.layoutInfo.visibleItemsInfo.size * elementHeight

            drawRoundRect(
                color = Color.Gray,
                topLeft = Offset(this.size.width - width.toPx(), scrollbarOffsetY),
                size = Size(width.toPx(), scrollbarHeight),
                cornerRadius = CornerRadius(width.toPx(), width.toPx()),
                alpha = alpha
            )
        }
    }
}
