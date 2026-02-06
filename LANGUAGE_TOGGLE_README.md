# 🌐 Language Toggle Feature - Experimental

## Overview
This is an **experimental proof-of-concept** for adding multi-language support to the Portfolio Manager application. It allows users to switch between English and vernacular Indian languages (Hindi, Tamil, Telugu).

## ⚠️ Important Notes
- **This is experimental code** - not production-ready
- **Completely isolated** - does not modify any business logic, APIs, or services
- **Easy to remove** - simply delete the files listed below and remove the script tag
- **Client-side only** - no backend changes required
- **Free** - no API costs, works offline

## 🎯 What It Does
- Adds a language toggle button in the Dashboard header
- Switches UI text between English ↔ Hindi ↔ Tamil ↔ Telugu
- Remembers user's language preference in browser localStorage
- Translates key UI elements on the Dashboard page

## 📁 Files Added

### Core Files (can be deleted to remove feature):
```
src/main/resources/static/
├── js/
│   └── language-toggle.js          # Translation logic
└── translations/
    ├── hi.json                     # Hindi translations
    ├── ta.json                     # Tamil translations
    └── te.json                     # Telugu translations
```

### Modified Files:
- `src/main/resources/templates/Dashboard.html` - Added CSS, toggle button, and script tag

## 🚀 How It Works

### 1. Translation System
The system uses a simple dictionary-based approach:
- Each language has a JSON file with translations
- UI elements are marked with `data-i18n` attributes
- JavaScript swaps text content based on selected language

### 2. Example Usage in HTML:
```html
<!-- Before -->
<span>Total Value</span>

<!-- After (marked for translation) -->
<span data-i18n="dashboard.total_value">Total Value</span>
```

### 3. Translation Files Structure:
```json
{
  "dashboard": {
    "title": "डैशबोर्ड",
    "total_value": "कुल मूल्य",
    "profit_loss": "लाभ/हानि"
  }
}
```

## 🎨 How to Use

1. **Start your application** (no changes to startup process)
2. **Navigate to Dashboard**
3. **Click the language button** in the top-right corner
4. **Select a language** from the dropdown
5. **Watch the UI text change** instantly

## ➕ Adding More Translations

### To add a new page:
1. Open the HTML template (e.g., `Performance.html`)
2. Add `data-i18n` attributes to translatable elements:
   ```html
   <h1 data-i18n="performance.title">Performance</h1>
   ```
3. Add translations to JSON files:
   ```json
   "performance": {
     "title": "प्रदर्शन"
   }
   ```
4. Include the script tag:
   ```html
   <script src="/js/language-toggle.js"></script>
   ```

### To add a new language:
1. Create a new JSON file: `src/main/resources/static/translations/xx.json`
2. Add language code to `language-toggle.js`:
   ```javascript
   this.supportedLanguages = {
       'en': 'English',
       'hi': 'हिंदी',
       'xx': 'New Language'  // Add here
   };
   ```

## 🗑️ How to Remove (Rollback)

If you want to disable or remove this feature completely:

### Option 1: Disable (Quick)
1. Open `Dashboard.html`
2. Comment out the script tag:
   ```html
   <!-- <script src="/js/language-toggle.js"></script> -->
   ```

### Option 2: Complete Removal
1. Delete the following files:
   ```
   src/main/resources/static/js/language-toggle.js
   src/main/resources/static/translations/hi.json
   src/main/resources/static/translations/ta.json
   src/main/resources/static/translations/te.json
   ```

2. In `Dashboard.html`, remove:
   - The CSS block marked "EXPERIMENTAL: Language Toggle Styles"
   - The `<div id="language-toggle-container"></div>` line
   - The script tag at the bottom
   - All `data-i18n` attributes from elements (optional)

3. Restart your application

## ⚙️ Technical Details

### Technologies Used:
- **Plain JavaScript** (no frameworks required)
- **CSS3** for styling
- **LocalStorage API** for persistence
- **JSON** for translation data

### Browser Compatibility:
- Chrome, Firefox, Safari, Edge (all modern versions)
- Requires JavaScript enabled

### Performance:
- Translation files are loaded once on page load
- Switching languages is instant (no network requests)
- Minimal impact on page load time (~5KB total)

## 🔄 Future Improvements (Ideas)

If you decide to make this production-ready, consider:

1. **Server-side rendering** with Spring i18n/MessageSource
2. **More comprehensive translations** (all pages, all elements)
3. **Professional translation review** (current translations are basic)
4. **RTL language support** (if adding Arabic, Hebrew, etc.)
5. **Translation management system** for easier updates
6. **Pluralization and formatting** (dates, numbers, currency)

## 🐛 Known Limitations

- Only Dashboard page is translated (proof-of-concept)
- Dynamic content (from API responses) is not translated
- Number formats and currency symbols remain in US format
- Chart labels are not translated
- Limited vocabulary (only key UI elements)

## 📞 Questions or Issues?

This is experimental code added for testing purposes. If you encounter issues:
1. Check browser console for errors
2. Verify translation files are accessible at `/translations/*.json`
3. Ensure JavaScript is enabled
4. Try clearing browser cache and localStorage

## ✅ Testing Checklist

- [ ] Language toggle button appears in Dashboard header
- [ ] Clicking button shows language dropdown
- [ ] Selecting Hindi changes text to Hindi
- [ ] Selecting Tamil changes text to Tamil
- [ ] Selecting Telugu changes text to Telugu
- [ ] Switching back to English restores original text
- [ ] Language preference is saved after page refresh
- [ ] No errors in browser console
- [ ] Existing functionality (charts, buttons) still works

---

**Remember**: This is an isolated experiment. The core application continues to work exactly as before, even if this feature is completely removed.
