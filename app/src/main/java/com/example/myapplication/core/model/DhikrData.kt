package com.example.myapplication.core.model

data class DhikrItem(
    val id: String,
    val sequenceNumber: Int,
    val title: String,
    val arabicText: String,
    val transliteration: String,
    val translation: String,
    val targetCount: Int
)

object DhikrPresets {
    // 1. Dzikir Setelah Salat Fardhu
    val postPrayerDhikr = listOf(
        DhikrItem(
            id = "post_istighfar",
            sequenceNumber = 1,
            title = "Istighfar",
            arabicText = "أَسْتَغْفِرُ اللَّهَ",
            transliteration = "Astaghfirullah (3x)",
            translation = "Aku memohon ampunan kepada Allah.",
            targetCount = 3
        ),
        DhikrItem(
            id = "post_salam",
            sequenceNumber = 2,
            title = "Doa Keselamatan",
            arabicText = "اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ، تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالإِكْرَامِ",
            transliteration = "Allahumma antas-salam wa minkas-salam tabarakta ya dzal-jalali wal-ikram",
            translation = "Ya Allah, Engkaulah As-Salam (Yang memberi keselamatan) dan dari-Mu keselamatan, Mahasuci Engkau wahai Tuhan Yang Memiliki Keagungan dan Kemuliaan.",
            targetCount = 1
        ),
        DhikrItem(
            id = "post_ayat_kursi",
            sequenceNumber = 3,
            title = "Ayat Kursi",
            arabicText = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ",
            transliteration = "Allahu laa ilaaha illaa Huwal Hayyul Qayyum, laa ta'khudzuhuu sinatuw wa laa nawm...",
            translation = "Allah, tidak ada tuhan selain Dia. Yang Mahahidup, yang terus-menerus mengurus makhluk-Nya, tidak mengantuk dan tidak tidur.",
            targetCount = 1
        ),
        DhikrItem(
            id = "post_tasbih",
            sequenceNumber = 4,
            title = "Tasbih",
            arabicText = "سُبْحَانَ اللَّهِ",
            transliteration = "Subhanallah",
            translation = "Maha Suci Allah.",
            targetCount = 33
        ),
        DhikrItem(
            id = "post_tahmid",
            sequenceNumber = 5,
            title = "Tahmid",
            arabicText = "الْحَمْدُ لِلَّهِ",
            transliteration = "Alhamdulillah",
            translation = "Segala Puji bagi Allah.",
            targetCount = 33
        ),
        DhikrItem(
            id = "post_takbir",
            sequenceNumber = 6,
            title = "Takbir",
            arabicText = "اللَّهُ أَكْبَرُ",
            transliteration = "Allahu Akbar",
            translation = "Allah Maha Besar.",
            targetCount = 33
        ),
        DhikrItem(
            id = "post_tahlil",
            sequenceNumber = 7,
            title = "Tahlil Penutup",
            arabicText = "لَا إِلَهَ إِلَّا اللهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
            transliteration = "Laa ilaaha illallahu wahdahu laa syariika lah, lahul mulku wa lahul hamdu wa Huwa 'ala kulli syai'in qadiir",
            translation = "Tiada sesembahan yang berhak disembah selain Allah semata, tiada sekutu bagi-Nya. Milik-Nya segala kerajaan dan pujian, dan Dia Mahakuasa atas segala sesuatu.",
            targetCount = 1
        )
    )

