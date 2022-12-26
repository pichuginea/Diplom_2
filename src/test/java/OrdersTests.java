import api.client.OrdersClient;
import api.client.OrdersClientAssertions;
import api.client.UserClient;
import api.pojo.AuthRegister;
import api.pojo.Orders;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;

public class OrdersTests {

	public List<String> ingredients = new ArrayList<>();
	private String accessToken;
	private String ingredient1;
	private String ingredient2;
	private UserClient userClient = new UserClient();
	private OrdersClient ordersClient = new OrdersClient();
	private OrdersClientAssertions ordersClientAssertions = new OrdersClientAssertions();

	@BeforeClass
	public static void log() {
		RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
	}

	@Before
	public void setUp() {
		RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";

		String email = "CreateEmail" + System.currentTimeMillis() + "@yandex.ru";
		String password = "CreatePw" + System.currentTimeMillis();
		String name = "CreateName" + System.currentTimeMillis();

		ValidatableResponse registerUserResponse = userClient.registerUserResponse(new AuthRegister(email, password, name));
		accessToken = registerUserResponse.extract().jsonPath().getString("accessToken");

		ValidatableResponse ingredients = ordersClient.getIngredientsResponse();
		ingredient1 = ingredients.extract().jsonPath().getString("data[0]._id");
		ingredient2 = ingredients.extract().jsonPath().getString("data[1]._id");
	}

	@Test
	@DisplayName("Order creation")
	@Description("Basic test for positive order creation with ingredients")
	public void checkOrderCreation() {
		ingredients.add(ingredient1);
		ingredients.add(ingredient2);
		ValidatableResponse ordersResponse = ordersClient.createOrderResponse(new Orders(ingredients), accessToken);

		ordersClientAssertions.checkOrderCreatedSuccess(ordersResponse);
	}

	@Test
	@DisplayName("Order unauthorized creation")
	@Description("Сase for order creation when user is unauthorized. Order must be created")
	public void checkSuccessOrderCreationUnauthorized() {
		ingredients.add(ingredient1);
		ingredients.add(ingredient2);
		ValidatableResponse ordersResponse = ordersClient.createOrderUnauthorized(new Orders(ingredients));

		ordersClientAssertions.checkOrderCreatedSuccess(ordersResponse);
	}

	@Test
	@DisplayName("Order creation without ingredients")
	@Description("Negative case for order creation order has no ingredients")
	public void checkOrderCreationWithoutIngredients() {
		ValidatableResponse ordersResponse = ordersClient.createOrderResponse(new Orders(ingredients), accessToken);

		ordersClientAssertions.checkOrderNotCreatedWithoutIngredients(ordersResponse);
	}

	@Test
	@DisplayName("Incorrect hash ingredients")
	@Description("Negative case for order creation order with incorrect ingredients hash")
	public void checkOrderCreationWithIncorrectHash() {
		ingredients.add("incorrect_hash_1");
		ingredients.add("incorrect_hash_2");
		ValidatableResponse ordersResponse = ordersClient.createOrderResponse(new Orders(ingredients), accessToken);

		ordersClientAssertions.checkOrderNotCreatedIncorrectHash(ordersResponse);
	}

	@Test
	@DisplayName("Get user orders")
	@Description("Basic test for positive user order getting")
	public void checkUserOrderGetting() {
		ingredients.add(ingredient1);
		ingredients.add(ingredient2);
		ValidatableResponse ordersResponse = ordersClient.createOrderResponse(new Orders(ingredients), accessToken);

		String orderId = ordersResponse.extract().jsonPath().getString("order._id");
		int orderNumber = ordersResponse.extract().jsonPath().getInt("order.number");

		ValidatableResponse getOrdersResponse = ordersClient.getOrdersResponse(accessToken);

		ordersClientAssertions.checkCreatedOrder(getOrdersResponse, orderId, orderNumber);
	}

	@Test
	@DisplayName("Get user order unauthorized")
	@Description("Negative case for user order getting when user is unauthorized")
	public void checkUserOrderUnauthorized() {
		ValidatableResponse getOrdersResponse = ordersClient.getOrdersUnauthorized();

		ordersClientAssertions.getOrderUnauthorized(getOrdersResponse);
	}

	@After
	public void deleteUser() {
		if (accessToken != null) {
			userClient.deleteUser(accessToken);
		}
	}
}
