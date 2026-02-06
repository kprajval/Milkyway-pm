/**
 * EXPERIMENTAL FEATURE - Language Toggle System
 * ==============================================
 * This is a proof-of-concept for UI translation.
 * Can be safely removed without affecting core functionality.
 * 
 * How it works:
 * 1. Loads translation dictionaries from JSON files
 * 2. Finds elements with 'data-i18n' attribute
 * 3. Swaps text content based on selected language
 * 
 * To disable: Remove script tag from HTML templates
 */

class LanguageToggle {
    constructor() {
        this.currentLanguage = localStorage.getItem('preferredLanguage') || 'en';
        this.translations = {};
        this.supportedLanguages = {
            'en': 'English',
            'hi': 'हिंदी',
            'ta': 'தமிழ்',
            'te': 'తెలుగు'
        };
    }

    /**
     * Initialize the language toggle system
     */
    async init() {
        try {
            // Load translation files for all supported languages
            await this.loadTranslations();
            
            // Apply saved language preference
            this.applyLanguage(this.currentLanguage);
            
            // Update toggle button if exists
            this.updateToggleButton();
            
            console.log('✅ Language toggle initialized:', this.currentLanguage);
        } catch (error) {
            console.error('❌ Language toggle failed to initialize:', error);
        }
    }

    /**
     * Load translation JSON files
     */
    async loadTranslations() {
        for (const lang in this.supportedLanguages) {
            if (lang === 'en') continue; // English is default, no file needed
            
            try {
                const response = await fetch(`/translations/${lang}.json`);
                if (response.ok) {
                    this.translations[lang] = await response.json();
                }
            } catch (error) {
                console.warn(`⚠️ Could not load translation: ${lang}`, error);
            }
        }
    }

    /**
     * Apply language to all translatable elements
     */
    applyLanguage(langCode) {
        // Translate text content elements
        const elements = document.querySelectorAll('[data-i18n]');
        
        elements.forEach(element => {
            const key = element.getAttribute('data-i18n');
            
            if (langCode === 'en') {
                // Restore original English text
                if (element.hasAttribute('data-i18n-original')) {
                    element.textContent = element.getAttribute('data-i18n-original');
                }
            } else {
                // Save original text if not already saved
                if (!element.hasAttribute('data-i18n-original')) {
                    element.setAttribute('data-i18n-original', element.textContent);
                }
                
                // Apply translation
                const translation = this.getTranslation(langCode, key);
                if (translation) {
                    element.textContent = translation;
                }
            }
        });
        
        // Translate placeholder attributes
        const placeholderElements = document.querySelectorAll('[data-i18n-placeholder]');
        
        placeholderElements.forEach(element => {
            const key = element.getAttribute('data-i18n-placeholder');
            
            if (langCode === 'en') {
                if (element.hasAttribute('data-i18n-placeholder-original')) {
                    element.placeholder = element.getAttribute('data-i18n-placeholder-original');
                }
            } else {
                if (!element.hasAttribute('data-i18n-placeholder-original')) {
                    element.setAttribute('data-i18n-placeholder-original', element.placeholder);
                }
                
                const translation = this.getTranslation(langCode, key);
                if (translation) {
                    element.placeholder = translation;
                }
            }
        });
        
        this.currentLanguage = langCode;
        localStorage.setItem('preferredLanguage', langCode);
    }

    /**
     * Get translation for a specific key
     */
    getTranslation(langCode, key) {
        const langDict = this.translations[langCode];
        if (!langDict) return null;
        
        // Support nested keys like "dashboard.title"
        const keys = key.split('.');
        let value = langDict;
        
        for (const k of keys) {
            if (value && typeof value === 'object') {
                value = value[k];
            } else {
                return null;
            }
        }
        
        return value;
    }

    /**
     * Toggle between languages
     */
    toggleLanguage() {
        const languages = Object.keys(this.supportedLanguages);
        const currentIndex = languages.indexOf(this.currentLanguage);
        const nextIndex = (currentIndex + 1) % languages.length;
        const nextLanguage = languages[nextIndex];
        
        this.applyLanguage(nextLanguage);
        this.updateToggleButton();
    }

    /**
     * Set specific language
     */
    setLanguage(langCode) {
        if (this.supportedLanguages[langCode]) {
            this.applyLanguage(langCode);
            this.updateToggleButton();
        }
    }

    /**
     * Update the toggle button text
     */
    updateToggleButton() {
        const button = document.getElementById('language-toggle-btn');
        if (button) {
            const langName = this.supportedLanguages[this.currentLanguage];
            button.innerHTML = `<i class="fas fa-language"></i> ${langName}`;
        }
    }

    /**
     * Create language selector dropdown
     */
    createLanguageSelector() {
        const container = document.getElementById('language-toggle-container');
        if (!container) return;

        const html = `
            <div class="language-selector">
                <button id="language-toggle-btn" class="lang-toggle-btn">
                    <i class="fas fa-language"></i> ${this.supportedLanguages[this.currentLanguage]}
                </button>
                <div class="language-dropdown" id="language-dropdown">
                    ${Object.entries(this.supportedLanguages).map(([code, name]) => `
                        <button class="lang-option ${code === this.currentLanguage ? 'active' : ''}" 
                                data-lang="${code}">
                            ${name}
                        </button>
                    `).join('')}
                </div>
            </div>
        `;
        
        container.innerHTML = html;
        
        // Add event listeners
        const toggleBtn = document.getElementById('language-toggle-btn');
        const dropdown = document.getElementById('language-dropdown');
        
        toggleBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            dropdown.classList.toggle('show');
        });
        
        document.addEventListener('click', () => {
            dropdown.classList.remove('show');
        });
        
        document.querySelectorAll('.lang-option').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                const lang = btn.getAttribute('data-lang');
                this.setLanguage(lang);
                dropdown.classList.remove('show');
                
                // Update active state
                document.querySelectorAll('.lang-option').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
            });
        });
    }
}

// Global instance
const languageToggle = new LanguageToggle();

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    languageToggle.init();
    languageToggle.createLanguageSelector();
});
