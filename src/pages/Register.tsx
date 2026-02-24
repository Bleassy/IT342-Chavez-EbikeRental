import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Zap, Mail, Lock, User as UserIcon } from "lucide-react";
import { useToast } from "@/hooks/use-toast";

const Register = () => {
  const [form, setForm] = useState({
    username: "", email: "", firstName: "", lastName: "", password: "", confirmPassword: "",
  });
  const [loading, setLoading] = useState(false);
  const { register } = useAuth();
  const navigate = useNavigate();
  const { toast } = useToast();

  const update = (field: string, value: string) => setForm((prev) => ({ ...prev, [field]: value }));

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (form.password !== form.confirmPassword) {
      toast({ title: "Error", description: "Passwords don't match", variant: "destructive" });
      return;
    }
    setLoading(true);
    const success = await register(form);
    setLoading(false);
    if (success) {
      navigate("/");
    } else {
      toast({ title: "Error", description: "Registration failed. Check your inputs.", variant: "destructive" });
    }
  };

  return (
    <div className="flex min-h-screen">
      <div className="hidden w-1/2 gradient-hero lg:flex lg:flex-col lg:items-center lg:justify-center lg:p-12">
        <div className="flex h-20 w-20 items-center justify-center rounded-2xl gradient-primary shadow-glow mb-8">
          <Zap className="h-10 w-10 text-primary-foreground" />
        </div>
        <h1 className="font-display text-4xl font-bold text-primary-foreground text-center">Join the Ride</h1>
        <p className="mt-4 max-w-md text-center text-lg text-primary-foreground/70">
          Create your account and start exploring the city electrically.
        </p>
      </div>

      <div className="flex w-full items-center justify-center px-6 py-12 lg:w-1/2">
        <div className="w-full max-w-md animate-fade-up">
          <h2 className="font-display text-3xl font-bold text-foreground">Create account</h2>
          <p className="mt-2 text-muted-foreground">Fill in your details to get started</p>

          <form onSubmit={handleSubmit} className="mt-8 space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label>First Name</Label>
                <Input placeholder="John" value={form.firstName} onChange={(e) => update("firstName", e.target.value)} required />
              </div>
              <div className="space-y-2">
                <Label>Last Name</Label>
                <Input placeholder="Doe" value={form.lastName} onChange={(e) => update("lastName", e.target.value)} required />
              </div>
            </div>
            <div className="space-y-2">
              <Label>Username</Label>
              <div className="relative">
                <UserIcon className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <Input className="pl-10" placeholder="johndoe" value={form.username} onChange={(e) => update("username", e.target.value)} required />
              </div>
            </div>
            <div className="space-y-2">
              <Label>Email</Label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <Input className="pl-10" type="email" placeholder="you@example.com" value={form.email} onChange={(e) => update("email", e.target.value)} required />
              </div>
            </div>
            <div className="space-y-2">
              <Label>Password</Label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <Input className="pl-10" type="password" placeholder="••••••••" value={form.password} onChange={(e) => update("password", e.target.value)} required />
              </div>
            </div>
            <div className="space-y-2">
              <Label>Confirm Password</Label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <Input className="pl-10" type="password" placeholder="••••••••" value={form.confirmPassword} onChange={(e) => update("confirmPassword", e.target.value)} required />
              </div>
            </div>
            <Button type="submit" className="w-full gradient-primary text-primary-foreground" disabled={loading}>
              {loading ? "Creating..." : "Create Account"}
            </Button>
          </form>

          <p className="mt-6 text-center text-sm text-muted-foreground">
            Already have an account?{" "}
            <Link to="/login" className="font-medium text-primary hover:underline">Sign in</Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Register;
