// MainActivity.kt
package com.example.alerenlifetrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

// Yardımcı Fonksiyonlar - EN BAŞTA OLMALI
fun getCurrentDate(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date())
}

fun addDays(dateStr: String, days: Int): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar.time = sdf.parse(dateStr) ?: Date()
    calendar.add(Calendar.DAY_OF_MONTH, days)
    return sdf.format(calendar.time)
}

fun formatDate(dateStr: String): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("tr"))
    val date = sdf.parse(dateStr) ?: Date()
    return outputFormat.format(date)
}

fun getMonthName(month: Int): String {
    val months = listOf(
        "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
        "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"
    )
    return months[month]
}

// Data Classes
data class Exercise(
    val name: String,
    val kg: String,
    val set: String,
    val tekrar: String,
    val bolge: String
)

data class Nutrition(
    val kalori: String = "",
    val karb: String = "",
    val yag: String = "",
    val protein: String = ""
)

data class StudySession(
    val ders: String,
    val slayt: String,
    val saat: String
)

data class DayData(
    val exercises: List<Exercise> = emptyList(),
    val nutrition: Nutrition = Nutrition(),
    val water: String = "",
    val cigarette: String = "",
    val study: List<StudySession> = emptyList()
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PersonalTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun PersonalTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFFEC4899),
            secondary = Color(0xFFF472B6),
            background = Color(0xFFFDF2F8)
        ),
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    var selectedDate by remember { mutableStateOf(getCurrentDate()) }
    var showCalendar by remember { mutableStateOf(false) }
    var allData by remember { mutableStateOf(mutableMapOf<String, DayData>()) }

    val tabs = listOf("Spor", "Beslenme", "Su & Sigara", "Ders")
    val icons = listOf(
        Icons.Default.FitnessCenter,
        Icons.Default.Restaurant,
        Icons.Default.LocalDrink,
        Icons.Default.Book
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Aleren'e Özel 💪",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFEC4899),
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White
            ) {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = title) },
                        label = { Text(title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFEC4899),
                            selectedTextColor = Color(0xFFEC4899),
                            indicatorColor = Color(0xFFFCE7F3)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFFDF2F8))
        ) {
            // Tarih Seçici
            DateSelector(
                selectedDate = selectedDate,
                onDateChange = { selectedDate = it },
                onCalendarClick = { showCalendar = true }
            )

            // İçerik
            when (selectedTab) {
                0 -> ExerciseTab(selectedDate, allData) { allData = it }
                1 -> NutritionTab(selectedDate, allData) { allData = it }
                2 -> WaterCigaretteTab(selectedDate, allData) { allData = it }
                3 -> StudyTab(selectedDate, allData) { allData = it }
            }
        }

        if (showCalendar) {
            CalendarDialog(
                selectedDate = selectedDate,
                onDateSelected = {
                    selectedDate = it
                    showCalendar = false
                },
                onDismiss = { showCalendar = false },
                dataMap = allData
            )
        }
    }
}

@Composable
fun DateSelector(
    selectedDate: String,
    onDateChange: (String) -> Unit,
    onCalendarClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onDateChange(addDays(selectedDate, -1)) }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Önceki Gün")
            }

            Text(
                text = formatDate(selectedDate),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.clickable { onCalendarClick() }
            )

            IconButton(onClick = { onDateChange(addDays(selectedDate, 1)) }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Sonraki Gün")
            }
        }
    }
}

