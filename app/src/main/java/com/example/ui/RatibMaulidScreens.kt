package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.KhutbahViewModel

// Data class representation for Ratib items
data class RatibItem(
    val id: Int,
    val title: String,
    val instruction: String,
    val arabic: String,
    val latin: String,
    val translation: String,
    val targetRepetition: Int
)

// Data class representation for Maulid sections
data class MaulidSection(
    val id: Int,
    val sectionName: String,
    val isMahalQiyam: Boolean = false,
    val arabic: String,
    val latin: String,
    val translation: String,
    val instruction: String = ""
)

// Static Data Store for Ratib and Maulid Nabi
object RatibMaulidData {
    
    val ratibAlHaddad = listOf(
        RatibItem(
            id = 1,
            title = "1. Al-Fatihah",
            instruction = "Membaca Surat Al-Fatihah kepada penyusun Ratib, Al-Imam Al-Habib Abdullah bin Alawi Al-Haddad.",
            arabic = "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ. الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ. الرَّحْمَنِ الرَّحِيمِ. مَالِكِ يَوْمِ الدِّينِ. إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ. اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ. صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ.",
            latin = "Bismillahir-rahmanir-rahim. Al-hamdu lillahi rabbil-'alamin. Ar-rahmanir-rahim. Maliki yawmid-din. Iyyaka na'budu wa iyyaka nasta'in. Ihdinas-siratal-mustaqim. Siratal-ladzina an'amta 'alayhim ghayril-maghdubi 'alayhim walad-dallin.",
            translation = "Dengan nama Allah Yang Maha Pengasih, Maha Penyayang. Segala puji bagi Allah, Tuhan seluruh alam. Yang Maha Pengasih, Maha Penyayang. Pemilik hari pembalasan. Hanya kepada Engkaulah kami menyembah dan hanya kepada Engkaulah kami memohon pertolongan. Tunjukkanlah kami jalan yang lurus. (Yaitu) jalan orang-orang yang telah Engkau beri nikmat kepadanya; bukan (jalan) mereka yang dimurkai, dan bukan (pula jalan) mereka yang sesat.",
            targetRepetition = 1
        ),
        RatibItem(
            id = 2,
            title = "2. Ayat Kursi",
            instruction = "Membaca Ayat Kursi (Al-Baqarah: 255) sebagai pelindung utama.",
            arabic = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَنْ ذَا الَّذِي يَشْفَعُ عِنْدَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ.",
            latin = "Allahu la ilaha illa huwal hayyul qayyum, la ta'khudzuhu sinatuwwa la naum. Lahu ma fis samawati wa ma fil ardh. Man dzalladzi yasyfa'u 'indahu illa bi idznih. Ya'lamu ma baina aidihim wa ma khalfahum, walayuhituna bisyai'im min 'ilmihi illa bima syaa'. Wasi'a kursiyyuhus samawati wal ardha wala ya'uduhu hifzhuhuma wahuwal 'aliyyul 'adzim.",
            translation = "Allah, tidak ada Tuhan melainkan Dia yang hidup kekal lagi terus menerus mengurus (makhluk-Nya); tidak mengantuk dan tidak tidur. Kepunyaan-Nya apa yang di langit dan di bumi. Tiada yang dapat memberi syafaat di sisi Allah tanpa izin-Nya? Allah mengetahui apa-apa yang di hadapan mereka dan di belakang mereka, dan mereka tidak mengetahui apa-apa dari ilmu Allah melainkan apa yang dikehendaki-Nya. Kursi Allah meliputi langit dan bumi. Dan Allah tidak merasa berat memelihara keduanya, dan Allah Maha Tinggi lagi Maha Besar.",
            targetRepetition = 1
        ),
        RatibItem(
            id = 3,
            title = "3. Akhir Al-Baqarah",
            instruction = "Membaca bagian akhir Surat Al-Baqarah (Ayat 285-286).",
            arabic = "آمَنَ الرَّسُولُ بِمَا أُنْزِلَ إِلَيْهِ مِنْ رَبِّهِ وَالْمُؤْمِنُونَ ۚ كُلٌّ آمَنَ بِاللَّهِ وَمَلَائِكَتِهِ وَكُتُبِهِ وَرُسُلِهِ لَا نُفَرِّقُ بَيْنَ أَحَدٍ مِنْ رُسُلِهِ ۚ وَقَالُوا سَمِعْنَا وَأَطَعْنَا ۖ غُفْرَانَكَ رَبَّنَا وَإِلَيْكَ الْمَصِيرُ. لَا يُكَلِّفُ اللَّهُ نَفْسًا إِلَّا وُسْعَهَا ۚ لَهَا مَا كَسَبَتْ وَعَلَيْهَا مَا اكْتَسَبَتْ ۗ رَبَّنَا لَا تُؤَاخِذْنَا إِنْ نَسِينَا أَوْ أَخْطَأْنَا ۚ رَبَّنَا وَلَا تَحْمِلْ عَلَيْنَا إِصْرًا كَمَا حَمَلْتَهُ عَلَى الَّذِينَ مِنْ قَبْلِنَا ۚ رَبَّنَا وَلَا تُحَمِّلْنَا مَا لَا طَاقَةَ لَنَا بِهِ ۖ وَاعْفُ عَنَّا وَاغْفِرْ لَنَا وَارْحَمْنَا ۚ أَنْتَ مَوْلَانَا فَانْصُرْنَا عَلَى الْقَوْمِ الْكَافِرِينَ.",
            latin = "Amanar-rasulu bima unzila ilayhi mir-rabbihi wal-mu'minun. Kullun amana billahi wa mala'ikatihi wa kutubihi wa rusulih, la nufarriqu bayna ahadin mir-rusulih. Wa qalu sami'na wa ata'na Ghufranaka Rabbana wa ilaykal-masir. La yukallifullahu nafsan illa wus'aha, laha ma kasabat wa 'alayha maktasabat. Rabbana la tu'akhidzna in-nasina aw akhta'na, Rabbana wala tahmil 'alayna isran kama hamaltahu 'alal-ladzina min qablina, Rabbana wala tuhammilna ma la taqata lana bih. Wa'fu 'anna waghfir lana warhamna Anta mawlana fansurna 'alal-qawmil-kafirin.",
            translation = "Rasul telah beriman kepada Al Quran yang diturunkan kepadanya dari Tuhannya, demikian pula orang-orang yang beriman. Semuanya beriman kepada Allah, malaikat-malaikat-Nya, kitab-kitab-Nya dan rasul-rasul-Nya. (Mereka mengatakan): \"Kami tidak membeda-bedakan antara seorang pun (dengan yang lain) dari rasul-rasul-Nya\", dan mereka mengatakan: \"Kami dengar dan kami taat\". (Mereka berdoa): \"Ampunilah kami ya Tuhan kami dan kepada Engkaulah tempat kembali\". Allah tidak membebani seseorang melainkan sesuai dengan kesanggupannya. Ia mendapat pahala (dari kebajikan) yang diusahakannya dan ia mendapat siksa (dari kejahatan) yang dikerjakannya. (Mereka berdoa): \"Ya Tuhan kami, janganlah Engkau hukum kami jika kami lupa atau kami tersalah. Ya Tuhan kami, janganlah Engkau bebankan kepada kami beban yang berat sebagaimana Engkau bebankan kepada orang-orang sebelum kami. Ya Tuhan kami, janganlah Engkau pikulkan kepada kami apa yang tak sanggup kami memikulnya. Beri maaflah kami; ampunilah kami; dan rahmatilah kami. Engkaulah Penolong kami, maka tolonglah kami terhadap kaum yang kafir\".",
            targetRepetition = 1
        ),
        RatibItem(
            id = 4,
            title = "4. Kalimat Tauhid & Pujian",
            instruction = "Meneguhkan keesaan Allah SWT.",
            arabic = "لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ يُحْيِي وَيُمِيتُ، وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ.",
            latin = "La ilaha illallah wahdahu la syarika lah, lahul-mulku wa lahul-hamdu yuhyi wa yumitu wa huwa 'ala kulli syay'in qadir.",
            translation = "Tiada Tuhan selain Allah yang Maha Esa, tiada sekutu bagi-Nya. Bagi-Nyalah segala kerajaan dan bagi-Nya segala pujian. Dia-lah yang menghidupkan dan yang mematikan, dan Dia-lah yang Maha Kuasa atas segala sesuatu.",
            targetRepetition = 3
        ),
        RatibItem(
            id = 5,
            title = "5. Tasbih & Tahmid",
            instruction = "Mensucikan serta memuji Allah.",
            arabic = "سُبْحَانَ اللَّهِ وَالْحَمْدُ لِلَّهِ وَلَا إِلَٰهَ إِلَّا اللَّهُ وَاللَّهُ أَكْبَرُ.",
            latin = "Subhanallah wal-hamdulillah wa la ilaha illallah wallahu akbar.",
            translation = "Maha Suci Allah, segala puji bagi Allah, tiada Tuhan selain Allah dan Allah Maha Besar.",
            targetRepetition = 3
        ),
        RatibItem(
            id = 6,
            title = "6. Tasbih Utama",
            instruction = "Pujian istimewa yang dicintai Allah.",
            arabic = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ سُبْحَانَ اللَّهِ الْعَظِيمِ.",
            latin = "Subhanallahi wa bihamdihi subhanallahil-'adzim.",
            translation = "Maha Suci Allah dengan segala pujian-Nya, Maha Suci Allah yang Maha Agung.",
            targetRepetition = 3
        ),
        RatibItem(
            id = 7,
            title = "7. Istighfar Pengampunan",
            instruction = "Memohon ampunan dan taubat yang tulus.",
            arabic = "رَبَّنَا اغْفِرْ لَنَا وَتُبْ عَلَيْنَا إِنَّكَ أَنْتَ التَّوَّابُ الرَّحِيمُ.",
            latin = "Rabbanaghfir lana wa tub 'alayna, innaka Antat-Tawwabur-Rahim.",
            translation = "Wahai Tuhan kami, ampunilah dosa kami dan terimalah taubat kami, sesungguhnya Engkaulah Penerima taubat lagi Maha Penyayang.",
            targetRepetition = 3
        ),
        RatibItem(
            id = 8,
            title = "8. Sholawat Ibrahimiyah Ringkas",
            instruction = "Membaca sholawat atas Nabi Muhammad SAW.",
            arabic = "اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ اللَّهُمَّ صَلِّ عَلَيْهِ وَسَلِّمْ.",
            latin = "Allahumma shalli 'ala Muhammad, Allahumma shalli 'alayhi wa sallim.",
            translation = "Ya Allah, limpahkanlah rahmat atas Nabi Muhammad. Ya Allah, limpahkanlah rahmat serta keselamatan atasnya.",
            targetRepetition = 3
        ),
        RatibItem(
            id = 9,
            title = "9. Isti'adzah (Perlindungan)",
            instruction = "Memohon perlindungan dari segala mara bahaya.",
            arabic = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ.",
            latin = "A'udzu bi kalimatillahit-tammati min syarri ma khalaq.",
            translation = "Aku berlindung dengan kalimat-kalimat Allah yang sempurna dari kejahatan apa-apa yang telah Dia ciptakan.",
            targetRepetition = 3
        ),
        RatibItem(
            id = 10,
            title = "10. Penolak Bahaya (Sakti)",
            instruction = "Perisai agung dari keburukan bumi dan langit.",
            arabic = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ.",
            latin = "Bismillahil-ladzi la yadurru ma'asmihi syay'un fil-ardi wa la fis-sama'i wa Huwas-Sami'ul-'Alim.",
            translation = "Dengan nama Allah yang karena nama-Nya tidak ada sesuatu pun di bumi maupun di langit yang dapat mendatangkan bahaya, dan Dia-lah Yang Maha Mendengar lagi Maha Mengetahui.",
            targetRepetition = 3
        ),
        RatibItem(
            id = 11,
            title = "11. Ridha Beragama",
            instruction = "Meneguhkan keridhaan dalam Iman Islam.",
            arabic = "رَضِينَا بِاللَّهِ رَبًّا وَبِالْإِسْلَامِ دِينًا وَبِمُحَمَّدٍ نَبِيًّا.",
            latin = "Radhina billahi Rabba, wa bil-Islami dina, wa bi-Muhammadin nabiyya.",
            translation = "Kami ridha Allah sebagai Tuhan kami, Islam sebagai agama kami, dan Nabi Muhammad sebagai Nabi kami.",
            targetRepetition = 3
        ),
        RatibItem(
            id = 12,
            title = "12. Basmalah & Hamdalah",
            instruction = "Segala kebaikan diiringi kehendak-Nya.",
            arabic = "بِسْمِ اللَّهِ وَالْحَمْدُ لِلَّهِ وَالْخَيْرُ وَالشَّرُّ بِمَشِيئَةِ اللَّهِ.",
            latin = "Bismillahi wal-hamdulillahi wal-khayru wasy-syarru bi masyiatillah.",
            translation = "Dengan nama Allah, segala puji bagi Allah, segala kebaikan dan keburukan terjadi dengan kehendak Allah.",
            targetRepetition = 3
        ),
        RatibItem(
            id = 13,
            title = "13. Iman Hari Akhir",
            instruction = "Keteguhan iman lahir dan batin.",
            arabic = "آمَنَّا بِاللَّهِ وَالْيَوْمِ الْآخِرِ تُبْنَا إِلَى اللَّهِ بَاطِنًا وَظَاهِرًا.",
            latin = "Amanna billahi wal-yawmil-akhiri tubna ilallahi batinan wa dhahira.",
            translation = "Kami beriman kepada Allah dan hari akhir, kami bertaubat kepada Allah lahir maupun batin.",
            targetRepetition = 3
        ),
        RatibItem(
            id = 14,
            title = "14. Permohonan Pemaafan",
            instruction = "Meminta ampunan dan penghapusan dosa.",
            arabic = "يَا رَبَّنَا وَاعْفُ عَنَّا وَامْحُ الَّذِي كَانَ مِنَّا.",
            latin = "Ya Rabbana wa'fu 'anna wamhulladzi kana minna.",
            translation = "Wahai Tuhan kami, maafkanlah kami dan hapuskanlah segala dosa yang ada pada diri kami.",
            targetRepetition = 3
        ),
        RatibItem(
            id = 15,
            title = "15. Istiqomah Islam & Keagungan",
            instruction = "Meminta wafat dalam khusnul khotimah dengan keagungan Allah.",
            arabic = "يَا ذَا الْجَلَالِ وَالْإِكْرَامِ أَمِتْنَا عَلَىٰ دِينِ الْإِسْلَامِ.",
            latin = "Ya Dzal-Jalali wal-Ikram, amitna 'ala dinil-Islam.",
            translation = "Wahai Tuhan Yang Mempunyai Keagungan dan Kemuliaan, wafatkanlah kami dalam agama Islam.",
            targetRepetition = 7
        ),
        RatibItem(
            id = 16,
            title = "16. Ya Qawiyyu Ya Matin",
            instruction = "Meminta keselamatan dari penindasan orang zhalim.",
            arabic = "يَا قَوِيُّ يَا مَتِينُ اكْفِ شَرَّ الظَّالِمِينَ.",
            latin = "Ya Qawiyyu Ya Matinu ikfi syarradz-dzaliminh.",
            translation = "Wahai Yang Maha Kuat, Wahai Yang Maha Kokoh, hindarkanlah kami dari keburukan orang-orang zhalim.",
            targetRepetition = 3
        ),
        RatibItem(
            id = 17,
            title = "17. Perdamaian Umat",
            instruction = "Mendoakan kemaslahatan seluruh Muslim.",
            arabic = "أَصْلَحَ اللَّهُ أُمُورَ الْمُسْلِمِينَ صَرَفَ اللَّهُ شَرَّ الْمُؤْذِينَ.",
            latin = "Ashlahallahu umurul-muslimin, sharafallahu syarral-mu'dzin.",
            translation = "Semoga Allah memperbaiki urusan kaum Muslimin dan semoga Allah menghindarkan mereka dari kejahatan orang-orang yang suka mengganggu.",
            targetRepetition = 3
        ),
        RatibItem(
            id = 18,
            title = "18. Sifat Agung",
            instruction = "Dzikir memanggil Asmaul Husna yang mengetahui segalanya.",
            arabic = "يَا عَلِيُّ يَا كَبِيرُ يَا عَلِيمُ يَا قَدِيرُ يَا سَمِيعُ يَا بَصِيرُ يَا لَطِيفُ يَا خَبِيرُ.",
            latin = "Ya 'Aliyyu Ya Kabiru, Ya 'Alimu Ya Qadiru, Ya Sami'u Ya Bashiru, Ya Latifu Ya Khabir.",
            translation = "Wahai Yang Maha Tinggi, Wahai Yang Maha Besar, Wahai Yang Maha Mengetahui, Wahai Yang Maha Kuasa, Wahai Yang Maha Mendengar, Wahai Yang Maha Melihat, Wahai Yang Maha Lembut, Wahai Yang Maha Teliti.",
            targetRepetition = 3
        ),
        RatibItem(
            id = 19,
            title = "19. Pelepas Duka",
            instruction = "Memohon kelapangan dada dan ampunan dosa.",
            arabic = "يَا فَارِجَ الْهَمِّ يَا كَاشِفَ الْغَمِّ يَا مَنْ لِعَبْدِهِ يَغْفِرُ وَيَرْحَمُ.",
            latin = "Ya Farijal-hamm, Ya Kasyifal-ghamm, Ya Man li 'abdihi yaghfiru wa yarham.",
            translation = "Wahai Yang menghilangkan kesedihan, Wahai Yang melenyapkan duka cita, Wahai Yang mengampuni dan mengasihi hamba-Nya khilaf.",
            targetRepetition = 3
        ),
        RatibItem(
            id = 20,
            title = "20. Istighfar Global",
            instruction = "Taubat menyeluruh atas segala kesalahan.",
            arabic = "أَسْتَغْفِرُ اللَّهَ رَبَّ الْبَرَايَا أَسْتَغْفِرُ اللَّهَ مِنَ الْخَطَايَا.",
            latin = "Astaghfirullaha Rabbal-baraya, astaghfirullaha minal-khathaya.",
            translation = "Aku memohon ampun kepada Allah Tuhan seluruh makhluk, aku memohon ampun kepada Allah dari segala kesalahan.",
            targetRepetition = 4
        ),
        RatibItem(
            id = 21,
            title = "21. Kalimat Thoyyibah Utama",
            instruction = "Peneguhan zikir tauhid sebagai bekal utama kelak.",
            arabic = "لَا إِلَٰهَ إِلَّا اللَّهُ.",
            latin = "La ilaha illallah.",
            translation = "Tiada Tuhan selain Allah.",
            targetRepetition = 50
        )
    )

