# Plan triển khai bài thi phân công cán bộ coi thi

Mục tiêu của bài này là xây dựng một ứng dụng Java client-server trong project `ThiTHLTM` để đọc danh sách cán bộ coi thi từ file Excel, nhận số giám thị `m`, số phòng thi `n` và thông tin "đợt thi". Server chịu trách nhiệm phân công ngẫu nhiên không lặp, bảo đảm mỗi phòng thi luôn có đúng 2 giám thị, phân bổ cán bộ còn dư làm giám sát hành lang, lưu dữ liệu vào MySQL. Client nhận kết quả trả về, hiển thị lên giao diện và xuất các file Excel vào thư mục theo đúng định dạng đề bài.

## 1. Phạm vi bắt buộc

- Client phải đọc dữ liệu từ file `Danh sach can bo coi thi.xlsx`.
- Người dùng nhập `m` và `n` trên client.
- Client gửi dữ liệu cần thiết sang server (bao gồm thông tin tên "đợt thi").
- Server nhận dữ liệu, lưu vào database, thực hiện thuật toán phân công ngẫu nhiên không trùng. Mỗi phòng thi phải có đúng 2 giám thị. Nếu số cán bộ còn dư sau khi phân 2 giám thị cho mỗi phòng, phần dư đó sẽ được chuyển thành cán bộ giám sát hành lang và chia đều cho tất cả các phòng thi. Server sau đó trả kết quả phân công về cho client.
- Client nhận kết quả, hiển thị danh sách cán bộ coi thi và giám sát hành lang lên giao diện. Sau đó client tạo thư mục với tên "đợt thi" (nếu không điền tên đợt thi thì tự lấy ngày giờ hiện tại đặt tên folder) và tạo 2 file Excel kết quả lưu vào thư mục đó. Các file Excel do client xuất phải có phần tiêu đề như hình mẫu: `Cộng hòa xã hội chủ nghĩa Việt Nam` và `Độc Lập - Tự Do - Hạnh Phúc`.
- Nếu số cán bộ cần xuất vượt quá giới hạn, phải tách sang nhiều sheet; mỗi sheet tối đa `24` cán bộ.

## 2. Hướng thiết kế để agent có thể code ngay

### 2.1. Kiến trúc

- Client thực hiện các việc sau: đọc Excel, lấy danh sách cán bộ hợp lệ, nhận đầu vào `m`, `n` cùng "đợt thi", gửi payload sang server. Sau đó nhận kết quả phân công, hiển thị lên giao diện, tạo thư mục theo tên đợt thi (hoặc ngày giờ hiện tại) và xuất 2 file Excel lưu vào thư mục đó.
- Server là nơi xử lý chính: kiểm tra dữ liệu, lưu vào MySQL, sinh phân công, và trả kết quả phân công (dữ liệu thô) về cho client.
- Không để client tự sắp xếp logic cuối cùng; mọi thuật toán bốc ngẫu nhiên và chia phòng phải nằm ở server để dễ kiểm soát.

### 2.2. Luồng xử lý

1. Client mở file Excel nguồn và trích các cột cần dùng.
2. Client yêu cầu người dùng nhập `m` và `n`.
3. Client gửi danh sách cán bộ cùng thông tin "đợt thi" (phiên làm việc) sang server.
4. Server tạo một đợt phân công mới trong database.
5. Server shuffle danh sách cán bộ bằng bộ sinh ngẫu nhiên chuẩn, sau đó lấy đúng số lượng cần dùng, không lặp.
6. Server chia cán bộ vào `n` phòng thi theo quy tắc cố định: mỗi phòng thi nhận đúng 2 giám thị. Phần cán bộ còn lại được gán làm giám sát hành lang và phân đều cho tất cả các phòng thi.
7. Server trả kết quả phân công về cho client.
8. Client nhận kết quả, hiển thị phân công lên giao diện. Tiếp theo, tạo thư mục có tên "đợt thi" (nếu trống sẽ lấy ngày giờ hiện tại) và sinh 2 file Excel kết quả lưu vào đó, mỗi sheet không quá `24` cán bộ.