    // 2. Dzikir Pagi Sesuai Sunnah
    val morningDhikr = listOf(
        DhikrItem(
            id = "morning_ayat_kursi",
            sequenceNumber = 1,
            title = "Ayat Kursi",
            arabicText = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ",
            transliteration = "Allahu laa ilaaha illaa Huwal Hayyul Qayyum...",
            translation = "Barangsiapa membacanya di pagi hari, akan dilindungi dari godaan setan hingga sore hari.",
            targetCount = 1
        ),
        DhikrItem(
            id = "morning_sayyidul_istighfar",
            sequenceNumber = 2,
            title = "Sayyidul Istighfar",
            arabicText = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ",
            transliteration = "Allahumma anta Rabbi laa ilaaha illa anta, khalaqtani wa ana 'abduka wa ana 'ala 'ahdika wa wa'dika mastatha'tu...",
            translation = "Ya Allah, Engkaulah Tuhanku, tiada tuhan selain Engkau. Engkau menciptakanku dan aku hamba-Mu. Aku memohon ampunan-Mu atas segala dosaku.",
            targetCount = 1
        ),
        DhikrItem(
            id = "morning_doa_pagi",
            sequenceNumber = 3,
            title = "Doa Menyambut Pagi",
            arabicText = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
            transliteration = "Ashbahna wa ashbahal mulku lillah, walhamdulillah, laa ilaaha illallahu wahdahu laa syariika lah...",
            translation = "Kami memasuki pagi hari dan kerajaan hanya milik Allah. Segala puji bagi Allah, tiada tuhan selain Allah semata.",
            targetCount = 1
        ),
        DhikrItem(
            id = "morning_muawwidzat",
            sequenceNumber = 4,
            title = "Al-Ikhlas, Al-Falaq, An-Nas",
            arabicText = "قُلْ هُوَ اللَّهُ أَحَدٌ • قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ • قُلْ أَعُوذُ بِرَبِّ النَّاسِ",
            transliteration = "Membaca Surat Al-Ikhlas, Al-Falaq, dan An-Nas masing-masing 3 kali",
            translation = "Mencukupi pembacanya dari segala marabahaya dan keburukan makhluk di waktu pagi.",
            targetCount = 3
        ),
        DhikrItem(
            id = "morning_bismillah",
            sequenceNumber = 5,
            title = "Perlindungan Nama Allah",
            arabicText = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
            transliteration = "Bismillahilladzi laa yadhurru ma'asmihi syai'un fil ardhi wa laa fis samaa'i wa Huwas Samii'ul 'Aliim",
            translation = "Dengan nama Allah yang dengan nama-Nya tidak ada sesuatu pun di bumi dan di langit yang dapat membahayakan, dan Dia Maha Mendengar lagi Maha Mengetahui.",
            targetCount = 3
        ),
        DhikrItem(
            id = "morning_radhitu",
            sequenceNumber = 6,
            title = "Keridhaan Iman",
            arabicText = "رَضِيتُ بِاللَّهِ رَبًّا، وَبِالْإِسْلَامِ دِينًا، وَبِمُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ نَبِيًّا",
            transliteration = "Radhiitu billaahi Rabba, wa bil Islaami diinaa, wa bi Muhammadin shallallaahu 'alayhi wa sallama Nabiyya",
            translation = "Aku rela Allah sebagai Tuhanku, Islam sebagai agamaku, dan Nabi Muhammad SAW sebagai Nabiku.",
            targetCount = 3
        ),
        DhikrItem(
            id = "morning_tasbih_100",
            sequenceNumber = 7,
            title = "Penghapus Dosa",
            arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
            transliteration = "Subhanallahi wa bihamdihi (100x)",
            translation = "Maha Suci Allah dan segala puji bagi-Nya. Barangsiapa mengucapkannya 100x dosanya akan diampuni meskipun sebanyak buih di lautan.",
            targetCount = 100
        )
    )

    // 3. Dzikir Petang Sesuai Sunnah
    val eveningDhikr = listOf(
        DhikrItem(
            id = "evening_ayat_kursi",
            sequenceNumber = 1,
            title = "Ayat Kursi",
            arabicText = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ",
            transliteration = "Allahu laa ilaaha illaa Huwal Hayyul Qayyum...",
            translation = "Menjaga pembacanya sepanjang malam hingga terbit fajar.",
            targetCount = 1
        ),
        DhikrItem(
            id = "evening_sayyidul_istighfar",
            sequenceNumber = 2,
            title = "Sayyidul Istighfar",
            arabicText = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ",
            transliteration = "Allahumma anta Rabbi laa ilaaha illa anta, khalaqtani wa ana 'abduka wa ana 'ala 'ahdika wa wa'dika mastatha'tu...",
            translation = "Doa permohonan ampunan paling utama dari segala kelalaian sepanjang hari.",
            targetCount = 1
        ),
        DhikrItem(
            id = "evening_doa_petang",
            sequenceNumber = 3,
            title = "Doa Menyambut Petang",
            arabicText = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
            transliteration = "Amsayna wa amsal mulku lillah, walhamdulillah, laa ilaaha illallahu wahdahu laa syariika lah...",
            translation = "Kami memasuki waktu sore dan kerajaan hanya milik Allah. Segala puji bagi Allah semata.",
            targetCount = 1
        ),
        DhikrItem(
            id = "evening_muawwidzat",
            sequenceNumber = 4,
            title = "Al-Ikhlas, Al-Falaq, An-Nas",
            arabicText = "قُلْ هُوَ اللَّهُ أَحَدٌ • قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ • قُلْ أَعُوذُ بِرَبِّ النَّاسِ",
            transliteration = "Membaca Surat Al-Ikhlas, Al-Falaq, dan An-Nas masing-masing 3 kali",
            translation = "Perlindungan malam hari dari segala godaan dan kejahatan makhluk.",
            targetCount = 3
        ),
        DhikrItem(
            id = "evening_bismillah",
            sequenceNumber = 5,
            title = "Perlindungan Nama Allah",
            arabicText = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
            transliteration = "Bismillahilladzi laa yadhurru ma'asmihi syai'un fil ardhi wa laa fis samaa'i wa Huwas Samii'ul 'Aliim",
            translation = "Tidak akan tertimpa marabahaya mendadak sepanjang malam.",
            targetCount = 3
        ),
        DhikrItem(
            id = "evening_audzu",
            sequenceNumber = 6,
            title = "Perlindungan Kalimat Allah",
            arabicText = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
            transliteration = "A'uudzu bikalimaatillaahit-taammaati min syarri maa khalaq",
            translation = "Aku berlindung dengan kalimat-kalimat Allah yang sempurna dari kejahatan apa yang diciptakan-Nya.",
            targetCount = 3
        ),
        DhikrItem(
            id = "evening_tasbih_100",
            sequenceNumber = 7,
            title = "Tasbih & Tahmid",
            arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
            transliteration = "Subhanallahi wa bihamdihi (100x)",
            translation = "Maha Suci Allah dan segala puji bagi-Nya.",
            targetCount = 100
        )
    )