    val ratibAlAthos = listOf(
        RatibItem(
            id = 1,
            title = "1. Pembuka (Ta'awudz & Al-Hasyr)",
            instruction = "Dzikir perlindungan yang dirangkai dari akhir Surat Al-Hasyr.",
            arabic = "أَعُوذُ بِاللَّهِ السَّمِيعِ الْعَلِيمِ مِنَ الشَّيْطَانِ الرَّجِيمِ. لَوْ أَنْزَلْنَا هَٰذَا الْقُرْآنَ عَلَىٰ جَبَلٍ لَرَأَيْتَهُ خَاشِعًا مُتَصَدِّعًا مِنْ خَشْيَةِ اللَّهِ ۚ وَتِلْكَ الْأَمْثَالُ نَضْرِبُهَا لِلنَّاسِ لَعَلَّهُمْ يَتَفَكَّرُونَ. هُوَ اللَّهُ الَّذِي لَا إِلَٰهَ إِلَّا هُوَ ۖ عَالِمُ الْغَيْبِ وَالشَّهَادَةِ ۖ هُوَ الرَّحْمَنُ الرَّحِيمُ.",
            latin = "A'udzu billahis-sami'il-'alimi minasy-syaythanir-rajim (3x).\n\nLaw anzalna hadzal-qur'ana 'ala jabalil-lara'aytahu khasyi'am mutasaddi'am min khasy-yatillah...",
            translation = "Aku berlindung kepada Allah Yang Maha Mendengar lagi Maha Mengetahui dari godaan syetan yang terkutuk (3x).\n\nSekiranya Kami turunkan Al-Qur'an ini kepada sebuah gunung, pasti kamu akan melihatnya tunduk terpecah-belah disebabkan takut kepada Allah...",
            targetRepetition = 3
        ),
        RatibItem(
            id = 2,
            title = "2. Benteng Diri",
            instruction = "Membaca asmaul husna untuk penolak bahaya.",
            arabic = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ.",
            latin = "A'udzu bi kalimatillahit-tammati min syarri ma khalaq.",
            translation = "Aku berlindung dengan kalimat-kalimat Allah yang sempurna dari kejahatan apa-apa yang telah Dia ciptakan.",
            targetRepetition = 3
        ),
        RatibItem(
            id = 3,
            title = "3. Bismillah Penolak Bahaya",
            instruction = "Pembuka perlindungan dari bencana darat, laut, dan udara.",
            arabic = "بِسْمِ اللَّهِ الَّذِي Lَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ.",
            latin = "Bismillahil-ladzi la yadurru ma'asmihi syay'un fil-ardi wa la fis-sama'i wa Huwas-Sami'ul-'Alim.",
            translation = "Dengan nama Allah yang karena nama-Nya tidak ada sesuatu pun di bumi maupun di langit yang dapat mendatangkan bahaya, dan Dia-lah Yang Maha Mendengar lagi Maha Mengetahui.",
            targetRepetition = 3
        ),
        RatibItem(
            id = 4,
            title = "4. Hauqolah Penyerahan Diri",
            instruction = "Memasrahkan segala daya upaya hanya milik Allah.",
            arabic = "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ الْعَلِيِّ الْعَظِيمِ.",
            latin = "Bismillahir-rahmanir-rahim wa la hawla wa la quwwata illa billahil-'aliyyil-'adzim.",
            translation = "Dengan nama Allah Yang Maha Pengasih lagi Maha Penyayang. Dan tidak ada daya maupun upaya selain dengan pertolongan Allah yang Maha Tinggi lagi Maha Agung.",
            targetRepetition = 10
        ),
        RatibItem(
            id = 5,
            title = "5. Benteng Iman & Perlindungan Al-Athos",
            instruction = "Memagari hati dalam kepasrahan total.",
            arabic = "بِسْمِ اللَّهِ تَحَصَّنَّا بِاللَّهِ، بِسْمِ اللَّهِ تَوَكَّلْنَا عَلَى اللَّهِ.",
            latin = "Bismillahi tahasshanna billah, bismillahi tawakkalna 'alallah.",
            translation = "Dengan nama Allah kami membentengi diri dengan Allah, dengan nama Allah kami berserah diri kepada Allah.",
            targetRepetition = 3
        )
    )

