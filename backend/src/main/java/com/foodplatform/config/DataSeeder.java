package com.foodplatform.config;

import com.foodplatform.model.*;
import com.foodplatform.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final PromoCodeRepository promoCodeRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;

    private final Random random = new Random(42);

    @Override
    public void run(String... args) {
        try {
        if (userRepository.count() > 0) {
            log.info("Database already seeded, skipping.");
            return;
        }

        log.info("Seeding database...");

        // ── Users ──────────────────────────────────────────────
        User admin = createUser("Admin User", "admin@food.com", "Admin123!", User.Role.ADMIN, "100 Admin Plaza");

        User[] owners = {
            createUser("Mario Rossi", "owner1@food.com", "Owner123!", User.Role.RESTAURANT_OWNER, "123 Pasta Lane"),
            createUser("Yuki Tanaka", "owner2@food.com", "Owner123!", User.Role.RESTAURANT_OWNER, "456 Sakura St"),
            createUser("James Wilson", "owner3@food.com", "Owner123!", User.Role.RESTAURANT_OWNER, "789 Grill Ave"),
            createUser("Sofia Garcia", "owner4@food.com", "Owner123!", User.Role.RESTAURANT_OWNER, "101 Taco Blvd"),
            createUser("Ahmed Hassan", "owner5@food.com", "Owner123!", User.Role.RESTAURANT_OWNER, "202 Spice Rd"),
            createUser("Chen Wei", "owner6@food.com", "Owner123!", User.Role.RESTAURANT_OWNER, "303 Noodle Way"),
            createUser("Priya Patel", "owner7@food.com", "Owner123!", User.Role.RESTAURANT_OWNER, "404 Bistro Ln"),
            createUser("Lars Andersen", "owner8@food.com", "Owner123!", User.Role.RESTAURANT_OWNER, "505 Harbor Dr"),
            createUser("Maria Santos", "owner9@food.com", "Owner123!", User.Role.RESTAURANT_OWNER, "606 Flame Ave"),
            createUser("Kenji Nakamura", "owner10@food.com", "Owner123!", User.Role.RESTAURANT_OWNER, "707 Seoul St"),
            createUser("Emma Johnson", "owner11@food.com", "Owner123!", User.Role.RESTAURANT_OWNER, "808 Green Ln"),
        };

        User[] customers = {
            createUser("John Customer", "customer@food.com", "Customer123!", User.Role.CUSTOMER, "456 Oak Ave, Apt 2B"),
            createUser("Sarah Miller", "customer2@food.com", "Customer123!", User.Role.CUSTOMER, "789 Pine St"),
            createUser("David Chen", "customer3@food.com", "Customer123!", User.Role.CUSTOMER, "101 Elm Blvd"),
            createUser("Emily Watson", "customer4@food.com", "Customer123!", User.Role.CUSTOMER, "222 Maple Dr"),
            createUser("Robert Taylor", "customer5@food.com", "Customer123!", User.Role.CUSTOMER, "333 Cedar Ct"),
            createUser("Jennifer Lee", "customer6@food.com", "Customer123!", User.Role.CUSTOMER, "444 Birch Way"),
            createUser("Michael Brown", "customer7@food.com", "Customer123!", User.Role.CUSTOMER, "555 Willow Rd"),
            createUser("Amanda White", "customer8@food.com", "Customer123!", User.Role.CUSTOMER, "666 Aspen Ln"),
            createUser("Christopher Davis", "customer9@food.com", "Customer123!", User.Role.CUSTOMER, "777 Spruce Ave"),
            createUser("Rachel Thompson", "customer10@food.com", "Customer123!", User.Role.CUSTOMER, "888 Redwood Blvd"),
        };

        User driver1 = createUser("Mike Driver", "driver@food.com", "Driver123!", User.Role.DELIVERY_DRIVER, "900 Express Way");
        User driver2 = createUser("Lisa Express", "driver2@food.com", "Driver123!", User.Role.DELIVERY_DRIVER, "901 Express Way");
        User[] drivers = { driver1, driver2 };

        // ── Restaurants ────────────────────────────────────────
        String[][] restaurantData = {
            { "Mario's Trattoria", "Authentic Italian cuisine with homemade pasta and wood-fired pizzas", "Italian", "123 Pasta Lane, Little Italy", "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=800", "4.7" },
            { "Tokyo Omakase", "Premium Japanese dining featuring fresh sushi, ramen, and traditional dishes", "Japanese", "456 Sakura Street, Japantown", "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=800", "4.8" },
            { "The Prime Cut", "Premium steakhouse serving the finest aged beef with classic sides", "American", "789 Grill Avenue, Downtown", "https://images.unsplash.com/photo-1544025162-d76694265947?w=800", "4.6" },
            { "Casa de Sofia", "Traditional Mexican flavors with a modern twist, from tacos to enchiladas", "Mexican", "101 Taco Boulevard, Midtown", "https://images.unsplash.com/photo-1565299585323-38d6b0865b47?w=800", "4.5" },
            { "Taj Mahal Palace", "Rich curries, tandoori specialties, and aromatic biryanis from Northern India", "Indian", "202 Spice Road, Curry Hill", "https://images.unsplash.com/photo-1585937421612-70a008356fbe?w=800", "4.7" },
            { "Bangkok Street", "Authentic Thai street food — pad thai, curries, and tropical desserts", "Thai", "303 Noodle Way, East Side", "https://images.unsplash.com/photo-1562565652-a0d8f0c59eb4?w=800", "4.4" },
            { "Le Petit Bistro", "Classic French bistro fare — escargot, coq au vin, and fine pastries", "French", "404 Bistro Lane, Uptown", "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=800", "4.8" },
            { "Mediterranean Breeze", "Fresh Mediterranean cuisine with hummus, kebabs, and grilled seafood", "Mediterranean", "505 Harbor Drive, Waterfront", "https://images.unsplash.com/photo-1544025162-d76694265947?w=800", "4.5" },
            { "Fogo de Chao", "Brazilian churrascaria with slow-grilled meats and tropical sides", "Brazilian", "606 Flame Avenue, South End", "https://images.unsplash.com/photo-1558030006-450675393462?w=800", "4.6" },
            { "Seoul Kitchen", "Korean BBQ, bibimbap, kimchi stew, and K-fried chicken", "Korean", "707 Seoul Street, Koreatown", "https://images.unsplash.com/photo-1590301157890-4810ed352733?w=800", "4.3" },
            { "Green Garden", "Plant-based gourmet — creative vegan bowls, burgers, and smoothies", "Vegan", "808 Green Lane, Arts District", "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=800", "4.4" },
        };

        Restaurant[] restaurants = new Restaurant[11];
        for (int i = 0; i < 11; i++) {
            restaurants[i] = createRestaurant(owners[i].getId(), restaurantData[i][0], restaurantData[i][1],
                    restaurantData[i][2], restaurantData[i][3], restaurantData[i][4], true,
                    Double.parseDouble(restaurantData[i][5]));
        }

        // ── Menu Items ─────────────────────────────────────────
        List<List<MenuItem>> allMenus = new ArrayList<>();
        allMenus.add(seedMenu(restaurants[0].getId(), new String[][] {
            { "Margherita Pizza", "Fresh mozzarella, tomato sauce, basil", "14.99", "Pizza" },
            { "Pepperoni Pizza", "Classic pepperoni with mozzarella", "16.99", "Pizza" },
            { "Quattro Formaggi", "Four cheese blend pizza", "17.99", "Pizza" },
            { "Diavola Pizza", "Spicy salami, chili flakes", "17.49", "Pizza" },
            { "Spaghetti Carbonara", "Creamy egg sauce, pancetta, parmesan", "15.99", "Pasta" },
            { "Fettuccine Alfredo", "Rich cream sauce with parmesan", "14.99", "Pasta" },
            { "Penne Arrabbiata", "Spicy tomato sauce with garlic", "13.99", "Pasta" },
            { "Lasagna Bolognese", "Layers of pasta, meat sauce, bechamel", "16.99", "Pasta" },
            { "Tiramisu", "Espresso-soaked ladyfingers", "9.99", "Desserts" },
            { "Panna Cotta", "Vanilla cream with berry coulis", "8.99", "Desserts" },
            { "Cannoli", "Crispy shells with ricotta cream", "7.99", "Desserts" },
        }));
        allMenus.add(seedMenu(restaurants[1].getId(), new String[][] {
            { "Salmon Nigiri (6pc)", "Fresh Atlantic salmon over rice", "16.99", "Sushi" },
            { "Dragon Roll", "Shrimp tempura, avocado, eel sauce", "18.99", "Sushi" },
            { "Rainbow Roll", "California roll with assorted sashimi", "19.99", "Sushi" },
            { "Tuna Sashimi", "Premium bluefin tuna, 8 slices", "22.99", "Sushi" },
            { "Tonkotsu Ramen", "Rich pork bone broth, chashu, egg", "16.99", "Ramen" },
            { "Miso Ramen", "Soybean paste broth, corn, butter", "15.99", "Ramen" },
            { "Shoyu Ramen", "Soy sauce broth, bamboo shoots", "14.99", "Ramen" },
            { "Chicken Teriyaki", "Grilled chicken with teriyaki glaze", "15.99", "Hot Dishes" },
            { "Tempura Platter", "Assorted shrimp and vegetable tempura", "17.99", "Hot Dishes" },
            { "Katsu Curry", "Breaded pork cutlet with curry", "16.49", "Hot Dishes" },
            { "Mochi Ice Cream", "Three flavors of mochi", "8.99", "Desserts" },
        }));
        allMenus.add(seedMenu(restaurants[2].getId(), new String[][] {
            { "Ribeye Steak 12oz", "Prime aged ribeye, herb butter", "38.99", "Steaks" },
            { "Filet Mignon 8oz", "Center-cut tenderloin, peppercorn sauce", "42.99", "Steaks" },
            { "New York Strip 14oz", "Classic strip steak, chimichurri", "36.99", "Steaks" },
            { "T-Bone Steak 16oz", "Strip and tenderloin, charbroiled", "44.99", "Steaks" },
            { "Wagyu Burger", "Wagyu beef, brioche bun, truffle aioli", "24.99", "Steaks" },
            { "Caesar Salad", "Romaine, parmesan, croutons", "11.99", "Sides" },
            { "Loaded Baked Potato", "Sour cream, cheddar, bacon", "8.99", "Sides" },
            { "Creamed Spinach", "Steakhouse style with nutmeg", "9.99", "Sides" },
            { "Chocolate Lava Cake", "Molten center, vanilla ice cream", "12.99", "Desserts" },
            { "Cheesecake", "Classic NY cheesecake, berry compote", "11.99", "Desserts" },
        }));
        allMenus.add(seedMenu(restaurants[3].getId(), new String[][] {
            { "Carne Asada Tacos", "Grilled steak, cilantro, onion, lime", "13.99", "Tacos" },
            { "Al Pastor Tacos", "Marinated pork, pineapple, salsa verde", "12.99", "Tacos" },
            { "Fish Tacos", "Beer-battered fish, cabbage slaw", "14.99", "Tacos" },
            { "Chicken Burrito", "Rice, beans, cheese, guacamole", "14.99", "Burritos" },
            { "Carnitas Burrito", "Slow-braised pork, pico de gallo", "15.99", "Burritos" },
            { "Cheese Enchiladas", "Corn tortillas, red sauce, queso", "13.49", "Mains" },
            { "Chicken Quesadilla", "Grilled chicken, melted cheese", "12.99", "Mains" },
            { "Guacamole & Chips", "Fresh avocado, lime, cilantro", "9.99", "Sides" },
            { "Elote", "Mexican street corn with cotija", "7.99", "Sides" },
            { "Churros", "Cinnamon sugar with chocolate sauce", "8.99", "Desserts" },
        }));
        allMenus.add(seedMenu(restaurants[4].getId(), new String[][] {
            { "Butter Chicken", "Creamy tomato curry with tender chicken", "16.99", "Curries" },
            { "Lamb Rogan Josh", "Slow-cooked lamb in Kashmiri spices", "18.99", "Curries" },
            { "Palak Paneer", "Spinach curry with cottage cheese", "14.99", "Curries" },
            { "Chicken Biryani", "Fragrant basmati rice with spiced chicken", "17.99", "Rice" },
            { "Vegetable Biryani", "Mixed vegetable basmati rice", "14.99", "Rice" },
            { "Tandoori Chicken", "Clay oven roasted with yogurt marinade", "16.49", "Tandoor" },
            { "Naan Bread", "Butter naan fresh from tandoor", "4.99", "Sides" },
            { "Garlic Naan", "Garlic-infused naan bread", "5.99", "Sides" },
            { "Samosa (3pc)", "Crispy pastry with spiced potato filling", "7.99", "Sides" },
            { "Gulab Jamun", "Rose-scented milk dumplings", "6.99", "Desserts" },
            { "Mango Lassi", "Sweet yogurt mango smoothie", "5.99", "Desserts" },
        }));
        allMenus.add(seedMenu(restaurants[5].getId(), new String[][] {
            { "Pad Thai", "Rice noodles, shrimp, peanuts, tamarind", "15.99", "Noodles" },
            { "Drunken Noodles", "Wide noodles, basil, chili, vegetables", "14.99", "Noodles" },
            { "Green Curry", "Coconut curry with bamboo and basil", "16.99", "Curries" },
            { "Red Curry", "Coconut curry with bell peppers", "15.99", "Curries" },
            { "Massaman Curry", "Rich peanut curry with potato", "16.49", "Curries" },
            { "Tom Yum Soup", "Spicy lemongrass shrimp soup", "12.99", "Soups" },
            { "Mango Sticky Rice", "Sweet coconut rice with mango", "9.99", "Desserts" },
            { "Thai Iced Tea", "Sweet creamy tea with condensed milk", "5.99", "Drinks" },
            { "Spring Rolls (4pc)", "Crispy vegetable spring rolls", "8.99", "Sides" },
            { "Satay Chicken", "Grilled skewers with peanut sauce", "11.99", "Sides" },
        }));
        allMenus.add(seedMenu(restaurants[6].getId(), new String[][] {
            { "Escargot", "Garlic herb butter baked snails", "16.99", "Starters" },
            { "French Onion Soup", "Caramelized onion, gruyere crouton", "12.99", "Starters" },
            { "Coq au Vin", "Braised chicken in red wine sauce", "24.99", "Mains" },
            { "Duck Confit", "Slow-cooked duck leg, lentils", "28.99", "Mains" },
            { "Beef Bourguignon", "Slow-braised beef in Burgundy wine", "26.99", "Mains" },
            { "Steak Frites", "Pan-seared steak, truffle fries", "32.99", "Mains" },
            { "Ratatouille", "Provencal vegetable medley", "18.99", "Mains" },
            { "Creme Brulee", "Vanilla custard, caramelized sugar", "10.99", "Desserts" },
            { "Chocolate Souffle", "Warm dark chocolate souffle", "12.99", "Desserts" },
            { "Tarte Tatin", "Caramelized apple tart, chantilly", "11.99", "Desserts" },
        }));
        allMenus.add(seedMenu(restaurants[7].getId(), new String[][] {
            { "Hummus Platter", "Classic hummus with warm pita", "10.99", "Starters" },
            { "Falafel Plate", "Crispy falafel, tahini, pickles", "13.99", "Starters" },
            { "Lamb Kebab", "Grilled lamb with rice and salad", "19.99", "Grills" },
            { "Chicken Shawarma", "Spiced chicken wrap with garlic sauce", "14.99", "Grills" },
            { "Grilled Sea Bass", "Lemon herb sea bass, roasted veg", "24.99", "Grills" },
            { "Mixed Grill Platter", "Assorted grilled meats and sides", "28.99", "Grills" },
            { "Tabbouleh", "Parsley, bulgur, tomato, lemon salad", "9.99", "Sides" },
            { "Fattoush Salad", "Crispy pita, mixed greens, sumac", "10.99", "Sides" },
            { "Baklava", "Layered phyllo, pistachios, honey", "8.99", "Desserts" },
            { "Turkish Delight", "Rose and pistachio lokum", "7.99", "Desserts" },
        }));
        allMenus.add(seedMenu(restaurants[8].getId(), new String[][] {
            { "Picanha", "Prime cap of rump, grilled over charcoal", "32.99", "Meats" },
            { "Beef Ribs", "Slow-smoked beef short ribs", "28.99", "Meats" },
            { "Chicken Hearts", "Grilled chicken hearts on skewers", "14.99", "Meats" },
            { "Linguica Sausage", "Brazilian smoked pork sausage", "16.99", "Meats" },
            { "Pao de Queijo", "Brazilian cheese bread rolls", "8.99", "Sides" },
            { "Feijoada", "Black bean stew with pork", "18.99", "Mains" },
            { "Farofa", "Toasted cassava flour with bacon", "6.99", "Sides" },
            { "Vinaigrette", "Brazilian tomato-onion salsa", "5.99", "Sides" },
            { "Brigadeiro", "Chocolate truffle bites", "7.99", "Desserts" },
            { "Acai Bowl", "Frozen acai with granola and banana", "12.99", "Desserts" },
        }));
        allMenus.add(seedMenu(restaurants[9].getId(), new String[][] {
            { "Korean BBQ Beef", "Bulgogi marinated beef with rice", "18.99", "BBQ" },
            { "Spicy Pork BBQ", "Gochujang marinated pork belly", "16.99", "BBQ" },
            { "Bibimbap", "Mixed rice bowl, vegetables, egg, gochujang", "15.99", "Bowls" },
            { "Kimchi Jjigae", "Spicy kimchi stew with pork", "14.99", "Stews" },
            { "Sundubu Jjigae", "Soft tofu stew with seafood", "15.99", "Stews" },
            { "Korean Fried Chicken", "Crispy double-fried, soy garlic", "16.99", "Chicken" },
            { "Japchae", "Sweet potato noodles, vegetables", "13.99", "Sides" },
            { "Kimchi", "Traditional fermented cabbage", "5.99", "Sides" },
            { "Tteokbokki", "Spicy rice cakes in gochujang", "11.99", "Sides" },
            { "Hotteok", "Sweet filled Korean pancake", "7.99", "Desserts" },
        }));
        allMenus.add(seedMenu(restaurants[10].getId(), new String[][] {
            { "Buddha Bowl", "Quinoa, roasted veg, tahini dressing", "15.99", "Bowls" },
            { "Jackfruit Tacos", "Pulled jackfruit, slaw, avocado", "13.99", "Mains" },
            { "Beyond Burger", "Plant-based patty, vegan cheese", "16.99", "Mains" },
            { "Cauliflower Steak", "Roasted with chimichurri and nuts", "17.99", "Mains" },
            { "Mushroom Risotto", "Creamy arborio with wild mushrooms", "16.49", "Mains" },
            { "Avocado Toast", "Sourdough, smashed avocado, seeds", "12.99", "Brunch" },
            { "Acai Smoothie Bowl", "Acai, banana, granola, berries", "13.99", "Brunch" },
            { "Sweet Potato Fries", "Crispy with chipotle aioli", "8.99", "Sides" },
            { "Vegan Brownie", "Rich chocolate with walnuts", "7.99", "Desserts" },
            { "Coconut Panna Cotta", "Coconut cream with mango puree", "9.99", "Desserts" },
        }));

        // ── Promo Codes ────────────────────────────────────────
        seedPromoCodes(admin.getId(), restaurants[0].getId());

        // ── Bulk Orders ────────────────────────────────────────
        String[] promos = { "WELCOME10", "SAVE5", "BIGORDER10" };
        Order.OrderStatus[] activeStatuses = { Order.OrderStatus.PENDING, Order.OrderStatus.CONFIRMED,
                Order.OrderStatus.PREPARING, Order.OrderStatus.READY, Order.OrderStatus.PICKED_UP, Order.OrderStatus.ARRIVED };
        Order.PaymentMethod[] paymentMethods = Order.PaymentMethod.values();

        LocalDateTime now = LocalDateTime.now();
        int totalOrders = 0;
        int totalReviews = 0;

        for (int ri = 0; ri < 11; ri++) {
            Restaurant restaurant = restaurants[ri];
            List<MenuItem> menu = allMenus.get(ri);
            int ordersForRestaurant = 150 + random.nextInt(30); // 150-179 per restaurant

            for (int oi = 0; oi < ordersForRestaurant; oi++) {
                User customer = customers[random.nextInt(customers.length)];
                int daysAgo = random.nextInt(90);
                int hour = 11 + random.nextInt(10); // 11am-8pm
                int minute = random.nextInt(60);
                LocalDateTime createdAt = now.minusDays(daysAgo).withHour(hour).withMinute(minute);

                // Pick 1-4 random items
                int itemCount = 1 + random.nextInt(4);
                List<Order.OrderItem> items = new ArrayList<>();
                Set<Integer> picked = new HashSet<>();
                for (int ii = 0; ii < itemCount && ii < menu.size(); ii++) {
                    int idx;
                    do { idx = random.nextInt(menu.size()); } while (picked.contains(idx));
                    picked.add(idx);
                    MenuItem mi = menu.get(idx);
                    items.add(new Order.OrderItem(mi.getId(), mi.getName(), mi.getPrice(), 1 + random.nextInt(3)));
                }

                double total = items.stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum();

                // Status distribution
                Order.OrderStatus status;
                double statusRoll = random.nextDouble();
                if (statusRoll < 0.55) status = Order.OrderStatus.DELIVERED;
                else if (statusRoll < 0.63) status = Order.OrderStatus.CANCELLED;
                else if (statusRoll < 0.69) status = Order.OrderStatus.PENDING;
                else if (statusRoll < 0.75) status = Order.OrderStatus.CONFIRMED;
                else if (statusRoll < 0.81) status = Order.OrderStatus.PREPARING;
                else if (statusRoll < 0.87) status = Order.OrderStatus.READY;
                else if (statusRoll < 0.92) status = Order.OrderStatus.PICKED_UP;
                else if (statusRoll < 0.96) status = Order.OrderStatus.ARRIVED;
                else status = Order.OrderStatus.DELIVERED; // extra delivered

                // Promo code ~15%
                String promoCode = null;
                double discount = 0;
                if (random.nextDouble() < 0.15 && total > 20) {
                    promoCode = promos[random.nextInt(promos.length)];
                    discount = promoCode.equals("SAVE5") ? 5.0 : Math.round(total * 0.10 * 100.0) / 100.0;
                }

                Order.PaymentMethod pm = paymentMethods[random.nextInt(paymentMethods.length)];
                Order.PaymentStatus ps;
                if (status == Order.OrderStatus.DELIVERED) ps = Order.PaymentStatus.SIMULATED_PAID;
                else if (status == Order.OrderStatus.CANCELLED) ps = Order.PaymentStatus.PENDING;
                else ps = random.nextDouble() < 0.7 ? Order.PaymentStatus.SIMULATED_PAID : Order.PaymentStatus.PENDING;

                String driverId = null;
                if (status == Order.OrderStatus.PICKED_UP || status == Order.OrderStatus.ARRIVED || status == Order.OrderStatus.DELIVERED) {
                    driverId = drivers[random.nextInt(drivers.length)].getId();
                }

                Order order = new Order();
                order.setCustomerId(customer.getId());
                order.setRestaurantId(restaurant.getId());
                order.setItems(items);
                order.setStatus(status);
                order.setTotalAmount(Math.round((total - discount) * 100.0) / 100.0);
                order.setDeliveryAddress(customer.getAddress());
                order.setPaymentStatus(ps);
                order.setPaymentMethod(pm);
                order.setDriverId(driverId);
                order.setPromoCode(promoCode);
                order.setDiscountAmount(discount);
                order.setCreatedAt(createdAt);
                order.setUpdatedAt(createdAt.plusMinutes(15 + random.nextInt(45)));
                orderRepository.save(order);
                totalOrders++;

                // Reviews for ~40% of delivered orders
                if (status == Order.OrderStatus.DELIVERED && random.nextDouble() < 0.40) {
                    int rating = pickRating();
                    String comment = pickComment(rating, restaurant.getCuisineType());
                    double sentiment = 0.1 + (rating - 1) * 0.2 + random.nextDouble() * 0.1;
                    sentiment = Math.round(sentiment * 100.0) / 100.0;
                    if (sentiment > 1.0) sentiment = 0.97;

                    Review review = new Review();
                    review.setCustomerId(customer.getId());
                    review.setCustomerName(customer.getName());
                    review.setRestaurantId(restaurant.getId());
                    review.setOrderId(order.getId());
                    review.setRating(rating);
                    review.setComment(comment);
                    review.setSentimentScore(sentiment);
                    review.setCreatedAt(createdAt.plusHours(1 + random.nextInt(24)));
                    reviewRepository.save(review);
                    totalReviews++;
                }
            }
        }

        // ── Carts ──────────────────────────────────────────────
        Cart cart1 = new Cart();
        cart1.setCustomerId(customers[1].getId());
        cart1.setRestaurantId(restaurants[1].getId());
        List<Cart.CartItem> cItems1 = new ArrayList<>();
        cItems1.add(new Cart.CartItem(allMenus.get(1).get(0).getId(), allMenus.get(1).get(0).getName(), allMenus.get(1).get(0).getPrice(), 2));
        cItems1.add(new Cart.CartItem(allMenus.get(1).get(4).getId(), allMenus.get(1).get(4).getName(), allMenus.get(1).get(4).getPrice(), 1));
        cart1.setItems(cItems1);
        cartRepository.save(cart1);

        Cart cart2 = new Cart();
        cart2.setCustomerId(customers[2].getId());
        cart2.setRestaurantId(restaurants[2].getId());
        List<Cart.CartItem> cItems2 = new ArrayList<>();
        cItems2.add(new Cart.CartItem(allMenus.get(2).get(0).getId(), allMenus.get(2).get(0).getName(), allMenus.get(2).get(0).getPrice(), 1));
        cItems2.add(new Cart.CartItem(allMenus.get(2).get(5).getId(), allMenus.get(2).get(5).getName(), allMenus.get(2).get(5).getPrice(), 1));
        cart2.setItems(cItems2);
        cartRepository.save(cart2);

        log.info("Database seeded: {} orders, {} reviews, 11 restaurants, 10 customers", totalOrders, totalReviews);
        } catch (Exception e) {
            log.warn("DataSeeder encountered an error (may be normal on Azure Cosmos DB): {}", e.getMessage());
        }
    }

    // ── Helpers ──────────────────────────────────────────────

    private User createUser(String name, String email, String password, User.Role role, String address) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        user.setAddress(address);
        user.setPhone("+1" + (1000000000L + random.nextInt(900000000)));
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private Restaurant createRestaurant(String ownerId, String name, String description,
                                         String cuisine, String address, String imageUrl,
                                         boolean isOpen, double rating) {
        Restaurant restaurant = new Restaurant();
        restaurant.setOwnerId(ownerId);
        restaurant.setName(name);
        restaurant.setDescription(description);
        restaurant.setCuisineType(cuisine);
        restaurant.setAddress(address);
        restaurant.setImageUrl(imageUrl);
        restaurant.setOpen(isOpen);
        restaurant.setRating(rating);
        restaurant.setCreatedAt(LocalDateTime.now());
        return restaurantRepository.save(restaurant);
    }

    private List<MenuItem> seedMenu(String restaurantId, String[][] items) {
        List<MenuItem> result = new ArrayList<>();
        for (String[] item : items) {
            MenuItem mi = new MenuItem();
            mi.setRestaurantId(restaurantId);
            mi.setName(item[0]);
            mi.setDescription(item[1]);
            mi.setPrice(Double.parseDouble(item[2]));
            mi.setCategory(item[3]);
            mi.setAvailable(true);
            result.add(menuItemRepository.save(mi));
        }
        return result;
    }

    private void seedPromoCodes(String adminId, String marioRestaurantId) {
        createPromoCode("WELCOME10", PromoCode.DiscountType.PERCENTAGE, 10, 15, 100, 0, true, null, adminId, LocalDateTime.now().plusMonths(3));
        createPromoCode("SAVE5", PromoCode.DiscountType.FIXED_AMOUNT, 5, 25, 50, 0, true, null, adminId, LocalDateTime.now().plusMonths(1));
        createPromoCode("EXPIRED20", PromoCode.DiscountType.PERCENTAGE, 20, 10, 200, 5, true, null, adminId, LocalDateTime.now().minusDays(30));
        createPromoCode("MAXED15", PromoCode.DiscountType.PERCENTAGE, 15, 20, 10, 10, true, null, adminId, LocalDateTime.now().plusMonths(2));
        createPromoCode("INACTIVE10", PromoCode.DiscountType.FIXED_AMOUNT, 10, 15, 50, 3, false, null, adminId, LocalDateTime.now().plusMonths(6));
        createPromoCode("MARIO20", PromoCode.DiscountType.PERCENTAGE, 20, 30, 100, 12, true, marioRestaurantId, adminId, LocalDateTime.now().plusMonths(2));
        createPromoCode("BIGORDER10", PromoCode.DiscountType.PERCENTAGE, 10, 50, 200, 0, true, null, adminId, LocalDateTime.now().plusMonths(4));
    }

    private void createPromoCode(String code, PromoCode.DiscountType type, double value,
                                  double minOrder, int maxUsage, int currentUsage,
                                  boolean isActive, String restaurantId, String createdBy, LocalDateTime expiresAt) {
        PromoCode promo = new PromoCode();
        promo.setCode(code);
        promo.setDiscountType(type);
        promo.setDiscountValue(value);
        promo.setMinimumOrderAmount(minOrder);
        promo.setMaxUsageCount(maxUsage);
        promo.setCurrentUsageCount(currentUsage);
        promo.setActive(isActive);
        promo.setRestaurantId(restaurantId);
        promo.setCreatedBy(createdBy);
        promo.setExpiresAt(expiresAt);
        promo.setCreatedAt(LocalDateTime.now());
        promoCodeRepository.save(promo);
    }

    private int pickRating() {
        double r = random.nextDouble();
        if (r < 0.40) return 5;
        if (r < 0.70) return 4;
        if (r < 0.85) return 3;
        if (r < 0.95) return 2;
        return 1;
    }

    private String pickComment(int rating, String cuisine) {
        String[][] comments = {
            { "Terrible experience, food was cold and tasteless.", "Very disappointing, would not order again.", "Worst " + cuisine + " food I've ever had." },
            { "Below average, some dishes were okay but overall not great.", "Not the best, delivery was late and food was lukewarm.", "Expected better for the price." },
            { "Decent food, nothing special. Average " + cuisine + " experience.", "It was okay, some items were good, others not so much.", "Fair portion sizes, average taste." },
            { "Great " + cuisine + " food! Would order again. Quick delivery.", "Really enjoyed the meal, flavors were authentic and fresh.", "Good quality and generous portions." },
            { "Absolutely amazing! Best " + cuisine + " food in the city!", "Incredible flavors, perfect seasoning, fast delivery. 10/10!", "Outstanding! Every dish was a masterpiece." },
        };
        return comments[rating - 1][random.nextInt(3)];
    }
}
