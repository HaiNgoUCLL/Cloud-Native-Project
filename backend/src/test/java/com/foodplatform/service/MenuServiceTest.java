package com.foodplatform.service;

import com.foodplatform.model.MenuItem;
import com.foodplatform.model.Restaurant;
import com.foodplatform.repository.MenuItemRepository;
import com.foodplatform.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock private MenuItemRepository menuItemRepository;
    @Mock private RestaurantRepository restaurantRepository;

    @InjectMocks
    private MenuService menuService;

    private Restaurant restaurant;
    private MenuItem menuItem;

    @BeforeEach
    void setUp() {
        restaurant = new Restaurant();
        restaurant.setId("rest1");
        restaurant.setOwnerId("owner1");

        menuItem = new MenuItem();
        menuItem.setId("item1");
        menuItem.setRestaurantId("rest1");
        menuItem.setName("Pizza");
        menuItem.setPrice(12.99);
        menuItem.setCategory("Main");
        menuItem.setAvailable(true);
    }

    @Test
    void getMenuByRestaurant_returnsList() {
        when(menuItemRepository.findByRestaurantId("rest1")).thenReturn(List.of(menuItem));

        List<MenuItem> result = menuService.getMenuByRestaurant("rest1");

        assertThat(result).hasSize(1);
    }

    @Test
    void addMenuItem_ownerMatch_success() {
        when(restaurantRepository.findById("rest1")).thenReturn(Optional.of(restaurant));
        when(menuItemRepository.save(any(MenuItem.class))).thenAnswer(i -> i.getArgument(0));

        MenuItem result = menuService.addMenuItem("rest1", menuItem, "owner1");

        assertThat(result.getRestaurantId()).isEqualTo("rest1");
    }

    @Test
    void addMenuItem_wrongOwner_throws() {
        when(restaurantRepository.findById("rest1")).thenReturn(Optional.of(restaurant));

        assertThatThrownBy(() -> menuService.addMenuItem("rest1", menuItem, "other"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("You can only manage your own restaurant's menu");
    }

    @Test
    void updateMenuItem_success() {
        when(restaurantRepository.findById("rest1")).thenReturn(Optional.of(restaurant));
        when(menuItemRepository.findById("item1")).thenReturn(Optional.of(menuItem));
        when(menuItemRepository.save(any(MenuItem.class))).thenAnswer(i -> i.getArgument(0));

        MenuItem updated = new MenuItem();
        updated.setName("Updated Pizza");
        updated.setPrice(15.99);
        updated.setDescription("Delicious");
        updated.setCategory("Main");
        updated.setAvailable(true);

        MenuItem result = menuService.updateMenuItem("rest1", "item1", updated, "owner1");

        assertThat(result.getName()).isEqualTo("Updated Pizza");
        assertThat(result.getPrice()).isEqualTo(15.99);
    }

    @Test
    void deleteMenuItem_success() {
        when(restaurantRepository.findById("rest1")).thenReturn(Optional.of(restaurant));

        menuService.deleteMenuItem("rest1", "item1", "owner1");

        verify(menuItemRepository).deleteById("item1");
    }
}
