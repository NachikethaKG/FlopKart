import React, { useState, useEffect } from 'react';
import { useCart } from '../context/CartContext';
import axiosClient from '../api/axiosClient';
import {
  Package,
  Plus,
  Trash2,
  DollarSign,
  Boxes,
  AlertTriangle,
  CheckCircle,
  Search,
  Filter,
  RefreshCw,
  X,
  Sparkles,
  ArrowUpDown,
  ChevronDown,
} from 'lucide-react';

const CATEGORIES = ['Electronics', 'Fashion', 'Books', 'Home & Kitchen'];

export default function ManagerDashboardPage() {
  const { showToast } = useCart();

  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('All');
  const [stockFilter, setStockFilter] = useState('All'); // 'All' | 'Low' | 'Out'

  // Modals & form state
  const [showAddModal, setShowAddModal] = useState(false);
  const [submittingProduct, setSubmittingProduct] = useState(false);
  const [newProduct, setNewProduct] = useState({
    name: '',
    description: '',
    category: 'Electronics',
    price: '',
    stockQuantity: '',
    imageUrl: '',
  });

  // Action states for inline edits
  const [activeActionId, setActiveActionId] = useState(null);
  const [customStockInputs, setCustomStockInputs] = useState({});
  const [customPriceInputs, setCustomPriceInputs] = useState({});

  const fetchProducts = async () => {
    setLoading(true);
    try {
      // Fetch up to 100 products for manager view
      const res = await axiosClient.get('/products', { params: { page: 0, size: 100 } });
      const pageData = res.data || res;
      setProducts(pageData.content || []);
    } catch (err) {
      console.error('Failed to load products for manager', err);
      if (showToast) showToast('Failed to load catalog inventory.', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, []);

  // Stock update handlers
  const handleStockDelta = async (productId, delta) => {
    setActiveActionId(productId);
    try {
      const res = await axiosClient.patch(`/products/${productId}/stock`, { delta });
      const updated = res.data || res;
      setProducts((prev) => prev.map((p) => (p.id === productId ? { ...p, stockQuantity: updated.stockQuantity } : p)));
      if (showToast) showToast(`Stock updated (${delta > 0 ? '+' : ''}${delta}).`);
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to adjust stock.';
      if (showToast) showToast(msg, 'error');
    } finally {
      setActiveActionId(null);
    }
  };

  const handleStockSet = async (productId) => {
    const qtyVal = customStockInputs[productId];
    if (qtyVal === undefined || qtyVal === '') return;
    const quantity = parseInt(qtyVal, 10);
    if (isNaN(quantity) || quantity < 0) {
      if (showToast) showToast('Stock quantity cannot be negative.', 'error');
      return;
    }

    setActiveActionId(productId);
    try {
      const res = await axiosClient.patch(`/products/${productId}/stock`, { quantity });
      const updated = res.data || res;
      setProducts((prev) => prev.map((p) => (p.id === productId ? { ...p, stockQuantity: updated.stockQuantity } : p)));
      setCustomStockInputs((prev) => ({ ...prev, [productId]: '' }));
      if (showToast) showToast(`Stock quantity set to ${quantity}.`);
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to update stock quantity.';
      if (showToast) showToast(msg, 'error');
    } finally {
      setActiveActionId(null);
    }
  };

  // Price update handler
  const handlePriceSet = async (productId) => {
    const priceVal = customPriceInputs[productId];
    if (priceVal === undefined || priceVal === '') return;
    const price = parseFloat(priceVal);
    if (isNaN(price) || price <= 0) {
      if (showToast) showToast('Price must be greater than zero.', 'error');
      return;
    }

    setActiveActionId(productId);
    try {
      const res = await axiosClient.patch(`/products/${productId}/price`, { price });
      const updated = res.data || res;
      setProducts((prev) => prev.map((p) => (p.id === productId ? { ...p, price: updated.price } : p)));
      setCustomPriceInputs((prev) => ({ ...prev, [productId]: '' }));
      if (showToast) showToast(`Price updated to $${price.toFixed(2)}.`);
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to update price.';
      if (showToast) showToast(msg, 'error');
    } finally {
      setActiveActionId(null);
    }
  };

  // Delete product handler
  const handleDeleteProduct = async (productId, productName) => {
    if (!window.confirm(`Are you sure you want to remove "${productName}" from the storefront?`)) {
      return;
    }

    setActiveActionId(productId);
    try {
      await axiosClient.delete(`/products/${productId}`);
      setProducts((prev) => prev.filter((p) => p.id !== productId));
      if (showToast) showToast(`"${productName}" removed from catalog.`);
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to delete product.';
      if (showToast) showToast(msg, 'error');
    } finally {
      setActiveActionId(null);
    }
  };

  // Add Product Submit
  const handleCreateProduct = async (e) => {
    e.preventDefault();
    if (!newProduct.name.trim()) {
      if (showToast) showToast('Product name is required.', 'error');
      return;
    }
    const priceNum = parseFloat(newProduct.price);
    if (isNaN(priceNum) || priceNum <= 0) {
      if (showToast) showToast('Please enter a valid price greater than zero.', 'error');
      return;
    }
    const stockNum = parseInt(newProduct.stockQuantity, 10);
    if (isNaN(stockNum) || stockNum < 0) {
      if (showToast) showToast('Stock quantity cannot be negative.', 'error');
      return;
    }

    setSubmittingProduct(true);
    try {
      const payload = {
        name: newProduct.name.trim(),
        description: newProduct.description.trim(),
        category: newProduct.category,
        price: priceNum,
        stockQuantity: stockNum,
        imageUrl: newProduct.imageUrl.trim() || 'https://images.unsplash.com/photo-1523275335684-37898b6baf30',
        active: true,
      };

      const res = await axiosClient.post('/products', payload);
      const created = res.data || res;
      setProducts((prev) => [created, ...prev]);
      setShowAddModal(false);
      setNewProduct({
        name: '',
        description: '',
        category: 'Electronics',
        price: '',
        stockQuantity: '',
        imageUrl: '',
      });
      if (showToast) showToast(`Successfully created "${created.name}"!`);
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to create product.';
      if (showToast) showToast(msg, 'error');
    } finally {
      setSubmittingProduct(false);
    }
  };

  // Filtered products list
  const filteredProducts = products.filter((p) => {
    const matchesSearch =
      searchTerm.trim() === '' ||
      p.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (p.description && p.description.toLowerCase().includes(searchTerm.toLowerCase())) ||
      (p.category && p.category.toLowerCase().includes(searchTerm.toLowerCase()));

    const matchesCategory =
      categoryFilter === 'All' ||
      (p.category && p.category.toLowerCase() === categoryFilter.toLowerCase());

    const matchesStock =
      stockFilter === 'All' ||
      (stockFilter === 'Out' && p.stockQuantity <= 0) ||
      (stockFilter === 'Low' && p.stockQuantity > 0 && p.stockQuantity <= 5);

    return matchesSearch && matchesCategory && matchesStock;
  });

  // KPI Calculations
  const totalStockCount = products.reduce((acc, curr) => acc + (curr.stockQuantity || 0), 0);
  const outOfStockCount = products.filter((p) => p.stockQuantity <= 0).length;
  const lowStockCount = products.filter((p) => p.stockQuantity > 0 && p.stockQuantity <= 5).length;

  return (
    <div className="flex-1 bg-gray-50/50 py-8 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto space-y-6">

        {/* Header & Quick Action */}
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-gray-200 shadow-sm">
          <div>
            <div className="inline-flex items-center gap-2 px-3 py-1 bg-indigo-50 text-indigo-700 rounded-full text-xs font-semibold mb-2">
              <Sparkles className="w-3.5 h-3.5" />
              <span>Manager Portal</span>
            </div>
            <h1 className="text-2xl sm:text-3xl font-black text-gray-900 tracking-tight">
              Inventory & Catalog Management
            </h1>
            <p className="text-sm text-gray-500 mt-1">
              Live stock levels, atomic adjustments, pricing overrides, and catalog creation.
            </p>
          </div>

          <div className="flex items-center gap-3">
            <button
              onClick={fetchProducts}
              disabled={loading}
              className="p-2.5 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-xl transition-colors"
              title="Refresh Catalog"
            >
              <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
            </button>
            <button
              onClick={() => setShowAddModal(true)}
              className="px-5 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white font-bold text-sm rounded-xl shadow-md transition-all flex items-center gap-2"
            >
              <Plus className="w-4 h-4" />
              <span>Add New Product</span>
            </button>
          </div>
        </div>

        {/* Stats KPI Cards */}
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          <div className="bg-white p-5 rounded-2xl border border-gray-200 shadow-sm">
            <div className="flex items-center justify-between text-gray-400 mb-2">
              <span className="text-xs font-bold uppercase tracking-wider">Total Products</span>
              <Package className="w-5 h-5 text-indigo-600" />
            </div>
            <div className="text-2xl font-black text-gray-900">{products.length}</div>
            <p className="text-xs text-gray-500 mt-1">{totalStockCount} units in inventory</p>
          </div>

          <div className="bg-white p-5 rounded-2xl border border-gray-200 shadow-sm">
            <div className="flex items-center justify-between text-gray-400 mb-2">
              <span className="text-xs font-bold uppercase tracking-wider">Low Stock (&le; 5)</span>
              <AlertTriangle className="w-5 h-5 text-amber-500" />
            </div>
            <div className="text-2xl font-black text-amber-600">{lowStockCount}</div>
            <p className="text-xs text-gray-500 mt-1">Requires reorder attention</p>
          </div>

          <div className="bg-white p-5 rounded-2xl border border-gray-200 shadow-sm">
            <div className="flex items-center justify-between text-gray-400 mb-2">
              <span className="text-xs font-bold uppercase tracking-wider">Out of Stock</span>
              <Boxes className="w-5 h-5 text-red-500" />
            </div>
            <div className="text-2xl font-black text-red-600">{outOfStockCount}</div>
            <p className="text-xs text-gray-500 mt-1">Customers cannot purchase</p>
          </div>

          <div className="bg-white p-5 rounded-2xl border border-gray-200 shadow-sm">
            <div className="flex items-center justify-between text-gray-400 mb-2">
              <span className="text-xs font-bold uppercase tracking-wider">Healthy Stock</span>
              <CheckCircle className="w-5 h-5 text-emerald-500" />
            </div>
            <div className="text-2xl font-black text-emerald-600">
              {products.length - outOfStockCount - lowStockCount}
            </div>
            <p className="text-xs text-gray-500 mt-1">Ready for fulfillment</p>
          </div>
        </div>

        {/* Filter Controls Bar */}
        <div className="bg-white p-4 rounded-2xl border border-gray-200 shadow-sm flex flex-col md:flex-row items-center justify-between gap-4">
          <div className="relative w-full md:w-80">
            <input
              type="text"
              placeholder="Search by name, description, category..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-9 pr-4 py-2 text-sm bg-gray-50 border border-gray-300 rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
            <Search className="w-4 h-4 text-gray-400 absolute left-3 top-1/2 -translate-y-1/2" />
          </div>

          <div className="flex flex-wrap items-center gap-2 w-full md:w-auto">
            <span className="text-xs font-bold uppercase text-gray-400 mr-1 flex items-center gap-1">
              <Filter className="w-3.5 h-3.5" /> Category:
            </span>
            {['All', ...CATEGORIES].map((cat) => (
              <button
                key={cat}
                onClick={() => setCategoryFilter(cat)}
                className={`px-3 py-1 text-xs font-semibold rounded-lg transition-colors ${
                  categoryFilter === cat
                    ? 'bg-indigo-600 text-white'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
              >
                {cat}
              </button>
            ))}

            <div className="h-4 w-px bg-gray-300 mx-1 hidden sm:block"></div>

            <button
              onClick={() => setStockFilter(stockFilter === 'Low' ? 'All' : 'Low')}
              className={`px-3 py-1 text-xs font-semibold rounded-lg transition-colors ${
                stockFilter === 'Low'
                  ? 'bg-amber-600 text-white'
                  : 'bg-amber-50 text-amber-800 hover:bg-amber-100'
              }`}
            >
              Low Stock Only
            </button>
          </div>
        </div>

        {/* Inventory Table */}
        <div className="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200 text-left text-sm">
              <thead className="bg-gray-50/75 text-xs uppercase font-bold text-gray-500 tracking-wider">
                <tr>
                  <th scope="col" className="px-6 py-4">Product Details</th>
                  <th scope="col" className="px-6 py-4">Category</th>
                  <th scope="col" className="px-6 py-4">Unit Price</th>
                  <th scope="col" className="px-6 py-4">Inventory & Quantity Controls</th>
                  <th scope="col" className="px-6 py-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {loading ? (
                  <tr>
                    <td colSpan="5" className="px-6 py-12 text-center text-gray-500">
                      <div className="flex flex-col items-center justify-center gap-2">
                        <RefreshCw className="w-6 h-6 text-indigo-600 animate-spin" />
                        <span>Loading catalog inventory...</span>
                      </div>
                    </td>
                  </tr>
                ) : filteredProducts.length === 0 ? (
                  <tr>
                    <td colSpan="5" className="px-6 py-12 text-center text-gray-500">
                      <div className="flex flex-col items-center justify-center gap-2">
                        <Package className="w-8 h-8 text-gray-300" />
                        <span className="font-semibold text-gray-700">No matching products found.</span>
                        <span className="text-xs text-gray-400">Try adjusting your search or category filters.</span>
                      </div>
                    </td>
                  </tr>
                ) : (
                  filteredProducts.map((p) => {
                    const isOutOfStock = p.stockQuantity <= 0;
                    const isLowStock = p.stockQuantity > 0 && p.stockQuantity <= 5;
                    const isBusy = activeActionId === p.id;

                    return (
                      <tr key={p.id} className="hover:bg-gray-50/60 transition-colors">
                        {/* Product Info */}
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="flex items-center gap-3">
                            <img
                              src={p.imageUrl || 'https://images.unsplash.com/photo-1523275335684-37898b6baf30'}
                              alt={p.name}
                              className="w-12 h-12 rounded-xl object-cover border border-gray-100 flex-shrink-0"
                            />
                            <div className="max-w-xs">
                              <div className="font-bold text-gray-900 truncate">{p.name}</div>
                              <div className="text-xs text-gray-400 line-clamp-1">
                                ID: #{p.id} {p.description ? `• ${p.description}` : ''}
                              </div>
                            </div>
                          </div>
                        </td>

                        {/* Category */}
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-gray-100 text-gray-700">
                            {p.category || 'General'}
                          </span>
                        </td>

                        {/* Unit Price & Controls */}
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="space-y-1.5">
                            <div className="font-bold text-gray-900 text-base">
                              ${parseFloat(p.price).toFixed(2)}
                            </div>
                            <div className="flex items-center gap-1.5">
                              <input
                                type="number"
                                step="0.01"
                                min="0.01"
                                placeholder="New $"
                                value={customPriceInputs[p.id] || ''}
                                onChange={(e) =>
                                  setCustomPriceInputs((prev) => ({ ...prev, [p.id]: e.target.value }))
                                }
                                className="w-20 px-2 py-1 text-xs border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-indigo-500"
                              />
                              <button
                                onClick={() => handlePriceSet(p.id)}
                                disabled={isBusy || !customPriceInputs[p.id]}
                                className="px-2 py-1 text-xs font-bold text-indigo-700 bg-indigo-50 hover:bg-indigo-100 rounded-lg disabled:opacity-40"
                              >
                                Save
                              </button>
                            </div>
                          </div>
                        </td>

                        {/* Stock & Quantity Controls */}
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="space-y-2">
                            <div className="flex items-center gap-2">
                              <span className="text-base font-black text-gray-900">
                                {p.stockQuantity} units
                              </span>
                              {isOutOfStock ? (
                                <span className="px-2 py-0.5 text-[10px] font-bold uppercase rounded-full bg-red-100 text-red-700">
                                  Out of Stock
                                </span>
                              ) : isLowStock ? (
                                <span className="px-2 py-0.5 text-[10px] font-bold uppercase rounded-full bg-amber-100 text-amber-700">
                                  Low Stock
                                </span>
                              ) : (
                                <span className="px-2 py-0.5 text-[10px] font-bold uppercase rounded-full bg-emerald-100 text-emerald-700">
                                  In Stock
                                </span>
                              )}
                            </div>

                            <div className="flex items-center gap-2">
                              {/* Quick Delta Buttons */}
                              <div className="inline-flex rounded-lg border border-gray-200 bg-gray-50 p-0.5">
                                <button
                                  onClick={() => handleStockDelta(p.id, -1)}
                                  disabled={isBusy || isOutOfStock}
                                  className="px-2.5 py-1 text-xs font-black text-gray-700 hover:bg-white rounded-md disabled:opacity-30 transition-all shadow-sm"
                                  title="Deduct 1 unit"
                                >
                                  -1
                                </button>
                                <button
                                  onClick={() => handleStockDelta(p.id, 1)}
                                  disabled={isBusy}
                                  className="px-2.5 py-1 text-xs font-black text-gray-700 hover:bg-white rounded-md disabled:opacity-30 transition-all shadow-sm"
                                  title="Add 1 unit"
                                >
                                  +1
                                </button>
                              </div>

                              {/* Custom Quantity Input */}
                              <input
                                type="number"
                                min="0"
                                placeholder="Qty"
                                value={customStockInputs[p.id] || ''}
                                onChange={(e) =>
                                  setCustomStockInputs((prev) => ({ ...prev, [p.id]: e.target.value }))
                                }
                                className="w-16 px-2 py-1 text-xs border border-gray-300 rounded-lg focus:outline-none focus:ring-1 focus:ring-indigo-500"
                              />
                              <button
                                onClick={() => handleStockSet(p.id)}
                                disabled={isBusy || customStockInputs[p.id] === undefined || customStockInputs[p.id] === ''}
                                className="px-2.5 py-1 text-xs font-bold text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg disabled:opacity-40"
                              >
                                Set
                              </button>
                            </div>
                          </div>
                        </td>

                        {/* Actions */}
                        <td className="px-6 py-4 whitespace-nowrap text-right">
                          <button
                            onClick={() => handleDeleteProduct(p.id, p.name)}
                            disabled={isBusy}
                            className="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-xl transition-colors"
                            title="Remove Product"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>
        </div>

      </div>

      {/* Add New Product Modal */}
      {showAddModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm animate-in fade-in duration-200">
          <div className="bg-white rounded-3xl max-w-lg w-full p-6 sm:p-8 shadow-2xl space-y-6 relative border border-gray-100">
            <div className="flex items-center justify-between border-b border-gray-100 pb-4">
              <div>
                <h3 className="text-xl font-black text-gray-900 tracking-tight">Create Catalog Product</h3>
                <p className="text-xs text-gray-500 mt-0.5">Item will be live and purchasable in the storefront immediately.</p>
              </div>
              <button
                onClick={() => setShowAddModal(false)}
                className="p-2 text-gray-400 hover:text-gray-600 rounded-xl hover:bg-gray-100 transition-colors"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleCreateProduct} className="space-y-4">
              <div>
                <label className="block text-xs font-bold uppercase text-gray-700 mb-1">Product Name *</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Ergonomic Office Chair"
                  value={newProduct.name}
                  onChange={(e) => setNewProduct({ ...newProduct, name: e.target.value })}
                  className="w-full px-4 py-2.5 text-sm border border-gray-300 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold uppercase text-gray-700 mb-1">Category *</label>
                  <select
                    value={newProduct.category}
                    onChange={(e) => setNewProduct({ ...newProduct, category: e.target.value })}
                    className="w-full px-3 py-2.5 text-sm border border-gray-300 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:outline-none bg-white"
                  >
                    {CATEGORIES.map((c) => (
                      <option key={c} value={c}>{c}</option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-bold uppercase text-gray-700 mb-1">Price ($ USD) *</label>
                  <input
                    type="number"
                    step="0.01"
                    min="0.01"
                    required
                    placeholder="99.99"
                    value={newProduct.price}
                    onChange={(e) => setNewProduct({ ...newProduct, price: e.target.value })}
                    className="w-full px-4 py-2.5 text-sm border border-gray-300 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold uppercase text-gray-700 mb-1">Initial Stock Quantity *</label>
                <input
                  type="number"
                  min="0"
                  required
                  placeholder="50"
                  value={newProduct.stockQuantity}
                  onChange={(e) => setNewProduct({ ...newProduct, stockQuantity: e.target.value })}
                  className="w-full px-4 py-2.5 text-sm border border-gray-300 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                />
              </div>

              <div>
                <label className="block text-xs font-bold uppercase text-gray-700 mb-1">Image URL</label>
                <input
                  type="url"
                  placeholder="https://images.unsplash.com/photo-..."
                  value={newProduct.imageUrl}
                  onChange={(e) => setNewProduct({ ...newProduct, imageUrl: e.target.value })}
                  className="w-full px-4 py-2.5 text-sm border border-gray-300 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                />
              </div>

              <div>
                <label className="block text-xs font-bold uppercase text-gray-700 mb-1">Description</label>
                <textarea
                  rows="3"
                  placeholder="Detailed specifications and key features..."
                  value={newProduct.description}
                  onChange={(e) => setNewProduct({ ...newProduct, description: e.target.value })}
                  className="w-full px-4 py-2 text-sm border border-gray-300 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                ></textarea>
              </div>

              <div className="pt-4 flex items-center justify-end gap-3 border-t border-gray-100">
                <button
                  type="button"
                  onClick={() => setShowAddModal(false)}
                  className="px-4 py-2 text-sm font-semibold text-gray-600 hover:bg-gray-100 rounded-xl transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submittingProduct}
                  className="px-6 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white font-bold text-sm rounded-xl shadow transition-all disabled:opacity-50"
                >
                  {submittingProduct ? 'Creating...' : 'Save Product'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
