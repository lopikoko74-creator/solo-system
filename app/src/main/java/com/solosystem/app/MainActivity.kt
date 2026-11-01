package com.solosystem.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

private val Void = Color(0xFF070B12)
private val Panel = Color(0xFF101A2A)
private val Blue = Color(0xFF38BDF8)
private val Cyan = Color(0xFF67E8F9)
private val Red = Color(0xFFFF5263)
private val Muted = Color(0xFF8EA5BD)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("solo_system_v5", MODE_PRIVATE)
        setContent { SoloApp(prefs) }
    }
}

data class Quest(val id: String, val title: String, val detail: String, val reward: Int, val penalty: Int, val weekly: Boolean = false)

@Composable
fun SoloApp(prefs: android.content.SharedPreferences) {
    var xp by remember { mutableIntStateOf(prefs.getInt("xp", 0)) }
    var level by remember { mutableIntStateOf(prefs.getInt("level", 1)) }
    var strength by remember { mutableIntStateOf(prefs.getInt("strength", 5)) }
    var endurance by remember { mutableIntStateOf(prefs.getInt("endurance", 5)) }
    var agility by remember { mutableIntStateOf(prefs.getInt("agility", 5)) }
    var intelligence by remember { mutableIntStateOf(prefs.getInt("intelligence", 5)) }
    var discipline by remember { mutableIntStateOf(prefs.getInt("discipline", 5)) }
    var streak by remember { mutableIntStateOf(prefs.getInt("streak", 0)) }
    var penalties by remember { mutableIntStateOf(prefs.getInt("penalties", 0)) }
    var tab by remember { mutableIntStateOf(0) }
    val today = LocalDate.now().toString()
    val week = LocalDate.now().get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear()).toString() + "-" + LocalDate.now().year
    val daily = listOf(
        Quest("move", "АКТИВНОСТЬ", "30 минут ходьбы или тренировки", 120, 300),
        Quest("learn", "ОБУЧЕНИЕ", "25 минут изучения полезного навыка", 100, 250),
        Quest("order", "ДИСЦИПЛИНА", "Навести порядок и выполнить главное дело дня", 100, 250),
        Quest("water", "РЕЖИМ", "Поддерживать нормальный питьевой режим", 80, 200)
    )
    val weekly = listOf(
        Quest("weekly_body", "НЕДЕЛЯ ТЕЛА", "3 умеренные тренировки или прогулки", 400, 800, true),
        Quest("weekly_focus", "НЕДЕЛЯ ФОКУСА", "5 учебных сессий по 25 минут", 350, 700, true)
    )
    fun save() { prefs.edit().putInt("xp", xp).putInt("level", level).putInt("strength", strength).putInt("endurance", endurance).putInt("agility", agility).putInt("intelligence", intelligence).putInt("discipline", discipline).putInt("streak", streak).putInt("penalties", penalties).apply() }
    fun addXp(amount: Int) {
        xp = (xp + amount).coerceAtLeast(0)
        while (xp >= level * 500) { xp -= level * 500; level++ }
        save()
    }
    fun complete(q: Quest) {
        val key = if (q.weekly) "done_${q.id}_$week" else "done_${q.id}_$today"
        if (!prefs.getBoolean(key, false)) { prefs.edit().putBoolean(key, true).apply(); addXp(q.reward); streak++ ; save() }
    }
    // Apply one S-rank missed-task penalty per expired period, only after the user has opened the app again.
    val lastDay = prefs.getString("last_day", today) ?: today
    if (lastDay != today) {
        val missed = daily.count { !prefs.getBoolean("done_${it.id}_$lastDay", false) }
        if (missed > 0 && !prefs.getBoolean("penalty_applied_$lastDay", false)) {
            val multiplier = if (streak >= 2) 2 else 1
            val penalty = daily.filter { !prefs.getBoolean("done_${it.id}_$lastDay", false) }.sumOf { it.penalty } * multiplier
            xp = (xp - penalty).coerceAtLeast(0); penalties += missed; streak = 0
            prefs.edit().putBoolean("penalty_applied_$lastDay", true).apply(); save()
        }
        prefs.edit().putString("last_day", today).apply()
    }
    val lastWeek = prefs.getString("last_week", week) ?: week
    if (lastWeek != week) {
        val missedWeekly = weekly.count { !prefs.getBoolean("done_${it.id}_$lastWeek", false) }
        if (missedWeekly > 0 && !prefs.getBoolean("weekly_penalty_applied_$lastWeek", false)) {
            val penalty = weekly.filter { !prefs.getBoolean("done_${it.id}_$lastWeek", false) }.sumOf { it.penalty }
            xp = (xp - penalty).coerceAtLeast(0); penalties += missedWeekly; streak = 0
            prefs.edit().putBoolean("weekly_penalty_applied_$lastWeek", true).apply(); save()
        }
        prefs.edit().putString("last_week", week).apply()
    }
    MaterialTheme(colorScheme = darkColorScheme(background = Void, surface = Panel, primary = Blue, secondary = Cyan, error = Red)) {
        Scaffold(containerColor = Void, bottomBar = {
            NavigationBar(containerColor = Panel, contentColor = Blue) {
                listOf("Система", "Квесты", "Рейды", "Статус").forEachIndexed { i, s ->
                    NavigationBarItem(selected = tab == i, onClick = { tab = i }, icon = { Icon(listOf(Icons.Default.Bolt, Icons.Default.CheckCircle, Icons.Default.Shield, Icons.Default.Person)[i], null) }, label = { Text(s, fontSize = 10.sp) })
                }
            }
        }) { padding ->
            LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(top = 18.dp, bottom = 24.dp)) {
                item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Column { Text("SOLO SYSTEM", color = Cyan, fontSize = 23.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp); Text("PLAYER DEVELOPMENT PROTOCOL", color = Muted, fontSize = 9.sp, letterSpacing = 2.sp) }; Text("RANK C", color = Blue, fontWeight = FontWeight.Bold) } }
                when (tab) {
                    0 -> {
                        item { PlayerCard(level, xp) }
                        item { SectionTitle("ЕЖЕДНЕВНЫЕ ПРОТОКОЛЫ") }
                        item { Text("Выполняй задания в комфортном и безопасном темпе. Пропуск не является поводом вредить себе.", color = Muted, fontSize = 12.sp) }
                        items(daily.size) { i -> QuestCard(daily[i], prefs.getBoolean("done_${daily[i].id}_$today", false)) { complete(daily[i]) } }
                        item { SectionTitle("НЕДЕЛЬНЫЙ РЕЙД") }
                        items(weekly.size) { i -> QuestCard(weekly[i], prefs.getBoolean("done_${weekly[i].id}_$week", false)) { complete(weekly[i]) } }
                    }
                    1 -> {
                        item { SectionTitle("ЖУРНАЛ ЗАДАНИЙ") }
                        item { Text("Ежедневные задания обновляются по календарному дню. Недельные — по номеру недели.", color = Muted) }
                        items(daily.size) { i -> QuestCard(daily[i], prefs.getBoolean("done_${daily[i].id}_$today", false)) { complete(daily[i]) } }
                        items(weekly.size) { i -> QuestCard(weekly[i], prefs.getBoolean("done_${weekly[i].id}_$week", false)) { complete(weekly[i]) } }
                    }
                    2 -> {
                        item { SectionTitle("ПОДЗЕМЕЛЬЯ") }
                        item { DungeonCard("ВОРОТА E-РАНГА", "Разминка • 15 минут движения", "НАГРАДА: 150 EXP", Blue) }
                        item { DungeonCard("ВОРОТА C-РАНГА", "Фокус • 3 последовательных рабочих блока", "НАГРАДА: 350 EXP", Cyan) }
                        item { DungeonCard("БОСС: СТРАЖ ПРИВЫЧЕК", "Недельное испытание регулярности", "НАГРАДА: 800 EXP", Red) }
                        item { Text("Рейды здесь — личные безопасные испытания, а не соревнование на износ.", color = Muted, fontSize = 12.sp) }
                    }
                    else -> {
                        item { PlayerCard(level, xp) }
                        item { SectionTitle("ХАРАКТЕРИСТИКИ") }
                        listOf("СИЛА" to strength, "ВЫНОСЛИВОСТЬ" to endurance, "ЛОВКОСТЬ" to agility, "ИНТЕЛЛЕКТ" to intelligence, "ДИСЦИПЛИНА" to discipline).forEach { (name, value) -> item { StatRow(name, value) } }
                        item { SectionTitle("СВОДКА") }
                        item { InfoRow("Пропущенные задания", penalties.toString()) }
                        item { InfoRow("Текущая серия выполнений", streak.toString()) }
                        item { InfoRow("Возраст персонажа", "20") }
                        item { InfoRow("Сложность наказаний", "S-RANK") }
                        item { Text("EXP — игровой показатель прогресса. Штрафы применяются только к игровому EXP и не влияют на здоровье, сон или реальные достижения.", color = Muted, fontSize = 12.sp) }
                    }
                }
                item { Text("SYSTEM • LOCAL SAVE ACTIVE", color = Muted, fontSize = 9.sp, letterSpacing = 2.sp, modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) }
            }
        }
    }
}

