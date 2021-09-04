package dev.thuatnguyen.otpcomposesample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.Color.Companion.Yellow
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import dev.thuatnguyen.otpcomposesample.ui.theme.OTPComposeSampleTheme
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {
    @ExperimentalComposeUiApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            OTPComposeSampleTheme {
                val showDialog = rememberSaveable {
                    mutableStateOf(false)
                }
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Column(
                        Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Button(onClick = { showDialog.value = true }) {
                            Text(text = "Verify")
                        }
                    }
                }

                OTPDialog(lifecycleOwner = this@MainActivity, showDialog)
            }
        }
    }
}

@ExperimentalComposeUiApi
@Composable
fun OTPDialog(lifecycleOwner: LifecycleOwner, showDialog: MutableState<Boolean>) {

    val arr: CharArray by remember {
        mutableStateOf(CharArray(6) { '-' })
    }
    val shoullCallAPI = rememberSaveable {
        mutableStateOf(false)
    }
    val errorState = rememberSaveable {
        mutableStateOf(false)
    }

    val successState = rememberSaveable {
        mutableStateOf(false)
    }
    LoadingProgress(showDialog = shoullCallAPI.value) {
        shoullCallAPI.value = false
    }

    if (shoullCallAPI.value) {
        lifecycleOwner.lifecycleScope.launch {
            delay(2000)
            shoullCallAPI.value = false
            val success = arr.joinToString(separator = "") == "123456"
            errorState.value = !success
            successState.value = success
        }

    }
    if (showDialog.value) {
        AlertDialog(
            shape = MaterialTheme.shapes.small,
            onDismissRequest = { },
            title = { Text(text = "OTP Code") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.size(16.dp))
                    Text(text = "")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val focusManager = LocalFocusManager.current
                        for (i in 0..5) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .absolutePadding(left = 2.dp, right = 1.dp)
                            ) {
                                OTPField(keyboardActions = KeyboardActions(
                                    onNext = {
                                        focusManager.moveFocus(FocusDirection.Right)
                                    }
                                ),
                                    isError = errorState.value,
                                    isSuccess = successState.value) { pre, curr ->
                                    if (curr.isEmpty()) {
                                        arr[i] = '-'
                                        errorState.value = false
                                        successState.value = false
                                        focusManager.moveFocus(FocusDirection.Left)
                                    } else {
                                        arr[i] = curr[0]
                                        shoullCallAPI.value = !arr.joinToString().contains("-")
                                        if (i < 5) {
                                            focusManager.moveFocus(FocusDirection.Right)
                                        } else {
                                            focusManager.moveFocus(FocusDirection.Out)
                                        }
                                    }
                                }
                            }
                        }

                    }
                    Text(
                        modifier = Modifier.padding(top = 8.dp),
                        text = if (errorState.value) "OTP is wrong!" else if (successState.value) "OTP verified!" else "",
                        color = when {
                            errorState.value -> Color(0xFFe61514)
                            successState.value -> Color(0xFF2bb35f)
                            else -> Yellow
                        }
                    )

                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog.value = false }) {
                    Text(text = "Confirm".uppercase())
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog.value = false }) {
                    Text(text = "Cancel".uppercase())
                }
            },
        )
    }
}

@Composable
fun OTPField(
    modifier: Modifier = Modifier,
    keyboardActions: KeyboardActions,
    isError: Boolean = false,
    isSuccess: Boolean = false,
    onValueChange: (String, String) -> Unit
) {

    val state = rememberSaveable { mutableStateOf("") }
    OutlinedTextField(
        modifier = modifier.padding(),
        singleLine = true,
        value = state.value,
        textStyle = TextStyle(fontWeight = Bold),
        onValueChange = {
            if (it.length <= 1) {
                state.value = it
            }
            onValueChange(state.value, it)
        },
        keyboardActions = keyboardActions,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        ),
        isError = isError,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = if (isSuccess) Color(0xFF2bb35f) else MaterialTheme.colors.primary.copy(
                alpha = ContentAlpha.high
            ),
            unfocusedBorderColor = if (isSuccess) Color(0xFF2bb35f) else MaterialTheme.colors.primary.copy(
                alpha = ContentAlpha.disabled
            )
        )
    )
}

@Composable
fun LoadingProgress(showDialog: Boolean, onClick: () -> Unit) {
    if (showDialog) {
        Dialog(
            onDismissRequest = onClick,
            DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(100.dp)
                    .background(White, shape = RoundedCornerShape(12.dp))
            ) {
                Column {
                    CircularProgressIndicator(modifier = Modifier.padding(6.dp))
                }
            }
        }
    }
}