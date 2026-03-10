import { Toaster } from "@/components/ui/toaster";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "@/contexts/AuthContext";
import Navbar from "@/components/Navbar";
import ProtectedRoute from "@/components/ProtectedRoute";
import Login from "@/pages/Login";
import Register from "@/pages/Register";
import Dashboard from "@/pages/Dashboard";
import BikeList from "@/pages/BikeList";
import BikeDetails from "@/pages/BikeDetails";
import BookingPage from "@/pages/BookingPage";
import BookingConfirmation from "@/pages/BookingConfirmation";
import RentalHistory from "@/pages/RentalHistory";
import AdminPanel from "@/pages/AdminPanel";
import AdminActiveRentals from "@/pages/AdminActiveRentals";
import AdminAllRides from "@/pages/AdminAllRides";
import Profile from "@/pages/Profile";
import NotFound from "@/pages/NotFound";
import GoogleCallback from "@/pages/GoogleCallback";

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
