package com.foodplatform.config;

import com.foodplatform.model.*;
import com.foodplatform.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded, skipping.");
            return;
        }

        log.info("Seeding database...");

        // ── Users ──────────────────────────────────────────────
        User admin = createUser("Admin User", "admin@food.com", "Admin123!", User.Role.ADMIN);
        User owner1 = createUser("Mario Rossi", "owner1@food.com", "Owner123!", User.Role.RESTAURANT_OWNER);
        User owner2 = createUser("Yuki Tanaka", "owner2@food.com", "Owner123!", User.Role.RESTAURANT_OWNER);
        User owner3 = createUser("James Wilson", "owner3@food.com", "Owner123!", User.Role.RESTAURANT_OWNER);
        User customer1 = createUser("John Customer", "customer@food.com", "Customer123!", User.Role.CUSTOMER);
        User customer2 = createUser("Sarah Miller", "customer2@food.com", "Customer123!", User.Role.CUSTOMER);
        User customer3 = createUser("David Chen", "customer3@food.com", "Customer123!", User.Role.CUSTOMER);
        User driver1 = createUser("Mike Driver", "driver@food.com", "Driver123!", User.Role.DELIVERY_DRIVER);
        User driver2 = createUser("Lisa Express", "driver2@food.com", "Driver123!", User.Role.DELIVERY_DRIVER);

        // ── Restaurants ────────────────────────────────────────
        Restaurant r1 = createRestaurant(owner1.getId(), "Mario's Trattoria",
                "Authentic Italian cuisine with homemade pasta and wood-fired pizzas",
                "Italian", "123 Pasta Lane, Little Italy", "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=800", true, 4.7);

        Restaurant r2 = createRestaurant(owner2.getId(), "Tokyo Omakase",
                "Premium Japanese dining featuring fresh sushi, ramen, and traditional dishes",
                "Japanese", "456 Sakura Street, Japantown", "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=800", true, 4.8);

        Restaurant r3 = createRestaurant(owner3.getId(), "The Prime Cut",
                "Premium steakhouse serving the finest aged beef with classic sides",
                "American", "789 Grill Avenue, Downtown", "https://images.unsplash.com/photo-1544025162-d76694265947?w=800", true, 4.6);

        // ── Menu Items ─────────────────────────────────────────
        List<MenuItem> marioItems = seedMarioMenu(r1.getId());
        List<MenuItem> tokyoItems = seedTokyoMenu(r2.getId());
        List<MenuItem> primeCutItems = seedPrimeCutMenu(r3.getId());

        // ── Promo Codes ────────────────────────────────────────
        seedPromoCodes(admin.getId(), r1.getId());

        // ── Orders ─────────────────────────────────────────────
        seedOrders(customer1, customer2, customer3, r1, r2, r3,
                marioItems, tokyoItems, primeCutItems, driver1, driver2);

        // ── Reviews ────────────────────────────────────────────
        seedReviews(customer1, customer2, customer3, r1, r2, r3);

        // ── Carts ──────────────────────────────────────────────
        seedCarts(customer2, customer3, r2, r3, tokyoItems, primeCutItems);

        log.info("Database seeded successfully!");
    }

    private User createUser(String name, String email, String password, User.Role role) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        user.setAddress("123 Main St");
        user.setPhone("+1234567890");
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

    private MenuItem createMenuItem(String restaurantId, String name, String description,
                                     double price, String category, boolean isAvailable) {
        MenuItem item = new MenuItem();
        item.setRestaurantId(restaurantId);
        item.setName(name);
        item.setDescription(description);
        item.setPrice(price);
        item.setCategory(category);
        item.setAvailable(isAvailable);
        return menuItemRepository.save(item);
    }

    // ── Menu Seeding ───────────────────────────────────────────

    private List<MenuItem> seedMarioMenu(String restaurantId) {
        List<MenuItem> items = new ArrayList<>();
        items.add(createMenuItem(restaurantId, "Margherita Pizza", "Fresh mozzarella, tomato sauce, basil on wood-fired crust", 14.99, "Pizza", true));
        items.add(createMenuItem(restaurantId, "Pepperoni Pizza", "Classic pepperoni with mozzarella and marinara", 16.99, "Pizza", true));
        items.add(createMenuItem(restaurantId, "Quattro Formaggi", "Four cheese blend: mozzarella, gorgonzola, parmesan, fontina", 17.99, "Pizza", true));
        items.add(createMenuItem(restaurantId, "Diavola Pizza", "Spicy salami, chili flakes, mozzarella, tomato sauce", 17.49, "Pizza", true));
        items.add(createMenuItem(restaurantId, "Prosciutto e Funghi", "Prosciutto ham, mushrooms, mozzarella, truffle oil", 18.99, "Pizza", true));

        items.add(createMenuItem(restaurantId, "Spaghetti Carbonara", "Creamy egg sauce, pancetta, parmesan, black pepper", 15.99, "Pasta", true));
        items.add(createMenuItem(restaurantId, "Fettuccine Alfredo", "Rich cream sauce with parmesan and butter", 14.99, "Pasta", true));
        items.add(createMenuItem(restaurantId, "Penne Arrabbiata", "Spicy tomato sauce with garlic and chili", 13.99, "Pasta", true));
        items.add(createMenuItem(restaurantId, "Lasagna Bolognese", "Layers of pasta, meat sauce, bechamel, and cheese", 16.99, "Pasta", true));
        items.add(createMenuItem(restaurantId, "Ravioli di Ricotta", "Handmade ravioli stuffed with ricotta and spinach", 17.49, "Pasta", true));

        items.add(createMenuItem(restaurantId, "Tiramisu", "Classic Italian dessert with espresso-soaked ladyfingers", 9.99, "Desserts", true));
        items.add(createMenuItem(restaurantId, "Panna Cotta", "Vanilla cream dessert with berry coulis", 8.99, "Desserts", true));
        items.add(createMenuItem(restaurantId, "Cannoli", "Crispy shells filled with sweet ricotta cream", 7.99, "Desserts", true));
        items.add(createMenuItem(restaurantId, "Gelato Trio", "Three scoops of artisan gelato", 8.49, "Desserts", true));
        items.add(createMenuItem(restaurantId, "Affogato", "Vanilla gelato drowned in hot espresso", 6.99, "Desserts", true));
        return items;
    }

    private List<MenuItem> seedTokyoMenu(String restaurantId) {
        List<MenuItem> items = new ArrayList<>();
        items.add(createMenuItem(restaurantId, "Salmon Nigiri (6pc)", "Fresh Atlantic salmon over seasoned rice", 16.99, "Sushi", true));
        items.add(createMenuItem(restaurantId, "Dragon Roll", "Shrimp tempura, avocado, eel sauce, tobiko", 18.99, "Sushi", true));
        items.add(createMenuItem(restaurantId, "Rainbow Roll", "California roll topped with assorted sashimi", 19.99, "Sushi", true));
        items.add(createMenuItem(restaurantId, "Tuna Sashimi", "Premium bluefin tuna, 8 slices", 22.99, "Sushi", true));
        items.add(createMenuItem(restaurantId, "Spicy Tuna Roll", "Spicy tuna, cucumber, sriracha mayo", 15.99, "Sushi", true));

        items.add(createMenuItem(restaurantId, "Tonkotsu Ramen", "Rich pork bone broth, chashu, soft egg, nori", 16.99, "Ramen", true));
        items.add(createMenuItem(restaurantId, "Miso Ramen", "Soybean paste broth, corn, butter, bean sprouts", 15.99, "Ramen", true));
        items.add(createMenuItem(restaurantId, "Shoyu Ramen", "Soy sauce broth, bamboo shoots, green onion", 14.99, "Ramen", true));
        items.add(createMenuItem(restaurantId, "Spicy Tantanmen", "Sesame-chili broth, ground pork, bok choy", 17.49, "Ramen", true));
        items.add(createMenuItem(restaurantId, "Vegetable Ramen", "Kombu dashi broth, tofu, seasonal vegetables", 14.49, "Ramen", true));

        items.add(createMenuItem(restaurantId, "Chicken Teriyaki", "Grilled chicken thigh with teriyaki glaze and rice", 15.99, "Hot Dishes", true));
        items.add(createMenuItem(restaurantId, "Beef Gyudon", "Thinly sliced beef and onion over rice", 14.99, "Hot Dishes", true));
        items.add(createMenuItem(restaurantId, "Tempura Platter", "Assorted shrimp and vegetable tempura", 17.99, "Hot Dishes", true));
        items.add(createMenuItem(restaurantId, "Katsu Curry", "Breaded pork cutlet with Japanese curry and rice", 16.49, "Hot Dishes", true));
        items.add(createMenuItem(restaurantId, "Yakitori Set", "Grilled chicken skewers with tare sauce", 13.99, "Hot Dishes", true));
        return items;
    }

    private List<MenuItem> seedPrimeCutMenu(String restaurantId) {
        List<MenuItem> items = new ArrayList<>();
        items.add(createMenuItem(restaurantId, "Ribeye Steak 12oz", "Prime aged ribeye, herb butter, grilled to perfection", 38.99, "Steaks", true));
        items.add(createMenuItem(restaurantId, "Filet Mignon 8oz", "Center-cut tenderloin, peppercorn sauce", 42.99, "Steaks", true));
        items.add(createMenuItem(restaurantId, "New York Strip 14oz", "Classic strip steak, charbroiled, chimichurri", 36.99, "Steaks", true));
        items.add(createMenuItem(restaurantId, "T-Bone Steak 16oz", "Best of both worlds: strip and tenderloin", 44.99, "Steaks", true));
        items.add(createMenuItem(restaurantId, "Wagyu Burger", "Premium wagyu beef, brioche bun, truffle aioli", 24.99, "Steaks", true));

        items.add(createMenuItem(restaurantId, "Caesar Salad", "Romaine, parmesan, croutons, house-made dressing", 11.99, "Sides & Salads", true));
        items.add(createMenuItem(restaurantId, "Loaded Baked Potato", "Sour cream, cheddar, bacon, chives", 8.99, "Sides & Salads", true));
        items.add(createMenuItem(restaurantId, "Creamed Spinach", "Classic steakhouse style with nutmeg", 9.99, "Sides & Salads", true));
        items.add(createMenuItem(restaurantId, "Truffle Mac & Cheese", "Three cheese blend with black truffle", 12.99, "Sides & Salads", true));
        items.add(createMenuItem(restaurantId, "Wedge Salad", "Iceberg, blue cheese, bacon, tomato", 10.99, "Sides & Salads", true));

        items.add(createMenuItem(restaurantId, "Chocolate Lava Cake", "Warm molten center, vanilla ice cream", 12.99, "Desserts", true));
        items.add(createMenuItem(restaurantId, "New York Cheesecake", "Classic creamy cheesecake, berry compote", 11.99, "Desserts", true));
        items.add(createMenuItem(restaurantId, "Creme Brulee", "Vanilla custard, caramelized sugar crust", 10.99, "Desserts", true));
        items.add(createMenuItem(restaurantId, "Apple Pie a la Mode", "Warm apple pie with cinnamon ice cream", 10.49, "Desserts", true));
        items.add(createMenuItem(restaurantId, "Bourbon Pecan Pie", "Toasted pecans, bourbon caramel, whipped cream", 11.49, "Desserts", true));
        return items;
    }

    // ── Promo Code Seeding ─────────────────────────────────────

    private void seedPromoCodes(String adminId, String marioRestaurantId) {
        // Active percentage discount
        createPromoCode("WELCOME10", PromoCode.DiscountType.PERCENTAGE, 10, 15, 100, 0,
                true, null, adminId, LocalDateTime.now().plusMonths(3));

        // Active fixed amount discount
        createPromoCode("SAVE5", PromoCode.DiscountType.FIXED_AMOUNT, 5, 25, 50, 0,
                true, null, adminId, LocalDateTime.now().plusMonths(1));

        // Expired promo — tests expiration validation
        createPromoCode("EXPIRED20", PromoCode.DiscountType.PERCENTAGE, 20, 10, 200, 5,
                true, null, adminId, LocalDateTime.now().minusDays(30));

        // Maxed out promo — tests usage limit
        createPromoCode("MAXED15", PromoCode.DiscountType.PERCENTAGE, 15, 20, 10, 10,
                true, null, adminId, LocalDateTime.now().plusMonths(2));

        // Inactive promo — tests deactivation
        createPromoCode("INACTIVE10", PromoCode.DiscountType.FIXED_AMOUNT, 10, 15, 50, 3,
                false, null, adminId, LocalDateTime.now().plusMonths(6));

        // Restaurant-specific promo — tests restaurant filtering
        createPromoCode("MARIO20", PromoCode.DiscountType.PERCENTAGE, 20, 30, 100, 12,
                true, marioRestaurantId, adminId, LocalDateTime.now().plusMonths(2));

        // High minimum order promo — edge case testing
        createPromoCode("BIGORDER10", PromoCode.DiscountType.PERCENTAGE, 10, 50, 200, 0,
                true, null, adminId, LocalDateTime.now().plusMonths(4));

        log.info("Promo codes seeded: WELCOME10, SAVE5, EXPIRED20, MAXED15, INACTIVE10, MARIO20, BIGORDER10");
    }

    private void createPromoCode(String code, PromoCode.DiscountType type, double value,
                                  double minOrder, int maxUsage, int currentUsage,
                                  boolean isActive, String restaurantId, String createdBy,
                                  LocalDateTime expiresAt) {
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

    // ── Order Seeding ──────────────────────────────────────────

    private void seedOrders(User customer1, User customer2, User customer3,
                             Restaurant r1, Restaurant r2, Restaurant r3,
                             List<MenuItem> marioItems, List<MenuItem> tokyoItems,
                             List<MenuItem> primeCutItems, User driver1, User driver2) {

        LocalDateTime now = LocalDateTime.now();

        // Order 1: DELIVERED, customer1 @ Mario's, paid, no promo (for review testing)
        createOrder(customer1.getId(), r1.getId(),
                List.of(orderItem(marioItems.get(0), 2), orderItem(marioItems.get(5), 1)),
                Order.OrderStatus.DELIVERED, Order.PaymentStatus.SIMULATED_PAID,
                Order.PaymentMethod.CREDIT_CARD, "456 Oak Ave, Apt 2B",
                driver1.getId(), null, 0, now.minusDays(7));

        // Order 2: DELIVERED, customer1 @ Tokyo, paid, with WELCOME10 promo (for review testing)
        createOrder(customer1.getId(), r2.getId(),
                List.of(orderItem(tokyoItems.get(0), 1), orderItem(tokyoItems.get(5), 1)),
                Order.OrderStatus.DELIVERED, Order.PaymentStatus.SIMULATED_PAID,
                Order.PaymentMethod.PAYPAL, "456 Oak Ave, Apt 2B",
                driver1.getId(), "WELCOME10", 3.40, now.minusDays(5));

        // Order 3: DELIVERED, customer2 @ Mario's, paid (for multi-customer review testing)
        createOrder(customer2.getId(), r1.getId(),
                List.of(orderItem(marioItems.get(1), 1), orderItem(marioItems.get(10), 2)),
                Order.OrderStatus.DELIVERED, Order.PaymentStatus.SIMULATED_PAID,
                Order.PaymentMethod.CREDIT_CARD, "789 Pine St",
                driver2.getId(), null, 0, now.minusDays(4));

        // Order 4: DELIVERED, customer2 @ Prime Cut, paid (for multi-customer review testing)
        createOrder(customer2.getId(), r3.getId(),
                List.of(orderItem(primeCutItems.get(0), 1), orderItem(primeCutItems.get(5), 1)),
                Order.OrderStatus.DELIVERED, Order.PaymentStatus.SIMULATED_PAID,
                Order.PaymentMethod.CASH, "789 Pine St",
                driver1.getId(), null, 0, now.minusDays(3));

        // Order 5: DELIVERED, customer3 @ Tokyo, paid (more review data)
        createOrder(customer3.getId(), r2.getId(),
                List.of(orderItem(tokyoItems.get(2), 1), orderItem(tokyoItems.get(6), 1)),
                Order.OrderStatus.DELIVERED, Order.PaymentStatus.SIMULATED_PAID,
                Order.PaymentMethod.CREDIT_CARD, "101 Elm Blvd",
                driver2.getId(), null, 0, now.minusDays(2));

        // Order 6: DELIVERED, customer3 @ Prime Cut, paid, with SAVE5 promo
        createOrder(customer3.getId(), r3.getId(),
                List.of(orderItem(primeCutItems.get(1), 1), orderItem(primeCutItems.get(6), 1)),
                Order.OrderStatus.DELIVERED, Order.PaymentStatus.SIMULATED_PAID,
                Order.PaymentMethod.PAYPAL, "101 Elm Blvd",
                driver1.getId(), "SAVE5", 5.00, now.minusDays(1));

        // Order 7: PENDING, customer1 @ Prime Cut, not paid (cancellation testing)
        createOrder(customer1.getId(), r3.getId(),
                List.of(orderItem(primeCutItems.get(4), 2)),
                Order.OrderStatus.PENDING, Order.PaymentStatus.PENDING,
                Order.PaymentMethod.CREDIT_CARD, "456 Oak Ave, Apt 2B",
                null, null, 0, now.minusHours(2));

        // Order 8: CONFIRMED, customer2 @ Tokyo, paid (status progression testing)
        createOrder(customer2.getId(), r2.getId(),
                List.of(orderItem(tokyoItems.get(3), 1), orderItem(tokyoItems.get(7), 1)),
                Order.OrderStatus.CONFIRMED, Order.PaymentStatus.SIMULATED_PAID,
                Order.PaymentMethod.CREDIT_CARD, "789 Pine St",
                null, null, 0, now.minusHours(1));

        // Order 9: PREPARING, customer3 @ Mario's, paid (driver assignment testing)
        createOrder(customer3.getId(), r1.getId(),
                List.of(orderItem(marioItems.get(2), 1), orderItem(marioItems.get(7), 1)),
                Order.OrderStatus.PREPARING, Order.PaymentStatus.SIMULATED_PAID,
                Order.PaymentMethod.PAYPAL, "101 Elm Blvd",
                null, null, 0, now.minusMinutes(45));

        // Order 10: OUT_FOR_DELIVERY, customer1 @ Tokyo, paid (delivery tracking)
        createOrder(customer1.getId(), r2.getId(),
                List.of(orderItem(tokyoItems.get(4), 2), orderItem(tokyoItems.get(10), 1)),
                Order.OrderStatus.OUT_FOR_DELIVERY, Order.PaymentStatus.SIMULATED_PAID,
                Order.PaymentMethod.CASH, "456 Oak Ave, Apt 2B",
                driver2.getId(), null, 0, now.minusMinutes(30));

        // Order 11: CANCELLED, customer2 @ Prime Cut, pending payment (analytics completeness)
        createOrder(customer2.getId(), r3.getId(),
                List.of(orderItem(primeCutItems.get(2), 1)),
                Order.OrderStatus.CANCELLED, Order.PaymentStatus.PENDING,
                Order.PaymentMethod.CREDIT_CARD, "789 Pine St",
                null, null, 0, now.minusDays(6));

        // Order 12: DELIVERED, customer1 @ Mario's, paid (additional analytics data)
        createOrder(customer1.getId(), r1.getId(),
                List.of(orderItem(marioItems.get(3), 1), orderItem(marioItems.get(8), 1), orderItem(marioItems.get(11), 1)),
                Order.OrderStatus.DELIVERED, Order.PaymentStatus.SIMULATED_PAID,
                Order.PaymentMethod.CREDIT_CARD, "456 Oak Ave, Apt 2B",
                driver1.getId(), null, 0, now.minusDays(10));

        log.info("12 sample orders seeded across all statuses");
    }

    private Order.OrderItem orderItem(MenuItem item, int quantity) {
        return new Order.OrderItem(item.getId(), item.getName(), item.getPrice(), quantity);
    }

    private void createOrder(String customerId, String restaurantId, List<Order.OrderItem> items,
                              Order.OrderStatus status, Order.PaymentStatus paymentStatus,
                              Order.PaymentMethod paymentMethod, String deliveryAddress,
                              String driverId, String promoCode, double discountAmount,
                              LocalDateTime createdAt) {
        double totalAmount = items.stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum() - discountAmount;

        Order order = new Order();
        order.setCustomerId(customerId);
        order.setRestaurantId(restaurantId);
        order.setItems(items);
        order.setStatus(status);
        order.setTotalAmount(Math.round(totalAmount * 100.0) / 100.0);
        order.setDeliveryAddress(deliveryAddress);
        order.setPaymentStatus(paymentStatus);
        order.setPaymentMethod(paymentMethod);
        order.setDriverId(driverId);
        order.setPromoCode(promoCode);
        order.setDiscountAmount(discountAmount);
        order.setCreatedAt(createdAt);
        order.setUpdatedAt(createdAt.plusMinutes(15));
        orderRepository.save(order);
    }

    // ── Review Seeding ─────────────────────────────────────────

    private void seedReviews(User customer1, User customer2, User customer3,
                              Restaurant r1, Restaurant r2, Restaurant r3) {
        // Get delivered orders to link reviews
        List<Order> allOrders = orderRepository.findAllByOrderByCreatedAtDesc();
        List<Order> deliveredOrders = allOrders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
                .toList();

        int reviewIdx = 0;

        // Reviews for Mario's Trattoria (r1) — 3 reviews
        List<Order> marioDelivered = deliveredOrders.stream()
                .filter(o -> o.getRestaurantId().equals(r1.getId())).toList();
        if (marioDelivered.size() >= 2) {
            createReview(customer1.getId(), customer1.getName(), r1.getId(),
                    marioDelivered.get(0).getId(), 5,
                    "Absolutely incredible pasta! The Carbonara was the best I've ever had. Wood-fired pizza crust was perfectly crispy.",
                    0.95);
            createReview(customer2.getId(), customer2.getName(), r1.getId(),
                    marioDelivered.get(1).getId(), 4,
                    "Great Italian food, generous portions. Tiramisu was divine. Delivery was a bit slow but worth the wait.",
                    0.78);
        }
        if (marioDelivered.size() >= 3) {
            createReview(customer1.getId(), customer1.getName(), r1.getId(),
                    marioDelivered.get(2).getId(), 4,
                    "Consistent quality every time. The Diavola Pizza has the perfect kick!",
                    0.82);
        }

        // Reviews for Tokyo Omakase (r2) — 2 reviews
        List<Order> tokyoDelivered = deliveredOrders.stream()
                .filter(o -> o.getRestaurantId().equals(r2.getId())).toList();
        if (tokyoDelivered.size() >= 1) {
            createReview(customer1.getId(), customer1.getName(), r2.getId(),
                    tokyoDelivered.get(0).getId(), 5,
                    "Best sushi in town! The Salmon Nigiri melts in your mouth. Tonkotsu ramen broth is rich and flavorful.",
                    0.92);
        }
        if (tokyoDelivered.size() >= 2) {
            createReview(customer3.getId(), customer3.getName(), r2.getId(),
                    tokyoDelivered.get(1).getId(), 3,
                    "Good food but the Rainbow Roll was slightly warm on arrival. Miso Ramen was still excellent though.",
                    0.45);
        }

        // Reviews for The Prime Cut (r3) — 3 reviews
        List<Order> primeDelivered = deliveredOrders.stream()
                .filter(o -> o.getRestaurantId().equals(r3.getId())).toList();
        if (primeDelivered.size() >= 1) {
            createReview(customer2.getId(), customer2.getName(), r3.getId(),
                    primeDelivered.get(0).getId(), 5,
                    "The Ribeye was cooked to absolute perfection! Medium-rare as requested. Caesar salad was fresh and crispy.",
                    0.97);
        }
        if (primeDelivered.size() >= 2) {
            createReview(customer3.getId(), customer3.getName(), r3.getId(),
                    primeDelivered.get(1).getId(), 2,
                    "Filet Mignon was overcooked unfortunately. Baked potato was cold. Disappointing for the price.",
                    0.15);
        }

        log.info("7 sample reviews seeded across 3 restaurants");
    }

    private void createReview(String customerId, String customerName, String restaurantId,
                               String orderId, int rating, String comment, Double sentimentScore) {
        Review review = new Review();
        review.setCustomerId(customerId);
        review.setCustomerName(customerName);
        review.setRestaurantId(restaurantId);
        review.setOrderId(orderId);
        review.setRating(rating);
        review.setComment(comment);
        review.setSentimentScore(sentimentScore);
        review.setCreatedAt(LocalDateTime.now());
        reviewRepository.save(review);
    }

    // ── Cart Seeding ───────────────────────────────────────────

    private void seedCarts(User customer2, User customer3, Restaurant r2, Restaurant r3,
                            List<MenuItem> tokyoItems, List<MenuItem> primeCutItems) {
        // Customer2 cart: items from Tokyo Omakase
        Cart cart1 = new Cart();
        cart1.setCustomerId(customer2.getId());
        cart1.setRestaurantId(r2.getId());
        List<Cart.CartItem> items1 = new ArrayList<>();
        items1.add(new Cart.CartItem(tokyoItems.get(0).getId(), tokyoItems.get(0).getName(), tokyoItems.get(0).getPrice(), 2));
        items1.add(new Cart.CartItem(tokyoItems.get(5).getId(), tokyoItems.get(5).getName(), tokyoItems.get(5).getPrice(), 1));
        cart1.setItems(items1);
        cartRepository.save(cart1);

        // Customer3 cart: items from The Prime Cut
        Cart cart2 = new Cart();
        cart2.setCustomerId(customer3.getId());
        cart2.setRestaurantId(r3.getId());
        List<Cart.CartItem> items2 = new ArrayList<>();
        items2.add(new Cart.CartItem(primeCutItems.get(0).getId(), primeCutItems.get(0).getName(), primeCutItems.get(0).getPrice(), 1));
        items2.add(new Cart.CartItem(primeCutItems.get(5).getId(), primeCutItems.get(5).getName(), primeCutItems.get(5).getPrice(), 1));
        items2.add(new Cart.CartItem(primeCutItems.get(10).getId(), primeCutItems.get(10).getName(), primeCutItems.get(10).getPrice(), 1));
        cart2.setItems(items2);
        cartRepository.save(cart2);

        log.info("2 sample carts seeded");
    }
}
