# SplitBill - Ứng Dụng Quản Lý & Chia Tiền Nhóm Thông Minh (Android - Kotlin)

Ứng dụng **SplitBill** giúp quản lý chi tiêu nhóm, tự động chia đều tiền và giải quyết công nợ tối ưu (Min Cash Flow) theo kiến trúc **MVVM**, sử dụng **Room Database** lưu trữ offline và giao diện hiện đại bằng **Jetpack Compose & Material Design 3**.

---

## 📱 Các Màn hình & Tính năng Chính

1. **Màn hình Danh sách & Tạo Nhóm (Group List)**:
   - Hiển thị danh sách nhóm trực quan: tên nhóm, mô tả, số lượng thành viên, tổng chi tiêu.
   - Thao tác tạo nhóm mới nhanh chóng với màu sắc đại diện tùy chọn.
   - Hỗ trợ xóa nhóm kèm dialog xác nhận an toàn.
   - Lưu trữ dữ liệu cục bộ an toàn với Room Database.

2. **Màn hình Chi tiết Nhóm (Group Detail)**:
   - Quản lý danh sách thành viên trong nhóm (thêm, xóa, xem thông tin).
   - Danh sách chi tiêu chi tiết (tiêu đề, ai đã trả, số tiền, chia cho bao nhiêu người, ngày giờ).
   - Thẻ thống kê tổng chi tiêu nhóm và nút truy cập nhanh thống kê số dư.

3. **Màn hình Thêm Khoản Chi Tiêu (Add Expense)**:
   - Nhập tiêu đề và số tiền bằng VNĐ (tự động định dạng tiền tệ đẹp mắt).
   - Chọn thành viên đã thanh toán từ danh sách chip sinh động.
   - Tự động tính toán chia đều chi phí theo số lượng người tham gia thời gian thực.
   - Cho phép chọn tất cả hoặc tick chọn từng thành viên tham gia khoản chi đó.

4. **Màn hình Thống kê Số dư & Quyết toán (Settlement)**:
   - Thống kê **Số dư ròng (Net Balance)** của từng thành viên:
     - Badge xanh lá: Được nhận lại tiền (`+... ₫`).
     - Badge đỏ: Cần chuyển trả tiền (`-... ₫`).
     - Badge xám: Đã cân bằng (`0 ₫`).
   - Thuật toán **Tối ưu hóa Quyết toán (Greedy Min Cash Flow)**:
     - Giảm tối đa số lần chuyển khoản giữa các thành viên.
     - Hiển thị rõ ràng: `[Người nợ] -> [Người nhận]: [Số tiền]`.

5. **Tính năng Nhắc Nợ Nhanh (Quick Debt Reminder)**:
   - Nút **"Nhắc nợ"** bên cạnh từng khoản nợ.
   - BottomSheet hiển thị nội dung tin nhắn soạn sẵn lịch sự, hóm hỉnh.
   - Tích hợp Android Intent:
     - Gửi nhanh qua **Zalo** (Package `com.zing.zalo`).
     - Gửi nhanh qua **Messenger** (Package `com.facebook.orca`).
     - Gửi qua ứng dụng bất kỳ (SMS, Telegram, Viber...) bằng Chooser hệ thống.
     - Sao chép tin nhắn vào Clipboard với 1 chạm.

---

## 🏗️ Kiến trúc & Công nghệ

- **Ngôn ngữ**: Kotlin (100%)
- **Kiến trúc**: MVVM (Model - View - ViewModel) + Clean Architecture
- **Giao diện**: Jetpack Compose + Material Design 3
- **Cơ sở dữ liệu**: Room Database (SQLite offline)
- **Xử lý bất đồng bộ**: Kotlin Coroutines & StateFlow
- **Điều hướng**: Jetpack Navigation Compose
- **Target SDK**: Android 15 (API 35), Min SDK: Android 8.0 (API 26)

---

## 🚀 Hướng dẫn Chạy Dự Án trong Android Studio

1. Mở **Android Studio** (Hedgehog, Iguana, Koala hoặc mới hơn).
2. Chọn **File -> Open...** và dẫn tới thư mục dự án này.
3. Chờ Android Studio đồng bộ Gradle (Gradle Sync).
4. Chọn máy ảo hoặc thiết bị thật (Android 8.0+) và nhấn nút **Run (Shift + F10)**.
