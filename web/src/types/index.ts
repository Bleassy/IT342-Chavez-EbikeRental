export type UserRole = "USER" | "ADMIN";

export interface User {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: UserRole;
  phone?: string;
  address?: string;
  nickname?: string;
  profilePictureUrl?: string;
}

export type BikeStatus = "AVAILABLE" | "RENTED" | "MAINTENANCE";

export interface Bike {
  id: string;
  name: string;
  batteryLevel: number;
  pricePerHour: number;
  status: BikeStatus;
  image?: string;
  description?: string;
}

export type BookingStatus = "ACTIVE" | "COMPLETED" | "CANCELLED";

export interface Booking {
  id: string;
  userId: string;
  userName?: string;
  userEmail?: string;
  bikeId: string;
  bikeName: string;
  rentalDuration: number;
  totalCost: number;
  bookingStatus: BookingStatus;
  startTime?: string;
  endTime?: string;
  createdAt: string;
}

export interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
}

export interface RegisterData {
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  password: string;
  confirmPassword: string;
  phone?: string;
  address?: string;
}

export interface LoginData {
  email: string;
  password: string;
}
