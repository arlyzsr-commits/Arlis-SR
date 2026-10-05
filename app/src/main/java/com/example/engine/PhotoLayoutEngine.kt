package com.example.engine

import com.example.data.model.ProgressPhoto

enum class LayoutTemplateType {
    HERO_ONE,               // 1 photo
    SPLIT_TWO,              // 2 photos side-by-side
    ONE_LARGE_TWO_SMALL,    // 3 photos: 1 large left, 2 stacked right
    FOUR_GRID_A,            // 4 photos: 2 top, 2 bottom equal
    FOUR_LARGE_LEFT_B,      // 4 photos: 1 big left, 3 stacked right
    FOUR_LARGE_TOP_C,       // 4 photos: 1 big top, 3 horizontal bottom
    FOUR_TWO_TOP_TWO_BOT_D, // 4 photos: 2 wide top, 2 bottom
    FOUR_LARGE_RIGHT_E,     // 4 photos: 3 stacked left, 1 big right
    FOUR_ASYMMETRIC_F       // 4 photos: staggered modern collage
}

data class PhotoSlot(
    val photo: ProgressPhoto,
    val normalizedX: Float,      // 0.0 to 1.0 (relative to photo container area)
    val normalizedY: Float,      // 0.0 to 1.0
    val normalizedWidth: Float,  // 0.0 to 1.0
    val normalizedHeight: Float, // 0.0 to 1.0
    val slotIndex: Int
)

data class LocationReportPage(
    val locationId: Long,
    val locationName: String,
    val workCategory: String,
    val pageIndexForLocation: Int, // 1, 2, 3...
    val totalPagesForLocation: Int,
    val overallPageIndex: Int,     // filled later
    val photos: List<ProgressPhoto>,
    val templateType: LayoutTemplateType,
    val slots: List<PhotoSlot>,
    val subTasksSummary: String = "",
    val subTasksList: List<Pair<String, Int>> = emptyList(), // Detailed list of (SubPekerjaan, progress %)
    val manualNote: String = "",
    val projectManagerMK: String = "",
    val picOwner: String = "",
    val kontraktor: String = "",
    val scopePekerjaan: String = ""
)

object PhotoLayoutEngine {

    /**
     * Groups photos by Location and chunks them into groups of max 4 photos.
     * Chooses an appropriate dynamic layout for each chunk.
     */
    fun createLocationPages(
        photos: List<ProgressPhoto>,
        overrideWorkCategory: String? = null,
        locationNotes: Map<String, String> = emptyMap(),
        projectManagerMK: String = "",
        picOwnerMap: Map<String, String> = emptyMap(),
        kontraktor: String = "",
        scopePekerjaan: String = ""
    ): List<LocationReportPage> {
        // Group by location
        val groupedByLocation = photos.groupBy { it.locationName }
        val resultPages = mutableListOf<LocationReportPage>()

        groupedByLocation.forEach { (locationName, locPhotos) ->
            val firstPhoto = locPhotos.firstOrNull() ?: return@forEach
            val locId = firstPhoto.locationId
            val category = overrideWorkCategory ?: firstPhoto.workCategory

            // Calculate Sub Pekerjaan and their progress percentage
            val subTasksList = locPhotos
                .groupBy { it.subPekerjaan.ifBlank { it.workCategory } }
                .map { (sub, pList) ->
                    val avgProg = pList.map { it.progress }.average().toInt()
                    Pair(sub, avgProg)
                }

            val subTasksSummary = subTasksList.joinToString("  •  ") { "${it.first}: ${it.second}%" }

            val manualNote = locationNotes[locationName] ?: ""
            val upperCat = category.uppercase()
            val picOwner = picOwnerMap[upperCat]
                ?: picOwnerMap[category]
                ?: picOwnerMap.entries.firstOrNull { it.key.contains(upperCat, ignoreCase = true) }?.value
                ?: picOwnerMap["SEMUA"]
                ?: ""

            // Chunk by 4 photos per page
            val chunks = locPhotos.chunked(4)
            val totalPages = chunks.size

            chunks.forEachIndexed { index, chunkPhotos ->
                val template = selectTemplateForChunk(chunkPhotos.size, index)
                val slots = calculateSlots(chunkPhotos, template)

                resultPages.add(
                    LocationReportPage(
                        locationId = locId,
                        locationName = locationName,
                        workCategory = category,
                        pageIndexForLocation = index + 1,
                        totalPagesForLocation = totalPages,
                        overallPageIndex = 0, // Assigned sequentially by caller
                        photos = chunkPhotos,
                        templateType = template,
                        slots = slots,
                        subTasksSummary = subTasksSummary,
                        subTasksList = subTasksList,
                        manualNote = manualNote,
                        projectManagerMK = projectManagerMK,
                        picOwner = picOwner,
                        kontraktor = kontraktor,
                        scopePekerjaan = scopePekerjaan
                    )
                )
            }
        }

        return resultPages
    }

    private fun selectTemplateForChunk(count: Int, chunkIndex: Int): LayoutTemplateType {
        return when (count) {
            1 -> LayoutTemplateType.HERO_ONE
            2 -> LayoutTemplateType.SPLIT_TWO
            3 -> LayoutTemplateType.ONE_LARGE_TWO_SMALL
            4 -> {
                // Vary template systematically/cyclically so each page has distinctive rhythm
                val fourTemplates = listOf(
                    LayoutTemplateType.FOUR_GRID_A,
                    LayoutTemplateType.FOUR_LARGE_LEFT_B,
                    LayoutTemplateType.FOUR_LARGE_TOP_C,
                    LayoutTemplateType.FOUR_LARGE_RIGHT_E,
                    LayoutTemplateType.FOUR_ASYMMETRIC_F,
                    LayoutTemplateType.FOUR_TWO_TOP_TWO_BOT_D
                )
                fourTemplates[chunkIndex % fourTemplates.size]
            }
            else -> LayoutTemplateType.FOUR_GRID_A
        }
    }