## 3. Database cần lưu gì và lưu như thế nào

### 3.1. Dữ liệu nguồn

- Lưu danh sách cán bộ đã đọc từ Excel vào bảng `can_bo_coi_thi`.
- Dữ liệu cần map đúng các cột: `TT`, `Mã GV`, `Họ Tên`, `Ngày sinh`, `Đơn vị công tác`.
- Mục đích của bảng này là tránh phải đọc lại toàn bộ file mỗi lần chạy và có thể truy vết dữ liệu đầu vào.

### 3.2. Dữ liệu phiên phân công

- Mỗi lần chạy bài toán tạo một bản ghi mới trong bảng `dot_phan_cong`.
- Bảng này lưu `m`, `n`, trạng thái xử lý, tên file nguồn, và thời gian tạo.

### 3.3. Dữ liệu phòng thi

- Lưu danh sách phòng thi đọc từ Excel vào bảng `phong_thi_nguon`.
- Dữ liệu cần map đúng các cột: `STT`, `Phòng thi`, `Ghi chú`.
- Khi chạy phân công, tạo bảng `phong_thi_phan_cong` để gắn phòng thi nguồn với từng đợt phân công.

### 3.4. Dữ liệu chi tiết phân công

- Mỗi cán bộ được gán vào một phòng thi sẽ có một dòng trong bảng `phan_cong_chi_tiet`.
- Bảng này lưu khóa của đợt phân công, khóa của phòng thi đã gắn với đợt đó, khóa cán bộ, và vị trí trong phòng nếu cần.
- Cần ràng buộc unique để một cán bộ không bị phân công lặp trong cùng một đợt.

## 4. Thuật toán bắt buộc

- Dùng `shuffle` để xáo trộn danh sách cán bộ trước khi phân công.
- Không dùng cách chọn ngẫu nhiên có hoàn lại.
- Không để một cán bộ xuất hiện ở hai phòng khác nhau trong cùng một đợt.
- Mỗi phòng thi phải luôn có đúng 2 giám thị.
- Số cán bộ còn dư sau khi gán giám thị sẽ trở thành cán bộ giám sát hành lang.
- Cán bộ giám sát hành lang phải được chia đều cho tất cả các phòng thi.
- Nếu số cán bộ ít hơn yêu cầu thì phải báo lỗi rõ ràng.
- Nếu `n <= 0` hoặc `m <= 0` thì không chạy phân công.
- Nếu dữ liệu Excel thiếu cột hoặc sai định dạng thì dừng và báo lỗi sớm.

## 5. Quy tắc xuất Excel

- Dòng tiêu đề đầu file phải có quốc hiệu và tiêu ngữ đúng như hình mẫu.
- Dữ liệu phải được chia theo sheet nếu vượt quá giới hạn hiển thị.
- Mỗi sheet tối đa `24` cán bộ.
- Giữ bố cục cột dễ đọc, ưu tiên giống file mẫu về thứ tự cột và cách trình bày.
- Nên có một sheet tổng hợp đầu tiên nếu phù hợp với đề bài.




## 6. Gợi ý cấu trúc mã nguồn cho agent

- `client`: đọc Excel, nhập tham số, gửi cho server, nhận kết quả phân công, hiển thị kết quả, tạo thư mục và xuất file Excel.
- `server`: nhận request, validate, lưu DB, phân công, trả dữ liệu phân công.
- `model`: DTO và entity cho cán bộ, phòng thi, đợt phân công.
- `dao`: thao tác MySQL.
- `util`: đọc Excel, ghi Excel, sinh ngẫu nhiên, kiểm tra dữ liệu.

## 7. Thứ tự code khuyến nghị