@Composable
fun ExerciseTab(
    selectedDate: String,
    allData: Map<String, DayData>,
    onDataChange: (MutableMap<String, DayData>) -> Unit
) {
    var exerciseName by remember { mutableStateOf("") }
    var kg by remember { mutableStateOf("") }
    var set by remember { mutableStateOf("") }
    var tekrar by remember { mutableStateOf("") }
    var bolge by remember { mutableStateOf("") }
    var editingIndex by remember { mutableStateOf<Int?>(null) }

    val dayData = allData[selectedDate] ?: DayData()
    val exercises = dayData.exercises

    LaunchedEffect(editingIndex) {
        if (editingIndex != null && editingIndex!! < exercises.size) {
            val ex = exercises[editingIndex!!]
            exerciseName = ex.name
            kg = ex.kg
            set = ex.set
            tekrar = ex.tekrar
            bolge = ex.bolge
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (editingIndex != null) "Hareketi Düzenle" else "Yeni Hareket Ekle",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFFEC4899),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = exerciseName,
                        onValueChange = { exerciseName = it },
                        label = { Text("Hareket Adı") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = kg,
                            onValueChange = { kg = it },
                            label = { Text("Kg") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = set,
                            onValueChange = { set = it },
                            label = { Text("Set") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = tekrar,
                        onValueChange = { tekrar = it },
                        label = { Text("Tekrar") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = bolge,
                        onValueChange = { bolge = it },
                        label = { Text("Çalıştırdığı Bölge") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (exerciseName.isNotEmpty() && kg.isNotEmpty() &&
                                    set.isNotEmpty() && tekrar.isNotEmpty() && bolge.isNotEmpty()) {
                                    val newExercise = Exercise(exerciseName, kg, set, tekrar, bolge)
                                    val newExercises = if (editingIndex != null) {
                                        exercises.toMutableList().apply {
                                            set(editingIndex!!, newExercise)
                                        }
                                    } else {
                                        exercises + newExercise
                                    }

                                    val newData = allData.toMutableMap()
                                    newData[selectedDate] = dayData.copy(exercises = newExercises)
                                    onDataChange(newData)

                                    exerciseName = ""
                                    kg = ""
                                    set = ""
                                    tekrar = ""
                                    bolge = ""
                                    editingIndex = null
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEC4899)
                            )
                        ) {
                            Text(if (editingIndex != null) "Güncelle" else "Ekle")
                        }

                        if (editingIndex != null) {
                            Button(
                                onClick = {
                                    exerciseName = ""
                                    kg = ""
                                    set = ""
                                    tekrar = ""
                                    bolge = ""
                                    editingIndex = null
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Gray
                                )
                            ) {
                                Text("İptal")
                            }
                        }
                    }
                }
            }
        }

        itemsIndexed(exercises) { index, exercise ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = exercise.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "${exercise.kg} kg • ${exercise.set} set • ${exercise.tekrar} tekrar",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Bölge: ${exercise.bolge}",
                            fontSize = 14.sp,
                            color = Color(0xFFEC4899)
                        )
                    }
                    Row {
                        IconButton(onClick = { editingIndex = index }) {
                            Icon(Icons.Default.Edit, contentDescription = "Düzenle")
                        }
                        IconButton(onClick = {
                            val newExercises = exercises.filterIndexed { i, _ -> i != index }
                            val newData = allData.toMutableMap()
                            newData[selectedDate] = dayData.copy(exercises = newExercises)
                            onDataChange(newData)
                        }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Sil",
                                tint = Color.Red
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NutritionTab(
    selectedDate: String,
    allData: Map<String, DayData>,
    onDataChange: (MutableMap<String, DayData>) -> Unit
) {
    val dayData = allData[selectedDate] ?: DayData()
    var kalori by remember(selectedDate) { mutableStateOf(dayData.nutrition.kalori) }
    var karb by remember(selectedDate) { mutableStateOf(dayData.nutrition.karb) }
    var yag by remember(selectedDate) { mutableStateOf(dayData.nutrition.yag) }
    var protein by remember(selectedDate) { mutableStateOf(dayData.nutrition.protein) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Günlük Beslenme",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFFEC4899),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = kalori,
                        onValueChange = { kalori = it },
                        label = { Text("Toplam Kalori") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = karb,
                        onValueChange = { karb = it },
                        label = { Text("Karbonhidrat (gr)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = yag,
                        onValueChange = { yag = it },
                        label = { Text("Yağ (gr)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = protein,
                        onValueChange = { protein = it },
                        label = { Text("Protein (gr)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val newData = allData.toMutableMap()
                            newData[selectedDate] = dayData.copy(
                                nutrition = Nutrition(kalori, karb, yag, protein)
                            )
                            onDataChange(newData)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEC4899)
                        )
                    ) {
                        Text("Kaydet")
                    }
                }
            }
        }
    }
}

@Composable
fun WaterCigaretteTab(
    selectedDate: String,
    allData: Map<String, DayData>,
    onDataChange: (MutableMap<String, DayData>) -> Unit
) {
    val dayData = allData[selectedDate] ?: DayData()
    var water by remember(selectedDate) { mutableStateOf(dayData.water) }
    var cigarette by remember(selectedDate) { mutableStateOf(dayData.cigarette) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💧 Su Tüketimi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFFEC4899),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = water,
                        onValueChange = { water = it },
                        label = { Text("Su (litre)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val newData = allData.toMutableMap()
                            newData[selectedDate] = dayData.copy(water = water)
                            onDataChange(newData)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEC4899)
                        )
                    ) {
                        Text("Kaydet")
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🚬 Sigara",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFFEC4899),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = cigarette,
                        onValueChange = { cigarette = it },
                        label = { Text("Sigara Sayısı") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val newData = allData.toMutableMap()
                            newData[selectedDate] = dayData.copy(cigarette = cigarette)
                            onDataChange(newData)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEC4899)
                        )
                    ) {
                        Text("Kaydet")
                    }
                }
            }
        }
    }
}

