import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import axiosClient from '../api/axiosClient';
import { Package, Clock, CheckCircle2, AlertCircle, ArrowRight, ArrowLeft, Loader2, ShoppingBag } from 'lucide-react';

export default function OrdersPage() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchOrders = async () => {
    setLoading(true);
    setError('');
    try {
      const res = await axiosClient.get('/orders');
      // res is unwrapped ApiResponse<List<OrderDto>>
      const list = res.data || res;
      setOrders(Array.isArray(list) ? list : []);
    } catch (err) {
      console.error('Failed to load orders', err);
      setError('Could not retrieve orders. Please check your connectivity.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, []);

  if (loading) {
    return (
      <div className="max-w-5xl mx-auto py-20 px-4 text-center flex flex-col items-center justify-center">
        <Loader2 className="w-10 h-10 text-blue-600 animate-spin mb-4" />
        <p className="text-sm font-medium text-gray-500">Loading your order history...</p>
      </div>
    );
  }

  if (orders.length === 0) {
    return (
      <div className="flex-1 bg-gray-50/50 py-16 px-4 text-center flex flex-col items-center justify-center">
        <div className="w-20 h-20 bg-blue-50 text-blue-600 rounded-3xl flex items-center justify-center mb-6 shadow-sm">
          <Package className="w-10 h-10 stroke-[1.5]" />
        </div>
        <h2 className="text-2xl sm:text-3xl font-black text-gray-900 tracking-tight mb-2">
          No Orders Yet
        </h2>
        <p className="text-sm text-gray-500 max-w-md mb-8 leading-relaxed">
          You haven't placed any orders with FlopKart yet. Discover our latest tech gear and electronics today!
        </p>
        <Link
          to="/"
          className="inline-flex items-center gap-2 px-6 py-3.5 bg-blue-600 hover:bg-blue-700 text-white font-bold text-sm rounded-xl shadow-md hover:shadow-lg transition-all"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Start Shopping</span>
        </Link>
      </div>
    );
  }

  return (
    <div className="flex-1 bg-gray-50/50 py-10">
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
        
        {/* Header */}
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-2xl sm:text-3xl font-black text-gray-900 tracking-tight">
              Order History
            </h1>
            <p className="text-xs text-gray-500 mt-1">
              {orders.length} {orders.length === 1 ? 'order' : 'orders'} placed and verified via distributed event stream
            </p>
          </div>
          <Link
            to="/"
            className="text-xs font-bold text-blue-600 hover:text-blue-700 hover:underline flex items-center gap-1"
          >
            <span>Continue Shopping</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </Link>
        </div>

        {error && (
          <div className="mb-6 p-4 bg-red-50 border border-red-200 text-red-700 text-sm rounded-2xl flex items-center justify-between">
            <span>{error}</span>
            <button onClick={fetchOrders} className="text-xs font-bold underline">Retry</button>
          </div>
        )}

        {/* Order Cards List */}
        <div className="space-y-6">
          {orders.map((order) => {
            const { orderNumber, status, totalAmount, items, createdAt, shippingAddress } = order;
            const isConfirmed = status === 'CONFIRMED';
            const isPending = status === 'PENDING';
            const isCancelled = status === 'CANCELLED';

            return (
              <div
                key={orderNumber}
                className="bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden hover:shadow-md transition-shadow"
              >
                {/* Order Top Bar */}
                <div className="bg-gray-50/80 px-6 py-4 border-b border-gray-100 flex flex-wrap items-center justify-between gap-4 text-xs">
                  <div className="flex items-center gap-4">
                    <div>
                      <span className="text-gray-400 block font-medium">Order Number</span>
                      <span className="font-mono font-bold text-gray-900">{orderNumber}</span>
                    </div>
                    {createdAt && (
                      <div className="border-l border-gray-200 pl-4">
                        <span className="text-gray-400 block font-medium">Date Placed</span>
                        <span className="text-gray-700">{new Date(createdAt).toLocaleDateString()}</span>
                      </div>
                    )}
                  </div>

                  <div className="flex items-center gap-4">
                    <div>
                      <span className="text-gray-400 block font-medium text-right">Total Amount</span>
                      <span className="font-black text-gray-900">${Number(totalAmount).toFixed(2)}</span>
                    </div>

                    {/* Status Pill */}
                    {isConfirmed && (
                      <span className="inline-flex items-center gap-1.5 px-3 py-1 bg-emerald-50 text-emerald-700 font-bold text-xs rounded-full border border-emerald-200">
                        <CheckCircle2 className="w-3.5 h-3.5" /> CONFIRMED
                      </span>
                    )}
                    {isPending && (
                      <span className="inline-flex items-center gap-1.5 px-3 py-1 bg-amber-50 text-amber-700 font-bold text-xs rounded-full border border-amber-200 animate-pulse">
                        <Clock className="w-3.5 h-3.5" /> PENDING SAGA
                      </span>
                    )}
                    {isCancelled && (
                      <span className="inline-flex items-center gap-1.5 px-3 py-1 bg-red-50 text-red-700 font-bold text-xs rounded-full border border-red-200">
                        <AlertCircle className="w-3.5 h-3.5" /> CANCELLED
                      </span>
                    )}
                  </div>
                </div>

                {/* Items & Footer */}
                <div className="p-6">
                  <div className="divide-y divide-gray-100">
                    {items && items.map((item, idx) => (
                      <div key={idx} className="py-2.5 first:pt-0 last:pb-0 flex items-center justify-between text-xs">
                        <div>
                          <p className="font-bold text-gray-900">{item.productName}</p>
                          <p className="text-gray-400">Qty: {item.quantity}</p>
                        </div>
                        <span className="font-semibold text-gray-900">
                          ${(Number(item.price) * item.quantity).toFixed(2)}
                        </span>
                      </div>
                    ))}
                  </div>

                  <div className="mt-6 pt-4 border-t border-gray-100 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
                    <p className="text-xs text-gray-500 truncate max-w-md">
                      Shipping to: <span className="text-gray-700">{shippingAddress || 'Standard Address'}</span>
                    </p>

                    <Link
                      to={`/orders/${orderNumber}`}
                      className="inline-flex items-center gap-1 text-xs font-bold text-blue-600 hover:text-blue-700 hover:underline"
                    >
                      <span>Track Saga Status</span>
                      <ArrowRight className="w-3.5 h-3.5" />
                    </Link>
                  </div>
                </div>
              </div>
            );
          })}
        </div>

      </div>
    </div>
  );
}
