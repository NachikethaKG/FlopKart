import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import axiosClient from '../api/axiosClient';
import { ShoppingCart, ArrowLeft, Check, AlertTriangle, ShieldCheck, Truck, RotateCcw, Package, Loader2 } from 'lucide-react';

export default function ProductDetailPage({ onAddToCart = null }) {
  const { id } = useParams();
  const navigate = useNavigate();

  const [product, setProduct] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [addedNotice, setAddedNotice] = useState(false);

  useEffect(() => {
    const fetchProduct = async () => {
      setLoading(true);
      setError('');
      try {
        const res = await axiosClient.get(`/products/${id}`);
        const data = res.data || res;
        setProduct(data);
      } catch (err) {
        console.error('Failed to load product', err);
        setError('Product not found or currently unavailable.');
      } finally {
        setLoading(false);
      }
    };

    fetchProduct();
  }, [id]);

  const handleAddToCart = () => {
    if (onAddToCart && product) {
      onAddToCart(product, quantity);
      setAddedNotice(true);
      setTimeout(() => setAddedNotice(false), 2500);
    }
  };

  if (loading) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16 flex-1 flex flex-col items-center justify-center">
        <Loader2 className="w-10 h-10 text-blue-600 animate-spin mb-4" />
        <p className="text-sm font-medium text-gray-500">Loading product details...</p>
      </div>
    );
  }

  if (error || !product) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16 flex-1 flex flex-col items-center justify-center text-center">
        <div className="w-16 h-16 bg-red-50 text-red-500 rounded-2xl flex items-center justify-center mb-4">
          <Package className="w-8 h-8" />
        </div>
        <h2 className="text-xl font-bold text-gray-900 mb-2">Product Not Found</h2>
        <p className="text-sm text-gray-500 max-w-md mb-6">{error || 'The requested product does not exist.'}</p>
        <Link
          to="/"
          className="inline-flex items-center gap-2 px-5 py-2.5 bg-blue-600 text-white font-bold text-sm rounded-xl hover:bg-blue-700 shadow"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Back to Catalog</span>
        </Link>
      </div>
    );
  }

  const { name, description, price, stockQuantity, category, imageUrl } = product;
  const isOutOfStock = stockQuantity <= 0;
  const isLowStock = stockQuantity > 0 && stockQuantity <= 5;

  return (
    <div className="flex-1 bg-gray-50/50 py-10">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        
        {/* Breadcrumb Navigation */}
        <div className="mb-6 flex items-center gap-2 text-xs font-semibold text-gray-500">
          <Link to="/" className="hover:text-blue-600 transition-colors">Catalog</Link>
          <span>/</span>
          {category && (
            <>
              <span className="hover:text-blue-600 cursor-pointer">{category}</span>
              <span>/</span>
            </>
          )}
          <span className="text-gray-900 truncate max-w-xs">{name}</span>
        </div>

        {/* Product Container */}
        <div className="bg-white rounded-3xl border border-gray-100 shadow-sm p-6 sm:p-10 grid grid-cols-1 md:grid-cols-2 gap-10">
          
          {/* Product Media Display */}
          <div className="relative aspect-square rounded-2xl bg-gray-50 overflow-hidden border border-gray-100 flex items-center justify-center">
            {imageUrl ? (
              <img
                src={imageUrl}
                alt={name}
                className="w-full h-full object-cover"
                onError={(e) => {
                  e.currentTarget.style.display = 'none';
                  e.currentTarget.parentElement.classList.add('flex', 'items-center', 'justify-center');
                  e.currentTarget.parentElement.innerHTML = '<div class="text-gray-300 flex flex-col items-center"><svg class="w-16 h-16" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/></svg></div>';
                }}
              />
            ) : (
              <Package className="w-20 h-20 text-gray-300 stroke-[1.5]" />
            )}

            {category && (
              <span className="absolute top-4 left-4 px-3 py-1 bg-white/90 backdrop-blur-md text-xs font-bold uppercase tracking-wider text-gray-700 rounded-full shadow-sm border border-gray-100">
                {category}
              </span>
            )}
          </div>

          {/* Product Information & Action Panel */}
          <div className="flex flex-col justify-between space-y-6">
            <div>
              <h1 className="text-2xl sm:text-4xl font-black text-gray-900 tracking-tight leading-tight">
                {name}
              </h1>

              {/* Price & Stock Badge */}
              <div className="flex items-center gap-4 mt-4">
                <span className="text-3xl font-black text-gray-900">
                  ${Number(price).toFixed(2)}
                </span>

                {isOutOfStock ? (
                  <span className="px-3 py-1 bg-red-100 text-red-700 text-xs font-bold rounded-full">
                    Out of Stock
                  </span>
                ) : isLowStock ? (
                  <span className="px-3 py-1 bg-amber-100 text-amber-800 text-xs font-bold rounded-full flex items-center gap-1">
                    <AlertTriangle className="w-3.5 h-3.5" /> Only {stockQuantity} units left!
                  </span>
                ) : (
                  <span className="px-3 py-1 bg-emerald-100 text-emerald-800 text-xs font-bold rounded-full flex items-center gap-1">
                    <Check className="w-3.5 h-3.5" /> In Stock ({stockQuantity} available)
                  </span>
                )}
              </div>

              {/* Description */}
              <div className="mt-6 border-t border-gray-100 pt-6">
                <h4 className="text-xs font-bold uppercase tracking-wider text-gray-400 mb-2">Description</h4>
                <p className="text-sm text-gray-600 leading-relaxed">
                  {description || 'Premium-grade merchandise fully integrated into the FlopKart distributed logistics platform.'}
                </p>
              </div>
            </div>

            {/* Quantity Selector & Add to Cart Action */}
            <div className="border-t border-gray-100 pt-6 space-y-4">
              <div className="flex items-center gap-4">
                <span className="text-xs font-bold uppercase tracking-wider text-gray-700">Quantity</span>
                <div className="flex items-center border border-gray-200 rounded-xl bg-gray-50">
                  <button
                    onClick={() => setQuantity((q) => Math.max(1, q - 1))}
                    disabled={quantity <= 1 || isOutOfStock}
                    className="px-3 py-1.5 text-gray-600 hover:text-black font-bold disabled:opacity-40"
                  >
                    -
                  </button>
                  <span className="px-4 py-1.5 text-sm font-bold text-gray-900 bg-white">
                    {quantity}
                  </span>
                  <button
                    onClick={() => setQuantity((q) => Math.min(stockQuantity, q + 1))}
                    disabled={quantity >= stockQuantity || isOutOfStock}
                    className="px-3 py-1.5 text-gray-600 hover:text-black font-bold disabled:opacity-40"
                  >
                    +
                  </button>
                </div>
              </div>

              <div className="flex flex-col sm:flex-row items-center gap-3">
                <button
                  onClick={handleAddToCart}
                  disabled={isOutOfStock}
                  className={`flex-1 w-full sm:w-auto py-3.5 px-6 font-bold rounded-2xl flex items-center justify-center gap-2 transition-all shadow-md ${
                    isOutOfStock
                      ? 'bg-gray-100 text-gray-400 cursor-not-allowed shadow-none'
                      : 'bg-blue-600 hover:bg-blue-700 text-white hover:shadow-lg active:scale-[0.98]'
                  }`}
                >
                  <ShoppingCart className="w-5 h-5" />
                  <span>{isOutOfStock ? 'Sold Out' : `Add ${quantity} to Cart`}</span>
                </button>

                <Link
                  to="/"
                  className="px-5 py-3.5 border border-gray-200 text-gray-700 hover:bg-gray-50 font-bold rounded-2xl text-center text-sm transition-colors"
                >
                  Continue Shopping
                </Link>
              </div>

              {addedNotice && (
                <div className="p-3 bg-emerald-50 border border-emerald-200 text-emerald-800 rounded-xl text-xs font-bold flex items-center gap-2 animate-in fade-in">
                  <Check className="w-4 h-4 text-emerald-600" />
                  <span>Added {quantity} unit(s) of {name} to your cart!</span>
                </div>
              )}
            </div>

            {/* Trust Badges */}
            <div className="grid grid-cols-3 gap-2 border-t border-gray-100 pt-6 text-center">
              <div className="flex flex-col items-center">
                <Truck className="w-5 h-5 text-gray-400 mb-1" />
                <span className="text-[11px] font-bold text-gray-700">Fast Shipping</span>
                <span className="text-[10px] text-gray-400">Guaranteed Dispatch</span>
              </div>
              <div className="flex flex-col items-center">
                <ShieldCheck className="w-5 h-5 text-gray-400 mb-1" />
                <span className="text-[11px] font-bold text-gray-700">Buyer Protection</span>
                <span className="text-[10px] text-gray-400">Secure Payments</span>
              </div>
              <div className="flex flex-col items-center">
                <RotateCcw className="w-5 h-5 text-gray-400 mb-1" />
                <span className="text-[11px] font-bold text-gray-700">Easy Returns</span>
                <span className="text-[10px] text-gray-400">30-Day Policy</span>
              </div>
            </div>

          </div>
        </div>
      </div>
    </div>
  );
}
