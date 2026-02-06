/**
 * Language Toggle Component
 * Handles vernacular support for English, Hindi, and Tamil
 */
const languageToggle = {
    currentLanguage: 'en',

    translations: {
        en: {
            "nav.dashboard": "Dashboard",
            "nav.performance": "Performance",
            "nav.market_lookup": "Market Lookup",
            "nav.transaction_history": "Transaction History",
            "dashboard.title": "Dashboard",
            "dashboard.total_purse": "Total Purse",
            "dashboard.portfolio_value": "Portfolio Value",
            "dashboard.profit_loss": "Profit/Loss",
            "dashboard.total_return": "Total Return",
            "performance.title": "Performance",
            "market.title": "Market Lookup",
            "market.search_placeholder": "Search for stocks...",
            "market.industry": "Industry",
            "market.website": "Website",
            "market.stock_price": "Stock Price",
            "market.add_watchlist": "Add to watchlist",
            "market.remove_watchlist": "Remove from watchlist",
            "market.company_desc": "Company Description",
            "market.latest_news": "Latest News & Sentiment",
            "market.no_news": "No news available for this stock."
        },
        hi: {
            "nav.dashboard": "डैशबोर्ड",
            "nav.performance": "प्रदर्शन",
            "nav.market_lookup": "बाज़ार खोज",
            "nav.transaction_history": "लेनदेन इतिहास",
            "dashboard.title": "डैशबोर्ड",
            "dashboard.total_purse": "कुल पर्स",
            "dashboard.portfolio_value": "पोर्टफोलियो मूल्य",
            "dashboard.profit_loss": "लाभ/हानि",
            "dashboard.total_return": "कुल रिटर्न",
            "performance.title": "प्रदर्शन",
            "market.title": "बाज़ार खोज",
            "market.search_placeholder": "स्टॉक खोजें...",
            "market.industry": "उद्योग",
            "market.website": "वेबसाइट",
            "market.stock_price": "शेयर की कीमत",
            "market.add_watchlist": "वॉचलिस्ट में जोड़ें",
            "market.remove_watchlist": "वॉचलिस्ट से हटाएं",
            "market.company_desc": "कंपनी विवरण",
            "market.latest_news": "नवीनतम समाचार और भावना",
            "market.no_news": "इस स्टॉक के लिए कोई समाचार उपलब्ध नहीं है।"
        },
        ta: {
            "nav.dashboard": "டாஷ்போர்டு",
            "nav.performance": "செயல்திறன்",
            "nav.market_lookup": "சந்தை தேடல்",
            "nav.transaction_history": "பரிவர்த்தனை வரலாறு",
            "dashboard.title": "டாஷ்போர்டு",
            "dashboard.total_purse": "மொத்த பை",
            "dashboard.portfolio_value": "போர்ட்ஃபோலியோ மதிப்பு",
            "dashboard.profit_loss": "லாபம்/நஷ்டம்",
            "dashboard.total_return": "மொத்த வருவாய்",
            "performance.title": "செயல்திறன்",
            "market.title": "சந்தை தேடல்",
            "market.search_placeholder": "பங்குகளைத் தேடுங்கள்...",
            "market.industry": "தொழில்",
            "market.website": "இணையதளம்",
            "market.stock_price": "பங்கு விலை",
            "market.add_watchlist": "கண்காணிப்பு பட்டியலில் சேர்",
            "market.remove_watchlist": "கண்காணிப்பு பட்டியலில் இருந்து நீக்கு",
            "market.company_desc": "நிறுவன விளக்கம்",
            "market.latest_news": "சமீபத்திய செய்திகள்",
            "market.no_news": "இந்த பங்கிற்கு செய்திகள் இல்லை."
        }
    },

    toggleDropdown: function () {
        const dropdown = document.getElementById('lang-dropdown');
        if (dropdown) {
            dropdown.classList.toggle('show');
        }
    },

    setLanguage: function (lang) {
        this.currentLanguage = lang;
        this.applyLanguage(lang);
        const dropdown = document.getElementById('lang-dropdown');
        if (dropdown) {
            dropdown.classList.remove('show');
        }

        const labels = { 'en': 'English', 'hi': 'हिंदी', 'ta': 'தமிழ்' };
        const labelEl = document.getElementById('current-lang-label');
        if (labelEl) {
            labelEl.innerText = labels[lang];
        }
    },

    getTranslation: function (lang, key) {
        return this.translations[lang] && this.translations[lang][key] ? this.translations[lang][key] : key;
    },

    applyLanguage: function (lang) {
        const t = this.translations[lang];
        if (!t) return;

        document.querySelectorAll('[data-i18n]').forEach(el => {
            const key = el.getAttribute('data-i18n');
            if (t[key]) {
                if (el.tagName === 'INPUT' && el.hasAttribute('placeholder')) {
                    el.placeholder = t[key];
                } else {
                    el.innerText = t[key];
                }
            }
        });
    },

    init: function () {
        // Close dropdown when clicking outside
        document.addEventListener('click', (e) => {
            if (!e.target.closest('.language-selector')) {
                const dropdown = document.getElementById('lang-dropdown');
                if (dropdown) dropdown.classList.remove('show');
            }
        });
    }
};

// Auto-initialize when DOM is ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
        languageToggle.init();
    });
} else {
    languageToggle.init();
}
