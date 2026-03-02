import { useState, useEffect, useRef } from "react";
import { useAuth } from "@/contexts/AuthContext";
import { fetchProfile, updateProfile, type ProfileData } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { useToast } from "@/hooks/use-toast";
import { User as UserIcon, Mail, Phone, MapPin, Pencil, Save, Loader2, Camera, Shield } from "lucide-react";

const Profile = () => {
  const { user, updateUser } = useAuth();
  const { toast } = useToast();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [profile, setProfile] = useState<ProfileData | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [editing, setEditing] = useState(false);

  // Editable form state
  const [form, setForm] = useState({
    firstName: "",
    lastName: "",
    phone: "",
    address: "",
    nickname: "",
  });

  useEffect(() => {
    fetchProfile()
      .then((p) => {
        setProfile(p);
        setForm({
          firstName: p.firstName || "",
          lastName: p.lastName || "",
          phone: p.phone || "",
          address: p.address || "",
          nickname: p.nickname || "",
        });
      })
      .catch((err) => console.error("Failed to fetch profile:", err))
      .finally(() => setLoading(false));
  }, []);

  const handleSave = async () => {
    setSaving(true);
    try {
      const updated = await updateProfile({
        firstName: form.firstName,
        lastName: form.lastName,
        phone: form.phone || undefined,
        address: form.address || undefined,
        nickname: form.nickname || undefined,
      });
      setProfile(updated);
      setEditing(false);
      // Sync with auth context so Navbar updates
      if (updateUser) {
        updateUser({
          firstName: updated.firstName,
          lastName: updated.lastName,
          phone: updated.phone || undefined,
          address: updated.address || undefined,
          nickname: updated.nickname || undefined,
          profilePictureUrl: updated.profilePictureUrl || undefined,
        });
      }
      toast({ title: "Profile updated", description: "Your changes have been saved." });
    } catch (err) {
      toast({ title: "Failed to save", description: "Could not update profile.", variant: "destructive" });
    } finally {
      setSaving(false);
    }
  };

  const handleImageUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // Convert to base64 for storage
    const reader = new FileReader();
    reader.onload = async () => {
      const base64 = reader.result as string;
      try {
        const updated = await updateProfile({ profilePictureUrl: base64 });
        setProfile(updated);
        if (updateUser) {
          updateUser({ profilePictureUrl: updated.profilePictureUrl || undefined });
        }
        toast({ title: "Profile picture updated" });
      } catch {
        toast({ title: "Upload failed", variant: "destructive" });
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

  if (!profile) {
    return (
      <div className="container py-20 text-center">
        <h1 className="text-2xl font-bold">Profile not found</h1>
      </div>
    );
  }

  const displayName = profile.nickname || `${profile.firstName} ${profile.lastName}`;

  return (
    <div className="container max-w-2xl py-8">
      <div className="mb-8 animate-fade-up">
        <h1 className="font-display text-3xl font-bold text-foreground">My Profile</h1>
        <p className="mt-2 text-muted-foreground">Manage your account information</p>
      </div>

      <div className="glass-card p-8 animate-fade-up">
        {/* Profile Picture + Name Header */}
        <div className="flex flex-col items-center gap-4 mb-8 sm:flex-row sm:items-start sm:gap-6">
          <div className="relative group">
            <div className="h-24 w-24 rounded-full overflow-hidden bg-muted flex items-center justify-center border-2 border-border">
              {profile.profilePictureUrl ? (
                <img src={profile.profilePictureUrl} alt="Profile" className="h-full w-full object-cover" />
              ) : (
                <UserIcon className="h-12 w-12 text-muted-foreground" />
              )}
            </div>
            <button
              onClick={() => fileInputRef.current?.click()}
              className="absolute inset-0 flex items-center justify-center rounded-full bg-black/50 opacity-0 group-hover:opacity-100 transition-opacity cursor-pointer"
            >
              <Camera className="h-6 w-6 text-white" />
            </button>
            <input
              ref={fileInputRef}
              type="file"
              accept="image/*"
              className="hidden"
              onChange={handleImageUpload}
            />
          </div>
          <div className="text-center sm:text-left">
            <h2 className="font-display text-2xl font-bold text-foreground">{displayName}</h2>
            <div className="flex items-center gap-2 mt-1">
              <Mail className="h-4 w-4 text-muted-foreground" />
              <span className="text-muted-foreground">{profile.email}</span>
            </div>
            <div className="mt-2">
              <span className={`rounded-full px-3 py-1 text-xs font-semibold ${
                profile.role === "ADMIN" ? "bg-primary/15 text-primary" : "bg-muted text-muted-foreground"
              }`}>
                <Shield className="inline h-3 w-3 mr-1" />{profile.role}
              </span>
            </div>
            <p className="text-xs text-muted-foreground mt-2">
              Member since {new Date(profile.createdAt).toLocaleDateString("en-US", { year: "numeric", month: "long", day: "numeric" })}
            </p>
          </div>
        </div>

        <hr className="border-border mb-6" />

        {/* Profile Fields */}
        <div className="space-y-5">
          {/* Email - Read Only */}
          <div className="space-y-1.5">
            <Label className="text-muted-foreground text-xs uppercase tracking-wider">Email</Label>
            <div className="flex items-center gap-3 rounded-lg bg-muted/50 px-4 py-3">
              <Mail className="h-4 w-4 text-muted-foreground" />
              <span className="text-foreground">{profile.email}</span>
              <span className="ml-auto text-xs text-muted-foreground italic">Cannot be changed</span>
            </div>
          </div>

          {/* Role - Read Only */}
          <div className="space-y-1.5">
            <Label className="text-muted-foreground text-xs uppercase tracking-wider">Role</Label>
            <div className="flex items-center gap-3 rounded-lg bg-muted/50 px-4 py-3">
              <Shield className="h-4 w-4 text-muted-foreground" />
              <span className="text-foreground">{profile.role}</span>
              <span className="ml-auto text-xs text-muted-foreground italic">Cannot be changed</span>
            </div>
          </div>

          {/* Editable fields */}
          <div className="grid gap-5 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="firstName">First Name</Label>
              {editing ? (
                <Input id="firstName" value={form.firstName} onChange={(e) => setForm({ ...form, firstName: e.target.value })} />
              ) : (
                <div className="rounded-lg border border-border px-4 py-2.5 text-foreground">{profile.firstName || "—"}</div>
              )}
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="lastName">Last Name</Label>
              {editing ? (
                <Input id="lastName" value={form.lastName} onChange={(e) => setForm({ ...form, lastName: e.target.value })} />
              ) : (
                <div className="rounded-lg border border-border px-4 py-2.5 text-foreground">{profile.lastName || "—"}</div>
              )}
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="nickname">Nickname</Label>
              {editing ? (
                <Input id="nickname" value={form.nickname} onChange={(e) => setForm({ ...form, nickname: e.target.value })} placeholder="Your display name" />
              ) : (
                <div className="rounded-lg border border-border px-4 py-2.5 text-foreground">{profile.nickname || "—"}</div>
              )}
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="phone">Phone Number</Label>
              {editing ? (
                <Input id="phone" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} placeholder="+63 9XX XXX XXXX" />
              ) : (
                <div className="flex items-center gap-2 rounded-lg border border-border px-4 py-2.5 text-foreground">
                  <Phone className="h-4 w-4 text-muted-foreground" />
                  {profile.phone || "—"}
                </div>
              )}
            </div>
          </div>

          <div className="space-y-1.5">
            <Label htmlFor="address">Address</Label>
            {editing ? (
              <Textarea id="address" value={form.address} onChange={(e) => setForm({ ...form, address: e.target.value })} placeholder="Your address" rows={3} />
            ) : (
              <div className="flex items-start gap-2 rounded-lg border border-border px-4 py-2.5 text-foreground min-h-[60px]">
                <MapPin className="h-4 w-4 text-muted-foreground mt-0.5" />
                {profile.address || "—"}
              </div>
            )}
          </div>
        </div>

        {/* Action Buttons */}
        <div className="mt-8 flex gap-3">
          {editing ? (
            <>
              <Button className="gradient-primary text-primary-foreground" onClick={handleSave} disabled={saving}>
                {saving ? <><Loader2 className="mr-2 h-4 w-4 animate-spin" /> Saving...</> : <><Save className="mr-2 h-4 w-4" /> Save Changes</>}
              </Button>
              <Button variant="outline" onClick={() => {
                setEditing(false);
                setForm({
                  firstName: profile.firstName || "",
                  lastName: profile.lastName || "",
                  phone: profile.phone || "",
                  address: profile.address || "",
                  nickname: profile.nickname || "",
                });
              }}>
                Cancel
              </Button>
            </>
          ) : (
            <Button variant="outline" onClick={() => setEditing(true)}>
              <Pencil className="mr-2 h-4 w-4" /> Edit Profile
            </Button>
          )}
        </div>
      </div>
    </div>
  );
};

export default Profile;
