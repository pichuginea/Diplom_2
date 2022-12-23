import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.*;

import static org.apache.http.HttpStatus.*;

import static io.restassured.RestAssured.given;

public class AuthRegisterTests {

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
	}

	@Test
	@DisplayName("User creation")
	@Description("Basic test for positive user creation")
	public void checkUserCreation() {
		Response response = createUser(email, password, name);

		authToken = response.jsonPath().getString("accessToken");

		Assert.assertEquals(SC_OK, response.statusCode());
		Assert.assertEquals("true", response.jsonPath().getString("success"));
		Assert.assertEquals(email.toLowerCase(), response.jsonPath().getString("user.email"));
		Assert.assertEquals(name, response.jsonPath().getString("user.name"));
		Assert.assertNotNull(authToken);
		Assert.assertNotNull(response.jsonPath().getString("refreshToken"));
	}

	@Test
	@DisplayName("Can not create two the same users")
	@Description("Negative test for two the same user creation")
	public void checkCanNotCreateTwoTheSameUsers() {
		Response firstUser = createUser(email, password, name);
		Response secondUser = createUser(email, password, name);

		authToken = firstUser.jsonPath().getString("accessToken");

		Assert.assertEquals(SC_OK, firstUser.statusCode());
		Assert.assertEquals(SC_FORBIDDEN, secondUser.statusCode());
		Assert.assertEquals("false", secondUser.jsonPath().getString("success"));
		Assert.assertEquals("User already exists", secondUser.jsonPath().getString("message"));
	}

	@Test
	@DisplayName("Create user without mandatory field")
	@Description("User will not be created if the password is empty")
	public void checkUserNotCreatedWithoutMandatoryField() {
		Response response = createUser(email, "", name);

		Assert.assertEquals(SC_FORBIDDEN, response.statusCode());
		Assert.assertEquals("false", response.jsonPath().getString("success"));
		Assert.assertEquals("Email, password and name are required fields", response.jsonPath().getString("message"));
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
