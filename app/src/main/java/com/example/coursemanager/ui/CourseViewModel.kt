package com.example.coursemanager.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursemanager.data.ApiClient
import com.example.coursemanager.data.Category
import com.example.coursemanager.data.Course
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed interface LoadState {
    data object Loading : LoadState
    data object Success : LoadState
    data class Error(val message: String) : LoadState
}

/** Một ViewModel dùng chung cho toàn app (phạm vi Activity). */
class CourseViewModel : ViewModel() {

    // Lấy service trong try/catch để URL sai không làm app crash lúc khởi tạo
    private val api get() = ApiClient.service

    var categories by mutableStateOf<List<Category>>(emptyList()); private set
    var courses by mutableStateOf<List<Course>>(emptyList()); private set
    var loadState by mutableStateOf<LoadState>(LoadState.Loading); private set
    var busy by mutableStateOf(false); private set          // đang gọi POST/PUT/DELETE
    var message by mutableStateOf<String?>(null); private set // thông báo Snackbar

    // Tìm kiếm + lọc (xử lý phía client trên dữ liệu lấy từ API)
    var query by mutableStateOf("")
    var filterCategoryId by mutableStateOf<String?>(null)

    val filteredCourses: List<Course>
        get() = courses.filter {
            (filterCategoryId == null || it.categoryId == filterCategoryId) &&
                it.title.contains(query.trim(), ignoreCase = true)
        }

    init {
        loadAll()
    }

    fun consumeMessage() {
        message = null
    }

    /** Tên danh mục lấy từ API (không hardcode). */
    fun categoryName(id: String): String =
        categories.firstOrNull { it.id == id }?.name ?: "Không xác định"

    // ---------------- Tải dữ liệu ----------------

    fun loadAll() {
        viewModelScope.launch {
            loadState = LoadState.Loading
            loadState = try {
                coroutineScope {
                    val cats = async { api.getCategories() }
                    val crs = async { api.getCourses() }
                    categories = cats.await()
                    courses = crs.await()
                }
                LoadState.Success
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                LoadState.Error(errorText(e))
            }
        }
    }

    /** Làm mới danh sách danh mục (dùng khi mở form), im lặng nếu lỗi. */
    fun refreshCategories() {
        viewModelScope.launch {
            try {
                categories = api.getCategories()
            } catch (e: CancellationException) {
                throw e
            } catch (ignored: Exception) {
            }
        }
    }

    /** GET /courses/{id} rồi cập nhật vào danh sách. */
    fun refreshCourse(id: String) {
        viewModelScope.launch {
            try {
                val fresh = api.getCourse(id)
                courses = courses.map { if (it.id == id) fresh else it }
            } catch (e: CancellationException) {
                throw e
            } catch (ignored: Exception) {
            }
        }
    }

    // ---------------- Khóa học ----------------

    fun saveCourse(course: Course, onSuccess: () -> Unit) = launchBusy {
        val isNew = course.id == null
        if (course.id == null) api.createCourse(course) else api.updateCourse(course.id, course)
        courses = api.getCourses() // đồng bộ lại với backend
        message = if (isNew) "Đã thêm khóa học" else "Đã cập nhật khóa học"
        onSuccess()
    }

    fun deleteCourse(id: String, onSuccess: () -> Unit) = launchBusy {
        api.deleteCourse(id)
        courses = api.getCourses()
        message = "Đã xóa khóa học"
        onSuccess()
    }

    // ---------------- Danh mục ----------------

    fun saveCategory(category: Category, onSuccess: () -> Unit) = launchBusy {
        val isNew = category.id == null
        if (category.id == null) api.createCategory(category) else api.updateCategory(category.id, category)
        categories = api.getCategories()
        message = if (isNew) "Đã thêm danh mục" else "Đã cập nhật danh mục"
        onSuccess()
    }

    /** Không cho xóa danh mục đang có khóa học (kiểm tra phía app trên dữ liệu mới nhất). */
    fun deleteCategory(category: Category) = launchBusy {
        val id = category.id ?: return@launchBusy
        val latestCourses = api.getCourses()
        courses = latestCourses
        val count = latestCourses.count { it.categoryId == id }
        if (count > 0) {
            message = "Không thể xóa: danh mục \"${category.name}\" đang có $count khóa học"
            return@launchBusy
        }
        api.deleteCategory(id)
        categories = api.getCategories()
        if (filterCategoryId == id) filterCategoryId = null
        message = "Đã xóa danh mục"
    }

    // ---------------- Tiện ích ----------------

    private fun launchBusy(block: suspend () -> Unit) {
        viewModelScope.launch {
            busy = true
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                message = errorText(e)
            } finally {
                busy = false
            }
        }
    }

    private fun errorText(e: Exception): String = when (e) {
        is IOException -> "Lỗi kết nối mạng. Kiểm tra Internet rồi thử lại."
        is HttpException -> "Lỗi máy chủ (HTTP ${e.code()})."
        else -> "Có lỗi xảy ra: ${e.message}"
    }
}
