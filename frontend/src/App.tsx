import { HexGrid, Layout, Hexagon } from "react-hexgrid";
import "./App.css";
import { request } from "./arenarequest";
import { UncontrolledReactSVGPanZoom } from "react-svg-pan-zoom";
import { useMemo } from "react";
import { EnemyIcon } from "./assets/EnemyIcon";
import { AntWorkerIcon } from "./assets/AntWorkerIcon";
import { AntFighterIcon } from "./assets/AntFighterIcon";
import { AntDetectiveIcon } from "./assets/AntDetectiveIcon";
import { PizzaIcon } from "./assets/PizzaIcon";

function App() {
  const { ants, enemies, map, food, home } = request;

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

  const minQ = Math.min(...map.map((hex) => hex.q));
  const minR = Math.min(...map.map((hex) => hex.r));

  // 2. Нормализуем координаты (сдвигаем к (0,0))
  const normalizedHexes = useMemo(
    () =>
      map.map((hex) => ({
        ...hex,
        q: hex.q - minQ, // Сдвигаем q
        r: hex.r - minR, // Сдвигаем r
        rq: hex.q,
        rr: hex.r,
      })),
    [minQ, minR]
  );
  /*  */

  const getHexContent = (q: number, r: number) => {
    const originalQ = q;
    const originalR = r;

    const hexAnts = ants.filter((a) => a.q === originalQ && a.r === originalR);
    const hexEnemies = enemies.filter(
      (e) => e.q === originalQ && e.r === originalR
    );
    const hexFood = food.filter((f) => f.q === originalQ && f.r === originalR);
    const hexHome = home.filter((h) => {
      return h.q === originalQ && h.r === originalR;
    });

    const antTypes = {
      type1: hexAnts.filter((a) => a.type === 0).length,
      type2: hexAnts.filter((a) => a.type === 1).length,
      type3: hexAnts.filter((a) => a.type === 2).length,
    };

    return {
      hasAnts: hexAnts.length > 0,
      antsCount: hexAnts.length,
      hasEnemies: hexEnemies.length > 0,
      enemiesCount: hexEnemies.length,
      hasFood: hexFood.length > 0,
      foodCount: hexFood.length,
      hasHome: hexHome.length > 0,
      homeCount: hexHome.length,
      hasType1: antTypes.type1 > 0,
      hasType2: antTypes.type2 > 0,
      hasType3: antTypes.type3 > 0,
      antTypes,
      totalCount:
        hexAnts.length + hexEnemies.length + hexFood.length + hexHome.length,
    };
  };

  const getHexStyle = (type: number, q: number, r: number) => {
    const content = getHexContent(q, r);
    let baseColor = getColorByType(type);

    const stroke = "#000000ff";
    const strokeWidth = 0.1;

    if (content.hasHome) {
      baseColor = "#000000ff"; // синий - база
    }

    return {
      fill: baseColor,
      stroke,
      strokeWidth,
    };
  };

  return (
    <UncontrolledReactSVGPanZoom
      width={window.innerWidth}
      height={window.innerHeight}
    >
      <svg
        width={window.innerWidth}
        height={window.innerHeight}
      >
        <HexGrid width={window.innerWidth / 2} height={window.innerHeight / 2}>
          <Layout
            size={{ x: 3, y: 3 }}
            flat={false}
            origin={{ x: 0, y: 0 }}
          >
            {normalizedHexes.map(({ q, r, rq, rr, type }) => {
              const hexStyle = getHexStyle(type, rq, rr);
               const content = getHexContent(rq, rr); // если нужно что то достать

              return (
                <Hexagon
                  key={`${q},${r}`}
                  q={q}
                  r={r}
                  s={-q - r}
                  className="custom-hex"
                  style={hexStyle}
                >
                  {content.hasType1 && (
                    <AntWorkerIcon
                      className="triangle"
                      size={3} // Размер иконки
                    />
                  )}
                  {content.hasType2 && (
                    <AntFighterIcon
                      className="triangle"
                      size={3} // Размер иконки
                    />
                  )}
                  {content.hasType3 && (
                    <AntDetectiveIcon
                      className="triangle"
                      size={3} // Размер иконки
                    />
                  )}
                  {content.hasEnemies && (
                    <EnemyIcon
                      className="triangle"
                      size={3} // Размер иконки
                    />
                  )}
                  {content.hasFood && (
                    <PizzaIcon
                      className="triangle"
                      size={3} // Размер иконки
                    />
                  )}
                  {content.hasEnemies && (
                    <EnemyIcon
                      className="triangle"
                      size={3} // Размер иконки
                    />
                  )}
                  <text
                    x="0"
                    y="0"
                    textAnchor="middle"
                    alignmentBaseline="central"
                    fill={"#000"}
                    fontSize="0.1"
                    dominantBaseline="middle"
                  ></text>
                </Hexagon>
              );
            })}
          </Layout>
        </HexGrid>
      </svg>
    </UncontrolledReactSVGPanZoom>
  );
}

function getColorByType(type: number) {
  const colors: Record<string, string> = {
    "1": "#8204a9ff", // муравейник
    "2": "#ffffffff", // пустой
    "3": "#e1c79bff", // грязь
    "4": "#a5f5f1ff", // кислота
    "5": "#cdcdcdff", // камни
  };
  return colors[type.toString()] || "#dddddd";
}
export default App;
