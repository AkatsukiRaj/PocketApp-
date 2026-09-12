package com.pocket.app.ui.share

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.pocket.app.PocketApplication
import com.pocket.app.data.model.ItemCategory
import com.pocket.app.data.model.ItemType
import com.pocket.app.utils.FileUtils
import kotlinx.coroutines.launch

class ShareReceiverActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val action = intent.action
        if (action == Intent.ACTION_SEND) {
            handleSingleSend()
        } else if (action == Intent.ACTION_SEND_MULTIPLE) {
            handleMultipleSend()
        } else {
            finish()
        }
    }

    private fun handleSingleSend() {
        val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Intent.EXTRA_STREAM)
        }

        if (uri != null) {
            val app = application as PocketApplication
            lifecycleScope.launch {
                try {
                    val detectedName = FileUtils.getFileName(this@ShareReceiverActivity, uri) ?: "Shared_File"
                    val savedItem = app.repository.saveIncomingUri(
                        uri = uri,
                        title = detectedName,
                        category = ItemCategory.GENERAL,
                        folderName = "General"
                    )
                    val toastMessage = if (savedItem.itemType == ItemType.PHOTO) {
                        "புகைப்படம் பாக்கெட்டில் சேமிக்கப்பட்டது! ✓\n(Photo saved to Pocket!)"
                    } else {
                        "ஆவணம் பாக்கெட்டில் சேமிக்கப்பட்டது! ✓\n(Document saved to Pocket!)"
                    }
                    Toast.makeText(applicationContext, toastMessage, Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(applicationContext, "சேமிப்பதில் பிழை / Error saving: ${e.localizedMessage ?: ""}", Toast.LENGTH_SHORT).show()
                } finally {
                    finish()
                }
            }
        } else {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!text.isNullOrBlank()) {
                val app = application as PocketApplication
                lifecycleScope.launch {
                    try {
                        app.repository.createNote(
                            title = "Shared Note",
                            content = text,
                            category = ItemCategory.GENERAL
                        )
                        Toast.makeText(applicationContext, "குறிப்பு பாக்கெட்டில் சேமிக்கப்பட்டது! ✓\n(Note saved to Pocket!)", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(applicationContext, "சேமிப்பதில் பிழை / Error saving: ${e.localizedMessage ?: ""}", Toast.LENGTH_SHORT).show()
                    } finally {
                        finish()
                    }
                }
            } else {
                Toast.makeText(applicationContext, "கோப்பு எதுவும் பெறப்படவில்லை (No file received)", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun handleMultipleSend() {
        val uris: ArrayList<Uri>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM)
        }

        if (!uris.isNullOrEmpty()) {
            val app = application as PocketApplication
            lifecycleScope.launch {
                var photoCount = 0
                var docCount = 0
                try {
                    for (u in uris) {
                        val detectedName = FileUtils.getFileName(this@ShareReceiverActivity, u) ?: "Shared_File"
                        val saved = app.repository.saveIncomingUri(
                            uri = u,
                            title = detectedName,
                            category = ItemCategory.GENERAL,
                            folderName = "General"
                        )
                        if (saved.itemType == ItemType.PHOTO) {
                            photoCount++
                        } else {
                            docCount++
                        }
                    }
                    val total = photoCount + docCount
                    val toastMessage = when {
                        photoCount > 0 && docCount == 0 -> "$photoCount புகைப்படங்கள் பாக்கெட்டில் சேமிக்கப்பட்டன! ✓\n($photoCount Photos saved!)"
                        docCount > 0 && photoCount == 0 -> "$docCount ஆவணங்கள் பாக்கெட்டில் சேமிக்கப்பட்டன! ✓\n($docCount Documents saved!)"
                        else -> "$total கோப்புகள் பாக்கெட்டில் சேமிக்கப்பட்டன! ✓\n($total Files saved!)"
                    }
                    Toast.makeText(applicationContext, toastMessage, Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(applicationContext, "சேமிப்பதில் பிழை / Error saving: ${e.localizedMessage ?: ""}", Toast.LENGTH_SHORT).show()
                } finally {
                    finish()
                }
            }
        } else {
            Toast.makeText(applicationContext, "கோப்புகள் எதுவும் பெறப்படவில்லை (No files received)", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
