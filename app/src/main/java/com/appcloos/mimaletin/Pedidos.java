package com.appcloos.mimaletin;

public class Pedidos {
    String codigoCliente;
    String codigoVendedor;
    String docSolicitado;
    String condicion;
    String tipoDocumento;
    String numeroDocumento;
    Double tipoPrecio;
    String nombreCliente;
    Double totalNeto;
    String fechaDocumento;
    String numeroPedido;
    Double totalNetoDcto;
    String estatus;

    public Double getTotalNetoDcto() {
        return totalNetoDcto;
    }

    public void setTotalNetoDcto(Double totalNetoDcto) {
        this.totalNetoDcto = totalNetoDcto;
    }

    public String getNumeroPedido() {
        return numeroPedido;
    }

    public void setNumeroPedido(String numeroPedido) {
        this.numeroPedido = numeroPedido;
    }

    public String getCodigoCliente() {
        return codigoCliente;
    }

    public void setCodigoCliente(String codigoCliente) {
        this.codigoCliente = codigoCliente;
    }

    public String getCodigoVendedor() {
        return codigoVendedor;
    }

    public void setCodigoVendedor(String codigoVendedor) {
        this.codigoVendedor = codigoVendedor;
    }

    public String getDocSolicitado() {
        return docSolicitado;
    }

    public void setDocSolicitado(String docSolicitado) {
        this.docSolicitado = docSolicitado;
    }

    public String getCondicion() {
        return condicion;
    }

    public void setCondicion(String condicion) {
        this.condicion = condicion;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public Double getTipoPrecio() {
        return tipoPrecio;
    }

    public void setTipoPrecio(Double tipoPrecio) {
        this.tipoPrecio = tipoPrecio;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public Double getTotalNeto() {
        return totalNeto;
    }

    public void setTotalNeto(Double totalNeto) {
        this.totalNeto = totalNeto;
    }

    public String getFechaDocumento() {
        return fechaDocumento;
    }

    public void setFechaDocumento(String fechaDocumento) {
        this.fechaDocumento = fechaDocumento;
    }

    public String getEstatus() {
        return estatus;
    }

    public void setEstatus(String estatus) {
        this.estatus = estatus;
    }
}