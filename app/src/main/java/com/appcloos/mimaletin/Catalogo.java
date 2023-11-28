/*Clase....: Catalogo
 * Autor.......: PCV MAR 2021
 * Objetivo....: Clase en donde se definen los tipos de datos que pertenecen tablas de los
 * articulos
 * Notas.......:
 *
 * Parámetros..: -- Ninguno --
 *
 * Modif.......:
 *
 * NOTAS.......: usado para manipular datos de la tabla de articulos
 *
 * Retorna.....: Ninguno
 *-------------**/


package com.appcloos.mimaletin;

//basicamente se compone de los tipos de variables y sus metodos get and set
public class Catalogo {


    String codigo;
    String sungrupo;
    String grupo;
    String nombre;
    String referencia;
    String marca;
    String unidad;
    Double precio1;
    Double precio2;
    Double precio3;
    Double precio4;
    Double precio5;
    Double precio6;
    Double precio7;
    int existencia;
    String fechamodifi;
    String codigoKardex;
    Double vta_min;
    String enpreventa;
    int multiplo;
    int vta_solofac;
    int vta_solone;
    Double dctotope;
    Double vta_max;
    int ActDirec;
    Double discont;

    public Catalogo() {

    }

    public int getVta_solofac() {
        return vta_solofac;
    }

    public void setVta_solofac(int vta_solofac) {
        this.vta_solofac = vta_solofac;
    }

    public int getVta_solone() {
        return vta_solone;
    }

    public void setVta_solone(int vta_solone) {
        this.vta_solone = vta_solone;
    }

    public int getMultiplo() {
        return multiplo;
    }

    public void setMultiplo(int multiplo) {
        this.multiplo = multiplo;
    }

    public String getEnpreventa() {
        return enpreventa;
    }

    public void setEnpreventa(String enpreventa) {
        this.enpreventa = enpreventa;
    }

    public Double getDctotope() {
        return dctotope;
    }

    public void setDctotope(Double dctotope) {
        this.dctotope = dctotope;
    }

    public Double getVta_min() {
        return vta_min;
    }

    public void setVta_min(Double vta_min) {
        this.vta_min = vta_min;
    }

    public Double getVta_max() {
        return vta_max;
    }

    public void setVta_max(Double vta_max) {
        this.vta_max = vta_max;
    }

    public String getCodigoKardex() {
        return codigoKardex;
    }

    public void setCodigoKardex(String codigoKardex) {
        this.codigoKardex = codigoKardex;
    }

    public int getActDirec() {
        return ActDirec;
    }

    public void setActDirec(int actDirec) {
        ActDirec = actDirec;
    }

    public String getSungrupo() {
        return sungrupo;
    }

    public void setSungrupo(String sungrupo) {
        this.sungrupo = sungrupo;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public Double getPrecio2() {
        return precio2;
    }

    public void setPrecio2(Double precio2) {
        this.precio2 = precio2;
    }

    public Double getPrecio3() {
        return precio3;
    }

    public void setPrecio3(Double precio3) {
        this.precio3 = precio3;
    }

    public Double getPrecio4() {
        return precio4;
    }

    public void setPrecio4(Double precio4) {
        this.precio4 = precio4;
    }

    public Double getPrecio5() {
        return precio5;
    }

    public void setPrecio5(Double precio5) {
        this.precio5 = precio5;
    }

    public Double getPrecio6() {
        return precio6;
    }

    public void setPrecio6(Double precio6) {
        this.precio6 = precio6;
    }

    public Double getPrecio7() {
        return precio7;
    }

    public void setPrecio7(Double precio7) {
        this.precio7 = precio7;
    }

    public Double getDiscont() {
        return discont;
    }

    public void setDiscont(Double discont) {
        this.discont = discont;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double getPrecio1() {
        return precio1;
    }

    public void setPrecio1(Double precio1) {
        this.precio1 = precio1;
    }

    public int getExistencia() {
        return existencia;
    }

    public void setExistencia(int existencia) {
        this.existencia = existencia;
    }

    public String getFechamodifi() {
        return fechamodifi;
    }

    public void setFechamodifi(String fechamodifi) {
        this.fechamodifi = fechamodifi;
    }
}