    val maulidSimtudduror = listOf(
        MaulidSection(
            id = 1,
            sectionName = "1. Sholawat Pembuka (Ya Rabbi Sholli)",
            arabic = "يَا رَبِّ صَلِّ عَلَى مُحَمَّدْ، يَا رَبِّ صَلِّ عَلَيْهِ وَسَلِّمْ.\nيَا رَبِّ بَلِّغْهُ الْوَسِيْلَةْ، يَا رَبِّ خُصَّهُ بِالْفَضِيْلَةْ.\nيَا رَبِّ وَارْضَ عَنِ الصَّحَابَةْ، يَا رَبِّ وَارْضَ عَنِ السُّلالَةْ.",
            latin = "Ya Rabbi shalli 'ala Muhammad, Ya Rabbi shalli 'alayhi wa sallim.\nYa Rabbi ballighhul-wasilah, Ya Rabbi khush-shahu bil-fadhilah.\nYa Rabbi wardha 'anis-shahabah, Ya Rabbi wardha 'anis-sulalah.",
            translation = "Wahai Tuhanku limpahkanlah rahmat atas Nabi Muhammad, Wahai Tuhanku limpahkanlah rahmat dan salam atasnya.\nWahai Tuhanku sampaikanlah dia pada kedudukan tinggi (Wasilah), Wahai Tuhanku keistimewakanlah dengan keutamaan.\nWahai Tuhanku ridhailah para sahabatnya, Wahai Tuhanku ridhailah keturunannya.",
            instruction = "Dibaca dengan penuh khidmat diiringi kerinduan bersholawat."
        ),
        MaulidSection(
            id = 2,
            sectionName = "2. Rawi I (Alhamdulillahil Qawiyyi)",
            arabic = "الْحَمْدُ للهِ الْقَوِيِّ سُلْطَانُهْ، الْوَاضِحِ بُرْهَانُهْ، الْمَبْسُوْطِ فِي الْوُجُوْدِ كَرَمُهُ وَإِحْسَانُهْ. تَعَالَى مَجْدُهُ وَعَظُمَ شَأْنُهْ. خَلَقَ الْخَلْقَ لِحِكْمَةٍ، وَطَوَى عَلَيْهَا عِلْمُهْ، وَبَسَطَ لَهُمْ مِنْ فَضْلِهِ مَا جَرَتْ بِهِ فِي قُدْرَتِهِ قِسْمَتُهْ.",
            latin = "Alhamdulillahil-qawiyyi sulthanuh, al-wadihi burhanuh, al-mabsuthi fil-wujudi karamuhu wa ihsanuh. Ta'ala majduhu wa 'adhuma syanuh. Khalaqal-khalqa li-hikmah, wa thawa 'alayha 'ilmuh, wa basatha lahum min fadhlihi ma jarat bihi fi qudratihi qismatuh.",
            translation = "Segala puji bagi Allah Yang Maha Kuat kekuasaan-Nya, yang terang benderang bukti kebesaran-Nya, yang terbentang luas kemurahan dan kebaikan-Nya di alam semesta. Maha Tinggi kemuliaan-Nya dan senantiasa agung kedudukan-Nya. Dia menciptakan makhluk semesta alam karena hikmah kebijaksanaan tertentu, yang diliputi oleh ilmu-Nya...",
            instruction = "Pembacaan riwayat silsilah serta mukaddimah cinta."
        ),
        MaulidSection(
            id = 3,
            sectionName = "3. Rawi II (Tajallal Haqqu)",
            arabic = "تَجَلَّى الْحَقُّ فِي بَهَاءِ وَنُوْرٍ، لِيَغْمُرَ الْوُجُوْدَ بِالْفَرَحِ وَالسُّرُوْرِ. وَبَعَثَ فِي الْأُمِّيِّيْنَ رَسُوْلاً مِنْهُمْ يَتْلُوْ عَلَيْهِمْ آيَاتِهِ وَيُزَكِّيْهِمْ وَيُعَلِّمُهُمُ الْكِتَابَ وَالْحِكْمَةَ وَإِنْ كَانُوْا مِنْ قَبْلُ لَفِيْ ضَلاَلٍ مُبِيْنٍ. نُوْرٌ تَنَقَّلَ فِي الْأَصْلاَبِ الطَّاهِرَةِ، حَتَّى اسْتَقَرَّ فِي صُلْبِ عَبْدِ اللهِ بْنِ عَبْدِ الْمُطَّلِبِ.",
            latin = "Tajallal-haqqu fi baha'in wa nur, liyaghmural-wujuda bil-farahi was-surur. Wa ba'atha fil-ummiyyina rasulam minhum yatlu 'alayhim ayatihi wa yuzakkihim wa yu'allimuhumul-kitaba wal-hikmata wa in kanu min qablu lafi dhalalim mubin. Nurun tanaqqala fil-aslabit-thahirah, hatta-staqarra fi sulbi 'Abdillah bin 'Abdil-Muththalib.",
            translation = "Telah tampak kebenaran dalam keindahan dan cahaya, demi memenuhi alam semesta ini dengan kebahagiaan dan sukacita. Dia mengutus di tengah umat yang buta huruf seorang rasyul dari kalangan mereka yang membacakan ayat-ayat-Nya, mensucikan mereka, serta mengajarkan kitab suci Al-Qur'an dan hikmah syariat...",
            instruction = "Perjalanan garis nasab cahaya suci kerasulan."
        ),
        MaulidSection(
            id = 4,
            sectionName = "4. Rawi III (Wa Asyhadu Alla Ilaha)",
            arabic = "وَأَشْهَدُ أَنْ لاَّ إِلٰهَ إِلاَّ اللهُ وَحْدَهُ لاَ شَرِيْكَ لَهُ، شَهَادَةً تُعْرِبُ عَمَّا انْطَوَى عَلَيْهِ الْجَنَانُ مِنَ الْإِيْمَانِ. وَأَشْهَدُ أَنَّ سَيِّدَنَا مُحَمَّدًا عَبْدُهُ وَرَسُوْلُهُ، الْمَبْعُوْثُ رَحْمَةً لِلْعَالَمِيْنَ، بَشِيرًا وَنَذِيرًا وَسِرَاجًا مُنِيرًا.",
            latin = "Wa asyhadu alla ilaha illallah wahdahu la syarika lah, syahadatan tu'ribu 'amman-thaway 'alayhil-jananu minal-iman. Wa asyhadu anna sayyidana Muhammadan 'abduhu wa rasuluh, al-mab'uthu rahmatal lil-'alamin, basyiraw wa nadziraw wa sirajam munira.",
            translation = "Dan saya bersaksi tiada Tuhan selain Allah yang Maha Esa tiada sekutu bagi-Nya, persaksian yang menerangkan apa yang terkandung di dalam sanubari berupa keimanan yang kokoh. Dan saya bersaksi bahwa junjungan kami Nabi Muhammad adalah hamba dan utusan-Nya yang diutus sebagai kasih sayang semesta alam...",
            instruction = "Kesaksian ketauhidan dan kesucian akhlak Rasul."
        ),
        MaulidSection(
            id = 5,
            sectionName = "5. Mahal Qiyam (Asyraqal Badru) - BERDIRI",
            isMahalQiyam = true,
            arabic = "أَشْرَقَ الْبَدْرُ عَلَيْنَا، فَاخْتَفَتْ مِنْهُ الْبُدُوْرُ.\nمِثْلَ حُسْنِكَ مَا رَأَيْنَا، قَطُّ يَا وَجْهَ السُّرُوْرِ.\nأَنْتَ شَمْسٌ أَنْتَ بَدْرٌ، أَنْتَ نُوْرٌ فَوْقَ نُوْرٍ.\nأَنْتَ إِكْسِيْرٌ وَغَالِي، أَنْتَ مِصْبَاحُ الصُّدُوْرِ.\nيَا حَبِيْبِيْ يَا مُحَمَّدْ، يَا عَرُوْسَ الْخَافِقَيْنِ.\nيَا مُؤَيَّدْ يَا مُمَجَّدْ، يَا إِمَامَ الْقِبْلَتَيْنِ.",
            latin = "Asyraqal-badru 'alayna, fakhtafat minhul-budur.\nMitsla husnika ma ra'ayna, qat-thu ya wajhas-surur.\nAnta syamsun anta badrun, anta nurun fawqa nur.\nAnta iksiruw wa ghali, anta misbahus-shudur.\nYa habibi ya Muhammad, ya 'arusal-khafiqayn.\nYa mu'ayyad ya mumajjad, ya imamal-qiblatayn.",
            translation = "Telah terbit bulan purnama menyinari kami, maka tenggelamlah semua cahaya bulan biasa.\nKemiripan keindahanmu tak pernah kami pandang sama sekali wahai wajah sukacita sejati.\nEngkau adalah matahari, engkau purna candra, engkau adalah cahaya di atas cahaya.\nEngkau adalah penawar kalbu yang paling berharga, engkau lentera yang menerangi dada sanubari.\nWahai kekasihku, wahai Muhammad, wahai pengantin ufuk timur dan barat.\nWahai yang dikokohkan wahai yang diagungkan utusan luhur, wahai imam dua kiblat shalat.",
            instruction = "Disunnahkan berdiri khidmat (Mahal Qiyam) menyambut kehadiran ruh luhur sang Nabi Muhammad SAW."
        )
    )

