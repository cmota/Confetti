@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.sessions

import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.items
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import dev.johnoreilly.confetti.BuildConfig
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.navigation.ConferenceDayKey
import dev.johnoreilly.confetti.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.type.Session
import dev.johnoreilly.confetti.utils.QueryResult
import dev.johnoreilly.confetti.wear.components.SectionHeader
import dev.johnoreilly.confetti.wear.components.SessionCard
import dev.johnoreilly.confetti.wear.ui.ConfettiThemeFixed
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewDevices
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewFontSizes
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import org.koin.androidx.compose.getViewModel
import java.time.format.DateTimeFormatter

@Composable
fun SessionsRoute(
    navigateToSession: (SessionDetailsKey) -> Unit,
    columnState: ScalingLazyColumnState,
    viewModel: SessionsViewModel = getViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (!BuildConfig.DEBUG) {
        ReportDrawnWhen {
            uiState is QueryResult.Success
        }
    }

    SessionsScreen(
        uiState = uiState,
        sessionSelected = {
            navigateToSession(it)
        },
        columnState = columnState
    )
}

@Composable
fun SessionsScreen(
    uiState: QueryResult<SessionsUiState>,
    sessionSelected: (SessionDetailsKey) -> Unit,
    columnState: ScalingLazyColumnState
) {
    val dayFormatter = remember { DateTimeFormatter.ofPattern("eeee H:mm") }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("H:mm") }

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        columnState = columnState,
    ) {
        when (uiState) {
            is QueryResult.Success -> {
                val sessions = uiState.result.sessionsByTime

                sessions.forEachIndexed { index, sessionsAtTime ->
                    item {
                        val time = sessionsAtTime.time.toJavaLocalDateTime()
                        if (index == 0) {
                            SectionHeader(dayFormatter.format(time))
                        } else {
                            SectionHeader(timeFormatter.format(time))
                        }
                    }

                    items(sessionsAtTime.sessions) { session ->
                        SessionCard(session, sessionSelected = {
                            sessionSelected(
                                SessionDetailsKey(
                                    conference = uiState.result.conferenceDay.conference,
                                    sessionId = it
                                )
                            )
                        }, uiState.result.now)
                    }
                }
            }

            else -> {
                // TODO
            }
        }
    }
}

@WearPreviewDevices
@WearPreviewFontSizes
@Composable
fun SessionListViewPreview() {
    val sessionTime = LocalDateTime(2022, 12, 25, 12, 30)

    ConfettiThemeFixed {
        SessionsScreen(
            uiState = QueryResult.Success(
                SessionsUiState(
                    ConferenceDayKey("wearconf", sessionTime.date),
                    sessionsByTime = listOf(
                        SessionAtTime(
                            sessionTime,
                            listOf(
                                SessionDetails(
                                    "1",
                                    "Wear it's at",
                                    "Talk",
                                    sessionTime,
                                    sessionTime,
                                    "Be aWear of what's coming",
                                    "en",
                                    listOf(),
                                    SessionDetails.Room("Main Hall"),
                                    listOf(),
                                    Session.type.name
                                )
                            )
                        )
                    ),
                    now = java.time.LocalDateTime.of(2022, 1, 1, 1, 1).toKotlinLocalDateTime()
                )
            ),
            sessionSelected = {},
            columnState = ScalingLazyColumnDefaults.belowTimeText().create()
        )
    }
}
