CREATE DATABASE IF NOT EXISTS thi_thltm
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE thi_thltm;

DROP TABLE IF EXISTS phan_cong_chi_tiet;
DROP TABLE IF EXISTS phong_thi_phan_cong;
DROP TABLE IF EXISTS dot_phan_cong;
DROP TABLE IF EXISTS phong_thi_nguon;
DROP TABLE IF EXISTS can_bo_coi_thi;

CREATE TABLE can_bo_coi_thi (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  tt_nguon INT NOT NULL,
  ma_gv VARCHAR(32) NOT NULL,
  ho_ten VARCHAR(255) NOT NULL,
  ngay_sinh DATE DEFAULT NULL,
  don_vi_cong_tac VARCHAR(255) DEFAULT NULL,
  source_row INT DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_can_bo_ma_gv (ma_gv),
  KEY idx_can_bo_ho_ten (ho_ten),
  KEY idx_can_bo_ngay_sinh (ngay_sinh)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE phong_thi_nguon (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  stt_nguon INT NOT NULL,
  phong_thi VARCHAR(50) NOT NULL,
  ghi_chu VARCHAR(255) DEFAULT NULL,
  source_row INT DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_phong_thi_gia_tri (phong_thi),
  KEY idx_phong_thi_stt (stt_nguon)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE dot_phan_cong (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  ma_dot VARCHAR(50) NOT NULL,
  ten_dot VARCHAR(255) NOT NULL,
  so_giam_thi INT NOT NULL,
  so_phong_thi INT NOT NULL,
  file_xlsx_can_bo VARCHAR(255) DEFAULT NULL,
  file_xlsx_phong_thi VARCHAR(255) DEFAULT NULL,
  trang_thai ENUM('NEW','PROCESSING','DONE','ERROR') NOT NULL DEFAULT 'NEW',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_dot_ma_dot (ma_dot),
  KEY idx_dot_trang_thai (trang_thai),
  KEY idx_dot_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE phong_thi_phan_cong (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  dot_phan_cong_id BIGINT UNSIGNED NOT NULL,
  phong_thi_nguon_id BIGINT UNSIGNED NOT NULL,
  so_phong INT NOT NULL,
  so_luong_can_bo INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_dot_phong (dot_phan_cong_id, phong_thi_nguon_id),
  UNIQUE KEY uq_dot_so_phong (dot_phan_cong_id, so_phong),
  KEY idx_ptpc_dot (dot_phan_cong_id),
  KEY idx_ptpc_phong_nguon (phong_thi_nguon_id),
  CONSTRAINT fk_ptpc_dot
    FOREIGN KEY (dot_phan_cong_id) REFERENCES dot_phan_cong(id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_ptpc_phong_nguon
    FOREIGN KEY (phong_thi_nguon_id) REFERENCES phong_thi_nguon(id)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE phan_cong_chi_tiet (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  dot_phan_cong_id BIGINT UNSIGNED NOT NULL,
  phong_thi_phan_cong_id BIGINT UNSIGNED NOT NULL,
  can_bo_id BIGINT UNSIGNED NOT NULL,
  role ENUM('GIAMTHI1','GIAMTHI2','GIAMSAT') NOT NULL DEFAULT 'GIAMTHI1',
  vi_tri_trong_phong INT NOT NULL,
  range_text VARCHAR(255) DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_dot_can_bo (dot_phan_cong_id, can_bo_id),
  UNIQUE KEY uq_phong_vi_tri (phong_thi_phan_cong_id, vi_tri_trong_phong),
  KEY idx_pc_role (role),
  KEY idx_pc_dot (dot_phan_cong_id),
  KEY idx_pc_phong (phong_thi_phan_cong_id),
  KEY idx_pc_can_bo (can_bo_id),
  CONSTRAINT fk_pc_dot
    FOREIGN KEY (dot_phan_cong_id) REFERENCES dot_phan_cong(id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_pc_phong
    FOREIGN KEY (phong_thi_phan_cong_id) REFERENCES phong_thi_phan_cong(id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_pc_can_bo
    FOREIGN KEY (can_bo_id) REFERENCES can_bo_coi_thi(id)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
