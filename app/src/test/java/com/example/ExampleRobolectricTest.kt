package com.example

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.engine.RoomManager
import com.example.viewmodel.MultiplayerViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Kaachu Phool", appName)
  }

  @Test
  fun `test join room in room manager`() = runBlocking {
    val roomManager = RoomManager()
    val status = roomManager.joinRoom("123456", "Het")
    println("Join room status: $status")
    assertNotNull(status)
  }

  @Test
  fun `test join room in multiplayer viewmodel`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val vm = MultiplayerViewModel(app)
    vm.joinRoom("123456", "Het")
  }

  @Test
  fun `test multiplayer lobby screen rendering`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val vm = MultiplayerViewModel(app)
    vm.joinRoom("123456", "Het")
    composeTestRule.setContent {
      com.example.ui.screens.MultiplayerLobbyScreen(
        viewModel = vm,
        onStartGame = { _, _, _, _ -> },
        onBackClick = {}
      )
    }
    composeTestRule.waitForIdle()
  }

  @Test
  fun `test click join room in MultiplayerDialog`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val vm = MultiplayerViewModel(app)
    var joinedCode = ""
    var joinedName = ""
    composeTestRule.setContent {
      com.example.ui.screens.MultiplayerDialog(
        defaultName = "Het",
        isLoading = false,
        errorMessage = null,
        onDismiss = {},
        onCreateRoom = {},
        onJoinRoom = { code, name ->
          joinedCode = code
          joinedName = name
          vm.joinRoom(code, name)
        }
      )
    }
    composeTestRule.onNodeWithTag("room_code_input").performTextInput("123456")
    composeTestRule.onNodeWithTag("join_room_button").performClick()
    composeTestRule.waitForIdle()
    assertEquals("123456", joinedCode)
    assertEquals("Het", joinedName)
  }
}
