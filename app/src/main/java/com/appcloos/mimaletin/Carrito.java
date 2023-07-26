/*Clase....: Carrito
 * Autor.......: PCV MAR 2021
 * Objetivo....: Clase en donde se definen los tipos de datos que pertenecen tablas de los
 * pedidos y a la tabla temporal de los pedidos
 * Notas.......:
 *
 * Parámetros..: -- Ninguno --
 *
 * Modif.......:
 *
 * NOTAS.......: Comparte uso con las tablas ke_carrito, ke_opti y ke_opmv
 *
 * Retorna.....: Ninguno
 *-------------**/



package com.appcloos.mimaletin;

//basicamente se compone de los tipos de variables y sus metodos get and set
public class Carrito {

    String codigo;
    String nombre;
    double tipoprecio;
    Double cantidadInsertar;
    String tipodoc;
    String numerodoc;

    public Double getDctolin() {
        return dctolin;
    }

    public Double getStotNeto() {
        return stotNeto;
    }

    public void setStotNeto(Double stotNeto) {
        this.stotNeto = stotNeto;
    }

    Double stotNeto;

    public void setDctolin(Double dctolin) {
        this.dctolin = dctolin;
    }

    Double dctolin;

    public String getNumerodoc() {
        return numerodoc;
    }

    public void setNumerodoc(String numerodoc) {
        this.numerodoc = numerodoc;
    }

    public String getTipodoc() {
        return tipodoc;
    }

    public void setTipodoc(String tipodoc) {
        this.tipodoc = tipodoc;
    }

    public Double getCantidadInsertar() {
        return cantidadInsertar;
    }

    public void setCantidadInsertar(Double cantidadInsertar) {
        this.cantidadInsertar = cantidadInsertar;
    }

    public double getTipoprecio() {
        return tipoprecio;
    }

    public void setTipoprecio(double tipoprecio) {
        this.tipoprecio = tipoprecio;
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

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    int cantidad;
    Double precio;
    Double preciou;

    public Double getPreciou() {
        return preciou;
    }

    public void setPreciou(Double preciou) {
        this.preciou = preciou;
    }
}
