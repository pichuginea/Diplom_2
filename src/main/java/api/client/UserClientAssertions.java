package api.client;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class UserClientAssertions {

	@Step("Check email already exists")
	public void checkEmailAlreadyExists(ValidatableResponse response) {
		response
				.assertThat().statusCode(SC_FORBIDDEN)
				.assertThat().body("success", equalTo(false)).and()
				.assertThat().body("message", equalTo("User with such email already exists"));
	}

	@Step("Check user edited successfully")
	public void checkUserEditedSuccess(ValidatableResponse response, String email, String name) {
		response
				.assertThat().statusCode(SC_OK)
				.assertThat().body("success", equalTo(true)).and()
				.assertThat().body("user.email", equalTo(email.toLowerCase())).and()
				.assertThat().body("user.name", equalTo(name));
	}

	@Step("Check get user unauthorized")
	public void checkUserUnauthorized(ValidatableResponse response) {
		response
				.assertThat().statusCode(SC_UNAUTHORIZED)
				.assertThat().body("success", equalTo(false)).and()
				.assertThat().body("message", equalTo("You should be authorised"));
	}

	@Step("Check get user")
	public void checkGetUserSuccess(ValidatableResponse response, String email, String name) {
		response
				.assertThat().statusCode(SC_OK)
				.assertThat().body("success", equalTo(true)).and()
				.assertThat().body("user.email", equalTo(email.toLowerCase())).and()
				.assertThat().body("user.name", equalTo(name));
	}

	@Step("Check email or password are incorrect on login")
	public void checkUserLoginEmailOrPwIncorrect(ValidatableResponse response) {
		response
				.assertThat().statusCode(SC_UNAUTHORIZED)
				.assertThat().body("success", equalTo(false)).and()
				.assertThat().body("message", equalTo("email or password are incorrect"));
	}

	@Step("Check user logged in successfully")
	public void checkUserLoginSuccess(ValidatableResponse response, String email, String name) {
		response
				.assertThat().statusCode(SC_OK)
				.assertThat().body("success", equalTo(true)).and()
				.assertThat().body("user.email", equalTo(email.toLowerCase())).and()
				.assertThat().body("user.name", equalTo(name)).and()
				.assertThat().body("accessToken", notNullValue());
	}

	@Step("Check user registered successfully")
	public void checkUserRegisteredSuccess(ValidatableResponse registerUserResponse, String email, String name) {
		registerUserResponse
				.assertThat().statusCode(SC_OK)
				.assertThat().body("success", equalTo(true)).and()
				.assertThat().body("user.email", equalTo(email.toLowerCase())).and()
				.assertThat().body("user.name", equalTo(name)).and()
				.assertThat().body("accessToken", notNullValue());

	}

	@Step("Check user registered already exists")
	public void checkUserRegisteredExists(ValidatableResponse response) {
		response.
				assertThat().statusCode(SC_FORBIDDEN).and()
				.assertThat().body("success", equalTo(false)).and()
				.assertThat().body("message", equalTo("User already exists"));

	}

	@Step("Check user registered has no email or password")
	public void checkUserRegisteredHasNoEmailOrPw(ValidatableResponse response) {
		response
				.assertThat().statusCode(SC_FORBIDDEN)
				.assertThat().body("success", equalTo(false)).and()
				.assertThat().body("message", equalTo("Email, password and name are required fields"));
	}
}
