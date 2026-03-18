package com.foodplatform.config;

import com.foodplatform.model.MenuItem;
import com.foodplatform.model.PromoCode;
import com.foodplatform.model.Restaurant;
import com.foodplatform.model.User;
import com.foodplatform.repository.MenuItemRepository;
import com.foodplatform.repository.PromoCodeRepository;
import com.foodplatform.repository.RestaurantRepository;
import com.foodplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final PromoCodeRepository promoCodeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded, skipping.");
            return;
        }

        log.info("Seeding database...");

        User admin = createUser("Admin User", "admin@food.com", "Admin123!", User.Role.ADMIN);
        User owner1 = createUser("Mario Rossi", "owner1@food.com", "Owner123!", User.Role.RESTAURANT_OWNER);
        User owner2 = createUser("Yuki Tanaka", "owner2@food.com", "Owner123!", User.Role.RESTAURANT_OWNER);
        User owner3 = createUser("James Wilson", "owner3@food.com", "Owner123!", User.Role.RESTAURANT_OWNER);
        createUser("John Customer", "customer@food.com", "Customer123!", User.Role.CUSTOMER);
        createUser("Mike Driver", "driver@food.com", "Driver123!", User.Role.DELIVERY_DRIVER);

        Restaurant r1 = createRestaurant(owner1.getId(), "Mario's Trattoria",
                "Authentic Italian cuisine with homemade pasta and wood-fired pizzas",
                "Italian", "123 Pasta Lane, Little Italy", "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=800", true, 4.7);

        Restaurant r2 = createRestaurant(owner2.getId(), "Tokyo Omakase",
                "Premium Japanese dining featuring fresh sushi, ramen, and traditional dishes",
                "Japanese", "456 Sakura Street, Japantown", "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=800", true, 4.8);

        Restaurant r3 = createRestaurant(owner3.getId(), "The Prime Cut",
                "Premium steakhouse serving the finest aged beef with classic sides",
                "American", "789 Grill Avenue, Downtown", "https://images.unsplash.com/photo-1544025162-d76694265947?w=800", true, 4.6);

        seedMarioMenu(r1.getId());
        seedTokyoMenu(r2.getId());
        seedPrimeCutMenu(r3.getId());

        seedPromoCodes(admin.getId());

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

    private void seedMarioMenu(String restaurantId) {
        createMenuItem(restaurantId, "Margherita Pizza", "Fresh mozzarella, tomato sauce, basil on wood-fired crust", 14.99, "Pizza", true);
        createMenuItem(restaurantId, "Pepperoni Pizza", "Classic pepperoni with mozzarella and marinara", 16.99, "Pizza", true);
        createMenuItem(restaurantId, "Quattro Formaggi", "Four cheese blend: mozzarella, gorgonzola, parmesan, fontina", 17.99, "Pizza", true);
        createMenuItem(restaurantId, "Diavola Pizza", "Spicy salami, chili flakes, mozzarella, tomato sauce", 17.49, "Pizza", true);
        createMenuItem(restaurantId, "Prosciutto e Funghi", "Prosciutto ham, mushrooms, mozzarella, truffle oil", 18.99, "Pizza", true);

        createMenuItem(restaurantId, "Spaghetti Carbonara", "Creamy egg sauce, pancetta, parmesan, black pepper", 15.99, "Pasta", true);
        createMenuItem(restaurantId, "Fettuccine Alfredo", "Rich cream sauce with parmesan and butter", 14.99, "Pasta", true);
        createMenuItem(restaurantId, "Penne Arrabbiata", "Spicy tomato sauce with garlic and chili", 13.99, "Pasta", true);
        createMenuItem(restaurantId, "Lasagna Bolognese", "Layers of pasta, meat sauce, bechamel, and cheese", 16.99, "Pasta", true);
        createMenuItem(restaurantId, "Ravioli di Ricotta", "Handmade ravioli stuffed with ricotta and spinach", 17.49, "Pasta", true);

        createMenuItem(restaurantId, "Tiramisu", "Classic Italian dessert with espresso-soaked ladyfingers", 9.99, "Desserts", true);
        createMenuItem(restaurantId, "Panna Cotta", "Vanilla cream dessert with berry coulis", 8.99, "Desserts", true);
        createMenuItem(restaurantId, "Cannoli", "Crispy shells filled with sweet ricotta cream", 7.99, "Desserts", true);
        createMenuItem(restaurantId, "Gelato Trio", "Three scoops of artisan gelato", 8.49, "Desserts", true);
        createMenuItem(restaurantId, "Affogato", "Vanilla gelato drowned in hot espresso", 6.99, "Desserts", true);
    }

    private void seedTokyoMenu(String restaurantId) {
        createMenuItem(restaurantId, "Salmon Nigiri (6pc)", "Fresh Atlantic salmon over seasoned rice", 16.99, "Sushi", true);
        createMenuItem(restaurantId, "Dragon Roll", "Shrimp tempura, avocado, eel sauce, tobiko", 18.99, "Sushi", true);
        createMenuItem(restaurantId, "Rainbow Roll", "California roll topped with assorted sashimi", 19.99, "Sushi", true);
        createMenuItem(restaurantId, "Tuna Sashimi", "Premium bluefin tuna, 8 slices", 22.99, "Sushi", true);
        createMenuItem(restaurantId, "Spicy Tuna Roll", "Spicy tuna, cucumber, sriracha mayo", 15.99, "Sushi", true);

        createMenuItem(restaurantId, "Tonkotsu Ramen", "Rich pork bone broth, chashu, soft egg, nori", 16.99, "Ramen", true);
        createMenuItem(restaurantId, "Miso Ramen", "Soybean paste broth, corn, butter, bean sprouts", 15.99, "Ramen", true);
        createMenuItem(restaurantId, "Shoyu Ramen", "Soy sauce broth, bamboo shoots, green onion", 14.99, "Ramen", true);
        createMenuItem(restaurantId, "Spicy Tantanmen", "Sesame-chili broth, ground pork, bok choy", 17.49, "Ramen", true);
        createMenuItem(restaurantId, "Vegetable Ramen", "Kombu dashi broth, tofu, seasonal vegetables", 14.49, "Ramen", true);

        createMenuItem(restaurantId, "Chicken Teriyaki", "Grilled chicken thigh with teriyaki glaze and rice", 15.99, "Hot Dishes", true);
        createMenuItem(restaurantId, "Beef Gyudon", "Thinly sliced beef and onion over rice", 14.99, "Hot Dishes", true);
        createMenuItem(restaurantId, "Tempura Platter", "Assorted shrimp and vegetable tempura", 17.99, "Hot Dishes", true);
        createMenuItem(restaurantId, "Katsu Curry", "Breaded pork cutlet with Japanese curry and rice", 16.49, "Hot Dishes", true);
        createMenuItem(restaurantId, "Yakitori Set", "Grilled chicken skewers with tare sauce", 13.99, "Hot Dishes", true);
    }

    private void seedPromoCodes(String adminId) {
        PromoCode promo1 = new PromoCode();
        promo1.setCode("WELCOME10");
        promo1.setDiscountType(PromoCode.DiscountType.PERCENTAGE);
        promo1.setDiscountValue(10);
        promo1.setMinimumOrderAmount(15);
        promo1.setMaxUsageCount(100);
        promo1.setCurrentUsageCount(0);
        promo1.setActive(true);
        promo1.setCreatedBy(adminId);
        promo1.setExpiresAt(LocalDateTime.now().plusMonths(3));
        promo1.setCreatedAt(LocalDateTime.now());
        promoCodeRepository.save(promo1);

        PromoCode promo2 = new PromoCode();
        promo2.setCode("SAVE5");
        promo2.setDiscountType(PromoCode.DiscountType.FIXED_AMOUNT);
        promo2.setDiscountValue(5);
        promo2.setMinimumOrderAmount(25);
        promo2.setMaxUsageCount(50);
        promo2.setCurrentUsageCount(0);
        promo2.setActive(true);
        promo2.setCreatedBy(adminId);
        promo2.setExpiresAt(LocalDateTime.now().plusMonths(1));
        promo2.setCreatedAt(LocalDateTime.now());
        promoCodeRepository.save(promo2);

        log.info("Promo codes seeded: WELCOME10 (10% off), SAVE5 ($5 off)");
    }

    private void seedPrimeCutMenu(String restaurantId) {
        createMenuItem(restaurantId, "Ribeye Steak 12oz", "Prime aged ribeye, herb butter, grilled to perfection", 38.99, "Steaks", true);
        createMenuItem(restaurantId, "Filet Mignon 8oz", "Center-cut tenderloin, peppercorn sauce", 42.99, "Steaks", true);
        createMenuItem(restaurantId, "New York Strip 14oz", "Classic strip steak, charbroiled, chimichurri", 36.99, "Steaks", true);
        createMenuItem(restaurantId, "T-Bone Steak 16oz", "Best of both worlds: strip and tenderloin", 44.99, "Steaks", true);
        createMenuItem(restaurantId, "Wagyu Burger", "Premium wagyu beef, brioche bun, truffle aioli", 24.99, "Steaks", true);

        createMenuItem(restaurantId, "Caesar Salad", "Romaine, parmesan, croutons, house-made dressing", 11.99, "Sides & Salads", true);
        createMenuItem(restaurantId, "Loaded Baked Potato", "Sour cream, cheddar, bacon, chives", 8.99, "Sides & Salads", true);
        createMenuItem(restaurantId, "Creamed Spinach", "Classic steakhouse style with nutmeg", 9.99, "Sides & Salads", true);
        createMenuItem(restaurantId, "Truffle Mac & Cheese", "Three cheese blend with black truffle", 12.99, "Sides & Salads", true);
        createMenuItem(restaurantId, "Wedge Salad", "Iceberg, blue cheese, bacon, tomato", 10.99, "Sides & Salads", true);

        createMenuItem(restaurantId, "Chocolate Lava Cake", "Warm molten center, vanilla ice cream", 12.99, "Desserts", true);
        createMenuItem(restaurantId, "New York Cheesecake", "Classic creamy cheesecake, berry compote", 11.99, "Desserts", true);
        createMenuItem(restaurantId, "Creme Brulee", "Vanilla custard, caramelized sugar crust", 10.99, "Desserts", true);
        createMenuItem(restaurantId, "Apple Pie a la Mode", "Warm apple pie with cinnamon ice cream", 10.49, "Desserts", true);
        createMenuItem(restaurantId, "Bourbon Pecan Pie", "Toasted pecans, bourbon caramel, whipped cream", 11.49, "Desserts", true);
    }
}
