import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * HotelReservation.java
 * A simple CLI-based Hotel Reservation System (single-file)
 * Features:
 * - Search rooms by category and availability
 * - Book and cancel reservations
 * - Room categories: Standard, Deluxe, Suite
 * - Simulated payment (no real gateway) and booking details view
 * - Persistence via Java serialization (save/load datastore)
 *
 * Usage:
 *  javac HotelReservation.java
 *  java HotelReservation
 *
 */
public class HotelReservation {

    // -------------------- Models --------------------
    static enum Category { STANDARD, DELUXE, SUITE }

    static class Room implements Serializable {
        private static final long serialVersionUID = 1L;
        int id;
        Category category;
        double pricePerNight;

        public Room(int id, Category c, double price) {
            this.id = id;
            this.category = c;
            this.pricePerNight = price;
        }

        public String toString() {
            return String.format("Room %d - %s - %.2f per night", id, category, pricePerNight);
        }
    }

    static class Reservation implements Serializable {
        private static final long serialVersionUID = 1L;
        String reservationId;
        String guestName;
        int roomId;
        Date checkIn;
        Date checkOut;
        double totalAmount;
        boolean paid;

        public Reservation(String reservationId, String guestName, int roomId, Date checkIn, Date checkOut, double totalAmount, boolean paid) {
            this.reservationId = reservationId;
            this.guestName = guestName;
            this.roomId = roomId;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
            this.totalAmount = totalAmount;
            this.paid = paid;
        }

        public String toString() {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            return String.format("Reservation %s | Guest: %s | Room: %d | %s -> %s | Amount: %.2f | Paid: %s",
                    reservationId, guestName, roomId, df.format(checkIn), df.format(checkOut), totalAmount, paid ? "YES" : "NO");
        }
    }

    // -------------------- Data Store --------------------
    static class DataStore implements Serializable {
        private static final long serialVersionUID = 1L;
        Map<Integer, Room> rooms = new HashMap<>();
        Map<String, Reservation> reservations = new HashMap<>();

        // helper: find reservations for a room
        public List<Reservation> reservationsForRoom(int roomId) {
            List<Reservation> out = new ArrayList<>();
            for (Reservation r : reservations.values()) if (r.roomId == roomId) out.add(r);
            return out;
        }

        public void save(String filename) throws IOException {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
                oos.writeObject(this);
            }
        }

