import { HexGrid, Layout, Hexagon } from "react-hexgrid";
import "./App.css";
import { request } from "./arenarequest";
import { UncontrolledReactSVGPanZoom } from "react-svg-pan-zoom";
import { useMemo } from "react";

function App() {
  // const API_KEY = `e9d0504b-f145-4d78-8219-c688ca06550f`;
  /*  const [data, setData] = useState();
  useEffect(() => {
    const fetchMapData = async () => {
      try {
        const response = await axios.get(
          "https://games-test.datsteam.dev/api/arena",
          {
            headers: {
              "Content-Type": "application/json",
              "X-AUTH-TOKEN": API_KEY,
            },
          }
        );

        const respData = response.data;
        console.log(respData);
        setData(respData); // предполагается, что setData доступен в области видимости
        return respData;
      } catch (error) {
        console.error("Ошибка запроса:", error);
        return [];
      }
    };

  }, []); */

  console.log(request.map);
  const minQ = Math.min(...request.map.map((hex) => hex.q));
  const minR = Math.min(...request.map.map((hex) => hex.r));

  // 2. Нормализуем координаты (сдвигаем к (0,0))
  const normalizedHexes = useMemo(
    () =>
      request.map.map((hex) => ({
        ...hex,
        q: hex.q - minQ, // Сдвигаем q
        r: hex.r - minR, // Сдвигаем r
        rq: hex.q,
        rr: hex.r,
      })),
    [minQ, minR]
  );

  return (
    <UncontrolledReactSVGPanZoom
      width={window.innerWidth}
      height={window.innerHeight}
    >
      <svg width={300} height={300}>
        <HexGrid width={100} height={100}>
          <Layout
            size={{ x: 3, y: 3 }}
            flat={false}
            spacing={1.1}
            origin={{ x: 0, y: 0 }}
          >
            {normalizedHexes.map(({ q, r, rq, rr, cost,type }) => (
              <Hexagon
                key={`${q},${r}`}
                q={q}
                r={r}
                s={-q - r}
                className="custom-hex"
              >
                <text
                  x="0"
                  y="0"
                  textAnchor="middle"
                  alignmentBaseline="central"
                  fill={getColorByType(type)}
                  fontSize="0.1"
                  dominantBaseline="middle"
                >
                  <tspan x="0" dy="-0.2">{`${rq};${rr}`}</tspan>
                  <tspan x="0" dy="0.8">{`${cost}`}</tspan>
                </text>
              </Hexagon>
            ))}
          </Layout>
        </HexGrid>
      </svg>
    </UncontrolledReactSVGPanZoom>
  );
}

// Функция для цветов гексов
function getColorByType(type: number) {
  const colors = {
    "2": "#aaffaa", // зелёный
    "3": "#ffaaaa", // красный
    "4": "#aaaaff", // синий
    "5": "#ffffaa", // жёлтый
  } as any;
  return colors[type.toString()] || "#dddddd";
}
export default App;
