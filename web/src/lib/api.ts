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

// Backend BikeDTO shape
export interface BackendBike {
  id: number;
  bikeCode: string;
  model: string;
  brand: string;
  color: string;
  year: number;
  type: string;
  pricePerHour: number;
  pricePerDay: number;
  status: string;
  description: string;
  imageUrl: string | null;
  condition: string;
  batteryLevel: number;
  location: string;
  createdAt: string;
}

// Backend BookingDTO shape
export interface BackendBooking {
  id: number;
  userId: number;
  userName?: string;
  userEmail?: string;
  bikeId: number;
  startTime: string;
  endTime: string;
  status: string;
  totalPrice: number;
  notes?: string;
  createdAt: string;
}

import { Bike, Booking } from "@/types";

/** Map backend bike to frontend Bike */
export function mapBike(b: BackendBike): Bike {
  return {
    id: b.id.toString(),
    name: `${b.brand} ${b.model}`,
    batteryLevel: b.batteryLevel ?? 100,
    pricePerHour: b.pricePerHour,
    status: b.status as Bike["status"],
    image: b.imageUrl || undefined,
    description: b.description || undefined,
  };
}

/** Map backend booking to frontend Booking (with optional bike name) */
export function mapBooking(b: BackendBooking, bikeName?: string): Booking {
  const start = new Date(b.startTime);
  const end = new Date(b.endTime);
  const durationMs = end.getTime() - start.getTime();
  const durationHours = Math.max(1, Math.round(durationMs / (1000 * 60 * 60)));

  // Map backend status to frontend status
  let bookingStatus: Booking["bookingStatus"] = "ACTIVE";
  if (b.status === "COMPLETED") bookingStatus = "COMPLETED";
  else if (b.status === "CANCELLED") bookingStatus = "CANCELLED";
  // PENDING and CONFIRMED are both "ACTIVE" in the frontend

  return {
    id: b.id.toString(),
    userId: b.userId.toString(),
    userName: b.userName || undefined,
    userEmail: b.userEmail || undefined,
    bikeId: b.bikeId.toString(),
    bikeName: bikeName || `Bike #${b.bikeId}`,
    rentalDuration: durationHours,
    totalCost: b.totalPrice,
    bookingStatus,
    startTime: b.startTime,
    endTime: b.endTime,
    createdAt: b.createdAt,
  };
}

// === API functions ===

export async function fetchBikes(): Promise<Bike[]> {
  const data = await apiGet<BackendBike[]>("/bikes");
  return data.map(mapBike);
}

export async function fetchBike(id: string): Promise<Bike> {
  const data = await apiGet<BackendBike>(`/bikes/${id}`);
  return mapBike(data);
}

export async function fetchUserBookings(userId: string): Promise<Booking[]> {
  const bookings = await apiGet<BackendBooking[]>(`/bookings/user/${userId}`);
  // Fetch all bikes to resolve names
  const bikes = await apiGet<BackendBike[]>("/bikes");
  const bikeMap = new Map(bikes.map((b) => [b.id, `${b.brand} ${b.model}`]));
  return bookings.map((b) => mapBooking(b, bikeMap.get(b.bikeId)));
}

export async function fetchAllBookings(): Promise<Booking[]> {
  const bookings = await apiGet<BackendBooking[]>("/bookings");
  const bikes = await apiGet<BackendBike[]>("/bikes");
  const bikeMap = new Map(bikes.map((b) => [b.id, `${b.brand} ${b.model}`]));
  return bookings.map((b) => mapBooking(b, bikeMap.get(b.bikeId)));
}

export async function createBooking(
  userId: string,
  bikeId: string,
  startTime: string,
  endTime: string,
): Promise<BackendBooking> {
  const params = new URLSearchParams({ userId, bikeId, startTime, endTime });
  const res = await fetch(`${API_URL}/api/bookings?${params}`, {
    method: "POST",
    headers: getAuthHeaders(),
  });
  if (!res.ok) throw new Error(`Booking failed: ${res.status}`);
  const json = await res.json();
  return json.data ?? json;
}

export async function completeBooking(bookingId: string): Promise<void> {
  await apiPut(`/bookings/${bookingId}/complete`);
}

export async function cancelBooking(bookingId: string): Promise<void> {
  await apiPut(`/bookings/${bookingId}/cancel`);
}

export async function confirmBooking(bookingId: string): Promise<void> {
  await apiPut(`/bookings/${bookingId}/confirm`);
}

// === Admin: Bike Management ===

export interface CreateBikePayload {
  bikeCode: string;
  model: string;
  brand: string;
  color: string;
  year: number;
  type: string;
  pricePerHour: number;
  pricePerDay: number;
  status: string;
  description: string;
  imageUrl?: string;
  condition: string;
  batteryLevel: number;
  location: string;
}

export async function createBike(payload: CreateBikePayload): Promise<BackendBike> {
  return apiPost<BackendBike>("/bikes", payload);
}

export async function updateBike(id: string, payload: Partial<CreateBikePayload>): Promise<BackendBike> {
  // Backend expects the full entity, so we merge with what we have
  const res = await fetch(`${API_URL}/api/bikes/${id}`, {
    method: "PUT",
    headers: getAuthHeaders(),
    body: JSON.stringify(payload),
  });
  if (!res.ok) throw new Error(`Update failed: ${res.status}`);
  const json = await res.json();
  return json.data ?? json;
}

export async function deleteBike(id: string): Promise<void> {
  await apiDelete(`/bikes/${id}`);
}

export async function updateBikeStatus(id: string, status: string): Promise<void> {
  const res = await fetch(`${API_URL}/api/bikes/${id}/status?status=${status}`, {
    method: "PUT",
    headers: getAuthHeaders(),
  });
  if (!res.ok) throw new Error(`Status update failed: ${res.status}`);
}

// === Admin: Users ===

export interface BackendUser {
  id: number;
  name: string;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  phone?: string;
  address?: string;
  nickname?: string;
  profilePictureUrl?: string;
  createdAt: string;
}

export async function fetchAllUsers(): Promise<BackendUser[]> {
  return apiGet<BackendUser[]>("/admin/users");
}

// === Profile ===

export interface ProfileData {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  phone: string | null;
  address: string | null;
  nickname: string | null;
  profilePictureUrl: string | null;
  role: string;
  isActive: boolean;
  createdAt: string;
}

export async function fetchProfile(): Promise<ProfileData> {
  return apiGet<ProfileData>("/profile");
}

export async function updateProfile(data: {
  firstName?: string;
  lastName?: string;
  phone?: string;
  address?: string;
  nickname?: string;
  profilePictureUrl?: string;
}): Promise<ProfileData> {
  return apiPut<ProfileData>("/profile", data);
}

export { apiGet, apiPost, apiPut, apiDelete };
