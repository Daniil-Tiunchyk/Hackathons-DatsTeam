import { Canvas } from "@react-three/fiber";
import { Environment, OrbitControls, Text } from "@react-three/drei";
import React, { useEffect } from "react";
import * as THREE from "three";
import towerData from "../constants/towers.json";
import { letters } from "../constants/cubeLetters";
import { directionVectors } from "./../constants/directions";
import { surface_size } from "./../constants/SceneConstants";

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
  return (
    <group position={position}>
      <mesh>
        <boxGeometry args={[1, 1, 1]} />
        <meshStandardMaterial
          color={color}
          metalness={1}
          roughness={0.2}
          opacity={0.6}
          transparent={true}
        />
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
          fontSize={0.4}
          font="/fonts/Minecraftia-Regular.ttf"
          color="black"
          depthTest={false}
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
    <Canvas shadows camera={{ position: [10, 10, 15], fov: 50 }}>
      <Environment files="/autumn_field_puresky_4k.hdr" background blur={0} />
      <ambientLight intensity={0.5} />
      <directionalLight position={[5, 10, 5]} intensity={1} castShadow />
      {/*  */}
      <mesh rotation={[-Math.PI / 2, 0, 0]} position={[0.5, -1, 0.5]}>
        <boxGeometry args={[...surface_size]} />
        <meshStandardMaterial
          color={"blueviolet"}
          opacity={0.2}
          transparent={true}
        />
      </mesh>

      <gridHelper args={[...surface_size]} position={[0.5, -0.5, 0.5]} />
      {/*  */}
      <OrbitControls />
      {words.map((word, index) => (
        <WordCubes key={index} word={word} />
      ))}
    </Canvas>
  );
}
