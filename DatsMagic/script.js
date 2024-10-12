const canvas = document.getElementById("mapCanvas");
const ctx = canvas.getContext("2d");
let jsonData;
let jsonDataRequest;
let scale = 0.1;
const maxScale = 16;
const minScale = 0.1;
let offsetX = 0;
let offsetY = 0;
let isDragging = false;
let startX, startY;

const mapSize = { x: 15000, y: 15000 }; // Реальные размеры карты

// Загрузка JSON данных
fetch("response.json")
  .then((response) => response.json())
  .then((data) => {
    jsonData = data;
    drawMap();
  })
  .catch((error) => console.error("Error loading JSON:", error));

function updateMap() {
  fetch("request.json")
    .then((response) => response.json())
    .then((data) => {
      jsonDataRequest = data;
      drawAttackCircles(jsonDataRequest);
    })
    .catch((error) => console.error("Error loading JSON:", error));
  fetch("response.json")
    .then((response) => response.json())
    .then((data) => {
      jsonData = data;
      drawMap();
      updatePointsDisplay();
    })
    .catch((error) => console.error("Error loading JSON:", error));
}

function drawAttackCircles(data) {
  const radius = 30;

  data.transports.forEach((transport) => {
    if (transport.attack && transport.attackCooldownMs >= 8500) {
      const x = transport.attack.x * scale + offsetX;
      const y = transport.attack.y * scale + offsetY;

      // Рисуем круг атаки
      ctx.beginPath();
      ctx.arc(x, y, radius, 0, Math.PI * 2, false);
      ctx.fillStyle = "rgba(255, 255, 255, 0.5)"; // Полупрозрачный белый
      ctx.fill();
      ctx.strokeStyle = "red";
      ctx.stroke();
    }
  });
}

function updatePointsDisplay() {
  const pointsDisplay = document.getElementById("pointsDisplay");
  if (jsonData && jsonData.points !== undefined) {
    pointsDisplay.innerText = `Points: ${jsonData.points}`;
  }
}

// Установка интервала обновления карты
setInterval(updateMap, 500); // Обновление каждые 500 миллисекунд

function drawMap() {
  ctx.clearRect(0, 0, canvas.width, canvas.height);

  ctx.save();
  ctx.translate(offsetX, offsetY);
  ctx.scale(scale, scale);

  drawGrid();
  if (jsonData) {
    drawAnomalies(jsonData.anomalies);
    drawBounties(jsonData.bounties);
    drawEnemies(jsonData.enemies);
    drawTransports(jsonData.transports);
  }

  ctx.restore();
}

// Функция для рисования сетки
function drawGrid() {
  const gridSpacing = 1000;
  ctx.strokeStyle = "#ddd";
  ctx.lineWidth = 0.5;

  // Горизонтальные и вертикальные линии
  for (let i = 0; i <= mapSize.x; i += gridSpacing) {
    ctx.beginPath();
    ctx.moveTo(i, 0);
    ctx.lineTo(i, mapSize.y);
    ctx.stroke();
    ctx.moveTo(0, i);
    ctx.lineTo(mapSize.x, i);
    ctx.stroke();
  }
}

// Рисуем аномалии
function drawAnomalies(anomalies) {
  anomalies.forEach((anomaly) => {
    ctx.fillStyle = "rgba(0, 0, 255, 0.1)";
    ctx.beginPath();
    ctx.arc(anomaly.x, anomaly.y, anomaly.effectiveRadius, 0, Math.PI * 2);
    ctx.fill();

    ctx.fillStyle = "rgba(0, 0, 255, 0.2)";
    ctx.beginPath();
    ctx.arc(anomaly.x, anomaly.y, anomaly.radius, 0, Math.PI * 2);
    ctx.fill();
    drawArrow(
      anomaly.x,
      anomaly.y,
      anomaly.x + anomaly.velocity.x * 20,
      anomaly.y + anomaly.velocity.y * 20,
      "blue"
    );
  });
}

// Рисуем баунти
function drawBounties(bounties) {
  ctx.fillStyle = "rgb(255,255,0)";
  bounties.forEach((bounty) => {
    ctx.beginPath();
    ctx.arc(bounty.x, bounty.y, bounty.radius, 0, Math.PI * 2);
    ctx.fill();
  });
}

// Рисуем врагов
function drawEnemies(enemies) {
  enemies.forEach((enemy) => {
    ctx.fillStyle = "rgba(255, 0, 0, 0.2)";
    ctx.beginPath();
    ctx.arc(enemy.x, enemy.y, 200, 0, Math.PI * 2);
    ctx.fill();

    ctx.fillStyle = "rgba(255, 0, 0, 0.6)";
    drawRoundedSquare(enemy.x - 7.5, enemy.y - 7.5, 15, 5); // Маленький красный квадрат
    ctx.fill();
    drawArrow(
      enemy.x,
      enemy.y,
      enemy.x + enemy.velocity.x * 20,
      enemy.y + enemy.velocity.y * 20,
      "red"
    );

    // Отображение состояния щита врага
    if (enemy.shieldLeftMs > 0) {
      ctx.strokeStyle = "cyan";
      ctx.lineWidth = 2;
      ctx.beginPath();
      ctx.arc(enemy.x, enemy.y, 20, 0, Math.PI * 2);
      ctx.stroke();
    }

    ctx.fillStyle = "white";
    ctx.font = "12px Arial";
    ctx.fillText(`HP: ${enemy.health}`, enemy.x + 20, enemy.y);
  });
}

