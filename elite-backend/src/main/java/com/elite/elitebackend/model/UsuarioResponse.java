package com.elite.elitebackend.model;

public class UsuarioResponse {
    private Integer id;
    private String userName;
    private String roll;
    private Integer idCliente;

    public UsuarioResponse() {
    }

    public UsuarioResponse(Integer id, String userName, String roll, Integer idCliente) {
        this.id = id;
        this.userName = userName;
        this.roll = roll;
        this.idCliente = idCliente;
    }

    public UsuarioResponse(Usuario usuario) {
        this.id = usuario.getId();
        this.userName = usuario.getUsername();
        this.roll = usuario.getRoll();
        this.idCliente = usuario.getCliente() != null ? usuario.getCliente().getId() : null;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRoll() {
        return roll;
    }

    public void setRoll(String roll) {
        this.roll = roll;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }
}
