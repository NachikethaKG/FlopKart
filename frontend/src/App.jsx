import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import HomePage from './pages/HomePage';
import ProductDetailPage from './pages/ProductDetailPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';

function CartPlaceholder() {
  return (
    <div className="max-w-4xl mx-auto py-16 px-4 text-center">
      <h2 className="text-2xl font-bold text-gray-900">Your Cart</h2>
      <p className="text-gray-500 mt-2">Interactive cart management will be wired in Phase 5.4.</p>
    </div>
  );
}

function OrdersPlaceholder() {
  return (
    <div className="max-w-4xl mx-auto py-16 px-4 text-center">
      <h2 className="text-2xl font-bold text-gray-900">Your Orders</h2>
      <p className="text-gray-500 mt-2">Order tracking UI will be connected in Phase 5.5.</p>
    </div>
  );
}

export default function App() {
  return (
    <Router>
      <AuthProvider>
        <div className="min-h-screen flex flex-col bg-gray-50">
          <Navbar />
          <main className="flex-1 flex flex-col">
            <Routes>
              <Route path="/" element={<HomePage />} />
              <Route path="/products" element={<HomePage />} />
              <Route path="/products/:id" element={<ProductDetailPage />} />
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterPage />} />
              <Route
                path="/cart"
                element={
                  <ProtectedRoute>
                    <CartPlaceholder />
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
              <Route path="*" element={<HomePage />} />
            </Routes>
          </main>
          <Footer />
        </div>
      </AuthProvider>
    </Router>
  );
}