// Рисуем транспорты
function drawTransports(transports) {
  transports.forEach((transport) => {
    ctx.fillStyle = "rgba(0, 255, 0, 0.2)";
    ctx.beginPath();
    ctx.arc(transport.x, transport.y, 400, 0, Math.PI * 2);
    ctx.fill();

    ctx.fillStyle = "rgba(0, 255, 0, 0.4)";
    ctx.beginPath();
    ctx.arc(transport.x, transport.y, 200, 0, Math.PI * 2);
    ctx.fill();

    ctx.fillStyle = "rgba(0, 255, 0, 0.6)";
    drawRoundedSquare(transport.x - 5, transport.y - 5, 10, 3); // Маленький зелёный квадрат
    ctx.fill();
    drawArrow(
      transport.x,
      transport.y,
      transport.x + transport.velocity.x * 20,
      transport.y + transport.velocity.y * 20,
      "white"
    );

    // Отображение состояния щита транспорта
    if (transport.shieldLeftMs > 0) {
      ctx.strokeStyle = "cyan";
      ctx.lineWidth = 2;
      ctx.beginPath();
      ctx.arc(transport.x, transport.y, 20, 0, Math.PI * 2);
      ctx.stroke();
    }

    ctx.fillStyle = "white";
    ctx.font = "12px Arial";
    const speed = Math.ceil(
      Math.sqrt(
        transport.velocity.x * transport.velocity.x +
          transport.velocity.y * transport.velocity.y
      )
    );

    ctx.fillText(
      `HP: ${transport.health}  КД: ${
        transport.attackCooldownMs / 1000
      }  C: ${speed}`,
      transport.x + 20,
      transport.y
    );
  });
}

// Функция для рисования квадрата со скруглёнными краями
function drawRoundedSquare(x, y, size, radius) {
  ctx.beginPath();
  ctx.moveTo(x + radius, y);
  ctx.lineTo(x + size - radius, y);
  ctx.quadraticCurveTo(x + size, y, x + size, y + radius);
  ctx.lineTo(x + size, y + size - radius);
  ctx.quadraticCurveTo(x + size, y + size, x + size - radius, y + size);
  ctx.lineTo(x + radius, y + size);
  ctx.quadraticCurveTo(x, y + size, x, y + size - radius);
  ctx.lineTo(x, y + radius);
  ctx.quadraticCurveTo(x, y, x + radius, y);
  ctx.closePath();
}

// Функция для рисования стрелки
function drawArrow(fromX, fromY, toX, toY, color) {
  const headLength = 10;
  const dx = toX - fromX;
  const dy = toY - fromY;
  const angle = Math.atan2(dy, dx);

  ctx.strokeStyle = color;
  ctx.lineWidth = 2;

  ctx.beginPath();
  ctx.moveTo(fromX, fromY);
  ctx.lineTo(toX, toY);
  ctx.stroke();

  ctx.beginPath();
  ctx.moveTo(toX, toY);
  ctx.lineTo(
    toX - headLength * Math.cos(angle - Math.PI / 6),
    toY - headLength * Math.sin(angle - Math.PI / 6)
  );
  ctx.lineTo(
    toX - headLength * Math.cos(angle + Math.PI / 6),
    toY - headLength * Math.sin(angle + Math.PI / 6)
  );
  ctx.lineTo(toX, toY);
  ctx.fillStyle = color;
  ctx.fill();
}

// Ограничение смещения карты по границам
function clampOffsets() {
  const maxOffsetX = 0;
  const maxOffsetY = 0;
  const minOffsetX = canvas.width - mapSize.x * scale;
  const minOffsetY = canvas.height - mapSize.y * scale;

  offsetX = Math.min(maxOffsetX, Math.max(minOffsetX, offsetX));
  offsetY = Math.min(maxOffsetY, Math.max(minOffsetY, offsetY));
}

// Функции масштабирования
function adjustScale(multiplier) {
  const newScale = Math.max(minScale, Math.min(maxScale, scale * multiplier));
  offsetX -= (canvas.width / 2 - offsetX) * (newScale / scale - 1);
  offsetY -= (canvas.height / 2 - offsetY) * (newScale / scale - 1);
  scale = newScale;
  clampOffsets();
  drawMap();
}

document
  .getElementById("zoomInBtn")
  .addEventListener("click", () => adjustScale(1.1));
document
  .getElementById("zoomOutBtn")
  .addEventListener("click", () => adjustScale(0.9));

canvas.addEventListener("wheel", (e) => {
  e.preventDefault();
  adjustScale(e.deltaY > 0 ? 0.9 : 1.1);
});

// Обработка перетаскивания карты
canvas.addEventListener("mousedown", (e) => {
  isDragging = true;
  startX = e.clientX - offsetX;
  startY = e.clientY - offsetY;
});

canvas.addEventListener("mousemove", (e) => {
  if (isDragging) {
    offsetX = e.clientX - startX;
    offsetY = e.clientY - startY;
    clampOffsets();
    drawMap();
  }
});

canvas.addEventListener("mouseup", () => (isDragging = false));
canvas.addEventListener("mouseleave", () => (isDragging = false));
