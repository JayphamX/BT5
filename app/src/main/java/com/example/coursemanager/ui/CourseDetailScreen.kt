package com.example.coursemanager.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.coursemanager.formatPrice
import com.example.coursemanager.isYoutube

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    vm: CourseViewModel,
    courseId: String,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val context = LocalContext.current
    val course = vm.courses.firstOrNull { it.id == courseId }
    var showTrailer by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    // GET /courses/{id} để lấy dữ liệu mới nhất của khóa học này
    LaunchedEffect(courseId) { vm.refreshCourse(courseId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết khóa học") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { pad ->
        if (course == null) {
            Box(Modifier.padding(pad).fillMaxSize(), Alignment.Center) {
                Text("Không tìm thấy khóa học")
            }
        } else {
            Column(
                Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AsyncImage(
                    model = course.thumbnail,
                    contentDescription = course.title,
                    contentScale = ContentScale.Crop,
                    placeholder = ColorPainter(Color.LightGray),
                    error = ColorPainter(Color.LightGray),
                    modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)
                )
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(course.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        vm.categoryName(course.categoryId),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    InfoRow("Giảng viên", course.instructor)
                    InfoRow("Học phí", formatPrice(course.price))
                    InfoRow("Thời lượng", "${course.duration} giờ")
                    InfoRow("Ngày tạo", course.createdAt)
                    Text(course.description, style = MaterialTheme.typography.bodyLarge)

                    Button(
                        onClick = {
                            if (isYoutube(course.trailerUrl)) {
                                // YouTube không phát trực tiếp bằng ExoPlayer -> mở app/trình duyệt
                                runCatching {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(course.trailerUrl))
                                    )
                                }
                            } else {
                                showTrailer = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Text("Xem trailer")
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) { Text("Sửa") }
                        Button(
                            onClick = { showDelete = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f)
                        ) { Text("Xóa") }
                    }
                }
            }
        }
    }

    if (showTrailer && course != null) {
        TrailerDialog(url = course.trailerUrl, onDismiss = { showTrailer = false })
    }
    if (showDelete && course != null) {
        ConfirmDialog(
            title = "Xóa khóa học?",
            text = "Bạn có chắc muốn xóa \"${course.title}\"? Hành động này không thể hoàn tác.",
            onConfirm = {
                showDelete = false
                course.id?.let { vm.deleteCourse(it, onSuccess = onBack) }
            },
            onDismiss = { showDelete = false }
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row {
        Text("$label: ", fontWeight = FontWeight.SemiBold)
        Text(value)
    }
}
