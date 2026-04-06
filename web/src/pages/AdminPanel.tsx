import { useState, useEffect, useCallback } from "react";
import {
  fetchBikes, fetchAllBookings, createBike, updateBike, deleteBike,
  updateBikeStatus, completeBooking, cancelBooking, confirmBooking,
  type CreateBikePayload, type BackendBike, mapBike,
} from "@/lib/api";
import { Button } from "@/components/ui/button";
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle, AlertDialogTrigger } from "@/components/ui/alert-dialog";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Bike, BikeStatus, Booking } from "@/types";
import { useToast } from "@/hooks/use-toast";
import { Pencil, Trash2, Plus, CheckCircle2, Bike as BikeIcon, ClipboardList, ImagePlus, Loader2, RefreshCw, XCircle } from "lucide-react";

const POLL_INTERVAL = 5000; // 5s auto-refresh

const AdminPanel = () => {
  const [bikes, setBikes] = useState<Bike[]>([]);
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);
  const [editingBike, setEditingBike] = useState<Bike | null>(null);
  const [showAddForm, setShowAddForm] = useState(false);
  const { toast } = useToast();

  const [newBike, setNewBike] = useState({ name: "", brand: "", model: "", batteryLevel: 100, pricePerHour: 5, description: "", image: "", color: "Black", type: "STANDARD", location: "Downtown Station" });
  const [bikeFilter, setBikeFilter] = useState<"ALL" | BikeStatus>("ALL");
  const [bookingFilter, setBookingFilter] = useState<"ALL" | "ACTIVE" | "COMPLETED" | "CANCELLED">("ALL");

  const filteredBikes = bikeFilter === "ALL" ? bikes : bikes.filter((b) => b.status === bikeFilter);

  // Sort bookings: ACTIVE first, then by newest date, COMPLETED & CANCELLED at the bottom
  const statusOrder: Record<string, number> = { ACTIVE: 0, COMPLETED: 1, CANCELLED: 2 };
  const sortedBookings = [...bookings].sort((a, b) => {
    const orderDiff = (statusOrder[a.bookingStatus] ?? 9) - (statusOrder[b.bookingStatus] ?? 9);
    if (orderDiff !== 0) return orderDiff;
    return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
  });
  const filteredBookings = bookingFilter === "ALL" ? sortedBookings : sortedBookings.filter((b) => b.bookingStatus === bookingFilter);

  // Load data from API
  const loadData = useCallback(async () => {
    try {
      const [b, bk] = await Promise.all([fetchBikes(), fetchAllBookings()]);
      setBikes(b);
      setBookings(bk);
    } catch (err) {
      console.error("Failed to load admin data:", err);
    } finally {
      setLoading(false);
    }
  }, []);

  // Initial load + polling for real-time updates
  useEffect(() => {
    loadData();
    const interval = setInterval(loadData, POLL_INTERVAL);
    return () => clearInterval(interval);
  }, [loadData]);

  const handleImageUpload = (e: React.ChangeEvent<HTMLInputElement>, target: "new" | "edit") => {
    const file = e.target.files?.[0];
    if (!file) return;
    // Convert to base64 so image persists to the database
    const reader = new FileReader();
    reader.onloadend = () => {
      const base64 = reader.result as string;
      if (target === "new") setNewBike({ ...newBike, image: base64 });
      else if (editingBike) setEditingBike({ ...editingBike, image: base64 });
    };
    reader.readAsDataURL(file);
  };

  const handleAddBike = async () => {
    if (!newBike.name && !newBike.model) return;
    const brand = newBike.brand || newBike.name.split(" ")[0] || "Generic";
    const model = newBike.model || newBike.name || "Bike";
    const code = `BIKE${Date.now().toString().slice(-6)}`;
    const payload: CreateBikePayload = {
      bikeCode: code, model, brand, color: newBike.color,
      year: new Date().getFullYear(), type: newBike.type,
      pricePerHour: newBike.pricePerHour, pricePerDay: newBike.pricePerHour * 6,
      status: "AVAILABLE", description: newBike.description,
      imageUrl: newBike.image || undefined, condition: "EXCELLENT",
      batteryLevel: newBike.batteryLevel, location: newBike.location,
    };
    try {
      await createBike(payload);
      toast({ title: "Bike added", description: `${brand} ${model} has been added to the fleet.` });
      setNewBike({ name: "", brand: "", model: "", batteryLevel: 100, pricePerHour: 5, description: "", image: "", color: "Black", type: "STANDARD", location: "Downtown Station" });
      setShowAddForm(false);
      loadData();
    } catch (err) {
      toast({ title: "Failed to add bike", variant: "destructive" });
    }
  };

  const handleDeleteBike = async (id: string) => {
    try {
      await deleteBike(id);
      toast({ title: "Bike removed", description: "Bike has been removed from the fleet." });
      loadData();
    } catch (err) {
      toast({ title: "Failed to delete bike", variant: "destructive" });
    }
  };

  const handleStatusChange = async (id: string, status: BikeStatus) => {
    try {
      await updateBikeStatus(id, status);
      toast({ title: "Status updated" });
      loadData();
    } catch (err) {
      toast({ title: "Failed to update status", variant: "destructive" });
    }
  };

  const handleCompleteBooking = async (id: string) => {
    try {
      await completeBooking(id);
      toast({ title: "Booking completed" });
      loadData();
    } catch (err) {
      toast({ title: "Failed to complete booking", variant: "destructive" });
    }
  };

  const handleCancelBooking = async (id: string) => {
    try {
      await cancelBooking(id);
      toast({ title: "Booking cancelled" });
      loadData();
    } catch (err) {
      toast({ title: "Failed to cancel booking", variant: "destructive" });
    }
  };

  const handleUpdateBike = async () => {
    if (!editingBike) return;
    // Parse brand/model from display name
    const parts = editingBike.name.split(" ");
    const brand = parts[0] || "Generic";
    const model = parts.slice(1).join(" ") || editingBike.name;
    try {
      await updateBike(editingBike.id, {
        model, brand,
        pricePerHour: editingBike.pricePerHour,
        batteryLevel: editingBike.batteryLevel,
        description: editingBike.description || "",
        imageUrl: editingBike.image,
      });
      setEditingBike(null);
      toast({ title: "Bike updated" });
      loadData();
    } catch (err) {
      toast({ title: "Failed to update bike", variant: "destructive" });
    }
  };

  // Inline image upload for bike table row
  const handleInlineImageUpload = async (e: React.ChangeEvent<HTMLInputElement>, bikeId: string) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onloadend = async () => {
      const base64 = reader.result as string;
      try {
        await updateBike(bikeId, { imageUrl: base64 });
        toast({ title: "Image uploaded", description: "Bike image has been updated." });
        loadData();
      } catch (err) {
        toast({ title: "Failed to upload image", variant: "destructive" });
      }
    };
    reader.readAsDataURL(file);
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
        <h1 className="font-display text-3xl font-bold text-foreground">Admin Panel</h1>
        <p className="mt-2 text-muted-foreground">Manage bikes and bookings</p>
      </div>

      <Tabs defaultValue="bikes" className="animate-fade-up">
        <TabsList className="mb-6">
          <TabsTrigger value="bikes" className="flex items-center gap-2">
            <BikeIcon className="h-4 w-4" /> Bikes ({bikes.length})
          </TabsTrigger>
          <TabsTrigger value="bookings" className="flex items-center gap-2">
            <ClipboardList className="h-4 w-4" /> Bookings ({bookings.length})
          </TabsTrigger>
        </TabsList>

        <TabsContent value="bikes">
          <div className="mb-4 flex justify-between">
            <Button variant="outline" size="sm" onClick={loadData}>
              <RefreshCw className="mr-2 h-4 w-4" /> Refresh
            </Button>
            <Button className="gradient-primary text-primary-foreground" onClick={() => setShowAddForm(!showAddForm)}>
              <Plus className="mr-2 h-4 w-4" /> Add Bike
            </Button>
          </div>

          {/* Status Filters */}
          <div className="mb-6 flex flex-wrap gap-2">
            {(["ALL", "AVAILABLE", "RENTED", "MAINTENANCE"] as const).map((status) => {
              const count = status === "ALL" ? bikes.length : bikes.filter((b) => b.status === status).length;
              const isActive = bikeFilter === status;
              return (
                <Button
                  key={status}
                  size="sm"
                  variant={isActive ? "default" : "outline"}
                  className={isActive ? "gradient-primary text-primary-foreground" : ""}
                  onClick={() => setBikeFilter(status)}
                >
                  {status === "ALL" ? "All" : status === "AVAILABLE" ? "Available" : status === "RENTED" ? "Rented" : "Maintenance"}
                  <span className={`ml-1.5 rounded-full px-1.5 py-0.5 text-xs ${isActive ? "bg-white/20" : "bg-muted"}`}>{count}</span>
                </Button>
              );
            })}
          </div>

          {/* Add Form */}
          {showAddForm && (
            <div className="glass-card mb-6 p-6 animate-fade-in">
              <h3 className="font-display text-lg font-semibold mb-4">Add New Bike</h3>
              <div className="grid gap-4 sm:grid-cols-2">
                <div className="space-y-2">
                  <Label>Brand</Label>
                  <Input value={newBike.brand} onChange={(e) => setNewBike({ ...newBike, brand: e.target.value })} placeholder="e.g. Trek" />
                </div>
                <div className="space-y-2">
                  <Label>Model</Label>
                  <Input value={newBike.model} onChange={(e) => setNewBike({ ...newBike, model: e.target.value })} placeholder="e.g. Mountain Pro X" />
                </div>
                <div className="space-y-2">
                  <Label>Price/Hour (₱)</Label>
                  <Input type="number" value={newBike.pricePerHour === 0 ? "" : newBike.pricePerHour} onChange={(e) => setNewBike({ ...newBike, pricePerHour: e.target.value === "" ? 0 : parseFloat(e.target.value) })} />
                </div>
                <div className="space-y-2">
                  <Label>Battery Level (%)</Label>
                  <Input type="number" value={newBike.batteryLevel === 0 ? "" : newBike.batteryLevel} onChange={(e) => setNewBike({ ...newBike, batteryLevel: e.target.value === "" ? 0 : parseInt(e.target.value) })} />
                </div>
                <div className="space-y-2 sm:col-span-2">
                  <Label>Description</Label>
                  <Textarea value={newBike.description} onChange={(e) => setNewBike({ ...newBike, description: e.target.value })} placeholder="Bike description" rows={3} />
                </div>
                <div className="space-y-2 sm:col-span-2">
                  <Label>Bike Image</Label>
                  <div className="flex items-center gap-4">
                    <label className="flex cursor-pointer items-center gap-2 rounded-lg border border-dashed border-border px-4 py-3 text-sm text-muted-foreground hover:bg-muted/50 transition-colors">
                      <ImagePlus className="h-4 w-4" />
                      {newBike.image ? "Change image" : "Upload image"}
                      <input type="file" accept="image/*" className="hidden" onChange={(e) => handleImageUpload(e, "new")} />
                    </label>
                    {newBike.image && <img src={newBike.image} alt="Preview" className="h-16 w-16 rounded-lg object-cover" />}
                  </div>
                </div>
              </div>
              <div className="mt-4 flex gap-2">
                <Button className="gradient-primary text-primary-foreground" onClick={handleAddBike}>Add Bike</Button>
                <Button variant="outline" onClick={() => setShowAddForm(false)}>Cancel</Button>
              </div>
            </div>
          )}

          {/* Edit Dialog */}
          <Dialog open={!!editingBike} onOpenChange={(open) => { if (!open) setEditingBike(null); }}>
            <DialogContent className="sm:max-w-lg">
              <DialogHeader>
                <DialogTitle className="font-display text-lg">Edit: {editingBike?.name}</DialogTitle>
              </DialogHeader>
              {editingBike && (
                <div className="grid gap-4 sm:grid-cols-2 pt-2">
                  <div className="space-y-2">
                    <Label>Name</Label>
                    <Input value={editingBike.name} onChange={(e) => setEditingBike({ ...editingBike, name: e.target.value })} />
                  </div>
                  <div className="space-y-2">
                    <Label>Price/Hour (₱)</Label>
                    <Input type="number" value={editingBike.pricePerHour === 0 ? "" : editingBike.pricePerHour} onChange={(e) => setEditingBike({ ...editingBike, pricePerHour: e.target.value === "" ? 0 : parseFloat(e.target.value) })} />
                  </div>
                  <div className="space-y-2">
                    <Label>Battery Level (%)</Label>
                    <Input type="number" value={editingBike.batteryLevel === 0 ? "" : editingBike.batteryLevel} onChange={(e) => setEditingBike({ ...editingBike, batteryLevel: e.target.value === "" ? 0 : parseInt(e.target.value) })} />
                  </div>
                  <div className="space-y-2 sm:col-span-2">
                    <Label>Description</Label>
                    <Textarea value={editingBike.description || ""} onChange={(e) => setEditingBike({ ...editingBike, description: e.target.value })} placeholder="Bike description" rows={3} />
                  </div>
                  <div className="space-y-2 sm:col-span-2">
                    <Label>Bike Image</Label>
                    <div className="flex items-center gap-4">
                      <label className="flex cursor-pointer items-center gap-2 rounded-lg border border-dashed border-border px-4 py-3 text-sm text-muted-foreground hover:bg-muted/50 transition-colors">
                        <ImagePlus className="h-4 w-4" />
                        {editingBike.image ? "Change image" : "Upload image"}
                        <input type="file" accept="image/*" className="hidden" onChange={(e) => handleImageUpload(e, "edit")} />
                      </label>
                      {editingBike.image && <img src={editingBike.image} alt="Preview" className="h-16 w-16 rounded-lg object-cover" />}
                    </div>
                  </div>
                  <div className="sm:col-span-2 flex gap-2 justify-end pt-2">
                    <Button variant="outline" onClick={() => setEditingBike(null)}>Cancel</Button>
                    <Button className="gradient-primary text-primary-foreground" onClick={handleUpdateBike}>Save Changes</Button>
                  </div>
                </div>
              )}
            </DialogContent>
          </Dialog>

          {/* Bike Table */}
          <div className="glass-card overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-border bg-muted/50">
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase text-muted-foreground">Image</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase text-muted-foreground">Name</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase text-muted-foreground">Battery</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase text-muted-foreground">Price/Hr</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase text-muted-foreground">Status</th>
                    <th className="px-4 py-3 text-right text-xs font-semibold uppercase text-muted-foreground">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredBikes.map((bike) => (
                    <tr key={bike.id} className="border-b border-border last:border-0 hover:bg-muted/30 transition-colors">
                      <td className="px-4 py-3">
                        <div className="relative group/img">
                          {bike.image ? (
                            <img src={bike.image} alt={bike.name} className="h-12 w-12 rounded-lg object-cover" />
                          ) : (
                            <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-muted">
                              <BikeIcon className="h-5 w-5 text-muted-foreground" />
                            </div>
                          )}
                          <label className="absolute inset-0 flex cursor-pointer items-center justify-center rounded-lg bg-black/50 opacity-0 transition-opacity group-hover/img:opacity-100">
                            <ImagePlus className="h-4 w-4 text-white" />
                            <input type="file" accept="image/*" className="hidden" onChange={(e) => handleInlineImageUpload(e, bike.id)} />
                          </label>
                        </div>
                      </td>
                      <td className="px-4 py-4 font-medium text-foreground">{bike.name}</td>
                      <td className="px-4 py-4 text-muted-foreground">{bike.batteryLevel}%</td>
                      <td className="px-4 py-4 text-muted-foreground">₱{bike.pricePerHour}</td>
                      <td className="px-4 py-4">
                        <select
                          value={bike.status}
                          onChange={(e) => handleStatusChange(bike.id, e.target.value as BikeStatus)}
                          className="rounded-lg border border-border bg-background px-3 py-1.5 text-sm"
                        >
                          <option value="AVAILABLE">Available</option>
                          <option value="RENTED">Rented</option>
                          <option value="MAINTENANCE">Maintenance</option>
                        </select>
                      </td>
                      <td className="px-4 py-4 text-right">
                        <div className="flex justify-end gap-2">
                          <Button variant="ghost" size="sm" onClick={() => setEditingBike(bike)}>
                            <Pencil className="h-4 w-4" />
                          </Button>
                          <AlertDialog>
                            <AlertDialogTrigger asChild>
                              <Button variant="ghost" size="sm" className="text-destructive">
                                <Trash2 className="h-4 w-4" />
                              </Button>
                            </AlertDialogTrigger>
                            <AlertDialogContent>
                              <AlertDialogHeader>
                                <AlertDialogTitle>Delete {bike.name}?</AlertDialogTitle>
                                <AlertDialogDescription>This action cannot be undone. This bike will be permanently removed from the fleet.</AlertDialogDescription>
                              </AlertDialogHeader>
                              <AlertDialogFooter>
                                <AlertDialogCancel>No, cancel</AlertDialogCancel>
                                <AlertDialogAction onClick={() => handleDeleteBike(bike.id)} className="bg-destructive text-destructive-foreground hover:bg-destructive/90">Yes, delete</AlertDialogAction>
                              </AlertDialogFooter>
                            </AlertDialogContent>
                          </AlertDialog>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </TabsContent>

        <TabsContent value="bookings">
          <div className="mb-4 flex justify-start">
            <Button variant="outline" size="sm" onClick={loadData}>
              <RefreshCw className="mr-2 h-4 w-4" /> Refresh
            </Button>
            <span className="ml-3 self-center text-xs text-muted-foreground">Auto-refreshes every 5s</span>
          </div>

          {/* Booking Status Filters */}
          <div className="mb-6 flex flex-wrap gap-2">
            {(["ALL", "ACTIVE", "COMPLETED", "CANCELLED"] as const).map((status) => {
              const count = status === "ALL" ? bookings.length : bookings.filter((b) => b.bookingStatus === status).length;
              const isActive = bookingFilter === status;
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
                  onClick={() => setBookingFilter(status)}
                >
                  {status === "ALL" ? "All" : status === "ACTIVE" ? "Active" : status === "COMPLETED" ? "Completed" : "Cancelled"}
                  <span className={`ml-1.5 rounded-full px-1.5 py-0.5 text-xs ${isActive ? "bg-white/20" : "bg-muted"}`}>{count}</span>
                </Button>
              );
            })}
          </div>
          <div className="glass-card overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-border bg-muted/50">
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase text-muted-foreground">ID</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase text-muted-foreground">User</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase text-muted-foreground">Bike</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase text-muted-foreground">Schedule</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase text-muted-foreground">Duration</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase text-muted-foreground">Total</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase text-muted-foreground">Status</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold uppercase text-muted-foreground">Booked On</th>
                    {(bookingFilter === "ALL" || bookingFilter === "ACTIVE") && (
                      <th className="px-4 py-3 text-right text-xs font-semibold uppercase text-muted-foreground">Actions</th>
                    )}
                  </tr>
                </thead>
                <tbody>
                  {filteredBookings.map((b) => (
                    <tr key={b.id} className="border-b border-border last:border-0 hover:bg-muted/30 transition-colors">
                      <td className="px-4 py-4 font-mono text-sm text-muted-foreground">{b.id}</td>
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
                          b.bookingStatus === "ACTIVE" ? "bg-success/15 text-success"
                          : b.bookingStatus === "COMPLETED" ? "bg-muted text-muted-foreground"
                          : "bg-destructive/15 text-destructive"
                        }`}>
                          {b.bookingStatus}
                        </span>
                      </td>
                      <td className="px-4 py-4 text-sm text-muted-foreground">
                        {new Date(b.createdAt).toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" })}
                      </td>
                      {(bookingFilter === "ALL" || bookingFilter === "ACTIVE") && (
                        <td className="px-4 py-4 text-right">
                          {b.bookingStatus === "ACTIVE" && (
                            <div className="flex justify-end gap-2">
                              <Button size="sm" variant="outline" onClick={() => handleCompleteBooking(b.id)}>
                                <CheckCircle2 className="mr-1.5 h-3.5 w-3.5" /> Complete
                              </Button>
                              <Button size="sm" variant="outline" className="text-destructive" onClick={() => handleCancelBooking(b.id)}>
                                <XCircle className="mr-1.5 h-3.5 w-3.5" /> Cancel
                              </Button>
                            </div>
                          )}
                        </td>
                      )}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </TabsContent>
      </Tabs>
    </div>
  );
};

export default AdminPanel;
