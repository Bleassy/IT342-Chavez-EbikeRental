import { useLocation, useNavigate, Navigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { CheckCircle2, ArrowRight } from "lucide-react";

const BookingConfirmation = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const state = location.state as { bikeName: string; duration: number; totalCost: number } | null;

  if (!state) return <Navigate to="/bikes" replace />;

  return (
    <div className="container flex min-h-[70vh] items-center justify-center py-8">
      <div className="glass-card max-w-md p-10 text-center animate-fade-up">
        <div className="mx-auto flex h-20 w-20 items-center justify-center rounded-full bg-primary/10 mb-6">
          <CheckCircle2 className="h-10 w-10 text-primary" />
        </div>

        <h1 className="font-display text-3xl font-bold text-foreground">Booking Confirmed!</h1>
        <p className="mt-3 text-muted-foreground">Your ride has been booked successfully.</p>

        <div className="mt-8 rounded-xl bg-muted p-6 text-left space-y-3">
          <div className="flex justify-between">
            <span className="text-sm text-muted-foreground">Bike</span>
            <span className="font-medium text-foreground">{state.bikeName}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-sm text-muted-foreground">Duration</span>
            <span className="font-medium text-foreground">{state.duration} hour{state.duration > 1 ? "s" : ""}</span>
          </div>
          <div className="flex justify-between border-t border-border pt-3">
            <span className="font-semibold text-foreground">Total Cost</span>
            <span className="font-display text-xl font-bold text-primary">₱{state.totalCost.toFixed(2)}</span>
          </div>
        </div>

        <div className="mt-8 flex flex-col gap-3">
          <Button className="gradient-primary text-primary-foreground" onClick={() => navigate("/history")}>
            View My Rentals <ArrowRight className="ml-2 h-4 w-4" />
          </Button>
          <Button variant="outline" onClick={() => navigate("/bikes")}>
            Browse More Bikes
          </Button>
        </div>
      </div>
    </div>
  );
};

export default BookingConfirmation;
