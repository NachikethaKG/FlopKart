import React from 'react';
import { useCart } from '../context/CartContext';
import { CheckCircle2, AlertCircle, Info, X } from 'lucide-react';

export default function Toast() {
  const { toast, dismissToast } = useCart();

  if (!toast.visible) return null;

  const bgStyles = {
    success: 'bg-emerald-600 text-white',
    error: 'bg-red-600 text-white',
    info: 'bg-blue-600 text-white',
  }[toast.type || 'success'];

  const Icon = {
    success: CheckCircle2,
    error: AlertCircle,
    info: Info,
  }[toast.type || 'success'];

  return (
    <aside
      aria-label="Notification"
      className="fixed bottom-6 right-6 z-50 animate-in slide-in-from-bottom-5 duration-300"
    >
      <div className={`flex items-center gap-3 px-4 py-3 rounded-2xl shadow-xl ${bgStyles}`}>
        <Icon className="w-5 h-5 flex-shrink-0" />
        <span className="text-sm font-semibold">{toast.message}</span>
        <button
          onClick={dismissToast}
          className="p-1 hover:bg-white/20 rounded-lg transition-colors ml-2"
          aria-label="Dismiss notification"
        >
          <X className="w-4 h-4" />
        </button>
      </div>
    </aside>
  );
}
