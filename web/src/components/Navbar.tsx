import { Link, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext";
import { Button } from "@/components/ui/button";
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle, AlertDialogTrigger } from "@/components/ui/alert-dialog";
import { Zap, Menu, X, User, LogOut, Shield } from "lucide-react";
import { useState } from "react";

const Navbar = () => {
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileOpen, setMobileOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  const isActive = (path: string) => location.pathname === path;

  const navLinks = [
    { to: "/", label: "Dashboard" },
    { to: "/bikes", label: "E-Bikes" },
    { to: "/history", label: "My Rentals" },
  ];

  if (!isAuthenticated) return null;

  return (
    <header className="sticky top-0 z-50 w-full border-b border-border bg-background/80 backdrop-blur-lg">
      <div className="container flex h-16 items-center justify-between">
        <Link to="/" className="flex items-center gap-2 font-display text-xl font-bold text-foreground">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg gradient-primary">
            <Zap className="h-5 w-5 text-primary-foreground" />
          </div>
          E-Bike Rental
        </Link>

        {/* Desktop Nav */}
        <nav className="hidden items-center gap-1 md:flex">
          {navLinks.map((link) => (
            <Link
              key={link.to}
              to={link.to}
              className={`rounded-lg px-4 py-2 text-sm font-medium transition-colors ${
                isActive(link.to)
                  ? "bg-primary/10 text-primary"
                  : "text-muted-foreground hover:text-foreground"
              }`}
            >
              {link.label}
            </Link>
          ))}
          {user?.role === "ADMIN" && (
            <Link
              to="/admin"
              className={`flex items-center gap-1.5 rounded-lg px-4 py-2 text-sm font-medium transition-colors ${
                location.pathname.startsWith("/admin")
                  ? "bg-primary/10 text-primary"
                  : "text-muted-foreground hover:text-foreground"
              }`}
            >
              <Shield className="h-3.5 w-3.5" />
              Admin
            </Link>
          )}
        </nav>

        <div className="hidden items-center gap-3 md:flex">
          <Link
            to="/profile"
            className="flex items-center gap-2 rounded-lg bg-muted px-3 py-1.5 hover:bg-muted/80 transition-colors cursor-pointer"
          >
            {user?.profilePictureUrl ? (
              <img src={user.profilePictureUrl} alt="" className="h-6 w-6 rounded-full object-cover" />
            ) : (
              <User className="h-4 w-4 text-muted-foreground" />
            )}
            <span className="text-sm font-medium">{user?.nickname || user?.firstName}</span>
            {user?.role === "ADMIN" && (
              <span className="rounded bg-primary/15 px-1.5 py-0.5 text-xs font-semibold text-primary">
                ADMIN
              </span>
            )}
          </Link>
          <AlertDialog>
            <AlertDialogTrigger asChild>
              <Button variant="ghost" size="sm">
                <LogOut className="mr-1.5 h-4 w-4" />
                Logout
              </Button>
            </AlertDialogTrigger>
            <AlertDialogContent>
              <AlertDialogHeader>
                <AlertDialogTitle>Are you sure you want to logout?</AlertDialogTitle>
                <AlertDialogDescription>You will need to sign in again to access your account.</AlertDialogDescription>
              </AlertDialogHeader>
              <AlertDialogFooter>
                <AlertDialogCancel>No, stay</AlertDialogCancel>
                <AlertDialogAction onClick={handleLogout}>Yes, logout</AlertDialogAction>
              </AlertDialogFooter>
            </AlertDialogContent>
          </AlertDialog>
        </div>

        {/* Mobile toggle */}
        <button className="md:hidden" onClick={() => setMobileOpen(!mobileOpen)}>
          {mobileOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
        </button>
      </div>

      {/* Mobile menu */}
      {mobileOpen && (
        <div className="border-t border-border bg-background p-4 md:hidden animate-fade-in">
          <nav className="flex flex-col gap-2">
            {navLinks.map((link) => (
              <Link
                key={link.to}
                to={link.to}
                onClick={() => setMobileOpen(false)}
                className={`rounded-lg px-4 py-2.5 text-sm font-medium ${
                  isActive(link.to) ? "bg-primary/10 text-primary" : "text-muted-foreground"
                }`}
              >
                {link.label}
              </Link>
            ))}
            {user?.role === "ADMIN" && (
              <Link
                to="/admin"
                onClick={() => setMobileOpen(false)}
                className="rounded-lg px-4 py-2.5 text-sm font-medium text-muted-foreground"
              >
                Admin Panel
              </Link>
            )}
            <Link
              to="/profile"
              onClick={() => setMobileOpen(false)}
              className="flex items-center gap-2 rounded-lg px-4 py-2.5 text-sm font-medium text-muted-foreground"
            >
              {user?.profilePictureUrl ? (
                <img src={user.profilePictureUrl} alt="" className="h-5 w-5 rounded-full object-cover" />
              ) : (
                <User className="h-4 w-4" />
              )}
              My Profile
            </Link>
            <AlertDialog>
              <AlertDialogTrigger asChild>
                <button className="mt-2 rounded-lg px-4 py-2.5 text-left text-sm font-medium text-destructive">
                  Logout
                </button>
              </AlertDialogTrigger>
              <AlertDialogContent>
                <AlertDialogHeader>
                  <AlertDialogTitle>Are you sure you want to logout?</AlertDialogTitle>
                  <AlertDialogDescription>You will need to sign in again to access your account.</AlertDialogDescription>
                </AlertDialogHeader>
                <AlertDialogFooter>
                  <AlertDialogCancel>No, stay</AlertDialogCancel>
                  <AlertDialogAction onClick={() => { handleLogout(); setMobileOpen(false); }}>Yes, logout</AlertDialogAction>
                </AlertDialogFooter>
              </AlertDialogContent>
            </AlertDialog>
          </nav>
        </div>
      )}
    </header>
  );
};

export default Navbar;
