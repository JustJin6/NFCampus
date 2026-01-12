package com.example.nfcampus.gui.about

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PrivacyPolicyScreen() {
    Scaffold { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Title
            Text(
                text = "Privacy Policy",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Content
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                // We Collect Section
                SectionHeader(text = "We Collect:")
                Spacer(modifier = Modifier.height(4.dp))
                SectionDescription(text = "Your name, student ID, major, and when/where you tap your phone to enter buildings.")

                Spacer(modifier = Modifier.height(36.dp))

                // We Use It Section
                SectionHeader(text = "We Use It:")
                Spacer(modifier = Modifier.height(4.dp))
                SectionDescription(text = "To let you into buildings, make sure the app works, and keep campus secure.")

                Spacer(modifier = Modifier.height(36.dp))

                // We Share It Section
                SectionHeader(text = "We Share It:")
                Spacer(modifier = Modifier.height(4.dp))
                SectionDescription(text = "Only with necessary university staff (e.g. Security or IT). We never sell it or use it for ads.")

                Spacer(modifier = Modifier.height(36.dp))

                // We Protect It Section
                SectionHeader(text = "We Protect It:")
                Spacer(modifier = Modifier.height(4.dp))
                SectionDescription(text = "Your data is stored securely and only kept as long as you're a student.")

                Spacer(modifier = Modifier.height(48.dp))

                // Questions Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SectionHeader(text = "Questions?")

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("Contact: ")
                            }
                            append("04-5456 000")
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Text(
                        buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("Email: ")
                            }
                            append("enquiry@peninsulacollege.edu.my")
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )
}

@Composable
fun SectionDescription(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        fontSize = 16.sp
    )
}