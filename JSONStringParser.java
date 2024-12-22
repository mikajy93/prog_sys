package com.dbmanager;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class JSONStringParser {

    public static Object parseValue(String value, String type) {
        switch (type.toUpperCase()) {
            case "VARCHAR":
                return parseVarchar(value);
            case "INT":
                return parseInteger(value);
            case "BIGINT":
                return parseBigInt(value);
            case "DECIMAL":
                return parseDecimal(value);
            case "DOUBLE":
                return parseDouble(value);
            case "FLOAT":
                return parseFloat(value);
            case "DATE":
                return parseDate(value);
            case "DATETIME":
                return parseDateTime(value);
            case "BOOLEAN":
                return parseBoolean(value);
            case "BLOB":
                return parseBlob(value);
            case "UUID":
                return parseUUID(value);
            default:
                throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

    // Parsing pour VARCHAR
    private static String parseVarchar(String value) {
        return value;
    }

    // Parsing pour INTEGER
    private static Integer parseInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid INT format: " + value, e);
        }
    }

    // Parsing pour BIGINT
    private static Long parseBigInt(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid BIGINT format: " + value, e);
        }
    }

    // Parsing pour DECIMAL
    private static BigDecimal parseDecimal(String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid DECIMAL format: " + value, e);
        }
    }

    // Parsing pour FLOAT
    private static Float parseFloat(String value) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid FLOAT format: " + value, e);
        }
    }

    // Parsing pour DOUBLE
    private static Double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid DOUBLE format: " + value, e);
        }
    }

    // Parsing pour DATE
    private static Date parseDate(String value) {
        return parseDateWithFormat(value, "yyyy-MM-dd");
    }

    // Parsing pour DATETIME
    private static Date parseDateTime(String value) {
        return parseDateWithFormat(value, "yyyy-MM-dd'T'HH:mm:ss");
    }

    private static Date parseDateWithFormat(String value, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            sdf.setLenient(false);
            return sdf.parse(value);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid DATE/DATETIME format: " + value, e);
        }
    }

    // Parsing pour BOOLEAN
    private static Boolean parseBoolean(String value) {
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value);
        }
        throw new IllegalArgumentException("Invalid BOOLEAN format: " + value);
    }

    // Parsing pour BLOB
    private static byte[] parseBlob(String value) {
        try {
            return value.getBytes("UTF-8");
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid BLOB format: " + value, e);
        }
    }

    // Parsing pour UUID
    private static UUID parseUUID(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format: " + value, e);
        }
    }

    // Exemple d'utilisation
    public static void main(String[] args) {
        System.out.println(parseValue("Hello, World!", "VARCHAR"));   // Hello, World!
        System.out.println(parseValue("123", "INT"));             // 123
        System.out.println(parseValue("9223372036854775807", "BIGINT")); // 9223372036854775807
        System.out.println(parseValue("12345.678", "DECIMAL"));       // 12345.678
        System.out.println(parseValue("123.45", "FLOAT"));            // 123.45
        System.out.println(parseValue("2024-12-15", "DATE"));         // Date Object
        System.out.println(parseValue("2024-12-15T10:30:45", "DATETIME")); // DateTime Object
        System.out.println(parseValue("true", "BOOLEAN"));            // true
        System.out.println(parseValue("TestBlob", "BLOB"));           // [B@hexcode
        System.out.println(parseValue("123e4567-e89b-12d3-a456-426614174000", "UUID")); // UUID Object

        try {
            System.out.println(parseValue("invalidUUID", "UUID"));    // Exception
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