    val maulidBarzanji = listOf(
        MaulidSection(
            id = 1,
            sectionName = "1. Pembuka (Abtadi-ul Imla-a)",
            arabic = "أَبْتَدِئُ الْإِمْلَاءَ بِاسْمِ الذَّاتِ الْعَلِيَّهْ، مُسْتَدِرًّا فَيْضَ الْبَرَكَاتِ عَلَى مَا أَنَالَهُ وَأَوْلَاهْ. وَأُثَنِّي بِحَمْدٍ مَوَارِدُهُ سَائِغَةٌ هَنِيَّهْ، مُمْتَطِيًا مِنَ الشُّكْرِ الْجَمِيلِ مَطَايَاهْ. وَأُصَلِّي وَأُسَلِّمُ عَلَى النُّورِ الْمَوْصُوفِ بِالتَّقَدُّمِ وَالْأَوَّلِيَّهْ، الْمُنْتَقِلِ فِي الْغُرَرِ الكَرِيمَةِ وَالْجِبَاهْ.",
            latin = "Abtadi'ul-imla'a bismid-dzatil-'aliyyah, mustadirran faydhal-barakati 'ala ma analahu wa awlah. Wa utsanni bi-hamdin mawariduhu sa'ighatun haniyyah, mumtatiyam minasy-syukril-jamili matayah. Wa ushalli wa usallimu 'alan-nuril-mawshufi bit-taqaddumi wal-awwaliyyah, al-muntaqili fil-ghuraril-karimati wal-jibah.",
            translation = "Aku memulai mencatatkan kisah sejarah ini dengan nama Dzat Yang Maha Tinggi, seraya memohon curahan barakah atas apa yang telah dianugerahkan-Nya. Dan aku memuji-Nya dengan segala pujian yang sumber khidmatnya sangat menyegarkan, mengendarai rasa syukur yang indah kepada-Nya. Serta aku bersholawat dan mengucapkan salam atas Cahaya Utama yang bersifat terdahulu...",
            instruction = "Diterjemahkan dan dibacakan pada awal perayaan Maulid Akbar."
        ),
        MaulidSection(
            id = 2,
            sectionName = "2. Kisah Nasab Mulia (Wa Ba'du)",
            arabic = "وَ بَعْدُ فَأَقُوْلُ هُوَ سَيِّدُنَا مُحَمَّدُ بْنُ عَبْدِ اللهِ بْنِ عَبْدِ الْمُطَّلِبِ وَاسْمُهُ شَيْبَةُ الْحَمْدِ حُمِدَتْ خِصَالُهُ السَّنِيَّهْ، اِبْنِ هَاشِمٍ وَاسْمُهُ عَمْرُو بْنُ عَبْدِ مَنَافٍ وَاسْمُهُ الْمُغِيْرَةُ الَّذِي يَنْتَمِي الارْتِقَاءُ لِعُلْيَاهْ.",
            latin = "Wa ba'du fa-aqulu Huwa sayyiduna Muhammadu-bnu 'Abdillahi-bni 'Abdil-Muththalibi wasmuhu Syaybatul-hamdi humidat khisaluhus-saniyyah, ibni Hasyimiw wasmuhu 'Amru-bnu 'Abdi Manafiw wasmuhu-l-Mughiratulladzi yantami-l-irtiqa'u li-'ulyah.",
            translation = "Dan setelah mukaddimah tersebut, aku menyatakan: Dia-lah junjungan kami Nabi Muhammad SAW putra Abdullah, putra Abdul Muththalib yang namanya adalah Syaibatul Hamdi, terpuji seluruh tabiat serta kemuliaannya, putra Hasyim (Amru), putra Abdu Manaf (Mughirah) yang silsilah puncaknya sangat terhormat...",
            instruction = "Nasab agung terpelihara dari masa Nabi Adam hingga Abdullah."
        ),
        MaulidSection(
            id = 3,
            sectionName = "3. Mahal Qiyam (Shallallahu 'Ala Muhammad) - BERDIRI",
            isMahalQiyam = true,
            arabic = "صَلَّى اللهُ عَلَى مُحَمَّدْ، صَلَّى اللهُ عَلَيْهِ وَسَلَّمْ.\nيَا نَبِيْ سَلَامٌ عَلَيْكَ، يَا رَسُوْلْ سَلَامٌ عَلَيْكَ.\nيَا حَبِيْبْ سَلَامٌ عَلَيْكَ، صَلَوَاتُ اللهِ عَلَيْكَ.\nأَبْرَزَ اللهُ الْمُشَفَّعْ، صَاحِبَ الْقَدْرِ الْمُرَفَّعْ.\nمَلأَ الْأَكْوَانَ نُوْرًا، وَعَلَا فِي الْعُلَا مَنْزِلُهْ.",
            latin = "Shallallahu 'ala Muhammad, Shallallahu 'alayhi wa sallim.\nYa Nabi salam 'alayka, Ya Rasul salam 'alayka.\nYa Habib salam 'alayka, shalawatullah 'alayka.\nAbrazallahul-musyaffa', sahibal-qadril-muraffa'.\nMala'al-akwana nura, wa 'ala fil-'ula manziluh.",
            translation = "Semoga rahmat Allah terlimpah atas Muhammad, Semoga rahmat & keselamatan terlimpah atasnya.\nWahai Nabi, salam sejahtera untukmu, Wahai Utusan Allah salam sejahtera untukmu.\nWahai Kekasih salam sejahtera untukmu, shalawat rahmat Allah selalu untukmu.\nAllah telah menampakkan utusan pemberi syafaat, pemilik derajat pangkat luhur ditinggikan.\nMemenuhi jagat semesta raya dengan cahaya benderang, serta posisinya membumbung tinggi di langit.",
            instruction = "Seluruh jamaah dipersilakan berdiri tegak penuh rasa keta'dziman (Mahal Qiyam)."
        )
    )
}

