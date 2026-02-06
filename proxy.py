from flask import Flask, jsonify, request
from flask_cors import CORS
import yfinance as yf
from textblob import TextBlob
import requests
import os
import google.generativeai as genai
from dotenv import load_dotenv

# Load environment variables
load_dotenv()

# Configure Gemini
GEN_API_KEY = os.getenv("GEMINI_API_KEY")
if GEN_API_KEY:
    genai.configure(api_key=GEN_API_KEY)

app = Flask(__name__)
CORS(app)

def get_sentiment(symbol):
    try:
        stock = yf.Ticker(symbol)
        news = stock.news
        if not news: return "Neutral"
        analysis_score = sum(TextBlob(n['title']).sentiment.polarity for n in news[:3])
        if analysis_score > 0.1: return "Bullish"
        elif analysis_score < -0.1: return "Bearish"
        return "Neutral"
    except: return "Neutral"

@app.route('/quote/<symbol>')
def get_quote(symbol):
    try:
        stock = yf.Ticker(symbol)
        info = stock.fast_info
        return jsonify({
            "symbol": symbol,
            "price": info.last_price,
            "high": info.day_high,
            "low": info.day_low,
            "prev_close": info.previous_close,
            "change_pct": ((info.last_price - info.previous_close) / info.previous_close) * 100 if info.previous_close else 0,
            "sentiment": get_sentiment(symbol)
        })
    except Exception as e:
        return jsonify({"error": str(e)}), 500
    
@app.route('/graph/<symbol>')
def get_graph_url(symbol):
    return jsonify({"url": f"https://finance.yahoo.com/quote/{symbol}/chart"})

@app.route('/history/<symbol>')
def get_history(symbol):
    try:
        stock = yf.Ticker(symbol)
        # Fetch last 7 days of data
        hist = stock.history(period="30d")
        data = {
            "labels": hist.index.strftime('%Y-%m-%d').tolist(),
            "prices": hist['Close'].tolist()
        }
        return jsonify(data)
    except Exception as e:
        return jsonify({"error": str(e)}), 500
    

@app.route('/search')
def search_stock():
    query = request.args.get('q')
    url = f"https://query2.finance.yahoo.com/v1/finance/search?q={query}"
    headers = {'User-Agent': 'Mozilla/5.0'}
    response = requests.get(url, headers=headers)
    data = response.json()
    # Filter for stocks only
    results = [{"symbol": q['symbol'], "name": q['shortname']} for q in data.get('quotes', []) if q.get('quoteType') == 'EQUITY']
    return jsonify(results)

@app.route('/info/<symbol>')
def get_info(symbol):
    stock = yf.Ticker(symbol)
    info = stock.info
    return jsonify({
        "symbol": symbol,
        "name": info.get('longName', 'N/A'),
        "industry": info.get('industry', 'N/A'),
        "website": info.get('website', 'N/A'),
        "description": info.get('longBusinessSummary', 'No description available.'),
        "price": info.get('currentPrice', info.get('regularMarketPrice', 0))
    })


@app.route('/fundamentals/<symbol>')
def get_fundamentals(symbol):
    try:
        stock = yf.Ticker(symbol)
        info = stock.info

        # Common fundamental metrics — handle missing values gracefully
        fundamentals = {
            "marketCap": info.get('marketCap'),
            "trailingPE": info.get('trailingPE'),
            "forwardPE": info.get('forwardPE'),
            "eps": info.get('trailingEps') or info.get('epsTrailingTwelveMonths'),
            "dividendYield": info.get('dividendYield'),
            "beta": info.get('beta'),
            "revenueTTM": info.get('totalRevenue') or info.get('revenueTrailing12Months'),
            "profitMargins": info.get('profitMargins'),
            "returnOnEquity": info.get('returnOnEquity'),
            "currentRatio": info.get('currentRatio'),
            "debtToEquity": info.get('debtToEquity')
        }

        # Clean keys and numeric normalization (if None left as None)
        return jsonify(fundamentals)
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/news/<symbol>')
def get_news(symbol):
    stock = yf.Ticker(symbol)
    news = stock.news
    formatted_news = []

    for item in news:
        # Access the 'content' block where the actual data resides
        content = item.get('content', {})
        
        # Extract summary or description
        summary = content.get('summary') or content.get('description')
        
        # Only include news if a summary/description is available
        if summary and summary.strip():
            formatted_news.append({
                "title": content.get('title'),
                "url": content.get('canonicalUrl', {}).get('url'),
                "publisher": content.get('provider', {}).get('displayName'),
                "time": content.get('pubDate'), # This is an ISO string
                "summary": summary
            })
            
    return jsonify(formatted_news)

def extract_symbol(text):
    # Simple heuristic: find first word that is all caps, 1-5 chars
    # In a real app, use NER or specific regex
    words = text.split()
    for word in words:
        clean_word = "".join(c for c in word if c.isalpha())
        if clean_word.isupper() and 1 <= len(clean_word) <= 5:
            return clean_word
    return None

