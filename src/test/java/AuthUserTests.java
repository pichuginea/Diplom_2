import api.client.UserClient;
import api.client.UserClientAssertions;
import api.pojo.AuthLogin;
import api.pojo.AuthRegister;
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

public class AuthUserTests {

	private String email;
	private String password;
	private String name;
	private String accessToken;
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

		ValidatableResponse registerUserResponse = userClient.registerUserResponse(new AuthRegister(email, password, name));

		accessToken = registerUserResponse.extract().jsonPath().getString("accessToken");
	}

	@Test
	@DisplayName("User getting")
	@Description("Basic test for positive user getting")
	public void checkUserGetting() {
		ValidatableResponse getUserResponse = userClient.getUser(accessToken);

		userClientAssertions.checkGetUserSuccess(getUserResponse, email, name);
	}

	@Test
	@DisplayName("User getting")
	@Description("Negative test for user getting when user is unauthorized")
	public void checkUserGettingUnauthorized() {
		ValidatableResponse getUserResponse = userClient.getUserUnauthorized();

		userClientAssertions.checkUserUnauthorized(getUserResponse);
	}

	@Test
	@DisplayName("User editing")
	@Description("Basic test for positive user editing. Change email, password, name fields and check it")
	public void checkUserEditing() {
		ValidatableResponse editUserResponse = userClient.editUserResponse(new AuthUser("new" + email, "new" + password, "new" + name), accessToken);

		userClientAssertions.checkUserEditedSuccess(editUserResponse, "new" + email, "new" + name);

		//Login with new credentials to check if password changed
		ValidatableResponse loginUserResponse = userClient.loginUserResponse(new AuthLogin("new" + email, "new" + password));
		userClientAssertions.checkUserLoginSuccess(loginUserResponse, "new" + email, "new" + name);
	}

	@Test
	@DisplayName("Email already exists")
	@Description("Check User with such email already exists message")
	public void checkCanNotEditExistingEmail() {
		ValidatableResponse registerUserResponse = userClient.registerUserResponse(new AuthRegister("new" + email, "new" + password, "new" + name));

		accessToken = registerUserResponse.extract().jsonPath().getString("accessToken");

		ValidatableResponse editSecondUserResponse = userClient.editUserResponse(new AuthUser(email, password, name), accessToken);

		userClientAssertions.checkEmailAlreadyExists(editSecondUserResponse);
	}

	@Test
	@DisplayName("User editing")
	@Description("Negative test for user editing when user is unauthorized")
	public void checkUserEditingUnauthorized() {
		ValidatableResponse editUserResponse = userClient.editUserUnauthorized(new AuthUser(email, password, name));

		userClientAssertions.checkUserUnauthorized(editUserResponse);
	}

	@After
	public void deleteUser() {
		if (accessToken != null) {
			userClient.deleteUser(accessToken);
		}
	}
}
