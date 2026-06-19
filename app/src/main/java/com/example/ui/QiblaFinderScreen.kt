package com.example.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.GeomagneticField
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CompassCalibration
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodel.KhutbahViewModel
import java.util.*
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaFinderScreen(
    viewModel: KhutbahViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    
    val userLocationName by viewModel.userLocationName.collectAsStateWithLifecycle()
    val userLatitude by viewModel.userLatitude.collectAsStateWithLifecycle()
    val userLongitude by viewModel.userLongitude.collectAsStateWithLifecycle()

    // Calculate pure Qibla bearing from True North
    val qiblaAngle = remember(userLatitude, userLongitude) {
        calculateQiblaDirection(userLatitude, userLongitude)
    }

    // Calculate distance of current position to Mecca Kaaba (Haversine formula in Km)
    val distanceToKaaba = remember(userLatitude, userLongitude) {
        calculateDistanceToKaaba(userLatitude, userLongitude)
    }

    // Calculate Geomagnetic Declination for True North correction
    val declination = remember(userLatitude, userLongitude) {
        try {
            val geomagneticField = GeomagneticField(
                userLatitude.toFloat(),
                userLongitude.toFloat(),
                0f,
                System.currentTimeMillis()
            )
            geomagneticField.declination
        } catch (e: Exception) {
            0f
        }
    }

    // Compass sensor heading and level logics
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    var rawHeading by remember { mutableStateOf(0f) }
    var isSensorAvailable by remember { mutableStateOf(true) }
    var sensorAccuracy by remember { mutableIntStateOf(SensorManager.SENSOR_STATUS_ACCURACY_HIGH) }
    
    // Device structural levelling angles (pitch and roll)
    var pitchState by remember { mutableStateOf(0f) }
    var rollState by remember { mutableStateOf(0f) }
    var tiltAngleState by remember { mutableStateOf(0f) }

    // Smooth magnetic interpolation state
    val smoothHeading by animateFloatAsState(
        targetValue = rawHeading,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "compass_smooth"
    )

    // Manual slider calibration fallback if sensor doesn't exist
    var manualHeading by remember { mutableStateOf(0f) }

    DisposableEffect(declination) {
        val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        
        if (accelSensor == null || magnetSensor == null) {
            isSensorAvailable = false
        }
        
        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)
        var hasGravity = false
        var hasGeomagnetic = false
        
        // Low-pass filter smoothing coefficient (high stability, zero micro-shakes)
        val alphaSmooth = 0.12f
        
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    if (!hasGravity) {
                        System.arraycopy(event.values, 0, gravity, 0, 3)
                        hasGravity = true
                    } else {
                        for (i in 0..2) {
                            gravity[i] = gravity[i] + alphaSmooth * (event.values[i] - gravity[i])
                        }
                    }
                    
                    // Device structural tilt calculation for bubble waterpass (using roll and pitch)
                    val x = gravity[0]
                    val y = gravity[1]
                    val z = gravity[2]
                    val totalG = sqrt(x * x + y * y + z * z)
                    if (totalG > 0.1f) {
                        val normZ = (z / totalG).coerceIn(-1.0f, 1.0f)
                        val tilt = Math.toDegrees(acos(abs(normZ).toDouble())).toFloat()
                        tiltAngleState = tilt
                        
                        // Normalized tilt factors scale down to reactive offsets (-1f to +1f)
                        // A 5m/s2 offset represents roughly ~30deg steep tilt
                        rollState = (x / 5f).coerceIn(-1f, 1f)
                        pitchState = (y / 5f).coerceIn(-1f, 1f)
                    }
                } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    sensorAccuracy = event.accuracy
                    if (!hasGeomagnetic) {
                        System.arraycopy(event.values, 0, geomagnetic, 0, 3)
                        hasGeomagnetic = true
                    } else {
                        for (i in 0..2) {
                            geomagnetic[i] = geomagnetic[i] + alphaSmooth * (event.values[i] - geomagnetic[i])
                        }
                    }
                }
                
                if (hasGravity && hasGeomagnetic) {
                    val r = FloatArray(9)
                    val i = FloatArray(9)
                    if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
                        val orientation = FloatArray(3)
                        SensorManager.getOrientation(r, orientation)
                        val azimuthRad = orientation[0]
                        var azimuthDeg = Math.toDegrees(azimuthRad.toDouble()).toFloat()
                        azimuthDeg = (azimuthDeg + 360f) % 360f
                        
                        // Core correction: translate magnetic north reading into true geographic north heading using declination
                        val trueHeadingDeg = (azimuthDeg + declination + 360f) % 360f
                        
                        // Smoothly handle wrap-around without rotation loops or jerky spins
                        var angleDiff = trueHeadingDeg - rawHeading
                        angleDiff = (angleDiff + 180f) % 360f
                        if (angleDiff < 0) {
                            angleDiff += 360f
                        }
                        val shortestDiff = angleDiff - 180f
                        
                        rawHeading += shortestDiff
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                if (sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    sensorAccuracy = accuracy
                }
            }
        }
        
        if (isSensorAvailable) {
            sensorManager.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_UI)
            sensorManager.registerListener(listener, magnetSensor, SensorManager.SENSOR_DELAY_UI)
        }
        
        onDispose {
            if (isSensorAvailable) {
                sensorManager.unregisterListener(listener)
            }
        }
    }

    val finalHeading = if (isSensorAvailable) {
        ((smoothHeading % 360f) + 360f) % 360f
    } else {
        manualHeading
    }
    
    // Relative angle of the Qibla seeker pointer relative to the top of the user's phone screen
    val relativeQiblaAngle = (qiblaAngle - finalHeading + 360) % 360

    // Is the phone pointed directly to Mecca (margin of error ±3.5 degrees)
    val isAligned = remember(relativeQiblaAngle) {
        val diff = abs(relativeQiblaAngle)
        diff <= 3.5 || diff >= 356.5
    }

    // Device is flat enough for perfect magnetic field calculation (maximum ±8.5 degrees tilt)
    val isLevel = isSensorAvailable && (tiltAngleState <= 8.5f)

    // Trigger physical touch vibration pulse when locked in the perfect direction
    LaunchedEffect(isAligned) {
        if (isAligned) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    var showHelpDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Kompas Kiblat",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("qibla_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali ke Beranda"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Petunjuk Penggunaan",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            val screenWidth = maxWidth
            val screenHeight = maxHeight
            val isLandscape = screenWidth > screenHeight

            if (isLandscape) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        DetailsCard(
                            userLocationName = userLocationName,
                            userLatitude = userLatitude,
                            userLongitude = userLongitude,
                            distanceToKaaba = distanceToKaaba,
                            qiblaAngle = qiblaAngle,
                            isSensorAvailable = isSensorAvailable,
                            sensorAccuracy = sensorAccuracy
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        GuidanceControls(
                            isAligned = isAligned,
                            isLevel = isLevel,
                            isSensorAvailable = isSensorAvailable,
                            relativeQiblaAngle = relativeQiblaAngle,
                            manualHeading = manualHeading,
                            onManualHeadingChange = { manualHeading = it }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        val computedSize = minOf(screenWidth * 0.45f, screenHeight * 0.85f)
                        val boundedCompassSize = computedSize.coerceIn(160.dp, 320.dp)
                        
                        CompassDial(
                            boundedCompassSize = boundedCompassSize,
                            isAligned = isAligned,
                            isLevel = isLevel,
                            finalHeading = finalHeading,
                            qiblaAngle = qiblaAngle,
                            relativeQiblaAngle = relativeQiblaAngle,
                            rollState = rollState,
                            pitchState = pitchState,
                            primaryColor = MaterialTheme.colorScheme.primary,
                            onSurfaceColor = MaterialTheme.colorScheme.onSurface,
                            textSecondary = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                        )
                    }
                }
            } else {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val boundedCompassSize = (screenWidth * 0.75f).coerceIn(180.dp, 280.dp)
                        
                        CompassDial(
                            boundedCompassSize = boundedCompassSize,
                            isAligned = isAligned,
                            isLevel = isLevel,
                            finalHeading = finalHeading,
                            qiblaAngle = qiblaAngle,
                            relativeQiblaAngle = relativeQiblaAngle,
                            rollState = rollState,
                            pitchState = pitchState,
                            primaryColor = MaterialTheme.colorScheme.primary,
                            onSurfaceColor = MaterialTheme.colorScheme.onSurface,
                            textSecondary = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                        )
                    }

                    DetailsCard(
                        userLocationName = userLocationName,
                        userLatitude = userLatitude,
                        userLongitude = userLongitude,
                        distanceToKaaba = distanceToKaaba,
                        qiblaAngle = qiblaAngle,
                        isSensorAvailable = isSensorAvailable,
                        sensorAccuracy = sensorAccuracy
                    )

                    GuidanceControls(
                        isAligned = isAligned,
                        isLevel = isLevel,
                        isSensorAvailable = isSensorAvailable,
                        relativeQiblaAngle = relativeQiblaAngle,
                        manualHeading = manualHeading,
                        onManualHeadingChange = { manualHeading = it }
                    )
                }
            }
        }
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Panduan Mencari Kiblat",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Aplikasi Quran Ku menghitung sudut arah kiblat dari koordinat lokasi Anda saat ini langsung ke koordinat Ka'bah di Makkah (21.4225° N, 39.8262° E).",
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    Text(
                        text = "1. Pastikan GPS dan Layanan Lokasi Anda aktif dan akurat untuk menghitung bearing kiblat yang presisi.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Text(
                        text = "2. Letakkan ponsel mendatar (sejajar dengan tanah/lantai) agar sensor magnetik internal bekerja maksimal.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Text(
                        text = "3. Jauhkan ponsel dari benda logam berat, komponen elektronik, atau medan magnet kuat yang bisa mengganggu akurasi sensor magnetik.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Text(
                        text = "4. Putar tubuh dan ponsel Anda secara perlahan hingga indikator lingkaran berubah warna menjadi hijau mantap dan bergetar.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showHelpDialog = false }) {
                    Text("Saya Mengerti")
                }
            }
        )
    }
}

