import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.*;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.*;

public class OrdersTests {

	@BeforeClass
	public static void log() {
		RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
	}

	private String email;
	private String password;
	private String name;
	private String authToken;
	private String ingredient1;
	private String ingredient2;

	@Before
	public void setUp() {
		RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";

		email = "CreateEmail" + System.currentTimeMillis() + "@yandex.ru";
		password = "CreatePw" + System.currentTimeMillis();
		name = "CreateName" + System.currentTimeMillis();
		authToken = createUser(email, password, name).jsonPath().getString("accessToken");
		Response ingredients = getIngredients();
		ingredient1 = ingredients.jsonPath().getString("data[0]._id");
		ingredient2 = ingredients.jsonPath().getString("data[1]._id");
	}

	@Test
	@DisplayName("Order creation")
	@Description("Basic test for positive order creation with ingredients")
	public void checkOrderCreation() {
		Response order = createOrder(ingredient1, ingredient2);

		Assert.assertEquals(SC_OK, order.statusCode());
		Assert.assertEquals("true", order.jsonPath().getString("success"));
		Assert.assertNotNull(order.jsonPath().getString("order.number"));
	}

	@Test
	@DisplayName("Order unauthorized creation")
	@Description("Сase for order creation when user is unauthorized. Order must be created")
	public void checkSuccessOrderCreationUnauthorized() {
		String orderRequestBody = "{ \"ingredients\" : [\"" + ingredient1 + "\", \"" + ingredient2 + "\"]}";

		Response order = given()
				.contentType(ContentType.JSON)
				.and()
				.body(orderRequestBody)
				.when()
				.post("/api/orders")
				.then()
				.extract().response();

		Assert.assertEquals(SC_OK, order.statusCode());
		Assert.assertEquals("true", order.jsonPath().getString("success"));
		Assert.assertNotNull(order.jsonPath().getString("order.number"));
	}

	@Test
	@DisplayName("Order creation without ingredients")
	@Description("Negative case for order creation order has no ingredients")
	public void checkOrderCreationWithoutIngredients() {
		Response order = given()
				.contentType(ContentType.JSON)
				.auth().oauth2(authToken.replace("Bearer ", ""))
				.and()
				.body("{ \"ingredients\" : []}")
				.when()
				.post("/api/orders")
				.then()
				.extract().response();

		Assert.assertEquals(SC_BAD_REQUEST, order.statusCode());
		Assert.assertEquals("false", order.jsonPath().getString("success"));
		Assert.assertEquals("Ingredient ids must be provided", order.jsonPath().getString("message"));
	}

	@Test
	@DisplayName("Incorrect hash ingredients")
	@Description("Negative case for order creation order with incorrect ingredients hash")
	public void checkOrderCreationWithIncorrectHash() {
		Response order = createOrder("incorrect_hash_1", "incorrect_hash_2");

		Assert.assertEquals(SC_INTERNAL_SERVER_ERROR, order.statusCode());
	}

	@Test
	@DisplayName("Get user orders")
	@Description("Basic test for positive user order getting")
	public void checkUserOrderGetting() {
		Response order = createOrder(ingredient1, ingredient2);
		String orderId = order.jsonPath().getString("order._id");
		String orderNumber = order.jsonPath().getString("order.number");

		Response getOrder = getOrders();

		Assert.assertEquals(SC_OK, order.statusCode());
		Assert.assertEquals("true", getOrder.jsonPath().getString("success"));
		Assert.assertEquals(orderId, getOrder.jsonPath().getString("orders[0]._id"));
		Assert.assertEquals(orderNumber, getOrder.jsonPath().getString("orders[0].number"));
		Assert.assertTrue(getOrder.jsonPath().getString("orders[0].ingredients").contains(ingredient1) &&
				getOrder.jsonPath().getString("orders[0].ingredients").contains(ingredient2));
	}

	@Test
	@DisplayName("Get user order unauthorized")
	@Description("Negative case for user order getting when user is unauthorized")
	public void checkUserOrderUnauthorized() {
		//Request without authorization
		Response getOrder = given()
				.contentType(ContentType.JSON)
				.and()
				.when()
				.get("/api/orders")
				.then()
				.extract().response();

		Assert.assertEquals(SC_UNAUTHORIZED, getOrder.statusCode());
		Assert.assertEquals("false", getOrder.jsonPath().getString("success"));
		Assert.assertEquals("You should be authorised", getOrder.jsonPath().getString("message"));
	}

	@Step("Get ingredients. Send GET request to /api/orders")
	public Response getOrders() {
		return given()
				.contentType(ContentType.JSON)
				.auth().oauth2(authToken.replace("Bearer ", ""))
				.and()
				.when()
				.get("/api/orders")
				.then()
				.extract().response();
	}

	@Step("Create order. Send POST request to /api/orders")
	public Response createOrder(String ingredient1, String ingredient2) {
		String orderRequestBody = "{ \"ingredients\" : [\"" + ingredient1 + "\", \"" + ingredient2 + "\"]}";

		return given()
				.contentType(ContentType.JSON)
				.auth().oauth2(authToken.replace("Bearer ", ""))
				.and()
				.body(orderRequestBody)
				.when()
				.post("/api/orders")
				.then()
				.extract().response();
	}

	@Step("Get ingredients. Send GET request to /api/ingredients")
	public Response getIngredients() {
		return given()
				.contentType(ContentType.JSON)
				.and()
				.when()
				.get("/api/ingredients")
				.then()
				.extract().response();
	}

	@Step("Create user. Send POST request to /api/auth/register")
	public Response createUser(String email, String password, String name) {
		String requestBody = "{ \"email\" : \"" + email + "\", \"password\":\"" + password + "\", \"name\":\"" + name + "\"}";

		return given()
				.contentType(ContentType.JSON)
				.and()
				.body(requestBody)
				.when()
				.post("/api/auth/register")
				.then()
				.extract().response();
	}

	@After
	@Step("Delete user. Send DELETE request to /api/auth/user")
	public void deleteUser() {
		if (authToken != null) {
			given()
					.contentType(ContentType.JSON)
					.auth().oauth2(authToken.replace("Bearer ", ""))
					.when()
					.delete("/api/auth/user")
					.then()
					.assertThat().statusCode(SC_ACCEPTED);
		}
	}
}
