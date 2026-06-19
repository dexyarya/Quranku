package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SurahModel
import com.example.ui.viewmodel.KhutbahViewModel
import com.example.ui.viewmodel.QuranUiState

data class TahlilGuideItem(
    val id: Int,
    val title: String,
    val arabic: String,
    val latin: String,
    val translation: String,
    val instruction: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YasinTahlilScreen(
    viewModel: KhutbahViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableIntStateOf(0) } // 0 = Yasin, 1 = Tahlil
    var fontSizeMultiplier by remember { mutableFloatStateOf(1.0f) } // 0.8f to 1.5f for accessibility

    // Tahlil static list definition
    val tahlilList = remember {
        listOf(
            TahlilGuideItem(
                id = 1,
                title = "1. Pengantar Al-Fatihah (Hadhoroh)",
                instruction = "Membaca surat Al-Fatihah ditujukan kepada Nabi Muhammad SAW, keluarga-Nya, sahabat-Nya, serta para ulama dan ahli kubur.",
                arabic = "إِلَى حَضْرَةِ النَّبِيِّ الْمُصْطَفَى صَلَّى اللهُ عَلَيْهِ وَسَلَّمَ وَآلِهِ وَأَصْحَابِهِ وَأَزْوَاجِهِ وَذُرِّيَّتِهِ، وَإِلَى أَرْوَاحِ إِخْوَانِهِ مِنَ الْأَنْبِيَاءِ وَالْمُرْسَلِينَ وَالْأَوْلِيَاءِ وَالشُّهَدَاءِ وَالصَّالِحِينَ وَالْعُلَمَاءِ الْعَامِلِينَ وَالْمُصَنِّفِينَ الْمُخْلِصِينَ وَجَمِيعِ الْمَلَائِكَةِ الْمُقَرَّبِينَ، خُصُوصًا إِلَى رُوحِ... اَلْفَاتِحَة",
                latin = "Ila hadhrotin nabiyyil musthofa shollallohu 'alaihi wa sallam wa aalihi wa ashohabishi wa azwajihi wa dzurriyyatihi, wa ila arwahi ikhwanihi minal anbiya'i wal mursalin wal auliya'i wasy syuhada'i wash sholihin wal 'ulama'il 'amilin wal mushonnifinal mukhlisin wa jami'il mala'ikatil muqorrobin, khushushon ila ruhi (Sebut nama ahli kubur)... AL-FATIHAH.",
                translation = "Dengan nama Allah yang maha pengasih, maha penyayang. Kepada yang terhormat Nabi Muhammad SAW, segenap keluarga, sahabat, istri, dan keturunannya. Dan kepada arwah para nabi, utusan, wali, syuhada, shalihin, ulama, penulis yang ikhlas, serta malaikat muqarrabin, khususnya kepada arwah... (Sebut nama almarhum), Al-Fatihah."
            ),
            TahlilGuideItem(
                id = 2,
                title = "2. Membaca Surat Al-Ikhlas (3x)",
                instruction = "Membaca Surat Al-Ikhlas sebanyak 3 kali diselingi dengan bacaan tahlil dan takbir.",
                arabic = "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ. قُلْ هُوَ اللَّهُ أَحَدٌ. اللَّهُ الصَّمَدُ. لَمْ يَلِدْ وَلَمْ يُولَدْ. وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ.",
                latin = "Bismillahir rahmanir rahim. Qul huwallahu ahad. Allahush shamad. Lam yalid wa lam yuled. Wa lam yakul lahu kufuwan ahad. (3x)\n\nLa ilaha illallahu wallahu akbar wali-llahil hamdu.",
                translation = "Katakanlah (Muhammad): Dia-lah Allah yang Maha Esa. Allah adalah tuhan yang bergantung kepada-Nya segala sesuatu. Dia tiada beranak dan tidak pula diperanakkan. Dan tidak ada seorang pun yang setara dengan Dia.\n\nTiada tuhan selain Allah, Allah Maha Besar, dan segala puji hanya milik Allah."
            ),
            TahlilGuideItem(
                id = 3,
                title = "3. Membaca Surat Al-Falaq",
                instruction = "Membaca Surat Al-Falaq diselingi dengan tahlil dan takbir.",
                arabic = "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ. مِن شَرِّ مَا خَلَقَ. وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ. وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ. وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ.",
                latin = "Qul a'udzu birabbil falaq. Min syarri ma khalaq. Wa min syarri ghasiqin idza waqab. Wa min syarrin naffatsati fil 'uqad. Wa min syarri hasidin idza hasad.\n\nLa ilaha illallahu wallahu akbar wali-llahil hamdu.",
                translation = "Katakanlah (Muhammad): Aku berlindung kepada Tuhan yang menguasai subuh, dari kejahatan makhluk-Nya, dan dari kejahatan malam apabila telah gelap gulita, dan dari kejahatan wanita-wanita penyihir yang meniup pada buhul-buhul, dan dari kejahatan pendengki bila ia dengki.\n\nTiada tuhan selain Allah, Allah Maha Besar, dan segala puji hanya milik Allah."
            ),
            TahlilGuideItem(
                id = 4,
                title = "4. Membaca Surat An-Nas",
                instruction = "Membaca Surat An-Nas diselingi tahlil dan takbir.",
                arabic = "قُل * أَعُوذُ بِرَبِّ النَّاسِ. مَلِكِ النَّاسِ. إِلَٰهِ النَّاسِ. مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ. الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ. مِنَ الْجِنَّةِ وَالنَّاسِ.",
                latin = "Qul a'udzu birabbin nas. Malikin nas. Ilahin nas. Min syarril waswasil khannas. Alladzi yuwaswisu fi shudurin nas. Minal jinnati wan nas.\n\nLa ilaha illallahu wallahu akbar wali-llahil hamdu.",
                translation = "Katakanlah (Muhammad): Aku berlindung kepada Tuhan (pemelihara) manusia. Raja manusia. Sembahan manusia. Dari kejahatan (bisikan) syetan yang biasa bersembunyi, yang membisikkan (kejahatan) ke dalam dada manusia dari jin dan manusia.\n\nTiada tuhan selain Allah, Allah Maha Besar, dan segala puji hanya milik Allah."
            ),
            TahlilGuideItem(
                id = 5,
                title = "5. Membaca Awal Surat Al-Baqarah (Ayat 1-5)",
                instruction = "Membaca bagian awal Surat Al-Baqarah.",
                arabic = "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ. الم. ذَٰلِكَ الْكِتَابُ لَا رَيْبَ ۛ فِيهِ ۛ هُدًى لِلْمُتَّقِينَ. الَّذِينَ يُؤْمِنُونَ بِالْغَيْبِ وَيُقِيمُونَ الصَّلَاةَ وَمِمَّا رَزَقْنَاهُمْ يُنْفِقُونَ. وَالَّذِينَ يُؤْمِنُونَ بِمَا أُنْزِلَ إِلَيْكَ وَمَا أُنْزِلَ مِنْ قَبْلِكَ وَبِالْآخِرَةِ هُمْ يُوقِنُونَ. أُولَٰئِكَ عَلَىٰ هُدًى مِنْ رَبِّهِمْ ۖ وَأُولَٰئِكَ هُمُ الْمُفْلِحُونَ.",
                latin = "Alif lam mim. Dzalikal kitabu la raiba fih, hudal lil muttaqin. Alladzina yu'minuna bil ghaibi wa yuqimunas shalata wa mimma razaqnahum yunfiqun. Walladzina yu'minuna bima unzila ilaika wa ma unzila min qablika wa bil-akhirati hum yuqinun. Ula'ika 'ala hudam mir rabbihim wa ula'ika humul muflihun.",
                translation = "Alif Lam Mim. Kitab (Al-Quran) ini tidak ada keraguan padanya; petunjuk bagi mereka yang bertakwa. (Yaitu) mereka yang beriman kepada yang ghaib, mendirikan shalat, dan menafkahkan sebagian rezeki yang Kami anugerahkan kepada mereka. Dan mereka yang beriman kepada Kitab (Al-Quran) yang telah diturunkan kepadamu dan kitab-kitab yang telah diturunkan sebelummu, serta mereka yakin akan adanya (kehidupan) akhirat. Mereka itulah yang tetap mendapat petunjuk dari Tuhannya, dan merekalah orang-orang yang beruntung."
            ),
            TahlilGuideItem(
                id = 6,
                title = "6. Membaca Ayat Kursi (Al-Baqarah 255)",
                instruction = "Membaca Ayat Kursi yang agung.",
                arabic = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَنْ ذَا الَّذِي يَشْفَعُ عِنْدَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ.",
                latin = "Allahu la ilaha illa huwal hayyul qayyum, la ta'khudzuhu sinatuwwa la naum. Lahu ma fis samawati wa ma fil ardh. Man dzalladzi yasyfa'u 'indahu illa bi idznih. Ya'lamu ma baina aidihim wa ma khalfahum, walayuhituna bisyai'im min 'ilmihi illa bima syaa'. Wasi'a kursiyyuhus samawati wal ardha wala ya'uduhu hifzhuhuma wahuwal 'aliyyul 'adzim.",
                translation = "Allah, tidak ada Tuhan melainkan Dia yang hidup kekal lagi terus menerus mengurus (makhluk-Nya); tidak mengantuk dan tidak tidur. Kepunyaan-Nya apa yang di langit dan di bumi. Tiada yang dapat memberi syafaat di sisi Allah tanpa izin-Nya? Allah mengetahui apa-apa yang di hadapan mereka dan di belakang mereka, dan mereka tidak mengetahui apa-apa dari ilmu Allah melainkan apa yang dikehendaki-Nya. Kursi Allah meliputi langit dan bumi. Dan Allah tidak merasa berat memelihara keduanya, dan Allah Maha Tinggi lagi Maha Besar."
            ),
            TahlilGuideItem(
                id = 7,
                title = "7. Membaca Istighfar Utama (3x/11x)",
                instruction = "Memohon ampunan kepada Allah SWT.",
                arabic = "أَسْتَغْفِرُ اللهَ الْعَظِيمَ (٣٠٠\nالَّذِي لَا إِلٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ وَأَتُوبُ إِلَيْهِ",
                latin = "Astaghfirullahal 'adzim. (3x)\n\nAlladzi la ilaha illa huwal hayyul qayyumu wa atubu ilaih.",
                translation = "Saya memohon ampun kepada Allah Yang Maha Agung. (3x)\n\nYang tiada tuhan selain Dia Yang Maha Hidup lagi Senantiasa Mengurus makhluk-Nya, dan saya bertaubat kepada-Nya."
            ),
            TahlilGuideItem(
                id = 8,
                title = "8. Membaca Tahlil dan Dzikir (100x/Bebas)",
                instruction = "Inti dari tahlil sebagai peneguhan kalimat tauhid.",
                arabic = "لَا إِلٰهَ إِلَّا اللهُ. (١٠٠\nلَا إِلٰهَ إِلَّا اللهُ سَيِّدُنَا مُحَمَّدٌ رَسُولُ اللهِ لِأَجْلِ مُنْتَهَى رَضَا اللهِ.",
                latin = "La ilaha illallah. (100x)\n\nLa ilaha illallahu sayyiduna muhammadur rasulullah shaollallahu 'alaihi wa sallam.",
                translation = "Tiada Tuhan selain Allah. (100x)\n\nTiada Tuhan selain Allah, junjungan kami Nabi Muhammad adalah utusan Allah, semoga Allah mencurahkan rahmat dan keselamatan kepada-Nya."
            ),
            TahlilGuideItem(
                id = 9,
                title = "9. Membaca Doa Arwah (Pemungkas Tahlil)",
                instruction = "Doa khusus memohonkan ampunan, kelapangan kubur bagi ahli kubur dan ditutup dengan keberkahan.",
                arabic = "اَللَّهُمَّ اغْفِرْ لَهُمْ وَارْحَمْهُمْ وَعَافِهِمْ وَاعْفُ عَنْهُمْ. اَللَّهُمَّ أَنْزِلِ الرَّحْمَةَ وَالْمَغْفِرَةَ عَلَى أَهْلِ الْقُبُورِ مِنْ أَهْلِ لَا إِلَهَ إِلَّا اللهُ مُحَمَّدٌ رَسُولُ اللهِ. رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ. سُبْحَانَ رَبِّكَ رَبِّ الْعِزَّةِ عَمَّا يَصِفُونَ، وَسَلَامٌ عَلَى الْمُرْسَلِينَ، وَالْحَمْدُ للهِ رَبِّ الْعَالَمِينَ.",
                latin = "Allahummaghfir lahum warhamhum wa 'afihim wa'fu 'anhum. Allahumma anzilir rahmata wal maghfirata 'ala ahlil quburi min ahli la ilaha illallahu muhammadur rasulullah. Rabbana aatina fid dunya hasanatawwa fil akhirati hasanatawwa qina 'adzaban naar. Subhana rabbika rabbil 'izzati 'amma yashifun, wa salamun 'alal mursalin, wal hamdulillahi rabbil 'alamin.",
                translation = "Ya Allah, ampunilah dosa mereka, kasihanilah mereka, sejahterakanlah mereka, dan maafkanlah kesalahan mereka. Ya Allah turunkanlah rahmat dan ampunan-Mu kepada penghuni kubur dari ahli kalimat 'La ilaha illallah Muhammadur Rasulullah'. Wahai Tuhan kami, berikanlah kepada kami kegembiraan di dunia dan keselamatan di akhirat, dan peliharalah kami dari siksa api neraka. Maha Suci Tuhan-Mu, Pemilik kemuliaan dari apa yang mereka katakan, keselamatan atas para rasul, dan segala puji bagi Tuhan Semesta Alam."
            )
        )
    }

    // Load Yasin details on launch structure
    LaunchedEffect(Unit) {
        viewModel.loadYasinDetail()
    }

    val yasinDetailState by viewModel.yasinDetailState.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val currentPlayingSurah by viewModel.currentPlayingSurah.collectAsStateWithLifecycle()
    val audioLoading by viewModel.audioLoading.collectAsStateWithLifecycle()

    val yasinSurahObject = remember {
        SurahModel(
            nomor = 36,
            nama = "يس",
            namaLatin = "Yasin",
            jumlahAyat = 83,
            tempatTurun = "Makkiyah",
            arti = "Ya Sin",
            deskripsi = "Surat Yasin"
        )
    }

    val isYasinPlaying = isPlaying && currentPlayingSurah?.nomor == 36

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Accessibility Text Size Controller Block
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatSize,
                        contentDescription = "Ukuran Tulisan",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Ukuran Tulisan Arab",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Slider buttons or Segments
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { fontSizeMultiplier = (fontSizeMultiplier - 0.1f).coerceAtLeast(0.7f) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape),
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Kecilkan", modifier = Modifier.size(16.dp))
                    }

                    Text(
                        text = "${(fontSizeMultiplier * 100).toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )

                    IconButton(
                        onClick = { fontSizeMultiplier = (fontSizeMultiplier + 0.1f).coerceAtMost(1.5f) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape),
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Besarkan", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Sub Bar to switch Yasin / Tahlil Recitation mode
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (activeTab == 0) Icons.Filled.MenuBook else Icons.Outlined.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text("Surat Yasin", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.testTag("tab_sub_yasin")
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (activeTab == 1) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text("Tahlil & Doa", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.testTag("tab_sub_tahlil")
            )
        }

        // Display selection content
        when (activeTab) {
            0 -> {
                // Surat Yasin Recitation
                Column(modifier = Modifier.fillMaxSize()) {
                    // Audio card for Surat Yasin play/pause helper
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Audio Murottal Yasin",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = if (isYasinPlaying) "Sedang Memutar Audio..." else "Dengarkan lantunan merdu Surat Yasin",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            // Dynamic action button
                            Button(
                                onClick = { viewModel.playMurottal(yasinSurahObject) },
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.testTag("btn_play_yasin_audio"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isYasinPlaying) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                            ) {
                                if (audioLoading && currentPlayingSurah?.nomor == 36) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (isYasinPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Mainkan",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isYasinPlaying) "Pause" else "Putar",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Surat Yasin list content loader
                    when (val state = yasinDetailState) {
                        is QuranUiState.Loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Sedang menyiapkan Surat Yasin...",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        is QuranUiState.Error -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudOff,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                                        modifier = Modifier.size(54.dp)
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = state.message,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.error,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Button(
                                        onClick = { viewModel.loadYasinDetail() }
                                    ) {
                                        Text("Coba Lagi")
                                    }
                                }
                            }
                        }

                        is QuranUiState.Success -> {
                            val surahDetail = state.surahDetail
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Surah metadata header card
                                item {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 12.dp),
                                        shape = RoundedCornerShape(20.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(20.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = surahDetail.nama,
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "${surahDetail.namaLatin} (${surahDetail.arti})",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${surahDetail.tempatTurun} • ${surahDetail.jumlahAyat} Ayat",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = surahDetail.deskripsi.replace("<p>", "").replace("</p>", ""),
                                                fontSize = 10.sp,
                                                textAlign = TextAlign.Center,
                                                lineHeight = 15.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                modifier = Modifier.padding(horizontal = 10.dp)
                                            )
                                        }
                                    }
                                }

                                // Bismillah bar
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ",
                                            fontSize = (22 * fontSizeMultiplier).sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                // Normal verses
                                itemsIndexed(surahDetail.ayat, key = { _, item -> item.nomorAyat }) { _, verse ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Verse tracking pill
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = verse.nomorAyat.toString(),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }

                                                // Bookmark icon if needed (simplified)
                                            }

                                            Spacer(modifier = Modifier.height(12.dp))

                                            // Arabic recitation
                                            Text(
                                                text = verse.teksArab,
                                                fontSize = (24 * fontSizeMultiplier).sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                textAlign = TextAlign.Right,
                                                lineHeight = (42 * fontSizeMultiplier).sp,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(bottom = 12.dp)
                                            )

                                            // Transliterator
                                            Text(
                                                text = verse.teksLatin,
                                                fontSize = (12 * fontSizeMultiplier).sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(bottom = 6.dp)
                                            )

                                            // Indonesian Translation
                                            Text(
                                                text = verse.teksIndonesia,
                                                fontSize = (11 * fontSizeMultiplier).sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                                lineHeight = (16 * fontSizeMultiplier).sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        else -> {}
                    }
                }
            }

            1 -> {
                // Tahlil list content recitation with step-by-step
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Card
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                )
                                .padding(20.dp)
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FeaturedPlayList,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "Tahlil & Doa Arwah",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Untaian doa, istighfar, tahlil, dan zikir tawasul mendoakan keselamatan bagi segenap orang tua dan ahli kubur.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    // Tahlil items
                    itemsIndexed(tahlilList, key = { _, item -> item.id }) { _, item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp)
                            ) {
                                // Title with green highlight
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                ) {
                                    Text(
                                        text = item.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                // Hint/Instruction
                                if (item.instruction.isNotEmpty()) {
                                    Text(
                                        text = item.instruction,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Arabic
                                Text(
                                    text = item.arabic,
                                    fontSize = (22 * fontSizeMultiplier).sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Right,
                                    lineHeight = (42 * fontSizeMultiplier).sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp)
                                )

                                // Latin
                                Text(
                                    text = item.latin,
                                    fontSize = (12 * fontSizeMultiplier).sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )

                                // Translation
                                Text(
                                    text = item.translation,
                                    fontSize = (11 * fontSizeMultiplier).sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    lineHeight = (16 * fontSizeMultiplier).sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
