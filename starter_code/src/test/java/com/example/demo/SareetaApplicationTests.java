package com.example.demo;

import com.example.demo.controllers.CartController;
import com.example.demo.controllers.ItemController;
import com.example.demo.controllers.OrderController;
import com.example.demo.controllers.UserController;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import com.example.demo.model.requests.ModifyCartRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SareetaApplicationTests {

	private CartController cartController;
	private CartRepository cartRepository = mock(CartRepository.class);

	private UserController userController;
	private UserRepository userRepository = mock(UserRepository.class);
	private BCryptPasswordEncoder bCryptPasswordEncoder = mock(BCryptPasswordEncoder.class);


	private OrderController orderController;
	private OrderRepository orderRepository = mock(OrderRepository.class);

	private ItemController itemController;
	private ItemRepository itemRepository = mock(ItemRepository.class);


	@Test
	public void contextLoads() {
	}

	@Before
	public void setUp() {

		cartController = new CartController();
		itemController = new ItemController();
		userController = new UserController();
		orderController = new OrderController();

		TestUtils.injectObjects(userController, "cartRepository", cartRepository);
		TestUtils.injectObjects(userController, "userRepository", userRepository);
		TestUtils.injectObjects(userController, "bCryptPasswordEncoder", bCryptPasswordEncoder);

		TestUtils.injectObjects(cartController, "cartRepository", cartRepository);
		TestUtils.injectObjects(cartController, "itemRepository", itemRepository);
		TestUtils.injectObjects(cartController, "userRepository", userRepository);

		TestUtils.injectObjects(orderController, "orderRepository", orderRepository);
		TestUtils.injectObjects(orderController, "userRepository", userRepository);

		TestUtils.injectObjects(itemController, "itemRepository", itemRepository);

	}

	@Test
	public void create_find_user() {

		when(bCryptPasswordEncoder.encode("testPassword")).thenReturn("thisIsHashed");

		CreateUserRequest request = new CreateUserRequest();
		request.setUsername("HananAlhajri");
		request.setPassword("testPassword");
		request.setConfirmPassword("testPassword");

		ResponseEntity<User> response = userController.createUser(request);

		assertNotNull(response);
		assertEquals(200, response.getStatusCodeValue());

		User user = response.getBody();

		assertNotNull(user);
		assertEquals(0, user.getId());
		assertEquals("HananAlhajri", user.getUsername());
		assertEquals("thisIsHashed", user.getPassword());

		ResponseEntity<User> findUserByUsername = userController.findByUserName("HananAlhajri");
		assertNotNull(findUserByUsername);

	}

	@Test
	public void find_non_existing_user() {
		ResponseEntity<User> findUserByUsername = userController.findByUserName("non-existing-user");
		assertEquals(404, findUserByUsername.getStatusCodeValue());
	}

	@Test
	public void add_remove_order_from_cart() {

		User user = new User();
		user.setUsername("HananAlhajri");

		Cart cart = new Cart();
		user.setCart(cart);

		Item item = new Item();
		item.setId(0L);
		item.setName("Keychron K8");
		item.setDescription("Keyboard");
		item.setPrice(BigDecimal.valueOf(500));

		cart.addItem(item);

		ModifyCartRequest request = new ModifyCartRequest();

		request.setQuantity(1);
		request.setItemId(0L);
		request.setUsername("HananAlhajri");

		when(userRepository.findByUsername(anyString())).thenReturn(user);
		when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

		ResponseEntity<Cart> addToCart = cartController.addTocart(request);

		assertNotNull(addToCart);
		assertEquals(200, addToCart.getStatusCodeValue());
		assertEquals(BigDecimal.valueOf(1000), Objects.requireNonNull(addToCart.getBody()).getTotal());

		ResponseEntity<Cart> removeFromCart = cartController.removeFromcart(request);

		assertNotNull(removeFromCart);
		assertEquals(200, removeFromCart.getStatusCodeValue());

	}

	@Test
	public void submit_order() {
		User user = new User();
		user.setUsername("HananAlhajri");

		when(userRepository.findByUsername(anyString())).thenReturn(user);

		Cart cart = new Cart();
		user.setCart(cart);

		Item item = new Item();
		item.setId(0L);
		item.setName("Keychron K8");
		item.setDescription("Keyboard");
		item.setPrice(BigDecimal.valueOf(500));

		cart.addItem(item);

		ResponseEntity<UserOrder> submittedOrder = orderController.submit(user.getUsername());
		assertNotNull(submittedOrder);
		assertEquals(200, submittedOrder.getStatusCodeValue());

		ResponseEntity<List<UserOrder>> ordersForUser = orderController.getOrdersForUser(user.getUsername());
		assertNotNull(ordersForUser);
		assertEquals(200, ordersForUser.getStatusCodeValue());
	}

}
