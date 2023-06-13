package balbucio.banco.model;

import java.util.Date;

public class Transference {

    private String tokenPagante;
    private String tokenRecebedor;
    private long value;
    private long time;

    public Transference(String tokenPagante, String tokenRecebedor, long value) {
        this.tokenPagante = tokenPagante;
        this.tokenRecebedor = tokenRecebedor;
        this.value = value;
        this.time = new Date().getTime();
    }

    public Transference(String tokenPagante, String tokenRecebedor, long value, long time) {
        this.tokenPagante = tokenPagante;
        this.tokenRecebedor = tokenRecebedor;
        this.value = value;
        this.time = time;
    }

    public String getTokenPagante() {
        return tokenPagante;
    }

    public void setTokenPagante(String tokenPagante) {
        this.tokenPagante = tokenPagante;
    }

    public String getTokenRecebedor() {
        return tokenRecebedor;
    }

    public void setTokenRecebedor(String tokenRecebedor) {
        this.tokenRecebedor = tokenRecebedor;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}