# --- Tool Functions (Actual & Dummy for Schema) ---
def get_stock_price(symbol: str):
    """Get the current stock price for a given symbol."""
    try:
        stock = yf.Ticker(symbol)
        info = stock.fast_info
        return info.last_price if info.last_price else None
    except:
        return None

def get_news_summary(symbol: str):
    """Get top 3 news headlines for a stock."""
    try:
        stock = yf.Ticker(symbol)
        news = stock.news
        results = []
        for n in news[:3]:
            # Try robust extraction similar to get_news endpoint
            # Handling yfinance structure variations
            title = n.get('title')
            url = n.get('link')
            
            # If nested in content (some versions)
            if not title and 'content' in n:
                c = n.get('content', {})
                title = c.get('title')
                url = c.get('canonicalUrl', {}).get('url')
            
            if title:
                results.append({"title": title, "url": url})
        return results
    except:
        return []

# Dummy functions to generate schema for client-side actions
def buy_stock(symbol: str, quantity: int):
    """Buy a specific quantity of a stock.
    Args:
        symbol: The stock ticker (e.g., AAPL).
        quantity: The number of shares to buy.
    """
    return {"action": "buy", "symbol": symbol, "quantity": quantity}

def sell_stock(symbol: str, quantity: int):
    """Sell a specific quantity of a stock.
    Args:
        symbol: The stock ticker.
        quantity: The number of shares to sell.
    """
    return {"action": "sell", "symbol": symbol, "quantity": quantity}

def get_portfolio():
    """Get the user's current portfolio holdings and balance."""
    return {"action": "get_portfolio"}

# List of tools
my_tools = [buy_stock, sell_stock, get_news_summary, get_portfolio]

@app.route('/chat', methods=['POST'])
def chat_bot():
    data = request.json
    user_msg = data.get('message', '')
    portfolio_context = data.get('context', {}) 
    # History passed from frontend (list of {role: 'user'|'model', parts: 'text'})
    chat_history = data.get('history', [])
    
    if not user_msg:
        return jsonify({"response": "Please say something!"})

    system_instruction = f"""
    You are an intelligent Portfolio Manager Assistant.
    
    Current Portfolio Context: {portfolio_context}
    
    CAPABILITIES:
    1. **Trading**: Use `buy_stock` or `sell_stock` when the user explicitly asks.
    2. **News**: Use `get_news_summary` INSTANTLY if the user asks for "news", "headlines", or "updates" on a stock.
    3. **Portfolio**: Use `get_portfolio` if asked "what do I own".
    
    RULES:
    - If asked for news, DO NOT ask "Would you like me to get it?". JUST CALL THE TOOL.
    - Keep responses concise.
    """

    try:
        # Pass functions directly to tools
        model = genai.GenerativeModel('gemini-2.5-flash', tools=my_tools, system_instruction=system_instruction)
        
        # Initialize chat with history
        # We need to convert the frontend history format to Gemini's expected Content objects if providing proper history
        # OR, simpler for this "REST" style: Append history to the prompt text or use start_chat(history=...)
        # Let's try start_chat(history=...)
        
        formatted_history = []
        for msg in chat_history:
            role = "user" if msg['role'] == "user" else "model"
            formatted_history.append({"role": role, "parts": [msg['content']]})

        chat = model.start_chat(history=formatted_history)
        
        response = chat.send_message(user_msg)
        
        part = response.parts[0]
        
        # Check for function call
        if part.function_call:
            fc = part.function_call
            tool_name = fc.name
            args = fc.args
            
            # Construct Client Action
            if tool_name == "buy_stock":
                return jsonify({
                    "type": "action",
                    "action": "buy",
                    "data": {"symbol": args['symbol'].upper(), "quantity": int(args['quantity'])},
                    "message": f"Processing purchase of {int(args['quantity'])} {args['symbol'].upper()}..."
                })
            elif tool_name == "sell_stock":
                return jsonify({
                    "type": "action",
                    "action": "sell",
                    "data": {"symbol": args['symbol'].upper(), "quantity": int(args['quantity'])},
                    "message": f"Processing sale of {int(args['quantity'])} {args['symbol'].upper()}..."
                })
            elif tool_name == "get_portfolio":
                return jsonify({
                    "type": "action",
                    "action": "get_portfolio",
                    "data": {},
                    "message": "Fetching your portfolio..."
                })
            elif tool_name == "get_news_summary":
                # Execute locally
                news_items = get_news_summary(args['symbol'])
                news_prompt = f"Summarize these headlines for {args['symbol']}:\n{news_items}"
                model_n = genai.GenerativeModel('gemini-2.5-flash')
                final_res = model_n.generate_content(news_prompt)
                return jsonify({"response": final_res.text})

        return jsonify({"response": response.text})

    except Exception as e:
        print(f"Gemini Error: {e}")
        return jsonify({"response": f"Error: {str(e)}"})

if __name__ == '__main__':
    # Allow overriding port via PORT env var for flexible dev setups
    try:
        port = int(os.getenv('PORT', '3000'))
    except:
        port = 3000
    app.run(port=port)