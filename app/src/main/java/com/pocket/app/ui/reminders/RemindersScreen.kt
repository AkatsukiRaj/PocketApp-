package com.pocket.app.ui.reminders

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pocket.app.data.model.ItemCategory
import com.pocket.app.data.model.PocketItem
import com.pocket.app.ui.viewmodel.PocketViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    viewModel: PocketViewModel,
    onBack: () -> Unit
) {
    val reminders by viewModel.reminders.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<PocketItem?>(null) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Reminders (நினைவூட்டல்)", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("மாத்திரை நேரம் & கட்டண நினைவூட்டல்", fontSize = 12.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFFD97706),
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Default.AddAlarm, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("புது அலாரம் (New Alarm)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (reminders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.NotificationsActive,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "நினைவூட்டல்கள் எதுவும் இல்லை.",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Text(
                            "மாத்திரை அல்லது கட்டண நேரத்திற்கு அலாரம் வைக்கலாம்.",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(reminders) { reminder ->
                        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                        val timeStr = reminder.reminderTime?.let { timeFormat.format(Date(it)) } ?: "--:--"

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (reminder.isAlarmActive) Color.White else Color(0xFFF1F5F9)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Surface(
                                        color = if (reminder.category == ItemCategory.MEDICAL) Color(0xFFFEF3C7) else Color(0xFFDBEAFE),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            reminder.category.tamilName,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (reminder.category == ItemCategory.MEDICAL) Color(0xFF92400E) else Color(0xFF1E40AF),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        timeStr,
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (reminder.isAlarmActive) Color(0xFF0F172A) else Color.Gray
                                    )
                                    Text(
                                        reminder.title,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (reminder.isAlarmActive) Color(0xFF1E293B) else Color.Gray
                                    )
                                    if (reminder.description.isNotBlank()) {
                                        Text(
                                            reminder.description,
                                            fontSize = 13.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Switch(
                                        checked = reminder.isAlarmActive,
                                        onCheckedChange = { viewModel.toggleAlarm(reminder) }
                                    )
                                    IconButton(onClick = { itemToDelete = reminder }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Reminder Dialog
    if (showAddDialog) {
        var reminderTitle by remember { mutableStateOf("") }
        var reminderDesc by remember { mutableStateOf("") }
        var chosenCategory by remember { mutableStateOf(ItemCategory.MEDICAL) }
        var selectedCalendar by remember {
            mutableStateOf(Calendar.getInstance().apply {
                add(Calendar.HOUR_OF_DAY, 1)
            })
        }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("புது நினைவூட்டல் (Set Alarm)", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = reminderTitle,
                        onValueChange = { reminderTitle = it },
                        label = { Text("எதற்கு? (எ.கா: சுகர் மாத்திரை)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = reminderDesc,
                        onValueChange = { reminderDesc = it },
                        label = { Text("விவரம் (எ.கா: உணவுக்குப் பின்)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Quick Preset Buttons for Seniors
                    Text("விரைவு நேரம் (Quick Times):", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                selectedCalendar = Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, 8)
                                    set(Calendar.MINUTE, 0)
                                    if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Text("காலை 8 AM", fontSize = 11.sp)
                        }
                        Button(
                            onClick = {
                                selectedCalendar = Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, 13)
                                    set(Calendar.MINUTE, 30)
                                    if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Text("மதியம் 1:30 PM", fontSize = 11.sp)
                        }
                        Button(
                            onClick = {
                                selectedCalendar = Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, 20)
                                    set(Calendar.MINUTE, 0)
                                    if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Text("இரவு 8 PM", fontSize = 11.sp)
                        }
                    }

                    // Custom Time Picker Button
                    OutlinedButton(
                        onClick = {
                            val c = selectedCalendar
                            TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    val newCal = Calendar.getInstance().apply {
                                        set(Calendar.HOUR_OF_DAY, hourOfDay)
                                        set(Calendar.MINUTE, minute)
                                        set(Calendar.SECOND, 0)
                                        if (timeInMillis <= System.currentTimeMillis()) {
                                            add(Calendar.DAY_OF_YEAR, 1)
                                        }
                                    }
                                    selectedCalendar = newCal
                                },
                                c.get(Calendar.HOUR_OF_DAY),
                                c.get(Calendar.MINUTE),
                                false
                            ).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                        Icon(Icons.Default.AccessTime, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("நேரம் மாற்றுக: ${sdf.format(selectedCalendar.time)}", fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reminderTitle.isNotBlank()) {
                            viewModel.addReminder(
                                title = reminderTitle,
                                desc = reminderDesc,
                                timeMillis = selectedCalendar.timeInMillis,
                                category = chosenCategory
                            )
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                ) {
                    Text("Set Alarm (அலாரம் வை)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete confirmation
    itemToDelete?.let { rem ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("அலாரத்தை நீக்க வேண்டுமா?") },
            text = { Text("\"${rem.title}\" அலாரம் நீக்கப்படும்.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteItem(rem)
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
