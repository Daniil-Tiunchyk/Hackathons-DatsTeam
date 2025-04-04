import { Canvas } from "@react-three/fiber";
import { OrbitControls, Text } from "@react-three/drei";
import React, { useEffect } from "react";
import * as THREE from "three";
import towerData from "../constants/towers.json"

const directionVectors = [
  [1, 0, 0], // dir = 0 → вправо по X
  [0, 1, 0], // dir = 1 → вверх по Y
  [0, 0, 1], // dir = 2 → вглубь по Z
];

const letters = [
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

// Функция для генерации случайного цвета в формате HEX
const getRandomColor = () => {
  const letters = "0123456789ABCDEF";
  let color = "#";
  for (let i = 0; i < 6; i++) {
    color += letters[Math.floor(Math.random() * 16)];
  }
  return color;
};


function LetterCube({ position, letter, color }) {
  console.log(position, letter);
  return (
    <group position={position}>
      <mesh>
        <boxGeometry args={[1, 1, 1]} />
        <meshStandardMaterial color={color} />
        <lineSegments>
          <edgesGeometry
            attach="geometry"
            args={[new THREE.BoxGeometry(1, 1, 1)]}
          />
          <lineBasicMaterial attach="material" color="black" />
        </lineSegments>
      </mesh>

      {letters.map((face, index) => (
        <Text
          key={index}
          position={face.position}
          rotation={face.rotation}
          fontSize={0.3}
          color="black"
          anchorX="center"
          anchorY="middle"
        >
          {letter}
        </Text>
      ))}
    </group>
  );
}

function WordCubes({ word }) {
  const { pos, dir, text } = word;
  const dirVec = directionVectors[dir];

  const wordColor = getRandomColor();

  return (
    <>
      {text.split("").map((char, i) => {
        const cubePos = [
          pos[0] + dirVec[0] * i,
          pos[1] + dirVec[1] * i,
          pos[2] + dirVec[2] * i,
        ];
        return (
          <LetterCube
            key={i}
            position={cubePos}
            letter={char}
            color={wordColor}
          />
        );
      })}
    </>
  );
}


export default function Scene() {
  const words = towerData.tower.words;

    useEffect(() => {
      // Обновляет размер холста при изменении экрана
      const handleResize = () => {
        const canvas = document.querySelector("canvas");
        if (canvas) {
          canvas.style.width = "100vw";
          canvas.style.height = "100vh";
        }
      };
      window.addEventListener("resize", handleResize);
      handleResize();
      return () => window.removeEventListener("resize", handleResize);
    }, []);


  return (
    <Canvas camera={{ position: [10, 10, 15], fov: 50 }}>
      <ambientLight intensity={0.6} />
      <directionalLight position={[5, 10, 5]} />
      <gridHelper args={[50, 50]} position={[0.5, -0.5, 0.5]} />{" "}
      {/* size = 20, divisions = 20 → cell = 1 */}
      <OrbitControls />
      {words.map((word, index) => (
        <WordCubes key={index} word={word} />
      ))}
    </Canvas>
  );
}
