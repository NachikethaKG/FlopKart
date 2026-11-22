import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { ShoppingBag, Trash2, ArrowRight, ArrowLeft, ShieldCheck, Zap, AlertCircle } from 'lucide-react';

export default function CartPage() {
  const { cart, items, totalItemCount, totalPrice, updateQuantity, removeItem, clearCart, loading } = useCart();
  const navigate = useNavigate();

  if (items.length === 0) {
    return (
      <div className="flex-1 bg-gray-50/50 py-16 px-4 sm:px-6 lg:px-8 flex flex-col items-center justify-center text-center">
        <div className="w-20 h-20 bg-blue-50 text-blue-600 rounded-3xl flex items-center justify-center mb-6 shadow-sm">
          <ShoppingBag className="w-10 h-10 stroke-[1.5]" />
        </div>
        <h2 className="text-2xl sm:text-3xl font-black text-gray-900 tracking-tight mb-2">
          Your Shopping Cart is Empty
        </h2>
        <p className="text-sm text-gray-500 max-w-md mb-8 leading-relaxed">
          Looks like you haven't added any products to your cart yet. Explore our catalog of cutting-edge electronics and apparel!
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
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        
        {/* Page Title */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8">
          <div>
            <h1 className="text-2xl sm:text-3xl font-black text-gray-900 tracking-tight">
              Shopping Cart
            </h1>
            <p className="text-xs text-gray-500 mt-1">
              {totalItemCount} {totalItemCount === 1 ? 'item' : 'items'} synchronized with high-performance Redis cache
            </p>
          </div>

          <button
            onClick={clearCart}
            disabled={loading}
            className="self-start sm:self-auto text-xs font-bold text-red-600 hover:text-red-700 hover:bg-red-50 px-3 py-1.5 rounded-lg border border-red-200 transition-colors"
          >
            Clear Entire Cart
          </button>
        </div>

        {/* Content Layout */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 items-start">
          
          {/* Cart Item Rows */}
          <div className="lg:col-span-2 space-y-4">
            {items.map((item) => {
              const { productId, productName, price, quantity, imageUrl, itemTotal } = item;
              const lineTotal = itemTotal || Number(price) * quantity;

              return (
                <div
                  key={productId}
                  className="bg-white p-5 rounded-2xl border border-gray-100 shadow-sm flex flex-col sm:flex-row items-center justify-between gap-4 hover:border-gray-200 transition-colors"
                >
                  {/* Thumbnail & Title */}
                  <div className="flex items-center gap-4 w-full sm:w-auto">
                    <div className="w-20 h-20 bg-gray-50 rounded-xl overflow-hidden border border-gray-100 flex-shrink-0 flex items-center justify-center">
                      {imageUrl ? (
                        <img
                          src={imageUrl}
                          alt={productName}
                          className="w-full h-full object-cover"
                          onError={(e) => {
                            e.currentTarget.style.display = 'none';
                            e.currentTarget.parentElement.innerHTML = '<div class="text-gray-300 font-bold text-xs">IMG</div>';
                          }}
                        />
                      ) : (
                        <ShoppingBag className="w-8 h-8 text-gray-300" />
                      )}
                    </div>

                    <div className="flex-1 min-w-0">
                      <Link
                        to={`/products/${productId}`}
                        className="text-base font-bold text-gray-900 hover:text-blue-600 transition-colors truncate block"
                      >
                        {productName}
                      </Link>
                      <span className="text-xs text-gray-500 block mt-0.5">
                        Unit Price: <span className="font-semibold text-gray-700">${Number(price).toFixed(2)}</span>
                      </span>
                    </div>
                  </div>

                  {/* Quantity Stepper & Line Price */}
                  <div className="flex items-center justify-between sm:justify-end gap-6 w-full sm:w-auto pt-3 sm:pt-0 border-t sm:border-t-0 border-gray-100">
                    
                    {/* Stepper */}
                    <div className="flex items-center border border-gray-200 rounded-xl bg-gray-50 overflow-hidden">
                      <button
                        onClick={() => updateQuantity(productId, quantity - 1)}
                        disabled={loading}
                        className="px-3 py-1.5 text-gray-600 hover:text-black hover:bg-gray-200 text-xs font-bold transition-colors"
                      >
                        -
                      </button>
                      <span className="px-3.5 py-1.5 text-xs font-bold text-gray-900 bg-white">
                        {quantity}
                      </span>
                      <button
                        onClick={() => updateQuantity(productId, quantity + 1)}
                        disabled={loading}
                        className="px-3 py-1.5 text-gray-600 hover:text-black hover:bg-gray-200 text-xs font-bold transition-colors"
                      >
                        +
                      </button>
                    </div>

                    {/* Subtotal */}
                    <div className="text-right min-w-[80px]">
                      <span className="text-sm font-black text-gray-900 block">
                        ${Number(lineTotal).toFixed(2)}
                      </span>
                    </div>

                    {/* Delete Action */}
                    <button
                      onClick={() => removeItem(productId)}
                      disabled={loading}
                      className="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-xl transition-colors"
                      title="Remove Item"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              );
            })}

            <div className="pt-2">
              <Link
                to="/"
                className="inline-flex items-center gap-1.5 text-xs font-bold text-blue-600 hover:text-blue-700 hover:underline"
              >
                <ArrowLeft className="w-3.5 h-3.5" />
                <span>Continue browsing products</span>
              </Link>
            </div>
          </div>

          {/* Order Summary Card */}
          <div className="bg-white p-6 sm:p-8 rounded-3xl border border-gray-100 shadow-sm space-y-6">
            <h3 className="text-lg font-black text-gray-900 tracking-tight">
              Order Summary
            </h3>

            <div className="space-y-3 text-sm">
              <div className="flex justify-between text-gray-600">
                <span>Items Subtotal ({totalItemCount})</span>
                <span className="font-semibold text-gray-900">${Number(totalPrice).toFixed(2)}</span>
              </div>

              <div className="flex justify-between text-gray-600">
                <span>Estimated Shipping</span>
                <span className="font-semibold text-emerald-600">FREE</span>
              </div>

              <div className="flex justify-between text-gray-600">
                <span>Estimated Sales Tax</span>
                <span className="font-semibold text-gray-900">$0.00</span>
              </div>

              <div className="border-t border-gray-100 pt-4 flex justify-between items-baseline">
                <span className="text-base font-bold text-gray-900">Total</span>
                <span className="text-2xl font-black text-blue-600">
                  ${Number(totalPrice).toFixed(2)}
                </span>
              </div>
            </div>

            <button
              onClick={() => navigate('/checkout')}
              disabled={loading || items.length === 0}
              className="w-full py-3.5 px-4 bg-blue-600 hover:bg-blue-700 text-white font-bold rounded-2xl shadow-md hover:shadow-lg active:scale-[0.98] transition-all flex items-center justify-center gap-2"
            >
              <span>Proceed to Checkout</span>
              <ArrowRight className="w-4 h-4" />
            </button>

            <div className="space-y-2 pt-2 border-t border-gray-100 text-xs text-gray-400">
              <div className="flex items-center gap-2">
                <Zap className="w-4 h-4 text-blue-500" />
                <span>Instant Redis cart synchronization</span>
              </div>
              <div className="flex items-center gap-2">
                <ShieldCheck className="w-4 h-4 text-emerald-500" />
                <span>Encrypted checkout via Spring Cloud Gateway</span>
              </div>
            </div>
          </div>

        </div>
      </div>
    </div>
  );
}
