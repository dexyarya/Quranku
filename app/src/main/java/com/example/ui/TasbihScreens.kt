package com.example.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import kotlinx.coroutines.delay
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.viewmodel.KhutbahViewModel

data class DhikrItem(
    val id: String,
    val title: String,
    val arabic: String,
    val latin: String,
    val translation: String,
    val defaultTarget: Int = 33
)

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun TasbihDigitalScreen(
    viewModel: KhutbahViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    
    // Default presets
    val defaultPresets = remember {
        listOf(
            DhikrItem(
                id = "tasbih",
                title = "Tasbih",
                arabic = "سُبْحَانَ اللهِ",
                latin = "Subhanallah",
                translation = "Maha Suci Allah",
                defaultTarget = 33
            ),
            DhikrItem(
                id = "tahmid",
                title = "Tahmid",
                arabic = "الْحَمْدُ للهِ",
                latin = "Alhamdulillah",
                translation = "Segala Puji Bagi Allah",
                defaultTarget = 33
            ),
            DhikrItem(
                id = "takbir",
                title = "Takbir",
                arabic = "اللهُ أَكْبَرُ",
                latin = "Allahu Akbar",
                translation = "Allah Maha Besar",
                defaultTarget = 33
            ),
            DhikrItem(
                id = "istighfar",
                title = "Istighfar",
                arabic = "أَسْتَغْفِرُ اللهَ الْعَظِيمَ",
                latin = "Astaghfirullahal'adzim",
                translation = "Aku memohon ampun kepada Allah Yang Maha Agung",
                defaultTarget = 33
            ),
            DhikrItem(
                id = "tahlil",
                title = "Tahlil",
                arabic = "لَا إِلَٰهَ إِلَّا اللهُ",
                latin = "La ilaha illallah",
                translation = "Tiada Tuhan selain Allah",
                defaultTarget = 100
            ),
            DhikrItem(
                id = "sholawat",
                title = "Sholawat",
                arabic = "اللَّهُمَّ صَلِّ عَلَى سَيِّدِنَا مُحَمَّدٍ",
                latin = "Allahumma sholli 'ala sayyidina Muhammad",
                translation = "Ya Allah, limpahkanlah rahmat kepada junjungan kami Nabi Muhammad",
                defaultTarget = 100
            ),
            DhikrItem(
                id = "hauqolah",
                title = "Hauqolah",
                arabic = "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللهِ Al-Aliyyil Adzim",
                latin = "La hawla wa la quwwata illa billah al-aliyyil adzim",
                translation = "Tiada daya dan kekuatan melainkan dengan pertolongan Allah Yang Maha Tinggi lagi Maha Agung",
                defaultTarget = 33
            )
        )
    }

    // Load custom dhikr from shared preferences (saved as list serialized manually or from local store if needed; we can manage it easily)
    val prefs = remember { context.getSharedPreferences("tasbih_prefs", Context.MODE_PRIVATE) }
    var customList by remember {
        mutableStateOf(
            loadCustomDhikrList(prefs)
        )
    }

    val allDhikrs = defaultPresets + customList

    // State
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    val selectedDhikr = allDhikrs.getOrElse(selectedIndex) { allDhikrs[0] }
    
    var count by rememberSaveable { mutableIntStateOf(0) }
    var targetLimit by rememberSaveable { mutableIntStateOf(selectedDhikr.defaultTarget) }
    val isUnbounded = targetLimit == 0
    
    // Overall total tasbih accumulated
    var totalSessionCount by rememberSaveable { mutableIntStateOf(prefs.getInt("total_accumulated_count", 0)) }
    
    // Scale animation state
    var buttonPressed by remember { mutableStateOf(false) }
    val buttonScale by animateFloatAsState(
        targetValue = if (buttonPressed) 0.92f else 1.0f,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.45f),
        label = "btn_scale"
    )

    // Option state
    var isVibrationEnabled by rememberSaveable { mutableStateOf(prefs.getBoolean("vibrate_on_click", true)) }
    var isSoundEnabled by rememberSaveable { mutableStateOf(prefs.getBoolean("sound_on_click", false)) }
    
    // Form custom dialog state
    var showAddDhikrDialog by remember { mutableStateOf(false) }
    var showCustomTargetDialog by remember { mutableStateOf(false) }

    fun playClickEffects() {
        if (isVibrationEnabled) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        if (isSoundEnabled) {
            try {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(35)
                    }
                }
            } catch (e: Exception) {
                // fallback
            }
        }
    }

    fun playTargetReachedEffects() {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val pattern = longArrayOf(0, 100, 100, 150, 50, 150, 100, 250)
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(400)
                }
            }
        } catch (e: Exception) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    LaunchedEffect(selectedIndex) {
        // Reset count and update target when selected dhikr switches
        count = 0
        targetLimit = selectedDhikr.defaultTarget
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        // Preset cards slider
        Text(
            text = "Pilih Bacaan Dzikir",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(allDhikrs.size) { index ->
                val item = allDhikrs[index]
                val isSelected = selectedIndex == index
                
                Surface(
                    onClick = { selectedIndex = index },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier
                        .widthIn(min = 100.dp, max = 180.dp)
                        .testTag("preset_${item.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = item.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.latin,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            item {
                // Add custom button
                OutlinedCard(
                    onClick = { showAddDhikrDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .height(58.dp)
                        .testTag("btn_tambah_custom_dzikir")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Tambah Dzikir Kustom",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Kustom Baru",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Selected Bacaan Display Panel
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Arabic
                Text(
                    text = selectedDhikr.arabic,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    lineHeight = 42.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Latin Transliterator
                Text(
                    text = selectedDhikr.latin,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // Meaning
                Text(
                    text = "\"${selectedDhikr.translation}\"",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }
        }

        // Digital counter machine
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Clean, beautiful Target Selection Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Target Hitungan Sesi",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val standardLimits = listOf(33, 99, 100, 0)
                        val isCustomActive = targetLimit !in standardLimits

                        // Display standard target options
                        standardLimits.forEach { limit ->
                            val label = if (limit == 0) "Bebas" else limit.toString()
                            val isActive = targetLimit == limit
                            
                            Surface(
                                onClick = {
                                    targetLimit = limit
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                contentColor = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                border = if (isActive) null else BorderStroke(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                                ),
                                modifier = Modifier.testTag("target_cap_$limit")
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                                )
                            }
                        }

                        // Display dynamic "Lainnya" or Custom Target Picker
                        Surface(
                            onClick = { showCustomTargetDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isCustomActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            contentColor = if (isCustomActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            border = if (isCustomActive) null else BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                            ),
                            modifier = Modifier.testTag("target_cap_custom")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = if (isCustomActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (isCustomActive) targetLimit.toString() else "Lainnya",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // The Big Circular Display Screen & Ripple click trigger combined
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .scale(buttonScale)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null, // Custom scale animation is used
                            onClick = {
                                buttonPressed = true
                                count++
                                val newTotal = totalSessionCount + 1
                                totalSessionCount = newTotal
                                prefs
                                    .edit()
                                    .putInt("total_accumulated_count", newTotal)
                                    .apply()

                                playClickEffects()

                                if (!isUnbounded && count == targetLimit) {
                                    playTargetReachedEffects()
                                }

                                // Reset status safely with a brief timeout structure
                                // simulating a physical mechanical push
                            }
                        )
                        .testTag("tasbih_tap_button"),
                    contentAlignment = Alignment.Center
                ) {
                    // Visual feedback effect on click
                    LaunchedEffect(buttonPressed) {
                        if (buttonPressed) {
                            delay(80)
                            buttonPressed = false
                        }
                    }

                    // Circular target status ring (drawn via layout compose)
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = count.toString(),
                                fontSize = 56.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.height(68.dp)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            val progressLabel = if (isUnbounded) "Hitungan Bebas" else "/ $targetLimit"
                            Text(
                                text = progressLabel,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )

                            if (!isUnbounded && count >= targetLimit) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 8.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Target Tercapai",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom utilities (Reset, Vibration, Sound, Total history)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reset Button
                    OutlinedIconButton(
                        onClick = {
                            count = 0
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        colors = IconButtonDefaults.outlinedIconButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                        modifier = Modifier.size(46.dp).testTag("tasbih_reset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RotateLeft,
                            contentDescription = "Reset Hitungan",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Sound Toggle Button (using physical vibration alternative)
                    IconButton(
                        onClick = {
                            isSoundEnabled = !isSoundEnabled
                            prefs.edit().putBoolean("sound_on_click", isSoundEnabled).apply()
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .background(
                                color = if (isSoundEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isSoundEnabled) Icons.Default.Vibration else Icons.Default.Smartphone,
                            contentDescription = "Geter Kuat",
                            tint = if (isSoundEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Haptic Toggle Button
                    IconButton(
                        onClick = {
                            isVibrationEnabled = !isVibrationEnabled
                            prefs.edit().putBoolean("vibrate_on_click", isVibrationEnabled).apply()
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .background(
                                color = if (isVibrationEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isVibrationEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Haptic Click Feedback",
                            tint = if (isVibrationEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Stats card: Life-time accumulated sessions
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.05f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Total Akumulasi Dzikir",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "$totalSessionCount kali",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    // Add Custom Dialog
    if (showAddDhikrDialog) {
        var inputLabel by remember { mutableStateOf("") }
        var inputArabic by remember { mutableStateOf("") }
        var inputLatin by remember { mutableStateOf("") }
        var inputMeaning by remember { mutableStateOf("") }
        var inputTargetStr by remember { mutableStateOf("33") }
        
        var hasError by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showAddDhikrDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Tambah Dzikir Kustom",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = inputLabel,
                        onValueChange = { inputLabel = it; hasError = false },
                        label = { Text("Nama Dzikir") },
                        placeholder = { Text("misal: Istighfar Pendek") },
                        modifier = Modifier.fillMaxWidth().testTag("custom_name_input"),
                        singleLine = true,
                        isError = hasError && inputLabel.isEmpty()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = inputArabic,
                        onValueChange = { inputArabic = it },
                        label = { Text("Teks Arab (Opsional)") },
                        placeholder = { Text("misal: اَسْتَغْفِرُ اللهَ") },
                        modifier = Modifier.fillMaxWidth().testTag("custom_arabic_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = inputLatin,
                        onValueChange = { inputLatin = it; hasError = false },
                        label = { Text("Teks Transliterasi / Latin") },
                        placeholder = { Text("misal: Astaghfirullah") },
                        modifier = Modifier.fillMaxWidth().testTag("custom_latin_input"),
                        singleLine = true,
                        isError = hasError && inputLatin.isEmpty()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = inputMeaning,
                        onValueChange = { inputMeaning = it },
                        label = { Text("Arti / Makna (Opsional)") },
                        placeholder = { Text("misal: Aku memohon ampunan kepada Allah") },
                        modifier = Modifier.fillMaxWidth().testTag("custom_meaning_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = inputTargetStr,
                        onValueChange = { inputTargetStr = it },
                        label = { Text("Target Sesi") },
                        placeholder = { Text("33, 99, 100, atau 0 untuk bebas") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("custom_target_input"),
                        singleLine = true
                    )

                    if (hasError) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Harap isi Nama Dzikir dan Teks Latin!",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showAddDhikrDialog = false }
                        ) {
                            Text("Batal")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = {
                                if (inputLabel.trim().isEmpty() || inputLatin.trim().isEmpty()) {
                                    hasError = true
                                } else {
                                    val target = inputTargetStr.toIntOrNull() ?: 33
                                    val newItem = DhikrItem(
                                        id = "custom_" + System.currentTimeMillis(),
                                        title = inputLabel.trim(),
                                        arabic = if (inputArabic.trim().isEmpty()) "بِسْمِ اللَّهِ" else inputArabic.trim(),
                                        latin = inputLatin.trim(),
                                        translation = if (inputMeaning.trim().isEmpty()) "Dengan menyebut nama-Mu ya Allah" else inputMeaning.trim(),
                                        defaultTarget = target
                                    )
                                    // Save
                                    customList = customList + newItem
                                    saveCustomDhikrList(prefs, customList)
                                    showAddDhikrDialog = false
                                    
                                    // Auto Select new item
                                    selectedIndex = defaultPresets.size + customList.size - 1
                                }
                            }
                        ) {
                            Text("Simpan")
                        }
                    }
                }
            }
        }
    }

    if (showCustomTargetDialog) {
        var inputTargetStr by remember { mutableStateOf(if (targetLimit !in listOf(33, 99, 100, 0)) targetLimit.toString() else "33") }
        var inputError by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showCustomTargetDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Adjust,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Atur Target Kustom",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = "Masukkan jumlah target hitungan yang Anda inginkan untuk sesi dzikir ini.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = inputTargetStr,
                        onValueChange = { 
                            if (it.all { char -> char.isDigit() }) {
                                inputTargetStr = it
                                inputError = false 
                            }
                        },
                        label = { Text("Jumlah Target") },
                        placeholder = { Text("misal: 1000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = inputError,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_target_limit_field")
                    )

                    if (inputError) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Harap masukkan angka di atas 0!",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showCustomTargetDialog = false }
                        ) {
                            Text("Batal")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = {
                                val value = inputTargetStr.toIntOrNull()
                                if (value == null || value <= 0) {
                                    inputError = true
                                } else {
                                    targetLimit = value
                                    showCustomTargetDialog = false
                                }
                            }
                        ) {
                            Text("Simpan Target")
                        }
                    }
                }
            }
        }
    }
}

private fun loadCustomDhikrList(prefs: android.content.SharedPreferences): List<DhikrItem> {
    val items = mutableListOf<DhikrItem>()
    val size = prefs.getInt("custom_dhikr_size", 0)
    for (i in 0 until size) {
        val id = prefs.getString("custom_dhikr_${i}_id", "") ?: ""
        val title = prefs.getString("custom_dhikr_${i}_title", "") ?: ""
        val arabic = prefs.getString("custom_dhikr_${i}_arabic", "") ?: ""
        val latin = prefs.getString("custom_dhikr_${i}_latin", "") ?: ""
        val trans = prefs.getString("custom_dhikr_${i}_trans", "") ?: ""
        val target = prefs.getInt("custom_dhikr_${i}_target", 33)
        if (id.isNotEmpty() && title.isNotEmpty()) {
            items.add(DhikrItem(id, title, arabic, latin, trans, target))
        }
    }
    return items
}

private fun saveCustomDhikrList(prefs: android.content.SharedPreferences, list: List<DhikrItem>) {
    val editor = prefs.edit()
    editor.putInt("custom_dhikr_size", list.size)
    list.forEachIndexed { i, item ->
        editor.putString("custom_dhikr_${i}_id", item.id)
        editor.putString("custom_dhikr_${i}_title", item.title)
        editor.putString("custom_dhikr_${i}_arabic", item.arabic)
        editor.putString("custom_dhikr_${i}_latin", item.latin)
        editor.putString("custom_dhikr_${i}_trans", item.translation)
        editor.putInt("custom_dhikr_${i}_target", item.defaultTarget)
    }
    editor.apply()
}
