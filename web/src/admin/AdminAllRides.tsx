import { useState, useEffect, useCallback } from "react";
import { fetchAllBookings } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Booking } from "@/types";
import { Loader2, RefreshCw, ArrowLeft, Clock } from "lucide-react";
import { Link } from "react-router-dom";

const POLL_INTERVAL = 5000;

const AdminAllRides = () => {
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<"ALL" | "COMPLETED" | "CANCELLED">("ALL");

  const loadData = useCallback(async () => {
    try {
      const all = await fetchAllBookings();
      // Only completed + cancelled, sorted newest first
      setBookings(
        all
          .filter((b) => b.bookingStatus === "COMPLETED" || b.bookingStatus === "CANCELLED")
          .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
      );
    } catch (err) {
      console.error("Failed to load rides:", err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadData();
    const interval = setInterval(loadData, POLL_INTERVAL);
    return () => clearInterval(interval);
  }, [loadData]);

  const filtered = filter === "ALL" ? bookings : bookings.filter((b) => b.bookingStatus === filter);
  const completedCount = bookings.filter((b) => b.bookingStatus === "COMPLETED").length;
  const cancelledCount = bookings.filter((b) => b.bookingStatus === "CANCELLED").length;

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
        <div className="rounded-xl bg-primary/15 p-3">
          <Clock className="h-6 w-6 text-primary" />
        </div>
        <div>
          <h1 className="font-display text-3xl font-bold text-foreground">All Rides</h1>
          <p className="text-muted-foreground">All completed and cancelled bookings across all users — {bookings.length} total</p>
        </div>
      </div>

      <div className="mb-4 flex items-center gap-3">
        <Button variant="outline" size="sm" onClick={loadData}>
          <RefreshCw className="mr-2 h-4 w-4" /> Refresh
        </Button>
        <span className="text-xs text-muted-foreground">Auto-refreshes every 5s</span>
      </div>

      {/* Filters */}
      <div className="mb-6 flex flex-wrap gap-2">
        {([
          { key: "ALL" as const, label: "All", count: bookings.length },
          { key: "COMPLETED" as const, label: "Completed", count: completedCount },
          { key: "CANCELLED" as const, label: "Cancelled", count: cancelledCount },
        ]).map(({ key, label, count }) => {
          const isActive = filter === key;
          return (
            <Button
              key={key}
              size="sm"
              variant={isActive ? "default" : "outline"}
              className={isActive ? (
                key === "COMPLETED" ? "bg-gray-600 hover:bg-gray-700 text-white"
                : key === "CANCELLED" ? "bg-red-600 hover:bg-red-700 text-white"
                : "gradient-primary text-primary-foreground"
              ) : ""}
              onClick={() => setFilter(key)}
            >
              {label}
              <span className={`ml-1.5 rounded-full px-1.5 py-0.5 text-xs ${isActive ? "bg-white/20" : "bg-muted"}`}>{count}</span>
            </Button>
          );
        })}
      </div>

      {filtered.length === 0 ? (
        <div className="glass-card p-12 text-center animate-fade-up">
          <Clock className="mx-auto h-12 w-12 text-muted-foreground/50" />
          <p className="mt-4 text-lg font-medium text-muted-foreground">No rides found</p>
          <p className="mt-1 text-sm text-muted-foreground">Completed and cancelled bookings from all users will appear here.</p>
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
                  <th className="px-4 py-3 text-left text-xs font-semibold uppercase text-muted-foreground">Status</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold uppercase text-muted-foreground">Booked On</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((b) => (
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
                    <td className="px-4 py-4">
                      <span className={`rounded-full px-3 py-1 text-xs font-semibold ${
                        b.bookingStatus === "COMPLETED" ? "bg-muted text-muted-foreground"
                        : "bg-destructive/15 text-destructive"
                      }`}>
                        {b.bookingStatus}
                      </span>
                    </td>
                    <td className="px-4 py-4 text-sm text-muted-foreground">
                      {new Date(b.createdAt).toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" })}
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

export default AdminAllRides;
