import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { CartProvider, useCart } from './context/CartContext';
import ProtectedRoute from './components/ProtectedRoute';
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import Toast from './components/Toast';
import HomePage from './pages/HomePage';
import ProductDetailPage from './pages/ProductDetailPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import CartPage from './pages/CartPage';

function OrdersPlaceholder() {
  return (
    <div className="max-w-4xl mx-auto py-16 px-4 text-center">
      <h2 className="text-2xl font-bold text-gray-900">Your Orders</h2>
      <p className="text-gray-500 mt-2">Order tracking UI will be connected in Phase 5.5.</p>
    </div>
  );
}

function MainRoutes() {
  const { addItem } = useCart();

  return (
    <div className="min-h-screen flex flex-col bg-gray-50">
      <Navbar />
      <main className="flex-1 flex flex-col">
        <Routes>
          <Route path="/" element={<HomePage onAddToCart={addItem} />} />
          <Route path="/products" element={<HomePage onAddToCart={addItem} />} />
          <Route path="/products/:id" element={<ProductDetailPage onAddToCart={addItem} />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route
            path="/cart"
            element={
              <ProtectedRoute>
                <CartPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/orders"
            element={
              <ProtectedRoute>
                <OrdersPlaceholder />
              </ProtectedRoute>
            }
          />
          <Route path="*" element={<HomePage onAddToCart={addItem} />} />
        </Routes>
      </main>
      <Footer />
      <Toast />
    </div>
  );
}

export default function App() {
  return (
    <Router>
      <AuthProvider>
        <CartProvider>
          <MainRoutes />
        </CartProvider>
      </AuthProvider>
    </Router>
  );
}
