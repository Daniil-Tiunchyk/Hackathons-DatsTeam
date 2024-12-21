const visibilityRadius = 30;
let cameraPosition = { x: 0, y: 0, z: 0 };

function createChunkTrace(chunk, color) {
    const chunkSize = 30; // Размер чанка
    const [cx, cy, cz] = chunk.center;

    // Вершины чанка
    const vertices = [
        [cx - chunkSize / 2, cy - chunkSize / 2, cz - chunkSize / 2], // 0
        [cx + chunkSize / 2, cy - chunkSize / 2, cz - chunkSize / 2], // 1
        [cx + chunkSize / 2, cy + chunkSize / 2, cz - chunkSize / 2], // 2
        [cx - chunkSize / 2, cy + chunkSize / 2, cz - chunkSize / 2], // 3
        [cx - chunkSize / 2, cy - chunkSize / 2, cz + chunkSize / 2], // 4
        [cx + chunkSize / 2, cy - chunkSize / 2, cz + chunkSize / 2], // 5
        [cx + chunkSize / 2, cy + chunkSize / 2, cz + chunkSize / 2], // 6
        [cx - chunkSize / 2, cy + chunkSize / 2, cz + chunkSize / 2], // 7
    ];

    // Грани чанка (каждая грань — три вершины)
    const faces = [
        [0, 1, 2, 3], // Нижняя грань (по часовой стрелке относительно нормали)
        // [4, 5, 6, 7], // Верхняя грань
        // [0, 4, 7, 3], // Левая боковая грань
        // [1, 5, 6, 2], // Правая боковая грань
        // [3, 2, 6, 7], // Задняя боковая грань
        // [0, 1, 5, 4], // Передняя боковая грань
    ];

    // Грани для заливки
    const faceTraces = faces.map((face) => ({
        x: face.map((i) => vertices[i][0]).concat(vertices[face[0]][0]),
        y: face.map((i) => vertices[i][1]).concat(vertices[face[0]][1]),
        z: face.map((i) => vertices[i][2]).concat(vertices[face[0]][2]),
        type: "mesh3d",
        color: color,
        opacity: 0.6, // Прозрачность
        name: "Chunk faces",
    }));

    // Рёбра чанка (каждая линия - пара индексов в `vertices`)
    const edges = [
        [0, 1],
        [1, 2],
        [2, 3],
        [3, 0], // Основание
        [4, 5],
        [5, 6],
        [6, 7],
        [7, 4], // Верх
        [0, 4],
        [1, 5],
        [2, 6],
        [3, 7], // Боковые рёбра
    ];

    // Данные для отображения рёбер
    const edgeTrace = {
        x: edges.flatMap(([i, j]) => [vertices[i][0], vertices[j][0], null]),
        y: edges.flatMap(([i, j]) => [vertices[i][1], vertices[j][1], null]),
        z: edges.flatMap(([i, j]) => [vertices[i][2], vertices[j][2], null]),
        type: "scatter3d",
        mode: "lines",
        line: { color: "rgba(0, 0, 0, 0.2)", width: 2 }, // Полупрозрачные рёбра
        name: "Chunk Edges",
    };

    return { edgeTrace, faceTraces };
}

