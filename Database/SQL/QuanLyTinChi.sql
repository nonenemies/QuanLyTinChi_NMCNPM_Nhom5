-- Fresh development/test database setup.
-- WARNING: running this script drops the existing QuanLyTinChi database and all its data.
-- Sample accounts use the development-only password "123"; do not use this script in production.
DROP DATABASE IF EXISTS QuanLyTinChi;
CREATE DATABASE QuanLyTinChi CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE QuanLyTinChi;

CREATE TABLE Users (
    UserID INT AUTO_INCREMENT PRIMARY KEY,
    HoTen VARCHAR(100) NOT NULL,
    Email VARCHAR(100) UNIQUE NOT NULL,
    PasswordHash VARCHAR(255) NOT NULL,
    Role VARCHAR(20) NOT NULL,
    MaSo VARCHAR(20) UNIQUE,
    Khoa VARCHAR(100) NULL,
    NgayTao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    TrangThai ENUM('HOAT_DONG', 'NGUNG_HOAT_DONG') NOT NULL DEFAULT 'HOAT_DONG'
) ENGINE=InnoDB;

CREATE TABLE SinhVien (
    UserID INT PRIMARY KEY,
    MaSV VARCHAR(20) UNIQUE NOT NULL,
    NgaySinh DATE NULL,
    GioiTinh TINYINT(1) NULL,
    Lop VARCHAR(50) NULL,
    KhoaHoc INT NULL,
    HeDaoTao VARCHAR(50) NULL,
    CONSTRAINT fk_sinhvien_user FOREIGN KEY (UserID) REFERENCES Users(UserID)
) ENGINE=InnoDB;

CREATE TABLE GiaoVien (
    UserID INT PRIMARY KEY,
    MaGV VARCHAR(20) UNIQUE NOT NULL,
    ChuyenNganh VARCHAR(100) NULL,
    HocVi VARCHAR(50) NULL,
    CONSTRAINT fk_giaovien_user FOREIGN KEY (UserID) REFERENCES Users(UserID)
) ENGINE=InnoDB;

CREATE TABLE Admin (
    UserID INT PRIMARY KEY,
    ChucVu VARCHAR(100) NULL,
    CONSTRAINT fk_admin_user FOREIGN KEY (UserID) REFERENCES Users(UserID)
) ENGINE=InnoDB;

CREATE TABLE MonHoc (
    MaMon VARCHAR(20) PRIMARY KEY,
    TenMon VARCHAR(200) NOT NULL,
    SoTinChi INT NOT NULL,
    LoaiMon VARCHAR(20) NOT NULL,
    MoTa VARCHAR(500) NULL,
    TrangThai ENUM('HOAT_DONG', 'NGUNG_HOAT_DONG') NOT NULL DEFAULT 'HOAT_DONG',
    CONSTRAINT chk_monhoc_tinchi CHECK (SoTinChi > 0)
) ENGINE=InnoDB;

CREATE TABLE MonTienQuyet (
    MaMon VARCHAR(20) NOT NULL,
    MaMonTienQuyet VARCHAR(20) NOT NULL,
    PRIMARY KEY (MaMon, MaMonTienQuyet),
    CONSTRAINT fk_tienquyet_mon FOREIGN KEY (MaMon) REFERENCES MonHoc(MaMon),
    CONSTRAINT fk_tienquyet_mon_tienquyet FOREIGN KEY (MaMonTienQuyet) REFERENCES MonHoc(MaMon),
    CONSTRAINT chk_tienquyet_khac_mon CHECK (MaMon <> MaMonTienQuyet)
) ENGINE=InnoDB;

CREATE TABLE LopHoc (
    MaLop VARCHAR(20) PRIMARY KEY,
    MaMon VARCHAR(20) NOT NULL,
    MaGV VARCHAR(20) NULL,
    HocKy VARCHAR(20) NOT NULL,
    SiSoToiDa INT NOT NULL,
    SiSoHienTai INT NOT NULL DEFAULT 0,
    TrangThai ENUM('HOAT_DONG', 'NGUNG_HOAT_DONG') NOT NULL DEFAULT 'HOAT_DONG',
    CONSTRAINT fk_lophoc_mon FOREIGN KEY (MaMon) REFERENCES MonHoc(MaMon),
    CONSTRAINT fk_lophoc_giaovien FOREIGN KEY (MaGV) REFERENCES GiaoVien(MaGV),
    CONSTRAINT chk_lophoc_siso CHECK (
        SiSoToiDa > 0 AND SiSoHienTai >= 0 AND SiSoHienTai <= SiSoToiDa
    )
) ENGINE=InnoDB;

CREATE TABLE CauHinhHocKy (
    HocKy VARCHAR(20) PRIMARY KEY,
    KetThucHocKy DATE NOT NULL,
    HanNhapDiem DATETIME NOT NULL,
    CONSTRAINT chk_hocky_thoigian CHECK (
        HanNhapDiem > TIMESTAMP(KetThucHocKy)
    )
) ENGINE=InnoDB;

