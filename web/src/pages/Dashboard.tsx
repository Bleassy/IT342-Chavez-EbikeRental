import { useAuth } from "@/contexts/AuthContext";
import { mockBikes, mockBookings } from "@/data/mockData";
import { Link } from "react-router-dom";
import { Bike, Zap, Clock, Activity, ArrowRight } from "lucide-react";
import { Button } from "@/components/ui/button";

const Dashboard = () => {
  const { user } = useAuth();
  const availableBikes = mockBikes.filter((b) => b.status === "AVAILABLE").length;
  const activeBookings = mockBookings.filter((b) => b.bookingStatus === "ACTIVE").length;
  const totalSpent = mockBookings
    .filter((b) => b.bookingStatus === "COMPLETED")
    .reduce((sum, b) => sum + b.totalCost, 0);

  const stats = [
    { label: "Available Bikes", value: availableBikes, icon: Bike, color: "text-primary", link: "/bikes" },
    { label: "Active Rentals", value: activeBookings, icon: Activity, color: "text-accent", link: "/bikes" },
    { label: "Total Spent", value: `₱${totalSpent.toFixed(0)}`, icon: Zap, color: "text-warning", link: null },
    { label: "Total Rides", value: mockBookings.length, icon: Clock, color: "text-primary", link: "/history" },
  ];

  return (
    <div className="container py-8">
      {/* Hero */}
      <div className="mb-10 animate-fade-up">
        <h1 className="font-display text-4xl font-bold text-foreground">
          Hey, {user?.firstName} 👋
        </h1>
        <p className="mt-2 text-lg text-muted-foreground">
          Ready for your next ride? Browse available e-bikes and hit the road.
        </p>
      </div>

      {/* Stats */}
      <div className="mb-10 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {stats.map((stat, i) => {
          const content = (
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">{stat.label}</p>
                <p className="mt-1 font-display text-3xl font-bold text-foreground">{stat.value}</p>
              </div>
              <div className={`rounded-xl bg-muted p-3 ${stat.color}`}>
                <stat.icon className="h-6 w-6" />
              </div>
            </div>
          );
          return stat.link ? (
            <Link
              key={stat.label}
              to={stat.link}
              className="glass-card p-6 animate-fade-up hover:-translate-y-1 transition-transform cursor-pointer"
              style={{ animationDelay: `${i * 100}ms` }}
            >
              {content}
            </Link>
          ) : (
            <div
              key={stat.label}
              className="glass-card p-6 animate-fade-up"
              style={{ animationDelay: `${i * 100}ms` }}
            >
              {content}
            </div>
          );
        })}
      </div>

      {/* Quick Actions */}
      <div className="grid gap-6 md:grid-cols-2">
        <div className="glass-card p-8 animate-fade-up" style={{ animationDelay: "400ms" }}>
          <h2 className="font-display text-xl font-semibold text-foreground">Browse E-Bikes</h2>
          <p className="mt-2 text-muted-foreground">
            Find the perfect e-bike for your next adventure from our fleet.
          </p>
          <Link to="/bikes">
            <Button className="mt-6 gradient-primary text-primary-foreground">
              View Bikes <ArrowRight className="ml-2 h-4 w-4" />
            </Button>
          </Link>
        </div>

        <div className="glass-card p-8 animate-fade-up" style={{ animationDelay: "500ms" }}>
          <h2 className="font-display text-xl font-semibold text-foreground">Rental History</h2>
          <p className="mt-2 text-muted-foreground">
            View your past and current rentals, costs, and details.
          </p>
          <Link to="/history">
            <Button variant="outline" className="mt-6">
              View History <ArrowRight className="ml-2 h-4 w-4" />
            </Button>
          </Link>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
