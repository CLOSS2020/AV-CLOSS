package com.appcloos.mimaletin;

public class Cliente {

    String codigo;
    String nombre;
    String direccion;
    String telefonos;
    String perscont;
    String vendedor;
    Double contribespecial;
    Double status;
    String sector;
    String subcodigo;
    String fechamodifi;

    int kne_activa;

    public int getKne_activa() {
        return kne_activa;
    }

    public void setKne_activa(int kne_activa) {
        this.kne_activa = kne_activa;
    }

    public Cliente() {

    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefonos() {
        return telefonos;
    }

    public void setTelefonos(String telefonos) {
        this.telefonos = telefonos;
    }

    public String getPerscont() {
        return perscont;
    }

    public void setPerscont(String perscont) {
        this.perscont = perscont;
    }

    public String getVendedor() {
        return vendedor;
    }

    public void setVendedor(String vendedor) {
        this.vendedor = vendedor;
    }

    public Double getContribespecial() {
        return contribespecial;
    }

    public void setContribespecial(Double contribespecial) {
        this.contribespecial = contribespecial;
    }

    public Double getStatus() {
        return status;
    }

    public void setStatus(Double status) {
        this.status = status;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getSubcodigo() {
        return subcodigo;
    }

    public void setSubcodigo(String subcodigo) {
        this.subcodigo = subcodigo;
    }

    public String getFechamodifi() {
        return fechamodifi;
    }

    public void setFechamodifi(String fechamodifi) {
        this.fechamodifi = fechamodifi;
    }
}
