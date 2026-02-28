import { useState } from "react";
import { mockBikes, mockBookings } from "@/data/mockData";
import { Button } from "@/components/ui/button";
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle, AlertDialogTrigger } from "@/components/ui/alert-dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Bike, BikeStatus } from "@/types";
import { useToast } from "@/hooks/use-toast";
import { Pencil, Trash2, Plus, CheckCircle2, Bike as BikeIcon, ClipboardList, ImagePlus } from "lucide-react";

const AdminPanel = () => {
  const [bikes, setBikes] = useState<Bike[]>([...mockBikes]);
  const [bookings, setBookings] = useState([...mockBookings]);
  const [editingBike, setEditingBike] = useState<Bike | null>(null);
  const [showAddForm, setShowAddForm] = useState(false);
  const { toast } = useToast();

  const [newBike, setNewBike] = useState({ name: "", batteryLevel: 100, pricePerHour: 5, description: "", image: "" });

  const handleImageUpload = (e: React.ChangeEvent<HTMLInputElement>, target: "new" | "edit") => {
    const file = e.target.files?.[0];
    if (!file) return;
    const url = URL.createObjectURL(file);
    if (target === "new") setNewBike({ ...newBike, image: url });
    else if (editingBike) setEditingBike({ ...editingBike, image: url });
  };

  const handleAddBike = () => {
    if (!newBike.name) return;
    const bike: Bike = {
      id: `bike-${Date.now()}`,
      name: newBike.name,
      batteryLevel: newBike.batteryLevel,
      pricePerHour: newBike.pricePerHour,
      status: "AVAILABLE",
      description: newBike.description,
      image: newBike.image || undefined,
    };
    setBikes([...bikes, bike]);
    setNewBike({ name: "", batteryLevel: 100, pricePerHour: 5, description: "", image: "" });
    setShowAddForm(false);
    toast({ title: "Bike added", description: `${bike.name} has been added to the fleet.` });
  };

  const handleDeleteBike = (id: string) => {
    setBikes(bikes.filter((b) => b.id !== id));
    toast({ title: "Bike removed", description: "Bike has been removed from the fleet." });
  };

  const handleStatusChange = (id: string, status: BikeStatus) => {
    setBikes(bikes.map((b) => (b.id === id ? { ...b, status } : b)));
    toast({ title: "Status updated" });
  };

  const handleCompleteBooking = (id: string) => {
    setBookings(bookings.map((b) => (b.id === id ? { ...b, bookingStatus: "COMPLETED" as const } : b)));
    toast({ title: "Booking completed" });
  };

  const handleUpdateBike = () => {
    if (!editingBike) return;
    setBikes(bikes.map((b) => (b.id === editingBike.id ? editingBike : b)));
    setEditingBike(null);
    toast({ title: "Bike updated" });
  };

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
          <div className="mb-6 flex justify-end">
            <Button className="gradient-primary text-primary-foreground" onClick={() => setShowAddForm(!showAddForm)}>
              <Plus className="mr-2 h-4 w-4" /> Add Bike
            </Button>
          </div>

          {/* Add Form */}
          {showAddForm && (
            <div className="glass-card mb-6 p-6 animate-fade-in">
              <h3 className="font-display text-lg font-semibold mb-4">Add New Bike</h3>
              <div className="grid gap-4 sm:grid-cols-2">
                <div className="space-y-2">
                  <Label>Name</Label>
                  <Input value={newBike.name} onChange={(e) => setNewBike({ ...newBike, name: e.target.value })} placeholder="Bike name" />
                </div>
                <div className="space-y-2">
                  <Label>Price/Hour (₱)</Label>
                  <Input type="number" value={newBike.pricePerHour} onChange={(e) => setNewBike({ ...newBike, pricePerHour: parseFloat(e.target.value) || 0 })} />
                </div>
                <div className="space-y-2">
                  <Label>Battery Level (%)</Label>
                  <Input type="number" value={newBike.batteryLevel} onChange={(e) => setNewBike({ ...newBike, batteryLevel: parseInt(e.target.value) || 0 })} />
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

          {/* Edit Form */}
          {editingBike && (
            <div className="glass-card mb-6 p-6 animate-fade-in">
              <h3 className="font-display text-lg font-semibold mb-4">Edit: {editingBike.name}</h3>
              <div className="grid gap-4 sm:grid-cols-2">
                <div className="space-y-2">
                  <Label>Name</Label>
                  <Input value={editingBike.name} onChange={(e) => setEditingBike({ ...editingBike, name: e.target.value })} />
                </div>
                <div className="space-y-2">
                  <Label>Price/Hour (₱)</Label>
                  <Input type="number" value={editingBike.pricePerHour} onChange={(e) => setEditingBike({ ...editingBike, pricePerHour: parseFloat(e.target.value) || 0 })} />
                </div>
                <div className="space-y-2">
                  <Label>Battery Level (%)</Label>
                  <Input type="number" value={editingBike.batteryLevel} onChange={(e) => setEditingBike({ ...editingBike, batteryLevel: parseInt(e.target.value) || 0 })} />
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
              </div>
              <div className="mt-4 flex gap-2">
                <Button className="gradient-primary text-primary-foreground" onClick={handleUpdateBike}>Save</Button>
                <Button variant="outline" onClick={() => setEditingBike(null)}>Cancel</Button>
              </div>
            </div>
          )}

          {/* Bike Table */}
          <div className="glass-card overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-border bg-muted/50">
                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase text-muted-foreground">Name</th>
                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase text-muted-foreground">Battery</th>
                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase text-muted-foreground">Price/Hr</th>
                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase text-muted-foreground">Status</th>
                    <th className="px-6 py-3 text-right text-xs font-semibold uppercase text-muted-foreground">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {bikes.map((bike) => (
                    <tr key={bike.id} className="border-b border-border last:border-0 hover:bg-muted/30 transition-colors">
                      <td className="px-6 py-4 font-medium text-foreground">{bike.name}</td>
                      <td className="px-6 py-4 text-muted-foreground">{bike.batteryLevel}%</td>
                      <td className="px-6 py-4 text-muted-foreground">₱{bike.pricePerHour}</td>
                      <td className="px-6 py-4">
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
                      <td className="px-6 py-4 text-right">
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
          <div className="glass-card overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-border bg-muted/50">
                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase text-muted-foreground">ID</th>
                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase text-muted-foreground">Bike</th>
                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase text-muted-foreground">Duration</th>
                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase text-muted-foreground">Total</th>
                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase text-muted-foreground">Status</th>
                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase text-muted-foreground">Date</th>
                    <th className="px-6 py-3 text-right text-xs font-semibold uppercase text-muted-foreground">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {bookings.map((b) => (
                    <tr key={b.id} className="border-b border-border last:border-0 hover:bg-muted/30 transition-colors">
                      <td className="px-6 py-4 font-mono text-sm text-muted-foreground">{b.id}</td>
                      <td className="px-6 py-4 font-medium text-foreground">{b.bikeName}</td>
                      <td className="px-6 py-4 text-muted-foreground">{b.rentalDuration}h</td>
                      <td className="px-6 py-4 font-medium text-foreground">₱{b.totalCost.toFixed(2)}</td>
                      <td className="px-6 py-4">
                        <span className={`rounded-full px-3 py-1 text-xs font-semibold ${
                          b.bookingStatus === "ACTIVE" ? "bg-success/15 text-success"
                          : b.bookingStatus === "COMPLETED" ? "bg-muted text-muted-foreground"
                          : "bg-destructive/15 text-destructive"
                        }`}>
                          {b.bookingStatus}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-sm text-muted-foreground">
                        {new Date(b.createdAt).toLocaleDateString()}
                      </td>
                      <td className="px-6 py-4 text-right">
                        {b.bookingStatus === "ACTIVE" && (
                          <Button size="sm" variant="outline" onClick={() => handleCompleteBooking(b.id)}>
                            <CheckCircle2 className="mr-1.5 h-3.5 w-3.5" /> Complete
                          </Button>
                        )}
                      </td>
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
