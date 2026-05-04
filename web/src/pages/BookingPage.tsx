import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { format } from "date-fns";
import { fetchBike, createBooking } from "@/lib/api";
import { useAuth } from "@/contexts/AuthContext";
import { useToast } from "@/hooks/use-toast";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Calendar } from "@/components/ui/calendar";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { ArrowLeft, Zap, Clock, DollarSign, CalendarIcon, Loader2 } from "lucide-react";
import { cn } from "@/lib/utils";
import { Bike } from "@/types";
import { StripePaymentComponent } from "@/components/StripePayment";
import { PaymentMethodSelector } from "@/components/PaymentMethodSelector";

type BookingStage = "booking" | "paymentMethod" | "stripePayment";

const BookingPage = () => {
  const { bikeId } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const { toast } = useToast();
  
  const [bike, setBike] = useState<Bike | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [duration, setDuration] = useState(1);
  const [rentalDate, setRentalDate] = useState<Date | undefined>();
  const [stage, setStage] = useState<BookingStage>("booking");
  const [bookingId, setBookingId] = useState<number | null>(null);
  const [startHour, setStartHour] = useState(() => {
    const now = new Date();
    const hours = now.getHours().toString().padStart(2, "0");
    const minutes = (Math.ceil(now.getMinutes() / 15) * 15 === 60 ? "00" : (Math.ceil(now.getMinutes() / 15) * 15).toString().padStart(2, "0"));
    return `${hours}:${minutes}`;
  });

  useEffect(() => {
    if (!bikeId) return;
    fetchBike(bikeId)
      .then(setBike)
      .catch(() => setBike(null))
      .finally(() => setLoading(false));
  }, [bikeId]);

  if (loading) {
    return (
      <div className="container flex min-h-[50vh] items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  if (!bike || bike.status !== "AVAILABLE") {
    return (
      <div className="container py-20 text-center">
        <h1 className="font-display text-2xl font-bold">Bike not available</h1>
        <Button variant="outline" className="mt-4" onClick={() => navigate("/bikes")}>Back to Fleet</Button>
      </div>
    );
  }

  const totalCost = bike.pricePerHour * duration;

  const handleConfirm = async () => {
    if (!user || !bikeId) return;
    const selectedDate = rentalDate || new Date();
    const [h, m] = startHour.split(":").map(Number);
    const startTime = new Date(selectedDate);
    startTime.setHours(h, m, 0, 0);
    const endTime = new Date(startTime.getTime() + duration * 60 * 60 * 1000);

    const formatLocalISO = (d: Date) => {
      const pad = (n: number) => n.toString().padStart(2, "0");
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
    };

    setSubmitting(true);
    try {
      const response = await createBooking(user.id, bikeId, formatLocalISO(startTime), formatLocalISO(endTime));
      const newBookingId = response?.id || response?.data?.id;
      if (newBookingId) {
        setBookingId(newBookingId);
        setStage("paymentMethod");
        toast({ title: "Booking created!", description: "Now choose a payment method." });
      } else {
        throw new Error("No booking ID returned");
      }
    } catch (err) {
      toast({ title: "Booking failed", description: "Could not create booking. Please try again.", variant: "destructive" });
    } finally {
      setSubmitting(false);
    }
  };

  const handleSelectPaymentMethod = (method: "online" | "cash") => {
    if (method === "online") {
      setStage("stripePayment");
    } else {
      toast({ title: "Booking Confirmed!", description: "Please pay cash when you pick up the bike." });
      navigate("/booking/confirmation", {
        state: { bikeName: bike.name, duration, totalCost, bikeId: bike.id, bookingId, paymentMethod: "cash", rentalDate: rentalDate ? format(rentalDate, "PPP") : format(new Date(), "PPP") }
      });
    }
  };

  const handlePaymentSuccess = (paymentIntentId: string) => {
    toast({ title: "Payment Successful!", description: "Your booking is confirmed." });
    navigate("/booking/confirmation", {
      state: { bikeName: bike.name, duration, totalCost, bikeId: bike.id, bookingId, paymentIntentId, paymentMethod: "online", rentalDate: rentalDate ? format(rentalDate, "PPP") : format(new Date(), "PPP") }
    });
  };

  const handlePaymentError = () => {
    setStage("paymentMethod");
  };

  return (
    <div className="container max-w-2xl py-8">
      <button onClick={() => stage === "booking" ? navigate(-1) : setStage(stage === "stripePayment" ? "paymentMethod" : "booking")} className="mb-6 flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground">
        <ArrowLeft className="h-4 w-4" /> Back
      </button>

      {stage === "booking" && (
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
              <p className="text-sm text-muted-foreground">${bike.pricePerHour}/hour • Battery: {bike.batteryLevel}%</p>
            </div>
          </div>
        </div>

        <div className="mt-8 space-y-6">
          <div className="space-y-2">
            <Label className="text-base">Rental Date</Label>
            <Popover>
              <PopoverTrigger asChild>
                <Button variant="outline" className={cn("w-full justify-start text-left font-normal", !rentalDate && "text-muted-foreground")}>
                  <CalendarIcon className="mr-2 h-4 w-4" />
                  {rentalDate ? format(rentalDate, "PPP") : "Pick a date"}
                </Button>
              </PopoverTrigger>
              <PopoverContent className="w-auto p-0" align="start">
                <Calendar mode="single" selected={rentalDate} onSelect={setRentalDate} disabled={(date) => date < new Date(new Date().setHours(0, 0, 0, 0))} initialFocus className={cn("p-3 pointer-events-auto")} />
              </PopoverContent>
            </Popover>
          </div>

          <div className="space-y-2">
            <Label className="text-base">Start Time</Label>
            <div className="flex items-center gap-3">
              <Clock className="h-5 w-5 text-muted-foreground" />
              <Input type="time" value={startHour} onChange={(e) => setStartHour(e.target.value)} className="w-40 font-display text-lg font-semibold" />
              <span className="text-sm text-muted-foreground">to {(() => { const [h, m] = startHour.split(":").map(Number); const end = new Date(); end.setHours(h + duration, m, 0, 0); return end.toLocaleTimeString("en-US", { hour: "2-digit", minute: "2-digit" }); })()}</span>
            </div>
          </div>

          <div className="space-y-2">
            <Label className="text-base">Rental Duration (hours)</Label>
            <div className="flex items-center gap-4">
              <Button variant="outline" size="sm" onClick={() => setDuration(Math.max(1, duration - 1))} disabled={duration <= 1}>minus</Button>
              <Input type="number" min={1} max={24} value={duration} onChange={(e) => setDuration(Math.max(1, Math.min(24, parseInt(e.target.value) || 1)))} className="w-24 text-center font-display text-xl font-bold" />
              <Button variant="outline" size="sm" onClick={() => setDuration(Math.min(24, duration + 1))} disabled={duration >= 24}>plus</Button>
            </div>
          </div>

          <div className="flex gap-2 flex-wrap">
            {[1, 2, 4, 8].map((h) => (<button key={h} onClick={() => setDuration(h)} className={`rounded-lg px-4 py-2 text-sm font-medium transition-colors ${duration === h ? "gradient-primary text-primary-foreground" : "bg-muted text-muted-foreground hover:text-foreground"}`}>{h}h</button>))}
          </div>

          <div className="rounded-xl border border-border p-6 space-y-4">
            <h3 className="font-display font-semibold text-foreground">Cost Breakdown</h3>
            <div className="flex justify-between text-sm"><span className="flex items-center gap-2 text-muted-foreground"><Clock className="h-4 w-4" /> Duration</span><span className="font-medium">{duration} hour{duration > 1 ? "s" : ""}</span></div>
            <div className="flex justify-between text-sm"><span className="flex items-center gap-2 text-muted-foreground"><DollarSign className="h-4 w-4" /> Rate</span><span className="font-medium">${bike.pricePerHour} per hour</span></div>
            <div className="border-t border-border pt-4 flex justify-between"><span className="font-display font-semibold text-foreground">Total</span><span className="font-display text-2xl font-bold text-primary">${totalCost.toFixed(2)}</span></div>
          </div>

          <Button className="w-full gradient-primary text-primary-foreground py-6 text-lg" onClick={handleConfirm} disabled={submitting}>
            {submitting ? <><Loader2 className="mr-2 h-5 w-5 animate-spin" /> Booking...</> : `Confirm Booking - $${totalCost.toFixed(2)}`}
          </Button>
        </div>
      </div>
      )}

      {stage === "paymentMethod" && bike && (
      <div className="glass-card p-8 animate-fade-up">
        <PaymentMethodSelector amount={bike.pricePerHour * duration} bikeName={bike.name} onSelectOnline={() => handleSelectPaymentMethod("online")} onSelectCash={() => handleSelectPaymentMethod("cash")} />
      </div>
      )}

      {stage === "stripePayment" && bookingId && bike && (
      <div className="glass-card p-8 animate-fade-up">
        <h1 className="font-display text-3xl font-bold text-foreground">Complete Payment</h1>
        <p className="mt-2 text-muted-foreground">Secure online payment for your {bike.name} rental</p>
        <div className="mt-8 rounded-xl bg-muted p-6">
          <div className="flex items-center gap-4">
            <div className="flex h-14 w-14 items-center justify-center rounded-xl gradient-primary">
              <DollarSign className="h-7 w-7 text-primary-foreground" />
            </div>
            <div>
              <h2 className="font-display text-xl font-semibold text-foreground">Total Amount Due</h2>
              <p className="font-display text-2xl font-bold text-primary">${(bike.pricePerHour * duration).toFixed(2)}</p>
            </div>
          </div>
        </div>
        <div className="mt-8">
          <StripePaymentComponent bookingId={bookingId} amount={bike.pricePerHour * duration} onSuccess={handlePaymentSuccess} onError={handlePaymentError} />
        </div>
      </div>
      )}
    </div>
  );
};

export default BookingPage;