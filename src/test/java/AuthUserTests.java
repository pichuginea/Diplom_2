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

public class AuthUserTests {

	@BeforeClass
	public static void log() {
		RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
	}

	private String email;
	private String password;
	private String name;
	private String authToken;

	@Before
	public void setUp() {
		RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";

		email = "CreateEmail" + System.currentTimeMillis() + "@yandex.ru";
		password = "CreatePw" + System.currentTimeMillis();
		name = "CreateName" + System.currentTimeMillis();
		authToken = createUser(email, password, name).jsonPath().getString("accessToken");
	}

	@Test
	@DisplayName("User getting")
	@Description("Basic test for positive user getting")
	public void checkUserGetting() {
		Response getUser = getUser();

		Assert.assertEquals(SC_OK, getUser.statusCode());
		Assert.assertEquals("true", getUser.jsonPath().getString("success"));
		Assert.assertEquals(email.toLowerCase(), getUser.jsonPath().getString("user.email"));
		Assert.assertEquals(name, getUser.jsonPath().getString("user.name"));
	}

	@Test
	@DisplayName("User getting")
	@Description("Negative test for user getting when user is unauthorized")
	public void checkUserGettingUnauthorized() {
		//Request without authorization
		Response getUser = given()
				.contentType(ContentType.JSON)
				.and()
				.when()
				.get("/api/auth/user")
				.then()
				.extract().response();

		Assert.assertEquals(SC_UNAUTHORIZED, getUser.statusCode());
		Assert.assertEquals("false", getUser.jsonPath().getString("success"));
		Assert.assertEquals("You should be authorised", getUser.jsonPath().getString("message"));
	}

	@Test
	@DisplayName("User editing")
	@Description("Basic test for positive user editing. Change email, password, name fields and check it")
	public void checkUserEditing() {
		Response editUser = editUser("new" + email, "new" + password, "new" + name);

		Assert.assertEquals(SC_OK, editUser.statusCode());
		Assert.assertEquals("true", editUser.jsonPath().getString("success"));
		Assert.assertEquals("new" + email.toLowerCase(), editUser.jsonPath().getString("user.email"));
		Assert.assertEquals("new" + name, editUser.jsonPath().getString("user.name"));

		//Login with new credentials to check if password changed
		Response loginUser = loginUser("new" + email, "new" + password);
		Assert.assertEquals(SC_OK, loginUser.statusCode());
	}

	@Test
	@DisplayName("Email already exists")
	@Description("Check User with such email already exists message")
	public void checkCanNotEditExistingEmail() {
		authToken = createUser("new" + email, "new" + password, "new" + name).jsonPath().getString("accessToken");

		//Edit second user with data from the first one
		Response editUser = editUser(email, password, name);

		Assert.assertEquals(SC_FORBIDDEN, editUser.statusCode());
		Assert.assertEquals("false", editUser.jsonPath().getString("success"));
		Assert.assertEquals("User with such email already exists", editUser.jsonPath().getString("message"));
	}

	@Test
	@DisplayName("User editing")
	@Description("Negative test for user editing when user is unauthorized")
	public void checkUserEditingUnauthorized() {
		//Request without authorization
		String editRequestBody = "{ \"email\" : \"" + email + "\", \"password\":\"" + password + "\", \"name\":\"" + name + "\"}";

		Response editUser = given()
				.contentType(ContentType.JSON)
				.and()
				.body(editRequestBody)
				.when()
				.patch("/api/auth/user")
				.then()
				.extract().response();

		Assert.assertEquals(SC_UNAUTHORIZED, editUser.statusCode());
		Assert.assertEquals("false", editUser.jsonPath().getString("success"));
		Assert.assertEquals("You should be authorised", editUser.jsonPath().getString("message"));
	}

	@Step("Get user. Send GET request to /api/auth/user")
	public Response getUser() {
		return given()
				.contentType(ContentType.JSON)
				.auth().oauth2(authToken.replace("Bearer ", ""))
				.and()
				.when()
				.get("/api/auth/user")
				.then()
				.extract().response();
	}

	@Step("Edit user. Send PATCH request to /api/auth/user")
	public Response editUser(String email, String password, String name) {
		String editRequestBody = "{ \"email\" : \"" + email + "\", \"password\":\"" + password + "\", \"name\":\"" + name + "\"}";

		return given()
				.contentType(ContentType.JSON)
				.auth().oauth2(authToken.replace("Bearer ", ""))
				.and()
				.body(editRequestBody)
				.when()
				.patch("/api/auth/user")
				.then()
				.extract().response();
	}

	@Step("Login user. Send POST request to /api/auth/login")
	public Response loginUser(String email, String password) {
		String loginRequestBody = "{ \"email\" : \"" + email + "\", \"password\":\"" + password + "\"}";

		return given()
				.contentType(ContentType.JSON)
				.and()
				.body(loginRequestBody)
				.when()
				.post("/api/auth/login")
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