@Composable fun PlayerCard(level: Int, xp: Int) {
    val need = level * 500
    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Blue.copy(alpha = .45f))) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("PLAYER STATUS", color = Muted, letterSpacing = 2.sp, fontSize = 10.sp); Text("ACTIVE", color = Cyan, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            Row(verticalAlignment = Alignment.Bottom) { Text("LV. ", color = Muted, fontSize = 17.sp); Text(level.toString().padStart(2, '0'), color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Black); Spacer(Modifier.width(12.dp)); Text("HUNTER • C-RANK", color = Blue, fontSize = 11.sp, modifier = Modifier.padding(bottom = 9.dp)) }
            LinearProgressIndicator(progress = { (xp.toFloat() / need).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(7.dp), color = Blue, trackColor = Color(0xFF26364B))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("$xp EXP", color = Cyan, fontWeight = FontWeight.Bold); Text("$need EXP", color = Muted) }
        }
    }
}
@Composable fun SectionTitle(s: String) { Text(s, color = Cyan, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, modifier = Modifier.padding(top = 8.dp)) }
@Composable fun QuestCard(q: Quest, done: Boolean, onDone: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(14.dp), border = androidx.compose.foundation.BorderStroke(1.dp, if (done) Cyan.copy(alpha = .5f) else Color(0xFF24364B))) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(q.title, color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 1.sp); Text(if (done) "COMPLETE" else "+${q.reward} EXP", color = if (done) Cyan else Blue, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            Text(q.detail, color = Muted, fontSize = 12.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("ШТРАФ ПРИ ПРОПУСКЕ: −${q.penalty} EXP", color = Red, fontSize = 10.sp)
                Button(onClick = onDone, enabled = !done, colors = ButtonDefaults.buttonColors(containerColor = Blue, contentColor = Void), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 5.dp)) { Text(if (done) "ЗАЧТЕНО" else "ВЫПОЛНЕНО", fontSize = 10.sp) }
            }
        }
    }
}
@Composable fun DungeonCard(name: String, desc: String, reward: String, color: Color) { Card(colors = CardDefaults.cardColors(containerColor = Panel), border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = .5f))) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) { Text(name, color = color, fontWeight = FontWeight.Black, letterSpacing = 1.sp); Text(desc, color = Muted, fontSize = 12.sp); Text(reward, color = Cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold) } } }
@Composable fun StatRow(name: String, value: Int) { Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(12.dp)).padding(13.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold); Text(value.toString(), color = Cyan, fontWeight = FontWeight.Bold) }; LinearProgressIndicator(progress = { (value / 30f).coerceIn(0f,1f) }, modifier = Modifier.fillMaxWidth().height(5.dp), color = Blue, trackColor = Color(0xFF26364B)) } }
@Composable fun InfoRow(a: String, b: String) { Row(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(10.dp)).padding(13.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(a, color = Muted, fontSize = 12.sp); Text(b, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp) } }
