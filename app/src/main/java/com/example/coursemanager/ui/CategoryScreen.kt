package com.example.coursemanager.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.coursemanager.data.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(vm: CourseViewModel, onBack: () -> Unit) {
    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Category?>(null) }   // null = thêm mới
    var deleting by remember { mutableStateOf<Category?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý danh mục") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { editing = null; showForm = true }) {
                Icon(Icons.Default.Add, contentDescription = "Thêm danh mục")
            }
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            LoadStateBox(state = vm.loadState, onRetry = { vm.loadAll() }) {
                if (vm.categories.isEmpty()) {
                    EmptyText("Chưa có danh mục nào.\nNhấn + để thêm danh mục.")
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(vm.categories, key = { it.id ?: it.name }) { c ->
                            val count = vm.courses.count { it.categoryId == c.id }
                            Card(Modifier.fillMaxWidth()) {
                                Row(Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp)) {
                                    Column(Modifier.weight(1f)) {
                                        Text(c.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text(c.description, style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            "$count khóa học",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(onClick = { editing = c; showForm = true }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Sửa")
                                    }
                                    IconButton(onClick = { deleting = c }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        CategoryFormDialog(
            initial = editing,
            busy = vm.busy,
            onSave = { name, desc ->
                vm.saveCategory(
                    Category(id = editing?.id, name = name, description = desc),
                    onSuccess = { showForm = false }
                )
            },
            onDismiss = { showForm = false }
        )
    }
    deleting?.let { c ->
        ConfirmDialog(
            title = "Xóa danh mục?",
            text = "Xóa danh mục \"${c.name}\"? (Không thể xóa nếu danh mục đang có khóa học.)",
            onConfirm = {
                deleting = null
                vm.deleteCategory(c)
            },
            onDismiss = { deleting = null }
        )
    }
}

@Composable
private fun CategoryFormDialog(
    initial: Category?,
    busy: Boolean,
    onSave: (name: String, description: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var desc by remember { mutableStateOf(initial?.description ?: "") }
    var nameError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Thêm danh mục" else "Sửa danh mục") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên danh mục") },
                    isError = nameError != null,
                    supportingText = { nameError?.let { Text(it) } },
                    singleLine = true
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Mô tả") }
                )
            }
        },
        confirmButton = {
            TextButton(enabled = !busy, onClick = {
                if (name.isBlank()) nameError = "Vui lòng nhập tên danh mục"
                else onSave(name.trim(), desc.trim())
            }) { Text("Lưu") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}
