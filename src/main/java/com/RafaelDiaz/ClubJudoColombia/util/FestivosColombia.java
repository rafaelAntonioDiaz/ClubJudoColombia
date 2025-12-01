package com.RafaelDiaz.ClubJudoColombia.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class FestivosColombia {

    private FestivosColombia() {}

    public static boolean esFestivo(LocalDate fecha) {
        return nombreFestivo(fecha).isPresent();
    }

    /**
     * Devuelve la CLAVE de traducción del festivo (ej: "festivo.navidad").
     */
    public static Optional<String> nombreFestivo(LocalDate fecha) {
        Map<LocalDate, String> festivos = festivosPorAnio(fecha.getYear());
        return Optional.ofNullable(festivos.get(fecha));
    }

    public static Map<LocalDate, String> festivosPorAnio(int year) {
        Map<LocalDate, String> res = new LinkedHashMap<>();

        // Fijos
        put(res, LocalDate.of(year, Month.JANUARY, 1), "festivo.ano_nuevo");
        put(res, LocalDate.of(year, Month.MAY, 1), "festivo.dia_trabajo");
        put(res, LocalDate.of(year, Month.JULY, 20), "festivo.independencia");
        put(res, LocalDate.of(year, Month.AUGUST, 7), "festivo.batalla_boyaca");
        put(res, LocalDate.of(year, Month.DECEMBER, 8), "festivo.inmaculada");
        put(res, LocalDate.of(year, Month.DECEMBER, 25), "festivo.navidad");

        // Pascua
        LocalDate pascua = pascuaOccidental(year);
        put(res, pascua.minusDays(3), "festivo.jueves_santo");
        put(res, pascua.minusDays(2), "festivo.viernes_santo");

        // Emiliani (Traslado a Lunes)
        trasladarALunes(res, LocalDate.of(year, Month.JANUARY, 6), "festivo.reyes_magos");
        trasladarALunes(res, LocalDate.of(year, Month.MARCH, 19), "festivo.san_jose");
        trasladarALunes(res, LocalDate.of(year, Month.JUNE, 29), "festivo.san_pedro");
        trasladarALunes(res, LocalDate.of(year, Month.AUGUST, 15), "festivo.asuncion");
        trasladarALunes(res, LocalDate.of(year, Month.OCTOBER, 12), "festivo.dia_raza");
        trasladarALunes(res, LocalDate.of(year, Month.NOVEMBER, 1), "festivo.todos_santos");
        trasladarALunes(res, LocalDate.of(year, Month.NOVEMBER, 11), "festivo.independencia_cartagena");

        // Móviles Pascua
        trasladarALunes(res, pascua.plusDays(39), "festivo.ascension");
        trasladarALunes(res, pascua.plusDays(60), "festivo.corpus_christi");
        trasladarALunes(res, pascua.plusDays(68), "festivo.sagrado_corazon");

        return res;
    }

    private static void trasladarALunes(Map<LocalDate, String> mapa, LocalDate fecha, String clave) {
        LocalDate observancia = (fecha.getDayOfWeek() == DayOfWeek.MONDAY)
                ? fecha
                : fecha.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        put(mapa, observancia, clave);
    }

    private static void put(Map<LocalDate, String> mapa, LocalDate fecha, String clave) {
        mapa.put(fecha, clave);
    }

    private static LocalDate pascuaOccidental(int year) {
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * m + 114) / 31;
        int day = ((h + l - 7 * m + 114) % 31) + 1;
        return LocalDate.of(year, month, day);
    }
}