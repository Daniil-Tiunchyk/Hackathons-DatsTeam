const token = process.env.REACT_APP_API_TOKEN;
const url = "https://games-test.datsteam.dev/api/towers";

export async function fetchTowers(method = "GET", body = null) {
  try {
    const response = await fetch(url, {
      method,
      headers: {
        "Content-Type": "application/json",
        "X-AUTH-TOKEN": token,
      },
      body: body ? JSON.stringify(body) : undefined,
    });

    if (!response.ok) {
      throw new Error(`Ошибка ${response.status}: ${response.statusText}`);
    }

    return await response.json();
  } catch (error) {
    console.error("Ошибка запроса:", error);
    throw error;
  }
}
