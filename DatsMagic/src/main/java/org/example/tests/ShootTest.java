package org.example.tests;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.models.move.GameState;
import org.example.models.move.TransportAction;
import org.example.scripts.ShootScript;

import java.util.List;
import java.util.stream.Collectors;

public class ShootTest {
    public static void main(String[] args) {
        String test = """
                {
                  "mapSize": {
                    "x": 9000.0,
                    "y": 9000.0
                  },
                  "name": "НИИЧАВО (Научно-исследовательский институт Чародейства и Волшебства)",
                  "points": 919.0,
                  "anomalies": [
                    {
                      "x": 8228.0,
                      "y": 2173.0,
                      "id": "384f6bb2-8bc9-4806-8a1e-f74853b93c7c",
                      "radius": 37.27,
                      "effectiveRadius": 2283.03,
                      "strength": 3752.0,
                      "velocity": {
                        "x": 29.44,
                        "y": 58.76
                      }
                    },
                    {
                      "x": -906.0,
                      "y": 6351.0,
                      "id": "3ba054a6-d292-4cb8-927c-54b1c099511c",
                      "radius": 53.13,
                      "effectiveRadius": 1758.23,
                      "strength": 1095.0,
                      "velocity": {
                        "x": -44.06,
                        "y": 66.77
                      }
                    },
                    {
                      "x": -1088.0,
                      "y": 4476.0,
                      "id": "86d445f2-bdd2-4ca5-8952-d8fb883ff0a0",
                      "radius": 39.99,
                      "effectiveRadius": 2467.69,
                      "strength": -3808.0,
                      "velocity": {
                        "x": -42.55,
                        "y": 67.75
                      }
                    },
                    {
                      "x": 6291.0,
                      "y": 4783.0,
                      "id": "0f9eb7c9-af0a-466b-b13e-e0218719bea5",
                      "radius": 51.16,
                      "effectiveRadius": 1582.79,
                      "strength": 957.0,
                      "velocity": {
                        "x": 62.18,
                        "y": -9.35
                      }
                    },
                    {
                      "x": 3382.0,
                      "y": 8870.0,
                      "id": "b14d3cb7-ac39-4a27-b675-175175c611bb",
                      "radius": 50.99,
                      "effectiveRadius": 1574.79,
                      "strength": 954.0,
                      "velocity": {
                        "x": -56.52,
                        "y": -56.61
                      }
                    },
                    {
                      "x": 5849.0,
                      "y": 3857.0,
                      "id": "4b2090ee-5813-4a49-8fb6-40b15bdd6306",
                      "radius": 42.49,
                      "effectiveRadius": 1369.7,
                      "strength": 1039.0,
                      "velocity": {
                        "x": -56.15,
                        "y": -34.29
                      }
                    },
                    {
                      "x": 1099.0,
                      "y": 105.0,
                      "id": "e190fe1c-7bce-4a97-aec7-507632f65252",
                      "radius": 42.57,
                      "effectiveRadius": 1354.96,
                      "strength": -1013.0,
                      "velocity": {
                        "x": -66.56,
                        "y": -31.2
                      }
                    },
                    {
                      "x": 2408.0,
                      "y": 1016.0,
                      "id": "4549e784-8e8c-456c-82f4-65cb702dd8e3",
                      "radius": 47.51,
                      "effectiveRadius": 1523.29,
                      "strength": 1028.0,
                      "velocity": {
                        "x": 49.58,
                        "y": -34.95
                      }
                    },
                    {
                      "x": 8711.0,
                      "y": 6762.0,
                      "id": "d01a4379-4412-41be-8c98-93b87d2a1792",
                      "radius": 55.14,
                      "effectiveRadius": 1682.59,
                      "strength": 931.0,
                      "velocity": {
                        "x": -61.58,
                        "y": 51.06
                      }
                    },
                    {
                      "x": 2594.0,
                      "y": 10189.0,
                      "id": "922e8068-d1b2-4b2e-9136-4146ae663d8d",
                      "radius": 52.73,
                      "effectiveRadius": 1591.39,
                      "strength": -911.0,
                      "velocity": {
                        "x": -24.24,
                        "y": -75.75
                      }
                    },
                    {
                      "x": 5495.0,
                      "y": -1400.0,
                      "id": "fe1754d0-64f0-45dc-9137-d881dc0ba25a",
                      "radius": 52.33,
                      "effectiveRadius": 1646.38,
                      "strength": 990.0,
                      "velocity": {
                        "x": -56.52,
                        "y": 56.62
                      }
                    },
                    {
                      "x": 10281.0,
                      "y": 3503.0,
                      "id": "0df9ffbc-0f36-4d6a-ac2f-7c982ed8b5b2",
                      "radius": 44.15,
                      "effectiveRadius": 1381.97,
                      "strength": 980.0,
                      "velocity": {
                        "x": -69.05,
                        "y": -40.4
                      }
                    },
                    {
                      "x": 2317.0,
                      "y": 10483.0,
                      "id": "014d353d-885e-4cbc-9325-3eac7a3118e3",
                      "radius": 49.94,
                      "effectiveRadius": 1498.09,
                      "strength": -900.0,
                      "velocity": {
                        "x": -55.92,
                        "y": -57.2
                      }
                    },
                    {
                      "x": 5278.0,
                      "y": 7466.0,
                      "id": "23a23629-2da6-4769-b725-7c5951969742",
                      "radius": 59.1,
                      "effectiveRadius": 1953.73,
                      "strength": -1093.0,
                      "velocity": {
                        "x": 45.61,
                        "y": -65.73
                      }
                    },
                    {
                      "x": 5039.0,
                      "y": 7764.0,
                      "id": "0b16cd1c-61df-45f6-9a7d-bc76d0ab09a4",
                      "radius": 55.44,
                      "effectiveRadius": 1804.04,
                      "strength": -1059.0,
                      "velocity": {
                        "x": -59.65,
                        "y": 53.31
                      }
                    },
                    {
                      "x": -1714.0,
                      "y": 4419.0,
                      "id": "e293aa3f-320d-4ab7-932a-e885871bbd17",
                      "radius": 59.13,
                      "effectiveRadius": 1877.25,
                      "strength": 1008.0,
                      "velocity": {
                        "x": -44.81,
                        "y": 46.64
                      }
                    },
                    {
                      "x": 8217.0,
                      "y": 3960.0,
                      "id": "dbb8083a-dc06-4341-bb34-aae44a44687d",
                      "radius": 48.6,
                      "effectiveRadius": 1519.08,
                      "strength": 977.0,
                      "velocity": {
                        "x": 51.95,
                        "y": 59.27
                      }
                    },
                    {
                      "x": 7489.0,
                      "y": 5426.0,
                      "id": "e098c261-3995-4633-ba07-d5ea53093aa8",
                      "radius": 50.18,
                      "effectiveRadius": 1547.46,
                      "strength": -951.0,
                      "velocity": {
                        "x": 22.6,
                        "y": -60.14
                      }
                    },
                    {
                      "x": 8164.0,
                      "y": 844.0,
                      "id": "55329bc5-0d85-4117-a060-9d0f1888aa46",
                      "radius": 51.4,
                      "effectiveRadius": 1620.5,
                      "strength": 994.0,
                      "velocity": {
                        "x": 55.25,
                        "y": -27.77
                      }
                    },
                    {
                      "x": 5695.0,
                      "y": 227.0,
                      "id": "220298bd-cf4e-4aae-b194-0842a0537164",
                      "radius": 57.21,
                      "effectiveRadius": 1727.81,
                      "strength": -912.0,
                      "velocity": {
                        "x": -10.72,
                        "y": 67.07
                      }
                    },
                    {
                      "x": 3791.0,
                      "y": 7849.0,
                      "id": "e23dab19-f0f9-43dc-85a5-d9d217122015",
                      "radius": 35.81,
                      "effectiveRadius": 1086.27,
                      "strength": -920.0,
                      "velocity": {
                        "x": -73.62,
                        "y": 31.31
                      }
                    },
                    {
                      "x": 1241.0,
                      "y": 8671.0,
                      "id": "6403b350-baec-43cd-b9d0-08140e5e430f",
                      "radius": 33.82,
                      "effectiveRadius": 1042.42,
                      "strength": -950.0,
                      "velocity": {
                        "x": -33.15,
                        "y": -42.51
                      }
                    },
                    {
                      "x": -1057.0,
                      "y": 5116.0,
                      "id": "e7449721-47b4-459a-92a8-28e124e5f7fe",
                      "radius": 46.68,
                      "effectiveRadius": 1519.14,
                      "strength": -1059.0,
                      "velocity": {
                        "x": 58.43,
                        "y": -54.64
                      }
                    },
                    {
                      "x": 3720.0,
                      "y": 10349.0,
                      "id": "f646e12f-5cba-4401-80cc-b3853cc365ae",
                      "radius": 44.65,
                      "effectiveRadius": 1393.39,
                      "strength": 974.0,
                      "velocity": {
                        "x": -20.22,
                        "y": -54.3
                      }
                    },
                    {
                      "x": 7343.0,
                      "y": 374.0,
                      "id": "e9737477-991a-45a0-ba5a-c233ed482664",
                      "radius": 30.64,
                      "effectiveRadius": 985.66,
                      "strength": -1035.0,
                      "velocity": {
                        "x": 60.35,
                        "y": -26.1
                      }
                    },
                    {
                      "x": 2746.0,
                      "y": 6184.0,
                      "id": "792b5114-c1ac-41b3-a365-1962f7cc075c",
                      "radius": 33.29,
                      "effectiveRadius": 1029.21,
                      "strength": -956.0,
                      "velocity": {
                        "x": -18.16,
                        "y": 53.22
                      }
                    },
                    {
                      "x": 4823.0,
                      "y": 2828.0,
                      "id": "bbed9c9d-db0b-4c12-a5aa-912fbcb06994",
                      "radius": 39.63,
                      "effectiveRadius": 1195.56,
                      "strength": 910.0,
                      "velocity": {
                        "x": -42.65,
                        "y": -27.82
                      }
                    },
                    {
                      "x": 2613.0,
                      "y": 364.0,
                      "id": "b7a41a35-2a60-48f8-b1c5-83219097966e",
                      "radius": 54.87,
                      "effectiveRadius": 1798.17,
                      "strength": 1074.0,
                      "velocity": {
                        "x": -27.22,
                        "y": 75.23
                      }
                    },
                    {
                      "x": -1178.0,
                      "y": 2048.0,
                      "id": "5ceab75d-313c-4ed7-a3a2-1e76f6f7e401",
                      "radius": 59.08,
                      "effectiveRadius": 1846.74,
                      "strength": 977.0,
                      "velocity": {
                        "x": 45.43,
                        "y": -43.24
                      }
                    },
                    {
                      "x": 9092.0,
                      "y": 2991.0,
                      "id": "da49a2e4-d5fa-4f00-aed4-9b4e6fcdb0c6",
                      "radius": 102.55,
                      "effectiveRadius": 102.55,
                      "strength": 0.0,
                      "velocity": {
                        "x": -52.45,
                        "y": -29.38
                      }
                    },
                    {
                      "x": 9744.0,
                      "y": 5003.0,
                      "id": "3134c20b-4871-4eb9-95b8-795c3d18afcb",
                      "radius": 44.93,
                      "effectiveRadius": 1486.63,
                      "strength": -1095.0,
                      "velocity": {
                        "x": 47.36,
                        "y": 64.48
                      }
                    },
                    {
                      "x": 8034.0,
                      "y": 2057.0,
                      "id": "74fe7abc-323e-4941-94d5-92e0f1e87d96",
                      "radius": 31.02,
                      "effectiveRadius": 1021.42,
                      "strength": -1084.0,
                      "velocity": {
                        "x": 44.5,
                        "y": -66.48
                      }
                    },
                    {
                      "x": 1115.0,
                      "y": 5546.0,
                      "id": "f324aa5f-73d8-414a-933c-5d2d54a60666",
                      "radius": 58.99,
                      "effectiveRadius": 1783.51,
                      "strength": 914.0,
                      "velocity": {
                        "x": 44.26,
                        "y": 11.0
                      }
                    },
                    {
                      "x": 2420.0,
                      "y": 4791.0,
                      "id": "6df8665c-57f6-4fab-a3f6-4fae8df4e47a",
                      "radius": 100.81,
                      "effectiveRadius": 100.81,
                      "strength": 0.0,
                      "velocity": {
                        "x": 66.27,
                        "y": 5.96
                      }
                    },
                    {
                      "x": 584.0,
                      "y": 2908.0,
                      "id": "91c8a07f-f238-4a2d-b1c2-cac78fb60a0a",
                      "radius": 112.08,
                      "effectiveRadius": 112.08,
                      "strength": 0.0,
                      "velocity": {
                        "x": 51.2,
                        "y": 19.96
                      }
                    }
                  ],
                  "transports": [
                    {
                      "x": 3930.0,
                      "y": 9002.0,
                      "id": "b3ad0dfd337abf9b92eaf6496a1f0ee40892429f",
                      "velocity": {
                        "x": 0.0,
                        "y": 0.0
                      },
                      "selfAcceleration": {
                        "x": 0.0,
                        "y": 0.0
                      },
                      "anomalyAcceleration": {
                        "x": -3.1,
                        "y": 0.13
                      },
                      "health": 0.0,
                      "status": "dead",
                      "deathCount": 42.0,
                      "shieldLeftMs": 0.0,
                      "shieldCooldownMs": 0.0,
                      "attackCooldownMs": 0.0
                    },
                    {
                      "x": 5837.0,
                      "y": 1027.0,
                      "id": "a6c440902445b043eb139ac05cdf6d0f1acbe8db",
                      "velocity": {
                        "x": 51.27,
                        "y": 16.29
                      },
                      "selfAcceleration": {
                        "x": 9.93,
                        "y": 1.16
                      },
                      "anomalyAcceleration": {
                        "x": 0.22,
                        "y": 1.25
                      },
                      "health": 100.0,
                      "status": "alive",
                      "deathCount": 35.0,
                      "shieldLeftMs": 0.0,
                      "shieldCooldownMs": 0.0,
                      "attackCooldownMs": 0.0
                    },
                    {
                      "x": 7941.0,
                      "y": 6716.0,
                      "id": "f646b39a9e2bd1dc9114e4f9c81efb1383b64888",
                      "velocity": {
                        "x": 60.76,
                        "y": 30.75
                      },
                      "selfAcceleration": {
                        "x": 9.935811626479012,
                        "y": 1.1312149765294275
                      },
                      "anomalyAcceleration": {
                        "x": 1.61,
                        "y": 0.55
                      },
                      "health": 100.0,
                      "status": "alive",
                      "deathCount": 31.0,
                      "shieldLeftMs": 0.0,
                      "shieldCooldownMs": 0.0,
                      "attackCooldownMs": 0.0
                    },
                    {
                      "x": 3230.0,
                      "y": 5105.0,
                      "id": "e06f8fafc1a4cde94bbc0dc9f1083fb301473afd",
                      "velocity": {
                        "x": 0.0,
                        "y": 0.0
                      },
                      "selfAcceleration": {
                        "x": 9.930898019983676,
                        "y": 1.173569135877523
                      },
                      "anomalyAcceleration": {
                        "x": 0.0,
                        "y": 0.0
                      },
                      "health": 100.0,
                      "status": "alive",
                      "deathCount": 36.0,
                      "shieldLeftMs": 0.0,
                      "shieldCooldownMs": 0.0,
                      "attackCooldownMs": 0.0
                    },
                    {
                      "x": 6780.0,
                      "y": 7326.0,
                      "id": "27ec2c4c96f869ed9a416de0e8e9f5f693c54a0f",
                      "velocity": {
                        "x": 46.29,
                        "y": 4.2
                      },
                      "selfAcceleration": {
                        "x": 9.93064919646516,
                        "y": 1.1756728017377451
                      },
                      "anomalyAcceleration": {
                        "x": 0.86,
                        "y": -0.13
                      },
                      "health": 100.0,
                      "status": "alive",
                      "deathCount": 30.0,
                      "shieldLeftMs": 0.0,
                      "shieldCooldownMs": 0.0,
                      "attackCooldownMs": 0.0
                    }
                  ],
                  "enemies": [
                    {
                      "x": 7576.0,
                      "y": 7187.0,
                      "velocity": {
                        "x": 2.81,
                        "y": 1.51
                      },
                      "health": 100.0,
                      "status": "alive",
                      "killBounty": 43.0,
                      "shieldLeftMs": 0.0
                    },
                    {
                      "x": 7107.0,
                      "y": 7430.0,
                      "velocity": {
                        "x": 46.41,
                        "y": 99.73
                      },
                      "health": 100.0,
                      "status": "alive",
                      "killBounty": 17.0,
                      "shieldLeftMs": 0.0
                    },
                    {
                      "x": 6805.0,
                      "y": 7711.0,
                      "velocity": {
                        "x": -84.11,
                        "y": -65.95
                      },
                      "health": 80.0,
                      "status": "alive",
                      "killBounty": 38.0,
                      "shieldLeftMs": 0.0
                    },
                    {
                      "x": 3066.0,
                      "y": 5389.0,
                      "velocity": {
                        "x": 92.17,
                        "y": 60.05
                      },
                      "health": 50.0,
                      "status": "alive",
                      "killBounty": 39.0,
                      "shieldLeftMs": 0.0
                    },
                    {
                      "x": 3076.0,
                      "y": 5129.0,
                      "velocity": {
                        "x": 19.15,
                        "y": 9.56
                      },
                      "health": 70.0,
                      "status": "alive",
                      "killBounty": 64.0,
                      "shieldLeftMs": 0.0
                    },
                    {
                      "x": 7723.0,
                      "y": 6405.0,
                      "velocity": {
                        "x": 1.14,
                        "y": 1.38
                      },
                      "health": 100.0,
                      "status": "alive",
                      "killBounty": 33.0,
                      "shieldLeftMs": 0.0
                    },
                    {
                      "x": 8174.0,
                      "y": 6711.0,
                      "velocity": {
                        "x": -22.62,
                        "y": 1.95
                      },
                      "health": 100.0,
                      "status": "alive",
                      "killBounty": 97.0,
                      "shieldLeftMs": 0.0
                    },
                    {
                      "x": 7806.0,
                      "y": 6700.0,
                      "velocity": {
                        "x": 10.14,
                        "y": 7.9
                      },
                      "health": 100.0,
                      "status": "alive",
                      "killBounty": 20.0,
                      "shieldLeftMs": 0.0
                    },
                    {
                      "x": 5826.0,
                      "y": 897.0,
                      "velocity": {
                        "x": 1.37,
                        "y": 9.2
                      },
                      "health": 30.0,
                      "status": "alive",
                      "killBounty": 24.0,
                      "shieldLeftMs": 0.0
                    },
                    {
                      "x": 5827.0,
                      "y": 945.0,
                      "velocity": {
                        "x": 6.35,
                        "y": -32.56
                      },
                      "health": 100.0,
                      "status": "alive",
                      "killBounty": 19.0,
                      "shieldLeftMs": 0.0
                    },
                    {
                      "x": 3381.0,
                      "y": 5679.0,
                      "velocity": {
                        "x": -70.03,
                        "y": -84.83
                      },
                      "health": 100.0,
                      "status": "alive",
                      "killBounty": 38.0,
                      "shieldLeftMs": 0.0
                    },
                    {
                      "x": 8087.0,
                      "y": 6886.0,
                      "velocity": {
                        "x": 8.66,
                        "y": 0.07
                      },
                      "health": 30.0,
                      "status": "alive",
                      "killBounty": 21.0,
                      "shieldLeftMs": 0.0
                    },
                    {
                      "x": 6348.0,
                      "y": 7582.0,
                      "velocity": {
                        "x": 53.62,
                        "y": -0.63
                      },
                      "health": 100.0,
                      "status": "alive",
                      "killBounty": 44.0,
                      "shieldLeftMs": 0.0
                    },
                    {
                      "x": 6384.0,
                      "y": 7017.0,
                      "velocity": {
                        "x": -13.13,
                        "y": 0.77
                      },
                      "health": 100.0,
                      "status": "alive",
                      "killBounty": 185.0,
                      "shieldLeftMs": 0.0
                    },
                    {
                      "x": 6263.0,
                      "y": 644.0,
                      "velocity": {
                        "x": 0.07,
                        "y": 0.05
                      },
                      "health": 100.0,
                      "status": "alive",
                      "killBounty": 30.0,
                      "shieldLeftMs": 0.0
                    },
                    {
                      "x": 3584.0,
                      "y": 5387.0,
                      "velocity": {
                        "x": -25.14,
                        "y": -29.07
                      },
                      "health": 70.0,
                      "status": "alive",
                      "killBounty": 64.0,
                      "shieldLeftMs": 0.0
                    },
                    {
                      "x": 331.0,
                      "y": 5949.0,
                      "velocity": {
                        "x": 5.27,
                        "y": -15.09
                      },
                      "health": 100.0,
                      "status": "alive",
                      "killBounty": 23.0,
                      "shieldLeftMs": 0.0
                    },
                    {
                      "x": 3207.0,
                      "y": 4897.0,
                      "velocity": {
                        "x": -4.07,
                        "y": -47.45
                      },
                      "health": 100.0,
                      "status": "alive",
                      "killBounty": 29.0,
                      "shieldLeftMs": 0.0
                    },
                    {
                      "x": 2951.0,
                      "y": 5342.0,
                      "velocity": {
                        "x": -63.23,
                        "y": -56.75
                      },
                      "health": 100.0,
                      "status": "alive",
                      "killBounty": 36.0,
                      "shieldLeftMs": 0.0
                    },
                    {
                      "x": 3401.0,
                      "y": 4983.0,
                      "velocity": {
                        "x": 0.87,
                        "y": -1.36
                      },
                      "health": 100.0,
                      "status": "alive",
                      "killBounty": 14.0,
                      "shieldLeftMs": 0.0
                    },
                    {
                      "x": 7165.0,
                      "y": 7774.0,
                      "velocity": {
                        "x": -1.53,
                        "y": -7.19
                      },
                      "health": 50.0,
                      "status": "alive",
                      "killBounty": 185.0,
                      "shieldLeftMs": 0.0
                    },
                    {
                      "x": 8277.0,
                      "y": 6331.0,
                      "velocity": {
                        "x": 34.43,
                        "y": 9.32
                      },
                      "health": 100.0,
                      "status": "alive",
                      "killBounty": 23.0,
                      "shieldLeftMs": 0.0
                    },
                    {
                      "x": 8239.0,
                      "y": 6550.0,
                      "velocity": {
                        "x": 27.39,
                        "y": 8.38
                      },
                      "health": 100.0,
                      "status": "alive",
                      "killBounty": 24.0,
                      "shieldLeftMs": 0.0
                    },
                    {
                      "x": 7792.0,
                      "y": 7059.0,
                      "velocity": {
                        "x": -103.35,
                        "y": -37.66
                      },
                      "health": 70.0,
                      "status": "alive",
                      "killBounty": 45.0,
                      "shieldLeftMs": 0.0
                    },
                    {
                      "x": 3530.0,
                      "y": 5156.0,
                      "velocity": {
                        "x": 5.94,
                        "y": -0.82
                      },
                      "health": 100.0,
                      "status": "alive",
                      "killBounty": 29.0,
                      "shieldLeftMs": 0.0
                    },
                    {
                      "x": 8268.0,
                      "y": 6796.0,
                      "velocity": {
                        "x": 35.18,
                        "y": 27.49
                      },
                      "health": 100.0,
                      "status": "alive",
                      "killBounty": 38.0,
                      "shieldLeftMs": 0.0
                    },
                    {
                      "x": 7998.0,
                      "y": 6981.0,
                      "velocity": {
                        "x": 13.53,
                        "y": 5.09
                      },
                      "health": 80.0,
                      "status": "alive",
                      "killBounty": 61.0,
                      "shieldLeftMs": 0.0
                    },
                    {
                      "x": 8088.0,
                      "y": 7074.0,
                      "velocity": {
                        "x": -64.36,
                        "y": -89.21
                      },
                      "health": 100.0,
                      "status": "alive",
                      "killBounty": 29.0,
                      "shieldLeftMs": 0.0
                    },
                    {
                      "x": 7033.0,
                      "y": 7372.0,
                      "velocity": {
                        "x": 9.35,
                        "y": 9.03
                      },
                      "health": 100.0,
                      "status": "alive",
                      "killBounty": 15.0,
                      "shieldLeftMs": 0.0
                    },
                    {
                      "x": 6372.0,
                      "y": 7674.0,
                      "velocity": {
                        "x": 11.07,
                        "y": 10.44
                      },
                      "health": 90.0,
                      "status": "alive",
                      "killBounty": 206.0,
                      "shieldLeftMs": 0.0
                    },
                    {
                      "x": 6972.0,
                      "y": 7763.0,
                      "velocity": {
                        "x": 8.04,
                        "y": 2.33
                      },
                      "health": 100.0,
                      "status": "alive",
                      "killBounty": 28.0,
                      "shieldLeftMs": 0.0
                    },
                    {
                      "x": 7088.0,
                      "y": 7722.0,
                      "velocity": {
                        "x": 10.73,
                        "y": 13.7
                      },
                      "health": 100.0,
                      "status": "alive",
                      "killBounty": 24.0,
                      "shieldLeftMs": 0.0
                    },
                    {
                      "x": 7829.0,
                      "y": 6653.0,
                      "velocity": {
                        "x": -17.16,
                        "y": -27.65
                      },
                      "health": 50.0,
                      "status": "alive",
                      "killBounty": 71.0,
                      "shieldLeftMs": 0.0
                    },
                    {
                      "x": 6031.0,
                      "y": 928.0,
                      "velocity": {
                        "x": 10.04,
                        "y": 26.07
                      },
                      "health": 100.0,
                      "status": "alive",
                      "killBounty": 134.0,
                      "shieldLeftMs": 0.0
                    }
                  ],
                  "wantedList": [],
                  "bounties": [
                    {
                      "x": 6970.0,
                      "y": 7418.0,
                      "points": 62.0,
                      "radius": 5.0
                    },
                    {
                      "x": 4131.0,
                      "y": 8786.0,
                      "points": 44.0,
                      "radius": 5.0
                    },
                    {
                      "x": 5679.0,
                      "y": 1142.0,
                      "points": 69.0,
                      "radius": 5.0
                    },
                    {
                      "x": 8177.0,
                      "y": 6682.0,
                      "points": 44.0,
                      "radius": 5.0
                    },
                    {
                      "x": 7725.0,
                      "y": 6960.0,
                      "points": 44.0,
                      "radius": 5.0
                    },
                    {
                      "x": 3185.0,
                      "y": 5396.0,
                      "points": 186.0,
                      "radius": 5.0
                    },
                    {
                      "x": 6795.0,
                      "y": 7404.0,
                      "points": 62.0,
                      "radius": 5.0
                    },
                    {
                      "x": 8266.0,
                      "y": 7024.0,
                      "points": 38.0,
                      "radius": 5.0
                    },
                    {
                      "x": 2905.0,
                      "y": 5510.0,
                      "points": 166.0,
                      "radius": 5.0
                    },
                    {
                      "x": 8354.0,
                      "y": 6597.0,
                      "points": 39.0,
                      "radius": 5.0
                    },
                    {
                      "x": 7887.0,
                      "y": 6407.0,
                      "points": 55.0,
                      "radius": 5.0
                    },
                    {
                      "x": 5657.0,
                      "y": 941.0,
                      "points": 67.0,
                      "radius": 5.0
                    },
                    {
                      "x": 5919.0,
                      "y": 951.0,
                      "points": 60.0,
                      "radius": 5.0
                    },
                    {
                      "x": 6959.0,
                      "y": 7545.0,
                      "points": 54.0,
                      "radius": 5.0
                    },
                    {
                      "x": 6713.0,
                      "y": 7069.0,
                      "points": 79.0,
                      "radius": 5.0
                    },
                    {
                      "x": 7533.0,
                      "y": 6948.0,
                      "points": 60.0,
                      "radius": 5.0
                    },
                    {
                      "x": 5982.0,
                      "y": 952.0,
                      "points": 62.0,
                      "radius": 5.0
                    },
                    {
                      "x": 6884.0,
                      "y": 7474.0,
                      "points": 63.0,
                      "radius": 5.0
                    },
                    {
                      "x": 6758.0,
                      "y": 7037.0,
                      "points": 79.0,
                      "radius": 5.0
                    },
                    {
                      "x": 6673.0,
                      "y": 7597.0,
                      "points": 66.0,
                      "radius": 5.0
                    },
                    {
                      "x": 6634.0,
                      "y": 7063.0,
                      "points": 78.0,
                      "radius": 5.0
                    },
                    {
                      "x": 6616.0,
                      "y": 6954.0,
                      "points": 90.0,
                      "radius": 5.0
                    },
                    {
                      "x": 7705.0,
                      "y": 6988.0,
                      "points": 48.0,
                      "radius": 5.0
                    },
                    {
                      "x": 5669.0,
                      "y": 1152.0,
                      "points": 75.0,
                      "radius": 5.0
                    },
                    {
                      "x": 5802.0,
                      "y": 818.0,
                      "points": 58.0,
                      "radius": 5.0
                    },
                    {
                      "x": 6088.0,
                      "y": 1312.0,
                      "points": 75.0,
                      "radius": 5.0
                    },
                    {
                      "x": 7677.0,
                      "y": 6732.0,
                      "points": 58.0,
                      "radius": 5.0
                    },
                    {
                      "x": 8157.0,
                      "y": 7101.0,
                      "points": 38.0,
                      "radius": 5.0
                    },
                    {
                      "x": 6299.0,
                      "y": 1016.0,
                      "points": 59.0,
                      "radius": 5.0
                    },
                    {
                      "x": 7855.0,
                      "y": 6778.0,
                      "points": 49.0,
                      "radius": 5.0
                    },
                    {
                      "x": 3345.0,
                      "y": 4818.0,
                      "points": 209.0,
                      "radius": 5.0
                    },
                    {
                      "x": 5833.0,
                      "y": 895.0,
                      "points": 61.0,
                      "radius": 5.0
                    },
                    {
                      "x": 4015.0,
                      "y": 8791.0,
                      "points": 39.0,
                      "radius": 5.0
                    },
                    {
                      "x": 3618.0,
                      "y": 8812.0,
                      "points": 35.0,
                      "radius": 5.0
                    },
                    {
                      "x": 7951.0,
                      "y": 6966.0,
                      "points": 44.0,
                      "radius": 5.0
                    },
                    {
                      "x": 6821.0,
                      "y": 7341.0,
                      "points": 69.0,
                      "radius": 5.0
                    },
                    {
                      "x": 5523.0,
                      "y": 791.0,
                      "points": 61.0,
                      "radius": 5.0
                    },
                    {
                      "x": 7892.0,
                      "y": 6354.0,
                      "points": 62.0,
                      "radius": 5.0
                    },
                    {
                      "x": 5570.0,
                      "y": 936.0,
                      "points": 67.0,
                      "radius": 5.0
                    },
                    {
                      "x": 7976.0,
                      "y": 6925.0,
                      "points": 38.0,
                      "radius": 5.0
                    },
                    {
                      "x": 7547.0,
                      "y": 6681.0,
                      "points": 60.0,
                      "radius": 5.0
                    },
                    {
                      "x": 7765.0,
                      "y": 6913.0,
                      "points": 49.0,
                      "radius": 5.0
                    },
                    {
                      "x": 6416.0,
                      "y": 7778.0,
                      "points": 57.0,
                      "radius": 5.0
                    },
                    {
                      "x": 6242.0,
                      "y": 1164.0,
                      "points": 65.0,
                      "radius": 5.0
                    },
                    {
                      "x": 6995.0,
                      "y": 7476.0,
                      "points": 55.0,
                      "radius": 5.0
                    },
                    {
                      "x": 5939.0,
                      "y": 1113.0,
                      "points": 70.0,
                      "radius": 5.0
                    },
                    {
                      "x": 6434.0,
                      "y": 7564.0,
                      "points": 71.0,
                      "radius": 5.0
                    },
                    {
                      "x": 8353.0,
                      "y": 6793.0,
                      "points": 40.0,
                      "radius": 5.0
                    },
                    {
                      "x": 7880.0,
                      "y": 6993.0,
                      "points": 47.0,
                      "radius": 5.0
                    },
                    {
                      "x": 2812.0,
                      "y": 5270.0,
                      "points": 154.0,
                      "radius": 5.0
                    },
                    {
                      "x": 5774.0,
                      "y": 975.0,
                      "points": 63.0,
                      "radius": 5.0
                    },
                    {
                      "x": 3328.0,
                      "y": 5591.0,
                      "points": 181.0,
                      "radius": 5.0
                    },
                    {
                      "x": 6484.0,
                      "y": 6960.0,
                      "points": 88.0,
                      "radius": 5.0
                    },
                    {
                      "x": 8284.0,
                      "y": 6960.0,
                      "points": 38.0,
                      "radius": 5.0
                    },
                    {
                      "x": 6065.0,
                      "y": 1430.0,
                      "points": 78.0,
                      "radius": 5.0
                    },
                    {
                      "x": 6376.0,
                      "y": 7797.0,
                      "points": 65.0,
                      "radius": 5.0
                    },
                    {
                      "x": 8320.0,
                      "y": 6448.0,
                      "points": 46.0,
                      "radius": 5.0
                    },
                    {
                      "x": 8178.0,
                      "y": 6809.0,
                      "points": 44.0,
                      "radius": 5.0
                    },
                    {
                      "x": 7728.0,
                      "y": 6985.0,
                      "points": 51.0,
                      "radius": 5.0
                    },
                    {
                      "x": 7562.0,
                      "y": 7157.0,
                      "points": 55.0,
                      "radius": 5.0
                    },
                    {
                      "x": 3085.0,
                      "y": 4905.0,
                      "points": 190.0,
                      "radius": 5.0
                    },
                    {
                      "x": 4105.0,
                      "y": 8854.0,
                      "points": 35.0,
                      "radius": 5.0
                    },
                    {
                      "x": 7737.0,
                      "y": 6760.0,
                      "points": 47.0,
                      "radius": 5.0
                    },
                    {
                      "x": 6825.0,
                      "y": 7164.0,
                      "points": 73.0,
                      "radius": 5.0
                    },
                    {
                      "x": 5475.0,
                      "y": 647.0,
                      "points": 56.0,
                      "radius": 5.0
                    },
                    {
                      "x": 7096.0,
                      "y": 7502.0,
                      "points": 56.0,
                      "radius": 5.0
                    },
                    {
                      "x": 4040.0,
                      "y": 8715.0,
                      "points": 45.0,
                      "radius": 5.0
                    },
                    {
                      "x": 7531.0,
                      "y": 6661.0,
                      "points": 68.0,
                      "radius": 5.0
                    },
                    {
                      "x": 4301.0,
                      "y": 8865.0,
                      "points": 26.0,
                      "radius": 5.0
                    },
                    {
                      "x": 7596.0,
                      "y": 6872.0,
                      "points": 49.0,
                      "radius": 5.0
                    },
                    {
                      "x": 6340.0,
                      "y": 7449.0,
                      "points": 79.0,
                      "radius": 5.0
                    },
                    {
                      "x": 8018.0,
                      "y": 6790.0,
                      "points": 48.0,
                      "radius": 5.0
                    },
                    {
                      "x": 3425.0,
                      "y": 5115.0,
                      "points": 206.0,
                      "radius": 5.0
                    },
                    {
                      "x": 2980.0,
                      "y": 5283.0,
                      "points": 175.0,
                      "radius": 5.0
                    },
                    {
                      "x": 5983.0,
                      "y": 972.0,
                      "points": 58.0,
                      "radius": 5.0
                    },
                    {
                      "x": 5885.0,
                      "y": 1038.0,
                      "points": 64.0,
                      "radius": 5.0
                    },
                    {
                      "x": 7596.0,
                      "y": 6515.0,
                      "points": 68.0,
                      "radius": 5.0
                    },
                    {
                      "x": 7712.0,
                      "y": 6453.0,
                      "points": 66.0,
                      "radius": 5.0
                    },
                    {
                      "x": 6017.0,
                      "y": 1179.0,
                      "points": 66.0,
                      "radius": 5.0
                    },
                    {
                      "x": 6072.0,
                      "y": 1057.0,
                      "points": 63.0,
                      "radius": 5.0
                    },
                    {
                      "x": 7962.0,
                      "y": 7032.0,
                      "points": 46.0,
                      "radius": 5.0
                    },
                    {
                      "x": 7145.0,
                      "y": 7669.0,
                      "points": 51.0,
                      "radius": 5.0
                    },
                    {
                      "x": 7656.0,
                      "y": 6947.0,
                      "points": 56.0,
                      "radius": 5.0
                    },
                    {
                      "x": 8291.0,
                      "y": 6861.0,
                      "points": 40.0,
                      "radius": 5.0
                    },
                    {
                      "x": 6079.0,
                      "y": 1333.0,
                      "points": 75.0,
                      "radius": 5.0
                    },
                    {
                      "x": 8018.0,
                      "y": 6803.0,
                      "points": 48.0,
                      "radius": 5.0
                    },
                    {
                      "x": 6569.0,
                      "y": 7288.0,
                      "points": 72.0,
                      "radius": 5.0
                    },
                    {
                      "x": 6539.0,
                      "y": 7307.0,
                      "points": 78.0,
                      "radius": 5.0
                    },
                    {
                      "x": 3630.0,
                      "y": 8861.0,
                      "points": 35.0,
                      "radius": 5.0
                    },
                    {
                      "x": 8141.0,
                      "y": 6846.0,
                      "points": 28.0,
                      "radius": 5.0
                    },
                    {
                      "x": 7017.0,
                      "y": 7214.0,
                      "points": 62.0,
                      "radius": 5.0
                    },
                    {
                      "x": 5720.0,
                      "y": 1042.0,
                      "points": 69.0,
                      "radius": 5.0
                    },
                    {
                      "x": 3426.0,
                      "y": 5069.0,
                      "points": 196.0,
                      "radius": 5.0
                    },
                    {
                      "x": 6110.0,
                      "y": 1123.0,
                      "points": 62.0,
                      "radius": 5.0
                    },
                    {
                      "x": 7578.0,
                      "y": 6692.0,
                      "points": 63.0,
                      "radius": 5.0
                    },
                    {
                      "x": 8212.0,
                      "y": 6970.0,
                      "points": 39.0,
                      "radius": 5.0
                    },
                    {
                      "x": 7905.0,
                      "y": 7125.0,
                      "points": 46.0,
                      "radius": 5.0
                    },
                    {
                      "x": 7817.0,
                      "y": 6443.0,
                      "points": 59.0,
                      "radius": 5.0
                    },
                    {
                      "x": 8266.0,
                      "y": 6894.0,
                      "points": 39.0,
                      "radius": 5.0
                    },
                    {
                      "x": 7837.0,
                      "y": 7020.0,
                      "points": 50.0,
                      "radius": 5.0
                    },
                    {
                      "x": 7963.0,
                      "y": 6584.0,
                      "points": 55.0,
                      "radius": 5.0
                    },
                    {
                      "x": 6081.0,
                      "y": 970.0,
                      "points": 62.0,
                      "radius": 5.0
                    },
                    {
                      "x": 6290.0,
                      "y": 959.0,
                      "points": 59.0,
                      "radius": 5.0
                    },
                    {
                      "x": 4225.0,
                      "y": 8877.0,
                      "points": 35.0,
                      "radius": 5.0
                    },
                    {
                      "x": 7598.0,
                      "y": 6519.0,
                      "points": 66.0,
                      "radius": 5.0
                    },
                    {
                      "x": 6516.0,
                      "y": 7443.0,
                      "points": 73.0,
                      "radius": 5.0
                    },
                    {
                      "x": 7943.0,
                      "y": 7034.0,
                      "points": 43.0,
                      "radius": 5.0
                    },
                    {
                      "x": 5787.0,
                      "y": 624.0,
                      "points": 54.0,
                      "radius": 5.0
                    },
                    {
                      "x": 2938.0,
                      "y": 5152.0,
                      "points": 169.0,
                      "radius": 5.0
                    },
                    {
                      "x": 5740.0,
                      "y": 1291.0,
                      "points": 78.0,
                      "radius": 5.0
                    },
                    {
                      "x": 8258.0,
                      "y": 7162.0,
                      "points": 29.0,
                      "radius": 5.0
                    },
                    {
                      "x": 3719.0,
                      "y": 8770.0,
                      "points": 44.0,
                      "radius": 5.0
                    },
                    {
                      "x": 8052.0,
                      "y": 6972.0,
                      "points": 43.0,
                      "radius": 5.0
                    },
                    {
                      "x": 5764.0,
                      "y": 864.0,
                      "points": 58.0,
                      "radius": 5.0
                    },
                    {
                      "x": 5633.0,
                      "y": 1462.0,
                      "points": 90.0,
                      "radius": 5.0
                    },
                    {
                      "x": 5644.0,
                      "y": 732.0,
                      "points": 59.0,
                      "radius": 5.0
                    },
                    {
                      "x": 7681.0,
                      "y": 6910.0,
                      "points": 54.0,
                      "radius": 5.0
                    },
                    {
                      "x": 3891.0,
                      "y": 8738.0,
                      "points": 44.0,
                      "radius": 5.0
                    },
                    {
                      "x": 4422.0,
                      "y": 8841.0,
                      "points": 44.0,
                      "radius": 5.0
                    },
                    {
                      "x": 8348.0,
                      "y": 6775.0,
                      "points": 34.0,
                      "radius": 5.0
                    },
                    {
                      "x": 6035.0,
                      "y": 1444.0,
                      "points": 80.0,
                      "radius": 5.0
                    },
                    {
                      "x": 6131.0,
                      "y": 688.0,
                      "points": 49.0,
                      "radius": 5.0
                    },
                    {
                      "x": 8380.0,
                      "y": 7137.0,
                      "points": 32.0,
                      "radius": 5.0
                    },
                    {
                      "x": 8305.0,
                      "y": 6675.0,
                      "points": 43.0,
                      "radius": 5.0
                    },
                    {
                      "x": 6407.0,
                      "y": 7779.0,
                      "points": 65.0,
                      "radius": 5.0
                    },
                    {
                      "x": 8116.0,
                      "y": 6926.0,
                      "points": 44.0,
                      "radius": 5.0
                    },
                    {
                      "x": 6640.0,
                      "y": 7100.0,
                      "points": 81.0,
                      "radius": 5.0
                    },
                    {
                      "x": 8385.0,
                      "y": 7071.0,
                      "points": 34.0,
                      "radius": 5.0
                    },
                    {
                      "x": 7587.0,
                      "y": 6502.0,
                      "points": 63.0,
                      "radius": 5.0
                    },
                    {
                      "x": 8185.0,
                      "y": 6345.0,
                      "points": 51.0,
                      "radius": 5.0
                    },
                    {
                      "x": 8204.0,
                      "y": 6625.0,
                      "points": 47.0,
                      "radius": 5.0
                    },
                    {
                      "x": 6425.0,
                      "y": 7734.0,
                      "points": 66.0,
                      "radius": 5.0
                    },
                    {
                      "x": 8281.0,
                      "y": 7041.0,
                      "points": 34.0,
                      "radius": 5.0
                    },
                    {
                      "x": 8360.0,
                      "y": 7044.0,
                      "points": 34.0,
                      "radius": 5.0
                    },
                    {
                      "x": 7665.0,
                      "y": 6322.0,
                      "points": 70.0,
                      "radius": 5.0
                    },
                    {
                      "x": 7789.0,
                      "y": 7185.0,
                      "points": 48.0,
                      "radius": 5.0
                    },
                    {
                      "x": 5691.0,
                      "y": 754.0,
                      "points": 58.0,
                      "radius": 5.0
                    },
                    {
                      "x": 3640.0,
                      "y": 8858.0,
                      "points": 38.0,
                      "radius": 5.0
                    },
                    {
                      "x": 3498.0,
                      "y": 5299.0,
                      "points": 208.0,
                      "radius": 5.0
                    },
                    {
                      "x": 3795.0,
                      "y": 8801.0,
                      "points": 38.0,
                      "radius": 5.0
                    },
                    {
                      "x": 5432.0,
                      "y": 1161.0,
                      "points": 60.0,
                      "radius": 5.0
                    },
                    {
                      "x": 2871.0,
                      "y": 5328.0,
                      "points": 149.0,
                      "radius": 5.0
                    },
                    {
                      "x": 5566.0,
                      "y": 623.0,
                      "points": 56.0,
                      "radius": 5.0
                    },
                    {
                      "x": 3593.0,
                      "y": 5018.0,
                      "points": 227.0,
                      "radius": 5.0
                    },
                    {
                      "x": 6113.0,
                      "y": 718.0,
                      "points": 51.0,
                      "radius": 5.0
                    },
                    {
                      "x": 7657.0,
                      "y": 6793.0,
                      "points": 60.0,
                      "radius": 5.0
                    }
                  ],
                  "maxSpeed": 110.0,
                  "maxAccel": 10.0,
                  "attackRange": 150.0,
                  "attackCooldownMs": 10000.0,
                  "attackDamage": 30.0,
                  "attackExplosionRadius": 30.0,
                  "reviveTimeoutSec": 2.0,
                  "shieldTimeMs": 5000.0,
                  "shieldCooldownMs": 40000.0,
                  "transportRadius": 5.0,
                  "errors": [
                    "transport b3ad0dfd337abf9b92eaf6496a1f0ee40892429f is dead",
                    "transport a6c440902445b043eb139ac05cdf6d0f1acbe8db acceleration is too high: 10.00, max allowed 10.00"
                  ]
                }""";
        Gson gson = new GsonBuilder().create();
        GameState gameState = gson.fromJson(test, GameState.class);
        List<TransportAction> transportList;
        transportList = gameState.getTransports()
                .stream().map(transport1 -> {
                    TransportAction transport = new TransportAction();
                    transport.setId(transport1.getId());
                    return transport;
                }).collect(Collectors.toList());
        ShootScript shootScript = new ShootScript();
        shootScript.shoot(transportList, gameState);
    }
}
