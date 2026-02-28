import { useState } from "react";
import { mockBikes } from "@/data/mockData";
import BikeCard from "@/components/BikeCard";
import { Input } from "@/components/ui/input";
import { Search } from "lucide-react";
import { BikeStatus } from "@/types";

const BikeList = () => {
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState<BikeStatus | "ALL">("ALL");

  const filtered = mockBikes.filter((b) => {
    const matchSearch = b.name.toLowerCase().includes(search.toLowerCase());
    const matchStatus = statusFilter === "ALL" || b.status === statusFilter;
    return matchSearch && matchStatus;
  });

  const filters: Array<{ label: string; value: BikeStatus | "ALL" }> = [
    { label: "All", value: "ALL" },
    { label: "Available", value: "AVAILABLE" },
    { label: "Rented", value: "RENTED" },
    { label: "Maintenance", value: "MAINTENANCE" },
  ];

  return (
    <div className="container py-8">
      <div className="mb-8 animate-fade-up">
        <h1 className="font-display text-3xl font-bold text-foreground">E-Bike Fleet</h1>
        <p className="mt-2 text-muted-foreground">Choose your ride from our available fleet</p>
      </div>

      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between animate-fade-up" style={{ animationDelay: "100ms" }}>
        <div className="relative max-w-sm flex-1">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            placeholder="Search bikes..."
            className="pl-10"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        <div className="flex gap-2">
          {filters.map((f) => (
            <button
              key={f.value}
              onClick={() => setStatusFilter(f.value)}
              className={`rounded-lg px-4 py-2 text-sm font-medium transition-colors ${
                statusFilter === f.value
                  ? "gradient-primary text-primary-foreground"
                  : "bg-muted text-muted-foreground hover:text-foreground"
              }`}
            >
              {f.label}
            </button>
          ))}
        </div>
      </div>

      <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
        {filtered.map((bike, i) => (
          <div key={bike.id} className="animate-fade-up" style={{ animationDelay: `${(i + 2) * 100}ms` }}>
            <BikeCard bike={bike} />
          </div>
        ))}
      </div>

      {filtered.length === 0 && (
        <div className="py-20 text-center text-muted-foreground">
          No bikes found matching your criteria.
        </div>
      )}
    </div>
  );
};

export default BikeList;
