package ua.syt0r.kanji.presentation.screen.main.screen.practice_preview.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material3.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ua.syt0r.kanji.R
import ua.syt0r.kanji.presentation.common.showSnackbarFlow
import ua.syt0r.kanji.presentation.common.theme.AppTheme
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.ui.CustomDropdownMenu
import ua.syt0r.kanji.presentation.common.ui.CustomRippleTheme
import ua.syt0r.kanji.presentation.common.ui.PreferredPopupLocation
import ua.syt0r.kanji.presentation.screen.main.screen.practice_preview.PracticePreviewScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_preview.data.*
import java.time.format.DateTimeFormatter
import kotlin.random.Random

private val GroupDetailsDateTimeFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun PracticePreviewScreenUI(
    state: State<ScreenState>,
    onSortSelected: (SortConfiguration) -> Unit,
    onVisibilitySelected: (VisibilityConfiguration) -> Unit,
    onUpButtonClick: () -> Unit,
    onEditButtonClick: () -> Unit,
    onCharacterClick: (String) -> Unit,
    onStartPracticeClick: (PracticeGroup, PracticeConfiguration) -> Unit,
    onDismissMultiselectClick: () -> Unit,
    onEnableMultiselectClick: () -> Unit,
    onGroupClickInMultiselectMode: (PracticeGroup) -> Unit,
    onMultiselectPracticeStart: (MultiselectPracticeConfiguration) -> Unit
) {

    var shouldShowSortDialog by remember { mutableStateOf(false) }
    if (shouldShowSortDialog) {
        PracticePreviewSortDialog(
            currentSortConfiguration = (state.value as ScreenState.Loaded).sortConfiguration,
            onDismissRequest = { shouldShowSortDialog = false },
            onApplySort = {
                shouldShowSortDialog = false
                onSortSelected(it)
            }
        )
    }

    var shouldShowMultiselectPracticeStartDialog by remember { mutableStateOf(false) }
    if (shouldShowMultiselectPracticeStartDialog) {
        val loadedState = state.value as ScreenState.Loaded
        if (loadedState.selectedGroupIndexes.isNotEmpty()) {
            PracticePreviewMultiselectDialog(
                groups = loadedState.groups,
                selectedGroupIndexes = loadedState.selectedGroupIndexes,
                onDismissRequest = { shouldShowMultiselectPracticeStartDialog = false },
                onStartClick = onMultiselectPracticeStart
            )
        } else {
            shouldShowMultiselectPracticeStartDialog = false
        }
    }

    var shouldShowVisibilityDialog by remember { mutableStateOf(false) }
    if (shouldShowVisibilityDialog) {
        PracticePreviewVisibilityDialog(
            visibilityConfiguration = (state.value as ScreenState.Loaded).visibilityConfiguration,
            onDismissRequest = { shouldShowVisibilityDialog = false },
            onApply = {
                shouldShowVisibilityDialog = false
                onVisibilitySelected(it)
            }
        )
    }

    val coroutineScope = rememberCoroutineScope()

    val selectedGroupIndexState = rememberSaveable { mutableStateOf<Int?>(null) }
    val bottomSheetGroupState: MutableState<PracticeGroup?> = remember { mutableStateOf(null) }

    val bottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    if (bottomSheetState.isVisible) {
        BackHandler { coroutineScope.launch { bottomSheetState.hide() } }
    }

    // Updates selected group or hides bottom sheet after review
    LaunchedEffect(Unit) {
        val stateFlow = snapshotFlow { state.value }.filterIsInstance<ScreenState.Loaded>()
        val indexFlow = snapshotFlow { selectedGroupIndexState.value }.filterNotNull()
        stateFlow.combine(indexFlow) { loadedState, index -> loadedState to index }
            .collectLatest { (loadedState, index) ->
                val groups = loadedState.run {
                    if (visibilityConfiguration.reviewOnlyGroups) reviewOnlyGroups else allGroups
                }
                val selectedGroup = groups.find { it.index == index }
                if (selectedGroup != null) {
                    bottomSheetGroupState.value = selectedGroup
                } else {
                    bottomSheetState.hide()
                }
            }
    }

    ModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetContent = {
            Surface {
                BottomSheetContent(
                    practiceGroupState = bottomSheetGroupState,
                    onCharacterClick = onCharacterClick,
                    onStudyClick = { practiceGroup, practiceConfiguration ->
                        onStartPracticeClick(practiceGroup, practiceConfiguration)
                    }
                )
            }
        }
    ) {

        val fabLayoutCoordinates = remember { mutableStateOf<LayoutCoordinates?>(null) }
        val snackbarHostState = remember { SnackbarHostState() }

        Scaffold(
            topBar = {
                Toolbar(
                    state = state,
                    upButtonClick = onUpButtonClick,
                    dismissMultiSelectButtonClick = onDismissMultiselectClick,
                    editButtonClick = onEditButtonClick,
                    sortButtonClick = { shouldShowSortDialog = true },
                    visibilityButtonClick = { shouldShowVisibilityDialog = true }
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            floatingActionButton = {

                val stateNotLoadedMessage =
                    stringResource(R.string.practice_preview_multiselect_not_loaded_message)
                val noGroupsSelectedMessage =
                    stringResource(R.string.practice_preview_multiselect_no_selection_message)

                FloatingActionButtonSection(
                    state = state,
                    fabLayoutCoordinates = fabLayoutCoordinates,
                    onStartMultiselectMode = {
                        val canStartMultiselect = state.value is ScreenState.Loaded
                        if (canStartMultiselect) {
                            onEnableMultiselectClick()
                        } else {
                            snackbarHostState.showSnackbarFlow(
                                stateNotLoadedMessage,
                                withDismissAction = true
                            ).launchIn(coroutineScope)
                        }
                    },
                    onConfigureMultiselectPractice = {
                        val canShowDialog = (state.value as? ScreenState.Loaded)
                            ?.selectedGroupIndexes
                            ?.isNotEmpty() == true
                        if (canShowDialog) {
                            shouldShowMultiselectPracticeStartDialog = true
                        } else {
                            snackbarHostState.showSnackbarFlow(
                                noGroupsSelectedMessage,
                                withDismissAction = true
                            ).launchIn(coroutineScope)
                        }
                    }
                )

            }
        ) { paddingValues ->

            val transition = updateTransition(targetState = state.value, label = "State Transition")
            transition.AnimatedContent(
                contentKey = { (it as? ScreenState.Loaded)?.visibilityConfiguration },
                transitionSpec = { fadeIn() with fadeOut() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) { screenState ->

                when (screenState) {
                    ScreenState.Loading -> {
                        LoadingState()
                    }
                    is ScreenState.Loaded -> {
                        LoadedState(
                            screenState = screenState,
                            fabLayoutCoordinates = fabLayoutCoordinates,
                            onGroupClick = {
                                if (screenState.isMultiselectEnabled) {
                                    onGroupClickInMultiselectMode(it)
                                } else {
                                    selectedGroupIndexState.value = it.index
                                    coroutineScope.launch { bottomSheetState.show() }
                                }
                            }
                        )

                        if (screenState.isMultiselectEnabled) {
                            BackHandler(onBack = onDismissMultiselectClick)
                        }
                    }
                }

            }

        }

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Toolbar(
    state: State<ScreenState>,
    upButtonClick: () -> Unit,
    dismissMultiSelectButtonClick: () -> Unit,
    editButtonClick: () -> Unit,
    sortButtonClick: () -> Unit,
    visibilityButtonClick: () -> Unit
) {
    TopAppBar(
        title = {

            var cachedTitleData: Triple<String, Boolean, Set<Int>>? by remember {
                mutableStateOf(null)
            }

            LaunchedEffect(state.value) {
                val updatedTitleData = state.value
                    .let { it as? ScreenState.Loaded }
                    ?.run { Triple(title, isMultiselectEnabled, selectedGroupIndexes) }

                if (updatedTitleData != null && updatedTitleData != cachedTitleData) {
                    cachedTitleData = updatedTitleData
                }
            }

            cachedTitleData?.let { (title, isMultiselectEnabled, selectedGroupIndexes) ->

                val text = if (isMultiselectEnabled) {
                    stringResource(
                        R.string.practice_preview_multiselect_title,
                        selectedGroupIndexes.size
                    )
                } else {
                    title
                }

                Text(text = text)

            }
        },
        navigationIcon = {
            val shouldShowMultiselectDismissButton by remember {
                derivedStateOf {
                    state.value
                        .let { it as? ScreenState.Loaded }
                        ?.isMultiselectEnabled == true
                }
            }
            if (shouldShowMultiselectDismissButton) {
                IconButton(
                    onClick = dismissMultiSelectButtonClick
                ) {
                    Icon(Icons.Default.Close, null)
                }
            } else {
                IconButton(
                    onClick = upButtonClick
                ) {
                    Icon(Icons.Default.ArrowBack, null)
                }
            }

        },
        actions = {
            val isLoadedState by remember {
                derivedStateOf { state.value is ScreenState.Loaded }
            }
            IconButton(
                onClick = visibilityButtonClick,
                enabled = isLoadedState
            ) {
                Icon(painterResource(R.drawable.baseline_visibility_24), null)
            }
            IconButton(
                onClick = editButtonClick
            ) {
                Icon(painterResource(R.drawable.ic_outline_edit_24), null)
            }
            IconButton(
                onClick = sortButtonClick,
                enabled = isLoadedState
            ) {
                Icon(painterResource(R.drawable.ic_baseline_sort_24), null)
            }
        }
    )
}

@Composable
private fun LoadingState() {
    CircularProgressIndicator(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize()
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun FloatingActionButtonSection(
    state: State<ScreenState>,
    fabLayoutCoordinates: MutableState<LayoutCoordinates?>,
    onStartMultiselectMode: () -> Unit,
    onConfigureMultiselectPractice: () -> Unit
) {

    val isInMultiselectMode by remember {
        derivedStateOf {
            state.value.let { it as? ScreenState.Loaded }?.isMultiselectEnabled == true
        }
    }

    FloatingActionButton(
        onClick = {
            if (isInMultiselectMode) {
                onConfigureMultiselectPractice()
            } else {
                onStartMultiselectMode()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.BottomEnd)
            .onGloballyPositioned { fabLayoutCoordinates.value = it },
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {

        AnimatedContent(
            targetState = isInMultiselectMode,
            transitionSpec = { fadeIn(tween(150, 150)) with fadeOut(tween(150)) }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (it) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_baseline_radio_button_checked_24),
                        contentDescription = null
                    )
                }
            }
        }

    }
}

@Composable
private fun LoadedState(
    screenState: ScreenState.Loaded,
    fabLayoutCoordinates: State<LayoutCoordinates?>,
    onGroupClick: (PracticeGroup) -> Unit
) {

    if (screenState.groups.isEmpty()) {
        Text(
            text = stringResource(R.string.practice_preview_empty),
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize()
        )
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(160.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .wrapContentSize(Alignment.TopCenter)
    ) {

        items(
            items = screenState.groups,
            key = { it.index }
        ) { group ->

            PracticeGroup(
                group = group,
                state = when {
                    screenState.isMultiselectEnabled -> screenState.selectedGroupIndexes
                        .contains(group.index)
                        .let {
                            if (it) GroupItemState.Selected else GroupItemState.Unselected
                        }
                    else -> GroupItemState.Default
                },
                onClick = { onGroupClick(group) },
                modifier = Modifier
            )

        }

        item(
            span = { GridItemSpan(maxLineSpan) }
        ) {

            val screenDensity = LocalDensity.current.density

            val spacerHeight = fabLayoutCoordinates.value
                ?.let {
                    val containerHeight = it.findRootCoordinates().size.height
                    val fabTopPos = it.boundsInRoot().top
                    (containerHeight - fabTopPos) / screenDensity + 16
                }
                ?.dp
                ?: 16.dp

            Spacer(modifier = Modifier.height(spacerHeight))
        }

    }
}

@Composable
private fun CharacterReviewState.toColor(): Color = when (this) {
    CharacterReviewState.RecentlyReviewed -> MaterialTheme.extraColorScheme.success
    CharacterReviewState.NeedReview -> MaterialTheme.extraColorScheme.outdated
    else -> MaterialTheme.colorScheme.surfaceVariant
}

private enum class GroupItemState { Default, Selected, Unselected }

@Composable
private fun PracticeGroup(
    group: PracticeGroup,
    state: GroupItemState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .padding(end = 8.dp)
                .background(group.reviewState.toColor(), CircleShape)
                .size(8.dp)
        )

        Text(
            text = stringResource(
                R.string.practice_preview_list_group_title,
                group.index,
                group.items.joinToString("") { it.character }
            ),
            maxLines = 1,
            modifier = Modifier
                .weight(1f)
                // TODO check when new font api is stable, currently LineHeightStyle.Alignment.Center
                //  with disabled font paddings doesn't help
                .padding(bottom = 1.dp),
            overflow = TextOverflow.Ellipsis,
        )

        if (state != GroupItemState.Default) {
            Icon(
                painter = painterResource(
                    id = if (state == GroupItemState.Selected) R.drawable.ic_baseline_radio_button_checked_24
                    else R.drawable.ic_baseline_radio_button_unchecked_24
                ),
                contentDescription = null,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

    }

}

@Composable
fun BottomSheetContent(
    practiceGroupState: State<PracticeGroup?>,
    onCharacterClick: (String) -> Unit,
    onStudyClick: (PracticeGroup, PracticeConfiguration) -> Unit
) {

    val practiceGroup by practiceGroupState
    var practiceConfiguration by rememberSaveable(practiceGroup) {
        mutableStateOf(
            PracticeConfiguration(
                isStudyMode = practiceGroup?.firstDate == null,
                shuffle = true
            )
        )
    }

    var shouldShowConfigDialog by remember { mutableStateOf(false) }
    if (shouldShowConfigDialog) {
        PracticePreviewStudyOptionsDialog(
            defaultConfiguration = practiceConfiguration,
            onDismissRequest = { shouldShowConfigDialog = false },
            onApplyConfiguration = {
                shouldShowConfigDialog = false
                practiceConfiguration = it
            }
        )
    }

    Box(
        modifier = Modifier.animateContentSize(tween(100, easing = LinearEasing))
    ) {
        when (val group = practiceGroup) {
            null -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .wrapContentSize()
                )
            }
            else -> {
                PracticeGroupDetails(
                    group = group,
                    practiceConfiguration = practiceConfiguration,
                    onCharacterClick = onCharacterClick,
                    onOptionsClick = { shouldShowConfigDialog = true },
                    onStartClick = { onStudyClick(group, practiceConfiguration) }
                )
            }
        }
    }

}


@Composable
private fun PracticeGroupDetails(
    group: PracticeGroup,
    practiceConfiguration: PracticeConfiguration,
    onCharacterClick: (String) -> Unit = {},
    onOptionsClick: () -> Unit = {},
    onStartClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = stringResource(R.string.practice_preview_group_template, group.index),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 1.dp) // TODO text alignment api
            )

            var hintDropdownShown by remember { mutableStateOf(false) }

            val dotColor = group.reviewState.toColor()
            val rippleTheme = remember(dotColor) { CustomRippleTheme { dotColor } }

            CompositionLocalProvider(LocalRippleTheme provides rippleTheme) {
                Box(
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .clip(CircleShape)
                        .clickable { hintDropdownShown = true }
                        .fillMaxHeight()
                        .padding(horizontal = 16.dp)
                ) {

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                            .align(Alignment.Center)
                    )

                    CustomDropdownMenu(
                        expanded = hintDropdownShown,
                        onDismissRequest = { hintDropdownShown = false },
                        preferredPopupLocation = PreferredPopupLocation.Top
                    ) {
                        Text(
                            text = when (group.reviewState) {
                                CharacterReviewState.RecentlyReviewed -> R.string.practice_preview_review_state_recently
                                CharacterReviewState.NeedReview -> R.string.practice_preview_review_state_need_review
                                CharacterReviewState.NeverReviewed -> R.string.practice_preview_review_state_never_reviewed
                            }.let { stringResource(it) },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

            }

        }

        val firstDateMessage = group.firstDate?.format(GroupDetailsDateTimeFormat)
            ?: stringResource(R.string.practice_preview_date_never)

        Text(
            text = stringResource(R.string.practice_preview_first_date_template, firstDateMessage),
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        val lastDateMessage = group.lastDate?.format(GroupDetailsDateTimeFormat)
            ?: stringResource(R.string.practice_preview_date_never)

        Text(
            text = stringResource(R.string.practice_preview_last_date_template, lastDateMessage),
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        val scrollState = remember(group.index) { ScrollState(0) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
        ) {

            Spacer(modifier = Modifier.width(20.dp))

            group.items.forEach {

                Text(
                    text = it.character,
                    fontSize = 32.sp,
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, it.reviewState.toColor(), MaterialTheme.shapes.medium)
                        .height(IntrinsicSize.Min)
                        .aspectRatio(1f, true)
                        .clickable { onCharacterClick(it.character) }
                        .padding(8.dp)
                        .wrapContentSize()
                )

                Spacer(modifier = Modifier.width(10.dp))

            }

            Spacer(modifier = Modifier.width(10.dp))

        }

        Row(
            modifier = Modifier
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onOptionsClick
            ) {
                Icon(Icons.Outlined.Settings, null)
            }

            Spacer(modifier = Modifier.width(8.dp))

            val studyMessage = when {
                practiceConfiguration.isStudyMode -> stringResource(R.string.practice_preview_config_study)
                else -> stringResource(R.string.practice_preview_config_review)
            }

            val shuffleMessage = when {
                practiceConfiguration.shuffle -> stringResource(R.string.practice_preview_config_shuffle)
                else -> stringResource(R.string.practice_preview_config_no_shuffle)
            }

            Text(
                text = stringResource(
                    R.string.practice_preview_config_template,
                    studyMessage,
                    shuffleMessage
                ),
                modifier = Modifier.weight(1f),
                color = Color.Gray
            )

            Spacer(modifier = Modifier.width(8.dp))

            FilledTonalButton(
                onClick = onStartClick
            ) {
                Text(text = stringResource(R.string.practice_preview_practice_start))
            }

        }

    }
}

@Preview
@Composable
private fun DarkLoadedPreview(
    useDarkTheme: Boolean = true,
    isMultiselectEnabled: Boolean = false
) {
    AppTheme(useDarkTheme = useDarkTheme) {
        val state = remember {
            mutableStateOf(
                ScreenState.Loaded(
                    title = "Test Practice",
                    sortConfiguration = SortConfiguration(),
                    visibilityConfiguration = VisibilityConfiguration(),
                    allGroups = (1..20).map { PracticeGroup.random(it) },
                    reviewOnlyGroups = (1..20).map { PracticeGroup.random(it, true) },
                    isMultiselectEnabled = isMultiselectEnabled,
                    selectedGroupIndexes = emptySet()
                )
            )
        }
        PracticePreviewScreenUI(
            state = state,
            onSortSelected = {},
            onVisibilitySelected = {},
            onUpButtonClick = {},
            onEditButtonClick = {},
            onCharacterClick = {},
            onStartPracticeClick = { _, _ -> },
            onDismissMultiselectClick = {},
            onEnableMultiselectClick = {},
            onGroupClickInMultiselectMode = {},
            onMultiselectPracticeStart = {}
        )
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_C)
@Composable
private fun LightLoadedPreview() {
    DarkLoadedPreview(useDarkTheme = false, isMultiselectEnabled = true)
}

@Preview(showBackground = true)
@Composable
private fun GroupDetailsPreview(useDarkTheme: Boolean = true) {
    AppTheme(useDarkTheme = false) {
        Surface {
            PracticeGroupDetails(
                group = PracticeGroup.random(index = Random.nextInt(1, 100)),
                practiceConfiguration = PracticeConfiguration(isStudyMode = true, shuffle = true)
            )
        }
    }
}