        public static DataStore load(String filename) throws IOException, ClassNotFoundException {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
                return (DataStore) ois.readObject();
            }
        }
    }

    // -------------------- Hotel Core --------------------
    static class HotelCore {
        DataStore store;
        Random rnd = new Random();

        public HotelCore(DataStore store) {
            this.store = store;
        }

        public List<Room> search(Category category, Date from, Date to) {
            List<Room> available = new ArrayList<>();
            for (Room r : store.rooms.values()) {
                if (category != null && r.category != category) continue;
                if (isRoomAvailable(r.id, from, to)) available.add(r);
            }
            return available;
        }

        public boolean isRoomAvailable(int roomId, Date from, Date to) {
            List<Reservation> res = store.reservationsForRoom(roomId);
            for (Reservation r : res) {
                if (datesOverlap(from, to, r.checkIn, r.checkOut)) return false;
            }
            return true;
        }

        private boolean datesOverlap(Date a1, Date a2, Date b1, Date b2) {
            // overlap if start < other end and other start < end
            return a1.before(b2) && b1.before(a2);
        }

        public Reservation book(String guestName, int roomId, Date from, Date to, boolean payNow) {
            if (!store.rooms.containsKey(roomId)) return null;
            if (!isRoomAvailable(roomId, from, to)) return null;
            long nights = (to.getTime() - from.getTime()) / (24L*60*60*1000);
            if (nights <= 0) nights = 1; // minimum 1
            double amount = nights * store.rooms.get(roomId).pricePerNight;
            String rid = generateReservationId();
            Reservation r = new Reservation(rid, guestName, roomId, from, to, amount, payNow);
            store.reservations.put(rid, r);
            return r;
        }

        public boolean cancel(String reservationId) {
            return store.reservations.remove(reservationId) != null;
        }

        public boolean pay(String reservationId) {
            Reservation r = store.reservations.get(reservationId);
            if (r == null) return false;
            if (r.paid) return true;
            // simulate payment
            r.paid = true;
            return true;
        }

        private String generateReservationId() {
            return "R" + Math.abs(rnd.nextInt()) + System.currentTimeMillis()%10000;
        }
    }

    // -------------------- CLI --------------------
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        DataStore store = null;
        String dataFile = "hotel_data.dat";

        // try load
        try {
            store = DataStore.load(dataFile);
            System.out.println("Loaded data from " + dataFile);
        } catch (Exception e) {
            store = seedData();
            System.out.println("Starting with seeded hotel data.");
        }

        HotelCore core = new HotelCore(store);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        boolean running = true;
        while (running) {
            System.out.println("\n--- HOTEL MENU ---");
            System.out.println("1) Show all rooms");
            System.out.println("2) Search available rooms");
            System.out.println("3) Book a room");
            System.out.println("4) Cancel reservation");
            System.out.println("5) View my reservations");
            System.out.println("6) Pay for reservation");
            System.out.println("7) Save data");
            System.out.println("8) Load data");
            System.out.println("9) Exit");
            System.out.print("Choose: ");
            String c = sc.nextLine().trim();
            try {
                switch (c) {
                    case "1":
                        for (Room r : store.rooms.values()) System.out.println("  " + r);
                        break;
                    case "2":
                        System.out.print("Category (STANDARD/DELUXE/SUITE or ALL): ");
                        String cat = sc.nextLine().trim().toUpperCase();
                        Category category = null;
                        if (!cat.equals("ALL") && !cat.isEmpty()) category = Category.valueOf(cat);
                        System.out.print("From (yyyy-MM-dd): ");
                        Date from = df.parse(sc.nextLine().trim());
                        System.out.print("To (yyyy-MM-dd): ");
                        Date to = df.parse(sc.nextLine().trim());
                        List<Room> avail = core.search(category, from, to);
                        if (avail.isEmpty()) System.out.println("No rooms available.");
                        else for (Room r : avail) System.out.println("  " + r);
                        break;
                    case "3":
                        System.out.print("Guest name: ");
                        String guest = sc.nextLine().trim();
                        System.out.print("Room id: ");
                        int rid = Integer.parseInt(sc.nextLine().trim());
                        System.out.print("From (yyyy-MM-dd): ");
                        Date f = df.parse(sc.nextLine().trim());
                        System.out.print("To (yyyy-MM-dd): ");
                        Date t = df.parse(sc.nextLine().trim());
                        System.out.print("Pay now? (yes/no): ");
                        boolean payNow = sc.nextLine().trim().equalsIgnoreCase("yes");
                        Reservation res = core.book(guest, rid, f, t, payNow);
                        if (res == null) System.out.println("Booking failed (room may not exist or is unavailable).");
                        else System.out.println("Booked: " + res);
                        break;
                    case "4":
                        System.out.print("Reservation id to cancel: ");
                        String cancelId = sc.nextLine().trim();
                        if (core.cancel(cancelId)) System.out.println("Cancelled."); else System.out.println("Cancel failed (id not found).");
                        break;
                    case "5":
                        System.out.print("Guest name to search reservations: ");
                        String name = sc.nextLine().trim();
                        boolean found = false;
                        for (Reservation r : store.reservations.values()) {
                            if (r.guestName.equalsIgnoreCase(name)) { System.out.println("  " + r); found = true; }
                        }
                        if (!found) System.out.println("No reservations found for " + name);
                        break;
                    case "6":
                        System.out.print("Reservation id to pay: ");
                        String payId = sc.nextLine().trim();
                        if (core.pay(payId)) System.out.println("Payment successful (simulated)."); else System.out.println("Payment failed (id not found).");
                        break;
                    case "7":
                        store.save(dataFile);
                        System.out.println("Saved to " + dataFile);
                        break;
                    case "8":
                        store = DataStore.load(dataFile);
                        core = new HotelCore(store);
                        System.out.println("Loaded from " + dataFile);
                        break;
                    case "9":
                        running = false; break;
                    default:
                        System.out.println("Unknown option.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        System.out.println("Goodbye!");
        sc.close();
    }

    private static DataStore seedData() {
        DataStore ds = new DataStore();
        // create 9 rooms: 4 standard, 3 deluxe, 2 suite
        int id = 101;
        for (int i = 0; i < 4; i++) ds.rooms.put(id, new Room(id++, Category.STANDARD, 3000 + i*100));
        for (int i = 0; i < 3; i++) ds.rooms.put(id, new Room(id++, Category.DELUXE, 5000 + i*200));
        for (int i = 0; i < 2; i++) ds.rooms.put(id, new Room(id++, Category.SUITE, 9000 + i*500));
        return ds;
    }
}
