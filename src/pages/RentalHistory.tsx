import { mockBookings } from "@/data/mockData";
import { Clock, DollarSign, Bike } from "lucide-react";

const statusStyles: Record<string, string> = {
  ACTIVE: "bg-success/15 text-success",
  COMPLETED: "bg-muted text-muted-foreground",
  CANCELLED: "bg-destructive/15 text-destructive",
};

const RentalHistory = () => {
  return (
    <div className="container py-8">
      <div className="mb-8 animate-fade-up">
        <h1 className="font-display text-3xl font-bold text-foreground">Rental History</h1>
        <p className="mt-2 text-muted-foreground">Your past and current rentals</p>
      </div>

      <div className="space-y-4">
        {mockBookings.map((booking, i) => (
          <div
            key={booking.id}
            className="glass-card p-6 animate-fade-up"
            style={{ animationDelay: `${i * 100}ms` }}
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
              </div>
            </div>
          </div>
        ))}
      </div>

      {mockBookings.length === 0 && (
        <div className="py-20 text-center text-muted-foreground">No rentals yet. Book your first ride!</div>
      )}
    </div>
  );
};

export default RentalHistory;
