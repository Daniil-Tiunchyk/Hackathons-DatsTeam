// types.ts
export interface Hex {
  q: number;
  r: number;
}

// coordinateConverter.ts
export class CoordinateConverter {
  /**
   * Преобразует координаты из формата odd-r в аксиальный (для данных от сервера)
   * @param oddrHex Гекс в формате odd-r (q=col, r=row)
   * @returns Тот же гекс в аксиальном формате
   */
  static oddrToAxial(oddrHex: Hex): Hex {
    const q = oddrHex.q - (oddrHex.r - (oddrHex.r & 1)) / 2;
    const r = oddrHex.r;
    return { q, r };
  }

  /**
   * Преобразует координаты из аксиального формата в odd-r (для данных на сервер)
   * @param axialHex Гекс в аксиальном формате
   * @returns Тот же гекс в формате odd-r (q=col, r=row)
   */
  static axialToOddr(axialHex: Hex): Hex {
    const col = axialHex.q + (axialHex.r - (axialHex.r & 1)) / 2;
    const row = axialHex.r;
    return { q: col, r: row };
  }
}