    /**
     * Computes normalized coordinates (0..1) for rendering both in Jetpack Compose
     * and in PDF Document Canvas.
     */
    fun calculateSlots(photos: List<ProgressPhoto>, template: LayoutTemplateType): List<PhotoSlot> {
        val count = photos.size
        val gap = 0.02f // normalized gap between slots

        return when (template) {
            LayoutTemplateType.HERO_ONE -> {
                if (count == 0) emptyList()
                else listOf(
                    PhotoSlot(photos[0], 0f, 0f, 1f, 1f, 0)
                )
            }
            LayoutTemplateType.SPLIT_TWO -> {
                val w = (1f - gap) / 2f
                photos.mapIndexed { idx, p ->
                    PhotoSlot(p, idx * (w + gap), 0f, w, 1f, idx)
                }
            }
            LayoutTemplateType.ONE_LARGE_TWO_SMALL -> {
                // 1 big on left (0.6 width), 2 smaller stacked on right (0.4 width)
                val leftW = 0.58f
                val rightW = 1f - leftW - gap
                val halfH = (1f - gap) / 2f

                photos.mapIndexed { idx, p ->
                    when (idx) {
                        0 -> PhotoSlot(p, 0f, 0f, leftW, 1f, 0)
                        1 -> PhotoSlot(p, leftW + gap, 0f, rightW, halfH, 1)
                        else -> PhotoSlot(p, leftW + gap, halfH + gap, rightW, halfH, 2)
                    }
                }
            }
            LayoutTemplateType.FOUR_GRID_A -> {
                // 2 top, 2 bottom equal
                val w = (1f - gap) / 2f
                val h = (1f - gap) / 2f
                photos.mapIndexed { idx, p ->
                    val col = idx % 2
                    val row = idx / 2
                    PhotoSlot(p, col * (w + gap), row * (h + gap), w, h, idx)
                }
            }
            LayoutTemplateType.FOUR_LARGE_LEFT_B -> {
                // 1 big on left, 3 small stacked vertically on right
                val leftW = 0.58f
                val rightW = 1f - leftW - gap
                val thirdH = (1f - 2 * gap) / 3f

                photos.mapIndexed { idx, p ->
                    when (idx) {
                        0 -> PhotoSlot(p, 0f, 0f, leftW, 1f, 0)
                        1 -> PhotoSlot(p, leftW + gap, 0f, rightW, thirdH, 1)
                        2 -> PhotoSlot(p, leftW + gap, thirdH + gap, rightW, thirdH, 2)
                        else -> PhotoSlot(p, leftW + gap, 2 * (thirdH + gap), rightW, thirdH, 3)
                    }
                }
            }
            LayoutTemplateType.FOUR_LARGE_TOP_C -> {
                // 1 big on top, 3 smaller horizontal on bottom
                val topH = 0.58f
                val botH = 1f - topH - gap
                val thirdW = (1f - 2 * gap) / 3f

                photos.mapIndexed { idx, p ->
                    when (idx) {
                        0 -> PhotoSlot(p, 0f, 0f, 1f, topH, 0)
                        1 -> PhotoSlot(p, 0f, topH + gap, thirdW, botH, 1)
                        2 -> PhotoSlot(p, thirdW + gap, topH + gap, thirdW, botH, 2)
                        else -> PhotoSlot(p, 2 * (thirdW + gap), topH + gap, thirdW, botH, 3)
                    }
                }
            }
            LayoutTemplateType.FOUR_LARGE_RIGHT_E -> {
                // 3 small stacked on left, 1 big on right
                val leftW = 0.40f
                val rightW = 1f - leftW - gap
                val thirdH = (1f - 2 * gap) / 3f

                photos.mapIndexed { idx, p ->
                    when (idx) {
                        0 -> PhotoSlot(p, leftW + gap, 0f, rightW, 1f, 0)
                        1 -> PhotoSlot(p, 0f, 0f, leftW, thirdH, 1)
                        2 -> PhotoSlot(p, 0f, thirdH + gap, leftW, thirdH, 2)
                        else -> PhotoSlot(p, 0f, 2 * (thirdH + gap), leftW, thirdH, 3)
                    }
                }
            }
            LayoutTemplateType.FOUR_ASYMMETRIC_F, LayoutTemplateType.FOUR_TWO_TOP_TWO_BOT_D -> {
                // Staggered asymmetric: top row (0.55w, 0.45w), bot row (0.42w, 0.58w)
                val h = (1f - gap) / 2f
                val topW1 = 0.54f
                val topW2 = 1f - topW1 - gap
                val botW1 = 0.44f
                val botW2 = 1f - botW1 - gap

                photos.mapIndexed { idx, p ->
                    when (idx) {
                        0 -> PhotoSlot(p, 0f, 0f, topW1, h, 0)
                        1 -> PhotoSlot(p, topW1 + gap, 0f, topW2, h, 1)
                        2 -> PhotoSlot(p, 0f, h + gap, botW1, h, 2)
                        else -> PhotoSlot(p, botW1 + gap, h + gap, botW2, h, 3)
                    }
                }
            }
        }
    }
}
