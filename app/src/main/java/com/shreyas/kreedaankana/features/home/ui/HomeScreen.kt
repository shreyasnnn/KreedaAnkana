package com.shreyas.kreedaankana.features.home.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shreyas.kreedaankana.core.auth.AuthManager
import com.shreyas.kreedaankana.features.notifications.viewmodel.NotificationViewModel
import com.shreyas.kreedaankana.features.challenge.viewmodel.ChallengeViewModel
import com.shreyas.kreedaankana.features.booking.viewmodel.BookingViewModel
import com.shreyas.kreedaankana.features.booking.data.Booking
import com.shreyas.kreedaankana.features.team.viewmodel.TeamViewModel

@Composable
fun HomeScreen(
    onNavigateToBooking: () -> Unit,
    onNavigateToPostChallenge: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToScores: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToCreateTeam: () -> Unit,
    onNavigateToDiscoverTeams: () -> Unit,
    onNavigateToMyTeams: () -> Unit,
    onNavigateToMyBookings: () -> Unit, // 🔹 NEW: Added My Bookings navigation callback
    challengeViewModel: ChallengeViewModel = viewModel(),
    notifViewModel: NotificationViewModel = viewModel(),
    bookingViewModel: BookingViewModel = viewModel(),
    teamViewModel: TeamViewModel = viewModel()
) {
    val name = AuthManager.getUserName()
    val userId = AuthManager.getUserId()

    val challengeState by challengeViewModel.state.collectAsState()
    val notifState by notifViewModel.state.collectAsState()
    val bookingState by bookingViewModel.state.collectAsState()
    val teamState by teamViewModel.state.collectAsState()

    val myTeamIds = teamState.myTeams.map { it.id }

    LaunchedEffect(Unit) {
        teamViewModel.loadMyTeams(userId)
        bookingViewModel.loadUserBookings(userId)
    }

    LaunchedEffect(myTeamIds) {
        challengeViewModel.loadInbox(myTeamIds)
    }

    val upcomingMatches = challengeState.incoming.filter { it.status == "open" || it.status == "scheduled" }
    val unreadCount = notifState.notifications.count { !it.isRead }
    val nextBooking = bookingState.bookings.firstOrNull { it.status == "confirmed" }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF0F7F8))) {
        Row(Modifier.fillMaxWidth().padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF00C853)), Alignment.Center) {
                Text(name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text("Good morning 👋", fontSize = 12.sp, color = Color.Gray)
                Text(name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            IconButton(onClick = onNavigateToNotifications) {
                BadgedBox(badge = { if (unreadCount > 0) Badge { Text(unreadCount.toString()) } }) {
                    Icon(Icons.Default.Notifications, null, tint = Color(0xFFFFB300))
                }
            }
        }

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { NextBookingHero(nextBooking, onNavigateToBooking) }

            item {
                Text("QUICK ACTIONS", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.Gray, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ActionCard(Modifier.weight(1f), "My Teams", Icons.Default.Groups, Color(0xFF00E676), onNavigateToMyTeams)
                    ActionCard(Modifier.weight(1f), "Join Team", Icons.Default.Search, Color.White, onNavigateToDiscoverTeams)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ActionCard(Modifier.weight(1f), "Create Team", Icons.Default.GroupAdd, Color.White, onNavigateToCreateTeam)
                    ActionCard(Modifier.weight(1f), "Post Challenge", Icons.Default.FlashOn, Color.White, onNavigateToPostChallenge)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ActionCard(Modifier.weight(1f), "Book Ground", Icons.Default.CalendarMonth, Color.White, onNavigateToBooking)
                    ActionCard(Modifier.weight(1f), "Team Schedule", Icons.Default.EventNote, Color.White, onNavigateToSchedule)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ActionCard(Modifier.weight(1f), "View Scores", Icons.Default.BarChart, Color.White, onNavigateToScores)
                    // 🔹 NEW: Replaced the Spacer with the My Bookings ActionCard
                    ActionCard(Modifier.weight(1f), "My Bookings", Icons.Default.Bookmarks, Color.White, onNavigateToMyBookings)
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("OPEN CHALLENGES", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.Gray, letterSpacing = 1.sp)
                    Text("See all →", color = Color(0xFF00C853), fontSize = 12.sp, modifier = Modifier.clickable { onNavigateToSchedule() })
                }
            }

            if (upcomingMatches.isEmpty()) {
                item { Text("No open challenges.", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp)) }
            } else {
                items(upcomingMatches.take(3)) { challenge ->
                    DashboardMatchCard(
                        title = "vs Any Team",
                        subtitle = "${challenge.sportType} • ${challenge.location.ifEmpty { "TBD" }}",
                        status = challenge.status.uppercase(),
                        color = if (challenge.status == "scheduled") Color(0xFF00C853) else Color(0xFFFFB300)
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }

            // Extra padding at the bottom so cards aren't hidden behind the floating nav bar
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun NextBookingHero(booking: Booking?, onBookNow: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF004D40))) {
        Box(Modifier.padding(24.dp)) {
            if (booking != null) {
                Column {
                    Text("NEXT BOOKING", color = Color.White.copy(0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(booking.groundName, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("${booking.date} • ${booking.timeSlot}", color = Color.White.copy(0.8f))
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("No Active Bookings", color = Color.White, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onBookNow) { Text("Book Ground Now", color = Color(0xFFC6FF00)) }
                }
            }
        }
    }
}

@Composable
fun ActionCard(modifier: Modifier, title: String, icon: ImageVector, bg: Color, onClick: () -> Unit) {
    // Determine colors based on whether this is a "Primary" card (Green background passed in) or Secondary (White)
    val isPrimary = bg != Color.White
    val containerColor = if (isPrimary) Color(0xFF004D40) else Color.White
    val contentColor = if (isPrimary) Color.White else Color(0xFF004D40)
    val iconBoxBg = if (isPrimary) Color(0xFF00E676) else Color(0xFFE8F5E9)
    val iconTintColor = if (isPrimary) Color(0xFF004D40) else Color(0xFF2E7D32)

    Card(
        modifier = modifier
            .height(130.dp) // Taller for a premium widget feel
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp), // Smoother, larger corners
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(if (isPrimary) 6.dp else 2.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            // Subtle Background Watermark Accent
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 24.dp, y = 24.dp)
                    .size(80.dp)
                    .background(
                        if (isPrimary) Color.White.copy(alpha = 0.05f) else Color(0xFF00E676).copy(alpha = 0.05f),
                        CircleShape
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween, // Spreads icon and text
                horizontalAlignment = Alignment.Start // Left-aligns content
            ) {
                // Top Left: Icon in a soft box
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(iconBoxBg, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconTintColor, modifier = Modifier.size(24.dp))
                }

                // Bottom: Title and subtle arrow
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = contentColor,
                        modifier = Modifier.weight(1f),
                        lineHeight = 18.sp
                    )

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Go",
                        tint = contentColor.copy(alpha = 0.3f), // Very subtle indicator
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardMatchCard(title: String, subtitle: String, status: String, color: Color) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(Color(0xFFE8F5E9)), Alignment.Center) {
                Text("C", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
            }
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            Surface(shape = CircleShape, color = color.copy(alpha = 0.15f)) {
                Text(text = status, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
            }
        }
    }
}