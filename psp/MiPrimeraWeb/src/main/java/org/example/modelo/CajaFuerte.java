package org.example.modelo;

import java.util.HashMap;

public class CajaFuerte

{

    private String nombre;
    private String num1;
    private String num2;
    private String num3;
    private boolean  abierta;
    private int start ;
    private  HashMap<String,String> secretos;


    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNum1() {
        return num1;
    }

    public void setNum1(String num1) {
        this.num1 = num1;
    }

    public String getNum2() {
        return num2;
    }

    public void setNum2(String num2) {
        this.num2 = num2;
    }

    public String getNum3() {
        return num3;
    }

    public void setNum3(String num3) {
        this.num3 = num3;
    }

    public boolean isAbierta() {
        return abierta;
    }

    public void setAbierta(boolean abierta) {
        this.abierta = abierta;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public HashMap<String, String> getSecretos() {
        return secretos;
    }

    public void setSecretos(HashMap<String, String> secretos) {
        this.secretos = secretos;
    }

    public CajaFuerte(String nombre, String num1, String num2, String num3, boolean abierta, int start, HashMap<String, String> secretos) {
        this.nombre = nombre;
        this.num1 = num1;
        this.num2 = num2;
        this.num3 = num3;
        this.abierta = abierta;
        this.start = start;
        this.secretos = secretos;
    }

    public CajaFuerte(String nombre,
                      String num1,
                      String num2,
                      String num3)
    {
        this(nombre,num1,num2,num3,false,1,new HashMap<>());


    }
}
