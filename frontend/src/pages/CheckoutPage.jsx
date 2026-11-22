import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import axiosClient from '../api/axiosClient';
import { ShieldCheck, Truck, CreditCard, ArrowLeft, Loader2, AlertCircle, ShoppingBag, Sparkles } from 'lucide-react';

export default function CheckoutPage() {
  const { cart, items, totalPrice, totalItemCount, refreshCart } = useCart();
  const { user } = useAuth();
  const navigate = useNavigate();

  const [addressLine, setAddressLine] = useState('');
  const [city, setCity] = useState('');
  const [stateProv, setStateProv] = useState('');
  const [postalCode, setPostalCode] = useState('');
  const [country, setCountry] = useState('United States');

  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  if (items.length === 0) {
    return (
      <div className="flex-1 bg-gray-50/50 py-16 px-4 text-center flex flex-col items-center justify-center">
        <ShoppingBag className="w-16 h-16 text-gray-300 mb-4" />
        <h2 className="text-xl font-bold text-gray-900 mb-2">Your Cart is Empty</h2>
        <p className="text-sm text-gray-500 mb-6">Please add items to your cart before proceeding to checkout.</p>
        <Link
          to="/"
          className="px-5 py-2.5 bg-blue-600 text-white font-bold rounded-xl text-sm shadow hover:bg-blue-700"
        >
          Browse Products
        </Link>
      </div>
    );
  }

  const handleSubmitOrder = async (e) => {
    e.preventDefault();
    setError('');

    if (!addressLine.trim() || !city.trim() || !postalCode.trim()) {
      setError('Please provide a complete shipping address.');
      return;
    }

    const fullShippingAddress = `${addressLine.trim()}, ${city.trim()}, ${stateProv.trim()} ${postalCode.trim()}, ${country.trim()}`;

    setSubmitting(true);
    try {
      const payload = {
        shippingAddress: fullShippingAddress,
        userEmail: user?.email || '',
      };

      const res = await axiosClient.post('/orders', payload);
      // res is unwrapped ApiResponse<OrderDto>
      const order = res.data || res;
      const orderNumber = order.orderNumber;

      if (!orderNumber) {
        throw new Error('Order placement succeeded but no order number was returned.');
      }

      // Refresh cart to reflect automatic clearance in Redis post-order
      await refreshCart();

      // Navigate to order tracking view
      navigate(`/orders/${orderNumber}`, { replace: true });
    } catch (err) {
      console.error('Order creation failed', err);
      const msg = err.response?.data?.message || err.message || 'Failed to place order. Please try again.';
      setError(msg);
      setSubmitting(false);
    }
  };

  return (
    <div className="flex-1 bg-gray-50/50 py-10">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        
        {/* Header */}
        <div className="mb-8">
          <Link
            to="/cart"
            className="inline-flex items-center gap-1.5 text-xs font-bold text-gray-500 hover:text-blue-600 mb-3 transition-colors"
          >
            <ArrowLeft className="w-3.5 h-3.5" />
            <span>Return to Cart</span>
          </Link>
          <h1 className="text-2xl sm:text-3xl font-black text-gray-900 tracking-tight">
            Checkout & Finalize Order
          </h1>
          <p className="text-xs text-gray-500 mt-1">
            Asynchronous Kafka Saga orchestration ensures real-time inventory reservation and payment processing
          </p>
        </div>

        {error && (
          <div className="mb-6 p-4 bg-red-50 border border-red-200 text-red-700 text-sm rounded-2xl flex items-center gap-3">
            <AlertCircle className="w-5 h-5 flex-shrink-0 text-red-500" />
            <span>{error}</span>
          </div>
        )}

        {/* Checkout Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 items-start">
          
          {/* Shipping Form (Left 2 cols) */}
          <form onSubmit={handleSubmitOrder} className="lg:col-span-2 space-y-6">
            
            {/* Customer Details */}
            <div className="bg-white p-6 sm:p-8 rounded-3xl border border-gray-100 shadow-sm space-y-4">
              <h2 className="text-base font-bold text-gray-900 flex items-center gap-2">
                <Truck className="w-5 h-5 text-blue-600" />
                <span>Shipping Address</span>
              </h2>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="sm:col-span-2">
                  <label className="block text-xs font-bold uppercase tracking-wider text-gray-600 mb-1">
                    Street Address
                  </label>
                  <input
                    type="text"
                    required
                    placeholder="123 Cloud Way, Suite 400"
                    value={addressLine}
                    onChange={(e) => setAddressLine(e.target.value)}
                    className="w-full px-4 py-2.5 text-sm bg-gray-50 border border-gray-300 rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold uppercase tracking-wider text-gray-600 mb-1">
                    City
                  </label>
                  <input
                    type="text"
                    required
                    placeholder="Seattle"
                    value={city}
                    onChange={(e) => setCity(e.target.value)}
                    className="w-full px-4 py-2.5 text-sm bg-gray-50 border border-gray-300 rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold uppercase tracking-wider text-gray-600 mb-1">
                    State / Province
                  </label>
                  <input
                    type="text"
                    required
                    placeholder="WA"
                    value={stateProv}
                    onChange={(e) => setStateProv(e.target.value)}
                    className="w-full px-4 py-2.5 text-sm bg-gray-50 border border-gray-300 rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold uppercase tracking-wider text-gray-600 mb-1">
                    Postal / ZIP Code
                  </label>
                  <input
                    type="text"
                    required
                    placeholder="98101"
                    value={postalCode}
                    onChange={(e) => setPostalCode(e.target.value)}
                    className="w-full px-4 py-2.5 text-sm bg-gray-50 border border-gray-300 rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold uppercase tracking-wider text-gray-600 mb-1">
                    Country
                  </label>
                  <input
                    type="text"
                    required
                    value={country}
                    onChange={(e) => setCountry(e.target.value)}
                    className="w-full px-4 py-2.5 text-sm bg-gray-50 border border-gray-300 rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
              </div>
            </div>

            {/* Payment Method Details (Simulated Gateway) */}
            <div className="bg-white p-6 sm:p-8 rounded-3xl border border-gray-100 shadow-sm space-y-4">
              <h2 className="text-base font-bold text-gray-900 flex items-center gap-2">
                <CreditCard className="w-5 h-5 text-indigo-600" />
                <span>Payment Simulation (Saga Event Orchestration)</span>
              </h2>
              
              <div className="p-4 bg-indigo-50/60 border border-indigo-100 rounded-2xl flex items-start gap-3">
                <Sparkles className="w-5 h-5 text-indigo-600 flex-shrink-0 mt-0.5" />
                <div className="text-xs text-indigo-900 leading-relaxed">
                  <p className="font-bold">Automated Asynchronous Payment</p>
                  <p className="text-indigo-700 mt-0.5">
                    Submitting this order fires an <code>OrderPlacedEvent</code> onto Kafka. The <code>payment-service</code> automatically verifies authorization, charges the order, and publishes a <code>PaymentProcessedEvent</code> to transition your order to <code>CONFIRMED</code>.
                  </p>
                </div>
              </div>
            </div>

            {/* Submit Button */}
            <button
              type="submit"
              disabled={submitting}
              className="w-full py-4 px-6 bg-blue-600 hover:bg-blue-700 text-white font-extrabold text-base rounded-2xl shadow-lg hover:shadow-xl active:scale-[0.99] disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 transition-all"
            >
              {submitting ? (
                <>
                  <Loader2 className="w-5 h-5 animate-spin" />
                  <span>Submitting to Kafka Saga...</span>
                </>
              ) : (
                <span>Place Order (${Number(totalPrice).toFixed(2)})</span>
              )}
            </button>
          </form>

          {/* Cart Breakdown Summary (Right col) */}
          <div className="bg-white p-6 sm:p-8 rounded-3xl border border-gray-100 shadow-sm space-y-6">
            <h3 className="text-base font-bold text-gray-900 border-b border-gray-100 pb-3">
              Order Review ({totalItemCount} {totalItemCount === 1 ? 'item' : 'items'})
            </h3>

            <div className="max-h-80 overflow-y-auto space-y-3 pr-1">
              {items.map((item) => (
                <div key={item.productId} className="flex items-center justify-between gap-3 text-xs">
                  <div className="flex-1 truncate">
                    <p className="font-bold text-gray-900 truncate">{item.productName}</p>
                    <p className="text-gray-500">Qty: {item.quantity} × ${Number(item.price).toFixed(2)}</p>
                  </div>
                  <span className="font-black text-gray-900">
                    ${(Number(item.price) * item.quantity).toFixed(2)}
                  </span>
                </div>
              ))}
            </div>

            <div className="border-t border-gray-100 pt-4 space-y-2 text-xs">
              <div className="flex justify-between text-gray-600">
                <span>Subtotal</span>
                <span className="font-semibold text-gray-900">${Number(totalPrice).toFixed(2)}</span>
              </div>
              <div className="flex justify-between text-gray-600">
                <span>Delivery</span>
                <span className="font-semibold text-emerald-600">FREE</span>
              </div>
              <div className="flex justify-between text-gray-600">
                <span>Taxes</span>
                <span className="font-semibold text-gray-900">$0.00</span>
              </div>
              <div className="border-t border-gray-100 pt-3 flex justify-between text-sm font-bold text-gray-900">
                <span>Total Due</span>
                <span className="text-xl font-black text-blue-600">${Number(totalPrice).toFixed(2)}</span>
              </div>
            </div>

            <div className="pt-2 border-t border-gray-100 flex items-center gap-2 text-[11px] text-gray-400">
              <ShieldCheck className="w-4 h-4 text-emerald-500 flex-shrink-0" />
              <span>Full end-to-end transactional integrity</span>
            </div>
          </div>

        </div>
      </div>
    </div>
  );
}