@Composable
private fun DetailsCard(
    userLocationName: String,
    userLatitude: Double,
    userLongitude: Double,
    distanceToKaaba: Double,
    qiblaAngle: Double,
    isSensorAvailable: Boolean,
    sensorAccuracy: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "Location Pin",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "LOKASI SAAT INI",
                        fontSize = 10.sp,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = userLocationName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f°", qiblaAngle),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "KOORDINAT",
                        fontSize = 9.sp,
                        letterSpacing = 0.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = String.format(Locale.US, "%.4f°, %.4f°", userLatitude, userLongitude),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "JARAK KE KA'BAH",
                        fontSize = 9.sp,
                        letterSpacing = 0.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = String.format(Locale.US, "%,.0f km", distanceToKaaba),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.tertiary.takeOrElse { Color(0xFFD4AF37) }
                    )
                }
            }
            
            if (isSensorAvailable) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = when (sensorAccuracy) {
                                SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> Color(0xFF4CAF50).copy(alpha = 0.08f)
                                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> Color(0xFFFF9800).copy(alpha = 0.08f)
                                else -> Color(0xFFF44336).copy(alpha = 0.08f)
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                              .background(
                                  color = when (sensorAccuracy) {
                                      SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> Color(0xFF4CAF50)
                                      SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> Color(0xFFFF9800)
                                      else -> Color(0xFFF44336)
                                  },
                                  shape = CircleShape
                              )
                      )
                      Spacer(modifier = Modifier.width(8.dp))
                      Text(
                          text = when (sensorAccuracy) {
                              SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "Presisi Tinggi (Kompas Handal)"
                              SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "Presisi Sedang (Kalibrasi Ringan)"
                              else -> "Akurasi Rendah! Buat Gerakan Angka 8 untuk Kalibrasi"
                          },
                          fontSize = 10.sp,
                          fontWeight = FontWeight.Bold,
                          color = when (sensorAccuracy) {
                              SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> Color(0xFF2E7D32)
                              SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> Color(0xFFEF6C00)
                              else -> Color(0xFFC62828)
                          }
                      )
                  }
              }
          }
      }
  }

  @Composable
  private fun GuidanceControls(
      isAligned: Boolean,
      isLevel: Boolean,
      isSensorAvailable: Boolean,
      relativeQiblaAngle: Double,
      manualHeading: Float,
      onManualHeadingChange: (Float) -> Unit
  ) {
      Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.fillMaxWidth()
      ) {
          AnimatedContent(
              targetState = isAligned,
              transitionSpec = {
                  fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(90))
              },
              label = "aligned_status"
          ) { targetAligned ->
              if (targetAligned) {
                  Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier
                          .background(
                              color = Color(0xFF10B981).copy(alpha = 0.12f),
                              shape = RoundedCornerShape(12.dp)
                          )
                          .border(1.1.dp, Color(0xFF10B981).copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                          .padding(horizontal = 24.dp, vertical = 12.dp)
                  ) {
                      Icon(
                          imageVector = Icons.Default.CheckCircle,
                          contentDescription = "Selesai",
                          tint = Color(0xFF10B981),
                          modifier = Modifier.size(20.dp)
                      )
                      Spacer(modifier = Modifier.width(10.dp))
                      Text(
                          text = "Kiblat Terkunci! Menghadap Ka'bah",
                          color = Color(0xFF065F46),
                          fontWeight = FontWeight.ExtraBold,
                          fontSize = 14.sp
                      )
                  }
              } else {
                  Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier
                          .background(
                              color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                              shape = RoundedCornerShape(12.dp)
                          )
                          .border(1.1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                          .padding(horizontal = 18.dp, vertical = 12.dp)
                  ) {
                      Icon(
                          imageVector = Icons.Default.CompassCalibration,
                          contentDescription = "Putar Ponsel",
                          tint = MaterialTheme.colorScheme.primary,
                          modifier = Modifier.size(18.dp)
                      )
                      Spacer(modifier = Modifier.width(10.dp))
                      val roundedDiff = ((relativeQiblaAngle + 180) % 360 - 180).toInt()
                      val turnInstruction = if (roundedDiff > 0) {
                          "Putar Kanan ${abs(roundedDiff)}°"
                      } else {
                          "Putar Kiri ${abs(roundedDiff)}°"
                      }
                      Text(
                          text = "$turnInstruction untuk menyelaraskan",
                          color = MaterialTheme.colorScheme.onSurface,
                          fontWeight = FontWeight.Bold,
                          fontSize = 14.sp
                      )
                  }
              }
          }

          Spacer(modifier = Modifier.height(10.dp))

          AnimatedVisibility(
              visible = isSensorAvailable && !isLevel,
              enter = fadeIn() + expandVertically(),
              exit = fadeOut() + shrinkVertically()
          ) {
              Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier
                      .padding(horizontal = 16.dp)
                      .background(
                          color = Color(0xFFFF9800).copy(alpha = 0.08f),
                          shape = RoundedCornerShape(10.dp)
                      )
                      .border(1.dp, Color(0xFFFF9800).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                      .padding(horizontal = 12.dp, vertical = 8.dp)
              ) {
                  Icon(
                      imageVector = Icons.Default.Info,
                      contentDescription = "Rekomendasi Level",
                      tint = Color(0xFFFF9800),
                      modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                      text = "Posisikan ponsel mendatar agar kompas lebih akurat",
                      color = Color(0xFFE65100),
                      fontWeight = FontWeight.SemiBold,
                      fontSize = 11.sp
                  )
              }
          }

          Spacer(modifier = Modifier.height(6.dp))

          if (!isSensorAvailable) {
              Box(
                  modifier = Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 12.dp)
                      .background(
                          color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
                          shape = RoundedCornerShape(14.dp)
                      )
                      .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                      .padding(14.dp)
              ) {
                  Column {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                          Icon(
                              imageVector = Icons.Default.CompassCalibration,
                              contentDescription = "No sensor",
                              tint = MaterialTheme.colorScheme.error,
                              modifier = Modifier.size(18.dp)
                          )
                          Spacer(modifier = Modifier.width(8.dp))
                          Text(
                              text = "Sensor Kompas Tidak Ditemukan",
                              fontSize = 13.sp,
                              fontWeight = FontWeight.Bold,
                              color = MaterialTheme.colorScheme.onErrorContainer
                          )
                      }
                      Spacer(modifier = Modifier.height(6.dp))
                      Text(
                          text = "Perangkat Anda tidak memiliki sensor kompas magnetik. Gunakan simulasi manual kemudi di bawah ini:",
                          fontSize = 12.sp,
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                      Spacer(modifier = Modifier.height(10.dp))
                      
                      Row(
                          verticalAlignment = Alignment.CenterVertically,
                          modifier = Modifier.fillMaxWidth()
                      ) {
                          Text("Utara:", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(44.dp))
                          Slider(
                              value = manualHeading,
                              onValueChange = onManualHeadingChange,
                              valueRange = 0f..360f,
                              colors = SliderDefaults.colors(
                                  thumbColor = MaterialTheme.colorScheme.primary,
                                  activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                              ),
                              modifier = Modifier.weight(1f)
                          )
                          Text("${manualHeading.toInt()}°", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(44.dp), textAlign = TextAlign.End)
                      }
                  }
              }
          } else {
              Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.Center,
                  modifier = Modifier.padding(vertical = 4.dp)
              ) {
                  Icon(
                      imageVector = Icons.Default.CompassCalibration,
                      contentDescription = "Status Sensor",
                      tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                      modifier = Modifier.size(12.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                      text = "Kalibrasi kompas dengan mengayunkan ponsel membentuk angka 8",
                      fontSize = 11.sp,
                      textAlign = TextAlign.Center,
                      color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                      fontWeight = FontWeight.Medium
                  )
              }
          }
      }
  }

  @Composable
  private fun CompassDial(
      boundedCompassSize: androidx.compose.ui.unit.Dp,
      isAligned: Boolean,
      isLevel: Boolean,
      finalHeading: Float,
      qiblaAngle: Double,
      relativeQiblaAngle: Double,
      rollState: Float,
      pitchState: Float,
      primaryColor: Color,
      onSurfaceColor: Color,
      textSecondary: Color
  ) {
      val animatedGlowSize by animateDpAsState(
          targetValue = if (isAligned) (boundedCompassSize + 30.dp) else (boundedCompassSize - 10.dp),
          animationSpec = tween(700, easing = LinearOutSlowInEasing),
          label = "glow_size"
      )
      val animatedGlowAlpha by animateFloatAsState(
          targetValue = if (isAligned) 0.22f else 0.05f,
          animationSpec = tween(500),
          label = "glow_alpha"
      )
      val alignmentTransitionColor by animateColorAsState(
          targetValue = if (isAligned) Color(0xFF10B981) else primaryColor,
          animationSpec = tween(400),
          label = "alignment_color"
      )

      Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier.size(boundedCompassSize + 36.dp)
      ) {
          Box(
              modifier = Modifier
                  .size(animatedGlowSize)
                  .background(
                      brush = Brush.radialGradient(
                          colors = listOf(
                              alignmentTransitionColor,
                              Color.Transparent
                          )
                      ),
                      shape = CircleShape
                  )
                  .graphicsLayer(alpha = animatedGlowAlpha)
          )

          Box(
              modifier = Modifier
                  .size(boundedCompassSize)
                  .background(
                      brush = Brush.radialGradient(
                          colors = listOf(
                              MaterialTheme.colorScheme.surface,
                              MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                          )
                      ),
                      shape = CircleShape
                  )
                  .border(
                      width = if (isAligned) 6.dp else 2.dp,
                      color = alignmentTransitionColor.copy(alpha = if (isAligned) 1f else 0.35f),
                      shape = CircleShape
                  )
                  .padding((boundedCompassSize.value * 0.05f).dp),
              contentAlignment = Alignment.Center
          ) {
              Canvas(
                  modifier = Modifier
                      .fillMaxSize()
                      .rotate(-finalHeading)
              ) {
                  val canvasSize = size.width
                  val center = Offset(canvasSize / 2, canvasSize / 2)
                  val radius = canvasSize / 2

                  drawCircle(
                      color = textSecondary.copy(alpha = 0.06f),
                      radius = radius * 0.85f,
                      style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                  )
                  drawCircle(
                      color = textSecondary.copy(alpha = 0.06f),
                      radius = radius * 0.65f,
                      style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                  )

                  val sweepAngle = 30f
                  val qiblaStartAngle = (qiblaAngle.toFloat() - 90f) - (sweepAngle / 2f)
                  drawArc(
                      brush = Brush.radialGradient(
                          colors = listOf(
                              Color(0xFFFFD4AF37).copy(alpha = 0.22f),
                              Color(0xFFFFD4AF37).copy(alpha = 0.04f),
                              Color.Transparent
                          ),
                          center = center,
                          radius = radius
                      ),
                      startAngle = qiblaStartAngle,
                      sweepAngle = sweepAngle,
                      useCenter = true
                  )

                  for (angle in 0 until 360 step 5) {
                      val angleRad = Math.toRadians(angle.toDouble())
                      val isMain = angle % 90 == 0
                      val isSub = angle % 30 == 0 && !isMain
                      val isFine = !isMain && !isSub && angle % 15 != 0
                      
                      val startLength = if (isMain) radius * 0.1f else if (isSub) radius * 0.07f else radius * 0.035f
                      val strokeWidth = if (isMain) radius * 0.015f else if (isSub) radius * 0.01f else radius * 0.005f
                      
                      val startX = (center.x + (radius - startLength) * sin(angleRad)).toFloat()
                      val startY = (center.y - (radius - startLength) * cos(angleRad)).toFloat()
                      val endX = (center.x + radius * sin(angleRad)).toFloat()
                      val endY = (center.y - radius * cos(angleRad)).toFloat()

                      val color = if (isMain && angle == 0) Color(0xFFEF4444)
                      else if (isMain) primaryColor
                      else textSecondary.copy(alpha = if (isFine) 0.25f else 0.6f)

                      drawLine(
                          color = color,
                          start = Offset(startX, startY),
                          end = Offset(endX, endY),
                          strokeWidth = strokeWidth
                      )
                  }

                  for (degLabel in 30..330 step 30) {
                      if (degLabel % 90 == 0) continue
                      drawDegreesLabel(degLabel.toString(), degLabel.toFloat(), radius, textSecondary, center)
                  }

                  drawCompassLabel("U", 0f, radius, Color(0xFFEF4444), center)
                  drawCompassLabel("T", 90f, radius, onSurfaceColor, center)
                  drawCompassLabel("S", 180f, radius, onSurfaceColor, center)
                  drawCompassLabel("B", 270f, radius, onSurfaceColor, center)

                  rotate(qiblaAngle.toFloat(), pivot = center) {
                      drawCircle(
                          color = Color(0xFFFFD4AF37),
                          radius = radius * 0.06f,
                          center = Offset(center.x, center.y - radius + radius * 0.15f)
                      )
                      drawCircle(
                          color = Color.White,
                          radius = radius * 0.03f,
                          center = Offset(center.x, center.y - radius + radius * 0.15f)
                      )
                  }
              }

              Box(
                  modifier = Modifier
                      .fillMaxSize()
                      .rotate(relativeQiblaAngle.toFloat())
                      .padding((boundedCompassSize.value * 0.12f).dp),
                  contentAlignment = Alignment.Center
              ) {
                  Canvas(modifier = Modifier.fillMaxSize()) {
                      val canvasSize = size.width
                      val center = Offset(canvasSize / 2, canvasSize / 2)

                      val needleStartColor = if (isAligned) Color(0xFF10B981) else Color(0xFFFFF176)
                      val needleEndColor = if (isAligned) Color(0xFF059669) else Color(0xFFFFD4AF37)

                      val pointerPath = Path().apply {
                          moveTo(center.x, canvasSize * 0.02f)
                          lineTo(center.x - canvasSize * 0.08f, center.y - canvasSize * 0.1f)
                          lineTo(center.x - canvasSize * 0.025f, center.y - canvasSize * 0.08f)
                          lineTo(center.x - canvasSize * 0.025f, center.y + canvasSize * 0.2f)
                          lineTo(center.x + canvasSize * 0.025f, center.y + canvasSize * 0.2f)
                          lineTo(center.x + canvasSize * 0.025f, center.y - canvasSize * 0.08f)
                          lineTo(center.x + canvasSize * 0.08f, center.y - canvasSize * 0.1f)
                          close()
                      }

                      drawPath(
                          path = pointerPath,
                          brush = Brush.verticalGradient(
                              colors = listOf(needleStartColor, needleEndColor)
                          )
                      )

                      drawLine(
                          color = Color.Black.copy(alpha = 0.12f),
                          start = Offset(center.x, canvasSize * 0.03f),
                          end = Offset(center.x, center.y + canvasSize * 0.2f),
                          strokeWidth = (canvasSize * 0.01f).coerceIn(1.5f, 3f)
                      )
                  }

                  Box(
                      modifier = Modifier
                          .align(Alignment.TopCenter)
                          .offset(y = (-boundedCompassSize * 0.03f))
                          .size((boundedCompassSize.value * 0.11f).coerceIn(24f, 36f).dp)
                          .background(
                              brush = Brush.radialGradient(
                                  colors = listOf(Color(0xFFFFF9C4), Color(0xFFFFD4AF37))
                              ),
                              shape = CircleShape
                          )
                          .border(1.5.dp, Color.White, CircleShape),
                      contentAlignment = Alignment.Center
                  ) {
                      Icon(
                          imageVector = Icons.Default.Mosque,
                          contentDescription = null,
                          tint = Color(0xFF131D19),
                          modifier = Modifier.size((boundedCompassSize.value * 0.057f).coerceIn(12f, 18f).dp)
                      )
                  }
              }

              val bubbleLevelSize = (boundedCompassSize.value * 0.27f).coerceIn(55f, 80f).dp
              val bubbleTravelFactor = (boundedCompassSize.value * 0.08f).coerceIn(14f, 24f)

              Box(
                  modifier = Modifier
                      .size(bubbleLevelSize)
                      .background(
                          brush = Brush.verticalGradient(
                              colors = listOf(
                                  MaterialTheme.colorScheme.surface,
                                  MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                              )
                          ),
                          shape = CircleShape
                      )
                      .border(
                          width = 2.dp,
                          color = if (isLevel) Color(0xFF10B981) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                          shape = CircleShape
                      ),
                  contentAlignment = Alignment.Center
              ) {
                  Canvas(modifier = Modifier.size((bubbleLevelSize.value * 0.31f).dp)) {
                      val c = Offset(size.width / 2, size.width / 2)
                      drawCircle(
                          color = if (isLevel) Color(0xFF10B981).copy(alpha = 0.4f) else primaryColor.copy(alpha = 0.18f),
                          radius = size.width / 2,
                          style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                      )
                      drawLine(
                          color = if (isLevel) Color(0xFF10B981).copy(alpha = 0.25f) else primaryColor.copy(alpha = 0.12f),
                          start = Offset(c.x, 0f),
                          end = Offset(c.x, size.height),
                          strokeWidth = 1.dp.toPx()
                      )
                      drawLine(
                          color = if (isLevel) Color(0xFF10B981).copy(alpha = 0.25f) else primaryColor.copy(alpha = 0.12f),
                          start = Offset(0f, c.y),
                          end = Offset(size.width, c.y),
                          strokeWidth = 1.dp.toPx()
                      )
                  }

                  val bubbleColor = if (isLevel) Color(0xFF10B981) else Color(0xFFFF9800)
                  
                  Box(
                      modifier = Modifier
                          .offset(
                              x = (-rollState * bubbleTravelFactor).dp,
                              y = (pitchState * bubbleTravelFactor).dp
                          )
                          .size((bubbleLevelSize.value * 0.2f).coerceIn(10f, 16f).dp)
                          .background(
                              brush = Brush.radialGradient(
                                  colors = listOf(
                                      bubbleColor,
                                      bubbleColor.copy(alpha = 0.7f),
                                      Color.Transparent
                                  )
                              ),
                              shape = CircleShape
                          )
                          .border(1.1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                          .graphicsLayer(shadowElevation = 3f)
                  )

                  Column(
                      horizontalAlignment = Alignment.CenterHorizontally,
                      verticalArrangement = Arrangement.Center,
                      modifier = Modifier.fillMaxSize()
                  ) {
                      Text(
                          text = String.format(Locale.getDefault(), "%d°", finalHeading.toInt()),
                          fontSize = (bubbleLevelSize.value * 0.158f).coerceIn(9f, 13f).sp,
                          fontWeight = FontWeight.Black,
                          color = MaterialTheme.colorScheme.onSurface,
                          modifier = Modifier
                              .background(
                                  color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
                                  shape = RoundedCornerShape(4.dp)
                              )
                              .padding(horizontal = 4.dp, vertical = 1.dp)
                      )
                      Text(
                          text = getDirectionAbbreviation(finalHeading),
                          fontSize = (bubbleLevelSize.value * 0.1f).coerceIn(7f, 10f).sp,
                          color = if (isLevel) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                          fontWeight = FontWeight.ExtraBold
                      )
                  }
              }
          }
      }
  }

