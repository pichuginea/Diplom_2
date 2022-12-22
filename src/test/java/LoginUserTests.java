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
import static org.hamcrest.Matchers.equalTo;

public class LoginUserTests {

	@BeforeClass
	public static void log() {
		RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
	}

	private String email;
	private String password;
	private String name;
	private String authToken = null;


	@Before
	public void setUp() {
		RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";

		email = "CreateEmail" + System.currentTimeMillis() + "@yandex.ru";
		password = "CreatePw" + System.currentTimeMillis();
		name = "CreateName" + System.currentTimeMillis();

		authToken = createUser(email, password, name).jsonPath().getString("accessToken");
		;
	}

	@Test
	@DisplayName("User login")
	@Description("Basic test for positive user authentication")
	public void checkUserAuthenticated() {
		Response response = loginUser(email, password);

		Assert.assertEquals(SC_OK, response.statusCode());
		Assert.assertEquals("true", response.jsonPath().getString("success"));
		Assert.assertEquals(email.toLowerCase(), response.jsonPath().getString("user.email"));
		Assert.assertEquals(name, response.jsonPath().getString("user.name"));
		Assert.assertNotNull(authToken);
		Assert.assertNotNull(response.jsonPath().getString("refreshToken"));
	}

	@Test
	@DisplayName("Login with incorrect email")
	@Description("Error must be returned if try to login with incorrect email")
	public void checkUserCanNotLoginWithIncorrectEmail() {
		Response response = loginUser("email", password);

		response.then()
				.assertThat().body("success", equalTo(false))
				.assertThat().body("message", equalTo("email or password are incorrect"))
				.and()
				.statusCode(SC_UNAUTHORIZED);
	}

	@Test
	@DisplayName("Login with incorrect password")
	@Description("Error must be returned if try to login with incorrect password")
	public void checkUserCanNotLoginWithIncorrectPassword() {
		Response response = loginUser(email, "password");

		response.then()
				.assertThat().body("success", equalTo(false))
				.assertThat().body("message", equalTo("email or password are incorrect"))
				.and()
				.statusCode(SC_UNAUTHORIZED);
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
	@Step("Delete courier. Send DELETE request to /api/auth/user")
	public void deleteUser() {
		given()
				.contentType(ContentType.JSON)
				.auth().oauth2(authToken.replace("Bearer ", ""))
				.when()
				.delete("/api/auth/user")
				.then()
				.assertThat().statusCode(SC_ACCEPTED);
	}
}