async function loadJSON() {
    try {
        const response = await fetch("../data/response_move.json");
        const jsonData = await response.json();

        const spawnPoints = await fetch("../spawn_points.csv");
        const csvData = await spawnPoints.text();

        const mapSize = jsonData.mapSize || [300, 300, 90];

        function isWithinVisibility(head, target) {
            return (
                Math.abs(target[0] - head[0]) <= visibilityRadius &&
                Math.abs(target[1] - head[1]) <= visibilityRadius &&
                Math.abs(target[2] - head[2]) <= visibilityRadius
            );
        }

        function filterVisibleObjects(head, objects) {
            return objects.filter((obj) => isWithinVisibility(head, obj));
        }

        function createSegments(points) {
            const segments = { x: [], y: [], z: [] };

            for (let i = 0; i < points.length; i++) {
                for (let j = i + 1; j < points.length; j++) {
                    const p1 = points[i];
                    const p2 = points[j];

                    const manhattanDistance =
                        Math.abs(p1[0] - p2[0]) +
                        Math.abs(p1[1] - p2[1]) +
                        Math.abs(p1[2] - p2[2]);

                    if (manhattanDistance === 1) {
                        segments.x.push(p1[0], p2[0], null);
                        segments.y.push(p1[1], p2[1], null);
                        segments.z.push(p1[2], p2[2], null);
                    }
                }
            }

            return segments;
        }

        function calculateCenterCoordinate(mapSize) {
            const x = mapSize[0] / 2;
            const y = mapSize[1] / 2;
            const z = mapSize[2] / 2;
            return [x, y, z];
        }

        function calculateAllChunksCenter(mapSize) {
            const mapChunkSise = [
                mapSize[0] / 30,
                mapSize[1] / 30,
                mapSize[2] / 30,
            ];
            console.log(mapChunkSise);
            const allChunks = [];
            for (let x = 0; x < mapChunkSise[0]; x++) {
                for (let y = 0; y < mapChunkSise[1]; y++) {
                    for (let z = 0; z < mapChunkSise[2]; z++) {
                        const center = [
                            30 * (x + 1 / 2),
                            30 * (y + 1 / 2),
                            30 * (z + 1 / 2),
                        ];
                        allChunks.push(center);
                    }
                }
            }
            return allChunks;
        }

        function calculateMdistance(p1, p2) {
            return (
                Math.abs(p1[0] - p2[0]) +
                Math.abs(p1[1] - p2[1]) +
                Math.abs(p1[2] - p2[2])
            );
        }

        function calculateChunksHierarchy(mapSize) {
            const mapCenter = calculateCenterCoordinate(mapSize);
            const allChunks = calculateAllChunksCenter(mapSize);
            const chunks = allChunks.map((center) => ({
                center,
                distance: calculateMdistance(center, mapCenter),
            }));
            return chunks;
        }

        const snakeTraces = [];
        const fenceTraces = [];
        const foodTraces = [];

        (jsonData.snakes || []).forEach((snake, index) => {
            if (!snake.geometry || snake.geometry.length === 0) return;

            const head = snake.geometry[0];
            if (!head || head.length !== 3) return;

            const visibleFences = filterVisibleObjects(
                head,
                jsonData.fences || []
            );
            const visibleFood = filterVisibleObjects(
                head,
                (jsonData.food || []).map((food) => food.c)
            );

            const segments = createSegments(snake.geometry);
            snakeTraces.push({
                x: segments.x,
                y: segments.y,
                z: segments.z,
                mode: "lines+markers",
                type: "scatter3d",
                marker: {
                    size: 10,
                    color: snake.status === "alive" ? "limegreen" : "red",
                },
                line: {
                    width: 4,
                    color: snake.status === "alive" ? "green" : "darkred",
                },
                name: `Snake ${index + 1} (${snake.status || "unknown"})`,
                legendgroup: "snakes",
                showlegend: index === 0,
            });

            if (visibleFences.length > 1) {
                const fenceSegments = createSegments(visibleFences);
                fenceTraces.push({
                    x: fenceSegments.x,
                    y: fenceSegments.y,
                    z: fenceSegments.z,
                    mode: "lines+markers",
                    type: "scatter3d",
                    marker: {
                        size: 10,
                        color: "gray",
                        symbol: "square",
                    },
                    line: {
                        width: 2,
                        color: "gray",
                    },
                    name: "Fence",
                    legendgroup: "fences",
                    showlegend: false,
                });
            }

            foodTraces.push({
                x: visibleFood.map((food) => food[0] || 0),
                y: visibleFood.map((food) => food[1] || 0),
                z: visibleFood.map((food) => food[2] || 0),
                mode: "markers",
                type: "scatter3d",
                marker: {
                    size: 12,
                    color: "orange",
                    symbol: "circle",
                },
                name: `Food visible to Snake ${index + 1}`,
                legendgroup: "food",
                showlegend: false,
            });
        });

        const layout = {
            title: "3D Snake Game",
            scene: {
                xaxis: { title: "X", range: [0, mapSize[0]] },
                yaxis: { title: "Y", range: [0, mapSize[1]] },
                zaxis: { title: "Z", range: [0, mapSize[2]] },
                camera: {
                    eye: { x: 1.5, y: 1.5, z: 1.5 },
                    center: {
                        x: cameraPosition.x,
                        y: cameraPosition.y,
                        z: cameraPosition.z,
                    },
                },
            },
            margin: { l: 0, r: 0, b: 0, t: 50 },
        };

        // Расчёт расстояний и чанков
        const chunksHierarchy = calculateChunksHierarchy(mapSize);
        // Функция для создания цветов
        function getColorByDistance(distance, maxDistance) {
            const normalizedDistance = distance / maxDistance;
            const hue = Math.floor(240 * (1 - normalizedDistance)); // От синего к красному
            return `hsla(${hue}, 100%, 50%, 0.5)`; // Прозрачность 0.5
        }

        // Находим максимальное расстояние для нормализации
        const maxDistance = Math.max(
            ...chunksHierarchy.map((chunk) => chunk.distance)
        );

        // Создаем следы чанков
        const chunkTraces = chunksHierarchy.flatMap((chunk) => {
            const color = getColorByDistance(chunk.distance, maxDistance);
            const { edgeTrace, faceTraces } = createChunkTrace(chunk, color);
            return [edgeTrace, ...faceTraces];
        });

        const rows = csvData.trim().split("\n").slice(1); // Убираем заголовок
        const points = rows.map((row) => {
            const [snakeId, x, y, z] = row.split(",").slice(0, 4); // Берём только snakeId, x, y, z
            return { snakeId, x: +x, y: +y, z: +z };
        });

        // Создаём трейс точек
        const pointTrace = {
            x: points.map((p) => p.x),
            y: points.map((p) => p.y),
            z: points.map((p) => p.z),
            mode: "markers", // Режим отображения точек
            type: "scatter3d",
            marker: {
                size: 5, // Размер точек
                color: "blue", // Цвет точек
                opacity: 0.8, // Прозрачность
            },
            text: points.map((p) => `SnakeID: ${p.snakeId}`), // Подписи
            name: "Spawn Points",
        };

        const allTraces = [
            ...chunkTraces,
            pointTrace,
            ...snakeTraces,
            ...fenceTraces,
            ...foodTraces,
        ];

        Plotly.newPlot("plotly-container", allTraces, layout);

        document
            .getElementById("toggle-fences")
            .addEventListener("click", () => {
                Plotly.restyle(
                    "plotly-container",
                    "visible",
                    false,
                    fenceTraces.map((_, i) => snakeTraces.length + i)
                );
            });

        document
            .getElementById("toggle-snakes")
            .addEventListener("click", () => {
                Plotly.restyle(
                    "plotly-container",
                    "visible",
                    false,
                    snakeTraces.map((_, i) => i)
                );
            });

        document.getElementById("toggle-food").addEventListener("click", () => {
            Plotly.restyle(
                "plotly-container",
                "visible",
                false,
                foodTraces.map((_, i) => fenceTraces.length + i)
            );
        });
    } catch (error) {
        console.error("Error loading JSON:", error);
    }
}

document.addEventListener("keydown", (event) => {
    switch (event.key) {
        case "w":
            cameraPosition.y += 10;
            break;
        case "a":
            cameraPosition.x -= 10;
            break;
        case "s":
            cameraPosition.y -= 10;
            break;
        case "d":
            cameraPosition.x += 10;
            break;
        default:
            break;
    }
    Plotly.relayout("plotly-container", {
        scene: {
            camera: {
                center: {
                    x: cameraPosition.x,
                    y: cameraPosition.y,
                    z: cameraPosition.z,
                },
            },
        },
    });
});

loadJSON().catch((error) => console.error("Error loading JSON:", error));