private fun DrawScope.drawDegreesLabel(
    text: String,
    angle: Float,
    radius: Float,
    color: Color,
    center: Offset
) {
    val angleRad = Math.toRadians(angle.toDouble())
    val textYOffset = radius * 0.015f
    val textX = (center.x + (radius * 0.85f) * sin(angleRad)).toFloat()
    val textY = (center.y - (radius * 0.85f) * cos(angleRad)).toFloat()

    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        textSize = radius * 0.055f
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.NORMAL)
        this.color = color.hashCode()
        textAlign = android.graphics.Paint.Align.CENTER
    }

    drawContext.canvas.nativeCanvas.drawText(
        text,
        textX,
        textY + textYOffset,
        paint
    )
}

private fun DrawScope.drawCompassLabel(
    text: String,
    angle: Float,
    radius: Float,
    color: Color,
    center: Offset
) {
    val angleRad = Math.toRadians(angle.toDouble())
    val textYOffset = radius * 0.022f
    val textX = (center.x + (radius * 0.76f) * sin(angleRad)).toFloat()
    val textY = (center.y - (radius * 0.76f) * cos(angleRad)).toFloat()

    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        textSize = radius * 0.10f
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        this.color = color.hashCode()
        textAlign = android.graphics.Paint.Align.CENTER
    }

    drawContext.canvas.nativeCanvas.drawText(
        text,
        textX,
        textY + textYOffset,
        paint
    )
}

