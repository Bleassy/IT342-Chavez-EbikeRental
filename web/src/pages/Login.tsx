import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Bike, Mail, Lock } from "lucide-react";
import { useToast } from "@/hooks/use-toast";

const Login = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [googleLoading, setGoogleLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();
  const { toast } = useToast();

  const handleGoogleClick = () => {
    const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID;

    if (!clientId || clientId.includes("YOUR_GOOGLE_CLIENT_ID")) {
      toast({
        title: "Configuration Error",
        description: "Google Client ID not configured. Please contact support.",
        variant: "destructive",
      });
      return;
    }

    setGoogleLoading(true);

    // Redirect to Google OAuth2 authorization endpoint
    const redirectUri = `${window.location.origin}/auth/google/callback`;
    const scope = encodeURIComponent("openid profile email");
    const googleAuthUrl =
      `https://accounts.google.com/o/oauth2/v2/auth` +
      `?client_id=${clientId}` +
      `&redirect_uri=${encodeURIComponent(redirectUri)}` +
      `&response_type=code` +
      `&scope=${scope}` +
      `&access_type=offline` +
      `&prompt=consent`;

    window.location.href = googleAuthUrl;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    const success = await login({ email, password });
    setLoading(false);
    if (success) {
      navigate("/");
    } else {
      toast({ title: "Login failed", description: "Invalid credentials. Password must be at least 4 characters.", variant: "destructive" });
    }
  };

  return (
    <div className="flex min-h-screen">
      {/* Left panel */}
      <div className="hidden w-1/2 gradient-hero lg:flex lg:flex-col lg:items-center lg:justify-center lg:p-12">
        <div className="flex h-20 w-20 items-center justify-center rounded-2xl gradient-primary shadow-glow mb-8">
          <Bike className="h-10 w-10 text-primary-foreground" />
        </div>
        <h1 className="font-display text-4xl font-bold text-primary-foreground text-center">
          E-Bike Rental
        </h1>
        <p className="mt-4 max-w-md text-center text-lg text-primary-foreground/70">
          Explore the city on two wheels. Electric, effortless, eco-friendly.
        </p>
      </div>

      {/* Right panel */}
      <div className="flex w-full items-center justify-center px-6 lg:w-1/2">
        <div className="w-full max-w-md animate-fade-up">
          <div className="mb-8 lg:hidden flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-lg gradient-primary">
              <Bike className="h-5 w-5 text-primary-foreground" />
            </div>
            <span className="font-display text-xl font-bold">E-Bike Rental</span>
          </div>

          <h2 className="font-display text-3xl font-bold text-foreground">Welcome back</h2>
          <p className="mt-2 text-muted-foreground">Sign in to your account to continue</p>

          <form onSubmit={handleSubmit} className="mt-8 space-y-5">
            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                  id="email"
                  type="email"
                  placeholder="you@example.com"
                  className="pl-10"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </div>
            </div>
            <div className="space-y-2">
              <Label htmlFor="password">Password</Label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                  id="password"
                  type="password"
                  placeholder="••••••••"
                  className="pl-10"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
              </div>
            </div>
            <Button type="submit" className="w-full gradient-primary text-primary-foreground" disabled={loading}>
              {loading ? "Signing in..." : "Sign In"}
            </Button>
          </form>

          {/* Divider */}
          <div className="relative my-6">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-muted"></div>
            </div>
            <div className="relative flex justify-center text-sm">
              <span className="bg-background px-2 text-muted-foreground">Or continue with</span>
            </div>
          </div>

          {/* Google Sign-In Button */}
          <Button
            type="button"
            variant="outline"
            className="w-full flex items-center justify-center gap-2"
            onClick={handleGoogleClick}
            disabled={googleLoading || loading}
          >
            <svg
              className="w-4 h-4"
              viewBox="0 0 24 24"
              fill="currentColor"
            >
              <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
              <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
              <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
              <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
            </svg>
            {googleLoading ? "Signing in..." : "Continue with Google"}
          </Button>

          <p className="mt-6 text-center text-sm text-muted-foreground">
            Don't have an account?{" "}
            <Link to="/register" className="font-medium text-primary hover:underline">
              Sign up
            </Link>
          </p>

          <div className="mt-8 rounded-lg bg-muted p-4 text-sm text-muted-foreground">
            <p className="font-medium text-foreground mb-1">Demo Accounts:</p>
            <p>User: john@example.com</p>
            <p>Admin: admin@ebike.com</p>
            <p className="mt-1 text-xs">Any password with 4+ characters works</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;
