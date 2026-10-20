# Ứng dụng Quản lý khóa học (Android – Kotlin + Jetpack Compose)

Backend: **MockAPI.io** (REST). Kiến trúc: **MVVM** (1 `CourseViewModel`), **Retrofit + Gson**, **Coil** (tải ảnh), **Media3 ExoPlayer** (phát trailer), **Navigation Compose**.

## 1. Yêu cầu
- Android Studio Koala (2024.1) trở lên, JDK 17 (đi kèm Android Studio)
- Android 8.0+ (minSdk 26), máy ảo hoặc điện thoại thật có Internet
- Python 3 (chỉ để chạy script nạp dữ liệu mẫu, không bắt buộc)

## 2. Tạo backend MockAPI
1. Đăng nhập https://mockapi.io/ → **New Project** (đặt tên bất kỳ) → ghi lại **API endpoint**, dạng  
   `https://<PROJECT_ID>.mockapi.io/api/v1`
2. Trong project, tạo **2 resource**:

   **`categories`**
   | Field | Type |
   |---|---|
   | name | String |
   | description | String |

   **`courses`**
   | Field | Type |
   |---|---|
   | title | String |
   | description | String |
   | categoryId | String |
   | instructor | String |
   | price | Number |
   | duration | Number |
   | thumbnail | String |
   | trailerUrl | String |
   | createdAt | String |

   (`id` do MockAPI tự sinh. Khi tạo resource, đặt số bản ghi tự sinh = 0, hoặc xóa dữ liệu fake có sẵn trước khi nạp dữ liệu mẫu.)
3. Nạp dữ liệu mẫu (3 danh mục + 10 khóa học):
   ```bash
   cd seed
   python3 seed.py https://<PROJECT_ID>.mockapi.io/api/v1
   ```
   Script đọc `seed/categories.json`, `seed/courses.json`, tạo danh mục trước rồi ánh xạ `categoryId` sang id thật MockAPI trả về.  
   Trailer dùng MP4 công khai của Google sample, ảnh bìa dùng `picsum.photos` nên phát/hiển thị được ngay.

## 3. Cấu hình và chạy app
1. Mở thư mục `CourseManager` bằng Android Studio, đợi Gradle Sync xong  
   (nếu thiếu `gradle-wrapper.jar`, Android Studio sẽ tự tải Gradle 8.7).
2. Mở `app/src/main/java/com/example/coursemanager/data/ApiService.kt`, sửa:
   ```kotlin
   const val BASE_URL = "https://<PROJECT_ID>.mockapi.io/api/v1/"   // phải có "/" ở cuối
   ```
3. Chọn thiết bị → **Run ▶**.

## 4. Cấu trúc mã nguồn
```
app/src/main/java/com/example/coursemanager/
├── MainActivity.kt          # điểm vào, bọc MaterialTheme
├── Util.kt                  # định dạng giá, kiểm tra URL / YouTube
├── data/
│   ├── Models.kt            # Category, Course (khớp JSON MockAPI)
│   └── ApiService.kt        # BASE_URL + interface Retrofit 9 endpoint + ApiClient
└── ui/
    ├── CourseViewModel.kt   # state, gọi API, CRUD, tìm kiếm/lọc, ràng buộc xóa danh mục
    ├── AppNav.kt            # Navigation + Snackbar thông báo
    ├── CourseListScreen.kt  # (1) danh sách, tìm kiếm, lọc
    ├── CourseDetailScreen.kt# (2) chi tiết, xem trailer, sửa, xóa
    ├── CourseFormScreen.kt  # (3) thêm/sửa + validate + xem trước ảnh
    ├── CategoryScreen.kt    # (4) quản lý danh mục
    ├── TrailerPlayer.kt     # ExoPlayer trong Dialog
    └── Components.kt        # loading/lỗi/rỗng, hộp thoại xác nhận
```

## 5. Đối chiếu yêu cầu đề bài
| Yêu cầu | Cách thực hiện |
|---|---|
| Danh sách: ảnh, tên, GV, danh mục, học phí | `CourseListScreen` – `AsyncImage` (Coil) + `vm.categoryName()` lấy từ API |
| Tìm kiếm theo tên, lọc theo danh mục | `vm.filteredCourses` (ô tìm kiếm + `FilterChip` từ `/categories`) |
| Chi tiết + tên danh mục theo `categoryId` | `CourseDetailScreen`, gọi `GET /courses/{id}` khi mở |
| Xem trailer | MP4 → ExoPlayer trong dialog; link YouTube → mở app/trình duyệt |
| Thêm khóa học | `CourseFormScreen`: Dropdown từ `GET /categories`, xem trước ảnh, `POST /courses`, tải lại danh sách |
| Sửa / xóa | `PUT /courses/{id}`; `DELETE` có hộp thoại xác nhận; luôn `GET /courses` lại sau thao tác |
| Quản lý danh mục | `CategoryScreen`: thêm/sửa/xóa; **chặn xóa** nếu còn khóa học (tải lại `/courses` rồi đếm theo `categoryId`) |
| Loading / lỗi mạng / rỗng | `LoadStateBox`, `EmptyText`, nút **Thử lại**, Snackbar báo lỗi `IOException`/`HttpException` |
| Validate | Trường bắt buộc, học phí số ≥ 0, thời lượng > 0, URL http/https, `categoryId` phải tồn tại trong danh sách API |

## 6. Gợi ý quay video thuyết trình
1. Mở MockAPI: 2 resource, dữ liệu 3 danh mục / 10 khóa học.
2. Mở `ApiService.kt`: giải thích 9 endpoint và `BASE_URL`.
3. Mở `CourseViewModel.kt`: `loadAll()` (2 request song song bằng `async`), `Gson` parse JSON → `List<Course>`, `categoryName()` nối theo `categoryId`.
4. Demo: danh sách → tìm kiếm/lọc → chi tiết + trailer → thêm (validate lỗi rồi thành công) → sửa → xóa (xác nhận) → quản lý danh mục (thử xóa danh mục đang có khóa học) → tắt mạng để thấy màn hình lỗi.

## 7. Lưu ý
- `usesCleartextTraffic="true"` để chấp nhận URL ảnh/video dạng `http://`.
- MockAPI gói miễn phí giới hạn số resource/bản ghi; 2 resource + 13 bản ghi là đủ.
