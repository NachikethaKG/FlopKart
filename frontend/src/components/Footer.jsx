import React from 'react';
import { Sparkles, ShieldCheck, Zap, RefreshCw, Heart } from 'lucide-react';

export default function Footer() {
  return (
    <footer className="bg-white border-t border-gray-200 mt-auto">
      {/* Platform Highlights */}
      <div className="border-b border-gray-100 bg-gray-50/50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 text-center md:text-left">
            <div className="flex items-center gap-4 justify-center md:justify-start">
              <div className="w-12 h-12 rounded-xl bg-blue-100 text-blue-600 flex items-center justify-center flex-shrink-0">
                <Zap className="w-6 h-6" />
              </div>
              <div>
                <h4 className="text-sm font-bold text-gray-900">Event-Driven Architecture</h4>
                <p className="text-xs text-gray-500">Asynchronous Saga choreography powered by Apache Kafka</p>
              </div>
            </div>

            <div className="flex items-center gap-4 justify-center md:justify-start">
              <div className="w-12 h-12 rounded-xl bg-green-100 text-green-600 flex items-center justify-center flex-shrink-0">
                <ShieldCheck className="w-6 h-6" />
              </div>
              <div>
                <h4 className="text-sm font-bold text-gray-900">Stateless Security</h4>
                <p className="text-xs text-gray-500">HMAC-SHA256 JWT tokens with role-based routing</p>
              </div>
            </div>

            <div className="flex items-center gap-4 justify-center md:justify-start">
              <div className="w-12 h-12 rounded-xl bg-purple-100 text-purple-600 flex items-center justify-center flex-shrink-0">
                <RefreshCw className="w-6 h-6" />
              </div>
              <div>
                <h4 className="text-sm font-bold text-gray-900">High Performance Caching</h4>
                <p className="text-xs text-gray-500">Redis 7 session cart with sub-millisecond lookups</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Main Footer Links */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          
          <div className="space-y-4">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 rounded-lg bg-blue-600 flex items-center justify-center text-white">
                <Sparkles className="w-5 h-5" />
              </div>
              <span className="text-lg font-black tracking-tight text-gray-900">
                Flop<span className="text-blue-600">Kart</span>
              </span>
            </div>
            <p className="text-xs text-gray-500 leading-relaxed">
              Cloud-native microservices e-commerce platform built with Java 21, Spring Boot 3, Apache Kafka, Redis, PostgreSQL, and React.
            </p>
          </div>

          <div>
            <h4 className="text-xs font-bold uppercase tracking-wider text-gray-900 mb-4">Catalog</h4>
            <ul className="space-y-2 text-sm text-gray-600">
              <li><span className="hover:text-blue-600 cursor-pointer">Electronics</span></li>
              <li><span className="hover:text-blue-600 cursor-pointer">Audio & Accessories</span></li>
              <li><span className="hover:text-blue-600 cursor-pointer">Wearables</span></li>
              <li><span className="hover:text-blue-600 cursor-pointer">New Arrivals</span></li>
            </ul>
          </div>

          <div>
            <h4 className="text-xs font-bold uppercase tracking-wider text-gray-900 mb-4">Architecture</h4>
            <ul className="space-y-2 text-sm text-gray-600">
              <li><span>Spring Cloud Gateway</span></li>
              <li><span>Eureka Discovery</span></li>
              <li><span>Kafka KRaft Messaging</span></li>
              <li><span>Resilience4j Circuit Breakers</span></li>
            </ul>
          </div>

          <div>
            <h4 className="text-xs font-bold uppercase tracking-wider text-gray-900 mb-4">Microservices</h4>
            <div className="flex flex-wrap gap-1.5">
              <span className="px-2 py-1 text-[11px] font-medium bg-gray-100 text-gray-700 rounded-md">user-service</span>
              <span className="px-2 py-1 text-[11px] font-medium bg-gray-100 text-gray-700 rounded-md">product-service</span>
              <span className="px-2 py-1 text-[11px] font-medium bg-gray-100 text-gray-700 rounded-md">cart-service</span>
              <span className="px-2 py-1 text-[11px] font-medium bg-gray-100 text-gray-700 rounded-md">order-service</span>
              <span className="px-2 py-1 text-[11px] font-medium bg-gray-100 text-gray-700 rounded-md">payment-service</span>
              <span className="px-2 py-1 text-[11px] font-medium bg-gray-100 text-gray-700 rounded-md">notification-service</span>
            </div>
          </div>
        </div>

        <div className="border-t border-gray-100 mt-10 pt-6 flex flex-col sm:flex-row items-center justify-between gap-4 text-xs text-gray-500">
          <p>© 2026 FlopKart E-Commerce Platform. All rights reserved.</p>
          <div className="flex items-center gap-1">
            <span>Built with</span>
            <Heart className="w-3.5 h-3.5 text-red-500 fill-red-500 inline" />
            <span>using Spring Boot 3 & React</span>
          </div>
        </div>
      </div>
    </footer>
  );
}
