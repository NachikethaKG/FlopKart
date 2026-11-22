import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import { Sparkles, ArrowRight, Shield, Zap, ShoppingBag } from 'lucide-react';

function StorefrontHome() {
  return (
    <div className="flex-1 flex flex-col">
      {/* Hero Banner */}
      <section className="relative overflow-hidden bg-gradient-to-br from-blue-700 via-indigo-700 to-slate-900 text-white py-20 px-4 sm:px-6 lg:px-8">
        <div className="absolute inset-0 opacity-10 bg-[radial-gradient(#fff_1px,transparent_1px)] [background-size:16px_16px]"></div>
        <div className="max-w-5xl mx-auto relative z-10 text-center space-y-6">
          <div className="inline-flex items-center gap-2 px-3 py-1 bg-white/10 backdrop-blur-md border border-white/20 rounded-full text-xs font-semibold tracking-wide text-blue-200">
            <Sparkles className="w-3.5 h-3.5 text-blue-300" />
            <span>Next-Gen Microservices Architecture</span>
          </div>

          <h1 className="text-4xl sm:text-6xl font-black tracking-tight leading-tight">
            Welcome to <span className="bg-gradient-to-r from-blue-200 to-indigo-100 bg-clip-text text-transparent">FlopKart</span>
          </h1>

          <p className="text-base sm:text-lg text-blue-100 max-w-2xl mx-auto leading-relaxed">
            Enterprise-grade e-commerce storefront backed by Spring Cloud Gateway, Apache Kafka KRaft, Redis 7 ephemeral caching, and PostgreSQL.
          </p>

          <div className="flex flex-wrap items-center justify-center gap-4 pt-2">
            <Link
              to="/products"
              className="inline-flex items-center gap-2 px-6 py-3 bg-white text-blue-700 hover:bg-blue-50 font-bold rounded-xl shadow-lg transition-transform hover:-translate-y-0.5"
            >
              <ShoppingBag className="w-5 h-5" />
              <span>Explore Catalog</span>
              <ArrowRight className="w-4 h-4 ml-1" />
            </Link>
            <Link
              to="/register"
              className="inline-flex items-center gap-2 px-6 py-3 bg-white/10 hover:bg-white/20 border border-white/30 text-white font-semibold rounded-xl backdrop-blur-md transition-colors"
            >
              <span>Create Account</span>
            </Link>
          </div>
        </div>
      </section>

      {/* Feature Grid */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div className="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm hover:shadow-md transition-shadow">
            <div className="w-12 h-12 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center mb-4">
              <Zap className="w-6 h-6" />
            </div>
            <h3 className="text-lg font-bold text-gray-900 mb-2">Zero-Lag Cart Synchronization</h3>
            <p className="text-sm text-gray-600 leading-relaxed">
              Real-time cart updates orchestrated via Redis session caching and Spring WebFlux gateway forwarding.
            </p>
          </div>

          <div className="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm hover:shadow-md transition-shadow">
            <div className="w-12 h-12 rounded-xl bg-green-50 text-green-600 flex items-center justify-center mb-4">
              <Shield className="w-6 h-6" />
            </div>
            <h3 className="text-lg font-bold text-gray-900 mb-2">Transactional Saga Consistency</h3>
            <p className="text-sm text-gray-600 leading-relaxed">
              Resilient order orchestration with automatic inventory reservation and compensating rollback triggers.
            </p>
          </div>

          <div className="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm hover:shadow-md transition-shadow">
            <div className="w-12 h-12 rounded-xl bg-purple-50 text-purple-600 flex items-center justify-center mb-4">
              <Sparkles className="w-6 h-6" />
            </div>
            <h3 className="text-lg font-bold text-gray-900 mb-2">Stateless JWT Sessions</h3>
            <p className="text-sm text-gray-600 leading-relaxed">
              Cryptographically signed HMAC tokens validated at the gateway boundary with automatic refresh flow.
            </p>
          </div>
        </div>
      </section>
    </div>
  );
}

export default function App() {
  return (
    <Router>
      <div className="min-h-screen flex flex-col bg-gray-50">
        <Navbar />
        <main className="flex-1 flex flex-col">
          <Routes>
            <Route path="/" element={<StorefrontHome />} />
            <Route path="*" element={<StorefrontHome />} />
          </Routes>
        </main>
        <Footer />
      </div>
    </Router>
  );
}
