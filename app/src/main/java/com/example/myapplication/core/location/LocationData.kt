package com.example.myapplication.core.location

data class CityLocation(
    val id: String,
    val name: String,
    val detail: String,
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double,
    val timeZoneOffsetHours: Double
)

object LocationPresets {
    val defaultCity = CityLocation(
        id = "jkt",
        name = "Jakarta Pusat",
        detail = "Masjid Istiqlal, DKI Jakarta",
        latitude = -6.1754,
        longitude = 106.8272,
        altitudeMeters = 25.0,
        timeZoneOffsetHours = 7.0
    )

    // Seluruh 38 Ibu Kota Provinsi di Indonesia + Pilihan Populer
    val cities = listOf(
        // Pulau Sumatera
        CityLocation("ach", "Banda Aceh", "Masjid Raya Baiturrahman, Aceh", 5.5483, 95.3238, 10.0, 7.0),
        CityLocation("mdn", "Medan", "Masjid Raya Al-Mashun, Sumatera Utara", 3.5952, 98.6722, 26.0, 7.0),
        CityLocation("pdg", "Padang", "Masjid Raya Sumatera Barat, Sumatera Barat", -0.9471, 100.4172, 10.0, 7.0),
        CityLocation("pbr", "Pekanbaru", "Masjid Agung An-Nur, Riau", 0.5071, 101.4478, 12.0, 7.0),
        CityLocation("tpi", "Tanjungpinang", "Masjid Raya Sultan Riau, Kepulauan Riau", 0.9167, 104.4500, 15.0, 7.0),
        CityLocation("jmb", "Jambi", "Masjid Agung Al-Falah, Jambi", -1.6101, 103.6131, 16.0, 7.0),
        CityLocation("plb", "Palembang", "Masjid Agung Sultan Mahmud Badaruddin, Sumatera Selatan", -2.9761, 104.7754, 8.0, 7.0),
        CityLocation("pkp", "Pangkalpinang", "Masjid Jami Pangkalpinang, Bangka Belitung", -2.1316, 106.1128, 15.0, 7.0),
        CityLocation("bkl", "Bengkulu", "Masjid Raya Baitul Izzah, Bengkulu", -3.8004, 102.2655, 17.0, 7.0),
        CityLocation("bdl", "Bandar Lampung", "Masjid Agung Al-Furqon, Lampung", -5.4500, 105.2667, 95.0, 7.0),

        // Pulau Jawa
        defaultCity,
        CityLocation("srg", "Serang", "Masjid Agung Ats-Tsaurah, Banten", -6.1104, 106.1640, 39.0, 7.0),
        CityLocation("bdg", "Bandung", "Masjid Raya Bandung, Jawa Barat", -6.9175, 107.6191, 708.0, 7.0),
        CityLocation("smg", "Semarang", "Masjid Agung Jawa Tengah, Jawa Tengah", -6.9667, 110.4167, 4.0, 7.0),
        CityLocation("yog", "Yogyakarta", "Masjid Gedhe Kauman, D.I. Yogyakarta", -7.7956, 110.3695, 113.0, 7.0),
        CityLocation("sby", "Surabaya", "Masjid Nasional Al-Akbar, Jawa Timur", -7.2575, 112.7521, 10.0, 7.0),

        // Bali & Nusa Tenggara
        CityLocation("dps", "Denpasar", "Masjid Agung Sudirman, Bali", -8.6705, 115.2126, 20.0, 8.0),
        CityLocation("mtr", "Mataram", "Islamic Center NTB, Nusa Tenggara Barat", -8.5833, 116.1167, 16.0, 8.0),
        CityLocation("loteng", "Lombok Tengah", "Masjid Agung Praya, NTB", -8.7063, 116.2798, 105.0, 8.0),
        CityLocation("kpg", "Kupang", "Masjid Raya Nuruss'adah, Nusa Tenggara Timur", -10.1772, 123.6070, 42.0, 8.0),

        // Pulau Kalimantan
        CityLocation("ptk", "Pontianak", "Masjid Raya Mujahidin, Kalimantan Barat", -0.0263, 109.3425, 3.0, 7.0),
        CityLocation("pky", "Palangka Raya", "Masjid Raya Darussalam, Kalimantan Tengah", -2.2161, 113.9139, 25.0, 7.0),
        CityLocation("bjm", "Banjarmasin", "Masjid Raya Sabilal Muhtadin, Kalimantan Selatan", -3.3194, 114.5908, 2.0, 8.0),
        CityLocation("smd", "Samarinda", "Islamic Center Samarinda, Kalimantan Timur", -0.5022, 117.1536, 15.0, 8.0),
        CityLocation("ikn", "Nusantara (IKN)", "Ibu Kota Nusantara, Kalimantan Timur", -0.9733, 116.7088, 50.0, 8.0),
        CityLocation("tjs", "Tanjung Selor", "Masjid Agung Istiqomah, Kalimantan Utara", 2.8375, 117.3653, 10.0, 8.0),

        // Pulau Sulawesi
        CityLocation("mnd", "Manado", "Masjid Raya Ahmad Yani, Sulawesi Utara", 1.4748, 124.8428, 10.0, 8.0),
        CityLocation("gto", "Gorontalo", "Masjid Agung Baiturrahim, Gorontalo", 0.5435, 123.0568, 12.0, 8.0),
        CityLocation("pal", "Palu", "Masjid Terapung Arkam Babu Rahman, Sulawesi Tengah", -0.9003, 119.8779, 15.0, 8.0),
        CityLocation("mam", "Mamuju", "Masjid Suhada, Sulawesi Barat", -2.6775, 118.8894, 8.0, 8.0),
        CityLocation("mks", "Makassar", "Masjid 99 Kubah, Sulawesi Selatan", -5.1477, 119.4327, 15.0, 8.0),
        CityLocation("kdr", "Kendari", "Masjid Al-Alam, Sulawesi Tenggara", -3.9985, 122.5126, 12.0, 8.0),

        // Kepulauan Maluku
        CityLocation("amb", "Ambon", "Masjid Raya Al-Fatah, Maluku", -3.6547, 128.1906, 10.0, 9.0),
        CityLocation("sff", "Sofifi / Ternate", "Masjid Raya Shaful Khairaat, Maluku Utara", 0.7303, 127.5683, 10.0, 9.0),

        // Pulau Papua
        CityLocation("jyp", "Jayapura", "Masjid Raya Baiturrahim, Papua", -2.5916, 140.6690, 30.0, 9.0),
        CityLocation("mnk", "Manokwari", "Masjid Ridwanul Bahri, Papua Barat", -0.8615, 134.0620, 20.0, 9.0),
        CityLocation("mrk", "Merauke", "Masjid Raya Al-Aqsha, Papua Selatan", -8.4991, 140.4005, 5.0, 9.0),
        CityLocation("nbr", "Nabire", "Masjid Agung Baiturrahman, Papua Tengah", -3.3667, 135.4833, 10.0, 9.0),
        CityLocation("wmn", "Wamena", "Masjid Baiturrahim Wamena, Papua Pegunungan", -4.0983, 138.9442, 1650.0, 9.0),
        CityLocation("srg_papua", "Sorong", "Masjid Raya Al-Akbar, Papua Barat Daya", -0.8762, 131.2558, 8.0, 9.0)
    )
}
