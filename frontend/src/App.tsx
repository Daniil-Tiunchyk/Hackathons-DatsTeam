import { HexGrid, Layout, Hexagon } from "react-hexgrid";
import "./App.css";
import { UncontrolledReactSVGPanZoom } from "react-svg-pan-zoom";
import { useEffect, useMemo, useState, useCallback } from "react";
import { EnemyIcon } from "./assets/EnemyIcon";
import { AntWorkerIcon } from "./assets/AntWorkerIcon";
import { AntFighterIcon } from "./assets/AntFighterIcon";
import { AntDetectiveIcon } from "./assets/AntDetectiveIcon";
import { PizzaIcon } from "./assets/PizzaIcon";
import axios from "axios";

// Мемоизированный компонент Hexagon для предотвращения лишних рендеров
const MemoizedHexagon = ({
  q,
  r,
  type,
  rq,
  rr,
  data,
}: {
  q: number;
  r: number;
  type: number;
  rq: number;
  rr: number;
  data: any;
}) => {
  const getHexContent = useCallback(
    (q: number, r: number) => {
      const originalQ = q;
      const originalR = r;

      const hexAnts = data.ants.filter(
        (a: any) => a.q === originalQ && a.r === originalR
      );
      const hexEnemies = data.enemies.filter(
        (e: any) => e.q === originalQ && e.r === originalR
      );
      const hexFood = data.food.filter(
        (f: any) => f.q === originalQ && f.r === originalR
      );
      const hexHome = data.home.filter(
        (h: any) => h.q === originalQ && h.r === originalR
      );

      const antTypes = {
        type1: hexAnts.filter((a: any) => a.type === 0).length,
        type2: hexAnts.filter((a: any) => a.type === 1).length,
        type3: hexAnts.filter((a: any) => a.type === 2).length,
      };

      return {
        hasAnts: hexAnts.length > 0,
        hasEnemies: hexEnemies.length > 0,
        hasFood: hexFood.length > 0,
        hasHome: hexHome.length > 0,
        hasType1: antTypes.type1 > 0,
        hasType2: antTypes.type2 > 0,
        hasType3: antTypes.type3 > 0,
      };
    },
    [data]
  );

  const hexStyle = useMemo(() => {
    const content = getHexContent(rq, rr);
    let baseColor = getColorByType(type);

    const stroke = "#000000ff";
    const strokeWidth = 0.1;

    if (content.hasHome) {
      baseColor = "#000000ff";
    }

    return {
      fill: baseColor,
      stroke,
      strokeWidth,
    };
  }, [type, rq, rr, getHexContent]);

  const content = useMemo(() => getHexContent(rq, rr), [rq, rr, getHexContent]);

  return (
    <Hexagon q={q} r={r} s={-q - r} className="custom-hex" style={hexStyle}>
      {content.hasType1 && <AntWorkerIcon className="triangle" size={3} />}
      {content.hasType2 && <AntFighterIcon className="triangle" size={3} />}
      {content.hasType3 && <AntDetectiveIcon className="triangle" size={3} />}
      {content.hasEnemies && <EnemyIcon className="triangle" size={3} />}
      {content.hasFood && <PizzaIcon className="triangle" size={3} />}

      <text
        x="0"
        y="0"
        textAnchor="middle"
        alignmentBaseline="central"
        fill={"#000"}
        fontSize="0.1"
        dominantBaseline="middle"
      >
      <tspan x="0" dy="-0.2">{`${rq};${rr}`}</tspan>
      </text>
    </Hexagon>
  );
};

function App() {
  const API_KEY = `e9d0504b-f145-4d78-8219-c688ca06550f`;
  const [data, setData] = useState<{
    ants: any[];
    enemies: any[];
    map: any[];
    food: any[];
    home: any[];
  } | null>(null);

  const [timer, setTimer] = useState(0);

  useEffect(() => {
    const interval = setInterval(() => {
      setTimer((prev) => prev + 1);
    }, 2000);

    return () => clearInterval(interval);
  }, []);

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
        console.log(response.data)
        setData(response.data);
      } catch (error) {
        console.error("Ошибка запроса:", error);
        setData({
          ants: [],
          enemies: [],
          map: [],
          food: [],
          home: [],
        });
      }
    };
    fetchMapData();
  }, [timer]);

  const normalizedHexes = useMemo(() => {
    if (!data?.map?.length) return [];

    const minQ = Math.min(...data.map.map((hex: any) => hex.q));
    const minR = Math.min(...data.map.map((hex: any) => hex.r));

    return data.map.map((hex: any) => ({
      ...hex,
      q: hex.q - minQ,
      r: hex.r - minR,
      rq: hex.q,
      rr: hex.r,
    }));
  }, [data]);

  if (!data) {
    return <div>Loading...</div>;
  }

  return (
    <UncontrolledReactSVGPanZoom
      width={window.innerWidth}
      height={window.innerHeight}
    >
      <svg width={window.innerWidth} height={window.innerHeight}>
        <HexGrid width={window.innerWidth / 2} height={window.innerHeight / 2}>
          <Layout size={{ x: 3, y: 3 }} flat={false} origin={{ x: 0, y: 0 }}>
            {normalizedHexes.map(({ q, r, rq, rr, type }) => (
              <MemoizedHexagon
                key={`${q}-${r}-${type}`}
                q={q}
                r={r}
                type={type}
                rq={rq}
                rr={rr}
                data={data}
              />
            ))}
          </Layout>
        </HexGrid>
      </svg>
    </UncontrolledReactSVGPanZoom>
  );
}

function getColorByType(type: number) {
  const colors: Record<string, string> = {
    "1": "#8204a9ff",
    "2": "#ffffffff",
    "3": "#e1c79bff",
    "4": "#a5f5f1ff",
    "5": "#cdcdcdff",
  };
  return colors[type.toString()] || "#dddddd";
}

export default App;
