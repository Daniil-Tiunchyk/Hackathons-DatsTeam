package org.example.Script;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.POST.Transport;
import org.example.models.move.GameState;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TestShoot {
    public static void main(String[] args) {
        String test = "{\n" +
                "  \"mapSize\": {\n" +
                "    \"x\": 9000.0,\n" +
                "    \"y\": 9000.0\n" +
                "  },\n" +
                "  \"name\": \"НИИЧАВО (Научно-исследовательский институт Чародейства и Волшебства)\",\n" +
                "  \"points\": 919.0,\n" +
                "  \"anomalies\": [\n" +
                "    {\n" +
                "      \"x\": 8228.0,\n" +
                "      \"y\": 2173.0,\n" +
                "      \"id\": \"384f6bb2-8bc9-4806-8a1e-f74853b93c7c\",\n" +
                "      \"radius\": 37.27,\n" +
                "      \"effectiveRadius\": 2283.03,\n" +
                "      \"strength\": 3752.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 29.44,\n" +
                "        \"y\": 58.76\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": -906.0,\n" +
                "      \"y\": 6351.0,\n" +
                "      \"id\": \"3ba054a6-d292-4cb8-927c-54b1c099511c\",\n" +
                "      \"radius\": 53.13,\n" +
                "      \"effectiveRadius\": 1758.23,\n" +
                "      \"strength\": 1095.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": -44.06,\n" +
                "        \"y\": 66.77\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": -1088.0,\n" +
                "      \"y\": 4476.0,\n" +
                "      \"id\": \"86d445f2-bdd2-4ca5-8952-d8fb883ff0a0\",\n" +
                "      \"radius\": 39.99,\n" +
                "      \"effectiveRadius\": 2467.69,\n" +
                "      \"strength\": -3808.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": -42.55,\n" +
                "        \"y\": 67.75\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6291.0,\n" +
                "      \"y\": 4783.0,\n" +
                "      \"id\": \"0f9eb7c9-af0a-466b-b13e-e0218719bea5\",\n" +
                "      \"radius\": 51.16,\n" +
                "      \"effectiveRadius\": 1582.79,\n" +
                "      \"strength\": 957.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 62.18,\n" +
                "        \"y\": -9.35\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 3382.0,\n" +
                "      \"y\": 8870.0,\n" +
                "      \"id\": \"b14d3cb7-ac39-4a27-b675-175175c611bb\",\n" +
                "      \"radius\": 50.99,\n" +
                "      \"effectiveRadius\": 1574.79,\n" +
                "      \"strength\": 954.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": -56.52,\n" +
                "        \"y\": -56.61\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 5849.0,\n" +
                "      \"y\": 3857.0,\n" +
                "      \"id\": \"4b2090ee-5813-4a49-8fb6-40b15bdd6306\",\n" +
                "      \"radius\": 42.49,\n" +
                "      \"effectiveRadius\": 1369.7,\n" +
                "      \"strength\": 1039.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": -56.15,\n" +
                "        \"y\": -34.29\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 1099.0,\n" +
                "      \"y\": 105.0,\n" +
                "      \"id\": \"e190fe1c-7bce-4a97-aec7-507632f65252\",\n" +
                "      \"radius\": 42.57,\n" +
                "      \"effectiveRadius\": 1354.96,\n" +
                "      \"strength\": -1013.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": -66.56,\n" +
                "        \"y\": -31.2\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 2408.0,\n" +
                "      \"y\": 1016.0,\n" +
                "      \"id\": \"4549e784-8e8c-456c-82f4-65cb702dd8e3\",\n" +
                "      \"radius\": 47.51,\n" +
                "      \"effectiveRadius\": 1523.29,\n" +
                "      \"strength\": 1028.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 49.58,\n" +
                "        \"y\": -34.95\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 8711.0,\n" +
                "      \"y\": 6762.0,\n" +
                "      \"id\": \"d01a4379-4412-41be-8c98-93b87d2a1792\",\n" +
                "      \"radius\": 55.14,\n" +
                "      \"effectiveRadius\": 1682.59,\n" +
                "      \"strength\": 931.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": -61.58,\n" +
                "        \"y\": 51.06\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 2594.0,\n" +
                "      \"y\": 10189.0,\n" +
                "      \"id\": \"922e8068-d1b2-4b2e-9136-4146ae663d8d\",\n" +
                "      \"radius\": 52.73,\n" +
                "      \"effectiveRadius\": 1591.39,\n" +
                "      \"strength\": -911.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": -24.24,\n" +
                "        \"y\": -75.75\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 5495.0,\n" +
                "      \"y\": -1400.0,\n" +
                "      \"id\": \"fe1754d0-64f0-45dc-9137-d881dc0ba25a\",\n" +
                "      \"radius\": 52.33,\n" +
                "      \"effectiveRadius\": 1646.38,\n" +
                "      \"strength\": 990.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": -56.52,\n" +
                "        \"y\": 56.62\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 10281.0,\n" +
                "      \"y\": 3503.0,\n" +
                "      \"id\": \"0df9ffbc-0f36-4d6a-ac2f-7c982ed8b5b2\",\n" +
                "      \"radius\": 44.15,\n" +
                "      \"effectiveRadius\": 1381.97,\n" +
                "      \"strength\": 980.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": -69.05,\n" +
                "        \"y\": -40.4\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 2317.0,\n" +
                "      \"y\": 10483.0,\n" +
                "      \"id\": \"014d353d-885e-4cbc-9325-3eac7a3118e3\",\n" +
                "      \"radius\": 49.94,\n" +
                "      \"effectiveRadius\": 1498.09,\n" +
                "      \"strength\": -900.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": -55.92,\n" +
                "        \"y\": -57.2\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 5278.0,\n" +
                "      \"y\": 7466.0,\n" +
                "      \"id\": \"23a23629-2da6-4769-b725-7c5951969742\",\n" +
                "      \"radius\": 59.1,\n" +
                "      \"effectiveRadius\": 1953.73,\n" +
                "      \"strength\": -1093.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 45.61,\n" +
                "        \"y\": -65.73\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 5039.0,\n" +
                "      \"y\": 7764.0,\n" +
                "      \"id\": \"0b16cd1c-61df-45f6-9a7d-bc76d0ab09a4\",\n" +
                "      \"radius\": 55.44,\n" +
                "      \"effectiveRadius\": 1804.04,\n" +
                "      \"strength\": -1059.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": -59.65,\n" +
                "        \"y\": 53.31\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": -1714.0,\n" +
                "      \"y\": 4419.0,\n" +
                "      \"id\": \"e293aa3f-320d-4ab7-932a-e885871bbd17\",\n" +
                "      \"radius\": 59.13,\n" +
                "      \"effectiveRadius\": 1877.25,\n" +
                "      \"strength\": 1008.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": -44.81,\n" +
                "        \"y\": 46.64\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 8217.0,\n" +
                "      \"y\": 3960.0,\n" +
                "      \"id\": \"dbb8083a-dc06-4341-bb34-aae44a44687d\",\n" +
                "      \"radius\": 48.6,\n" +
                "      \"effectiveRadius\": 1519.08,\n" +
                "      \"strength\": 977.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 51.95,\n" +
                "        \"y\": 59.27\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7489.0,\n" +
                "      \"y\": 5426.0,\n" +
                "      \"id\": \"e098c261-3995-4633-ba07-d5ea53093aa8\",\n" +
                "      \"radius\": 50.18,\n" +
                "      \"effectiveRadius\": 1547.46,\n" +
                "      \"strength\": -951.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 22.6,\n" +
                "        \"y\": -60.14\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 8164.0,\n" +
                "      \"y\": 844.0,\n" +
                "      \"id\": \"55329bc5-0d85-4117-a060-9d0f1888aa46\",\n" +
                "      \"radius\": 51.4,\n" +
                "      \"effectiveRadius\": 1620.5,\n" +
                "      \"strength\": 994.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 55.25,\n" +
                "        \"y\": -27.77\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 5695.0,\n" +
                "      \"y\": 227.0,\n" +
                "      \"id\": \"220298bd-cf4e-4aae-b194-0842a0537164\",\n" +
                "      \"radius\": 57.21,\n" +
                "      \"effectiveRadius\": 1727.81,\n" +
                "      \"strength\": -912.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": -10.72,\n" +
                "        \"y\": 67.07\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 3791.0,\n" +
                "      \"y\": 7849.0,\n" +
                "      \"id\": \"e23dab19-f0f9-43dc-85a5-d9d217122015\",\n" +
                "      \"radius\": 35.81,\n" +
                "      \"effectiveRadius\": 1086.27,\n" +
                "      \"strength\": -920.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": -73.62,\n" +
                "        \"y\": 31.31\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 1241.0,\n" +
                "      \"y\": 8671.0,\n" +
                "      \"id\": \"6403b350-baec-43cd-b9d0-08140e5e430f\",\n" +
                "      \"radius\": 33.82,\n" +
                "      \"effectiveRadius\": 1042.42,\n" +
                "      \"strength\": -950.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": -33.15,\n" +
                "        \"y\": -42.51\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": -1057.0,\n" +
                "      \"y\": 5116.0,\n" +
                "      \"id\": \"e7449721-47b4-459a-92a8-28e124e5f7fe\",\n" +
                "      \"radius\": 46.68,\n" +
                "      \"effectiveRadius\": 1519.14,\n" +
                "      \"strength\": -1059.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 58.43,\n" +
                "        \"y\": -54.64\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 3720.0,\n" +
                "      \"y\": 10349.0,\n" +
                "      \"id\": \"f646e12f-5cba-4401-80cc-b3853cc365ae\",\n" +
                "      \"radius\": 44.65,\n" +
                "      \"effectiveRadius\": 1393.39,\n" +
                "      \"strength\": 974.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": -20.22,\n" +
                "        \"y\": -54.3\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7343.0,\n" +
                "      \"y\": 374.0,\n" +
                "      \"id\": \"e9737477-991a-45a0-ba5a-c233ed482664\",\n" +
                "      \"radius\": 30.64,\n" +
                "      \"effectiveRadius\": 985.66,\n" +
                "      \"strength\": -1035.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 60.35,\n" +
                "        \"y\": -26.1\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 2746.0,\n" +
                "      \"y\": 6184.0,\n" +
                "      \"id\": \"792b5114-c1ac-41b3-a365-1962f7cc075c\",\n" +
                "      \"radius\": 33.29,\n" +
                "      \"effectiveRadius\": 1029.21,\n" +
                "      \"strength\": -956.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": -18.16,\n" +
                "        \"y\": 53.22\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 4823.0,\n" +
                "      \"y\": 2828.0,\n" +
                "      \"id\": \"bbed9c9d-db0b-4c12-a5aa-912fbcb06994\",\n" +
                "      \"radius\": 39.63,\n" +
                "      \"effectiveRadius\": 1195.56,\n" +
                "      \"strength\": 910.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": -42.65,\n" +
                "        \"y\": -27.82\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 2613.0,\n" +
                "      \"y\": 364.0,\n" +
                "      \"id\": \"b7a41a35-2a60-48f8-b1c5-83219097966e\",\n" +
                "      \"radius\": 54.87,\n" +
                "      \"effectiveRadius\": 1798.17,\n" +
                "      \"strength\": 1074.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": -27.22,\n" +
                "        \"y\": 75.23\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": -1178.0,\n" +
                "      \"y\": 2048.0,\n" +
                "      \"id\": \"5ceab75d-313c-4ed7-a3a2-1e76f6f7e401\",\n" +
                "      \"radius\": 59.08,\n" +
                "      \"effectiveRadius\": 1846.74,\n" +
                "      \"strength\": 977.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 45.43,\n" +
                "        \"y\": -43.24\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 9092.0,\n" +
                "      \"y\": 2991.0,\n" +
                "      \"id\": \"da49a2e4-d5fa-4f00-aed4-9b4e6fcdb0c6\",\n" +
                "      \"radius\": 102.55,\n" +
                "      \"effectiveRadius\": 102.55,\n" +
                "      \"strength\": 0.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": -52.45,\n" +
                "        \"y\": -29.38\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 9744.0,\n" +
                "      \"y\": 5003.0,\n" +
                "      \"id\": \"3134c20b-4871-4eb9-95b8-795c3d18afcb\",\n" +
                "      \"radius\": 44.93,\n" +
                "      \"effectiveRadius\": 1486.63,\n" +
                "      \"strength\": -1095.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 47.36,\n" +
                "        \"y\": 64.48\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 8034.0,\n" +
                "      \"y\": 2057.0,\n" +
                "      \"id\": \"74fe7abc-323e-4941-94d5-92e0f1e87d96\",\n" +
                "      \"radius\": 31.02,\n" +
                "      \"effectiveRadius\": 1021.42,\n" +
                "      \"strength\": -1084.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 44.5,\n" +
                "        \"y\": -66.48\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 1115.0,\n" +
                "      \"y\": 5546.0,\n" +
                "      \"id\": \"f324aa5f-73d8-414a-933c-5d2d54a60666\",\n" +
                "      \"radius\": 58.99,\n" +
                "      \"effectiveRadius\": 1783.51,\n" +
                "      \"strength\": 914.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 44.26,\n" +
                "        \"y\": 11.0\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 2420.0,\n" +
                "      \"y\": 4791.0,\n" +
                "      \"id\": \"6df8665c-57f6-4fab-a3f6-4fae8df4e47a\",\n" +
                "      \"radius\": 100.81,\n" +
                "      \"effectiveRadius\": 100.81,\n" +
                "      \"strength\": 0.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 66.27,\n" +
                "        \"y\": 5.96\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 584.0,\n" +
                "      \"y\": 2908.0,\n" +
                "      \"id\": \"91c8a07f-f238-4a2d-b1c2-cac78fb60a0a\",\n" +
                "      \"radius\": 112.08,\n" +
                "      \"effectiveRadius\": 112.08,\n" +
                "      \"strength\": 0.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 51.2,\n" +
                "        \"y\": 19.96\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"transports\": [\n" +
                "    {\n" +
                "      \"x\": 3930.0,\n" +
                "      \"y\": 9002.0,\n" +
                "      \"id\": \"b3ad0dfd337abf9b92eaf6496a1f0ee40892429f\",\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 0.0,\n" +
                "        \"y\": 0.0\n" +
                "      },\n" +
                "      \"selfAcceleration\": {\n" +
                "        \"x\": 0.0,\n" +
                "        \"y\": 0.0\n" +
                "      },\n" +
                "      \"anomalyAcceleration\": {\n" +
                "        \"x\": -3.1,\n" +
                "        \"y\": 0.13\n" +
                "      },\n" +
                "      \"health\": 0.0,\n" +
                "      \"status\": \"dead\",\n" +
                "      \"deathCount\": 42.0,\n" +
                "      \"shieldLeftMs\": 0.0,\n" +
                "      \"shieldCooldownMs\": 0.0,\n" +
                "      \"attackCooldownMs\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 5837.0,\n" +
                "      \"y\": 1027.0,\n" +
                "      \"id\": \"a6c440902445b043eb139ac05cdf6d0f1acbe8db\",\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 51.27,\n" +
                "        \"y\": 16.29\n" +
                "      },\n" +
                "      \"selfAcceleration\": {\n" +
                "        \"x\": 9.93,\n" +
                "        \"y\": 1.16\n" +
                "      },\n" +
                "      \"anomalyAcceleration\": {\n" +
                "        \"x\": 0.22,\n" +
                "        \"y\": 1.25\n" +
                "      },\n" +
                "      \"health\": 100.0,\n" +
                "      \"status\": \"alive\",\n" +
                "      \"deathCount\": 35.0,\n" +
                "      \"shieldLeftMs\": 0.0,\n" +
                "      \"shieldCooldownMs\": 0.0,\n" +
                "      \"attackCooldownMs\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7941.0,\n" +
                "      \"y\": 6716.0,\n" +
                "      \"id\": \"f646b39a9e2bd1dc9114e4f9c81efb1383b64888\",\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 60.76,\n" +
                "        \"y\": 30.75\n" +
                "      },\n" +
                "      \"selfAcceleration\": {\n" +
                "        \"x\": 9.935811626479012,\n" +
                "        \"y\": 1.1312149765294275\n" +
                "      },\n" +
                "      \"anomalyAcceleration\": {\n" +
                "        \"x\": 1.61,\n" +
                "        \"y\": 0.55\n" +
                "      },\n" +
                "      \"health\": 100.0,\n" +
                "      \"status\": \"alive\",\n" +
                "      \"deathCount\": 31.0,\n" +
                "      \"shieldLeftMs\": 0.0,\n" +
                "      \"shieldCooldownMs\": 0.0,\n" +
                "      \"attackCooldownMs\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 3230.0,\n" +
                "      \"y\": 5105.0,\n" +
                "      \"id\": \"e06f8fafc1a4cde94bbc0dc9f1083fb301473afd\",\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 0.0,\n" +
                "        \"y\": 0.0\n" +
                "      },\n" +
                "      \"selfAcceleration\": {\n" +
                "        \"x\": 9.930898019983676,\n" +
                "        \"y\": 1.173569135877523\n" +
                "      },\n" +
                "      \"anomalyAcceleration\": {\n" +
                "        \"x\": 0.0,\n" +
                "        \"y\": 0.0\n" +
                "      },\n" +
                "      \"health\": 100.0,\n" +
                "      \"status\": \"alive\",\n" +
                "      \"deathCount\": 36.0,\n" +
                "      \"shieldLeftMs\": 0.0,\n" +
                "      \"shieldCooldownMs\": 0.0,\n" +
                "      \"attackCooldownMs\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6780.0,\n" +
                "      \"y\": 7326.0,\n" +
                "      \"id\": \"27ec2c4c96f869ed9a416de0e8e9f5f693c54a0f\",\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 46.29,\n" +
                "        \"y\": 4.2\n" +
                "      },\n" +
                "      \"selfAcceleration\": {\n" +
                "        \"x\": 9.93064919646516,\n" +
                "        \"y\": 1.1756728017377451\n" +
                "      },\n" +
                "      \"anomalyAcceleration\": {\n" +
                "        \"x\": 0.86,\n" +
                "        \"y\": -0.13\n" +
                "      },\n" +
                "      \"health\": 100.0,\n" +
                "      \"status\": \"alive\",\n" +
                "      \"deathCount\": 30.0,\n" +
                "      \"shieldLeftMs\": 0.0,\n" +
                "      \"shieldCooldownMs\": 0.0,\n" +
                "      \"attackCooldownMs\": 0.0\n" +
                "    }\n" +
                "  ],\n" +
                "  \"enemies\": [\n" +
                "    {\n" +
                "      \"x\": 7576.0,\n" +
                "      \"y\": 7187.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 2.81,\n" +
                "        \"y\": 1.51\n" +
                "      },\n" +
                "      \"health\": 100.0,\n" +
                "      \"status\": \"alive\",\n" +
                "      \"killBounty\": 43.0,\n" +
                "      \"shieldLeftMs\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7107.0,\n" +
                "      \"y\": 7430.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 46.41,\n" +
                "        \"y\": 99.73\n" +
                "      },\n" +
                "      \"health\": 100.0,\n" +
                "      \"status\": \"alive\",\n" +
                "      \"killBounty\": 17.0,\n" +
                "      \"shieldLeftMs\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6805.0,\n" +
                "      \"y\": 7711.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": -84.11,\n" +
                "        \"y\": -65.95\n" +
                "      },\n" +
                "      \"health\": 80.0,\n" +
                "      \"status\": \"alive\",\n" +
                "      \"killBounty\": 38.0,\n" +
                "      \"shieldLeftMs\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 3066.0,\n" +
                "      \"y\": 5389.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 92.17,\n" +
                "        \"y\": 60.05\n" +
                "      },\n" +
                "      \"health\": 50.0,\n" +
                "      \"status\": \"alive\",\n" +
                "      \"killBounty\": 39.0,\n" +
                "      \"shieldLeftMs\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 3076.0,\n" +
                "      \"y\": 5129.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 19.15,\n" +
                "        \"y\": 9.56\n" +
                "      },\n" +
                "      \"health\": 70.0,\n" +
                "      \"status\": \"alive\",\n" +
                "      \"killBounty\": 64.0,\n" +
                "      \"shieldLeftMs\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7723.0,\n" +
                "      \"y\": 6405.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 1.14,\n" +
                "        \"y\": 1.38\n" +
                "      },\n" +
                "      \"health\": 100.0,\n" +
                "      \"status\": \"alive\",\n" +
                "      \"killBounty\": 33.0,\n" +
                "      \"shieldLeftMs\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 8174.0,\n" +
                "      \"y\": 6711.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": -22.62,\n" +
                "        \"y\": 1.95\n" +
                "      },\n" +
                "      \"health\": 100.0,\n" +
                "      \"status\": \"alive\",\n" +
                "      \"killBounty\": 97.0,\n" +
                "      \"shieldLeftMs\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7806.0,\n" +
                "      \"y\": 6700.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 10.14,\n" +
                "        \"y\": 7.9\n" +
                "      },\n" +
                "      \"health\": 100.0,\n" +
                "      \"status\": \"alive\",\n" +
                "      \"killBounty\": 20.0,\n" +
                "      \"shieldLeftMs\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 5826.0,\n" +
                "      \"y\": 897.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 1.37,\n" +
                "        \"y\": 9.2\n" +
                "      },\n" +
                "      \"health\": 30.0,\n" +
                "      \"status\": \"alive\",\n" +
                "      \"killBounty\": 24.0,\n" +
                "      \"shieldLeftMs\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 5827.0,\n" +
                "      \"y\": 945.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 6.35,\n" +
                "        \"y\": -32.56\n" +
                "      },\n" +
                "      \"health\": 100.0,\n" +
                "      \"status\": \"alive\",\n" +
                "      \"killBounty\": 19.0,\n" +
                "      \"shieldLeftMs\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 3381.0,\n" +
                "      \"y\": 5679.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": -70.03,\n" +
                "        \"y\": -84.83\n" +
                "      },\n" +
                "      \"health\": 100.0,\n" +
                "      \"status\": \"alive\",\n" +
                "      \"killBounty\": 38.0,\n" +
                "      \"shieldLeftMs\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 8087.0,\n" +
                "      \"y\": 6886.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 8.66,\n" +
                "        \"y\": 0.07\n" +
                "      },\n" +
                "      \"health\": 30.0,\n" +
                "      \"status\": \"alive\",\n" +
                "      \"killBounty\": 21.0,\n" +
                "      \"shieldLeftMs\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6348.0,\n" +
                "      \"y\": 7582.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 53.62,\n" +
                "        \"y\": -0.63\n" +
                "      },\n" +
                "      \"health\": 100.0,\n" +
                "      \"status\": \"alive\",\n" +
                "      \"killBounty\": 44.0,\n" +
                "      \"shieldLeftMs\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6384.0,\n" +
                "      \"y\": 7017.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": -13.13,\n" +
                "        \"y\": 0.77\n" +
                "      },\n" +
                "      \"health\": 100.0,\n" +
                "      \"status\": \"alive\",\n" +
                "      \"killBounty\": 185.0,\n" +
                "      \"shieldLeftMs\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6263.0,\n" +
                "      \"y\": 644.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 0.07,\n" +
                "        \"y\": 0.05\n" +
                "      },\n" +
                "      \"health\": 100.0,\n" +
                "      \"status\": \"alive\",\n" +
                "      \"killBounty\": 30.0,\n" +
                "      \"shieldLeftMs\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 3584.0,\n" +
                "      \"y\": 5387.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": -25.14,\n" +
                "        \"y\": -29.07\n" +
                "      },\n" +
                "      \"health\": 70.0,\n" +
                "      \"status\": \"alive\",\n" +
                "      \"killBounty\": 64.0,\n" +
                "      \"shieldLeftMs\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 331.0,\n" +
                "      \"y\": 5949.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 5.27,\n" +
                "        \"y\": -15.09\n" +
                "      },\n" +
                "      \"health\": 100.0,\n" +
                "      \"status\": \"alive\",\n" +
                "      \"killBounty\": 23.0,\n" +
                "      \"shieldLeftMs\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 3207.0,\n" +
                "      \"y\": 4897.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": -4.07,\n" +
                "        \"y\": -47.45\n" +
                "      },\n" +
                "      \"health\": 100.0,\n" +
                "      \"status\": \"alive\",\n" +
                "      \"killBounty\": 29.0,\n" +
                "      \"shieldLeftMs\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 2951.0,\n" +
                "      \"y\": 5342.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": -63.23,\n" +
                "        \"y\": -56.75\n" +
                "      },\n" +
                "      \"health\": 100.0,\n" +
                "      \"status\": \"alive\",\n" +
                "      \"killBounty\": 36.0,\n" +
                "      \"shieldLeftMs\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 3401.0,\n" +
                "      \"y\": 4983.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 0.87,\n" +
                "        \"y\": -1.36\n" +
                "      },\n" +
                "      \"health\": 100.0,\n" +
                "      \"status\": \"alive\",\n" +
                "      \"killBounty\": 14.0,\n" +
                "      \"shieldLeftMs\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7165.0,\n" +
                "      \"y\": 7774.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": -1.53,\n" +
                "        \"y\": -7.19\n" +
                "      },\n" +
                "      \"health\": 50.0,\n" +
                "      \"status\": \"alive\",\n" +
                "      \"killBounty\": 185.0,\n" +
                "      \"shieldLeftMs\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 8277.0,\n" +
                "      \"y\": 6331.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 34.43,\n" +
                "        \"y\": 9.32\n" +
                "      },\n" +
                "      \"health\": 100.0,\n" +
                "      \"status\": \"alive\",\n" +
                "      \"killBounty\": 23.0,\n" +
                "      \"shieldLeftMs\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 8239.0,\n" +
                "      \"y\": 6550.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 27.39,\n" +
                "        \"y\": 8.38\n" +
                "      },\n" +
                "      \"health\": 100.0,\n" +
                "      \"status\": \"alive\",\n" +
                "      \"killBounty\": 24.0,\n" +
                "      \"shieldLeftMs\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7792.0,\n" +
                "      \"y\": 7059.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": -103.35,\n" +
                "        \"y\": -37.66\n" +
                "      },\n" +
                "      \"health\": 70.0,\n" +
                "      \"status\": \"alive\",\n" +
                "      \"killBounty\": 45.0,\n" +
                "      \"shieldLeftMs\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 3530.0,\n" +
                "      \"y\": 5156.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 5.94,\n" +
                "        \"y\": -0.82\n" +
                "      },\n" +
                "      \"health\": 100.0,\n" +
                "      \"status\": \"alive\",\n" +
                "      \"killBounty\": 29.0,\n" +
                "      \"shieldLeftMs\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 8268.0,\n" +
                "      \"y\": 6796.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 35.18,\n" +
                "        \"y\": 27.49\n" +
                "      },\n" +
                "      \"health\": 100.0,\n" +
                "      \"status\": \"alive\",\n" +
                "      \"killBounty\": 38.0,\n" +
                "      \"shieldLeftMs\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7998.0,\n" +
                "      \"y\": 6981.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 13.53,\n" +
                "        \"y\": 5.09\n" +
                "      },\n" +
                "      \"health\": 80.0,\n" +
                "      \"status\": \"alive\",\n" +
                "      \"killBounty\": 61.0,\n" +
                "      \"shieldLeftMs\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 8088.0,\n" +
                "      \"y\": 7074.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": -64.36,\n" +
                "        \"y\": -89.21\n" +
                "      },\n" +
                "      \"health\": 100.0,\n" +
                "      \"status\": \"alive\",\n" +
                "      \"killBounty\": 29.0,\n" +
                "      \"shieldLeftMs\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7033.0,\n" +
                "      \"y\": 7372.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 9.35,\n" +
                "        \"y\": 9.03\n" +
                "      },\n" +
                "      \"health\": 100.0,\n" +
                "      \"status\": \"alive\",\n" +
                "      \"killBounty\": 15.0,\n" +
                "      \"shieldLeftMs\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6372.0,\n" +
                "      \"y\": 7674.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 11.07,\n" +
                "        \"y\": 10.44\n" +
                "      },\n" +
                "      \"health\": 90.0,\n" +
                "      \"status\": \"alive\",\n" +
                "      \"killBounty\": 206.0,\n" +
                "      \"shieldLeftMs\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6972.0,\n" +
                "      \"y\": 7763.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 8.04,\n" +
                "        \"y\": 2.33\n" +
                "      },\n" +
                "      \"health\": 100.0,\n" +
                "      \"status\": \"alive\",\n" +
                "      \"killBounty\": 28.0,\n" +
                "      \"shieldLeftMs\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7088.0,\n" +
                "      \"y\": 7722.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 10.73,\n" +
                "        \"y\": 13.7\n" +
                "      },\n" +
                "      \"health\": 100.0,\n" +
                "      \"status\": \"alive\",\n" +
                "      \"killBounty\": 24.0,\n" +
                "      \"shieldLeftMs\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7829.0,\n" +
                "      \"y\": 6653.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": -17.16,\n" +
                "        \"y\": -27.65\n" +
                "      },\n" +
                "      \"health\": 50.0,\n" +
                "      \"status\": \"alive\",\n" +
                "      \"killBounty\": 71.0,\n" +
                "      \"shieldLeftMs\": 0.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6031.0,\n" +
                "      \"y\": 928.0,\n" +
                "      \"velocity\": {\n" +
                "        \"x\": 10.04,\n" +
                "        \"y\": 26.07\n" +
                "      },\n" +
                "      \"health\": 100.0,\n" +
                "      \"status\": \"alive\",\n" +
                "      \"killBounty\": 134.0,\n" +
                "      \"shieldLeftMs\": 0.0\n" +
                "    }\n" +
                "  ],\n" +
                "  \"wantedList\": [],\n" +
                "  \"bounties\": [\n" +
                "    {\n" +
                "      \"x\": 6970.0,\n" +
                "      \"y\": 7418.0,\n" +
                "      \"points\": 62.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 4131.0,\n" +
                "      \"y\": 8786.0,\n" +
                "      \"points\": 44.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 5679.0,\n" +
                "      \"y\": 1142.0,\n" +
                "      \"points\": 69.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 8177.0,\n" +
                "      \"y\": 6682.0,\n" +
                "      \"points\": 44.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7725.0,\n" +
                "      \"y\": 6960.0,\n" +
                "      \"points\": 44.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 3185.0,\n" +
                "      \"y\": 5396.0,\n" +
                "      \"points\": 186.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6795.0,\n" +
                "      \"y\": 7404.0,\n" +
                "      \"points\": 62.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 8266.0,\n" +
                "      \"y\": 7024.0,\n" +
                "      \"points\": 38.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 2905.0,\n" +
                "      \"y\": 5510.0,\n" +
                "      \"points\": 166.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 8354.0,\n" +
                "      \"y\": 6597.0,\n" +
                "      \"points\": 39.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7887.0,\n" +
                "      \"y\": 6407.0,\n" +
                "      \"points\": 55.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 5657.0,\n" +
                "      \"y\": 941.0,\n" +
                "      \"points\": 67.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 5919.0,\n" +
                "      \"y\": 951.0,\n" +
                "      \"points\": 60.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6959.0,\n" +
                "      \"y\": 7545.0,\n" +
                "      \"points\": 54.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6713.0,\n" +
                "      \"y\": 7069.0,\n" +
                "      \"points\": 79.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7533.0,\n" +
                "      \"y\": 6948.0,\n" +
                "      \"points\": 60.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 5982.0,\n" +
                "      \"y\": 952.0,\n" +
                "      \"points\": 62.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6884.0,\n" +
                "      \"y\": 7474.0,\n" +
                "      \"points\": 63.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6758.0,\n" +
                "      \"y\": 7037.0,\n" +
                "      \"points\": 79.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6673.0,\n" +
                "      \"y\": 7597.0,\n" +
                "      \"points\": 66.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6634.0,\n" +
                "      \"y\": 7063.0,\n" +
                "      \"points\": 78.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6616.0,\n" +
                "      \"y\": 6954.0,\n" +
                "      \"points\": 90.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7705.0,\n" +
                "      \"y\": 6988.0,\n" +
                "      \"points\": 48.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 5669.0,\n" +
                "      \"y\": 1152.0,\n" +
                "      \"points\": 75.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 5802.0,\n" +
                "      \"y\": 818.0,\n" +
                "      \"points\": 58.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6088.0,\n" +
                "      \"y\": 1312.0,\n" +
                "      \"points\": 75.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7677.0,\n" +
                "      \"y\": 6732.0,\n" +
                "      \"points\": 58.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 8157.0,\n" +
                "      \"y\": 7101.0,\n" +
                "      \"points\": 38.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6299.0,\n" +
                "      \"y\": 1016.0,\n" +
                "      \"points\": 59.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7855.0,\n" +
                "      \"y\": 6778.0,\n" +
                "      \"points\": 49.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 3345.0,\n" +
                "      \"y\": 4818.0,\n" +
                "      \"points\": 209.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 5833.0,\n" +
                "      \"y\": 895.0,\n" +
                "      \"points\": 61.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 4015.0,\n" +
                "      \"y\": 8791.0,\n" +
                "      \"points\": 39.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 3618.0,\n" +
                "      \"y\": 8812.0,\n" +
                "      \"points\": 35.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7951.0,\n" +
                "      \"y\": 6966.0,\n" +
                "      \"points\": 44.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6821.0,\n" +
                "      \"y\": 7341.0,\n" +
                "      \"points\": 69.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 5523.0,\n" +
                "      \"y\": 791.0,\n" +
                "      \"points\": 61.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7892.0,\n" +
                "      \"y\": 6354.0,\n" +
                "      \"points\": 62.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 5570.0,\n" +
                "      \"y\": 936.0,\n" +
                "      \"points\": 67.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7976.0,\n" +
                "      \"y\": 6925.0,\n" +
                "      \"points\": 38.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7547.0,\n" +
                "      \"y\": 6681.0,\n" +
                "      \"points\": 60.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7765.0,\n" +
                "      \"y\": 6913.0,\n" +
                "      \"points\": 49.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6416.0,\n" +
                "      \"y\": 7778.0,\n" +
                "      \"points\": 57.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6242.0,\n" +
                "      \"y\": 1164.0,\n" +
                "      \"points\": 65.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6995.0,\n" +
                "      \"y\": 7476.0,\n" +
                "      \"points\": 55.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 5939.0,\n" +
                "      \"y\": 1113.0,\n" +
                "      \"points\": 70.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6434.0,\n" +
                "      \"y\": 7564.0,\n" +
                "      \"points\": 71.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 8353.0,\n" +
                "      \"y\": 6793.0,\n" +
                "      \"points\": 40.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7880.0,\n" +
                "      \"y\": 6993.0,\n" +
                "      \"points\": 47.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 2812.0,\n" +
                "      \"y\": 5270.0,\n" +
                "      \"points\": 154.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 5774.0,\n" +
                "      \"y\": 975.0,\n" +
                "      \"points\": 63.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 3328.0,\n" +
                "      \"y\": 5591.0,\n" +
                "      \"points\": 181.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6484.0,\n" +
                "      \"y\": 6960.0,\n" +
                "      \"points\": 88.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 8284.0,\n" +
                "      \"y\": 6960.0,\n" +
                "      \"points\": 38.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6065.0,\n" +
                "      \"y\": 1430.0,\n" +
                "      \"points\": 78.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6376.0,\n" +
                "      \"y\": 7797.0,\n" +
                "      \"points\": 65.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 8320.0,\n" +
                "      \"y\": 6448.0,\n" +
                "      \"points\": 46.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 8178.0,\n" +
                "      \"y\": 6809.0,\n" +
                "      \"points\": 44.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7728.0,\n" +
                "      \"y\": 6985.0,\n" +
                "      \"points\": 51.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7562.0,\n" +
                "      \"y\": 7157.0,\n" +
                "      \"points\": 55.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 3085.0,\n" +
                "      \"y\": 4905.0,\n" +
                "      \"points\": 190.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 4105.0,\n" +
                "      \"y\": 8854.0,\n" +
                "      \"points\": 35.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7737.0,\n" +
                "      \"y\": 6760.0,\n" +
                "      \"points\": 47.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6825.0,\n" +
                "      \"y\": 7164.0,\n" +
                "      \"points\": 73.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 5475.0,\n" +
                "      \"y\": 647.0,\n" +
                "      \"points\": 56.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7096.0,\n" +
                "      \"y\": 7502.0,\n" +
                "      \"points\": 56.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 4040.0,\n" +
                "      \"y\": 8715.0,\n" +
                "      \"points\": 45.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7531.0,\n" +
                "      \"y\": 6661.0,\n" +
                "      \"points\": 68.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 4301.0,\n" +
                "      \"y\": 8865.0,\n" +
                "      \"points\": 26.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7596.0,\n" +
                "      \"y\": 6872.0,\n" +
                "      \"points\": 49.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6340.0,\n" +
                "      \"y\": 7449.0,\n" +
                "      \"points\": 79.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 8018.0,\n" +
                "      \"y\": 6790.0,\n" +
                "      \"points\": 48.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 3425.0,\n" +
                "      \"y\": 5115.0,\n" +
                "      \"points\": 206.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 2980.0,\n" +
                "      \"y\": 5283.0,\n" +
                "      \"points\": 175.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 5983.0,\n" +
                "      \"y\": 972.0,\n" +
                "      \"points\": 58.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 5885.0,\n" +
                "      \"y\": 1038.0,\n" +
                "      \"points\": 64.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7596.0,\n" +
                "      \"y\": 6515.0,\n" +
                "      \"points\": 68.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7712.0,\n" +
                "      \"y\": 6453.0,\n" +
                "      \"points\": 66.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6017.0,\n" +
                "      \"y\": 1179.0,\n" +
                "      \"points\": 66.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6072.0,\n" +
                "      \"y\": 1057.0,\n" +
                "      \"points\": 63.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7962.0,\n" +
                "      \"y\": 7032.0,\n" +
                "      \"points\": 46.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7145.0,\n" +
                "      \"y\": 7669.0,\n" +
                "      \"points\": 51.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7656.0,\n" +
                "      \"y\": 6947.0,\n" +
                "      \"points\": 56.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 8291.0,\n" +
                "      \"y\": 6861.0,\n" +
                "      \"points\": 40.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6079.0,\n" +
                "      \"y\": 1333.0,\n" +
                "      \"points\": 75.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 8018.0,\n" +
                "      \"y\": 6803.0,\n" +
                "      \"points\": 48.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6569.0,\n" +
                "      \"y\": 7288.0,\n" +
                "      \"points\": 72.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6539.0,\n" +
                "      \"y\": 7307.0,\n" +
                "      \"points\": 78.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 3630.0,\n" +
                "      \"y\": 8861.0,\n" +
                "      \"points\": 35.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 8141.0,\n" +
                "      \"y\": 6846.0,\n" +
                "      \"points\": 28.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7017.0,\n" +
                "      \"y\": 7214.0,\n" +
                "      \"points\": 62.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 5720.0,\n" +
                "      \"y\": 1042.0,\n" +
                "      \"points\": 69.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 3426.0,\n" +
                "      \"y\": 5069.0,\n" +
                "      \"points\": 196.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6110.0,\n" +
                "      \"y\": 1123.0,\n" +
                "      \"points\": 62.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7578.0,\n" +
                "      \"y\": 6692.0,\n" +
                "      \"points\": 63.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 8212.0,\n" +
                "      \"y\": 6970.0,\n" +
                "      \"points\": 39.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7905.0,\n" +
                "      \"y\": 7125.0,\n" +
                "      \"points\": 46.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7817.0,\n" +
                "      \"y\": 6443.0,\n" +
                "      \"points\": 59.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 8266.0,\n" +
                "      \"y\": 6894.0,\n" +
                "      \"points\": 39.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7837.0,\n" +
                "      \"y\": 7020.0,\n" +
                "      \"points\": 50.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7963.0,\n" +
                "      \"y\": 6584.0,\n" +
                "      \"points\": 55.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6081.0,\n" +
                "      \"y\": 970.0,\n" +
                "      \"points\": 62.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6290.0,\n" +
                "      \"y\": 959.0,\n" +
                "      \"points\": 59.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 4225.0,\n" +
                "      \"y\": 8877.0,\n" +
                "      \"points\": 35.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7598.0,\n" +
                "      \"y\": 6519.0,\n" +
                "      \"points\": 66.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6516.0,\n" +
                "      \"y\": 7443.0,\n" +
                "      \"points\": 73.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7943.0,\n" +
                "      \"y\": 7034.0,\n" +
                "      \"points\": 43.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 5787.0,\n" +
                "      \"y\": 624.0,\n" +
                "      \"points\": 54.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 2938.0,\n" +
                "      \"y\": 5152.0,\n" +
                "      \"points\": 169.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 5740.0,\n" +
                "      \"y\": 1291.0,\n" +
                "      \"points\": 78.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 8258.0,\n" +
                "      \"y\": 7162.0,\n" +
                "      \"points\": 29.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 3719.0,\n" +
                "      \"y\": 8770.0,\n" +
                "      \"points\": 44.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 8052.0,\n" +
                "      \"y\": 6972.0,\n" +
                "      \"points\": 43.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 5764.0,\n" +
                "      \"y\": 864.0,\n" +
                "      \"points\": 58.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 5633.0,\n" +
                "      \"y\": 1462.0,\n" +
                "      \"points\": 90.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 5644.0,\n" +
                "      \"y\": 732.0,\n" +
                "      \"points\": 59.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7681.0,\n" +
                "      \"y\": 6910.0,\n" +
                "      \"points\": 54.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 3891.0,\n" +
                "      \"y\": 8738.0,\n" +
                "      \"points\": 44.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 4422.0,\n" +
                "      \"y\": 8841.0,\n" +
                "      \"points\": 44.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 8348.0,\n" +
                "      \"y\": 6775.0,\n" +
                "      \"points\": 34.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6035.0,\n" +
                "      \"y\": 1444.0,\n" +
                "      \"points\": 80.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6131.0,\n" +
                "      \"y\": 688.0,\n" +
                "      \"points\": 49.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 8380.0,\n" +
                "      \"y\": 7137.0,\n" +
                "      \"points\": 32.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 8305.0,\n" +
                "      \"y\": 6675.0,\n" +
                "      \"points\": 43.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6407.0,\n" +
                "      \"y\": 7779.0,\n" +
                "      \"points\": 65.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 8116.0,\n" +
                "      \"y\": 6926.0,\n" +
                "      \"points\": 44.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6640.0,\n" +
                "      \"y\": 7100.0,\n" +
                "      \"points\": 81.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 8385.0,\n" +
                "      \"y\": 7071.0,\n" +
                "      \"points\": 34.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7587.0,\n" +
                "      \"y\": 6502.0,\n" +
                "      \"points\": 63.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 8185.0,\n" +
                "      \"y\": 6345.0,\n" +
                "      \"points\": 51.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 8204.0,\n" +
                "      \"y\": 6625.0,\n" +
                "      \"points\": 47.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6425.0,\n" +
                "      \"y\": 7734.0,\n" +
                "      \"points\": 66.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 8281.0,\n" +
                "      \"y\": 7041.0,\n" +
                "      \"points\": 34.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 8360.0,\n" +
                "      \"y\": 7044.0,\n" +
                "      \"points\": 34.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7665.0,\n" +
                "      \"y\": 6322.0,\n" +
                "      \"points\": 70.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7789.0,\n" +
                "      \"y\": 7185.0,\n" +
                "      \"points\": 48.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 5691.0,\n" +
                "      \"y\": 754.0,\n" +
                "      \"points\": 58.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 3640.0,\n" +
                "      \"y\": 8858.0,\n" +
                "      \"points\": 38.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 3498.0,\n" +
                "      \"y\": 5299.0,\n" +
                "      \"points\": 208.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 3795.0,\n" +
                "      \"y\": 8801.0,\n" +
                "      \"points\": 38.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 5432.0,\n" +
                "      \"y\": 1161.0,\n" +
                "      \"points\": 60.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 2871.0,\n" +
                "      \"y\": 5328.0,\n" +
                "      \"points\": 149.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 5566.0,\n" +
                "      \"y\": 623.0,\n" +
                "      \"points\": 56.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 3593.0,\n" +
                "      \"y\": 5018.0,\n" +
                "      \"points\": 227.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 6113.0,\n" +
                "      \"y\": 718.0,\n" +
                "      \"points\": 51.0,\n" +
                "      \"radius\": 5.0\n" +
                "    },\n" +
                "    {\n" +
                "      \"x\": 7657.0,\n" +
                "      \"y\": 6793.0,\n" +
                "      \"points\": 60.0,\n" +
                "      \"radius\": 5.0\n" +
                "    }\n" +
                "  ],\n" +
                "  \"maxSpeed\": 110.0,\n" +
                "  \"maxAccel\": 10.0,\n" +
                "  \"attackRange\": 150.0,\n" +
                "  \"attackCooldownMs\": 10000.0,\n" +
                "  \"attackDamage\": 30.0,\n" +
                "  \"attackExplosionRadius\": 30.0,\n" +
                "  \"reviveTimeoutSec\": 2.0,\n" +
                "  \"shieldTimeMs\": 5000.0,\n" +
                "  \"shieldCooldownMs\": 40000.0,\n" +
                "  \"transportRadius\": 5.0,\n" +
                "  \"errors\": [\n" +
                "    \"transport b3ad0dfd337abf9b92eaf6496a1f0ee40892429f is dead\",\n" +
                "    \"transport a6c440902445b043eb139ac05cdf6d0f1acbe8db acceleration is too high: 10.00, max allowed 10.00\"\n" +
                "  ]\n" +
                "}";
        Gson gson = new GsonBuilder().create();
        GameState gameState = gson.fromJson(test, GameState.class);
        List<Transport> transportList = new ArrayList<>();
        transportList = gameState.getTransports()
                .stream().map(transport1 -> {
                    Transport transport =  new Transport();
                    transport.setId(transport1.getId());
                    return transport;
                }).collect(Collectors.toList());
        ShootScript shootScript = new ShootScript();
        shootScript.shoot(transportList,gameState);
    }
}
