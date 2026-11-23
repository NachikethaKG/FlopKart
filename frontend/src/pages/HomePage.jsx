import React, { useState, useEffect } from 'react';
import { useSearchParams, useLocation } from 'react-router-dom';
import axiosClient from '../api/axiosClient';
import ProductCard from '../components/ProductCard';
import { Search, SlidersHorizontal, ChevronLeft, ChevronRight, PackageX, Sparkles, ShoppingBag, Loader2 } from 'lucide-react';

const CATEGORIES = ['All', 'Electronics', 'Fashion', 'Books', 'Home & Kitchen'];

export default function HomePage({ onAddToCart = null }) {
  const [searchParams, setSearchParams] = useSearchParams();
  const searchParam = searchParams.get('search') || '';
  const location = useLocation();

  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  const [activeCategory, setActiveCategory] = useState('All');
  const [searchTerm, setSearchTerm] = useState(searchParam);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(8);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  const clearFilters = () => {
    setActiveCategory('All');
    setSearchTerm('');
    setSearchParams({});
    setPage(0);
  };

  // Reset filters if navigating with resetFilters state
  useEffect(() => {
    if (location.state?.resetFilters) {
      clearFilters();
    }
  }, [location.state]);

  // Global event listener for Catalog navigation/reset
  useEffect(() => {
    const handleReset = () => {
      clearFilters();
    };
    window.addEventListener('flopkart:reset-catalog', handleReset);
    return () => window.removeEventListener('flopkart:reset-catalog', handleReset);
  }, []);

  // Sync state if search query param changes
  useEffect(() => {
    setSearchTerm(searchParam);
    setPage(0);
  }, [searchParam]);

  const fetchProducts = async () => {
    setLoading(true);
    setError('');
    try {
      const params = {
        page,
        size: pageSize,
      };
      if (activeCategory !== 'All') {
        params.category = activeCategory;
      }

      const res = await axiosClient.get('/products', { params });
      // res is unwrapped ApiResponse<PageResponse<ProductDto>>
      const pageData = res.data || res;
      
      let list = pageData.content || [];
      // Client-side search filtering if backend filter isn't querying full text
      if (searchTerm.trim()) {
        const query = searchTerm.toLowerCase();
        list = list.filter(
          (p) =>
            p.name.toLowerCase().includes(query) ||
            (p.description && p.description.toLowerCase().includes(query)) ||
            (p.category && p.category.toLowerCase().includes(query))
        );
      }

      setProducts(list);
      setTotalPages(pageData.totalPages || 1);
      setTotalElements(pageData.totalElements || list.length);
    } catch (err) {
      console.error('Failed to fetch products', err);
      setError('Unable to load products. Ensure the backend microservices are running.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, [page, pageSize, activeCategory, searchTerm]);

  const handleCategorySelect = (cat) => {
    setActiveCategory(cat);
    setPage(0);
  };

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    setSearchParams(searchTerm.trim() ? { search: searchTerm.trim() } : {});
    setPage(0);
  };

  return (
    <div className="flex-1 flex flex-col bg-gray-50/50">
      
      {/* Hero Banner */}
      <section className="bg-gradient-to-r from-blue-700 via-indigo-700 to-slate-900 text-white py-14 px-4 sm:px-6 lg:px-8 relative overflow-hidden">
        <div className="absolute inset-0 opacity-10 bg-[radial-gradient(#fff_1px,transparent_1px)] [background-size:16px_16px]"></div>
        <div className="max-w-7xl mx-auto relative z-10 flex flex-col md:flex-row items-center justify-between gap-8">
          <div className="max-w-2xl space-y-4 text-center md:text-left">
            <div className="inline-flex items-center gap-2 px-3 py-1 bg-white/10 backdrop-blur-md rounded-full text-xs font-semibold text-blue-200 border border-white/20">
              <Sparkles className="w-3.5 h-3.5 text-blue-300" />
              <span>Cloud Microservices Showcase</span>
            </div>
            <h1 className="text-3xl sm:text-5xl font-black tracking-tight leading-tight">
              FlopKart SuperStore
            </h1>
            <p className="text-sm sm:text-base text-blue-100 max-w-xl">
              Browse top electronics, apparel, and essentials powered by high-throughput PostgreSQL and Redis caches.
            </p>
            <div className="pt-2 flex items-center justify-center md:justify-start gap-3">
              <button
                type="button"
                onClick={clearFilters}
                className="px-5 py-2.5 bg-white text-blue-700 hover:bg-blue-50 font-bold text-sm rounded-xl shadow-lg transition-all flex items-center gap-2 group"
              >
                <ShoppingBag className="w-4 h-4 group-hover:scale-110 transition-transform" />
                <span>Explore Catalog</span>
              </button>
            </div>
          </div>

          {/* Quick Search Card */}
          <div className="w-full md:w-96 bg-white/10 backdrop-blur-md p-4 rounded-2xl border border-white/20 shadow-xl">
            <form onSubmit={handleSearchSubmit} className="space-y-3">
              <label className="block text-xs font-bold uppercase tracking-wider text-blue-200">
                Find Products
              </label>
              <div className="relative">
                <input
                  type="text"
                  placeholder="e.g. Headphones, Smartphone..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="w-full pl-9 pr-4 py-2.5 text-sm bg-white text-gray-900 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-400 placeholder-gray-400"
                />
                <Search className="w-4 h-4 text-gray-400 absolute left-3 top-1/2 -translate-y-1/2" />
              </div>
              <button
                type="submit"
                className="w-full py-2 bg-blue-600 hover:bg-blue-500 font-bold text-xs uppercase tracking-wider rounded-xl transition-colors shadow"
              >
                Search Store
              </button>
            </form>
          </div>
        </div>
      </section>

      {/* Main Catalog View */}
      <section className="max-w-7xl mx-auto w-full px-4 sm:px-6 lg:px-8 py-10 flex-1 flex flex-col">
        
        {/* Category Filter Pills & Search Status */}
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pb-6 border-b border-gray-200">
          <div className="flex items-center gap-2 overflow-x-auto w-full sm:w-auto pb-2 sm:pb-0">
            <span className="text-xs font-bold uppercase tracking-wider text-gray-400 mr-2 flex items-center gap-1">
              <SlidersHorizontal className="w-3.5 h-3.5" /> Filters:
            </span>
            {CATEGORIES.map((cat) => (
              <button
                key={cat}
                onClick={() => handleCategorySelect(cat)}
                className={`px-4 py-1.5 text-xs font-bold rounded-full transition-all whitespace-nowrap ${
                  activeCategory === cat
                    ? 'bg-blue-600 text-white shadow-sm'
                    : 'bg-white text-gray-700 border border-gray-200 hover:bg-gray-100'
                }`}
              >
                {cat}
              </button>
            ))}
          </div>

          <div className="text-xs text-gray-500 font-medium self-end sm:self-auto">
            Showing <span className="font-bold text-gray-900">{products.length}</span> of {totalElements} products
          </div>
        </div>

        {/* Error State */}
        {error && (
          <div className="my-6 p-4 bg-amber-50 border border-amber-200 rounded-2xl text-amber-800 text-sm flex items-center justify-between">
            <span>{error}</span>
            <button
              onClick={fetchProducts}
              className="px-3 py-1 bg-amber-200 hover:bg-amber-300 rounded-lg font-bold text-xs"
            >
              Retry
            </button>
          </div>
        )}

        {/* Loading Skeletons */}
        {loading ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6 py-8">
            {[...Array(8)].map((_, i) => (
              <div
                key={i}
                className="bg-white rounded-2xl p-4 border border-gray-100 shadow-sm animate-pulse space-y-4"
              >
                <div className="aspect-square bg-gray-200 rounded-xl"></div>
                <div className="h-4 bg-gray-200 rounded w-3/4"></div>
                <div className="h-3 bg-gray-200 rounded w-1/2"></div>
                <div className="flex justify-between items-center pt-2">
                  <div className="h-6 bg-gray-200 rounded w-1/3"></div>
                  <div className="h-8 bg-gray-200 rounded w-1/4"></div>
                </div>
              </div>
            ))}
          </div>
        ) : products.length === 0 ? (
          /* Empty State */
          <div className="flex-1 flex flex-col items-center justify-center py-20 text-center">
            <div className="w-16 h-16 bg-gray-100 text-gray-400 rounded-2xl flex items-center justify-center mb-4">
              <PackageX className="w-8 h-8" />
            </div>
            <h3 className="text-lg font-bold text-gray-900 mb-1">No products found</h3>
            <p className="text-sm text-gray-500 max-w-sm mb-6">
              No inventory matching your selected category "{activeCategory}" or search term "{searchTerm}".
            </p>
            <button
              onClick={clearFilters}
              className="px-5 py-2.5 bg-blue-600 hover:bg-blue-700 text-white font-bold text-sm rounded-xl shadow transition-all"
            >
              Reset Filters
            </button>
          </div>
        ) : (
          /* Product Grid */
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6 py-8">
            {products.map((product) => (
              <ProductCard
                key={product.id}
                product={product}
                onAddToCart={onAddToCart}
              />
            ))}
          </div>
        )}

        {/* Pagination Controls */}
        {totalPages > 1 && (
          <div className="mt-auto pt-8 border-t border-gray-200 flex items-center justify-between">
            <button
              onClick={() => setPage((prev) => Math.max(0, prev - 1))}
              disabled={page === 0}
              className="flex items-center gap-1 px-4 py-2 text-xs font-bold text-gray-700 bg-white border border-gray-300 rounded-xl hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed shadow-sm"
            >
              <ChevronLeft className="w-4 h-4" />
              <span>Previous</span>
            </button>

            <span className="text-xs font-semibold text-gray-600">
              Page <span className="text-gray-900">{page + 1}</span> of{' '}
              <span className="text-gray-900">{totalPages}</span>
            </span>

            <button
              onClick={() => setPage((prev) => Math.min(totalPages - 1, prev + 1))}
              disabled={page >= totalPages - 1}
              className="flex items-center gap-1 px-4 py-2 text-xs font-bold text-gray-700 bg-white border border-gray-300 rounded-xl hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed shadow-sm"
            >
              <span>Next</span>
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        )}
      </section>
    </div>
  );
}
