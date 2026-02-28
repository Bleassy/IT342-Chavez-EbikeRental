import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext";
import { useToast } from "@/hooks/use-toast";
import { Zap } from "lucide-react";

const GoogleCallback = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { loginWithGoogleCode } = useAuth();
  const { toast } = useToast();
  const [processing, setProcessing] = useState(true);

  useEffect(() => {
    const code = searchParams.get("code");
    const error = searchParams.get("error");

    if (error) {
      toast({ title: "Error", description: "Google login was cancelled or failed.", variant: "destructive" });
      navigate("/login");
      return;
    }

    if (!code) {
      toast({ title: "Error", description: "No authorization code received.", variant: "destructive" });
      navigate("/login");
      return;
    }

    const exchangeCode = async () => {
      try {
        const success = await loginWithGoogleCode(code);
        if (success) {
          toast({ title: "Success", description: "Logged in with Google!", variant: "default" });
          navigate("/");
        } else {
          toast({ title: "Error", description: "Google login failed.", variant: "destructive" });
          navigate("/login");
        }
      } catch (err) {
        console.error("Google callback error:", err);
        toast({ title: "Error", description: "Google login failed.", variant: "destructive" });
        navigate("/login");
      } finally {
        setProcessing(false);
      }
    };

    exchangeCode();
  }, [searchParams, navigate, loginWithGoogleCode, toast]);

  return (
    <div className="flex min-h-screen items-center justify-center">
      <div className="text-center animate-fade-up">
        <div className="flex h-16 w-16 items-center justify-center rounded-2xl gradient-primary shadow-glow mb-4 mx-auto animate-pulse">
          <Zap className="h-8 w-8 text-primary-foreground" />
        </div>
        <h2 className="text-xl font-semibold">
          {processing ? "Signing you in with Google..." : "Redirecting..."}
        </h2>
        <p className="text-muted-foreground mt-2">Please wait a moment</p>
      </div>
    </div>
  );
};

export default GoogleCallback;
