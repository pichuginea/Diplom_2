import api.client.UserClient;
import api.client.UserClientAssertions;
import api.pojo.AuthLogin;
import api.pojo.AuthUser;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class AuthLoginTests {

	private String email;
	private String password;
	private String name;
	private String accessToken = null;
	private UserClient userClient = new UserClient();
	private UserClientAssertions userClientAssertions = new UserClientAssertions();

	@BeforeClass
	public static void log() {
		RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
	}

	@Before
	public void setUp() {
		RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";

		email = "CreateEmail" + System.currentTimeMillis() + "@yandex.ru";
		password = "CreatePw" + System.currentTimeMillis();
		name = "CreateName" + System.currentTimeMillis();

		ValidatableResponse registerUserResponse = userClient.registerUserResponse(new AuthUser(email, password, name));

		accessToken = registerUserResponse.extract().jsonPath().getString("accessToken");
	}

	@Test
	@DisplayName("User login")
	@Description("Basic test for positive user authentication")
	public void checkUserAuthenticated() {
		ValidatableResponse loginUserResponse = userClient.loginUserResponse(new AuthLogin(email, password));

		userClientAssertions.checkUserLoginSuccess(loginUserResponse, email, name);
	}

	@Test
	@DisplayName("Login with incorrect email")
	@Description("Error must be returned if try to login with incorrect email")
	public void checkUserCanNotLoginWithIncorrectEmail() {
		ValidatableResponse loginUserResponse = userClient.loginUserResponse(new AuthLogin("email", password));

		userClientAssertions.checkUserLoginEmailOrPwIncorrect(loginUserResponse);
	}

	@Test
	@DisplayName("Login with incorrect password")
	@Description("Error must be returned if try to login with incorrect password")
	public void checkUserCanNotLoginWithIncorrectPassword() {
		ValidatableResponse loginUserResponse = userClient.loginUserResponse(new AuthLogin(email, "password"));

		userClientAssertions.checkUserLoginEmailOrPwIncorrect(loginUserResponse);
	}

	@After
	@Step("Delete courier. Send DELETE request to /api/auth/user")
	public void deleteUser() {
		userClient.deleteUser(accessToken);
	}
}
