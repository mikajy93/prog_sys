package com.sgbd;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class SQLTypeChecker {

    public boolean ifVARCHAR(Object x) {
        return x instanceof String;
    }

    public boolean ifINT(Object x) {
        return x instanceof Integer;
    }

    public boolean ifBIGINT(Object x) {
        return x instanceof Long;
    }

    public boolean ifDECIMAL(Object x) {
        return x instanceof BigDecimal;
    }

    public boolean ifFLOAT(Object x) {
        return x instanceof Float || x instanceof Double || x instanceof Integer;
    }

    public boolean ifDOUBLE(Object x) {
        return x instanceof Double || x instanceof Integer;
    }

    public boolean ifDATE(Object x) {
        return x instanceof Date || x instanceof LocalDate;
    }

    public boolean ifTIME(Object x) {
        return x instanceof Time || x instanceof LocalTime;
    }

    public boolean ifDATETIME(Object x) {
        return x instanceof Timestamp || x instanceof LocalDateTime;
    }

    public boolean ifBOOLEAN(Object x) {
        return x instanceof Boolean;
    }

    public boolean ifBLOB(Object x) {
        return x instanceof byte[];
    }

    public boolean ifUUID(Object x) {
        return x instanceof UUID;
    }

    public boolean ifENUM(Object x) {
        return x.getClass().isEnum();
    }

    public static void main(String[] args) {
        SQLTypeChecker checker = new SQLTypeChecker();

        String str = "Hello, world!";
        Integer intVal = 123;
        Long bigIntVal = 123456789L;
        BigDecimal decimalVal = new BigDecimal("123.45");
        Double floatVal = 12.34;
        Date dateVal = Date.valueOf("2024-12-15");
        LocalDateTime dateTimeVal = LocalDateTime.now();
        Boolean boolVal = true;
        byte[] blobVal = new byte[]{1, 2, 3};
        UUID uuidVal = UUID.randomUUID();

        System.out.println("Is VARCHAR: " + checker.ifVARCHAR(str));  // true
        System.out.println("Is INTEGER: " + checker.ifINT(intVal));  // true
        System.out.println("Is BIGINT: " + checker.ifBIGINT(bigIntVal));  // true
        System.out.println("Is DECIMAL: " + checker.ifDECIMAL(decimalVal));  // true
        System.out.println("Is FLOAT: " + checker.ifFLOAT(floatVal));  // true
        System.out.println("Is DATE: " + checker.ifDATE(dateVal));  // true
        System.out.println("Is DATETIME: " + checker.ifDATETIME(dateTimeVal));  // true
        System.out.println("Is BOOLEAN: " + checker.ifBOOLEAN(boolVal));  // true
        System.out.println("Is BLOB: " + checker.ifBLOB(blobVal));  // true
        System.out.println("Is UUID: " + checker.ifUUID(uuidVal));  // true
    }
}
