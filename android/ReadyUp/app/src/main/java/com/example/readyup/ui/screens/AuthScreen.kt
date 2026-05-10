package com.example.readyup.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.readyup.ui.theme.*
import com.example.readyup.viewmodel.AuthState
import com.example.readyup.viewmodel.AuthViewModel

@Composable
fun AuthScreen(vm: AuthViewModel) {
    val state by vm.state.collectAsState()
    var isRegister by remember { mutableStateOf(false) }

    // Login fields
    var loginUser by remember { mutableStateOf("") }
    var loginPass by remember { mutableStateOf("") }

    // Register fields
    var regUser by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPass by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Cyan.copy(alpha = 0.06f), Color.Transparent),
                    radius = 800f
                )
            )
            .background(BgDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier
                .padding(24.dp)
                .widthIn(max = 400.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            // Logo
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("READY", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Cyan, letterSpacing = 3.sp)
                Text("UP", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Rose, letterSpacing = 3.sp)
            }
            Text(
                "Tu biblioteca de videojuegos",
                fontSize = 12.sp,
                color = TextMuted,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 36.dp)
            )

            // Card
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Surface)
                    .border(1.dp, BorderColor)
            ) {
                // Top accent line
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(Brush.horizontalGradient(listOf(Cyan, Rose)))
                )

                // Tab row
                Row(Modifier.fillMaxWidth()) {
                    TabButton("Entrar", !isRegister) { isRegister = false }
                    TabButton("Registro", isRegister) { isRegister = true }
                }

                Column(Modifier.padding(24.dp)) {
                    if (!isRegister) {
                        // LOGIN
                        RuTextField(
                            value = loginUser,
                            onValueChange = { loginUser = it },
                            label = "Usuario",
                            imeAction = ImeAction.Next,
                            onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                        Spacer(Modifier.height(14.dp))
                        RuTextField(
                            value = loginPass,
                            onValueChange = { loginPass = it },
                            label = "Contraseña",
                            isPassword = true,
                            imeAction = ImeAction.Done,
                            onImeAction = { vm.login(loginUser, loginPass) }
                        )
                    } else {
                        // REGISTER
                        RuTextField(
                            value = regUser,
                            onValueChange = { regUser = it },
                            label = "Usuario",
                            imeAction = ImeAction.Next,
                            onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                        Spacer(Modifier.height(14.dp))
                        RuTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it },
                            label = "Email",
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                            onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                        Spacer(Modifier.height(14.dp))
                        RuTextField(
                            value = regPass,
                            onValueChange = { regPass = it },
                            label = "Contraseña",
                            isPassword = true,
                            imeAction = ImeAction.Done,
                            onImeAction = { vm.register(regUser, regEmail, regPass) }
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    // Error
                    AnimatedVisibility(visible = state.error != null) {
                        Text(
                            state.error ?: "",
                            color = Rose,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    // Submit button
                    Button(
                        onClick = {
                            vm.clearError()
                            if (!isRegister) vm.login(loginUser, loginPass)
                            else vm.register(regUser, regEmail, regPass)
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        enabled = !state.isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Cyan),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(18.dp))
                        } else {
                            Text(
                                if (!isRegister) "ENTRAR →" else "CREAR CUENTA →",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun RowScope.TabButton(label: String, isActive: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .weight(1f)
            .clickable(onClick = onClick)
            .background(if (isActive) Surface2 else Color.Transparent)
            .border(
                width = if (isActive) 0.dp else 0.dp,
                color = Color.Transparent
            )
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                label.uppercase(),
                color = if (isActive) Cyan else TextMuted,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontSize = 13.sp
            )
            if (isActive) {
                Spacer(Modifier.height(4.dp))
                Box(Modifier.width(24.dp).height(2.dp).background(Cyan))
            }
        }
    }
}

@Composable
fun RuTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {}
) {
    Column {
        Text(
            label.uppercase(),
            fontSize = 10.sp,
            letterSpacing = 1.5.sp,
            color = TextMuted,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else keyboardType, imeAction = imeAction),
            keyboardActions = KeyboardActions(onAny = { onImeAction() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Cyan,
                unfocusedBorderColor = BorderColor,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = Cyan,
                focusedContainerColor = BgDark,
                unfocusedContainerColor = BgDark
            ),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp)
        )
    }
}
