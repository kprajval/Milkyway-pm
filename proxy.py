from flask import Flask, jsonify, request
from flask_cors import CORS
import yfinance as yf
from textblob import TextBlob
import requests

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

if __name__ == '__main__':
    app.run(port=3000)