1. Tạo schema MySQL bằng file SQL kèm theo.
2. Tạo model và DAO cho các bảng chính.
3. Viết phần đọc Excel ở client.
4. Viết socket hoặc cơ chế giao tiếp client-server.
5. Viết thuật toán phân công ngẫu nhiên ở server.
6. Viết phần hiển thị kết quả và xuất Excel nhiều sheet ở client.
7. Kiểm tra các case lỗi và dữ liệu biên.

## 8. Điều kiện hoàn thành

- Client đọc được file Excel và lấy đúng dữ liệu đầu vào.
- Server lưu được dữ liệu vào MySQL.
- Phân công không bị lặp cán bộ trong cùng một đợt.
- Mỗi phòng thi luôn có đúng 2 giám thị và phần cán bộ dư được chia đều làm giám sát hành lang.
- File Excel do client tạo ra có đúng tiêu đề, đúng bố cục, và tách sheet khi cần, nằm trong đúng thư mục được yêu cầu.
- Có xử lý lỗi cho các trường hợp nhập sai `m`, `n`, hoặc file Excel không hợp lệ.

## 9. Định dạng file Excel kết quả (chi tiết)

Hai file Excel kết quả phải được xuất theo đúng cấu trúc dưới đây để thuận tiện cho kiểm tra và lưu trữ.

1) File: DANHSACH PHANCONG.XLSX (danh sách phân công)

- Mô tả: danh sách mỗi cán bộ và cờ chỉ vị trí giám thị 1 hoặc giám thị 2, kèm tên phòng thi.
- Cột (theo thứ tự):
	- `STT` (số thứ tự trong file kết quả, chuỗi/định dạng 01, 02...)
	- `Mã GV` (mã cán bộ, lấy từ file nguồn)
	- `Họ và tên` (tên đầy đủ)
	- `Giám thị 1` (ghi ký hiệu `X` nếu cán bộ này là giám thị số 1 của phòng đó; để trống nếu không)
	- `Giám thị 2` (ghi ký hiệu `X` nếu cán bộ này là giám thị số 2 của phòng đó; để trống nếu không)
	- `Phòng thi` (mã/đầu số phòng thi như trong file phòng thi, ví dụ `128`, `C102`)

- Ghi chú cho agent khi sinh file: mỗi hàng biểu diễn một cán bộ. Nếu cán bộ là giám thị 1 thì cột `Giám thị 1` = `X`, nếu là giám thị 2 thì cột `Giám thị 2` = `X`.

2) File: DANHSACH GIAMSAT.XLSX (danh sách cán bộ giám sát hành lang)

- Mô tả: danh sách cán bộ giám sát hành lang và phạm vi phòng mà họ giám sát.
- Cột (theo thứ tự):
	- `STT` (số thứ tự)
	- `Mã GV` (mã cán bộ)
	- `Họ và tên` (tên đầy đủ)
	- `Phòng thi được giám sát` (mô tả phạm vi phòng, có thể là một khoảng như `Từ C101 đến C110` hoặc `Từ 128 đến 137`)

- Ghi chú cho agent khi sinh file: chia đều số cán bộ giám sát hành lang cho tất cả phòng thi. Với mỗi cán bộ giám sát, ghi phạm vi phòng mà họ phụ trách theo định dạng `Từ <phòng bắt đầu> đến <phòng kết thúc>` hoặc danh sách phòng ngăn cách bởi dấu phẩy nếu cần.

3) Tổng quát:

- Tên file phải dùng chữ hoa như mẫu (ví dụ `DANHSACH PHANCONG.XLSX` và `DANHSACH GIAMSAT.XLSX`).
- Mỗi sheet không quá 24 dòng dữ liệu nếu đề bài yêu cầu giới hạn tại từng sheet; nếu cần tách nhiều sheet thì giữ tiêu đề giống nhau trên các sheet.
- Giữ encoding UTF-8 và định dạng ký tự tiếng Việt (các tiêu đề phải giống mẫu: `Cộng hòa xã hội chủ nghĩa Việt Nam` / `Độc Lập - Tự Do - Hạnh Phúc`).