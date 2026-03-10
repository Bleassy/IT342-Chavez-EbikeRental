import { useState, useEffect, useCallback } from "react";
import { fetchAllBookings, completeBooking, cancelBooking } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Booking } from "@/types";
import { useToast } from "@/hooks/use-toast";
import { CheckCircle2, XCircle, Loader2, RefreshCw, ArrowLeft, Activity } from "lucide-react";
import { Link } from "react-router-dom";

const POLL_INTERVAL = 5000;

const AdminActiveRentals = () => {
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);
  const { toast } = useToast();

  const loadData = useCallback(async () => {
    try {
      const all = await fetchAllBookings();
      // Only active bookings, sorted newest first
      setBookings(
        all
          .filter((b) => b.bookingStatus === "ACTIVE")
          .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
      );
    } catch (err) {
      console.error("Failed to load active rentals:", err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadData();
    const interval = setInterval(loadData, POLL_INTERVAL);
    return () => clearInterval(interval);
  }, [loadData]);

  const handleComplete = async (id: string) => {
    try {
      await completeBooking(id);
      toast({ title: "Booking completed" });
      loadData();
    } catch {
      toast({ title: "Failed to complete booking", variant: "destructive" });
    }
  };

  const handleCancel = async (id: string) => {
    try {
      await cancelBooking(id);
      toast({ title: "Booking cancelled" });
      loadData();
    } catch {
      toast({ title: "Failed to cancel booking", variant: "destructive" });
    }
  };

  if (loading) {
    return (
      <div className="container flex min-h-[50vh] items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  return (
    <div className="container py-8">
      <Link to="/" className="mb-6 flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground transition-colors">
        <ArrowLeft className="h-4 w-4" /> Back to Dashboard
      </Link>

      <div className="mb-8 flex items-center gap-3 animate-fade-up">
        <div className="rounded-xl bg-green-500/15 p-3">
          <Activity className="h-6 w-6 text-green-600" />
        </div>
        <div>
          <h1 className="font-display text-3xl font-bold text-foreground">Active Rentals</h1>
          <p className="text-muted-foreground">All users with currently active e-bike bookings — {bookings.length} active</p>
        </div>
      </div>

      <div className="mb-4 flex items-center gap-3">
        <Button variant="outline" size="sm" onClick={loadData}>
          <RefreshCw className="mr-2 h-4 w-4" /> Refresh
        </Button>
        <span className="text-xs text-muted-foreground">Auto-refreshes every 5s</span>
      </div>

      {bookings.length === 0 ? (
        <div className="glass-card p-12 text-center animate-fade-up">
          <Activity className="mx-auto h-12 w-12 text-muted-foreground/50" />
          <p className="mt-4 text-lg font-medium text-muted-foreground">No active rentals right now</p>
          <p className="mt-1 text-sm text-muted-foreground">When users book e-bikes, their active rentals will appear here.</p>
        </div>
      ) : (
        <div className="glass-card overflow-hidden animate-fade-up">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b border-border bg-muted/50">
                  <th className="px-4 py-3 text-left text-xs font-semibold uppercase text-muted-foreground">User</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold uppercase text-muted-foreground">Bike</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold uppercase text-muted-foreground">Schedule</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold uppercase text-muted-foreground">Duration</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold uppercase text-muted-foreground">Total</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold uppercase text-muted-foreground">Booked On</th>
                  <th className="px-4 py-3 text-right text-xs font-semibold uppercase text-muted-foreground">Actions</th>
                </tr>
              </thead>
              <tbody>
                {bookings.map((b) => (
                  <tr key={b.id} className="border-b border-border last:border-0 hover:bg-muted/30 transition-colors">
                    <td className="px-4 py-4">
                      <div className="font-medium text-foreground">{b.userName || "Unknown"}</div>
                      <div className="text-xs text-muted-foreground">{b.userEmail || ""}</div>
                    </td>
                    <td className="px-4 py-4 font-medium text-foreground">{b.bikeName}</td>
                    <td className="px-4 py-4 text-sm text-muted-foreground">
                      {b.startTime && (
                        <div>
                          <div>{new Date(b.startTime).toLocaleDateString("en-US", { month: "short", day: "numeric" })} {new Date(b.startTime).toLocaleTimeString("en-US", { hour: "2-digit", minute: "2-digit" })}</div>
                          <div className="text-xs">to {b.endTime && new Date(b.endTime).toLocaleTimeString("en-US", { hour: "2-digit", minute: "2-digit" })}</div>
                        </div>
                      )}
                    </td>
                    <td className="px-4 py-4 text-muted-foreground">{b.rentalDuration}h</td>
                    <td className="px-4 py-4 font-medium text-foreground">₱{b.totalCost.toFixed(2)}</td>
                    <td className="px-4 py-4 text-sm text-muted-foreground">
                      {new Date(b.createdAt).toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" })}
                    </td>
                    <td className="px-4 py-4 text-right">
                      <div className="flex justify-end gap-2">
                        <Button size="sm" variant="outline" onClick={() => handleComplete(b.id)}>
                          <CheckCircle2 className="mr-1.5 h-3.5 w-3.5" /> Complete
                        </Button>
                        <Button size="sm" variant="outline" className="text-destructive" onClick={() => handleCancel(b.id)}>
                          <XCircle className="mr-1.5 h-3.5 w-3.5" /> Cancel
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminActiveRentals;
