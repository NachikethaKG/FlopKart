import React from 'react';
import { Link } from 'react-router-dom';
import { ShoppingCart, Check, AlertTriangle, Eye, Package } from 'lucide-react';

export default function ProductCard({ product, onAddToCart = null }) {
  const { id, name, description, price, stockQuantity, category, imageUrl } = product;
  const isOutOfStock = stockQuantity <= 0;
  const isLowStock = stockQuantity > 0 && stockQuantity <= 5;

  return (
    <div className="group bg-white rounded-2xl border border-gray-100 shadow-sm hover:shadow-xl transition-all duration-300 flex flex-col overflow-hidden relative">
      {/* Category Badge & Stock Alert */}
      <div className="absolute top-3 left-3 right-3 flex items-center justify-between z-10 pointer-events-none">
        {category && (
          <span className="px-2.5 py-1 text-xs font-bold uppercase tracking-wider bg-white/90 backdrop-blur-md text-gray-700 rounded-full shadow-sm border border-gray-100">
            {category}
          </span>
        )}
        {isOutOfStock ? (
          <span className="px-2 py-0.5 text-xs font-bold bg-red-600 text-white rounded-full shadow">
            Out of Stock
          </span>
        ) : isLowStock ? (
          <span className="px-2 py-0.5 text-xs font-bold bg-amber-500 text-white rounded-full shadow flex items-center gap-1">
            <AlertTriangle className="w-3 h-3" /> Only {stockQuantity} left
          </span>
        ) : (
          <span className="px-2 py-0.5 text-xs font-medium bg-emerald-50 text-emerald-700 rounded-full border border-emerald-200">
            In Stock
          </span>
        )}
      </div>

      {/* Image Thumbnail */}
      <Link to={`/products/${id}`} className="block relative aspect-square bg-gray-50 overflow-hidden">
        {imageUrl ? (
          <img
            src={imageUrl}
            alt={name}
            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
            loading="lazy"
            onError={(e) => {
              e.currentTarget.style.display = 'none';
              e.currentTarget.parentElement.classList.add('flex', 'items-center', 'justify-center');
              e.currentTarget.parentElement.innerHTML = '<div class="text-gray-300 flex flex-col items-center"><svg class="w-12 h-12" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/></svg></div>';
            }}
          />
        ) : (
          <div className="w-full h-full flex flex-col items-center justify-center text-gray-300 bg-gradient-to-tr from-gray-100 to-gray-50">
            <Package className="w-12 h-12 stroke-[1.5]" />
          </div>
        )}
      </Link>

      {/* Product Details */}
      <div className="p-5 flex-1 flex flex-col justify-between">
        <div>
          <Link to={`/products/${id}`}>
            <h3 className="font-bold text-gray-900 text-base leading-snug line-clamp-1 group-hover:text-blue-600 transition-colors">
              {name}
            </h3>
          </Link>
          <p className="text-xs text-gray-500 mt-1.5 line-clamp-2 leading-relaxed">
            {description || 'High-performance product engineered for the modern digital ecosystem.'}
          </p>
        </div>

        <div className="pt-4 mt-4 border-t border-gray-100 flex items-center justify-between gap-2">
          <div>
            <span className="text-xs text-gray-400 block font-medium">Price</span>
            <span className="text-xl font-black text-gray-900">
              ${Number(price).toFixed(2)}
            </span>
          </div>

          <div className="flex items-center gap-2">
            <Link
              to={`/products/${id}`}
              className="p-2 text-gray-500 hover:text-blue-600 hover:bg-blue-50 rounded-xl transition-colors"
              title="View Details"
            >
              <Eye className="w-4 h-4" />
            </Link>

            <button
              onClick={() => onAddToCart && onAddToCart(product, 1)}
              disabled={isOutOfStock}
              className={`flex items-center gap-1.5 px-3.5 py-2 text-xs font-bold rounded-xl transition-all shadow-sm ${
                isOutOfStock
                  ? 'bg-gray-100 text-gray-400 cursor-not-allowed shadow-none'
                  : 'bg-blue-600 hover:bg-blue-700 text-white hover:shadow-md active:scale-95'
              }`}
            >
              <ShoppingCart className="w-3.5 h-3.5" />
              <span>Add</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
