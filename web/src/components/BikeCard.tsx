import { Bike } from "@/types";
import { Link } from "react-router-dom";
import { Battery, Clock, Zap } from "lucide-react";

interface BikeCardProps {
  bike: Bike;
}

const statusColors: Record<string, string> = {
  AVAILABLE: "bg-success/15 text-success",
  RENTED: "bg-warning/15 text-warning",
  MAINTENANCE: "bg-destructive/15 text-destructive",
};

const BikeCard = ({ bike }: BikeCardProps) => {
  return (
    <Link
      to={`/bikes/${bike.id}`}
      className="group glass-card overflow-hidden transition-all duration-300 hover:-translate-y-1 hover:shadow-lg"
    >
      {/* Image placeholder */}
      <div className="relative h-48 gradient-hero flex items-center justify-center overflow-hidden">
        <Zap className="h-16 w-16 text-primary/40 transition-transform duration-500 group-hover:scale-110" />
        <div className="absolute inset-0 bg-gradient-to-t from-foreground/20 to-transparent" />
        <span className={`absolute right-3 top-3 rounded-full px-3 py-1 text-xs font-semibold ${statusColors[bike.status]}`}>
          {bike.status}
        </span>
      </div>

      <div className="p-5">
        <h3 className="font-display text-lg font-semibold text-foreground group-hover:text-primary transition-colors">
          {bike.name}
        </h3>
        {bike.description && (
          <p className="mt-1 text-sm text-muted-foreground line-clamp-2">{bike.description}</p>
        )}
        <div className="mt-4 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="flex items-center gap-1 text-sm text-muted-foreground">
              <Battery className="h-4 w-4" />
              <span>{bike.batteryLevel}%</span>
            </div>
            <div className="flex items-center gap-1 text-sm text-muted-foreground">
              <Clock className="h-4 w-4" />
              <span>₱{bike.pricePerHour}/hr</span>
            </div>
          </div>
        </div>
        <div className="mt-4 flex items-center justify-between border-t border-border pt-3">
          <span className="font-display text-xl font-bold text-primary">₱{bike.pricePerHour}</span>
          <span className="text-sm text-muted-foreground">per hour</span>
        </div>
      </div>
    </Link>
  );
};

export default BikeCard;