CREATE TABLE ChuongTrinhDaoTao (
    Khoa VARCHAR(100) NOT NULL,
    MaMon VARCHAR(20) NOT NULL,
    PRIMARY KEY (Khoa, MaMon),
    CONSTRAINT fk_ctdt_mon FOREIGN KEY (MaMon) REFERENCES MonHoc(MaMon)
) ENGINE=InnoDB;

CREATE TABLE DotDangKyNganh (
    Khoa VARCHAR(100) PRIMARY KEY,
    MoDangKy DATETIME NOT NULL,
    DongDangKy DATETIME NOT NULL,
    CONSTRAINT chk_dotdangky_thoigian CHECK (DongDangKy > MoDangKy)
) ENGINE=InnoDB;

CREATE TABLE LichHoc (
    MaLich INT AUTO_INCREMENT PRIMARY KEY,
    MaLop VARCHAR(20) NOT NULL,
    Thu INT NOT NULL,
    GioBatDau TIME NOT NULL,
    GioKetThuc TIME NOT NULL,
    PhongHoc VARCHAR(50) NULL,
    CONSTRAINT fk_lichhoc_lop FOREIGN KEY (MaLop) REFERENCES LopHoc(MaLop),
    CONSTRAINT chk_lichhoc_thu CHECK (Thu BETWEEN 2 AND 8),
    CONSTRAINT chk_lichhoc_gio CHECK (GioBatDau < GioKetThuc)
) ENGINE=InnoDB;

CREATE TABLE ThanhPhanDanhGia (
    MaTP INT AUTO_INCREMENT PRIMARY KEY,
    MaMon VARCHAR(20) NOT NULL,
    TenThanhPhan VARCHAR(200) NOT NULL,
    TrongSo DECIMAL(5,2) NOT NULL,
    ThuTu INT NULL,
    TrangThai TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_thanhphan_mon FOREIGN KEY (MaMon) REFERENCES MonHoc(MaMon),
    CONSTRAINT uq_thanhphan_mon_thutu UNIQUE (MaMon, ThuTu),
    CONSTRAINT chk_thanhphan_trongso CHECK (TrongSo > 0 AND TrongSo <= 1)
) ENGINE=InnoDB;

CREATE TABLE DangKyHoc (
    MaDangKy INT AUTO_INCREMENT PRIMARY KEY,
    MaLop VARCHAR(20) NOT NULL,
    MaSV VARCHAR(20) NOT NULL,
    CONSTRAINT uq_dangky_sv_lop UNIQUE (MaSV, MaLop),
    NgayDangKy DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    DiemTongKet DECIMAL(5,2) NULL,
    TrangThai VARCHAR(20) NOT NULL DEFAULT 'DANG_HOC',
    CONSTRAINT fk_dangky_lop FOREIGN KEY (MaLop) REFERENCES LopHoc(MaLop),
    CONSTRAINT fk_dangky_sinhvien FOREIGN KEY (MaSV) REFERENCES SinhVien(MaSV)
) ENGINE=InnoDB;

CREATE TABLE DiemThanhPhan (
    MaDiemTP INT AUTO_INCREMENT PRIMARY KEY,
    MaDangKy INT NOT NULL,
    MaTP INT NOT NULL,
    DiemDatDuoc DECIMAL(5,2) NOT NULL,
    NgayNhap DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_diem_dangky FOREIGN KEY (MaDangKy) REFERENCES DangKyHoc(MaDangKy),
    CONSTRAINT fk_diem_thanhphan FOREIGN KEY (MaTP) REFERENCES ThanhPhanDanhGia(MaTP),
    CONSTRAINT uq_diem_dangky_thanhphan UNIQUE (MaDangKy, MaTP),
    CONSTRAINT chk_diem_giatri CHECK (DiemDatDuoc BETWEEN 0 AND 10)
) ENGINE=InnoDB;

CREATE TABLE YeuCauDoiLop (
    MaYeuCau INT AUTO_INCREMENT PRIMARY KEY,
    MaSV VARCHAR(20) NOT NULL,
    LopCu VARCHAR(20) NOT NULL,
    LopMoi VARCHAR(20) NOT NULL,
    TrangThai VARCHAR(20) NOT NULL DEFAULT 'CHO_XAC_NHAN',
    LyDo VARCHAR(200) NULL,
    NgayTao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    NgayXuLy DATETIME NULL,
    NguoiXuLy INT NULL,
    CONSTRAINT fk_doilop_sinhvien FOREIGN KEY (MaSV) REFERENCES SinhVien(MaSV),
    CONSTRAINT fk_doilop_lopcu FOREIGN KEY (LopCu) REFERENCES LopHoc(MaLop),
    CONSTRAINT fk_doilop_lopmoi FOREIGN KEY (LopMoi) REFERENCES LopHoc(MaLop),
    CONSTRAINT fk_doilop_nguoixuly FOREIGN KEY (NguoiXuLy) REFERENCES Users(UserID),
    CONSTRAINT chk_doilop_khac_lop CHECK (LopCu <> LopMoi)
) ENGINE=InnoDB;

