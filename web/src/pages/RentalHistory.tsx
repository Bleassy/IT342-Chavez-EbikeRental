import { useState, useEffect } from "react";
import { fetchUserBookings, cancelBooking } from "@/lib/api";
import { useAuth } from "@/contexts/AuthContext";
import { Booking } from "@/types";
import { Clock, DollarSign, Bike, Loader2, XCircle, ChevronDown, CalendarDays, MapPin, Timer } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useToast } from "@/hooks/use-toast";

const statusStyles: Record<string, string> = {
  ACTIVE: "bg-success/15 text-success",
  COMPLETED: "bg-muted text-muted-foreground",
  CANCELLED: "bg-destructive/15 text-destructive",
};

const RentalHistory = () => {
  const { user } = useAuth();
  const { toast } = useToast();
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);
  const [cancellingId, setCancellingId] = useState<string | null>(null);
  const [filter, setFilter] = useState<"ALL" | "ACTIVE" | "COMPLETED" | "CANCELLED">("ALL");
  const [expandedId, setExpandedId] = useState<string | null>(null);

  // Sort bookings: ACTIVE first, then by newest date, COMPLETED & CANCELLED at the bottom
  const statusOrder: Record<string, number> = { ACTIVE: 0, COMPLETED: 1, CANCELLED: 2 };
  const sortedBookings = [...bookings].sort((a, b) => {
    const orderDiff = (statusOrder[a.bookingStatus] ?? 9) - (statusOrder[b.bookingStatus] ?? 9);
    if (orderDiff !== 0) return orderDiff;
    return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
  });
  const filteredBookings = filter === "ALL" ? sortedBookings : sortedBookings.filter((b) => b.bookingStatus === filter);

  const loadBookings = () => {
    if (!user) return;
    fetchUserBookings(user.id)
      .then(setBookings)
      .catch((err) => console.error("Failed to fetch bookings:", err))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadBookings();
  }, [user]);

  const handleCancel = async (bookingId: string) => {
    setCancellingId(bookingId);
    try {
      await cancelBooking(bookingId);
      toast({ title: "Booking cancelled", description: "Your booking has been cancelled and the bike is now available." });
      loadBookings();
    } catch {
      toast({ title: "Failed to cancel", description: "Could not cancel the booking.", variant: "destructive" });
    } finally {
      setCancellingId(null);
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
      <div className="mb-8 animate-fade-up">
        <h1 className="font-display text-3xl font-bold text-foreground">Rental History</h1>
        <p className="mt-2 text-muted-foreground">Your past and current rentals</p>
      </div>

      {/* Status Filters */}
      <div className="mb-6 flex flex-wrap gap-2 animate-fade-up">
        {(["ALL", "ACTIVE", "COMPLETED", "CANCELLED"] as const).map((status) => {
          const count = status === "ALL" ? bookings.length : bookings.filter((b) => b.bookingStatus === status).length;
          const isActive = filter === status;
          return (
            <Button
              key={status}
              size="sm"
              variant={isActive ? "default" : "outline"}
              className={isActive ? (
                status === "ACTIVE" ? "bg-green-600 hover:bg-green-700 text-white"
                : status === "COMPLETED" ? "bg-gray-600 hover:bg-gray-700 text-white"
                : status === "CANCELLED" ? "bg-red-600 hover:bg-red-700 text-white"
                : "gradient-primary text-primary-foreground"
              ) : ""}
              onClick={() => setFilter(status)}
            >
              {status === "ALL" ? "All" : status === "ACTIVE" ? "Active" : status === "COMPLETED" ? "Completed" : "Cancelled"}
              <span className={`ml-1.5 rounded-full px-1.5 py-0.5 text-xs ${isActive ? "bg-white/20" : "bg-muted"}`}>{count}</span>
            </Button>
          );
        })}
      </div>

      <div className="space-y-4">
        {filteredBookings.map((booking, i) => {
          const isExpanded = expandedId === booking.id;
          return (
            <div
              key={booking.id}
              className="glass-card overflow-hidden animate-fade-up"
              style={{ animationDelay: `${i * 100}ms` }}
            >
              {/* Clickable header row */}
              <button
                type="button"
                className="w-full p-6 text-left hover:bg-muted/30 transition-colors"
                onClick={() => setExpandedId(isExpanded ? null : booking.id)}
              >
                <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                  <div className="flex items-center gap-4">
                    <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-muted">
                      <Bike className="h-6 w-6 text-primary" />
                    </div>
                    <div>
                      <h3 className="font-display font-semibold text-foreground">{booking.bikeName}</h3>
                      <p className="text-sm text-muted-foreground">
                        {new Date(booking.createdAt).toLocaleDateString("en-US", {
                          year: "numeric", month: "short", day: "numeric", hour: "2-digit", minute: "2-digit",
                        })}
                      </p>
                    </div>
                  </div>

                  <div className="flex items-center gap-6">
                    <div className="flex items-center gap-1.5 text-sm text-muted-foreground">
                      <Clock className="h-4 w-4" />
                      <span>{booking.rentalDuration}h</span>
                    </div>
                    <div className="flex items-center gap-1.5 text-sm">
                      <DollarSign className="h-4 w-4 text-muted-foreground" />
                      <span className="font-display font-semibold text-foreground">₱{booking.totalCost.toFixed(2)}</span>
                    </div>
                    <span className={`rounded-full px-3 py-1 text-xs font-semibold ${statusStyles[booking.bookingStatus]}`}>
                      {booking.bookingStatus}
                    </span>
                    <ChevronDown className={`h-4 w-4 text-muted-foreground transition-transform ${isExpanded ? "rotate-180" : ""}`} />
                  </div>
                </div>
              </button>

              {/* Expanded details */}
              {isExpanded && (
                <div className="border-t border-border bg-muted/20 px-6 py-5 animate-fade-in">
                  <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                    <div className="flex items-start gap-3">
                      <CalendarDays className="mt-0.5 h-4 w-4 text-muted-foreground" />
                      <div>
                        <p className="text-xs font-semibold uppercase text-muted-foreground">Schedule</p>
                        {booking.startTime ? (
                          <>
                            <p className="text-sm font-medium text-foreground">
                              {new Date(booking.startTime).toLocaleDateString("en-US", { weekday: "short", year: "numeric", month: "short", day: "numeric" })}
                            </p>
                            <p className="text-sm text-muted-foreground">
                              {new Date(booking.startTime).toLocaleTimeString("en-US", { hour: "2-digit", minute: "2-digit" })}
                              {" — "}
                              {booking.endTime && new Date(booking.endTime).toLocaleTimeString("en-US", { hour: "2-digit", minute: "2-digit" })}
                            </p>
                          </>
                        ) : (
                          <p className="text-sm text-muted-foreground">Not set</p>
                        )}
                      </div>
                    </div>

                    <div className="flex items-start gap-3">
                      <Timer className="mt-0.5 h-4 w-4 text-muted-foreground" />
                      <div>
                        <p className="text-xs font-semibold uppercase text-muted-foreground">Duration</p>
                        <p className="text-sm font-medium text-foreground">{booking.rentalDuration} hour{booking.rentalDuration !== 1 ? "s" : ""}</p>
                      </div>
                    </div>

                    <div className="flex items-start gap-3">
                      <DollarSign className="mt-0.5 h-4 w-4 text-muted-foreground" />
                      <div>
                        <p className="text-xs font-semibold uppercase text-muted-foreground">Total Cost</p>
                        <p className="text-sm font-medium text-foreground">₱{booking.totalCost.toFixed(2)}</p>
                      </div>
                    </div>

                    <div className="flex items-start gap-3">
                      <Bike className="mt-0.5 h-4 w-4 text-muted-foreground" />
                      <div>
                        <p className="text-xs font-semibold uppercase text-muted-foreground">Bike</p>
                        <p className="text-sm font-medium text-foreground">{booking.bikeName}</p>
                      </div>
                    </div>

                    <div className="flex items-start gap-3">
                      <MapPin className="mt-0.5 h-4 w-4 text-muted-foreground" />
                      <div>
                        <p className="text-xs font-semibold uppercase text-muted-foreground">Status</p>
                        <span className={`rounded-full px-3 py-1 text-xs font-semibold ${statusStyles[booking.bookingStatus]}`}>
                          {booking.bookingStatus}
                        </span>
                      </div>
                    </div>

                    <div className="flex items-start gap-3">
                      <CalendarDays className="mt-0.5 h-4 w-4 text-muted-foreground" />
                      <div>
                        <p className="text-xs font-semibold uppercase text-muted-foreground">Booked On</p>
                        <p className="text-sm font-medium text-foreground">
                          {new Date(booking.createdAt).toLocaleDateString("en-US", { year: "numeric", month: "short", day: "numeric", hour: "2-digit", minute: "2-digit" })}
                        </p>
                      </div>
                    </div>
                  </div>

                  {booking.bookingStatus === "ACTIVE" && (
                    <div className="mt-5 flex justify-end border-t border-border pt-4">
                      <Button
                        size="sm"
                        variant="outline"
                        className="text-destructive border-destructive/30 hover:bg-destructive/10"
                        disabled={cancellingId === booking.id}
                        onClick={(e) => { e.stopPropagation(); handleCancel(booking.id); }}
                      >
                        {cancellingId === booking.id ? (
                          <Loader2 className="mr-1.5 h-3.5 w-3.5 animate-spin" />
                        ) : (
                          <XCircle className="mr-1.5 h-3.5 w-3.5" />
                        )}
                        Cancel Booking
                      </Button>
                    </div>
                  )}
                </div>
              )}
            </div>
          );
        })}
      </div>

      {filteredBookings.length === 0 && (
        <div className="py-20 text-center text-muted-foreground">
          {bookings.length === 0 ? "No rentals yet. Book your first ride!" : `No ${filter.toLowerCase()} bookings.`}
        </div>
      )}
    </div>
  );
};

export default RentalHistory;
