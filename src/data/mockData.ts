import { Bike, Booking, User } from "@/types";

export const mockBikes: Bike[] = [
  {
    id: "1",
    name: "Thunder Volt X1",
    batteryLevel: 95,
    pricePerHour: 8.5,
    status: "AVAILABLE",
    description: "High-performance city cruiser with extended range battery. Perfect for daily commutes.",
  },
  {
    id: "2",
    name: "EcoRide Pro 500",
    batteryLevel: 78,
    pricePerHour: 6.0,
    status: "AVAILABLE",
    description: "Eco-friendly commuter bike with regenerative braking. Lightweight and agile.",
  },
  {
    id: "3",
    name: "Trailblazer E-MTB",
    batteryLevel: 60,
    pricePerHour: 12.0,
    status: "AVAILABLE",
    description: "Off-road capable electric mountain bike with full suspension.",
  },
  {
    id: "4",
    name: "Urban Glide S2",
    batteryLevel: 88,
    pricePerHour: 7.0,
    status: "RENTED",
    description: "Sleek urban design with integrated lights and fenders.",
  },
  {
    id: "5",
    name: "Cargo Master E1",
    batteryLevel: 45,
    pricePerHour: 10.0,
    status: "MAINTENANCE",
    description: "Heavy-duty cargo e-bike for hauling groceries and packages.",
  },
  {
    id: "6",
    name: "Sprint Racer 3000",
    batteryLevel: 100,
    pricePerHour: 15.0,
    status: "AVAILABLE",
    description: "Speed-focused e-bike with sport geometry and powerful motor.",
  },
];

export const mockUsers: User[] = [
  {
    id: "user-1",
    username: "john_doe",
    email: "john@example.com",
    firstName: "John",
    lastName: "Doe",
    role: "USER",
  },
  {
    id: "admin-1",
    username: "admin",
    email: "admin@ebike.com",
    firstName: "Admin",
    lastName: "User",
    role: "ADMIN",
  },
];

export const mockBookings: Booking[] = [
  {
    id: "b1",
    userId: "user-1",
    bikeId: "4",
    bikeName: "Urban Glide S2",
    rentalDuration: 3,
    totalCost: 21.0,
    bookingStatus: "ACTIVE",
    createdAt: "2026-02-24T09:00:00Z",
  },
  {
    id: "b2",
    userId: "user-1",
    bikeId: "1",
    bikeName: "Thunder Volt X1",
    rentalDuration: 2,
    totalCost: 17.0,
    bookingStatus: "COMPLETED",
    createdAt: "2026-02-22T14:00:00Z",
  },
  {
    id: "b3",
    userId: "user-1",
    bikeId: "3",
    bikeName: "Trailblazer E-MTB",
    rentalDuration: 5,
    totalCost: 60.0,
    bookingStatus: "COMPLETED",
    createdAt: "2026-02-20T10:30:00Z",
  },
];
