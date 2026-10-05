package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.Architecture
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.outlined.Foundation
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProgressStatus
import com.example.data.model.WorkCategory
import com.example.ui.theme.PastelAmberContainer
import com.example.ui.theme.PastelAmberWarning
import com.example.ui.theme.PastelBorderColor
import com.example.ui.theme.PastelCharcoalText
import com.example.ui.theme.PastelCreamBackground
import com.example.ui.theme.PastelDustyBlue
import com.example.ui.theme.PastelDustyBlueContainer
import com.example.ui.theme.PastelMutedPeach
import com.example.ui.theme.PastelMutedPeachContainer
import com.example.ui.theme.PastelMutedText
import com.example.ui.theme.PastelSageContainer
import com.example.ui.theme.PastelSageOnContainer
import com.example.ui.theme.PastelSagePrimary
import com.example.ui.theme.PastelSurfaceCard
import com.example.ui.theme.StatusBelumMulai
import com.example.ui.theme.StatusPerbaikan
import com.example.ui.theme.StatusProgress
import com.example.ui.theme.StatusSedangDikerjakan
import com.example.ui.theme.StatusSelesai

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status) {
        ProgressStatus.SELESAI.label -> Color(0xFFE5F5EC) to StatusSelesai
        ProgressStatus.SEDANG_DIKERJAKAN.label -> PastelDustyBlueContainer to StatusSedangDikerjakan
        ProgressStatus.PROGRESS.label -> PastelAmberContainer to StatusProgress
        ProgressStatus.PERBAIKAN.label -> PastelMutedPeachContainer to StatusPerbaikan
        else -> Color(0xFFEBEBEB) to StatusBelumMulai
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = status,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor.copy(alpha = 0.85f),
                    letterSpacing = 0.5.sp
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = contentColor
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = contentColor.copy(alpha = 0.75f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun WorkCategoryCard(
    category: WorkCategory,
    photoCount: Int,
    progressPercentage: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val (containerColor, accentColor, icon) = when (category) {
        WorkCategory.STRUKTUR -> Triple(PastelSageContainer, PastelSagePrimary, Icons.Outlined.Foundation)
        WorkCategory.ARSITEK -> Triple(PastelDustyBlueContainer, PastelDustyBlue, Icons.Outlined.Architecture)
        WorkCategory.MEP -> Triple(PastelMutedPeachContainer, PastelMutedPeach, Icons.Filled.ElectricBolt)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("work_category_card_${category.name.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PastelSurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(containerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = category.displayName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelCharcoalText
                    )
                    Text(
                        text = "$progressPercentage%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = category.subtitle,
                    fontSize = 12.sp,
                    color = PastelMutedText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LinearProgressIndicator(
                        progress = { progressPercentage / 100f },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = accentColor,
                        trackColor = containerColor
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "$photoCount foto",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = PastelMutedText
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = PastelMutedText.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

enum class LapoorNavDestination(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    GALERI("Lihat Foto", Icons.Default.PhotoLibrary),
    FOTO("Ambil Foto", Icons.Default.CameraAlt),
    LAPORAN("Laporan", Icons.Default.Description),
    PROYEK("Proyek", Icons.Default.Business),
    PROFIL("Profil", Icons.Default.Info)
}

@Composable
fun LapoorBottomBar(
    currentDestination: LapoorNavDestination,
    onNavigate: (LapoorNavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val bottomNavItems = listOf(
        LapoorNavDestination.HOME,
        LapoorNavDestination.GALERI,
        LapoorNavDestination.FOTO,
        LapoorNavDestination.LAPORAN,
        LapoorNavDestination.PROYEK
    )

    NavigationBar(
        modifier = modifier
            .border(width = 0.5.dp, color = PastelBorderColor, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        containerColor = Color.White,
        tonalElevation = 6.dp
    ) {
        bottomNavItems.forEach { destination ->
            val isSelected = currentDestination == destination
            val isCamera = destination == LapoorNavDestination.FOTO

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(destination) },
                icon = {
                    if (isCamera) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(PastelSagePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.label,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                label = {
                    Text(
                        text = destination.label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PastelSagePrimary,
                    selectedTextColor = PastelSagePrimary,
                    indicatorColor = if (isCamera) Color.Transparent else PastelSageContainer,
                    unselectedIconColor = PastelMutedText,
                    unselectedTextColor = PastelMutedText
                ),
                modifier = Modifier.testTag("nav_tab_${destination.name.lowercase()}")
            )
        }
    }
}
