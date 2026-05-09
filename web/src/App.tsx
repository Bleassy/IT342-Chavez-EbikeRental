import { Toaster } from "@/components/ui/toaster";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "@/contexts/AuthContext";
import Navbar from "@/pages/Navbar";
import ProtectedRoute from "@/pages/ProtectedRoute";
import Login from "@/auth/Login";
import Register from "@/auth/Register";
import Dashboard from "@/pages/Dashboard";
import BikeList from "@/bike/BikeList";
import BikeDetails from "@/bike/BikeDetails";
import BookingPage from "@/booking/BookingPage";
import BookingConfirmation from "@/booking/BookingConfirmation";
import RentalHistory from "@/pages/RentalHistory";
import AdminPanel from "@/admin/AdminPanel";
import AdminActiveRentals from "@/admin/AdminActiveRentals";
import AdminAllRides from "@/admin/AdminAllRides";
import Profile from "@/pages/Profile";
import NotFound from "@/auth/NotFound";
import GoogleCallback from "@/auth/GoogleCallback";

const queryClient = new QueryClient();

const App = () => (
  <QueryClientProvider client={queryClient}>
    <TooltipProvider>
      <Toaster />
      <Sonner />
      <BrowserRouter>
        <AuthProvider>
          <Navbar />
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/auth/google/callback" element={<GoogleCallback />} />
            <Route path="/" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
            <Route path="/bikes" element={<ProtectedRoute><BikeList /></ProtectedRoute>} />
            <Route path="/bikes/:id" element={<ProtectedRoute><BikeDetails /></ProtectedRoute>} />
            <Route path="/booking/:bikeId" element={<ProtectedRoute><BookingPage /></ProtectedRoute>} />
            <Route path="/booking/confirmation" element={<ProtectedRoute><BookingConfirmation /></ProtectedRoute>} />
            <Route path="/history" element={<ProtectedRoute><RentalHistory /></ProtectedRoute>} />
            <Route path="/admin" element={<ProtectedRoute requiredRole="ADMIN"><AdminPanel /></ProtectedRoute>} />
            <Route path="/admin/active-rentals" element={<ProtectedRoute requiredRole="ADMIN"><AdminActiveRentals /></ProtectedRoute>} />
            <Route path="/admin/all-rides" element={<ProtectedRoute requiredRole="ADMIN"><AdminAllRides /></ProtectedRoute>} />
            <Route path="/profile" element={<ProtectedRoute><Profile /></ProtectedRoute>} />
            <Route path="*" element={<NotFound />} />
          </Routes>
        </AuthProvider>
      </BrowserRouter>
    </TooltipProvider>
  </QueryClientProvider>
);

export default App;
