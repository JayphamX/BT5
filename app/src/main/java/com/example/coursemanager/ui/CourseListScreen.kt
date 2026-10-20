package com.example.coursemanager.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.widget.Toast
import coil.compose.AsyncImage
import com.example.coursemanager.data.Course
import com.example.coursemanager.formatPrice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseListScreen(
    vm: CourseViewModel,
    onOpenCourse: (String) -> Unit,
    onAdd: () -> Unit,
    onCategories: () -> Unit
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Khóa học") },
                actions = {
                    IconButton(onClick = { vm.loadAll() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Tải lại")
                    }
                    TextButton(onClick = onCategories) { Text("Danh mục") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "Thêm khóa học")
            }
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            OutlinedTextField(
                value = vm.query,
                onValueChange = { vm.query = it },
                placeholder = { Text("Tìm theo tên khóa học") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (vm.query.isNotEmpty()) {
                        IconButton(onClick = { vm.query = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Xóa tìm kiếm")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = vm.filterCategoryId == null,
                        onClick = { vm.filterCategoryId = null },
                        label = { Text("Tất cả") }
                    )
                }
                items(vm.categories, key = { it.id ?: it.name }) { c ->
                    FilterChip(
                        selected = vm.filterCategoryId == c.id,
                        onClick = { vm.filterCategoryId = c.id },
                        label = { Text(c.name) }
                    )
                }
            }

            LoadStateBox(state = vm.loadState, onRetry = { vm.loadAll() }) {
                val list = vm.filteredCourses
                if (list.isEmpty()) {
                    EmptyText(
                        if (vm.courses.isEmpty()) "Chưa có khóa học nào.\nNhấn + để thêm khóa học."
                        else "Không tìm thấy khóa học phù hợp."
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(list, key = { it.id ?: it.title }) { course ->
                            CourseCard(
                                course = course,
                                categoryName = vm.categoryName(course.categoryId),
                                onClick = {
                                    val id = course.id
                                    if (id == null) {
                                        Toast.makeText(context, "Khóa học này không có id từ API", Toast.LENGTH_SHORT).show()
                                    } else {
                                        onOpenCourse(id)
                                    }
                                }
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
private fun CourseCard(course: Course, categoryName: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AsyncImage(
                model = course.thumbnail,
                contentDescription = course.title,
                contentScale = ContentScale.Crop,
                placeholder = ColorPainter(Color.LightGray),
                error = ColorPainter(Color.LightGray),
                modifier = Modifier.size(96.dp).clip(RoundedCornerShape(8.dp))
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    course.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text("GV: ${course.instructor}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    categoryName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    formatPrice(course.price),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}
