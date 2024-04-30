package com.jainhardik120.passbud.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun CheckBoxWithText(
    checked: Boolean = false,
    onCheckedChange: (Boolean) -> Unit,
    text: String
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(48.dp)
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                role = Role.Checkbox
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(end = 16.dp)
        )
        Spacer(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            modifier = Modifier.padding(end = 12.dp)
        )
    }
}

@Composable
fun PasswordGenerator(
    onPasswordGenerated: (String) -> Unit = {},
    shouldGeneratePassword: Boolean = true
) {

    var numbersChecked by remember { mutableStateOf(true) }
    var specialCharactersChecked by remember { mutableStateOf(true) }
    var capitalLettersChecked by remember { mutableStateOf(true) }
    var length by remember {
        mutableStateOf(20f)
    }

    fun getListOfCharacters(
        isNumbersChecked: Boolean,
        isSpecialCharactersChecked: Boolean,
        isCapitalLettersChecked: Boolean
    ): String {
        val lowercaseLetters = "abcdefghijklmnopqrstuvwxyz"
        val uppercaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val numbers = "0123456789"
        val specialCharacters = "!@#$%^&*()_+~`|}{[]:;?><,./-="
        var listOfCharacters = lowercaseLetters
        if (isCapitalLettersChecked) {
            listOfCharacters += uppercaseLetters
        }
        if (isNumbersChecked) {
            listOfCharacters += numbers
        }
        if (isSpecialCharactersChecked) {
            listOfCharacters += specialCharacters
        }

        return listOfCharacters
    }

    fun singlePasswordGenerator(length: Int, listOfCharacters: String): String {
        val password = StringBuilder(length)
        for (i in 0 until length) {
            val randomIndex = Random.nextInt(listOfCharacters.length)
            password.append(listOfCharacters[randomIndex])
        }
        return password.toString()
    }


    fun createPassword() {
        val listOfCharacters = getListOfCharacters(
            numbersChecked, specialCharactersChecked, capitalLettersChecked
        )
        onPasswordGenerated(singlePasswordGenerator(length.roundToInt(), listOfCharacters))
    }

    LaunchedEffect(key1 = Unit, block = {
        if (shouldGeneratePassword) {
            createPassword()
        }
    })

    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Length : ${length.roundToInt()}",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
            IconButton(onClick = { createPassword() }) {
                Icon(Icons.Rounded.Refresh, contentDescription = "Refresh Icon")
            }
        }
        CheckBoxWithText(checked = numbersChecked, onCheckedChange = {
            numbersChecked = it
            createPassword()
        }, text = "Numbers")
        CheckBoxWithText(checked = specialCharactersChecked, onCheckedChange = {
            specialCharactersChecked = it
            createPassword()
        }, text = "Special Characters")
        CheckBoxWithText(checked = capitalLettersChecked, onCheckedChange = {
            capitalLettersChecked = it
            createPassword()
        }, text = "Capital Letters")
        Slider(
            value = length,
            valueRange = 1f..200f,
            onValueChange = {
                length = it
                createPassword()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}