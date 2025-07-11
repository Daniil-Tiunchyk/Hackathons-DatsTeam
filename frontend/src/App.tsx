import { HexGrid, Layout, Hexagon } from "react-hexgrid";
import "./App.css";

const hexes = [
  { q: 0, r: 0, s: 0 },
  { q: 0, r: 1, s: -1 },
  { q: 0, r: 2, s: -2 },
  { q: 0, r: 3, s: -3 },
  { q: 0, r: 4, s: -4 },
  { q: 0, r: 5, s: -5 },

  { q: 1, r: 0, s: -1 },
  { q: 1, r: 1, s: -2 },
  { q: 1, r: 2, s: -3 },
  { q: 1, r: 3, s: -4 },
  { q: 1, r: 4, s: -5 },
  { q: 1, r: 5, s: -6 },

  { q: 2, r: 0, s: -2 },
  { q: 2, r: 1, s: -3 },
  { q: 2, r: 2, s: -4 },
  { q: 2, r: 3, s: -5 },
  { q: 2, r: 4, s: -6 },
  { q: 2, r: 5, s: -7 },

  { q: 3, r: 0, s: -3 },
  { q: 3, r: 1, s: -4 },
  { q: 3, r: 2, s: -5 },
  { q: 3, r: 3, s: -6 },
  { q: 3, r: 4, s: -7 },
  { q: 3, r: 5, s: -8 },

  { q: 4, r: 0, s: -4 },
  { q: 4, r: 1, s: -5 },
  { q: 4, r: 2, s: -6 },
  { q: 4, r: 3, s: -7 },
  { q: 4, r: 4, s: -8 },
  { q: 4, r: 5, s: -9 },

  { q: 5, r: 0, s: -5 },
  { q: 5, r: 1, s: -6 },
  { q: 5, r: 2, s: -7 },
  { q: 5, r: 3, s: -8 },
  { q: 5, r: 4, s: -9 },
  { q: 5, r: 5, s: -10 },
];

function App() {
  return (
    <HexGrid width={window.innerWidth} height={window.innerHeight}>
      <Layout
        size={{ x: 3, y: 3 }}
        flat={false}
        spacing={1.1}
        origin={{ x: 0, y: 0 }}
      >
        {hexes.map(({ q, r }) => (
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
              fill="#fff"
              fontSize="0.2"
            >
              {`${q},${r}`}
            </text>
          </Hexagon>
        ))}
      </Layout>
    </HexGrid>
  );
}

export default App;