    // 4. Dzikir Bebas / Harian
    val freeDhikr = listOf(
        DhikrItem(
            id = "free_tasbih",
            sequenceNumber = 1,
            title = "Tasbih",
            arabicText = "سُبْحَانَ اللَّهِ",
            transliteration = "Subhanallah",
            translation = "Maha Suci Allah.",
            targetCount = 33
        ),
        DhikrItem(
            id = "free_tahmid",
            sequenceNumber = 2,
            title = "Tahmid",
            arabicText = "الْحَمْدُ لِلَّهِ",
            transliteration = "Alhamdulillah",
            translation = "Segala puji bagi Allah.",
            targetCount = 33
        ),
        DhikrItem(
            id = "free_takbir",
            sequenceNumber = 3,
            title = "Takbir",
            arabicText = "اللَّهُ أَكْبَرُ",
            transliteration = "Allahu Akbar",
            translation = "Allah Maha Besar.",
            targetCount = 33
        ),
        DhikrItem(
            id = "free_tahlil",
            sequenceNumber = 4,
            title = "Tahlil",
            arabicText = "لَا إِلَهَ إِلَّا اللهُ",
            transliteration = "Laa ilaaha illallah",
            translation = "Tiada sesembahan yang berhak disembah selain Allah.",
            targetCount = 100
        ),
        DhikrItem(
            id = "free_istighfar",
            sequenceNumber = 5,
            title = "Istighfar",
            arabicText = "أَسْتَغْفِرُ اللَّهَ الْعَظِيمَ وَأَتُوبُ إِلَيْهِ",
            transliteration = "Astaghfirullahal 'Azhiim wa atuubu ilayh",
            translation = "Aku memohon ampunan kepada Allah Yang Mahaagung dan bertaubat kepada-Nya.",
            targetCount = 100
        ),
        DhikrItem(
            id = "free_shalawat",
            sequenceNumber = 6,
            title = "Shalawat Nabi",
            arabicText = "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ",
            transliteration = "Allahumma shalli 'ala Muhammad wa 'ala aali Muhammad",
            translation = "Ya Allah, limpahkanlah rahmat kepada Nabi Muhammad dan keluarga Nabi Muhammad.",
            targetCount = 100
        ),
        DhikrItem(
            id = "free_hauqalah",
            sequenceNumber = 7,
            title = "Hauqalah",
            arabicText = "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ الْعَلِيِّ الْعَظِيمِ",
            transliteration = "Laa hawla wa laa quwwata illaa billaahil 'Aliyyil 'Azhiim",
            translation = "Tiada daya dan upaya kecuali dengan pertolongan Allah Yang Mahatinggi lagi Mahaagung.",
            targetCount = 33
        )
    )

    fun getItemsForCategory(category: String): List<DhikrItem> {
        return when (category) {
            "Dzikir Pagi" -> morningDhikr
            "Dzikir Petang" -> eveningDhikr
            "Dzikir Bebas" -> freeDhikr
            else -> postPrayerDhikr
        }
    }
}
