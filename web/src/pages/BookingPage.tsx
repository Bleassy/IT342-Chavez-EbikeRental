import { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { format } from "date-fns";
import { mockBikes } from "@/data/mockData";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Calendar } from "@/components/ui/calendar";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { ArrowLeft, Zap, Clock, DollarSign, CalendarIcon } from "lucide-react";
import { cn } from "@/lib/utils";

const BookingPage = () => {
  const { bikeId } = useParams();
  const navigate = useNavigate();
  const bike = mockBikes.find((b) => b.id === bikeId);
  const [duration, setDuration] = useState(1);
  const [rentalDate, setRentalDate] = useState<Date>();

  if (!bike || bike.status !== "AVAILABLE") {
    return (
      <div className="container py-20 text-center">
        <h1 className="font-display text-2xl font-bold">Bike not available</h1>
        <Button variant="outline" className="mt-4" onClick={() => navigate("/bikes")}>Back to Fleet</Button>
      </div>
    );
  }

  const totalCost = bike.pricePerHour * duration;

  const handleConfirm = () => {
    navigate("/booking/confirmation", {
      state: { bikeName: bike.name, duration, totalCost, bikeId: bike.id, rentalDate: rentalDate ? format(rentalDate, "PPP") : "Not selected" },
    });
  };

  return (
    <div className="container max-w-2xl py-8">
      <button onClick={() => navigate(-1)} className="mb-6 flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground">
        <ArrowLeft className="h-4 w-4" /> Back
      </button>

      <div className="glass-card p-8 animate-fade-up">
        <h1 className="font-display text-3xl font-bold text-foreground">Book Your Ride</h1>
        <p className="mt-2 text-muted-foreground">Complete your booking for {bike.name}</p>

        <div className="mt-8 rounded-xl bg-muted p-6">
          <div className="flex items-center gap-4">
            <div className="flex h-14 w-14 items-center justify-center rounded-xl gradient-primary">
              <Zap className="h-7 w-7 text-primary-foreground" />
            </div>
            <div>
              <h2 className="font-display text-xl font-semibold text-foreground">{bike.name}</h2>
              <p className="text-sm text-muted-foreground">₱{bike.pricePerHour}/hour • Battery: {bike.batteryLevel}%</p>
            </div>
          </div>
        </div>

        <div className="mt-8 space-y-6">
          {/* Rental Date */}
          <div className="space-y-2">
            <Label className="text-base">Rental Date</Label>
            <Popover>
              <PopoverTrigger asChild>
                <Button
                  variant="outline"
                  className={cn(
                    "w-full justify-start text-left font-normal",
                    !rentalDate && "text-muted-foreground"
                  )}
                >
                  <CalendarIcon className="mr-2 h-4 w-4" />
                  {rentalDate ? format(rentalDate, "PPP") : "Pick a date"}
                </Button>
              </PopoverTrigger>
              <PopoverContent className="w-auto p-0" align="start">
                <Calendar
                  mode="single"
                  selected={rentalDate}
                  onSelect={setRentalDate}
                  disabled={(date) => date < new Date(new Date().setHours(0, 0, 0, 0))}
                  initialFocus
                  className={cn("p-3 pointer-events-auto")}
                />
              </PopoverContent>
            </Popover>
          </div>

          <div className="space-y-2">
            <Label className="text-base">Rental Duration (hours)</Label>
            <div className="flex items-center gap-4">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setDuration(Math.max(1, duration - 1))}
                disabled={duration <= 1}
              >
                −
              </Button>
              <Input
                type="number"
                min={1}
                max={24}
                value={duration}
                onChange={(e) => setDuration(Math.max(1, Math.min(24, parseInt(e.target.value) || 1)))}
                className="w-24 text-center font-display text-xl font-bold"
              />
              <Button
                variant="outline"
                size="sm"
                onClick={() => setDuration(Math.min(24, duration + 1))}
                disabled={duration >= 24}
              >
                +
              </Button>
            </div>
          </div>

          {/* Quick select */}
          <div className="flex gap-2 flex-wrap">
            {[1, 2, 4, 8].map((h) => (
              <button
                key={h}
                onClick={() => setDuration(h)}
                className={`rounded-lg px-4 py-2 text-sm font-medium transition-colors ${
                  duration === h ? "gradient-primary text-primary-foreground" : "bg-muted text-muted-foreground hover:text-foreground"
                }`}
              >
                {h}h
              </button>
            ))}
          </div>

          {/* Cost breakdown */}
          <div className="rounded-xl border border-border p-6 space-y-4">
            <h3 className="font-display font-semibold text-foreground">Cost Breakdown</h3>
            <div className="flex justify-between text-sm">
              <span className="flex items-center gap-2 text-muted-foreground">
                <Clock className="h-4 w-4" /> Duration
              </span>
              <span className="font-medium">{duration} hour{duration > 1 ? "s" : ""}</span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="flex items-center gap-2 text-muted-foreground">
                <DollarSign className="h-4 w-4" /> Rate
              </span>
              <span className="font-medium">₱{bike.pricePerHour}/hr</span>
            </div>
            <div className="border-t border-border pt-4 flex justify-between">
              <span className="font-display font-semibold text-foreground">Total</span>
              <span className="font-display text-2xl font-bold text-primary">₱{totalCost.toFixed(2)}</span>
            </div>
          </div>

          <Button
            className="w-full gradient-primary text-primary-foreground py-6 text-lg"
            onClick={handleConfirm}
          >
            Confirm Booking — ₱{totalCost.toFixed(2)}
          </Button>
        </div>
      </div>
    </div>
  );
};

export default BookingPage;
