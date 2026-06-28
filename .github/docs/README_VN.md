<div align="center">

<img src="../../app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" width="96" alt="WebToApp icon" />

# WebToApp

### Xây dựng APK Android từ các dự án web, trực tiếp trên điện thoại của bạn.

**WebToApp là một trình tạo APK trên thiết bị cho các trang web, ứng dụng HTML, dự án đa phương tiện và runtime máy chủ cục bộ.**
Biến một URL, một thư mục dự án hoặc thư viện phương tiện thành một ứng dụng Android có thể cài đặt mà bạn có thể xem trước, ký, cài đặt, chia sẻ hoặc xuất mà không cần gửi bản dựng đến một dịch vụ từ xa.

[English](../../README.md) · [简体中文](README_CN.md) · **Tiếng Việt**

[![Stars](https://img.shields.io/github/stars/shiaho777/web-to-app?style=for-the-badge)](https://github.com/shiaho777/web-to-app/stargazers)
[![Forks](https://img.shields.io/github/forks/shiaho777/web-to-app?style=for-the-badge)](https://github.com/shiaho777/web-to-app/network/members)
[![License](https://img.shields.io/badge/License-Unlicense-blue?style=for-the-badge)](../../LICENSE)
[![Android](https://img.shields.io/badge/Android-23%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)](#)

</div>

<p align="center">
  <a href="#tại-sao-chọn-webtoapp">Tại sao chọn WebToApp</a> ·
  <a href="#bạn-có-thể-xây-dựng-những-gì">Bạn có thể xây dựng những gì</a> ·
  <a href="#điểm-nổi-bật">Điểm nổi bật</a> ·
  <a href="#chợ-mô-đun">Chợ Mô-đun</a> ·
  <a href="#bản-đồ-tính-năng">Bản đồ tính năng</a> ·
  <a href="#xây-dựng-từ-mã-nguồn">Xây dựng</a>
</p>

---

<div align="center">
<img src="../assets/social-preview.jpg" width="90%" alt="WebToApp: Trang chủ ứng dụng của tôi, Trình chọn tạo ứng dụng, hộp công cụ chính và các hành động APK theo từng ứng dụng đang chạy trên điện thoại Android" />
</div>

---

## Tại sao chọn WebToApp

Hầu hết các công cụ "trang web thành ứng dụng" chỉ dừng lại ở việc bọc một URL. WebToApp giống như một xưởng APK bỏ túi: nó kết hợp WebView có thể định cấu hình, runtime máy chủ cục bộ, ký APK, mô-đun mở rộng, nhập/xuất dự án và quản lý ứng dụng trong một ứng dụng Android.

- **Xây dựng trên thiết bị** - đóng gói và ký APK bên trong ứng dụng, không có hàng đợi xây dựng từ xa.
- **Vượt xa các trang tĩnh** - đóng gói các trang web, bản dựng HTML/front-end, Node.js, PHP, Python, Go, WordPress, ứng dụng phương tiện, thư viện ảnh và ứng dụng đa trang web.
- **Kiểm soát đầu ra** - chọn tên gói, biểu tượng, quyền, khóa ký, sơ đồ chữ ký, tùy chọn runtime và định dạng xuất.
- **Mở rộng sau khi phát hành** - thêm các mô-đun JS/CSS, userscript hoặc tiện ích mở rộng MV3 Chrome mà không cần xây dựng lại ứng dụng chủ.
- **Luôn có thể kiểm tra** - máy khách Android, danh mục mô-đun và logic bản dựng nằm trong kho lưu trữ này.

## Bạn có thể xây dựng những gì

| Đầu vào | Đầu ra | Hữu ích cho |
| --- | --- | --- |
| URL Trang web | APK dựa trên WebView | Trang đích, công cụ, trang tổng quan, tài liệu, hệ thống nội bộ |
| HTML / front-end tĩnh | APK dựa trên Localhost | React, Vue, Vite, bản dựng tĩnh, ứng dụng web ngoại tuyến |
| Node.js / PHP / Python / Go | APK có máy chủ cục bộ trên thiết bị | Ứng dụng máy chủ nhỏ, công cụ quản trị, bản demo, nguyên mẫu |
| WordPress | APK chạy WordPress qua PHP cục bộ + SQLite | Trang web di động, demo chủ đề/plugin, gói nội dung cục bộ |
| Hình ảnh / video / thư viện ảnh | APK tập trung vào phương tiện | Album, tài liệu khóa học, danh mục đầu tư, trình xem ngoại tuyến |
| Nhiều trang web | APK đa trang web dạng Tab/thẻ/nguồn cấp dữ liệu/ngăn kéo | Trung tâm liên kết, cổng thông tin, bộ sưu tập ứng dụng |
| APK đã cài đặt | Bản sao được đổi thương hiệu hoặc ngụy trang phím tắt | Thử nghiệm Biểu tượng/tên/gói và nghiên cứu đóng gói lại ứng dụng |

Các giá trị `AppType` được hỗ trợ bao gồm Web, HTML, Frontend, WordPress, Node.js, PHP, Python, Go, Image, Video, Gallery và Multi-Web.

## Quy trình

1. **Tạo** một ứng dụng từ URL, thư mục dự án, tập hợp phương tiện hoặc mẫu runtime cục bộ.
2. **Tùy chỉnh** WebView, thanh công cụ, màn hình giật gân, mô-đun, quyền, ký và hành vi runtime.
3. **Xem trước** trên điện thoại trước khi tạo APK cuối cùng.
4. **Xây dựng và ký** APK trên thiết bị thông qua `com.android.tools.build:apksig`.
5. **Cài đặt, chia sẻ, xuất hoặc sao lưu** ứng dụng được tạo và dữ liệu dự án của nó.

## Điểm nổi bật

| Lĩnh vực | Điều gì nổi bật |
| --- | --- |
| Trình tạo APK | Bản vá AXML/ARSC nhị phân, chèn tài nguyên, cắt xén quyền, ký V1/V2/V3, xuất AAB sẵn sàng cho Google Play với tính năng ký trên thiết bị |
| Kiểm soát WebView | User-Agent, chế độ máy tính để bàn, chèn JS/CSS, DNS-over-HTTPS, proxy, trang lỗi tùy chỉnh, chiến lược bộ nhớ đệm PWA |
| Công cụ trình duyệt | System WebView theo mặc định, runtime GeckoView tùy chọn cho kết xuất kiểu Firefox |
| Runtime cục bộ | Node.js, PHP 8.4, Python, Go và WordPress chạy qua máy chủ HTTP cục bộ |
| Tiện ích mở rộng | Các mô-đun tích hợp, userscripts với API `GM_*`, tập lệnh nội dung tiện ích mở rộng MV3 Chrome, chia sẻ mã QR/xuất |
| Quyền riêng tư và tăng cường | Chặn quảng cáo, mã hóa tài nguyên, kiểm tra runtime, cách ly WebView, cổng mã kích hoạt |
| Trải nghiệm ứng dụng | Màn hình giật gân, BGM/LRC lời bài hát, cửa sổ nổi, chủ đề thanh trạng thái, thông báo, liên kết sâu, số liệu thống kê sử dụng |
| AI Coding | Tạo theo lời nhắc cho các ứng dụng web, mô-đun mở rộng và dự án runtime bên trong quy trình làm việc trên thiết bị di động |

## Tải Ứng dụng

Các bản phát hành được xuất bản trên [GitHub Releases](https://github.com/shiaho777/web-to-app/releases).

Ứng dụng chủ WebToApp ghim `targetSdk = 28` một cách có chủ ý - không phải là một hạn chế, mà là lựa chọn cho phép ứng dụng được tạo chạy Node.js, PHP, Python, Go và WordPress dưới dạng tệp nhị phân gốc thẳng từ bộ lưu trữ ứng dụng, giống như cách Termux làm. Hầu hết các công cụ "URL sang APK" chỉ có thể bọc WebView; chạy đầy đủ máy chủ runtime trên thiết bị là phần khó và 28 là thứ làm cho nó khả thi. Do đó, ứng dụng chủ được vận chuyển qua GitHub Releases.

Sự đánh đổi này chỉ áp dụng cho ứng dụng chủ và các ứng dụng runtime phân nhánh. Các ứng dụng Web, HTML, front-end và phương tiện được tạo hoàn toàn có thể xuất bản lên Google Play: trình xuất AAB trên thiết bị ghi lại `targetSdk` theo mức Play yêu cầu (hiện tại là 35) và ký bó cục bộ, vì vậy chúng được vận chuyển trực tiếp đến Cửa hàng Play. Các ứng dụng phân nhánh chạy cục bộ (Node.js, PHP, Python, Go, WordPress) chỉ được giữ lại APK, vì các ứng dụng Cửa hàng Play không thể phân nhánh các tệp nhị phân trên Android hiện đại.

## Chợ Mô-đun

WebToApp có một chợ mô-đun được hỗ trợ bởi GitHub cho các tiện ích mở rộng JS/CSS của cộng đồng. Danh mục này chỉ là các tệp trong kho lưu trữ này, vì vậy các đóng góp sử dụng quy trình pull request bình thường.

```
modules/
├── registry.json        # danh mục cho ứng dụng
├── submissions.json     # siêu dữ liệu PR / người gửi do CI tạo
├── README.md            # hướng dẫn cho người đóng góp
├── hello-world/
├── night-shift/
├── reading-mode/
├── floating-search/
└── auto-scroll/
```

Ứng dụng lấy cả `registry.json` và `submissions.json`, và chỉ hiển thị các mô-đun xuất hiện trong cả hai. Điều đó giữ cho danh mục trong ứng dụng phù hợp với các PR đã thực sự được hợp nhất.

- Người dùng mở **Mô-đun mở rộng** và chạm vào biểu tượng mặt tiền cửa hàng.
- Những người đóng góp thêm một thư mục trong `modules/`, cập nhật `registry.json` và mở PR.
- Bộ nhớ đệm khách mặc định là một giờ, vì vậy các mô-đun được hợp nhất lan truyền mà không cần cập nhật ứng dụng.

[Hướng dẫn đóng góp mô-đun](../../modules/README.md) · [Hướng dẫn đóng góp chung](../CONTRIBUTING.md)

## Bản đồ Tính năng

Ứng dụng đầy đủ có nhiều công tắc. Các phần dưới đây nhóm các phần quan trọng mà không làm cho phần đầu của README giống như một bãi chứa cài đặt.

<details>
<summary><b>Công cụ trình duyệt và mạng</b></summary>

- Chế độ máy tính để bàn, User-Agent tùy chỉnh và chèn JS/CSS ở document start, end hoặc idle.
- Ngụy trang hương vị Kernel cho bản trình bày kiểu Chrome, Edge, Samsung Internet, Firefox hoặc Safari trong khi vẫn giữ nguyên công cụ thực.
- Xử lý cửa sổ bật lên: cùng một cửa sổ, trình duyệt bên ngoài, cửa sổ bật lên hoặc khối.
- Proxy tĩnh HTTP/HTTPS/SOCKS5, proxy PAC, xác thực, quy tắc vòng qua và cầu nối HTTP-to-SOCKS cục bộ.
- Các nhà cung cấp DNS-over-HTTPS: Cloudflare, Google, AdGuard, NextDNS, CleanBrowsing, Quad9, Mullvad, cộng với các điểm cuối tùy chỉnh.
- Chiến lược bộ nhớ đệm ngoại tuyến PWA, trang lỗi tùy chỉnh, máy chủ ứng dụng ghi đè và trình xử lý lược đồ thanh toán.
- Các công tắc tương thích tùy chọn để tải xuống blob, bộ nhớ cuộn, sửa chữa hình ảnh, khay nhớ tạm, định hướng, polyfill thông báo, cầu nối mạng riêng và cổng khả năng Native Bridge.

</details>

<details>
<summary><b>Tiện ích mở rộng và tự động hóa</b></summary>

- Mô-đun tích hợp: tải xuống video, trích xuất Bilibili/Douyin/Xiaohongshu, trình tăng cường video, trình phân tích web, tìm trong trang, chế độ tối, công cụ quyền riêng tư, công cụ tăng cường nội dung và công cụ chặn phần tử.
- Hỗ trợ Userscript cho các tập lệnh `.user.js` kiểu Greasemonkey/Tampermonkey.
- Cầu nối `GM_*` với bộ nhớ, yêu cầu, kiểu, lệnh menu và API `GM.*` dựa trên lời hứa dựa trên cấp phép tập lệnh.
- MV3 Chrome extension runtime cho các tập lệnh nội dung dựa trên bảng kê khai ở các thế giới bị cách ly hoặc thế giới chính.
- `chrome.*` polyfills cho runtime, lưu trữ, tab, tập lệnh và phân tích cú pháp yêu cầu mạng khai báo.
- Mã xuất (`WTA1:` gzip + Base64) và chia sẻ QR qua ZXing.
- Các kỹ năng AI Coding để tạo mô-đun, userscript, tiện ích mở rộng MV3, ứng dụng front-end và dự án runtime cục bộ.

</details>

<details>
<summary><b>Runtime trên thiết bị</b></summary>

- **Node.js** chạy trong một quy trình hệ điều hành `:nodejs` chuyên dụng thông qua trình bao bọc `node_launcher` gốc tải `libnode.so`.
- **PHP** sử dụng PHP 8.4 từ `pmmp/PHP-Binaries`, tải xuống một lần trong lần sử dụng đầu tiên, có hỗ trợ Composer.
- **Python** hỗ trợ Flask, Django, FastAPI thông qua uvicorn, Tornado, máy chủ HTTP tích hợp và các phần phụ thuộc pip trong `.pypackages`.
- **Go** hỗ trợ bản dựng `go build` trên thiết bị, các bản dựng ngoại tuyến `vendor/`, phục vụ tĩnh và trình bao bọc gốc `go_exec_loader`.
- **WordPress** chạy trên PHP cục bộ với SQLite thông qua `sqlite-database-integration`, có nhập chủ đề và plugin.
- Màn hình Linux Environment quản lý các chuỗi công cụ và phần phụ thuộc cho Node, PHP và Python.
- Port Manager điều phối các cổng runtime trên các ứng dụng được tạo thông qua các bộ thu phát sóng.

</details>

<details>
<summary><b>Trải nghiệm ứng dụng</b></summary>

- Màn hình giật gân hình ảnh hoặc video với hành vi bỏ qua, phạm vi cắt xén và hướng cố định.
- Danh sách phát nhạc nền có lời bài hát LRC được đồng bộ hóa, hoạt ảnh lời bài hát, phông chữ/màu sắc/nét/bóng tùy chỉnh và tìm kiếm nhạc trực tuyến.
- Thanh công cụ, thanh trạng thái, chế độ tối thanh trạng thái, hành vi điều hướng, chế độ cửa sổ nổi và kiểu menu nhấn giữ.
- Các mẫu thông báo cho những khoảnh khắc khởi chạy, khoảng thời gian và không có mạng.
- Lớp phủ dịch với 20 ngôn ngữ đích và các công cụ Google, MyMemory, LibreTranslate, Lingva hoặc Auto.
- Web Notification polyfill, thông báo theo lịch trình và liên tục với cập nhật tiến độ, URL polling foreground service, liên kết sâu, khởi động tự động khởi động, khởi chạy theo lịch trình và dịch vụ chạy nền.
- Thống kê sử dụng theo từng ứng dụng với Vico charts và theo dõi tình trạng URL.

</details>

<details>
<summary><b>Bảo mật, quyền riêng tư và quyền truy cập có kiểm soát</b></summary>

- Mã hóa tài nguyên cho cấu hình được đóng gói, HTML, phương tiện và BGM thông qua PBKDF2 + AES-256-GCM.
- Mật khẩu mã hóa tùy chỉnh tùy chọn để bảo vệ mạnh hơn so với các giá trị mặc định có nguồn gốc từ gói/chứng chỉ.
- Runtime anti-debug, anti-Frida, and DEX-tamper checks khi mã hóa tài nguyên được bật.
- Các phản hồi đe dọa: chỉ ghi log, thoát âm thầm hoặc sự cố ngẫu nhiên.
- WebView/cách ly nội dung cho lưu trữ, WebRTC, Canvas, Audio, WebGL, phông chữ, tiêu đề và bề mặt IP.
- Ngụy trang dấu vân tay trình duyệt trên 28 vectơ, bao gồm UA, WebGL, Canvas, AudioContext, ClientRects, múi giờ, ngôn ngữ, bộ nhớ, thiết bị đa phương tiện, WebRTC, phông chữ, pin, quyền, hiệu suất, bộ nhớ, thông báo, phương tiện CSS, truyền iframe và dọn dẹp ngăn xếp lỗi.
- Trình chặn quảng cáo theo quy tắc Hosts với bộ lọc MutationObserver và 23 danh sách bộ lọc cộng đồng được tích hợp sẵn.
- Cổng mã kích hoạt có xác minh cục bộ hoặc điểm cuối HTTPS của riêng bạn được ký bằng EC P-256. Xem [tài liệu kích hoạt từ xa](remote-activation.md).

</details>

<details>
<summary><b>Xuất và ký APK</b></summary>

- Tên gói tùy chỉnh, `versionName`, `versionCode`, biểu tượng, nhãn, mục tiêu kiến trúc và định dạng xuất.
- Chèn quyền trong thời gian xây dựng cho APK được tạo, với các quyền chưa sử dụng được cắt khỏi bảng kê khai mẫu.
- Tùy chọn hiệu suất: nén hình ảnh, chuyển đổi WebP, thu nhỏ mã, tải chậm, tìm nạp trước DNS và gợi ý tải trước.
- Sao lưu/khôi phục toàn bộ dự án và sao lưu/khôi phục dữ liệu ứng dụng.
- Xuất AAB trên thiết bị với ghi lại `targetSdk` theo yêu cầu của Play và siêu dữ liệu protobuf được tạo cục bộ, được ký bằng kho khóa của bạn.
- Tạo, nhập, xuất, xóa kho khóa và xem dấu vân tay chứng chỉ.
- Nhập PKCS12/PFX/JKS/BKS, bao gồm các trường hợp khóa tải lên của Android Studio trong đó mật khẩu lưu trữ và mật khẩu khóa khác nhau.
- Các kiểm soát sơ đồ chữ ký cho V1, V2 và V3, với tính năng tự động dự phòng cho khả năng tương thích chứng chỉ cũ.
- Tên tệp chữ ký V1 tùy chỉnh cho `META-INF/<name>.SF` và `META-INF/<name>.RSA`.

</details>

<details>
<summary><b>Các công cụ và tính năng nghiên cứu chuyên biệt</b></summary>

- Website Scraper cho các gói ngoại tuyến: HTML, CSS, JS, hình ảnh, phông chữ, CSS `url()`, `srcset`, `@import`, viết lại đường dẫn, giới hạn cùng miền, giới hạn độ sâu và giới hạn kích thước.
- Bố cục Đa web: tab, thẻ, nguồn cấp dữ liệu, ngăn kéo, biểu tượng mỗi trang web, màu chủ đề, bộ chọn trích xuất, khoảng thời gian làm mới và JS/CSS được chia sẻ.
- Ứng dụng thư viện ảnh với phương tiện được phân loại, chế độ xem lưới/danh sách/dòng thời gian, phát ngẫu nhiên/lặp lại một, sắp xếp, thanh hình thu nhỏ, lớp phủ, tự động chuyển tiếp và bộ nhớ phát lại.
- Trình sửa đổi ứng dụng (App Modifier) để ngụy trang phím tắt hoặc sao chép nhị phân thực sự với bảng kê khai/vá tài nguyên và ký lại.
- Các tính năng Bắt buộc chạy, BlackTech, Ngụy trang thiết bị và Cơn bão biểu tượng được bao gồm để trình diễn kỹ thuật và chỉ được sử dụng với sự đồng ý sáng suốt của người dùng.

</details>

## Ghi chú Kiến trúc

- Kho lưu trữ có hai mô-đun Gradle: `app` là trình xây dựng và ứng dụng chủ đầy đủ; `shell` là máy chủ runtime được nhúng vào các APK được tạo.
- Mã runtime được tạo trong `app` và được đồng bộ hóa thành `shell`, do đó WebView/hành vi runtime được chia sẻ có một nguồn sự thật.
- Trình tạo APK vá các APK mẫu ở cấp AXML/ARSC nhị phân, chèn cấu hình/tài nguyên, cắt bớt quyền và ký bằng `apksig`.
- Ứng dụng chủ ghim `targetSdk = 28` một cách có chủ ý - đó là điều cho phép các ứng dụng được tạo rẽ nhánh và thực thi các runtime gốc (Node.js, PHP, Python, Go, WordPress) từ bộ lưu trữ ứng dụng, một khả năng mà các công cụ bao bọc URL còn thiếu; bộ xuất AAB ghi lại riêng `targetSdk` để phân phối Cửa hàng Play.
- Server runtimes và runtime gốc GeckoView tùy chọn được tải xuống vào lần sử dụng đầu tiên thay vì được gộp vào APK cơ sở.

## Kho công nghệ (Tech Stack)

- Kotlin, Jetpack Compose, Material 3
- Koin cho chèn phụ thuộc (dependency injection)
- Room 2.7.2 + KSP để lưu giữ
- OkHttp 4.12.0 + `okhttp-dnsoverhttps`
- `com.android.tools.build:apksig` 8.3.0 để ký APK
- `protobuf-javalite` 3.25.5 cho siêu dữ liệu AAB
- GeckoView như một công cụ trình duyệt tùy chọn
- Coil để tải hình ảnh/video/GIF
- AndroidX Security Crypto + DataStore cho các bí mật được lưu trữ
- Vico Compose-M3 cho biểu đồ
- ZXing cho chia sẻ QR
- Apache Commons Compress + xz để nhập dự án và lấy dữ liệu trang web
- C++ gốc qua JNI cho `node_launcher` và `go_exec_loader`
- Robolectric cho các bài kiểm tra đơn vị (unit tests)

Xem [app/build.gradle.kts](../../app/build.gradle.kts) để biết danh sách phụ thuộc đầy đủ.

## Xây dựng từ mã nguồn

Yêu cầu: Android Studio Hedgehog hoặc mới hơn, JDK 17. Gradle wrapper ghim Gradle 9.4.1.

```bash
git clone https://github.com/shiaho777/web-to-app.git
cd web-to-app
./gradlew assembleDebug
```

Đối với các bản dựng phát hành, hãy định cấu hình ký thông qua `local.properties` và `app/build.gradle.kts`.

## Đóng góp

| Lane | Việc bạn làm | Hướng dẫn |
| --- | --- | --- |
| `modules/` | Xuất bản mô-đun cộng đồng lên thị trường trong ứng dụng | [modules/README.md](../../modules/README.md) |
| Issues | Báo cáo lỗi hoặc yêu cầu tính năng | [GitHub Issues](https://github.com/shiaho777/web-to-app/issues) |
| Code | Khắc phục lỗi hoặc xây dựng tính năng trong máy khách Android | [CONTRIBUTING.md](../CONTRIBUTING.md) |

## Liên hệ

Được phát triển bởi **shiaho**.

| Nền tảng | Liên kết |
| --- | --- |
| GitHub | [github.com/shiaho777/web-to-app](https://github.com/shiaho777/web-to-app) |
| Telegram | [t.me/webtoapp777](https://t.me/webtoapp777) |
| X (Twitter) | [@shiaho777](https://x.com/shiaho777) |
| Bilibili | [b23.tv/8mGDo2N](https://b23.tv/8mGDo2N) |
| Nhóm QQ | 1041130206 |

## Giấy phép (License)

[The Unlicense](../../LICENSE).

Các tính năng nâng cao như bắt buộc chạy, BlackTech, ngụy trang thiết bị và Cơn bão biểu tượng nhằm mục đích trình diễn kỹ thuật và chỉ được sử dụng với sự đồng ý rõ ràng của người dùng.

<div align="center">

**Mã nguồn mở · Xây dựng cho người dùng quyền lực của Android · Star để hỗ trợ dự án**

</div>
