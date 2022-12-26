import api.client.UserClient;
import api.client.UserClientAssertions;
import api.pojo.AuthUser;
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

public class AuthRegisterTests {

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
	}

	@Test
	@DisplayName("User creation")
	@Description("Basic test for positive user creation")
	public void checkUserCreation() {
		ValidatableResponse registerUserResponse = userClient.registerUserResponse(new AuthUser(email, password, name));

		accessToken = registerUserResponse.extract().jsonPath().getString("accessToken");

		userClientAssertions.checkUserRegisteredSuccess(registerUserResponse, email, name);
	}

	@Test
	@DisplayName("Can not create two the same users")
	@Description("Negative test for two the same user creation")
	public void checkCanNotCreateTwoTheSameUsers() {
		ValidatableResponse firstUserResponse = userClient.registerUserResponse(new AuthUser(email, password, name));
		ValidatableResponse secondUserResponse = userClient.registerUserResponse(new AuthUser(email, password, name));

		userClientAssertions.checkUserRegisteredSuccess(firstUserResponse, email, name);

		accessToken = firstUserResponse.extract().jsonPath().getString("accessToken");

		userClientAssertions.checkUserRegisteredExists(secondUserResponse);
	}

	@Test
	@DisplayName("Create user without mandatory field")
	@Description("User will not be created if the password is empty")
	public void checkUserNotCreatedWithoutMandatoryField() {
		ValidatableResponse registerUserResponse = userClient.registerUserResponse(new AuthUser(email, "", name));

		userClientAssertions.checkUserRegisteredHasNoEmailOrPw(registerUserResponse);
	}

	@After
	public void deleteUser() {
		if (accessToken != null) {
			userClient.deleteUser(accessToken);
		}
	}
}