// Great Circle Bearing calculation (Qibla direction)
private fun calculateQiblaDirection(latitude: Double, longitude: Double): Double {
    val phiReal = Math.toRadians(latitude)
    val lambdaReal = Math.toRadians(longitude)
    val phiKaaba = Math.toRadians(21.4225241)
    val lambdaKaaba = Math.toRadians(39.826206)
    
    val deltaLambda = lambdaKaaba - lambdaReal
    
    val y = sin(deltaLambda)
    val x = cos(phiReal) * tan(phiKaaba) - sin(phiReal) * cos(deltaLambda)
    
    val qiblaRad = atan2(y, x)
    var qiblaDeg = Math.toDegrees(qiblaRad)
    
    qiblaDeg = (qiblaDeg + 360.0) % 360.0
    return qiblaDeg
}

// Great Circle Distance calculation (Haversine formula in Km)
private fun calculateDistanceToKaaba(latitude: Double, longitude: Double): Double {
    val r = 6371.0 // Earth radius in km
    val lat1 = Math.toRadians(latitude)
    val lng1 = Math.toRadians(longitude)
    val lat2 = Math.toRadians(21.4225241)
    val lng2 = Math.toRadians(39.826206)
    
    val dLat = lat2 - lat1
    val dLng = lng2 - lng1
    
    val dLatSin = sin(dLat / 2)
    val dLngSin = sin(dLng / 2)
    val a = dLatSin * dLatSin + cos(lat1) * cos(lat2) * dLngSin * dLngSin
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}

// Convert angle back to standard Indonesian direction string
private fun getDirectionAbbreviation(deg: Float): String {
    val degreeNormalized = (deg + 360) % 360
    return when {
        degreeNormalized >= 337.5 || degreeNormalized < 22.5 -> "U"  // Utara
        degreeNormalized >= 22.5 && degreeNormalized < 67.5 -> "TL"  // Timur Laut
        degreeNormalized >= 67.5 && degreeNormalized < 112.5 -> "T"  // Timur
        degreeNormalized >= 112.5 && degreeNormalized < 157.5 -> "TG" // Tenggara
        degreeNormalized >= 157.5 && degreeNormalized < 202.5 -> "S"  // Selatan
        degreeNormalized >= 202.5 && degreeNormalized < 247.5 -> "BD" // Barat Daya
        degreeNormalized >= 247.5 && degreeNormalized < 292.5 -> "B"  // Barat
        else -> "BL" // Barat Laut
    }
}
