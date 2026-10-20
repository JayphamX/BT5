package com.example.coursemanager.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.coursemanager.data.Course
import com.example.coursemanager.isValidUrl
import java.time.LocalDate

/** Form dùng chung cho Thêm (courseId == null) và Sửa (courseId != null). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseFormScreen(vm: CourseViewModel, courseId: String?, onBack: () -> Unit) {
    val existing = remember(courseId) { courseId?.let { id -> vm.courses.firstOrNull { it.id == id } } }

    var title by remember { mutableStateOf(existing?.title ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var instructor by remember { mutableStateOf(existing?.instructor ?: "") }
    var categoryId by remember { mutableStateOf(existing?.categoryId ?: "") }
    var price by remember { mutableStateOf(existing?.price?.toString() ?: "") }
    var duration by remember { mutableStateOf(existing?.duration?.toString() ?: "") }
    var thumbnail by remember { mutableStateOf(existing?.thumbnail ?: "") }
    var trailerUrl by remember { mutableStateOf(existing?.trailerUrl ?: "") }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var menuOpen by remember { mutableStateOf(false) }

    // Luôn lấy danh mục mới nhất từ API cho Dropdown
    LaunchedEffect(Unit) { vm.refreshCategories() }

    fun validate(): Map<String, String> {
        val e = mutableMapOf<String, String>()
        if (title.isBlank()) e["title"] = "Vui lòng nhập tên khóa học"
        if (description.isBlank()) e["description"] = "Vui lòng nhập mô tả"
        if (instructor.isBlank()) e["instructor"] = "Vui lòng nhập tên giảng viên"
        if (vm.categories.none { it.id == categoryId }) e["category"] = "Vui lòng chọn danh mục hợp lệ"

        val p = price.trim().toLongOrNull()
        when {
            price.isBlank() -> e["price"] = "Vui lòng nhập học phí"
            p == null -> e["price"] = "Học phí phải là số nguyên"
            p < 0 -> e["price"] = "Học phí không được âm"
        }
        val d = duration.trim().toIntOrNull()
        when {
            duration.isBlank() -> e["duration"] = "Vui lòng nhập thời lượng"
            d == null -> e["duration"] = "Thời lượng phải là số nguyên"
            d <= 0 -> e["duration"] = "Thời lượng phải lớn hơn 0"
        }
        if (!isValidUrl(thumbnail.trim())) e["thumbnail"] = "URL ảnh không hợp lệ (bắt đầu bằng http/https)"
        if (!isValidUrl(trailerUrl.trim())) e["trailerUrl"] = "URL trailer không hợp lệ (bắt đầu bằng http/https)"
        return e
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (courseId == null) "Thêm khóa học" else "Sửa khóa học") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { pad ->
        if (courseId != null && existing == null) {
            Box(Modifier.padding(pad).fillMaxSize(), Alignment.Center) { Text("Không tìm thấy khóa học") }
        } else {
            Column(
                Modifier.padding(pad).padding(16.dp).fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FormField("Tên khóa học", title, { title = it }, errors["title"])
                FormField("Mô tả", description, { description = it }, errors["description"], singleLine = false)
                FormField("Giảng viên", instructor, { instructor = it }, errors["instructor"])

                // Dropdown danh mục lấy từ API /categories
                ExposedDropdownMenuBox(expanded = menuOpen, onExpandedChange = { menuOpen = it }) {
                    OutlinedTextField(
                        value = if (categoryId.isEmpty()) "" else vm.categoryName(categoryId),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Danh mục") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuOpen) },
                        isError = errors.containsKey("category"),
                        supportingText = { errors["category"]?.let { Text(it) } },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        if (vm.categories.isEmpty()) {
                            DropdownMenuItem(text = { Text("Chưa có danh mục") }, onClick = { menuOpen = false })
                        }
                        vm.categories.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c.name) },
                                onClick = {
                                    categoryId = c.id.orEmpty()
                                    menuOpen = false
                                }
                            )
                        }
                    }
                }

                FormField("Học phí (VNĐ)", price, { price = it }, errors["price"], KeyboardType.Number)
                FormField("Thời lượng (giờ)", duration, { duration = it }, errors["duration"], KeyboardType.Number)
                FormField("URL ảnh bìa", thumbnail, { thumbnail = it }, errors["thumbnail"], KeyboardType.Uri)

                // Xem trước ảnh từ URL vừa nhập
                if (isValidUrl(thumbnail.trim())) {
                    AsyncImage(
                        model = thumbnail.trim(),
                        contentDescription = "Xem trước ảnh bìa",
                        contentScale = ContentScale.Crop,
                        placeholder = ColorPainter(Color.LightGray),
                        error = ColorPainter(Color.LightGray),
                        modifier = Modifier.fillMaxWidth().height(180.dp)
                    )
                }

                FormField("URL trailer (MP4)", trailerUrl, { trailerUrl = it }, errors["trailerUrl"], KeyboardType.Uri)

                Button(
                    enabled = !vm.busy,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        errors = validate()
                        if (errors.isEmpty()) {
                            vm.saveCourse(
                                Course(
                                    id = existing?.id,
                                    title = title.trim(),
                                    description = description.trim(),
                                    categoryId = categoryId,
                                    instructor = instructor.trim(),
                                    price = price.trim().toLong(),
                                    duration = duration.trim().toInt(),
                                    thumbnail = thumbnail.trim(),
                                    trailerUrl = trailerUrl.trim(),
                                    createdAt = existing?.createdAt ?: LocalDate.now().toString()
                                ),
                                onSuccess = onBack
                            )
                        }
                    }
                ) { Text(if (vm.busy) "Đang lưu..." else "Lưu") }
            }
        }
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    error: String?,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = { error?.let { Text(it) } },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth()
    )
}
