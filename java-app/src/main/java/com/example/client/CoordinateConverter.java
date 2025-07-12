package com.example.client;

import com.example.domain.Hex;

/**
 * Утилитный класс для преобразования гексагональных координат между
 * аксиальной (Axial) системой, используемой в доменной логике, и
 * смещенной системой odd-r (Offset), используемой игровым API.
 * <p>
 * Формулы основаны на эталонном руководстве от Red Blob Games.
 */
public final class CoordinateConverter {

    private CoordinateConverter() {
    }

    /**
     * Преобразует координаты из формата odd-r (нечетные ряды сдвинуты вправо) в аксиальный.
     * Используется для всех данных, ПОЛУЧАЕМЫХ от сервера.
     *
     * @param oddrHex Гекс в формате odd-r (где q=col, r=row).
     * @return Тот же гекс в аксиальном формате.
     */
    public static Hex oddrToAxial(Hex oddrHex) {
        int q = oddrHex.q() - (oddrHex.r() - (oddrHex.r() & 1)) / 2;
        int r = oddrHex.r();
        return new Hex(q, r);
    }

    /**
     * Преобразует координаты из аксиального формата в odd-r (нечетные ряды сдвинуты вправо).
     * Используется для всех данных, ОТПРАВЛЯЕМЫХ на сервер.
     *
     * @param axialHex Гекс в аксиальном формате.
     * @return Тот же гекс в формате odd-r (где q=col, r=row).
     */
    public static Hex axialToOddr(Hex axialHex) {
        int col = axialHex.q() + (axialHex.r() - (axialHex.r() & 1)) / 2;
        int row = axialHex.r();
        return new Hex(col, row);
    }
}
