package com.pocket.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.pocket.app.MainActivity

class PocketGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            PocketWidgetContent()
        }
    }

    @Composable
    private fun PocketWidgetContent() {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.background)
                .padding(12.dp)
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Text(
                    text = "Pocket (பாக்கெட்)",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.primary
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            Text(
                text = "📌 1-Tap Quick Access",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = GlanceTheme.colors.onBackground
                )
            )

            Text(
                text = "ஆதார், மாத்திரை நேரம், குறிப்புகளை உடனடியாக பார்க்க கிளிக் செய்யவும்.",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = GlanceTheme.colors.secondary
                )
            )
        }
    }
}