@Composable
fun StudyTab(
    selectedDate: String,
    allData: Map<String, DayData>,
    onDataChange: (MutableMap<String, DayData>) -> Unit
) {
    var ders by remember { mutableStateOf("") }
    var slayt by remember { mutableStateOf("") }
    var saat by remember { mutableStateOf("") }
    var editingIndex by remember { mutableStateOf<Int?>(null) }

    val dayData = allData[selectedDate] ?: DayData()
    val studySessions = dayData.study

    LaunchedEffect(editingIndex) {
        if (editingIndex != null && editingIndex!! < studySessions.size) {
            val session = studySessions[editingIndex!!]
            ders = session.ders
            slayt = session.slayt
            saat = session.saat
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (editingIndex != null) "Ders Seansını Düzenle" else "Yeni Ders Seansı",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFFEC4899),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = ders,
                        onValueChange = { ders = it },
                        label = { Text("Ders Adı") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = slayt,
                        onValueChange = { slayt = it },
                        label = { Text("Çalışılan Slayt Sayısı") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = saat,
                        onValueChange = { saat = it },
                        label = { Text("Çalışma Süresi (saat)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (ders.isNotEmpty() && slayt.isNotEmpty() && saat.isNotEmpty()) {
                                    val newSession = StudySession(ders, slayt, saat)
                                    val newSessions = if (editingIndex != null) {
                                        studySessions.toMutableList().apply {
                                            set(editingIndex!!, newSession)
                                        }
                                    } else {
                                        studySessions + newSession
                                    }

                                    val newData = allData.toMutableMap()
                                    newData[selectedDate] = dayData.copy(study = newSessions)
                                    onDataChange(newData)

                                    ders = ""
                                    slayt = ""
                                    saat = ""
                                    editingIndex = null
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEC4899)
                            )
                        ) {
                            Text(if (editingIndex != null) "Güncelle" else "Ekle")
                        }

                        if (editingIndex != null) {
                            Button(
                                onClick = {
                                    ders = ""
                                    slayt = ""
                                    saat = ""
                                    editingIndex = null
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Gray
                                )
                            ) {
                                Text("İptal")
                            }
                        }
                    }
                }
            }
        }

        itemsIndexed(studySessions) { index, session ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = session.ders,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "${session.slayt} slayt • ${session.saat} saat",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                    Row {
                        IconButton(onClick = { editingIndex = index }) {
                            Icon(Icons.Default.Edit, contentDescription = "Düzenle")
                        }
                        IconButton(onClick = {
                            val newSessions = studySessions.filterIndexed { i, _ -> i != index }
                            val newData = allData.toMutableMap()
                            newData[selectedDate] = dayData.copy(study = newSessions)
                            onDataChange(newData)
                        }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Sil",
                                tint = Color.Red
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarDialog(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    dataMap: Map<String, DayData>
) {
    val calendar = Calendar.getInstance()
    calendar.time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDate)!!

    var currentMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var currentYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (currentMonth == 0) {
                        currentMonth = 11
                        currentYear--
                    } else {
                        currentMonth--
                    }
                }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Önceki Ay")
                }
                Text(
                    text = "${getMonthName(currentMonth)} $currentYear",
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = {
                    if (currentMonth == 11) {
                        currentMonth = 0
                        currentYear++
                    } else {
                        currentMonth++
                    }
                }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Sonraki Ay")
                }
            }
        },
        text = {
            CalendarGrid(
                month = currentMonth,
                year = currentYear,
                selectedDate = selectedDate,
                onDateSelected = onDateSelected,
                dataMap = dataMap
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat")
            }
        }
    )
}

@Composable
fun CalendarGrid(
    month: Int,
    year: Int,
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    dataMap: Map<String, DayData>
) {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, 1)
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Paz", "Pzt", "Sal", "Çar", "Per", "Cum", "Cmt").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        var dayCounter = 1
        for (week in 0..5) {
            if (dayCounter > daysInMonth) break

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (day in 0..6) {
                    if ((week == 0 && day < firstDayOfWeek) || dayCounter > daysInMonth) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        val dateStr = String.format("%04d-%02d-%02d", year, month + 1, dayCounter)
                        val hasData = dataMap[dateStr]?.let { data ->
                            data.exercises.isNotEmpty() || data.study.isNotEmpty() ||
                                    data.water.isNotEmpty() || data.cigarette.isNotEmpty()
                        } ?: false

                        val isSelected = dateStr == selectedDate

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .background(
                                    color = when {
                                        isSelected -> Color(0xFFEC4899)
                                        hasData -> Color(0xFFFCE7F3)
                                        else -> Color.Transparent
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onDateSelected(dateStr) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayCounter.toString(),
                                color = if (isSelected) Color.White else Color.Black,
                                fontSize = 14.sp
                            )
                        }
                        dayCounter++
                    }
                }
            }
        }
    }
}

