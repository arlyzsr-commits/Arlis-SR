package com.example.data.model

enum class WorkCategory(val displayName: String, val subtitle: String) {
    STRUKTUR("STRUKTUR", "Pondasi, Kolom, Balok, Plat, Tangga"),
    ARSITEK("ARSITEK", "Dinding, Lantai, Plafon, Finishing"),
    MEP("MEP", "Mechanical, Electrical, Plumbing");

    companion object {
        fun fromString(value: String): WorkCategory {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: STRUKTUR
        }

        fun getSubTasks(category: WorkCategory): List<String> {
            return when (category) {
                STRUKTUR -> listOf(
                    "Pondasi",
                    "Kolom",
                    "Balok",
                    "Plat",
                    "Tangga",
                    "Struktur baja",
                    "Struktur kayu",
                    "Pekerjaan lainnya"
                )
                ARSITEK -> listOf(
                    "Dinding",
                    "Lantai",
                    "Plafon",
                    "Pintu & jendela",
                    "Finishing",
                    "Interior",
                    "Eksterior",
                    "Pekerjaan lainnya"
                )
                MEP -> listOf(
                    "Electrical",
                    "Plumbing",
                    "HVAC",
                    "Fire Fighting",
                    "ICT",
                    "Sound System",
                    "Ventilation",
                    "Solar Panel",
                    "Pekerjaan lainnya"
                )
            }
        }
    }
}

enum class ProgressStatus(val label: String) {
    BELUM_MULAI("Belum Mulai"),
    SEDANG_DIKERJAKAN("Sedang Dikerjakan"),
    PROGRESS("Progress"),
    SELESAI("Selesai"),
    PERBAIKAN("Perbaikan");

    companion object {
        fun fromLabel(label: String): ProgressStatus {
            return entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: SEDANG_DIKERJAKAN
        }
    }
}
