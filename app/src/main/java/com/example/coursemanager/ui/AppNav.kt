package com.example.coursemanager.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun AppNav(vm: CourseViewModel = viewModel()) {
    val nav = rememberNavController()
    val snackbar = remember { SnackbarHostState() }

    // Hiển thị mọi thông báo (thành công / lỗi) từ ViewModel bằng Snackbar
    LaunchedEffect(vm.message) {
        vm.message?.let {
            snackbar.showSnackbar(it)
            vm.consumeMessage()
        }
    }

    Box {
        NavHost(navController = nav, startDestination = "list") {
            composable("list") {
                CourseListScreen(
                    vm = vm,
                    onOpenCourse = { nav.navigate("detail/$it") },
                    onAdd = { nav.navigate("form") },
                    onCategories = { nav.navigate("categories") }
                )
            }
            composable("detail/{id}") { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                CourseDetailScreen(
                    vm = vm,
                    courseId = id,
                    onBack = { nav.popBackStack() },
                    onEdit = { nav.navigate("form?id=$id") }
                )
            }
            composable(
                route = "form?id={id}",
                arguments = listOf(navArgument("id") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { entry ->
                CourseFormScreen(
                    vm = vm,
                    courseId = entry.arguments?.getString("id"),
                    onBack = { nav.popBackStack() }
                )
            }
            composable("categories") {
                CategoryScreen(vm = vm, onBack = { nav.popBackStack() })
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }
}
