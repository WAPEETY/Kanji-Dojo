package ua.syt0r.kanji.presentation.screen.main.screen.practice_import

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.presentation.screen.main.screen.practice_import.PracticeImportScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_import.data.gradeImportPracticeCategory
import ua.syt0r.kanji.presentation.screen.main.screen.practice_import.data.jlptImportPracticeCategory
import ua.syt0r.kanji.presentation.screen.main.screen.practice_import.data.kanaImportPracticeCategory
import javax.inject.Inject


@HiltViewModel
class PracticeImportViewModel @Inject constructor(
    private val analyticsManager: AnalyticsManager
) : ViewModel(), PracticeImportScreenContract.ViewModel {

    override val state = mutableStateOf<ScreenState>(ScreenState.Loading)

    init {
        state.value = ScreenState.Loaded(
            listOf(
                kanaImportPracticeCategory,
                jlptImportPracticeCategory,
                gradeImportPracticeCategory
            )
        )
    }

    override fun reportScreenShown() {
        analyticsManager.setScreen("import")
    }

}