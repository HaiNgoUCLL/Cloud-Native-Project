package com.foodplatform.service;

import com.foodplatform.model.Restaurant;
import com.foodplatform.repository.MenuItemRepository;
import com.foodplatform.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock private RestaurantRepository restaurantRepository;
    @Mock private MenuItemRepository menuItemRepository;

    @InjectMocks
    private RestaurantService restaurantService;

    private Restaurant restaurant;

    @BeforeEach
    void setUp() {
        restaurant = new Restaurant();
        restaurant.setId("rest1");
        restaurant.setOwnerId("owner1");
        restaurant.setName("Mario's Trattoria");
        restaurant.setDescription("Italian");
        restaurant.setCuisineType("Italian");
        restaurant.setAddress("123 Main St");
        restaurant.setOpen(true);
        restaurant.setRating(4.5);
        restaurant.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getAllRestaurants_noFilters_returnsAll() {
        when(restaurantRepository.findAll()).thenReturn(List.of(restaurant));

        List<Restaurant> result = restaurantService.getAllRestaurants(null, null);

        assertThat(result).hasSize(1);
    }

    @Test
    void getAllRestaurants_withCuisineFilter() {
        when(restaurantRepository.findByCuisineTypeIgnoreCase("Italian")).thenReturn(List.of(restaurant));

        List<Restaurant> result = restaurantService.getAllRestaurants(null, "Italian");

        assertThat(result).hasSize(1);
    }

    @Test
    void getAllRestaurants_withSearchFilter() {
        when(restaurantRepository.findByNameContainingIgnoreCase("Mario")).thenReturn(List.of(restaurant));

        List<Restaurant> result = restaurantService.getAllRestaurants("Mario", null);

        assertThat(result).hasSize(1);
    }

    @Test
    void getAllRestaurants_withBothFilters() {
        when(restaurantRepository.findByNameContainingIgnoreCaseAndCuisineTypeIgnoreCase("Mario", "Italian"))
                .thenReturn(List.of(restaurant));

        List<Restaurant> result = restaurantService.getAllRestaurants("Mario", "Italian");

        assertThat(result).hasSize(1);
    }

    @Test
    void getRestaurantById_found() {
        when(restaurantRepository.findById("rest1")).thenReturn(Optional.of(restaurant));

        Restaurant result = restaurantService.getRestaurantById("rest1");

        assertThat(result.getName()).isEqualTo("Mario's Trattoria");
    }

    @Test
    void getRestaurantById_notFound_throws() {
        when(restaurantRepository.findById("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> restaurantService.getRestaurantById("bad"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Restaurant not found");
    }

    @Test
    void createRestaurant_setsOwnerAndDate() {
        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(i -> i.getArgument(0));

        Restaurant result = restaurantService.createRestaurant(restaurant, "owner1");

        assertThat(result.getOwnerId()).isEqualTo("owner1");
        assertThat(result.getCreatedAt()).isNotNull();
    }

    @Test
    void updateRestaurant_ownerMatch_success() {
        when(restaurantRepository.findById("rest1")).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(i -> i.getArgument(0));

        Restaurant updated = new Restaurant();
        updated.setName("Updated Name");
        updated.setDescription("Updated");
        updated.setCuisineType("Italian");
        updated.setAddress("456 New St");
        updated.setOpen(false);

        Restaurant result = restaurantService.updateRestaurant("rest1", updated, "owner1");

        assertThat(result.getName()).isEqualTo("Updated Name");
    }

    @Test
    void updateRestaurant_wrongOwner_throws() {
        when(restaurantRepository.findById("rest1")).thenReturn(Optional.of(restaurant));

        assertThatThrownBy(() -> restaurantService.updateRestaurant("rest1", restaurant, "other"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("You can only update your own restaurant");
    }

    @Test
    void deleteRestaurant_cascadesMenuItems() {
        when(restaurantRepository.findById("rest1")).thenReturn(Optional.of(restaurant));

        restaurantService.deleteRestaurant("rest1", "owner1");

        verify(menuItemRepository).deleteByRestaurantId("rest1");
        verify(restaurantRepository).deleteById("rest1");
    }

    @Test
    void getRestaurantsByOwner_returnsOwnerRestaurants() {
        when(restaurantRepository.findByOwnerId("owner1")).thenReturn(List.of(restaurant));

        List<Restaurant> result = restaurantService.getRestaurantsByOwner("owner1");

        assertThat(result).hasSize(1);
    }
}
