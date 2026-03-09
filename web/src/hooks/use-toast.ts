import * as React from "react";

export interface ToastProps {
  id: string;
  title?: string;
  description?: string;
  variant?: "default" | "destructive";
  open?: boolean;
  onOpenChange?: (open: boolean) => void;
}

const TOAST_LIMIT = 5;
const TOAST_REMOVE_DELAY = 3000;

type ToastAction =
  | { type: "ADD_TOAST"; toast: ToastProps }
  | { type: "DISMISS_TOAST"; toastId: string }
  | { type: "REMOVE_TOAST"; toastId: string };

interface ToastState {
  toasts: ToastProps[];
}

function reducer(state: ToastState, action: ToastAction): ToastState {
  switch (action.type) {
    case "ADD_TOAST":
      return { toasts: [action.toast, ...state.toasts].slice(0, TOAST_LIMIT) };
    case "DISMISS_TOAST":
      return {
        toasts: state.toasts.map((t) =>
          t.id === action.toastId ? { ...t, open: false } : t
        ),
      };
    case "REMOVE_TOAST":
      return { toasts: state.toasts.filter((t) => t.id !== action.toastId) };
    default:
      return state;
  }
}

let dispatch: React.Dispatch<ToastAction> | null = null;

let count = 0;
function genId() {
  return `toast-${++count}`;
}

export function toast(props: Omit<ToastProps, "id">) {
  const id = genId();
  const t: ToastProps = { ...props, id, open: true };
  if (dispatch) {
    dispatch({ type: "ADD_TOAST", toast: t });
    setTimeout(() => {
      if (dispatch) dispatch({ type: "DISMISS_TOAST", toastId: id });
      setTimeout(() => {
        if (dispatch) dispatch({ type: "REMOVE_TOAST", toastId: id });
      }, 300);
    }, TOAST_REMOVE_DELAY);
  }
  return id;
}

export function useToast() {
  const [state, localDispatch] = React.useReducer(reducer, { toasts: [] });

  React.useEffect(() => {
    dispatch = localDispatch;
    return () => {
      dispatch = null;
    };
  }, [localDispatch]);

  return {
    toasts: state.toasts,
    toast: (props: Omit<ToastProps, "id">) => toast(props),
    dismiss: (toastId: string) => localDispatch({ type: "DISMISS_TOAST", toastId }),
  };
}
