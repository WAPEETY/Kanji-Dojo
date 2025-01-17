package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.ui

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.syt0r.kanji.R
import ua.syt0r.kanji.presentation.common.theme.AppTheme
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.data.RadicalSearchListItem
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.data.RadicalSearchState
import java.util.*
import kotlin.math.ceil
import kotlin.math.roundToInt

private val TextSize = 20.sp
private val ItemHeight: Dp
    @Composable
    get() {
        val value = TextSize.value * 2f * LocalDensity.current.fontScale
        return value.dp
    }

@Composable
fun RadicalSearch(
    state: State<RadicalSearchState>,
    selectedRadicals: MutableState<Set<String>>,
    height: State<Dp>,
    onCharacterClick: (String) -> Unit
) {

    val charactersSectionDataState = remember { derivedStateOf { state.value.characterListItems } }
    val radicalsSectionDataState = remember { derivedStateOf { state.value.radicalsListItems } }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(height.value)
    ) {

        DraggableContainerIndicator()
        Header(selectedRadicals)
        LoadingIndicator(loadingState = remember { derivedStateOf { state.value.isLoading } })

        val configuration = LocalConfiguration.current
        if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {

            Column {

                Text(
                    text = stringResource(R.string.search_radical_found_chars_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )

                CharactersLine(
                    itemsState = charactersSectionDataState,
                    onClick = onCharacterClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp)
                )

                Text(
                    text = stringResource(R.string.search_radical_radicals_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )

                CharactersGrid(
                    items = radicalsSectionDataState,
                    onClick = {
                        selectedRadicals.value = selectedRadicals.value.run {
                            if (contains(it)) minus(it) else plus(it)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

            }

        } else {

            Row(Modifier.fillMaxSize()) {

                Column(modifier = Modifier.weight(1f)) {

                    Text(
                        text = stringResource(R.string.search_radical_found_chars_title),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )

                    if (charactersSectionDataState.value.isEmpty()) {
                        Text(
                            text = stringResource(R.string.search_radical_found_chars_empty),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .height(ItemHeight)
                                .wrapContentSize()
                                .padding(start = 40.dp)
                        )
                    } else {
                        CharactersGrid(
                            items = charactersSectionDataState,
                            onClick = onCharacterClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentSize()
                        )
                    }

                }

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outline)
                )


                Column(modifier = Modifier.weight(1f)) {

                    Text(
                        text = stringResource(R.string.search_radical_radicals_title),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )

                    CharactersGrid(
                        items = radicalsSectionDataState,
                        onClick = {
                            selectedRadicals.value = selectedRadicals.value.run {
                                if (contains(it)) minus(it) else plus(it)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                }

            }

        }
    }
}

@Composable
private fun DraggableContainerIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize()
            .padding(vertical = 6.dp)
            .size(width = 60.dp, height = 4.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.outline)
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun Header(selectedRadicalsState: MutableState<Set<String>>) {

    val transition = updateTransition(
        targetState = selectedRadicalsState.value,
        label = "Selected Radicals Visibility Transition"
    )
    transition.AnimatedContent(
        contentKey = { it.isEmpty() },
        transitionSpec = { fadeIn() with fadeOut() }
    ) { radicals ->

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(ItemHeight)
                .wrapContentSize(Alignment.CenterStart)
        ) {
            if (radicals.isEmpty()) {
                Text(
                    text = stringResource(R.string.search_radical_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            } else {
                LazyRow(
                    modifier = Modifier
                        .height(ItemHeight)
                        .weight(1f)
                        .padding(start = 20.dp)
                ) {
                    items(
                        items = radicals.toList(),
                        key = { it }
                    ) {
                        Text(
                            text = it,
                            fontSize = TextSize,
                            modifier = Modifier
                                .height(ItemHeight)
                                .widthIn(min = ItemHeight)
                                .clip(MaterialTheme.shapes.small)
                                .then(MultipleWidthModifier(ItemHeight))
                                .clickable { selectedRadicalsState.value = radicals.minus(it) }
                                .wrapContentSize()
                                .padding(horizontal = ItemHeight / 5)
                        )
                    }
                }
                IconButton(
                    onClick = { selectedRadicalsState.value = emptySet() }
                ) {
                    Icon(Icons.Default.Close, null)
                }

            }

        }

    }

}

@Composable
private fun LoadingIndicator(loadingState: State<Boolean>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
    ) {
        AnimatedVisibility(
            visible = loadingState.value,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxSize(),
                trackColor = MaterialTheme.colorScheme.background
            )
        }
    }
}

class MultipleWidthModifier(val height: Dp) : LayoutModifier {
    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val placeable = measurable.measure(constraints)
        val heightPx = height.roundToPx()
        val widthPx = (ceil(placeable.width.toFloat() / heightPx) * heightPx).roundToInt()
        return layout(width = widthPx, height = heightPx, alignmentLines = emptyMap()) {
            placeable.place(0, 0)
        }
    }
}

@Composable
private fun CharactersLine(
    itemsState: State<List<RadicalSearchListItem>>,
    onClick: (String) -> Unit,
    modifier: Modifier
) {

    val items by itemsState

    if (items.isEmpty()) {
        Text(
            text = stringResource(R.string.search_radical_found_chars_empty),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .height(ItemHeight)
                .wrapContentSize()
                .padding(start = 40.dp)
        )
    } else {
        LazyRow(
            modifier = modifier.height(ItemHeight),
        ) {
            items(itemsState.value) {
                when (it) {
                    is RadicalSearchListItem.StrokeGroup -> {
                        Text(
                            text = it.count.toString(),
                            fontSize = TextSize,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(MaterialTheme.shapes.small)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .wrapContentSize()
                        )
                    }
                    is RadicalSearchListItem.Character -> {
                        Text(
                            text = it.character,
                            fontSize = TextSize,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(MaterialTheme.shapes.small)
                                .then(MultipleWidthModifier(ItemHeight))
                                .clickable { onClick(it.character) }
                                .wrapContentSize()
                                .padding(horizontal = ItemHeight / 5)
                        )
                    }
                }
            }
        }
    }

}

@Composable
private fun CharactersGrid(
    items: State<List<RadicalSearchListItem>>,
    onClick: (String) -> Unit,
    modifier: Modifier
) {

    LazyVerticalGrid(
        columns = GridCells.Adaptive(ItemHeight),
        modifier = modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = items.value,
            contentType = { it::class }
        ) {
            when (it) {
                is RadicalSearchListItem.StrokeGroup -> {
                    Text(
                        text = it.count.toString(),
                        fontSize = TextSize,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .wrapContentSize()
                    )
                }
                is RadicalSearchListItem.Character -> {
                    val textColor = if (it.isEnabled) {
                        Color.Unspecified
                    } else {
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    }
                    Text(
                        text = it.character,
                        fontSize = TextSize,
                        color = textColor,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(MaterialTheme.shapes.small)
                            .clickable(enabled = it.isEnabled) { onClick(it.character) }
                            .wrapContentSize()
                            .padding(horizontal = ItemHeight / 5)
                    )
                }
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    AppTheme(useDarkTheme = false) {
        val screenHeight = LocalConfiguration.current.screenHeightDp
        RadicalSearch(
            state = rememberUpdatedState(RadicalSearchState.random()),
            selectedRadicals = remember { mutableStateOf(emptySet()) },
            height = remember { mutableStateOf(screenHeight.dp) },
            onCharacterClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectedRadicalsPreview() {
    AppTheme(useDarkTheme = false) {
        val screenHeight = LocalConfiguration.current.screenHeightDp
        val state = RadicalSearchState.random()
        RadicalSearch(
            state = rememberUpdatedState(state),
            selectedRadicals = remember {
                mutableStateOf(
                    state.radicalsListItems.filterIsInstance<RadicalSearchListItem.Character>()
                        .take(2)
                        .map { it.character }
                        .toSet()
                )
            },
            height = remember { mutableStateOf(screenHeight.dp) },
            onCharacterClick = {}
        )
    }
}

@Preview(device = Devices.PIXEL_C)
@Composable
private fun TabletPreview() {
    AppTheme(useDarkTheme = true) {
        val screenHeight = LocalConfiguration.current.screenHeightDp
        Surface {
            RadicalSearch(
                state = rememberUpdatedState(RadicalSearchState.random()),
                selectedRadicals = remember { mutableStateOf(emptySet()) },
                height = remember { mutableStateOf(screenHeight.dp) },
                onCharacterClick = {}
            )
        }

    }
}