package Model;

import java.util.*;

public abstract class Users {
    protected Integer userID;
    protected String hoTen;
    protected String email;
    protected String passwordHash;
    protected String role;
    protected String maSo;
    protected String khoa;
    protected Date ngayTao;
    protected TrangThaiHoatDong trangThai;

    public Users(Integer userID, String hoTen, String email, String passwordHash,
                 String role, String maSo, String khoa,
                 Date ngayTao, TrangThaiHoatDong trangThai) {
        this.userID = userID;
        this.hoTen = hoTen;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.maSo = maSo;
        this.khoa = khoa;
        this.ngayTao = ngayTao;
        this.trangThai = trangThai;
    }

    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getMaSo() {
        return maSo;
    }

    public void setMaSo(String maSo) {
        this.maSo = maSo;
    }

    public String getKhoa() {
        return khoa;
    }

    public void setKhoa(String khoa) {
        this.khoa = khoa;
    }

    public Date getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(Date ngayTao) {
        this.ngayTao = ngayTao;
    }

    public TrangThaiHoatDong getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiHoatDong trangThai) {
        this.trangThai = trangThai;
    }
}
