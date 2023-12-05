package de.androidcrypto.firebaseuitutorial.utils;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Build;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {

    // gives an 19 byte long timestamp yyyy.MM.dd HH:mm:ss
    public static String getUtcTimestamp() {
        // gives a 19 character long string
        ZonedDateTime zonedDateTime = ZonedDateTime.);
        return getActualUtcZonedDateTime()
                .format(DateTimeFormatter.ofPattern("uuuu.MM.dd HH:mm:ss"));
    }

    public static String getTimestamp() {
        // gives a 19 character long string
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return ZonedDateTime
                    .now(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("uuuu.MM.dd HH:mm:ss"));
        } else {
            return new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new Date());
        }
    }

    /**
     * Important: as Instant is available on Android 26+ you need to add a backwards library to run this on older Android versions
     */
    /*
    we need to setup an option in build.gradle (app)_
    ...
    compileOptions {
        // Flag to enable support for the new language APIs, important for timestamps
        coreLibraryDesugaringEnabled true
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    ...
    dependencies {
        [...]
        coreLibraryDesugaring 'com.android.tools:desugar_jdk_libs:1.0.9'
        [...]
    }
     */

    public static byte[] getActualInstant8Bytes() {
        return longTo8Bytes(getActualInstant());
    }
    public static long getActualInstant() {
        Instant instant = Instant.now();
        return instant.getEpochSecond();
    }

    /**
     * the getTimestampString methods return a String like '2023 09 07 16:27:23' or '20230907 165047',
     * inputs can be a <long> timestamp or <byte[]> timestamp8Bytes
     */

    public static String getTimestampString19Chars(byte[] timestamp8Bytes) {
        if ((timestamp8Bytes == null) || (timestamp8Bytes.length != 8)) return "";
        return getTimestampString19Chars(byte8ArrayToLong(timestamp8Bytes));
    }

    @SuppressLint("SimpleDateFormat")
    public static String getTimestampString19Chars(long timestamp) {
        java.sql.Date date = new java.sql.Date(timestamp * 1000);
        return new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(date);
    }

    public static String getTimestampString15Chars(byte[] timestamp8Bytes) {
        if ((timestamp8Bytes == null) || (timestamp8Bytes.length != 8)) return "";
        return getTimestampString15Chars(byte8ArrayToLong(timestamp8Bytes));
    }

    @SuppressLint("SimpleDateFormat")
    public static String getTimestampString15Chars(long timestamp) {
        java.sql.Date date = new java.sql.Date(timestamp * 1000);
        return new SimpleDateFormat("yyyyMMdd HHmmss").format(date);
    }

    // the following methods work with ZonedDateTime

    // the ZoneId can be 'ZoneId utcTimeZone = ZoneId.of("UTC");'
    // or get the device's ZoneId: getDevicesZoneId();

    public static byte[] getActualZonedDateTime8Bytes() {
        return longTo8Bytes(getActualZonedDateTime(getDevicesZoneId()));
    }

    public static byte[] getActualZonedDateTime8Bytes(ZoneId zoneId) {
        return longTo8Bytes(getActualZonedDateTime(zoneId));
    }

    /**
     * get the UTC timestamp as ZonedDateTime
     * @return long timestamp
     */
    public static long getActualUtcZonedDateTime() {
        return getActualZonedDateTime(ZoneId.of("UTC"));
    }

    public static long getActualZonedDateTime(ZoneId zoneId) {
        ZonedDateTime zonedDateTime = ZonedDateTime.now(zoneId);
        return zonedDateTime.toEpochSecond();
    }

    /**
     * get a formatted date/time string, based on DevicesLocale and DevicesZoneId
     * @param timestamp
     * @return e.g. for Europe/Germany Summertime: '07.09.2023 17:32:37'
     */
    public static String getZoneDatedStringMediumLocale (long timestamp) {
        return getZoneDatedStringMedium(timestamp, getDevicesZoneId(), getDevicesLocale());
    }

    public static String getZoneDatedStringShortLocale (long timestamp) {
        return getZoneDatedStringShort(timestamp, getDevicesZoneId(), getDevicesLocale());
    }

    // returns for Europe/Germany Summertime: '07.09.23 17:32'
    public static String getZoneDatedStringShortDefault(byte[] instant8Bytes) {
        return getZoneDatedStringShort(instant8Bytes, getDevicesZoneId(), getDevicesLocale());
    }

    // returns for Europe/Germany Summertime: '07.09.2023 17:32:37'
    public static String getZoneDatedStringMediumDefault(byte[] instant8Bytes) {
        return getZoneDatedStringMedium(instant8Bytes, getDevicesZoneId(), getDevicesLocale());
    }

    // returns for Europe/Germany Summertime: '7. September 2023 17:32:37 MESZ'
    public static String getZoneDatedStringLongDefault(byte[] instant8Bytes) {
        return getZoneDatedStringLong(instant8Bytes, getDevicesZoneId(), getDevicesLocale());
    }

    // returns for Europe/Germany Summertime: 'Donnerstag, 7. September 2023 17:32:37 Mitteleuropaeische Sommerzeit'
    public static String getZoneDatedStringFullDefault(byte[] instant8Bytes) {
        return getZoneDatedStringFull(instant8Bytes, getDevicesZoneId(), getDevicesLocale());
    }


    public static String getZoneDatedStringShort(byte[] instant8Bytes, ZoneId zoneId, Locale locale) {
        if ((instant8Bytes == null) || (instant8Bytes.length != 8)) return "";
        return getZoneDatedStringShort(byte8ArrayToLong(instant8Bytes), zoneId, locale);
    }

    public static String getZoneDatedStringShort(long instantLong, ZoneId zoneId, Locale locale) {
        Instant instant = Instant.ofEpochSecond(instantLong);
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                .withLocale(locale);
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, zoneId);
        return zonedDateTime.format(formatter);
    }

    public static String getZoneDatedStringMedium(byte[] instant8Bytes, ZoneId zoneId, Locale locale) {
        if ((instant8Bytes == null) || (instant8Bytes.length != 8)) return "";
        return getZoneDatedStringMedium(byte8ArrayToLong(instant8Bytes), zoneId, locale);
    }

    public static String getZoneDatedStringMedium(long instantLong, ZoneId zoneId, Locale locale) {
        Instant instant = Instant.ofEpochSecond(instantLong);
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(locale);
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, zoneId);
        return zonedDateTime.format(formatter);
    }

    public static String getZoneDatedStringLong(byte[] instant8Bytes, ZoneId zoneId, Locale locale) {
        if ((instant8Bytes == null) || (instant8Bytes.length != 8)) return "";
        return getZoneDatedStringLong(byte8ArrayToLong(instant8Bytes), zoneId, locale);
    }

    public static String getZoneDatedStringLong(long instantLong, ZoneId zoneId, Locale locale) {
        Instant instant = Instant.ofEpochSecond(instantLong);
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG)
                .withLocale(locale);
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, zoneId);
        return zonedDateTime.format(formatter);
    }

    public static String getZoneDatedStringFull(byte[] instant8Bytes, ZoneId zoneId, Locale locale) {
        if ((instant8Bytes == null) || (instant8Bytes.length != 8)) return "";
        return getZoneDatedStringFull(byte8ArrayToLong(instant8Bytes), zoneId, locale);
    }

    public static String getZoneDatedStringFull(long instantLong, ZoneId zoneId, Locale locale) {
        Instant instant = Instant.ofEpochSecond(instantLong);
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL)
                .withLocale(locale);
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, zoneId);
        return zonedDateTime.format(formatter);
    }

    // get the  device's ZoneId
    private static ZoneId getDevicesZoneId() {
        return ZoneId.systemDefault();
    }

    // this is the locale of the Android device, not the app
    private static Locale getDevicesLocale(){
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = Resources.getSystem().getConfiguration().getLocales().get(0);
        } else {
            //noinspection deprecation
            locale = Resources.getSystem().getConfiguration().locale;
        }
        return locale;
    }

    public static byte[] longTo8Bytes(long l) {
        final int LongBYTES = 8;
        byte[] result = new byte[LongBYTES];
        for (int i = LongBYTES - 1; i >= 0; i--) {
            result[i] = (byte)(l & 0xFF);
            l >>= Byte.SIZE;
        }
        return result;
    }

    public static long byte8ArrayToLong(final byte[] b) {
        final int LongBYTES = 8;
        long result = 0;
        for (int i = 0; i < LongBYTES; i++) {
            result <<= Byte.SIZE;
            result |= (b[i] & 0xFF);
        }
        return result;
    }

}