-- Development sample accounts. Password for every account below: 123.
INSERT INTO Users (UserID, HoTen, Email, PasswordHash, Role, MaSo, Khoa) VALUES
(1, 'Quản Trị Viên', 'admin@truong.edu.vn', '123', 'ADMIN', 'AD01', NULL),
(2, 'Thầy Trần Code', 'code.tran@truong.edu.vn', '123', 'GIAOVIEN', 'GV01', 'CNTT'),
(3, 'Nguyễn Văn Sinh', 'sinh.nguyen@sv.edu.vn', '123', 'SINHVIEN', 'SV001', 'CNTT'),
(4, 'Lê Thị Viên', 'vien.le@sv.edu.vn', '123', 'SINHVIEN', 'SV002', 'CNTT'),
(5, 'Trần Văn Ba', 'ba.tran@sv.edu.vn', '123', 'SINHVIEN', 'SV003', 'CNTT');

INSERT INTO Admin (UserID, ChucVu) VALUES (1, 'Trưởng phòng Đào tạo');
INSERT INTO GiaoVien (UserID, MaGV, ChuyenNganh, HocVi)
VALUES (2, 'GV01', 'Công nghệ phần mềm', 'Thạc sĩ');
INSERT INTO SinhVien (UserID, MaSV, NgaySinh, GioiTinh, Lop, KhoaHoc, HeDaoTao) VALUES
(3, 'SV001', '2003-05-12', 1, 'CN01', 2021, 'Chính quy'),
(4, 'SV002', '2003-09-20', 0, 'CN01', 2021, 'Chính quy'),
(5, 'SV003', '2003-11-03', 1, 'CN02', 2021, 'Chính quy');

INSERT INTO MonHoc (MaMon, TenMon, SoTinChi, LoaiMon, MoTa) VALUES
('IT01', 'Lập trình hướng đối tượng', 3, 'BATBUOC', 'Môn học nền tảng về lập trình hướng đối tượng.'),
('IT02', 'Cơ sở dữ liệu', 3, 'BATBUOC', 'Mô hình dữ liệu quan hệ và truy vấn SQL.'),
('IT03', 'Cấu trúc dữ liệu và giải thuật', 3, 'BATBUOC', 'Cấu trúc dữ liệu và thuật toán cơ bản.'),
('IT04', 'Phân tích thiết kế hệ thống', 3, 'BATBUOC', 'Phân tích yêu cầu và thiết kế hệ thống thông tin.'),
('IT05', 'Mạng máy tính', 3, 'BATBUOC', 'Kiến thức nền tảng về mạng máy tính.');

-- Curriculum is preloaded for the sample department before registration opens.
INSERT INTO ChuongTrinhDaoTao (Khoa, MaMon) VALUES
('CNTT', 'IT01'),
('CNTT', 'IT02'),
('CNTT', 'IT03'),
('CNTT', 'IT04'),
('CNTT', 'IT05');

-- The sample registration period is open for 30 days from database setup.
INSERT INTO CauHinhHocKy (HocKy, KetThucHocKy, HanNhapDiem)
VALUES ('2026-2', '2026-12-31', '2027-01-31 23:59:00');

INSERT INTO LopHoc (MaLop, MaMon, MaGV, HocKy, SiSoToiDa) VALUES
('OOP_L01', 'IT01', 'GV01', '2026-2', 40),
('DB_L01', 'IT02', 'GV01', '2026-2', 40),
('DSA_L01', 'IT03', 'GV01', '2026-2', 40),
('SA_L01', 'IT04', 'GV01', '2026-2', 40),
('NET_L01', 'IT05', 'GV01', '2026-2', 40);

INSERT INTO LichHoc (MaLop, Thu, GioBatDau, GioKetThuc, PhongHoc) VALUES
('OOP_L01', 2, '08:00:00', '10:00:00', 'Phòng 101 - Nhà A'),
('DB_L01', 3, '08:00:00', '10:00:00', 'Phòng 202 - Nhà A'),
('DSA_L01', 4, '08:00:00', '10:00:00', 'Phòng 303 - Nhà A'),
('SA_L01', 5, '08:00:00', '10:00:00', 'Phòng 104 - Nhà B'),
('NET_L01', 6, '08:00:00', '10:00:00', 'Phòng 205 - Nhà B');

INSERT INTO ThanhPhanDanhGia (MaMon, TenThanhPhan, TrongSo, ThuTu) VALUES
('IT01', 'Giữa kỳ', 0.30, 1), ('IT01', 'Cuối kỳ', 0.70, 2),
('IT02', 'Giữa kỳ', 0.30, 1), ('IT02', 'Cuối kỳ', 0.70, 2),
('IT03', 'Giữa kỳ', 0.30, 1), ('IT03', 'Cuối kỳ', 0.70, 2),
('IT04', 'Giữa kỳ', 0.30, 1), ('IT04', 'Cuối kỳ', 0.70, 2),
('IT05', 'Giữa kỳ', 0.30, 1), ('IT05', 'Cuối kỳ', 0.70, 2);
