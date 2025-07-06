export const letters = [
  // Передняя грань (по Z)
  { position: [0, 0, 0.51], rotation: [0, 0, 0] },
  // Задняя грань (по -Z)
  { position: [0, 0, -0.51], rotation: [0, Math.PI, 0] },
  // Правая грань (по X)
  { position: [0.51, 0, 0], rotation: [0, -Math.PI / 2, 0] },
  // Левая грань (по -X)
  { position: [-0.51, 0, 0], rotation: [0, Math.PI / 2, 0] },
  // Верхняя грань (по Y)
  { position: [0, 0.51, 0], rotation: [-Math.PI / 2, 0, 0] },
  // Нижняя грань (по -Y)
  { position: [0, -0.51, 0], rotation: [Math.PI / 2, 0, 0] },
];