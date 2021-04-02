package com.zkxl.locklibrary.bluetoothlib.handler.bean;

public class LockMessageBean {
    private String NAME;
    private String APID;
    private byte FUN;
    private byte BR;
    private byte HBT;
    private byte MWT;
    private String ADDR;
    private String CH;

    public LockMessageBean( String NAME, String APID, byte FUN, byte BR, byte HBT, byte MWT, String ADDR, String CH) {
        this.NAME = NAME;
        this.APID = APID;
        this.FUN = FUN;
        this.BR = BR;
        this.HBT = HBT;
        this.MWT = MWT;
        this.ADDR = ADDR;
        this.CH = CH;
    }



    public String getNAME() {
        return NAME;
    }

    public void setNAME(String NAME) {
        this.NAME = NAME;
    }

    public String getAPID() {
        return APID;
    }

    public void setAPID(String APID) {
        this.APID = APID;
    }

    public byte getFUN() {
        return FUN;
    }

    public void setFUN(byte FUN) {
        this.FUN = FUN;
    }

    public byte getBR() {
        return BR;
    }

    public void setBR(byte BR) {
        this.BR = BR;
    }

    public byte getHBT() {
        return HBT;
    }

    public void setHBT(byte HBT) {
        this.HBT = HBT;
    }

    public byte getMWT() {
        return MWT;
    }

    public void setMWT(byte MWT) {
        this.MWT = MWT;
    }

    public String getADDR() {
        return ADDR;
    }

    public void setADDR(String ADDR) {
        this.ADDR = ADDR;
    }

    public String getCH() {
        return CH;
    }

    public void setCH(String CH) {
        this.CH = CH;
    }

}
