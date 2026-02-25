import { useParams, useNavigate } from "react-router-dom";
import { mockBikes } from "@/data/mockData";
import { Button } from "@/components/ui/button";
import { Battery, Clock, Zap, ArrowLeft, AlertTriangle } from "lucide-react";

const statusColors: Record<string, string> = {
  AVAILABLE: "bg-success/15 text-success",
  RENTED: "bg-warning/15 text-warning",
  MAINTENANCE: "bg-destructive/15 text-destructive",
};

const BikeDetails = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const bike = mockBikes.find((b) => b.id === id);

  if (!bike) {
    return (
      <div className="container py-20 text-center">
        <h1 className="font-display text-2xl font-bold text-foreground">Bike not found</h1>
        <Button variant="outline" className="mt-4" onClick={() => navigate("/bikes")}>
          Back to Fleet
        </Button>
      </div>
    );
  }

  return (
    <div className="container max-w-4xl py-8">
      <button onClick={() => navigate("/bikes")} className="mb-6 flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground transition-colors">
        <ArrowLeft className="h-4 w-4" /> Back to Fleet
      </button>

      <div className="glass-card overflow-hidden animate-fade-up">
        <div className="h-64 gradient-hero flex items-center justify-center">
          <Zap className="h-24 w-24 text-primary/30" />
        </div>

        <div className="p-8">
          <div className="flex flex-wrap items-start justify-between gap-4">
            <div>
              <h1 className="font-display text-3xl font-bold text-foreground">{bike.name}</h1>
              <span className={`mt-2 inline-block rounded-full px-3 py-1 text-sm font-semibold ${statusColors[bike.status]}`}>
                {bike.status}
              </span>
            </div>
            <div className="text-right">
              <p className="font-display text-4xl font-bold text-primary">₱{bike.pricePerHour}</p>
              <p className="text-sm text-muted-foreground">per hour</p>
            </div>
          </div>

          {bike.description && (
            <p className="mt-6 text-lg text-muted-foreground">{bike.description}</p>
          )}

          <div className="mt-8 grid grid-cols-2 gap-4">
            <div className="rounded-xl bg-muted p-4">
              <div className="flex items-center gap-2 text-muted-foreground">
                <Battery className="h-5 w-5" />
                <span className="text-sm">Battery Level</span>
              </div>
              <p className="mt-1 font-display text-2xl font-bold text-foreground">{bike.batteryLevel}%</p>
              <div className="mt-2 h-2 rounded-full bg-border">
                <div
                  className="h-full rounded-full gradient-primary transition-all"
                  style={{ width: `${bike.batteryLevel}%` }}
                />
              </div>
            </div>
            <div className="rounded-xl bg-muted p-4">
              <div className="flex items-center gap-2 text-muted-foreground">
                <Clock className="h-5 w-5" />
                <span className="text-sm">Rate</span>
              </div>
              <p className="mt-1 font-display text-2xl font-bold text-foreground">₱{bike.pricePerHour}/hr</p>
            </div>
          </div>

          <div className="mt-8">
            {bike.status === "AVAILABLE" ? (
              <Button
                className="w-full gradient-primary text-primary-foreground py-6 text-lg"
                onClick={() => navigate(`/booking/${bike.id}`)}
              >
                <Zap className="mr-2 h-5 w-5" />
                Book This Bike
              </Button>
            ) : (
              <div className="flex items-center justify-center gap-2 rounded-xl bg-muted p-4 text-muted-foreground">
                <AlertTriangle className="h-5 w-5" />
                <span>This bike is currently {bike.status.toLowerCase()}</span>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default BikeDetails;
