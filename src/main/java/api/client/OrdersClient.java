package api.client;

import api.pojo.Orders;
import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import static io.restassured.RestAssured.given;

public class OrdersClient {

	@Step("Get orders. Send GET request to /api/orders")
	public ValidatableResponse getOrdersResponse(String accessToken) {
		return given()
				.contentType(ContentType.JSON)
				.auth().oauth2(accessToken.replace("Bearer ", ""))
				.and()
				.when()
				.get("/api/orders")
				.then();
	}

	@Step("Get orders unauthorized. Send GET request to /api/orders")
	public ValidatableResponse getOrdersUnauthorized() {
		return given()
				.contentType(ContentType.JSON)
				.and()
				.when()
				.get("/api/orders")
				.then();
	}

	@Step("Get ingredients. Send GET request to /api/ingredients")
	public ValidatableResponse getIngredientsResponse() {
		return given()
				.contentType(ContentType.JSON)
				.and()
				.when()
				.get("/api/ingredients")
				.then();
	}

	@Step("Create order. Send POST request to /api/orders")
	public ValidatableResponse createOrderResponse(Orders ingredients, String accessToken) {
		return given()
				.contentType(ContentType.JSON)
				.auth().oauth2(accessToken.replace("Bearer ", ""))
				.and()
				.body(ingredients)
				.when()
				.post("/api/orders")
				.then();
	}

	@Step("Create order unauthorized. Send POST request to /api/orders")
	public ValidatableResponse createOrderUnauthorized(Orders ingredients) {
		return given()
				.contentType(ContentType.JSON)
				.and()
				.body(ingredients)
				.when()
				.post("/api/orders")
				.then();
	}
}
