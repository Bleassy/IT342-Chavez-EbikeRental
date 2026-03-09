const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";

function getAuthHeaders(): HeadersInit {
  const stored = localStorage.getItem("ebike_auth");
  if (stored) {
    try {
      const { token } = JSON.parse(stored);
      if (token) return { "Content-Type": "application/json", Authorization: `Bearer ${token}` };
    } catch { /* ignore */ }
  }
  return { "Content-Type": "application/json" };
}

async function apiGet<T>(path: string): Promise<T> {
  const res = await fetch(`${API_URL}/api${path}`, { headers: getAuthHeaders() });
  if (!res.ok) throw new Error(`API error ${res.status}`);
  const json = await res.json();
  return json.data ?? json;
}

async function apiPost<T>(path: string, body?: unknown): Promise<T> {
  const res = await fetch(`${API_URL}/api${path}`, {
    method: "POST",
    headers: getAuthHeaders(),
    body: body ? JSON.stringify(body) : undefined,
  });
  if (!res.ok) throw new Error(`API error ${res.status}`);
  const json = await res.json();
  return json.data ?? json;
}

async function apiPut<T>(path: string, body?: unknown): Promise<T> {
  const res = await fetch(`${API_URL}/api${path}`, {
    method: "PUT",
    headers: getAuthHeaders(),
    body: body ? JSON.stringify(body) : undefined,
  });
  if (!res.ok) throw new Error(`API error ${res.status}`);
  const json = await res.json();
  return json.data ?? json;
}

async function apiDelete(path: string): Promise<void> {
  const res = await fetch(`${API_URL}/api${path}`, {
    method: "DELETE",
    headers: getAuthHeaders(),
  });
  if (!res.ok) throw new Error(`API error ${res.status}`);
}

export { apiGet, apiPost, apiPut, apiDelete };