// Complete Composable Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatibMaulidScreen(
    viewModel: KhutbahViewModel,
    initialTab: Int = 0, // 0 = Ratib, 1 = Maulid
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeMainTab by remember { mutableIntStateOf(initialTab) } // 0 = Ratib, 1 = Maulid
    var fontSizeMultiplier by remember { mutableFloatStateOf(1.0f) } // 0.8f to 1.5f for comfortable text resizing
    
    // Sub selections state
    var selectedRatibIndex by remember { mutableIntStateOf(0) } // 0 = Ratib Al-Haddad, 1 = Ratib Al-Athos
    var selectedMaulidIndex by remember { mutableIntStateOf(0) } // 0 = Maulid Simtudduror, 1 = Maulid Al-Barzanji
    
    // Tapping Counter state keeper mapped to Ratib Item id to let user keep track of repetitions
    val dhikrCounters = remember { mutableStateMapOf<String, Int>() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Styled Top App Bar with back and resize choices
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = if (activeMainTab == 0) "Dzikir Ratib" else "Maulid Nabi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Kembali ke Beranda")
                }
            },
            actions = {
                // Interactive font resizing button
                IconButton(
                    onClick = {
                        fontSizeMultiplier = if (fontSizeMultiplier >= 1.4f) 0.9f else fontSizeMultiplier + 0.15f
                    }
                ) {
                    Icon(imageVector = Icons.Default.FormatSize, contentDescription = "Ubah Ukuran Font")
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.shadow(2.dp)
        )

        // Text Size controller preview strip if customized
        if (fontSizeMultiplier != 1.0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                    .padding(vertical = 4.dp, horizontal = 16.dp)
            ) {
                Text(
                    text = "Ukuran teks berukuran ${"%.0f".format(fontSizeMultiplier * 100)}% (Sentuh ikon AA untuk mengubah)",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        // Custom Central Dual Segment Tab Row
        TabRow(
            selectedTabIndex = activeMainTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = activeMainTab == 0,
                onClick = { activeMainTab = 0 },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Wirid Ratib",
                            fontSize = 13.sp,
                            fontWeight = if (activeMainTab == 0) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                },
                modifier = Modifier.testTag("tab_wirid_ratib")
            )
            Tab(
                selected = activeMainTab == 1,
                onClick = { activeMainTab = 1 },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Maulid Nabi",
                            fontSize = 13.sp,
                            fontWeight = if (activeMainTab == 1) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                },
                modifier = Modifier.testTag("tab_maulid_nabi")
            )
        }

        // Display contents dynamically
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (activeMainTab == 0) {
                // Ratib Section
                Column(modifier = Modifier.fillMaxSize()) {
                    // Modern selection pills to toggle Al-Haddad and Al-Athos
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Ratib Al-Haddad", "Ratib Al-Athos").forEachIndexed { index, title ->
                            val isSelected = selectedRatibIndex == index
                            Surface(
                                onClick = { selectedRatibIndex = index },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                        }
                    }

                    // Card explanation
                    Box(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 4.dp)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("✨", fontSize = 24.sp)
                                Column {
                                    Text(
                                        text = if (selectedRatibIndex == 0) "Karya Al-Haddad" else "Karya Al-Athos",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = if (selectedRatibIndex == 0) {
                                            "Dianjurkan dibaca setelah shalat Isya atau Shubuh untuk menjaga benteng diri lahir batin."
                                        } else {
                                            "Dzikir agung sebagai penawar kegalauan serta tameng pelindung dari marabahaya."
                                        },
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Content list or scrollable rows
                    val itemsList = if (selectedRatibIndex == 0) RatibMaulidData.ratibAlHaddad else RatibMaulidData.ratibAlAthos
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(itemsList, key = { item -> "${selectedRatibIndex}_ratib_${item.id}" }) { item ->
                            val counterKey = "${selectedRatibIndex}_counter_${item.id}"
                            val currentCount = dhikrCounters[counterKey] ?: 0

                            RatibCardItem(
                                item = item,
                                currentCount = currentCount,
                                fontSizeMultiplier = fontSizeMultiplier,
                                onIncrementCount = {
                                    if (currentCount < item.targetRepetition) {
                                        dhikrCounters[counterKey] = currentCount + 1
                                    }
                                },
                                onResetCount = {
                                    dhikrCounters[counterKey] = 0
                                }
                            )
                        }
                    }
                }
            } else {
                // Maulid Screen Sections
                Column(modifier = Modifier.fillMaxSize()) {
                    // Modern selection pills to toggle various Maulid
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Simtudduror", "Al-Barzanji").forEachIndexed { index, title ->
                            val isSelected = selectedMaulidIndex == index
                            Surface(
                                onClick = { selectedMaulidIndex = index },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            ) {
                                Text(
                                    text = "Maulid $title",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                        }
                    }

                    // Maulid list of elements
                    val sectionsList = if (selectedMaulidIndex == 0) RatibMaulidData.maulidSimtudduror else RatibMaulidData.maulidBarzanji
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(sectionsList, key = { section -> "${selectedMaulidIndex}_maulid_${section.id}" }) { section ->
                            MaulidCardItem(
                                section = section,
                                fontSizeMultiplier = fontSizeMultiplier
                            )
                        }
                    }
                }
            }
        }
    }
}

