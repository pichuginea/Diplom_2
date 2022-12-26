package api.client;

import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_ACCEPTED;

public class UserClient {

	@Step("Get user. Send GET request to /api/auth/user")
	public ValidatableResponse getUser(String accessToken) {
		return given()
				.contentType(ContentType.JSON)
				.auth().oauth2(accessToken.replace("Bearer ", ""))
				.and()
				.when()
				.get("/api/auth/user")
				.then();
	}

	@Step("Get user unauthorized. Send GET request to /api/auth/user")
	public ValidatableResponse getUserUnauthorized() {
		return given()
				.contentType(ContentType.JSON)
				.and()
				.when()
				.get("/api/auth/user")
				.then();
	}

	@Step("Edit user unauthorized. Send PATCH request to /api/auth/user")
	public ValidatableResponse editUserUnauthorized(Object body) {
		return given()
				.contentType(ContentType.JSON)
				.and()
				.body(body)
				.when()
				.patch("/api/auth/user")
				.then();
	}

	@Step("Edit user. Send PATCH request to /api/auth/user")
	public ValidatableResponse editUserResponse(Object body, String accessToken) {
		return given()
				.contentType(ContentType.JSON)
				.auth().oauth2(accessToken.replace("Bearer ", ""))
				.and()
				.body(body)
				.when()
				.patch("/api/auth/user")
				.then();
	}

	@Step("Login user. Send POST request to /api/auth/login")
	public ValidatableResponse loginUserResponse(Object body) {
		return given()
				.contentType(ContentType.JSON)
				.and()
				.body(body)
				.when()
				.post("/api/auth/login")
				.then();
	}

	@Step("Create user. Send POST request to /api/auth/register")
	public ValidatableResponse registerUserResponse(Object body) {
		return given()
				.contentType(ContentType.JSON)
				.and()
				.body(body)
				.when()
				.post("/api/auth/register")
				.then();
	}

	@Step("Delete user. Send DELETE request to /api/auth/user")
	public void deleteUser(String accessToken) {
		given()
				.contentType(ContentType.JSON)
				.auth().oauth2(accessToken.replace("Bearer ", ""))
				.when()
				.delete("/api/auth/user")
				.then()
				.assertThat().statusCode(SC_ACCEPTED);
	}
}
