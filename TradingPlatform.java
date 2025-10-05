import java.io.*;
import java.util.*;

/**
 * TradingPlatform.java
 * A simple simulated stock trading platform (single-file).
 * - OOP design using nested classes: Stock, User, Transaction, Market, Portfolio, DataStore
 * - CLI menu for viewing market data, buying/selling, viewing portfolio, saving/loading data
 * - Persists user portfolio to file using simple serialization (DataStore)
 *
 * How it works (high level):
 * - Market holds a set of Stocks with simulated prices.
 * - User has a Portfolio which tracks holdings and cash balance.
 * - Transactions record buys and sells and affect the portfolio.
 * - DataStore saves and loads the portfolio to a file.
 */
public class TradingPlatform {

    // ------------------- STOCK -------------------
    static class Stock implements Serializable {
        private static final long serialVersionUID = 1L;
        String symbol;
        String name;
        double price; // current price

        public Stock(String symbol, String name, double price) {
            this.symbol = symbol.toUpperCase();
            this.name = name;
            this.price = price;
        }

        public String toString() {
            return String.format("%s (%s): %.2f", symbol, name, price);
        }
    }

    // ------------------- TRANSACTION -------------------
    static class Transaction implements Serializable {
        private static final long serialVersionUID = 1L;
        Date time;
        String type; // BUY or SELL
        String symbol;
        int qty;
        double price; // price per share

        public Transaction(String type, String symbol, int qty, double price) {
            this.time = new Date();
            this.type = type;
            this.symbol = symbol.toUpperCase();
            this.qty = qty;
            this.price = price;
        }

        public String toString() {
            return String.format("%s | %s %d x %s @ %.2f", time, type, qty, symbol, price);
        }
    }

    // ------------------- PORTFOLIO -------------------
    static class Portfolio implements Serializable {
        private static final long serialVersionUID = 1L;
        double cash;
        Map<String, Integer> holdings = new HashMap<>(); // symbol -> qty
        List<Transaction> history = new ArrayList<>();

        public Portfolio(double startingCash) {
            this.cash = startingCash;
        }

        public void record(Transaction t) {
            history.add(t);
        }

        public void buy(String symbol, int qty, double price) {
            holdings.put(symbol, holdings.getOrDefault(symbol, 0) + qty);
            cash -= qty * price;
            record(new Transaction("BUY", symbol, qty, price));
        }

        public boolean sell(String symbol, int qty, double price) {
            int have = holdings.getOrDefault(symbol, 0);
            if (qty > have) return false;
            holdings.put(symbol, have - qty);
            if (holdings.get(symbol) == 0) holdings.remove(symbol);
            cash += qty * price;
            record(new Transaction("SELL", symbol, qty, price));
            return true;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Cash: %.2f\n", cash));
            sb.append("Holdings:\n");
            if (holdings.isEmpty()) sb.append("  (none)\n");
            else for (Map.Entry<String, Integer> e : holdings.entrySet()) sb.append("  " + e.getKey() + " : " + e.getValue() + "\n");
            return sb.toString();
        }
    }

    // ------------------- USER -------------------
    static class User implements Serializable {
        private static final long serialVersionUID = 1L;
        String username;
        Portfolio portfolio;

        public User(String username, double startingCash) {
            this.username = username;
            this.portfolio = new Portfolio(startingCash);
        }
    }

    // ------------------- MARKET -------------------
    static class Market {
        Map<String, Stock> stocks = new HashMap<>();
        Random rnd = new Random();

        public Market() {
            // seed some stocks
            add(new Stock("AAPL", "Apple Inc.", 175.00));
            add(new Stock("GOOGL", "Alphabet (Google)", 135.50));
            add(new Stock("MSFT", "Microsoft Corp.", 310.40));
            add(new Stock("TSLA", "Tesla Inc.", 240.00));
            add(new Stock("INFY", "Infosys Ltd.", 90.75));
            add(new Stock("TCS", "Tata Consultancy Services", 3200.00));
        }

        public void add(Stock s) {
            stocks.put(s.symbol, s);
        }

        public Stock get(String symbol) {
            return stocks.get(symbol.toUpperCase());
        }

        public Collection<Stock> all() {
            return stocks.values();
        }

        // simulate small price movements
        public void tick() {
            for (Stock s : stocks.values()) {
                double changePct = (rnd.nextDouble() - 0.5) * 0.04; // -2% to +2%
                s.price = Math.max(0.01, s.price * (1 + changePct));
            }
        }

        public double valueOf(String symbol, int qty) {
            Stock s = get(symbol);
            return s == null ? 0 : s.price * qty;
        }
    }

    // ------------------- DATASTORE (persistence) -------------------
    static class DataStore {
        public static void save(User user, String filename) throws IOException {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
                oos.writeObject(user);
            }
        }

