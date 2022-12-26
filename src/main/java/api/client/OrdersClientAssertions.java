package api.client;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

public class OrdersClientAssertions {

	@Step("Check get order unauthorized")
	public void getOrderUnauthorized(ValidatableResponse response) {
		response
				.assertThat().statusCode(SC_UNAUTHORIZED).and()
				.assertThat().body("success", equalTo(false)).and()
				.assertThat().body("message", equalTo("You should be authorised"));

	}

	@Step("Check created order")
	public void checkCreatedOrder(ValidatableResponse response, String orderId, int orderNumber) {
		response
				.assertThat().statusCode(SC_OK).and()
				.assertThat().body("success", equalTo(true)).and()
				.assertThat().body("orders[0]._id", equalTo(orderId)).and()
				.assertThat().body("orders[0].number", equalTo(orderNumber)).and()
				.assertThat().body("orders[0].ingredients", notNullValue());
	}

	@Step("Check order not created with incorrect hash ingredients")
	public void checkOrderNotCreatedIncorrectHash(ValidatableResponse response) {
		response.assertThat().statusCode(SC_INTERNAL_SERVER_ERROR);
	}

	@Step("Check order not created without ingredients")
	public void checkOrderNotCreatedWithoutIngredients(ValidatableResponse response) {
		response
				.assertThat().statusCode(SC_BAD_REQUEST).and()
				.assertThat().body("success", equalTo(false)).and()
				.assertThat().body("message", equalTo("Ingredient ids must be provided"));
	}

	@Step("Check order created successfully")
	public void checkOrderCreatedSuccess(ValidatableResponse response) {
		response
				.assertThat().statusCode(SC_OK).and()
				.assertThat().body("success", equalTo(true)).and()
				.assertThat().body("order.number", notNullValue());
	}
}
