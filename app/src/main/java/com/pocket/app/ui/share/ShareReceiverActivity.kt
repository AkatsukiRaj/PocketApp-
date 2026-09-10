package com.pocket.app.ui.share

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.pocket.app.PocketApplication
import com.pocket.app.data.model.ItemCategory
import com.pocket.app.ui.theme.PocketTheme
import com.pocket.app.utils.FileUtils
import kotlinx.coroutines.launch

class ShareReceiverActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Intent.EXTRA_STREAM)
        }

        if (uri == null) {
            Toast.makeText(this, "கோப்பு கிடைக்கவில்லை (No file received)", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val detectedName = FileUtils.getFileName(this, uri) ?: "Shared_File"

        setContent {
            PocketTheme {
                ShareReceiverDialog(
                    fileName = detectedName,
                    onSave = { title, category ->
                        saveFileAndFinish(uri, title, category)
                    },
                    onCancel = { finish() }
                )
            }
        }
    }

    private fun saveFileAndFinish(uri: Uri, title: String, category: ItemCategory) {
        val app = application as PocketApplication
        lifecycleScope.launch {
            try {
                app.repository.saveIncomingUri(uri, title, category)
                Toast.makeText(this@ShareReceiverActivity, "பாக்கெட்டில் வெற்றிகரமாக சேமிக்கப்பட்டது! ✓", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this@ShareReceiverActivity, "Error saving file: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                finish()
            }
        }
    }
}

@Composable
fun ShareReceiverDialog(
    fileName: String,
    onSave: (String, ItemCategory) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf(fileName) }
    var selectedCategory by remember { mutableStateOf(ItemCategory.MEDICAL) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    "Pocket-ல் சேமிக்க (Save to Pocket)",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("கோப்பின் பெயர் (Name)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("எந்த பகுதியில் சேர்க்க வேண்டும்?", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ItemCategory.values().forEach { cat ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCategory = cat }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "${cat.displayName} (${cat.tamilName})",
                                fontSize = 15.sp,
                                fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onCancel) {
                        Text("ரத்து (Cancel)", fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(title, selectedCategory) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("சேமி (Save)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