        public static User load(String filename) throws IOException, ClassNotFoundException {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
                return (User) ois.readObject();
            }
        }
    }

    // ------------------- MAIN (CLI) -------------------
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Market market = new Market();
        System.out.println("Welcome to the Simple Stock Trading Simulator!");
        System.out.print("Enter your username: ");
        String username = sc.nextLine().trim();
        User user = null;

        // check for optional load filename via args
        if (args.length > 0) {
            String maybefile = args[0];
            try {
                user = DataStore.load(maybefile);
                System.out.println("Loaded user from file: " + maybefile);
            } catch (Exception e) {
                System.out.println("Failed to load file: " + e.getMessage());
            }
        }

        if (user == null) {
            System.out.print("Enter starting cash (e.g. 10000): ");
            double cash = 10000.0;
            try { cash = Double.parseDouble(sc.nextLine().trim()); } catch (Exception ignored) {}
            user = new User(username, cash);
        }

        boolean running = true;
        while (running) {
            System.out.println("\n--- MENU ---");
            System.out.println("1) Show market prices");
            System.out.println("2) Buy stock");
            System.out.println("3) Sell stock");
            System.out.println("4) Show portfolio & performance");
            System.out.println("5) Show transaction history");
            System.out.println("6) Simulate market tick (price update)");
            System.out.println("7) Save portfolio to file");
            System.out.println("8) Load portfolio from file");
            System.out.println("9) Exit");
            System.out.print("Choose an option: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    System.out.println("Market prices:");
                    for (Stock s : market.all()) System.out.println("  " + s);
                    break;
                case "2":
                    System.out.print("Enter symbol to buy: ");
                    String bsym = sc.nextLine().trim().toUpperCase();
                    Stock bstock = market.get(bsym);
                    if (bstock == null) { System.out.println("Unknown symbol."); break; }
                    System.out.print("Enter quantity: ");
                    int bq = parseIntSafe(sc.nextLine().trim());
                    double cost = bq * bstock.price;
                    if (bq <= 0) { System.out.println("Quantity must be positive."); break; }
                    if (cost > user.portfolio.cash) { System.out.println("Insufficient cash. Cost = " + cost); break; }
                    user.portfolio.buy(bsym, bq, bstock.price);
                    System.out.println("Bought " + bq + " of " + bsym + " for " + cost);
                    break;
                case "3":
                    System.out.print("Enter symbol to sell: ");
                    String ssym = sc.nextLine().trim().toUpperCase();
                    Stock sstock = market.get(ssym);
                    if (sstock == null) { System.out.println("Unknown symbol."); break; }
                    System.out.print("Enter quantity: ");
                    int sq = parseIntSafe(sc.nextLine().trim());
                    if (sq <= 0) { System.out.println("Quantity must be positive."); break; }
                    boolean ok = user.portfolio.sell(ssym, sq, sstock.price);
                    if (!ok) System.out.println("You do not have enough shares to sell.");
                    else System.out.println("Sold " + sq + " of " + ssym + " for " + (sq * sstock.price));
                    break;
                case "4":
                    System.out.println("Portfolio for " + user.username + ":");
                    System.out.print(user.portfolio);
                    System.out.println("Market value of holdings:");
                    double totalHoldings = 0.0;
                    for (Map.Entry<String, Integer> e : user.portfolio.holdings.entrySet()) {
                        double val = market.valueOf(e.getKey(), e.getValue());
                        totalHoldings += val;
                        System.out.println("  " + e.getKey() + " -> " + e.getValue() + " shares, market value = " + String.format("%.2f", val));
                    }
                    double net = user.portfolio.cash + totalHoldings;
                    System.out.println(String.format("Total portfolio value (cash + holdings) = %.2f", net));
                    break;
                case "5":
                    System.out.println("Transaction history:");
                    if (user.portfolio.history.isEmpty()) System.out.println("  (no transactions)");
                    else for (Transaction t : user.portfolio.history) System.out.println("  " + t);
                    break;
                case "6":
                    market.tick();
                    System.out.println("Market tick simulated (prices updated).");
                    break;
                case "7":
                    System.out.print("Enter filename to save (e.g. user.dat): ");
                    String file = sc.nextLine().trim();
                    try {
                        DataStore.save(user, file);
                        System.out.println("Saved to " + file);
                    } catch (Exception e) { System.out.println("Failed to save: " + e.getMessage()); }
                    break;
                case "8":
                    System.out.print("Enter filename to load (e.g. user.dat): ");
                    String f2 = sc.nextLine().trim();
                    try {
                        User loaded = DataStore.load(f2);
                        user = loaded;
                        System.out.println("Loaded user: " + user.username);
                    } catch (Exception e) { System.out.println("Failed to load: " + e.getMessage()); }
                    break;
                case "9":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }

        System.out.println("Goodbye!");
        sc.close();
    }

    private static int parseIntSafe(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return -1; }
    }
}