// Elegant Card Representation of a single Ratib Dzikir Item
@Composable
fun RatibCardItem(
    item: RatibItem,
    currentCount: Int,
    fontSizeMultiplier: Float,
    onIncrementCount: () -> Unit,
    onResetCount: () -> Unit
) {
    val isCompleted = currentCount >= item.targetRepetition && item.targetRepetition > 1

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isCompleted) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Title + Target repetition Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Repetition badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isCompleted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    contentColor = if (isCompleted) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                ) {
                    Text(
                        text = "Dibaca ${item.targetRepetition}x",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Dzikir Instruction/benefit if any
            if (item.instruction.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.instruction,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.82f),
                    lineHeight = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Arabic text beautifully rendered
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                    .padding(16.dp)
            ) {
                Text(
                    text = item.arabic,
                    fontSize = (21.sp.value * fontSizeMultiplier).sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Right,
                    lineHeight = (34.sp.value * fontSizeMultiplier).sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Latin Transliteration
            Text(
                text = "Latin:",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = item.latin,
                fontSize = (12.5.sp.value * fontSizeMultiplier).sp,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 17.sp,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Translation
            Text(
                text = "Artinya:",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = item.translation,
                fontSize = (12.5.sp.value * fontSizeMultiplier).sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 17.sp,
                modifier = Modifier.padding(top = 2.dp)
            )

            // Dynamic interactive counter if repetition > 1
            if (item.targetRepetition > 1) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Reset button
                        IconButton(
                            onClick = onResetCount,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Ulangi Hitungan", modifier = Modifier.size(16.dp))
                        }

                        Text(
                            text = if (isCompleted) "Selesai dibaca ✨" else "Sisa: ${item.targetRepetition - currentCount}x",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Large circular tap counter bubble
                    Surface(
                        onClick = onIncrementCount,
                        shape = RoundedCornerShape(20.dp),
                        color = if (isCompleted) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        contentColor = if (isCompleted) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onPrimary
                        },
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier
                            .height(44.dp)
                            .widthIn(min = 120.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        ) {
                            Icon(
                                imageVector = if (isCompleted) Icons.Default.Check else Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Ketuk: $currentCount / ${item.targetRepetition}",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// Elegant Card Representation of a single Maulid Section
@Composable
fun MaulidCardItem(
    section: MaulidSection,
    fontSizeMultiplier: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (section.isMahalQiyam) {
                // Highlight yellow/golden background for Mahal Qiyam (The Stand)
                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.22f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (section.isMahalQiyam) {
                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Section name + Standing highlight badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = section.sectionName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.5.sp,
                    color = if (section.isMahalQiyam) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.weight(1f)
                )

                if (section.isMahalQiyam) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    ) {
                        Text(
                            text = "BERDIRI",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (section.instruction.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = section.instruction,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    lineHeight = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Arabic
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (section.isMahalQiyam) {
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                        }
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = section.arabic,
                    fontSize = (21.sp.value * fontSizeMultiplier).sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Right,
                    lineHeight = (34.sp.value * fontSizeMultiplier).sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Latin
            Text(
                text = "Latin:",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (section.isMahalQiyam) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
            )
            Text(
                text = section.latin,
                fontSize = (12.5.sp.value * fontSizeMultiplier).sp,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 17.sp,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Translation
            Text(
                text = "Artinya:",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (section.isMahalQiyam) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
            )
            Text(
                text = section.translation,
                fontSize = (12.5.sp.value * fontSizeMultiplier).sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 17.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
