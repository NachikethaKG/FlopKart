import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import axiosClient from '../api/axiosClient';
import { CheckCircle2, Clock, AlertCircle, ArrowLeft, Package, Truck, CreditCard, Sparkles, Loader2 } from 'lucide-react';

export default function OrderDetailPage() {
  const { orderNumber } = useParams();

  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [pollCount, setPollCount] = useState(0);

  const fetchOrder = async () => {
    try {
      const res = await axiosClient.get(`/orders/${orderNumber}`);
      const orderData = res.data || res;
      setOrder(orderData);
      return orderData;
    } catch (err) {
      console.error('Failed to load order', err);
      setError('Order not found or access denied.');
      return null;
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    let intervalId = null;

    const startPolling = async () => {
      const currentOrder = await fetchOrder();
      
      // If order is PENDING, poll every 2s up to 15 times (~30s max)
      if (currentOrder && currentOrder.status === 'PENDING') {
        intervalId = setInterval(async () => {
          setPollCount((prev) => prev + 1);
          const updated = await fetchOrder();
          if (updated && updated.status !== 'PENDING') {
            clearInterval(intervalId);
          }
        }, 2000);
      }
    };

    startPolling();

    return () => {
      if (intervalId) clearInterval(intervalId);
    };
  }, [orderNumber]);

  if (loading && !order) {
    return (
      <div className="max-w-4xl mx-auto py-20 px-4 text-center flex flex-col items-center justify-center">
        <Loader2 className="w-10 h-10 text-blue-600 animate-spin mb-4" />
        <p className="text-sm text-gray-500">Querying order state from distributed ledger...</p>
      </div>
    );
  }

  if (error || !order) {
    return (
      <div className="max-w-4xl mx-auto py-20 px-4 text-center">
        <div className="w-16 h-16 bg-red-50 text-red-500 rounded-2xl flex items-center justify-center mx-auto mb-4">
          <AlertCircle className="w-8 h-8" />
        </div>
        <h2 className="text-xl font-bold text-gray-900 mb-2">Order Not Found</h2>
        <p className="text-sm text-gray-500 mb-6">{error || 'No matching order was located.'}</p>
        <Link
          to="/orders"
          className="inline-flex items-center gap-2 px-5 py-2.5 bg-blue-600 text-white font-bold text-sm rounded-xl"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>View All Orders</span>
        </Link>
      </div>
    );
  }

  const { status, totalAmount, shippingAddress, items, createdAt } = order;
  const isPending = status === 'PENDING';
  const isConfirmed = status === 'CONFIRMED';
  const isCancelled = status === 'CANCELLED';

  return (
    <div className="flex-1 bg-gray-50/50 py-10">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        
        {/* Navigation Breadcrumb */}
        <div className="mb-6 flex items-center justify-between">
          <Link
            to="/orders"
            className="inline-flex items-center gap-1.5 text-xs font-bold text-gray-500 hover:text-blue-600 transition-colors"
          >
            <ArrowLeft className="w-3.5 h-3.5" />
            <span>Back to All Orders</span>
          </Link>
          <span className="text-xs text-gray-400">
            Order #{orderNumber}
          </span>
        </div>

        {/* Main Card */}
        <div className="bg-white rounded-3xl border border-gray-100 shadow-sm p-6 sm:p-10 space-y-8">
          
          {/* Status Header */}
          <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 border-b border-gray-100 pb-6">
            <div>
              <span className="text-xs font-bold uppercase tracking-wider text-gray-400">Order Reference</span>
              <h1 className="text-2xl sm:text-3xl font-black text-gray-900 tracking-tight font-mono">
                {orderNumber}
              </h1>
              {createdAt && (
                <p className="text-xs text-gray-500 mt-1">
                  Placed on {new Date(createdAt).toLocaleString()}
                </p>
              )}
            </div>

            {/* Dynamic Status Pill */}
            <div>
              {isConfirmed && (
                <span className="inline-flex items-center gap-2 px-4 py-2 bg-emerald-50 text-emerald-700 font-extrabold text-sm rounded-2xl border border-emerald-200">
                  <CheckCircle2 className="w-5 h-5 text-emerald-600" />
                  <span>CONFIRMED</span>
                </span>
              )}
              {isPending && (
                <span className="inline-flex items-center gap-2 px-4 py-2 bg-amber-50 text-amber-700 font-extrabold text-sm rounded-2xl border border-amber-200 animate-pulse">
                  <Clock className="w-5 h-5 text-amber-600" />
                  <span>PROCESSING SAGA ({pollCount}s)</span>
                </span>
              )}
              {isCancelled && (
                <span className="inline-flex items-center gap-2 px-4 py-2 bg-red-50 text-red-700 font-extrabold text-sm rounded-2xl border border-red-200">
                  <AlertCircle className="w-5 h-5 text-red-600" />
                  <span>CANCELLED</span>
                </span>
              )}
            </div>
          </div>

          {/* Saga Timeline Tracker */}
          <div className="p-6 bg-gray-50 rounded-2xl border border-gray-100 space-y-4">
            <h3 className="text-xs font-bold uppercase tracking-wider text-gray-500 flex items-center gap-1.5">
              <Sparkles className="w-4 h-4 text-blue-600" />
              <span>Kafka Saga Orchestration Status</span>
            </h3>

            <div className="grid grid-cols-1 sm:grid-cols-4 gap-4 pt-2">
              <div className="flex flex-col items-center sm:items-start text-center sm:text-left">
                <div className="w-8 h-8 rounded-full bg-blue-600 text-white flex items-center justify-center font-bold text-xs mb-2 shadow">
                  ✓
                </div>
                <span className="text-xs font-bold text-gray-900">1. Order Placed</span>
                <span className="text-[10px] text-gray-500">Order Service</span>
              </div>

              <div className="flex flex-col items-center sm:items-start text-center sm:text-left">
                <div className="w-8 h-8 rounded-full bg-blue-600 text-white flex items-center justify-center font-bold text-xs mb-2 shadow">
                  ✓
                </div>
                <span className="text-xs font-bold text-gray-900">2. Inventory Reserved</span>
                <span className="text-[10px] text-gray-500">Product Service</span>
              </div>

              <div className="flex flex-col items-center sm:items-start text-center sm:text-left">
                <div className={`w-8 h-8 rounded-full flex items-center justify-center font-bold text-xs mb-2 shadow ${
                  isConfirmed ? 'bg-blue-600 text-white' : isPending ? 'bg-amber-500 text-white animate-pulse' : 'bg-red-500 text-white'
                }`}>
                  {isConfirmed ? '✓' : isPending ? '...' : '✕'}
                </div>
                <span className="text-xs font-bold text-gray-900">3. Payment Event</span>
                <span className="text-[10px] text-gray-500">Payment Service (Kafka)</span>
              </div>

              <div className="flex flex-col items-center sm:items-start text-center sm:text-left">
                <div className={`w-8 h-8 rounded-full flex items-center justify-center font-bold text-xs mb-2 shadow ${
                  isConfirmed ? 'bg-emerald-600 text-white' : 'bg-gray-200 text-gray-500'
                }`}>
                  {isConfirmed ? '✓' : '4'}
                </div>
                <span className="text-xs font-bold text-gray-900">4. Order Confirmed</span>
                <span className="text-[10px] text-gray-500">Final Outcome</span>
              </div>
            </div>
          </div>

          {/* Itemized Order Table */}
          <div>
            <h3 className="text-sm font-bold uppercase tracking-wider text-gray-700 mb-4 flex items-center gap-2">
              <Package className="w-4 h-4 text-blue-600" />
              <span>Purchased Items ({items?.length || 0})</span>
            </h3>

            <div className="border border-gray-100 rounded-2xl overflow-hidden divide-y divide-gray-100">
              {items && items.map((item, idx) => (
                <div key={idx} className="p-4 flex items-center justify-between text-sm hover:bg-gray-50">
                  <div>
                    <p className="font-bold text-gray-900">{item.productName}</p>
                    <p className="text-xs text-gray-500">Quantity: {item.quantity} × ${Number(item.price).toFixed(2)}</p>
                  </div>
                  <span className="font-black text-gray-900">
                    ${(Number(item.price) * item.quantity).toFixed(2)}
                  </span>
                </div>
              ))}
            </div>
          </div>

          {/* Details & Total Summary */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-6 pt-4 border-t border-gray-100">
            <div>
              <h4 className="text-xs font-bold uppercase tracking-wider text-gray-400 mb-2 flex items-center gap-1.5">
                <Truck className="w-3.5 h-3.5" /> Shipping Address
              </h4>
              <p className="text-sm text-gray-700 bg-gray-50 p-3.5 rounded-xl border border-gray-100">
                {shippingAddress || 'Digital Fulfillment / Standard Address'}
              </p>
            </div>

            <div className="space-y-2 text-sm bg-gray-50 p-4 rounded-xl border border-gray-100 self-start">
              <div className="flex justify-between text-gray-600 text-xs">
                <span>Shipping Method</span>
                <span className="font-semibold text-emerald-600">Standard Delivery (FREE)</span>
              </div>
              <div className="flex justify-between text-gray-600 text-xs">
                <span>Payment</span>
                <span className="font-semibold text-gray-900">Simulated Card (Processed)</span>
              </div>
              <div className="border-t border-gray-200 pt-2 flex justify-between items-baseline font-bold text-gray-900">
                <span>Grand Total</span>
                <span className="text-xl font-black text-blue-600">
                  ${Number(totalAmount).toFixed(2)}
                </span>
              </div>
            </div>
          </div>

          {/* CTA Footer */}
          <div className="pt-4 flex flex-wrap items-center justify-between gap-4 border-t border-gray-100">
            <Link
              to="/orders"
              className="text-xs font-bold text-blue-600 hover:text-blue-700 hover:underline"
            >
              ← Back to Order History
            </Link>
            <Link
              to="/"
              className="px-5 py-2.5 bg-blue-600 hover:bg-blue-700 text-white font-bold text-xs uppercase tracking-wider rounded-xl shadow transition-all"
            >
              Continue Shopping
            </Link>
          </div>

        </div>
      </div>
    </div>
  );
}
